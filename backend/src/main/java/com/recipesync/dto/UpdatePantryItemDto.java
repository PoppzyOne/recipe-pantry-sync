package com.recipesync.dto;

import com.recipesync.entity.QuantityLevel;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record UpdatePantryItemDto(
    @PositiveOrZero(message = "Quantity must be positive or zero")
    Double quantity,

    @Size(max = 20, message = "Unit cannot exceed 20 characters")
    String unit,

    QuantityLevel quantityLevel,

    Boolean inStock
) {
    public UpdatePantryItemDto(
            Double quantity,
            String unit,
            Boolean inStock
    ) {
        this(quantity, unit, null, inStock);
    }
}
