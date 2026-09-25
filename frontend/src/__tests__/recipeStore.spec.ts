import { describe, it, expect, beforeEach, vi } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useRecipeStore } from '../stores/recipeStore'

describe('recipeStore', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.restoreAllMocks()
  })

  it('initializes with empty recipes and loading false', () => {
    const store = useRecipeStore()
    expect(store.recipes).toEqual([])
    expect(store.loading).toBe(false)
    expect(store.error).toBeNull()
  })

  it('fetches recipes successfully', async () => {
    const mockRecipes = [
      {
        id: 1,
        title: 'Pasta Carbonara',
        description: 'Klassiker',
        instructions: 'Koka pasta',
        servings: 4,
        prepTimeMinutes: 10,
        cookTimeMinutes: 15,
        createdAt: '2026-09-25T12:00:00Z',
        updatedAt: null,
      },
    ]

    global.fetch = vi.fn().mockResolvedValue({
      ok: true,
      status: 200,
      json: async () => mockRecipes,
    } as Response)

    const store = useRecipeStore()
    await store.fetchRecipes()

    expect(store.recipes).toHaveLength(1)
    expect(store.recipes[0]?.title).toBe('Pasta Carbonara')
    expect(store.loading).toBe(false)
    expect(store.error).toBeNull()
  })

  it('handles fetch errors properly', async () => {
    global.fetch = vi.fn().mockResolvedValue({
      ok: false,
      status: 500,
      statusText: 'Internal Server Error',
      json: async () => ({ message: 'Database connection failed' }),
    } as Response)

    const store = useRecipeStore()
    await store.fetchRecipes()

    expect(store.recipes).toEqual([])
    expect(store.error).toBe('Database connection failed')
    expect(store.loading).toBe(false)
  })

  it('edits a recipe successfully', async () => {
    const store = useRecipeStore()
    store.recipes = [
      {
        id: 1,
        title: 'Original Recipe',
        description: 'Old desc',
        instructions: 'Step 1',
        servings: 4,
        prepTimeMinutes: 10,
        cookTimeMinutes: 20,
        ingredients: [],
        createdAt: '2026-09-25T12:00:00Z',
        updatedAt: null,
      },
    ]

    const updatedRecipe = {
      id: 1,
      title: 'Updated Recipe Title',
      description: 'New desc',
      instructions: 'Step 1, Step 2',
      servings: 6,
      prepTimeMinutes: 15,
      cookTimeMinutes: 25,
      ingredients: [],
      createdAt: '2026-09-25T12:00:00Z',
      updatedAt: '2026-09-25T13:00:00Z',
    }

    global.fetch = vi.fn().mockResolvedValue({
      ok: true,
      status: 200,
      json: async () => updatedRecipe,
    } as Response)

    const res = await store.editRecipe(1, {
      title: 'Updated Recipe Title',
      description: 'New desc',
      instructions: 'Step 1, Step 2',
      servings: 6,
      prepTimeMinutes: 15,
      cookTimeMinutes: 25,
    })

    expect(res).toEqual(updatedRecipe)
    expect(store.recipes[0]?.title).toBe('Updated Recipe Title')
    expect(store.recipes[0]?.servings).toBe(6)
  })
})
