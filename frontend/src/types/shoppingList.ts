import type { IngredientCategory } from './recipe'

export interface ShoppingListItem {
  id: number | string
  ingredientId?: number | null
  name: string
  category: IngredientCategory
  amount?: number | null
  unit?: string | null
  checked: boolean
  recipeTitle?: string | null
  createdAt: string
  pendingSync?: boolean
}

export interface AddShoppingListItemDto {
  ingredientId?: number | null
  name: string
  category?: IngredientCategory | null
  amount?: number | null
  unit?: string | null
  recipeTitle?: string | null
}

export interface UpdateShoppingListItemDto {
  ingredientId?: number | null
  name?: string | null
  category?: IngredientCategory | null
  amount?: number | null
  unit?: string | null
  checked?: boolean | null
  recipeTitle?: string | null
}
