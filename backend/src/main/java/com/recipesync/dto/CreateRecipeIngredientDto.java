package com.recipesync.dto;

import com.recipesync.entity.IngredientCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record CreateRecipeIngredientDto(
    @NotBlank(message = "Ingredient name must not be blank")
    @Size(max = 100, message = "Ingredient name cannot exceed 100 characters")
    String name,

    IngredientCategory category,

    @PositiveOrZero(message = "Amount must be positive or zero")
    Double amount,

    @Size(max = 20, message = "Unit cannot exceed 20 characters")
    String unit,

    @Size(max = 100, message = "Notes cannot exceed 100 characters")
    String notes
) {
}
