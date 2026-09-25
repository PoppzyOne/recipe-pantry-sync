import { ref } from 'vue'
import { defineStore } from 'pinia'
import { recipeApi } from '@/api/recipeApi'
import type { Recipe, CreateRecipeDto, UpdateRecipeDto } from '@/types/recipe'

export const useRecipeStore = defineStore('recipes', () => {
  const recipes = ref<Recipe[]>([])
  const selectedRecipe = ref<Recipe | null>(null)
  const loading = ref<boolean>(false)
  const error = ref<string | null>(null)

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
    fetchRecipes,
    fetchRecipeById,
    addRecipe,
    editRecipe,
    removeRecipe,
    clearError,
  }
})
