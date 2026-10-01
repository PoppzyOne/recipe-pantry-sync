package com.recipesync.dto;

import com.recipesync.entity.IngredientCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CreateShoppingListItemDto(
    Long ingredientId,

    @NotBlank(message = "Name is required")
    @Size(max = 255, message = "Name must not exceed 255 characters")
    String name,

    IngredientCategory category,

    @Positive(message = "Amount must be positive")
    Double amount,

    @Size(max = 50, message = "Unit must not exceed 50 characters")
    String unit,

    @Size(max = 255, message = "Recipe title must not exceed 255 characters")
    String recipeTitle
) {}
