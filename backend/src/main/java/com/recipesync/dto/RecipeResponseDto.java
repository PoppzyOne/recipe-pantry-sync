package com.recipesync.dto;

import java.time.Instant;
import java.util.List;

public record RecipeResponseDto(
    Long id,
    String title,
    String description,
    String instructions,
    Integer servings,
    Integer prepTimeMinutes,
    Integer cookTimeMinutes,
    List<RecipeIngredientDto> ingredients,
    Instant createdAt,
    Instant updatedAt
) {
}
