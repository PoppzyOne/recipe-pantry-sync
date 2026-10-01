import type { IngredientCategory } from './recipe'

export type MealType = 'BREAKFAST' | 'LUNCH' | 'DINNER' | 'SNACK'

export const MEAL_TYPES: { value: MealType; label: string; icon: string }[] = [
  { value: 'LUNCH', label: 'Lunch', icon: '☀️' },
  { value: 'DINNER', label: 'Middag', icon: '🌙' },
  { value: 'BREAKFAST', label: 'Frukost', icon: '🌅' },
  { value: 'SNACK', label: 'Mellanmål', icon: '🍎' },
]

export const MEAL_TYPE_LABELS: Record<MealType, { label: string; icon: string }> = {
  LUNCH: { label: 'Lunch', icon: '☀️' },
  DINNER: { label: 'Middag', icon: '🌙' },
  BREAKFAST: { label: 'Frukost', icon: '🌅' },
  SNACK: { label: 'Mellanmål', icon: '🍎' },
}

export interface MealPlanItem {
  id: number
  planDate: string
  mealType: MealType
  recipeId: number | null
  recipeTitle: string | null
  customTitle: string | null
  servings: number
  notes: string | null
  createdAt: string
  updatedAt: string | null
}

export interface CreateMealPlanItemDto {
  planDate: string
  mealType: MealType
  recipeId?: number | null
  customTitle?: string | null
  servings?: number
  notes?: string | null
}

export interface UpdateMealPlanItemDto {
  planDate?: string
  mealType?: MealType
  recipeId?: number | null
  customTitle?: string | null
  servings?: number
  notes?: string | null
}

export interface MealPlanShoppingItem {
  ingredientId: number
  name: string
  category: IngredientCategory
  neededAmount: number
  pantryAmount: number
  missingAmount: number
  unit: string | null
  recipeTitles: string[]
}
