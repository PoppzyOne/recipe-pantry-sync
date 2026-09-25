package com.recipesync.dto;

import com.recipesync.entity.IngredientCategory;
import com.recipesync.entity.QuantityLevel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record CreatePantryItemDto(
    @NotBlank(message = "Ingredient name must not be blank")
    @Size(max = 100, message = "Ingredient name cannot exceed 100 characters")
    String ingredientName,

    IngredientCategory category,

    @PositiveOrZero(message = "Quantity must be positive or zero")
    Double quantity,

    @Size(max = 20, message = "Unit cannot exceed 20 characters")
    String unit,

    QuantityLevel quantityLevel,

    Boolean inStock
) {
    public CreatePantryItemDto(
            String ingredientName,
            IngredientCategory category,
            Double quantity,
            String unit,
            Boolean inStock
    ) {
        this(ingredientName, category, quantity, unit, null, inStock);
    }
}
