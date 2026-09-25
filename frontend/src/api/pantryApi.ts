import type { PantryItem, CreatePantryItemDto, UpdatePantryItemDto } from '@/types/pantry'

const BASE_URL = '/api/pantry'

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
      // Body not JSON
    }

    throw new ApiError(errorMessage, response.status, errorDetails)
  }

  if (response.status === 204) {
    return undefined as unknown as T
  }

  return response.json() as Promise<T>
}

export const pantryApi = {
  async getAll(inStock?: boolean): Promise<PantryItem[]> {
    const url = inStock !== undefined ? `${BASE_URL}?inStock=${inStock}` : BASE_URL
    const response = await fetch(url, {
      headers: { Accept: 'application/json' },
    })
    return handleResponse<PantryItem[]>(response)
  },

  async getById(id: number): Promise<PantryItem> {
    const response = await fetch(`${BASE_URL}/${id}`, {
      headers: { Accept: 'application/json' },
    })
    return handleResponse<PantryItem>(response)
  },

  async create(dto: CreatePantryItemDto): Promise<PantryItem> {
    const response = await fetch(BASE_URL, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        Accept: 'application/json',
      },
      body: JSON.stringify(dto),
    })
    return handleResponse<PantryItem>(response)
  },

  async update(id: number, dto: UpdatePantryItemDto): Promise<PantryItem> {
    const response = await fetch(`${BASE_URL}/${id}`, {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json',
        Accept: 'application/json',
      },
      body: JSON.stringify(dto),
    })
    return handleResponse<PantryItem>(response)
  },

  async toggleStock(id: number): Promise<PantryItem> {
    const response = await fetch(`${BASE_URL}/${id}/toggle`, {
      method: 'PATCH',
      headers: { Accept: 'application/json' },
    })
    return handleResponse<PantryItem>(response)
  },

  async delete(id: number): Promise<void> {
    const response = await fetch(`${BASE_URL}/${id}`, {
      method: 'DELETE',
    })
    return handleResponse<void>(response)
  },
}
