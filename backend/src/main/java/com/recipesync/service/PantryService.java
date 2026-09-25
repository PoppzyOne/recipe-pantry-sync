package com.recipesync.service;

import com.recipesync.dto.CreatePantryItemDto;
import com.recipesync.dto.PantryItemResponseDto;
import com.recipesync.dto.RecipeIngredientDto;
import com.recipesync.dto.UpdatePantryItemDto;
import com.recipesync.entity.Ingredient;
import com.recipesync.entity.PantryItem;
import com.recipesync.entity.QuantityLevel;
import com.recipesync.entity.Recipe;
import com.recipesync.entity.RecipeIngredient;
import com.recipesync.repository.PantryItemRepository;
import com.recipesync.repository.RecipeRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@ApplicationScoped
public class PantryService {

    private final PantryItemRepository pantryItemRepository;
    private final IngredientService ingredientService;
    private final RecipeRepository recipeRepository;

    @Inject
    public PantryService(
            PantryItemRepository pantryItemRepository,
            IngredientService ingredientService,
            RecipeRepository recipeRepository
    ) {
        this.pantryItemRepository = pantryItemRepository;
        this.ingredientService = ingredientService;
        this.recipeRepository = recipeRepository;
    }

    public List<PantryItemResponseDto> listAll() {
        return pantryItemRepository.listAll().stream()
                .map(this::toDto)
                .toList();
    }

    public List<PantryItemResponseDto> listInStock() {
        return pantryItemRepository.listInStock().stream()
                .map(this::toDto)
                .toList();
    }

    public PantryItemResponseDto getById(Long id) {
        return pantryItemRepository.findByIdOptional(id)
                .map(this::toDto)
                .orElseThrow(() -> new NotFoundException("Pantry item not found with id: " + id));
    }

    @Transactional
    public PantryItemResponseDto addOrUpdateItem(CreatePantryItemDto dto) {
        Ingredient ingredient = ingredientService.getOrCreate(dto.ingredientName(), dto.category());

        PantryItem item = pantryItemRepository.findByIngredientId(ingredient.id)
                .orElseGet(() -> {
                    PantryItem newItem = new PantryItem();
                    newItem.ingredient = ingredient;
                    return newItem;
                });

        if (dto.quantity() != null) {
            item.quantity = dto.quantity();
        }
        if (dto.unit() != null) {
            item.unit = dto.unit();
        }
        if (dto.quantityLevel() != null) {
            item.quantityLevel = dto.quantityLevel();
            if (dto.quantityLevel() == QuantityLevel.EMPTY) {
                item.inStock = false;
            } else if (dto.inStock() == null) {
                item.inStock = true;
            }
        }
        if (dto.inStock() != null) {
            item.inStock = dto.inStock();
        } else if (dto.quantityLevel() == null && item.id == null) {
            item.inStock = true;
        }

        pantryItemRepository.persist(item);
        return toDto(item);
    }

    @Transactional
    public PantryItemResponseDto updateItem(Long id, UpdatePantryItemDto dto) {
        PantryItem item = pantryItemRepository.findByIdOptional(id)
                .orElseThrow(() -> new NotFoundException("Pantry item not found with id: " + id));

        if (dto.quantity() != null) {
            item.quantity = dto.quantity();
        }
        if (dto.unit() != null) {
            item.unit = dto.unit();
        }
        if (dto.quantityLevel() != null) {
            item.quantityLevel = dto.quantityLevel();
            if (dto.quantityLevel() == QuantityLevel.EMPTY) {
                item.inStock = false;
            } else if (dto.inStock() == null) {
                item.inStock = true;
            }
        }
        if (dto.inStock() != null) {
            item.inStock = dto.inStock();
        }

        return toDto(item);
    }

    @Transactional
    public PantryItemResponseDto toggleInStock(Long id) {
        PantryItem item = pantryItemRepository.findByIdOptional(id)
                .orElseThrow(() -> new NotFoundException("Pantry item not found with id: " + id));

        item.inStock = !item.inStock;
        if (!item.inStock && item.quantityLevel != null && item.quantityLevel != QuantityLevel.EMPTY) {
            item.quantityLevel = QuantityLevel.EMPTY;
        } else if (item.inStock && item.quantityLevel == QuantityLevel.EMPTY) {
            item.quantityLevel = QuantityLevel.FULL;
        }
        return toDto(item);
    }

    @Transactional
    public void deleteItem(Long id) {
        boolean deleted = pantryItemRepository.deleteById(id);
        if (!deleted) {
            throw new NotFoundException("Pantry item not found with id: " + id);
        }
    }

    public List<RecipeIngredientDto> getMissingIngredientsForRecipe(Long recipeId) {
        Recipe recipe = recipeRepository.findByIdOptional(recipeId)
                .orElseThrow(() -> new NotFoundException("Recipe not found with id: " + recipeId));

        Set<Long> inStockIngredientIds = pantryItemRepository.listInStock().stream()
                .map(item -> item.ingredient.id)
                .collect(Collectors.toSet());

        return recipe.ingredients.stream()
                .filter(ri -> !inStockIngredientIds.contains(ri.ingredient.id))
                .map(ri -> new RecipeIngredientDto(
                        ri.id,
                        ri.ingredient.id,
                        ri.ingredient.name,
                        ri.ingredient.category,
                        ri.amount,
                        ri.unit,
                        ri.notes
                ))
                .toList();
    }

    private PantryItemResponseDto toDto(PantryItem item) {
        return new PantryItemResponseDto(
                item.id,
                item.ingredient.id,
                item.ingredient.name,
                item.ingredient.category,
                item.quantity,
                item.unit,
                item.quantityLevel,
                item.inStock,
                item.updatedAt
        );
    }
}
