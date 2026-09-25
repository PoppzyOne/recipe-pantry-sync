export type IngredientCategory =
  | 'PRODUCE'
  | 'DAIRY'
  | 'MEAT'
  | 'PANTRY'
  | 'SPICES'
  | 'BAKERY'
  | 'FROZEN'
  | 'OTHER'

export type MeasurementUnit =
  | 'g'
  | 'kg'
  | 'ml'
  | 'cl'
  | 'dl'
  | 'l'
  | 'krm'
  | 'tsk'
  | 'msk'
  | 'st'
  | 'pkt'
  | 'burk'
  | 'klyfta'
  | 'port'
  | 'övrigt'

export const MEASUREMENT_UNITS: { value: string; label: string }[] = [
  { value: 'g', label: 'g (gram)' },
  { value: 'kg', label: 'kg (kilo)' },
  { value: 'ml', label: 'ml (milliliter)' },
  { value: 'cl', label: 'cl (centiliter)' },
  { value: 'dl', label: 'dl (deciliter)' },
  { value: 'l', label: 'l (liter)' },
  { value: 'krm', label: 'krm (kryddmått)' },
  { value: 'tsk', label: 'tsk (tesked)' },
  { value: 'msk', label: 'msk (matsked)' },
  { value: 'st', label: 'st (styck)' },
  { value: 'pkt', label: 'pkt (paket)' },
  { value: 'burk', label: 'burk' },
  { value: 'klyfta', label: 'klyfta' },
  { value: 'port', label: 'port (portioner)' },
]

export interface RecipeIngredient {
  id: number
  ingredientId: number
  name: string
  category: IngredientCategory
  amount: number | null
  unit: string | null
  notes: string | null
}

export interface CreateRecipeIngredientDto {
  name: string
  category?: IngredientCategory | null
  amount?: number | null
  unit?: string | null
  notes?: string | null
}

export interface Recipe {
  id: number
  title: string
  description: string | null
  instructions: string | null
  servings: number | null
  prepTimeMinutes: number | null
  cookTimeMinutes: number | null
  ingredients: RecipeIngredient[]
  createdAt: string
  updatedAt: string | null
}

export interface CreateRecipeDto {
  title: string
  description?: string | null
  instructions?: string | null
  servings?: number | null
  prepTimeMinutes?: number | null
  cookTimeMinutes?: number | null
  ingredients?: CreateRecipeIngredientDto[] | null
}

export interface UpdateRecipeDto {
  title: string
  description?: string | null
  instructions?: string | null
  servings?: number | null
  prepTimeMinutes?: number | null
  cookTimeMinutes?: number | null
  ingredients?: CreateRecipeIngredientDto[] | null
}
