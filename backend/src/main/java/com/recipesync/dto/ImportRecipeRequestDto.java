package com.recipesync.dto;

public record ImportRecipeRequestDto(
    String url,
    String text
) {
    public ImportRecipeRequestDto(String url) {
        this(url, null);
    }
}
