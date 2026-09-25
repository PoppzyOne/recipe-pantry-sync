package com.recipesync.service;

import com.recipesync.dto.CreateRecipeDto;
import com.recipesync.dto.CreateRecipeIngredientDto;
import com.recipesync.dto.RecipeIngredientDto;
import com.recipesync.dto.RecipeResponseDto;
import com.recipesync.dto.UpdateRecipeDto;
import com.recipesync.entity.Ingredient;
import com.recipesync.entity.Recipe;
import com.recipesync.entity.RecipeIngredient;
import com.recipesync.repository.RecipeRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class RecipeService {

    private final RecipeRepository recipeRepository;
    private final IngredientService ingredientService;

    @Inject
    public RecipeService(RecipeRepository recipeRepository, IngredientService ingredientService) {
        this.recipeRepository = recipeRepository;
        this.ingredientService = ingredientService;
    }

    public List<RecipeResponseDto> getAllRecipes() {
        return recipeRepository.listAll().stream()
                .map(this::toDto)
                .toList();
    }

    public RecipeResponseDto getRecipeById(Long id) {
        return recipeRepository.findByIdOptional(id)
                .map(this::toDto)
                .orElseThrow(() -> new NotFoundException("Recipe not found with id: " + id));
    }

    @Transactional
    public RecipeResponseDto createRecipe(CreateRecipeDto dto) {
        Recipe recipe = toEntity(dto);
        recipeRepository.persist(recipe);
        return toDto(recipe);
    }

    @Transactional
    public RecipeResponseDto updateRecipe(Long id, UpdateRecipeDto dto) {
        Recipe recipe = recipeRepository.findByIdOptional(id)
                .orElseThrow(() -> new NotFoundException("Recipe not found with id: " + id));

        recipe.title = dto.title() != null ? dto.title().trim() : null;
        recipe.description = normalizeText(dto.description());
        recipe.instructions = normalizeText(dto.instructions());
        recipe.servings = dto.servings();
        recipe.prepTimeMinutes = dto.prepTimeMinutes();
        recipe.cookTimeMinutes = dto.cookTimeMinutes();

        if (dto.ingredients() != null) {
            recipe.clearIngredients();
            for (CreateRecipeIngredientDto ingDto : dto.ingredients()) {
                Ingredient ingredient = ingredientService.getOrCreate(ingDto.name(), ingDto.category());
                RecipeIngredient recipeIngredient = new RecipeIngredient();
                recipeIngredient.ingredient = ingredient;
                recipeIngredient.amount = ingDto.amount();
                recipeIngredient.unit = ingDto.unit();
                recipeIngredient.notes = ingDto.notes();
                recipe.addIngredient(recipeIngredient);
            }
        }

        return toDto(recipe);
    }

    @Transactional
    public void deleteRecipe(Long id) {
        boolean deleted = recipeRepository.deleteById(id);
        if (!deleted) {
            throw new NotFoundException("Recipe not found with id: " + id);
        }
    }

    public RecipeResponseDto toDto(Recipe recipe) {
        List<RecipeIngredientDto> ingredientDtos = new ArrayList<>();
        if (recipe.ingredients != null) {
            for (RecipeIngredient ri : recipe.ingredients) {
                ingredientDtos.add(new RecipeIngredientDto(
                        ri.id,
                        ri.ingredient.id,
                        ri.ingredient.name,
                        ri.ingredient.category,
                        ri.amount,
                        ri.unit,
                        ri.notes
                ));
            }
        }

        return new RecipeResponseDto(
                recipe.id,
                recipe.title,
                recipe.description,
                recipe.instructions,
                recipe.servings,
                recipe.prepTimeMinutes,
                recipe.cookTimeMinutes,
                ingredientDtos,
                recipe.createdAt,
                recipe.updatedAt
        );
    }

    public Recipe toEntity(CreateRecipeDto dto) {
        Recipe recipe = new Recipe();
        recipe.title = dto.title() != null ? dto.title().trim() : null;
        recipe.description = normalizeText(dto.description());
        recipe.instructions = normalizeText(dto.instructions());
        recipe.servings = dto.servings();
        recipe.prepTimeMinutes = dto.prepTimeMinutes();
        recipe.cookTimeMinutes = dto.cookTimeMinutes();

        if (dto.ingredients() != null) {
            for (CreateRecipeIngredientDto ingDto : dto.ingredients()) {
                Ingredient ingredient = ingredientService.getOrCreate(ingDto.name(), ingDto.category());
                RecipeIngredient recipeIngredient = new RecipeIngredient();
                recipeIngredient.ingredient = ingredient;
                recipeIngredient.amount = ingDto.amount();
                recipeIngredient.unit = ingDto.unit();
                recipeIngredient.notes = ingDto.notes();
                recipe.addIngredient(recipeIngredient);
            }
        }

        return recipe;
    }

    private String normalizeText(String input) {
        if (input == null) {
            return null;
        }
        return input.replace("\r\n", "\n").replace("\\n", "\n").trim();
    }
}
