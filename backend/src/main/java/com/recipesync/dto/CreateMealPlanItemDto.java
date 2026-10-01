package com.recipesync.dto;

import com.recipesync.entity.MealType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record CreateMealPlanItemDto(
    @NotNull(message = "Plan date is required")
    LocalDate planDate,

    @NotNull(message = "Meal type is required")
    MealType mealType,

    Long recipeId,

    @Size(max = 200, message = "Custom title cannot exceed 200 characters")
    String customTitle,

    @Positive(message = "Servings must be positive")
    Integer servings,

    @Size(max = 1000, message = "Notes cannot exceed 1000 characters")
    String notes
) {
    public CreateMealPlanItemDto(
            LocalDate planDate,
            MealType mealType,
            Long recipeId,
            Integer servings,
            String notes
    ) {
        this(planDate, mealType, recipeId, null, servings, notes);
    }
}

