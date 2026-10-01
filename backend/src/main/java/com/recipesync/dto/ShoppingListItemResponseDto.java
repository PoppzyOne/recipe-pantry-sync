package com.recipesync.dto;

import com.recipesync.entity.IngredientCategory;
import java.time.Instant;

public record ShoppingListItemResponseDto(
    Long id,
    Long ingredientId,
    String name,
    IngredientCategory category,
    Double amount,
    String unit,
    boolean checked,
    String recipeTitle,
    Instant createdAt
) {}
