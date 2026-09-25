import { ref, computed } from 'vue'
import { defineStore } from 'pinia'
import { pantryApi } from '@/api/pantryApi'
import type { PantryItem, CreatePantryItemDto, UpdatePantryItemDto } from '@/types/pantry'
import type { IngredientCategory } from '@/types/recipe'

export const usePantryStore = defineStore('pantry', () => {
  const items = ref<PantryItem[]>([])
  const loading = ref<boolean>(false)
  const error = ref<string | null>(null)

  const activeCategory = ref<IngredientCategory | 'ALL'>('ALL')
  const filterStock = ref<'ALL' | 'IN_STOCK' | 'OUT_OF_STOCK'>('ALL')
  const searchQuery = ref<string>('')

  const inStockCount = computed(() => items.value.filter((i) => i.inStock).length)
  const outOfStockCount = computed(() => items.value.filter((i) => !i.inStock).length)

  const filteredItems = computed(() => {
    return items.value.filter((item) => {
      // Category filter
      if (activeCategory.value !== 'ALL' && item.category !== activeCategory.value) {
        return false
      }
      // Stock status filter
      if (filterStock.value === 'IN_STOCK' && !item.inStock) {
        return false
      }
      if (filterStock.value === 'OUT_OF_STOCK' && item.inStock) {
        return false
      }
      // Search query filter
      if (searchQuery.value.trim()) {
        const query = searchQuery.value.toLowerCase().trim()
        return item.ingredientName.toLowerCase().includes(query)
      }
      return true
    })
  })

  function isIngredientInStock(ingredientId: number): boolean {
    const found = items.value.find((i) => i.ingredientId === ingredientId)
    return !!found && found.inStock
  }

  async function fetchPantry(): Promise<void> {
    loading.value = true
    error.value = null
    try {
      items.value = await pantryApi.getAll()
    } catch (err) {
      error.value = err instanceof Error ? err.message : 'Kunde inte hämta skafferiet'
    } finally {
      loading.value = false
    }
  }

  async function addItem(dto: CreatePantryItemDto): Promise<PantryItem | null> {
    loading.value = true
    error.value = null
    try {
      const saved = await pantryApi.create(dto)
      const existingIdx = items.value.findIndex((i) => i.id === saved.id)
      if (existingIdx !== -1) {
        items.value[existingIdx] = saved
      } else {
        items.value.unshift(saved)
      }
      return saved
    } catch (err) {
      error.value = err instanceof Error ? err.message : 'Kunde inte spara varan i skafferiet'
      return null
    } finally {
      loading.value = false
    }
  }

  async function toggleStock(id: number): Promise<boolean> {
    const item = items.value.find((i) => i.id === id)
    if (!item) return false

    // Optimistic update
    item.inStock = !item.inStock
    try {
      const updated = await pantryApi.toggleStock(id)
      item.inStock = updated.inStock
      item.updatedAt = updated.updatedAt
      return true
    } catch (err) {
      // Rollback on failure
      item.inStock = !item.inStock
      error.value = err instanceof Error ? err.message : 'Kunde inte uppdatera lagerstatus'
      return false
    }
  }

  async function updateItem(id: number, dto: UpdatePantryItemDto): Promise<PantryItem | null> {
    try {
      const updated = await pantryApi.update(id, dto)
      const index = items.value.findIndex((i) => i.id === id)
      if (index !== -1) {
        items.value[index] = updated
      }
      return updated
    } catch (err) {
      error.value = err instanceof Error ? err.message : 'Kunde inte uppdatera skafferivara'
      return null
    }
  }

  async function removeItem(id: number): Promise<boolean> {
    try {
      await pantryApi.delete(id)
      items.value = items.value.filter((i) => i.id !== id)
      return true
    } catch (err) {
      error.value = err instanceof Error ? err.message : 'Kunde inte ta bort skafferivara'
      return false
    }
  }

  function clearError() {
    error.value = null
  }

  return {
    items,
    loading,
    error,
    activeCategory,
    filterStock,
    searchQuery,
    inStockCount,
    outOfStockCount,
    filteredItems,
    isIngredientInStock,
    fetchPantry,
    addItem,
    toggleStock,
    updateItem,
    removeItem,
    clearError,
  }
})
