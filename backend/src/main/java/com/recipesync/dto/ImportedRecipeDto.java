package com.recipesync.dto;

import java.util.List;

public record ImportedRecipeDto(
    String title,
    String description,
    String instructions,
    Integer servings,
    Integer prepTimeMinutes,
    Integer cookTimeMinutes,
    String sourceUrl,
    String imageUrl,
    List<CreateRecipeIngredientDto> ingredients
) {
}
