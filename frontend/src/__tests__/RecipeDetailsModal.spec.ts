import { describe, it, expect, beforeEach, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import RecipeDetailsModal from '../components/RecipeDetailsModal.vue'
import type { Recipe } from '@/types/recipe'

describe('RecipeDetailsModal', () => {
  const dummyRecipe: Recipe = {
    id: 42,
    title: 'Krämig Pasta Carbonara',
    description: 'En riktig klassiker med mycket smak.',
    instructions: '1. Koka pastan al dente.\n2. Stek fläsket krispigt.\n3. Rör ner ägg och ost.',
    servings: 4,
    prepTimeMinutes: 10,
    cookTimeMinutes: 15,
    ingredients: [
      {
        id: 1,
        ingredientId: 101,
        name: 'Spaghetti',
        category: 'PANTRY',
        amount: 400,
        unit: 'g',
        notes: 'al dente',
      },
      {
        id: 2,
        ingredientId: 102,
        name: 'Guanciale',
        category: 'MEAT',
        amount: 200,
        unit: 'g',
        notes: null,
      },
    ],
    createdAt: '2026-10-01T12:00:00Z',
    updatedAt: null,
  }

  beforeEach(() => {
    setActivePinia(createPinia())
    global.fetch = vi.fn<typeof fetch>().mockResolvedValue({
      ok: true,
      status: 200,
      json: async () => [],
    } as Response)
  })

  it('renders recipe details, description and total cooking time', () => {
    const wrapper = mount(RecipeDetailsModal, {
      props: { recipe: dummyRecipe },
    })

    expect(wrapper.text()).toContain('Krämig Pasta Carbonara')
    expect(wrapper.text()).toContain('En riktig klassiker med mycket smak.')
    expect(wrapper.text()).toContain('4 st') // Servings
    expect(wrapper.text()).toContain('10 min') // Prep time
    expect(wrapper.text()).toContain('15 min') // Cook time
    expect(wrapper.text()).toContain('25 min') // Total time (10 + 15)
  })

  it('formats instruction steps cleanly into individual numbered steps', () => {
    const wrapper = mount(RecipeDetailsModal, {
      props: { recipe: dummyRecipe },
    })

    expect(wrapper.text()).toContain('3 steg')
    expect(wrapper.text()).toContain('Koka pastan al dente.')
    expect(wrapper.text()).toContain('Stek fläsket krispigt.')
    expect(wrapper.text()).toContain('Rör ner ägg och ost.')
  })

  it('renders ingredients with amounts and units', () => {
    const wrapper = mount(RecipeDetailsModal, {
      props: { recipe: dummyRecipe },
    })

    expect(wrapper.text()).toContain('Spaghetti')
    expect(wrapper.text()).toContain('400 g')
    expect(wrapper.text()).toContain('Guanciale')
    expect(wrapper.text()).toContain('200 g')
  })

  it('emits close, edit and delete events properly', async () => {
    const wrapper = mount(RecipeDetailsModal, {
      props: { recipe: dummyRecipe },
    })

    const closeBtn = wrapper.find('button[title="Stäng (Esc)"]')
    await closeBtn.trigger('click')
    expect(wrapper.emitted('close')).toBeTruthy()

    const editBtn = wrapper.findAll('button').find((b) => b.text().includes('Redigera'))
    expect(editBtn).toBeDefined()
    await editBtn?.trigger('click')
    expect(wrapper.emitted('edit')?.[0]).toEqual([dummyRecipe])

    const deleteBtn = wrapper.findAll('button').find((b) => b.text().includes('Radera recept'))
    expect(deleteBtn).toBeDefined()
    await deleteBtn?.trigger('click')
    expect(wrapper.emitted('delete')?.[0]).toEqual([dummyRecipe.id])
  })
})
