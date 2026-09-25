import type { IngredientCategory } from './recipe'

export interface ShoppingListItem {
  id: string
  ingredientId?: number | null
  name: string
  category: IngredientCategory
  amount?: number | null
  unit?: string | null
  checked: boolean
  recipeTitle?: string | null
  createdAt: string
}

export interface AddShoppingListItemDto {
  ingredientId?: number | null
  name: string
  category?: IngredientCategory | null
  amount?: number | null
  unit?: string | null
  recipeTitle?: string | null
}
