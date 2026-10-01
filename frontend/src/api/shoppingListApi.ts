import type {
  ShoppingListItem,
  AddShoppingListItemDto,
  UpdateShoppingListItemDto,
} from '@/types/shoppingList'

const BASE_URL = '/api/shopping-list'

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

export const shoppingListApi = {
  async getAll(): Promise<ShoppingListItem[]> {
    const response = await fetch(BASE_URL, {
      headers: {
        Accept: 'application/json',
      },
    })
    return handleResponse<ShoppingListItem[]>(response)
  },

  async getById(id: number | string): Promise<ShoppingListItem> {
    const response = await fetch(`${BASE_URL}/${id}`, {
      headers: {
        Accept: 'application/json',
      },
    })
    return handleResponse<ShoppingListItem>(response)
  },

  async create(dto: AddShoppingListItemDto): Promise<ShoppingListItem> {
    const response = await fetch(BASE_URL, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        Accept: 'application/json',
      },
      body: JSON.stringify(dto),
    })
    return handleResponse<ShoppingListItem>(response)
  },

  async createBatch(dtos: AddShoppingListItemDto[]): Promise<ShoppingListItem[]> {
    const response = await fetch(`${BASE_URL}/batch`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        Accept: 'application/json',
      },
      body: JSON.stringify(dtos),
    })
    return handleResponse<ShoppingListItem[]>(response)
  },

  async update(id: number | string, dto: UpdateShoppingListItemDto): Promise<ShoppingListItem> {
    const response = await fetch(`${BASE_URL}/${id}`, {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json',
        Accept: 'application/json',
      },
      body: JSON.stringify(dto),
    })
    return handleResponse<ShoppingListItem>(response)
  },

  async toggleChecked(id: number | string): Promise<ShoppingListItem> {
    const response = await fetch(`${BASE_URL}/${id}/toggle`, {
      method: 'PATCH',
      headers: {
        Accept: 'application/json',
      },
    })
    return handleResponse<ShoppingListItem>(response)
  },

  async delete(id: number | string): Promise<void> {
    const response = await fetch(`${BASE_URL}/${id}`, {
      method: 'DELETE',
    })
    return handleResponse<void>(response)
  },

  async clearCompleted(): Promise<{ deletedCount: number }> {
    const response = await fetch(`${BASE_URL}/completed`, {
      method: 'DELETE',
      headers: {
        Accept: 'application/json',
      },
    })
    return handleResponse<{ deletedCount: number }>(response)
  },

  async syncToPantry(): Promise<{ syncedCount: number }> {
    const response = await fetch(`${BASE_URL}/sync-to-pantry`, {
      method: 'POST',
      headers: {
        Accept: 'application/json',
      },
    })
    return handleResponse<{ syncedCount: number }>(response)
  },
}
