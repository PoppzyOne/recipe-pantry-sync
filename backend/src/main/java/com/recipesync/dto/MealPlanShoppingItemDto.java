package com.recipesync.dto;

import com.recipesync.entity.IngredientCategory;
import java.util.List;

public record MealPlanShoppingItemDto(
    Long ingredientId,
    String name,
    IngredientCategory category,
    Double neededAmount,
    Double pantryAmount,
    Double missingAmount,
    String unit,
    List<String> recipeTitles
) {
}
