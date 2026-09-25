package com.recipesync.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.util.List;

public record UpdateRecipeDto(
    @NotBlank(message = "Title must not be blank")
    @Size(max = 200, message = "Title cannot exceed 200 characters")
    String title,

    String description,

    String instructions,

    @Positive(message = "Servings must be a positive number")
    Integer servings,

    @PositiveOrZero(message = "Prep time must be positive or zero")
    Integer prepTimeMinutes,

    @PositiveOrZero(message = "Cook time must be positive or zero")
    Integer cookTimeMinutes,

    @Valid
    List<CreateRecipeIngredientDto> ingredients
) {
    public UpdateRecipeDto(
            String title,
            String description,
            String instructions,
            Integer servings,
            Integer prepTimeMinutes,
            Integer cookTimeMinutes
    ) {
        this(title, description, instructions, servings, prepTimeMinutes, cookTimeMinutes, null);
    }
}
