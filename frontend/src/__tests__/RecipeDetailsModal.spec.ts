import { mount } from '@vue/test-utils'
import { describe, it, expect } from 'vitest'
import RecipeDetailsModal from '@/components/RecipeDetailsModal.vue'
import { createPinia, setActivePinia } from 'pinia'
import type { Recipe } from '@/types/recipe'

setActivePinia(createPinia())

const mockRecipe: Recipe = {
  id: 1,
  title: 'Test Recept',
  description: 'Beskrivning',
  instructions: 'Steg 1',
  servings: 4,
  prepTimeMinutes: 10,
  cookTimeMinutes: 20,
  ingredients: [
    { id: 1, ingredientId: 1, name: 'Ingrediens 1', category: 'PRODUCE', amount: 100, unit: 'g', notes: null },
    { id: 2, ingredientId: 2, name: 'Ingrediens 2', category: 'PANTRY', amount: 2, unit: 'st', notes: null },
  ],
  createdAt: '2026-10-23T00:00:00Z',
  updatedAt: null,
}

describe('RecipeDetailsModal.vue', () => {
  it('scales ingredients correctly when portions change', async () => {
    const wrapper = mount(RecipeDetailsModal, {
      props: { recipe: mockRecipe },
    })

    // Check initial state
    let ingredientAmounts = wrapper.findAll('.font-semibold.text-gray-700.bg-gray-50')
    expect(ingredientAmounts[0]).toBeDefined()
    expect(ingredientAmounts[0]?.text()).toContain('100 g')
    expect(ingredientAmounts[1]).toBeDefined()
    expect(ingredientAmounts[1]?.text()).toContain('2 st')

    // Find input and change servings from 4 to 8 (scaling factor = 2)
    const servingsInput = wrapper.find('input[type="number"]')
    await servingsInput.setValue(8)

    // Check scaled state
    ingredientAmounts = wrapper.findAll('.font-semibold.text-gray-700.bg-gray-50')
    expect(ingredientAmounts[0]).toBeDefined()
    expect(ingredientAmounts[0]?.text()).toContain('200 g')
    expect(ingredientAmounts[1]).toBeDefined()
    expect(ingredientAmounts[1]?.text()).toContain('4 st')

    // Change servings from 8 to 2 (scaling factor = 0.5)
    await servingsInput.setValue(2)

    // Check scaled state
    ingredientAmounts = wrapper.findAll('.font-semibold.text-gray-700.bg-gray-50')
    expect(ingredientAmounts[0]).toBeDefined()
    expect(ingredientAmounts[0]?.text()).toContain('50 g')
    expect(ingredientAmounts[1]).toBeDefined()
    expect(ingredientAmounts[1]?.text()).toContain('1 st')
  })

  it('verifies ingredient amounts scale proportionally', async () => {
    const wrapper = mount(RecipeDetailsModal, {
      props: { recipe: mockRecipe },
    })

    const servingsInput = wrapper.find('input[type="number"]')
    
    // Scale to 2 servings (4 -> 2, factor = 0.5)
    await servingsInput.setValue(2)
    let ingredientAmounts = wrapper.findAll('.font-semibold.text-gray-700.bg-gray-50')
    expect(ingredientAmounts[0]?.text()).toContain('50 g')
    expect(ingredientAmounts[1]?.text()).toContain('1 st')

    // Scale to 8 servings (4 -> 8, factor = 2)
    await servingsInput.setValue(8)
    ingredientAmounts = wrapper.findAll('.font-semibold.text-gray-700.bg-gray-50')
    expect(ingredientAmounts[0]?.text()).toContain('200 g')
    expect(ingredientAmounts[1]?.text()).toContain('4 st')
  })
})
