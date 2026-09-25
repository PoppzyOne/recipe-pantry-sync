import type { Recipe, CreateRecipeDto, UpdateRecipeDto } from '@/types/recipe'

const BASE_URL = '/api/recipes'

class ApiError extends Error {
  constructor(
    message: string,
    public status?: number,
    public details?: unknown
  ) {
    super(message)
    this.name = 'ApiError'
  }
}

async function handleResponse<T>(response: Response): Promise<T> {
  if (!response.ok) {
    let errorMessage = `HTTP Error ${response.status}: ${response.statusText}`
    let errorDetails: unknown = null

    try {
      const errorJson = await response.json()
      errorDetails = errorJson
      if (errorJson.message) {
        errorMessage = errorJson.message
      } else if (errorJson.title) {
        errorMessage = errorJson.title
      } else if (Array.isArray(errorJson.violations) && errorJson.violations.length > 0) {
        errorMessage = errorJson.violations
          .map((v: { field?: string; message?: string }) => v.message || 'Validation error')
          .join(', ')
      }
    } catch {
      // Body is not JSON
    }

    throw new ApiError(errorMessage, response.status, errorDetails)
  }

  if (response.status === 204) {
    return undefined as unknown as T
  }

  return response.json() as Promise<T>
}

export const recipeApi = {
  async getAll(): Promise<Recipe[]> {
    const response = await fetch(BASE_URL, {
      headers: {
        Accept: 'application/json',
      },
    })
    return handleResponse<Recipe[]>(response)
  },

  async getById(id: number): Promise<Recipe> {
    const response = await fetch(`${BASE_URL}/${id}`, {
      headers: {
        Accept: 'application/json',
      },
    })
    return handleResponse<Recipe>(response)
  },

  async create(dto: CreateRecipeDto): Promise<Recipe> {
    const response = await fetch(BASE_URL, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        Accept: 'application/json',
      },
      body: JSON.stringify(dto),
    })
    return handleResponse<Recipe>(response)
  },

  async update(id: number, dto: UpdateRecipeDto): Promise<Recipe> {
    const response = await fetch(`${BASE_URL}/${id}`, {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json',
        Accept: 'application/json',
      },
      body: JSON.stringify(dto),
    })
    return handleResponse<Recipe>(response)
  },

  async delete(id: number): Promise<void> {
    const response = await fetch(`${BASE_URL}/${id}`, {
      method: 'DELETE',
    })
    return handleResponse<void>(response)
  },

  async getMissingIngredients(id: number): Promise<import('@/types/recipe').RecipeIngredient[]> {
    const response = await fetch(`${BASE_URL}/${id}/missing-ingredients`, {
      headers: { Accept: 'application/json' },
    })
    return handleResponse<import('@/types/recipe').RecipeIngredient[]>(response)
  },
}
