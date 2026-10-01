package com.recipesync.dto;

import com.recipesync.entity.MealType;
import java.time.Instant;
import java.time.LocalDate;

public record MealPlanItemResponseDto(
    Long id,
    LocalDate planDate,
    MealType mealType,
    Long recipeId,
    String recipeTitle,
    String customTitle,
    Integer servings,
    String notes,
    Instant createdAt,
    Instant updatedAt
) {
}
