export type IngredientCategory =
  | 'PRODUCE'
  | 'DAIRY'
  | 'MEAT'
  | 'PANTRY'
  | 'SPICES'
  | 'BAKERY'
  | 'FROZEN'
  | 'OTHER'

export const CATEGORY_LABELS: Record<IngredientCategory, { label: string; color: string }> = {
  PANTRY: { label: 'Skafferi & Torrvaror', color: 'bg-amber-50 text-amber-700 border-amber-200' },
  PRODUCE: { label: 'Frukt & Grönt', color: 'bg-green-50 text-green-700 border-green-200' },
  DAIRY: { label: 'Mejeri & Ost', color: 'bg-blue-50 text-blue-700 border-blue-200' },
  MEAT: { label: 'Kött, Fågel & Fisk', color: 'bg-red-50 text-red-700 border-red-200' },
  SPICES: { label: 'Kryddor & Smaksättare', color: 'bg-orange-50 text-orange-700 border-orange-200' },
  BAKERY: { label: 'Bröd & Bakning', color: 'bg-yellow-50 text-yellow-700 border-yellow-200' },
  FROZEN: { label: 'Frysvaror', color: 'bg-cyan-50 text-cyan-700 border-cyan-200' },
  OTHER: { label: 'Övrigt', color: 'bg-gray-50 text-gray-700 border-gray-200' },
}

export const INGREDIENT_CATEGORIES: { value: IngredientCategory; label: string }[] = [
  { value: 'PANTRY', label: 'Skafferi & Torrvaror' },
  { value: 'PRODUCE', label: 'Frukt & Grönt' },
  { value: 'DAIRY', label: 'Mejeri & Ost' },
  { value: 'MEAT', label: 'Kött, Fågel & Fisk' },
  { value: 'SPICES', label: 'Kryddor & Smaksättare' },
  { value: 'BAKERY', label: 'Bröd & Bakning' },
  { value: 'FROZEN', label: 'Frysvaror' },
  { value: 'OTHER', label: 'Övrigt' },
]

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
