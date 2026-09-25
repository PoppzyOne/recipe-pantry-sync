package com.recipesync;

import com.recipesync.dto.CreateRecipeDto;
import com.recipesync.dto.RecipeResponseDto;
import com.recipesync.dto.UpdateRecipeDto;
import com.recipesync.service.RecipeService;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.ws.rs.NotFoundException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class RecipeServiceTest {

    @Inject
    RecipeService recipeService;

    @Test
    void testGetAllRecipesReturnsList() {
        List<RecipeResponseDto> recipes = recipeService.getAllRecipes();
        assertNotNull(recipes);
        assertFalse(recipes.isEmpty(), "Recipes list should not be empty");
    }

    @Test
    void testGetRecipeByIdThrowsWhenNotFound() {
        assertThrows(NotFoundException.class, () -> recipeService.getRecipeById(888888L));
    }

    @Test
    void testCreateRecipePersistsAndReturnsDto() {
        CreateRecipeDto dto = new CreateRecipeDto(
                "Service Test Recipe",
                "Testing service layer directly",
                "Step 1: Test",
                3,
                10,
                20
        );

        RecipeResponseDto created = recipeService.createRecipe(dto);

        assertNotNull(created.id());
        assertEquals("Service Test Recipe", created.title());
        assertEquals(3, created.servings());
        assertNotNull(created.createdAt());
        assertNotNull(created.updatedAt());

        // Verify retrieval
        RecipeResponseDto fetched = recipeService.getRecipeById(created.id());
        assertEquals(created.id(), fetched.id());
        assertEquals("Service Test Recipe", fetched.title());
    }

    @Test
    void testUpdateRecipeModifiesFields() {
        CreateRecipeDto initialDto = new CreateRecipeDto(
                "Initial Title",
                "Initial Desc",
                "Initial Inst",
                2,
                5,
                10
        );
        RecipeResponseDto created = recipeService.createRecipe(initialDto);

        UpdateRecipeDto updateDto = new UpdateRecipeDto(
                "Updated Title",
                "Updated Desc",
                "Updated Inst",
                6,
                15,
                30
        );

        RecipeResponseDto updated = recipeService.updateRecipe(created.id(), updateDto);

        assertEquals("Updated Title", updated.title());
        assertEquals(6, updated.servings());
        assertEquals(15, updated.prepTimeMinutes());
        assertEquals(30, updated.cookTimeMinutes());
    }

    @Test
    void testUpdateRecipeThrowsWhenNotFound() {
        UpdateRecipeDto updateDto = new UpdateRecipeDto(
                "Title",
                "Desc",
                "Inst",
                4,
                10,
                10
        );

        assertThrows(NotFoundException.class, () -> recipeService.updateRecipe(777777L, updateDto));
    }

    @Test
    void testDeleteRecipeRemovesEntity() {
        CreateRecipeDto dto = new CreateRecipeDto(
                "To Be Deleted",
                "Desc",
                "Inst",
                1,
                5,
                5
        );
        RecipeResponseDto created = recipeService.createRecipe(dto);
        Long id = created.id();

        assertDoesNotThrow(() -> recipeService.deleteRecipe(id));
        assertThrows(NotFoundException.class, () -> recipeService.getRecipeById(id));
    }

    @Test
    void testDeleteRecipeThrowsWhenNotFound() {
        assertThrows(NotFoundException.class, () -> recipeService.deleteRecipe(666666L));
    }
}
