import { describe, it, expect, beforeEach, vi } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { usePantryStore } from '../stores/pantryStore'

describe('pantryStore', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.restoreAllMocks()
  })

  it('fetches and counts items properly', async () => {
    const mockPantry = [
      {
        id: 1,
        ingredientId: 1,
        ingredientName: 'Spaghetti',
        category: 'PANTRY' as const,
        quantity: 500,
        unit: 'g',
        inStock: true,
        updatedAt: '2026-09-25T12:00:00Z',
      },
      {
        id: 2,
        ingredientId: 2,
        ingredientName: 'Guanciale',
        category: 'MEAT' as const,
        quantity: 0,
        unit: 'g',
        inStock: false,
        updatedAt: '2026-09-25T12:00:00Z',
      },
    ]

    global.fetch = vi.fn().mockResolvedValue({
      ok: true,
      status: 200,
      json: async () => mockPantry,
    } as Response)

    const store = usePantryStore()
    await store.fetchPantry()

    expect(store.items).toHaveLength(2)
    expect(store.inStockCount).toBe(1)
    expect(store.outOfStockCount).toBe(1)
    expect(store.isIngredientInStock(1)).toBe(true)
    expect(store.isIngredientInStock(2)).toBe(false)
  })

  it('filters items by stock status', async () => {
    const mockPantry = [
      {
        id: 1,
        ingredientId: 1,
        ingredientName: 'Spaghetti',
        category: 'PANTRY' as const,
        quantity: 500,
        unit: 'g',
        inStock: true,
        updatedAt: '2026-09-25T12:00:00Z',
      },
      {
        id: 2,
        ingredientId: 2,
        ingredientName: 'Guanciale',
        category: 'MEAT' as const,
        quantity: 0,
        unit: 'g',
        inStock: false,
        updatedAt: '2026-09-25T12:00:00Z',
      },
    ]

    global.fetch = vi.fn().mockResolvedValue({
      ok: true,
      status: 200,
      json: async () => mockPantry,
    } as Response)

    const store = usePantryStore()
    await store.fetchPantry()

    store.filterStock = 'IN_STOCK'
    expect(store.filteredItems).toHaveLength(1)
    expect(store.filteredItems[0]?.ingredientName).toBe('Spaghetti')

    store.filterStock = 'OUT_OF_STOCK'
    expect(store.filteredItems).toHaveLength(1)
    expect(store.filteredItems[0]?.ingredientName).toBe('Guanciale')
  })

  it('updates an item quantity and quantityLevel successfully', async () => {
    const store = usePantryStore()
    store.items = [
      {
        id: 1,
        ingredientId: 1,
        ingredientName: 'Spaghetti',
        category: 'PANTRY',
        quantity: 500,
        unit: 'g',
        quantityLevel: 'FULL',
        inStock: true,
        updatedAt: '2026-09-25T12:00:00Z',
      },
    ]

    const updatedItem = {
      id: 1,
      ingredientId: 1,
      ingredientName: 'Spaghetti',
      category: 'PANTRY' as const,
      quantity: 200,
      unit: 'g',
      quantityLevel: 'LOW' as const,
      inStock: true,
      updatedAt: '2026-09-25T13:00:00Z',
    }

    global.fetch = vi.fn().mockResolvedValue({
      ok: true,
      status: 200,
      json: async () => updatedItem,
    } as Response)

    const res = await store.updateItem(1, { quantity: 200, unit: 'g', quantityLevel: 'LOW' })

    expect(res).toEqual(updatedItem)
    expect(store.items[0]?.quantity).toBe(200)
    expect(store.items[0]?.quantityLevel).toBe('LOW')
  })
})
