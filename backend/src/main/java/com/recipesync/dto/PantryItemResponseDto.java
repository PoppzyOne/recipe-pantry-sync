package com.recipesync.dto;

import com.recipesync.entity.IngredientCategory;
import com.recipesync.entity.QuantityLevel;
import java.time.Instant;

public record PantryItemResponseDto(
    Long id,
    Long ingredientId,
    String ingredientName,
    IngredientCategory category,
    Double quantity,
    String unit,
    QuantityLevel quantityLevel,
    boolean inStock,
    Instant updatedAt
) {
}
