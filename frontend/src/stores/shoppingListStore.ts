import { ref, computed, watch } from 'vue'
import { defineStore } from 'pinia'
import type { ShoppingListItem, AddShoppingListItemDto } from '@/types/shoppingList'
import type { RecipeIngredient } from '@/types/recipe'
import { recipeApi } from '@/api/recipeApi'
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
  const isOffline = ref<boolean>(typeof navigator !== 'undefined' ? !navigator.onLine : false)

  // Listen to network status for store awareness
  if (typeof window !== 'undefined') {
    window.addEventListener('online', () => {
      isOffline.value = false
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

  function addItem(dto: AddShoppingListItemDto): ShoppingListItem {
    // If item with same name exists and is unchecked, merge or increment amount
    const existing = items.value.find(
      (i) => i.name.toLowerCase().trim() === dto.name.toLowerCase().trim() && !i.checked
    )

    if (existing) {
      if (dto.amount && existing.amount && existing.unit === dto.unit) {
        existing.amount += dto.amount
      }
      return existing
    }

    const newItem: ShoppingListItem = {
      id: `item_${Date.now()}_${Math.random().toString(36).substring(2, 7)}`,
      ingredientId: dto.ingredientId,
      name: dto.name.trim(),
      category: dto.category || 'PANTRY',
      amount: dto.amount,
      unit: dto.unit,
      checked: false,
      recipeTitle: dto.recipeTitle,
      createdAt: new Date().toISOString(),
    }

    items.value.unshift(newItem)
    return newItem
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

  function toggleItem(id: string): void {
    const item = items.value.find((i) => i.id === id)
    if (item) {
      item.checked = !item.checked
    }
  }

  function removeItem(id: string): void {
    items.value = items.value.filter((i) => i.id !== id)
  }

  function clearChecked(): void {
    items.value = items.value.filter((i) => !i.checked)
  }

  function clearAll(): void {
    items.value = []
  }

  async function syncCheckedToPantry(): Promise<number> {
    const pantryStore = usePantryStore()
    const purchased = checkedItems.value

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

  return {
    items,
    isOffline,
    totalCount,
    remainingCount,
    uncheckedItems,
    checkedItems,
    itemsByCategory,
    addItem,
    addMissingIngredientsFromRecipe,
    toggleItem,
    removeItem,
    clearChecked,
    clearAll,
    syncCheckedToPantry,
  }
})
