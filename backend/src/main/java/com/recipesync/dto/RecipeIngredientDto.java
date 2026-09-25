package com.recipesync.dto;

import com.recipesync.entity.IngredientCategory;

public record RecipeIngredientDto(
    Long id,
    Long ingredientId,
    String name,
    IngredientCategory category,
    Double amount,
    String unit,
    String notes
) {
}
