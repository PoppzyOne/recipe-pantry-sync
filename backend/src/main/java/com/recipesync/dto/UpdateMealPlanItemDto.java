package com.recipesync.dto;

import com.recipesync.entity.MealType;
import java.time.LocalDate;

public record UpdateMealPlanItemDto(
    LocalDate planDate,
    MealType mealType,
    Long recipeId,
    String customTitle,
    Integer servings,
    String notes
) {
}
