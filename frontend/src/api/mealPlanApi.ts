import type {
  MealPlanItem,
  CreateMealPlanItemDto,
  UpdateMealPlanItemDto,
  MealPlanShoppingItem,
} from '@/types/mealPlan'

const BASE_URL = '/api/meal-plans'

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

export const mealPlanApi = {
  async getMealPlans(startDate?: string, endDate?: string): Promise<MealPlanItem[]> {
    const params = new URLSearchParams()
    if (startDate) params.set('startDate', startDate)
    if (endDate) params.set('endDate', endDate)
    const queryString = params.toString() ? `?${params.toString()}` : ''

    const response = await fetch(`${BASE_URL}${queryString}`, {
      headers: {
        Accept: 'application/json',
      },
    })
    return handleResponse<MealPlanItem[]>(response)
  },

  async getById(id: number): Promise<MealPlanItem> {
    const response = await fetch(`${BASE_URL}/${id}`, {
      headers: {
        Accept: 'application/json',
      },
    })
    return handleResponse<MealPlanItem>(response)
  },

  async create(dto: CreateMealPlanItemDto): Promise<MealPlanItem> {
    const response = await fetch(BASE_URL, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        Accept: 'application/json',
      },
      body: JSON.stringify(dto),
    })
    return handleResponse<MealPlanItem>(response)
  },

  async update(id: number, dto: UpdateMealPlanItemDto): Promise<MealPlanItem> {
    const response = await fetch(`${BASE_URL}/${id}`, {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json',
        Accept: 'application/json',
      },
      body: JSON.stringify(dto),
    })
    return handleResponse<MealPlanItem>(response)
  },

  async delete(id: number): Promise<void> {
    const response = await fetch(`${BASE_URL}/${id}`, {
      method: 'DELETE',
    })
    return handleResponse<void>(response)
  },

  async calculateShoppingList(startDate: string, endDate: string): Promise<MealPlanShoppingItem[]> {
    const params = new URLSearchParams({ startDate, endDate })
    const response = await fetch(`${BASE_URL}/shopping-list?${params.toString()}`, {
      headers: {
        Accept: 'application/json',
      },
    })
    return handleResponse<MealPlanShoppingItem[]>(response)
  },
}
