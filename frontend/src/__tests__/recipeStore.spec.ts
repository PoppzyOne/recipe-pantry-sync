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

    global.fetch = vi.fn<typeof fetch>().mockResolvedValue({
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
    global.fetch = vi.fn<typeof fetch>().mockResolvedValue({
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

    global.fetch = vi.fn<typeof fetch>().mockResolvedValue({
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

  it('filters recipes by title, ingredients, and max cooking time', () => {
    const store = useRecipeStore()
    store.recipes = [
      {
        id: 1,
        title: 'Köttbullar med mos',
        description: 'Svensk klassiker',
        instructions: 'Stek',
        servings: 4,
        prepTimeMinutes: 15,
        cookTimeMinutes: 20, // total 35 min
        ingredients: [
          { id: 1, ingredientId: 10, name: 'Blandfärs', category: 'MEAT', amount: 500, unit: 'g', notes: null },
          { id: 2, ingredientId: 11, name: 'Potatis', category: 'PRODUCE', amount: 1, unit: 'kg', notes: null },
        ],
        createdAt: '2026-09-25T12:00:00Z',
        updatedAt: null,
      },
      {
        id: 2,
        title: 'Pannkakor',
        description: 'Frasiga',
        instructions: 'Vispa smet',
        servings: 4,
        prepTimeMinutes: 5,
        cookTimeMinutes: 15, // total 20 min
        ingredients: [
          { id: 3, ingredientId: 12, name: 'Mjölk', category: 'DAIRY', amount: 6, unit: 'dl', notes: null },
          { id: 4, ingredientId: 13, name: 'Ägg', category: 'DAIRY', amount: 3, unit: 'st', notes: null },
        ],
        createdAt: '2026-09-26T12:00:00Z',
        updatedAt: null,
      },
      {
        id: 3,
        title: 'Bolognese långkok',
        description: 'Mustig',
        instructions: 'Koka länge',
        servings: 6,
        prepTimeMinutes: 20,
        cookTimeMinutes: 70, // total 90 min
        ingredients: [
          { id: 5, ingredientId: 14, name: 'Nötfärs', category: 'MEAT', amount: 800, unit: 'g', notes: null },
        ],
        createdAt: '2026-09-27T12:00:00Z',
        updatedAt: null,
      },
    ]

    // Default: all 3 returned
    expect(store.filteredRecipes).toHaveLength(3)

    // Search by title
    store.searchQuery = 'pannkakor'
    expect(store.filteredRecipes).toHaveLength(1)
    expect(store.filteredRecipes[0]?.title).toBe('Pannkakor')

    // Search by ingredient ("Blandfärs")
    store.searchQuery = 'blandfärs'
    expect(store.filteredRecipes).toHaveLength(1)
    expect(store.filteredRecipes[0]?.title).toBe('Köttbullar med mos')

    // Filter by max time <= 30 min
    store.searchQuery = ''
    store.maxTimeFilter = 30
    expect(store.filteredRecipes).toHaveLength(1)
    expect(store.filteredRecipes[0]?.title).toBe('Pannkakor')

    // Filter by max time <= 45 min
    store.maxTimeFilter = 45
    expect(store.filteredRecipes).toHaveLength(2)

    // Reset filters
    store.resetFilters()
    expect(store.filteredRecipes).toHaveLength(3)
    expect(store.searchQuery).toBe('')
    expect(store.maxTimeFilter).toBe('ALL')
  })

  it('sorts recipes by title and total cooking time', () => {
    const store = useRecipeStore()
    store.recipes = [
      {
        id: 1,
        title: 'Ärtsoppa',
        description: null,
        instructions: null,
        servings: 4,
        prepTimeMinutes: 10,
        cookTimeMinutes: 50, // total 60 min
        ingredients: [],
        createdAt: '2026-09-25T12:00:00Z',
        updatedAt: null,
      },
      {
        id: 2,
        title: 'Bovetegröt',
        description: null,
        instructions: null,
        servings: 1,
        prepTimeMinutes: 2,
        cookTimeMinutes: 8, // total 10 min
        ingredients: [],
        createdAt: '2026-09-26T12:00:00Z',
        updatedAt: null,
      },
    ]

    // Sort by Title (A-Ö)
    store.sortBy = 'TITLE'
    expect(store.filteredRecipes[0]?.title).toBe('Bovetegröt')
    expect(store.filteredRecipes[1]?.title).toBe('Ärtsoppa')

    // Sort by Time (Fastest first)
    store.sortBy = 'TIME'
    expect(store.filteredRecipes[0]?.title).toBe('Bovetegröt')
    expect(store.filteredRecipes[1]?.title).toBe('Ärtsoppa')
  })
})
