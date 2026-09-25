import type { IngredientCategory } from './recipe'

export type QuantityLevel = 'FULL' | 'HALF' | 'LOW' | 'EMPTY'

export const QUANTITY_LEVELS: {
  value: QuantityLevel
  label: string
  shortLabel: string
  color: string
  badgeColor: string
}[] = [
  {
    value: 'FULL',
    label: 'Fullt',
    shortLabel: 'Fullt',
    color: 'bg-emerald-600 text-white hover:bg-emerald-700',
    badgeColor: 'bg-emerald-50 text-emerald-700 border-emerald-200',
  },
  {
    value: 'HALF',
    label: 'Halvt',
    shortLabel: 'Halvt',
    color: 'bg-blue-600 text-white hover:bg-blue-700',
    badgeColor: 'bg-blue-50 text-blue-700 border-blue-200',
  },
  {
    value: 'LOW',
    label: 'Lite kvar',
    shortLabel: 'Lite kvar',
    color: 'bg-amber-600 text-white hover:bg-amber-700',
    badgeColor: 'bg-amber-50 text-amber-700 border-amber-200',
  },
  {
    value: 'EMPTY',
    label: 'Slut',
    shortLabel: 'Slut',
    color: 'bg-red-600 text-white hover:bg-red-700',
    badgeColor: 'bg-red-50 text-red-700 border-red-200',
  },
]

export interface PantryItem {
  id: number
  ingredientId: number
  ingredientName: string
  category: IngredientCategory
  quantity: number | null
  unit: string | null
  quantityLevel?: QuantityLevel | null
  inStock: boolean
  updatedAt: string
}

export interface CreatePantryItemDto {
  ingredientName: string
  category?: IngredientCategory | null
  quantity?: number | null
  unit?: string | null
  quantityLevel?: QuantityLevel | null
  inStock?: boolean | null
}

export interface UpdatePantryItemDto {
  quantity?: number | null
  unit?: string | null
  quantityLevel?: QuantityLevel | null
  inStock?: boolean | null
}
