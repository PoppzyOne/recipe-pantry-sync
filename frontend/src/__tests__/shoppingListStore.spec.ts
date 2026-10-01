import { describe, it, expect, beforeEach } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useShoppingListStore } from '../stores/shoppingListStore'

describe('shoppingListStore', () => {
  beforeEach(() => {
    localStorage.clear()
    setActivePinia(createPinia())
  })

  it('adds an item and persists to localStorage', () => {
    const store = useShoppingListStore()
    const item = store.addItem({
      name: 'Mjölk',
      category: 'DAIRY',
      amount: 1,
      unit: 'liter',
    })

    expect(store.items).toHaveLength(1)
    expect(item.name).toBe('Mjölk')
    expect(store.remainingCount).toBe(1)

    // Check localStorage
    const stored = JSON.parse(localStorage.getItem('recipe_pantry_sync_shopping_list_v1') || '[]')
    expect(stored).toHaveLength(1)
    expect(stored[0]?.name).toBe('Mjölk')
  })

  it('toggles checked status and calculates remaining count', () => {
    const store = useShoppingListStore()
    const item = store.addItem({ name: 'Smör', category: 'DAIRY' })

    expect(store.remainingCount).toBe(1)

    store.toggleItem(item.id)
    expect(store.remainingCount).toBe(0)
    expect(store.checkedItems).toHaveLength(1)

    store.clearChecked()
    expect(store.items).toHaveLength(0)
  })

  it('formats shopping list text correctly for sharing', () => {
    const store = useShoppingListStore()

    // When empty
    expect(store.getFormattedShareText()).toBe('🛒 Inköpslistan är tom!')

    // Add items
    store.addItem({ name: 'Morötter', category: 'PRODUCE', amount: 500, unit: 'g' })
    store.addItem({ name: 'Mjölk', category: 'DAIRY', amount: 1, unit: 'l' })

    const text = store.getFormattedShareText()
    expect(text).toContain('🛒 Inköpslista – Skafferiet')
    expect(text).toContain('🥬 Frukt & Grönt:')
    expect(text).toContain('• Morötter (500 g)')
    expect(text).toContain('🧀 Mejeri & Ost:')
    expect(text).toContain('• Mjölk (1 l)')
  })
})
