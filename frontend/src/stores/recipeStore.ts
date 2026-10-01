import { ref, computed } from 'vue'
import { defineStore } from 'pinia'
import { recipeApi } from '@/api/recipeApi'
import type { Recipe, CreateRecipeDto, UpdateRecipeDto } from '@/types/recipe'

export type RecipeSortOption = 'NEWEST' | 'TITLE' | 'TIME'

export function getRecipeTotalTime(recipe: Recipe): number {
  return (recipe.prepTimeMinutes || 0) + (recipe.cookTimeMinutes || 0)
}

export const useRecipeStore = defineStore('recipes', () => {
  const recipes = ref<Recipe[]>([])
  const selectedRecipe = ref<Recipe | null>(null)
  const loading = ref<boolean>(false)
  const error = ref<string | null>(null)

  // Filters & Search
  const searchQuery = ref<string>('')
  const maxTimeFilter = ref<number | 'ALL'>('ALL')
  const sortBy = ref<RecipeSortOption>('NEWEST')

  const totalRecipesCount = computed(() => recipes.value.length)

  const hasActiveFilters = computed(() => {
    return searchQuery.value.trim() !== '' || maxTimeFilter.value !== 'ALL' || sortBy.value !== 'NEWEST'
  })

  const filteredRecipes = computed(() => {
    let result = [...recipes.value]

    // 1. Search Query (Title, Description, or Ingredients)
    const query = searchQuery.value.toLowerCase().trim()
    if (query) {
      result = result.filter((r) => {
        const titleMatch = r.title.toLowerCase().includes(query)
        const descMatch = r.description ? r.description.toLowerCase().includes(query) : false
        const ingredientMatch = Array.isArray(r.ingredients)
          ? r.ingredients.some((ing) => ing.name.toLowerCase().includes(query))
          : false
        return titleMatch || descMatch || ingredientMatch
      })
    }

    // 2. Max Time Filter
    if (maxTimeFilter.value !== 'ALL') {
      const maxLimit = maxTimeFilter.value
      result = result.filter((r) => {
        const total = getRecipeTotalTime(r)
        return total > 0 && total <= maxLimit
      })
    }

    // 3. Sorting
    result.sort((a, b) => {
      if (sortBy.value === 'TITLE') {
        return a.title.localeCompare(b.title, 'sv')
      }
      if (sortBy.value === 'TIME') {
        const timeA = getRecipeTotalTime(a)
        const timeB = getRecipeTotalTime(b)
        // If one has 0 / unspecified time, place it at the end
        if (timeA === 0 && timeB > 0) return 1
        if (timeB === 0 && timeA > 0) return -1
        return timeA - timeB
      }
      // Default: NEWEST (by id desc)
      return b.id - a.id
    })

    return result
  })

  function resetFilters(): void {
    searchQuery.value = ''
    maxTimeFilter.value = 'ALL'
    sortBy.value = 'NEWEST'
  }

  async function fetchRecipes(): Promise<void> {
    loading.value = true
    error.value = null
    try {
      recipes.value = await recipeApi.getAll()
    } catch (err) {
      error.value = err instanceof Error ? err.message : 'Failed to fetch recipes'
    } finally {
      loading.value = false
    }
  }

  async function fetchRecipeById(id: number): Promise<Recipe | null> {
    loading.value = true
    error.value = null
    try {
      const recipe = await recipeApi.getById(id)
      selectedRecipe.value = recipe
      return recipe
    } catch (err) {
      error.value = err instanceof Error ? err.message : `Failed to fetch recipe #${id}`
      return null
    } finally {
      loading.value = false
    }
  }

  async function addRecipe(dto: CreateRecipeDto): Promise<Recipe | null> {
    loading.value = true
    error.value = null
    try {
      const created = await recipeApi.create(dto)
      recipes.value.unshift(created)
      return created
    } catch (err) {
      error.value = err instanceof Error ? err.message : 'Failed to create recipe'
      return null
    } finally {
      loading.value = false
    }
  }

  async function editRecipe(id: number, dto: UpdateRecipeDto): Promise<Recipe | null> {
    loading.value = true
    error.value = null
    try {
      const updated = await recipeApi.update(id, dto)
      const index = recipes.value.findIndex((r) => r.id === id)
      if (index !== -1) {
        recipes.value[index] = updated
      }
      if (selectedRecipe.value?.id === id) {
        selectedRecipe.value = updated
      }
      return updated
    } catch (err) {
      error.value = err instanceof Error ? err.message : `Failed to update recipe #${id}`
      return null
    } finally {
      loading.value = false
    }
  }

  async function removeRecipe(id: number): Promise<boolean> {
    loading.value = true
    error.value = null
    try {
      await recipeApi.delete(id)
      recipes.value = recipes.value.filter((r) => r.id !== id)
      if (selectedRecipe.value?.id === id) {
        selectedRecipe.value = null
      }
      return true
    } catch (err) {
      error.value = err instanceof Error ? err.message : `Failed to delete recipe #${id}`
      return false
    } finally {
      loading.value = false
    }
  }

  function clearError() {
    error.value = null
  }

  return {
    recipes,
    selectedRecipe,
    loading,
    error,
    searchQuery,
    maxTimeFilter,
    sortBy,
    totalRecipesCount,
    hasActiveFilters,
    filteredRecipes,
    resetFilters,
    fetchRecipes,
    fetchRecipeById,
    addRecipe,
    editRecipe,
    removeRecipe,
    clearError,
  }
})
