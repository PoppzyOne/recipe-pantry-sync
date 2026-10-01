package com.recipesync.dto;

import com.recipesync.entity.MealType;
import java.time.LocalDate;

public record CreateMealPlanItemDto(
    LocalDate planDate,
    MealType mealType,
    Long recipeId,
    String customTitle,
    Integer servings,
    String notes
) {
}
