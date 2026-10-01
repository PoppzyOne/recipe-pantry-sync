import { ref, computed, watch } from 'vue'
import { defineStore } from 'pinia'
import type { ShoppingListItem, AddShoppingListItemDto } from '@/types/shoppingList'
import type { RecipeIngredient } from '@/types/recipe'
import { recipeApi } from '@/api/recipeApi'
import { shoppingListApi } from '@/api/shoppingListApi'
import { usePantryStore } from './pantryStore'

const STORAGE_KEY = 'recipe_pantry_sync_shopping_list_v1'

function loadFromStorage(): ShoppingListItem[] {
  try {
    const raw = localStorage.getItem(STORAGE_KEY)
    if (!raw) return []
    return JSON.parse(raw) as ShoppingListItem[]
  } catch (e) {
    console.error('Failed to load shopping list from localStorage', e)
    return []
  }
}

function saveToStorage(items: ShoppingListItem[]): void {
  try {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(items))
  } catch (e) {
    console.error('Failed to save shopping list to localStorage', e)
  }
}

export const useShoppingListStore = defineStore('shoppingList', () => {
  const items = ref<ShoppingListItem[]>(loadFromStorage())
  const loading = ref<boolean>(false)
  const isSyncing = ref<boolean>(false)
  const error = ref<string | null>(null)
  const isOffline = ref<boolean>(typeof navigator !== 'undefined' ? !navigator.onLine : false)

  // Listen to network status for store awareness & auto-sync
  if (typeof window !== 'undefined') {
    window.addEventListener('online', () => {
      isOffline.value = false
      syncWithBackend()
    })
    window.addEventListener('offline', () => {
      isOffline.value = true
    })
  }

  // Auto-persist on any state change
  watch(
    items,
    (newItems) => {
      saveToStorage(newItems)
    },
    { deep: true, flush: 'sync' }
  )

  const uncheckedItems = computed(() => items.value.filter((i) => !i.checked))
  const checkedItems = computed(() => items.value.filter((i) => i.checked))

  const totalCount = computed(() => items.value.length)
  const remainingCount = computed(() => uncheckedItems.value.length)

  // Items grouped by category for efficient grocery store navigation
  const itemsByCategory = computed(() => {
    const map = new Map<string, ShoppingListItem[]>()
    for (const item of items.value) {
      const cat = item.category || 'OTHER'
      if (!map.has(cat)) {
        map.set(cat, [])
      }
      map.get(cat)!.push(item)
    }
    return map
  })

  // Synchronize with backend: push pending offline items and fetch updated server state
  async function syncWithBackend(): Promise<void> {
    if (isOffline.value) return

    isSyncing.value = true
    error.value = null
    try {
      // 1. Check for any items that were created offline and have temporary string IDs
      const pendingItems = items.value.filter(
        (i) => typeof i.id === 'string' && (i.id.startsWith('temp_') || i.id.startsWith('item_') || i.pendingSync)
      )

      if (pendingItems.length > 0) {
        const dtos: AddShoppingListItemDto[] = pendingItems.map((p) => ({
          ingredientId: p.ingredientId,
          name: p.name,
          category: p.category,
          amount: p.amount,
          unit: p.unit,
          recipeTitle: p.recipeTitle,
        }))
        await shoppingListApi.createBatch(dtos)
      }

      // 2. Fetch authoritative list from backend
      const serverItems = await shoppingListApi.getAll()
      items.value = serverItems
      saveToStorage(serverItems)
    } catch (err) {
      console.warn('Backend sync failed, falling back to local storage cache:', err)
      // Keep local state intact
    } finally {
      isSyncing.value = false
    }
  }

  // Initial fetch on store creation (skip in unit test environment)
  if (typeof window !== 'undefined' && navigator.onLine && import.meta.env.MODE !== 'test') {
    syncWithBackend()
  }

  function addItem(dto: AddShoppingListItemDto): ShoppingListItem {
    // Check if item with same name exists and is unchecked -> merge
    const existing = items.value.find(
      (i) => i.name.toLowerCase().trim() === dto.name.toLowerCase().trim() && !i.checked
    )

    if (existing) {
      if (dto.amount && existing.amount && existing.unit === dto.unit) {
        existing.amount = Math.round((existing.amount + dto.amount) * 100) / 100
      }
      // If online and it has a server ID, update on server
      if (!isOffline.value && typeof existing.id === 'number' && import.meta.env.MODE !== 'test') {
        shoppingListApi.update(existing.id, {
          amount: existing.amount,
          unit: existing.unit,
        }).catch((err) => console.warn('Failed to update existing item on server:', err))
      }
      return existing
    }

    // Optimistic local add
    const tempId = `temp_${Date.now()}_${Math.random().toString(36).substring(2, 7)}`
    const newItem: ShoppingListItem = {
      id: tempId,
      ingredientId: dto.ingredientId,
      name: dto.name.trim(),
      category: dto.category || 'PANTRY',
      amount: dto.amount,
      unit: dto.unit,
      checked: false,
      recipeTitle: dto.recipeTitle,
      createdAt: new Date().toISOString(),
      pendingSync: isOffline.value,
    }

    items.value.unshift(newItem)

    // Sync with backend if online
    if (!isOffline.value && import.meta.env.MODE !== 'test') {
      shoppingListApi
        .create(dto)
        .then((created) => {
          const idx = items.value.findIndex((i) => i.id === tempId)
          if (idx !== -1) {
            items.value[idx] = created
          }
        })
        .catch((err) => {
          console.warn('Failed to save item to server, kept locally as pending:', err)
          newItem.pendingSync = true
        })
    }

    return newItem
  }

  async function addBatch(dtos: AddShoppingListItemDto[]): Promise<number> {
    if (dtos.length === 0) return 0

    // Optimistic add of each
    for (const dto of dtos) {
      addItem(dto)
    }

    // If online, trigger background sync
    if (!isOffline.value) {
      syncWithBackend().catch((e) => console.warn('Background sync failed after batch add:', e))
    }

    return dtos.length
  }

  async function addMissingIngredientsFromRecipe(
    recipeId: number,
    recipeTitle: string
  ): Promise<number> {
    const missing: RecipeIngredient[] = await recipeApi.getMissingIngredients(recipeId)

    let addedCount = 0
    for (const ing of missing) {
      addItem({
        ingredientId: ing.ingredientId,
        name: ing.name,
        category: ing.category,
        amount: ing.amount,
        unit: ing.unit,
        recipeTitle: recipeTitle,
      })
      addedCount++
    }

    return addedCount
  }

  function toggleItem(id: number | string): void {
    const item = items.value.find((i) => i.id === id)
    if (item) {
      item.checked = !item.checked

      // Sync to backend if online and item has a server ID
      if (!isOffline.value && typeof id === 'number' && import.meta.env.MODE !== 'test') {
        shoppingListApi.toggleChecked(id).catch((err) => {
          console.warn('Failed to toggle item on server:', err)
        })
      }
    }
  }

  function removeItem(id: number | string): void {
    items.value = items.value.filter((i) => i.id !== id)

    if (!isOffline.value && typeof id === 'number' && import.meta.env.MODE !== 'test') {
      shoppingListApi.delete(id).catch((err) => {
        console.warn('Failed to delete item from server:', err)
      })
    }
  }

  function clearChecked(): void {
    items.value = items.value.filter((i) => !i.checked)

    if (!isOffline.value && import.meta.env.MODE !== 'test') {
      shoppingListApi.clearCompleted().catch((err) => {
        console.warn('Failed to clear completed items from server:', err)
      })
    }
  }

  function clearAll(): void {
    items.value = []
  }

  async function syncCheckedToPantry(): Promise<number> {
    const pantryStore = usePantryStore()
    const purchased = checkedItems.value
    if (purchased.length === 0) return 0

    // If online, use the atomic backend sync endpoint
    if (!isOffline.value && import.meta.env.MODE !== 'test') {
      try {
        const res = await shoppingListApi.syncToPantry()
        items.value = items.value.filter((i) => !i.checked)
        await pantryStore.fetchPantry()
        return res.syncedCount
      } catch (err) {
        console.warn('Backend syncToPantry failed, falling back to client-side sync:', err)
      }
    }

    // Client-side / offline fallback
    let syncedCount = 0
    for (const item of purchased) {
      await pantryStore.addItem({
        ingredientName: item.name,
        category: item.category,
        quantity: item.amount || 1,
        unit: item.unit || 'st',
        inStock: true,
      })
      syncedCount++
    }

    clearChecked()
    return syncedCount
  }

  function getFormattedShareText(): string {
    const pending = uncheckedItems.value
    if (pending.length === 0) {
      return '🛒 Inköpslistan är tom!'
    }

    const categoryNames: Record<string, { label: string; icon: string }> = {
      PRODUCE: { label: 'Frukt & Grönt', icon: '🥬' },
      DAIRY: { label: 'Mejeri & Ost', icon: '🧀' },
      MEAT: { label: 'Kött, Fågel & Fisk', icon: '🥩' },
      PANTRY: { label: 'Skafferi & Torrvaror', icon: '🌾' },
      SPICES: { label: 'Kryddor & Smaksättare', icon: '🧂' },
      BAKERY: { label: 'Bröd & Bakning', icon: '🍞' },
      FROZEN: { label: 'Frysvaror', icon: '🧊' },
      OTHER: { label: 'Övrigt', icon: '📦' },
    }

    // Group unchecked items by category
    const grouped = new Map<string, ShoppingListItem[]>()
    for (const item of pending) {
      const cat = item.category || 'OTHER'
      if (!grouped.has(cat)) {
        grouped.set(cat, [])
      }
      grouped.get(cat)!.push(item)
    }

    const lines: string[] = ['🛒 Inköpslista – Skafferiet', '']

    for (const [cat, catItems] of grouped.entries()) {
      const info = categoryNames[cat] || { label: cat, icon: '📦' }
      lines.push(`${info.icon} ${info.label}:`)
      for (const item of catItems) {
        let line = `• ${item.name}`
        if (item.amount) {
          line += ` (${item.amount} ${item.unit || ''})`.trimEnd()
        }
        lines.push(line)
      }
      lines.push('')
    }

    return lines.join('\n').trim()
  }

  return {
    items,
    loading,
    isSyncing,
    error,
    isOffline,
    totalCount,
    remainingCount,
    uncheckedItems,
    checkedItems,
    itemsByCategory,
    syncWithBackend,
    addItem,
    addBatch,
    addMissingIngredientsFromRecipe,
    toggleItem,
    removeItem,
    clearChecked,
    clearAll,
    syncCheckedToPantry,
    getFormattedShareText,
  }
})
