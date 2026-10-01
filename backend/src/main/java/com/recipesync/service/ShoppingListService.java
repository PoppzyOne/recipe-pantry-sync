package com.recipesync.service;

import com.recipesync.dto.CreatePantryItemDto;
import com.recipesync.dto.CreateShoppingListItemDto;
import com.recipesync.dto.ShoppingListItemResponseDto;
import com.recipesync.dto.UpdateShoppingListItemDto;
import com.recipesync.entity.Ingredient;
import com.recipesync.entity.IngredientCategory;
import com.recipesync.entity.ShoppingListItem;
import com.recipesync.repository.IngredientRepository;
import com.recipesync.repository.ShoppingListItemRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class ShoppingListService {

    private final ShoppingListItemRepository shoppingListItemRepository;
    private final IngredientRepository ingredientRepository;
    private final IngredientService ingredientService;
    private final PantryService pantryService;

    @Inject
    public ShoppingListService(
            ShoppingListItemRepository shoppingListItemRepository,
            IngredientRepository ingredientRepository,
            IngredientService ingredientService,
            PantryService pantryService
    ) {
        this.shoppingListItemRepository = shoppingListItemRepository;
        this.ingredientRepository = ingredientRepository;
        this.ingredientService = ingredientService;
        this.pantryService = pantryService;
    }

    public List<ShoppingListItemResponseDto> listAll() {
        return shoppingListItemRepository.listAllOrdered().stream()
                .map(this::toDto)
                .toList();
    }

    public ShoppingListItemResponseDto getById(Long id) {
        return shoppingListItemRepository.findByIdOptional(id)
                .map(this::toDto)
                .orElseThrow(() -> new NotFoundException("Shopping list item not found with id: " + id));
    }

    @Transactional
    public ShoppingListItemResponseDto create(CreateShoppingListItemDto dto) {
        String cleanName = dto.name().trim();

        // Check if an unchecked item with the same name already exists
        Optional<ShoppingListItem> existingOpt = shoppingListItemRepository.findByNameAndUnchecked(cleanName);
        if (existingOpt.isPresent()) {
            ShoppingListItem existing = existingOpt.get();
            boolean unitMatches = (existing.unit == null && dto.unit() == null) ||
                    (existing.unit != null && existing.unit.equalsIgnoreCase(dto.unit()));

            if (unitMatches && dto.amount() != null) {
                double currentAmount = existing.amount != null ? existing.amount : 0.0;
                existing.amount = roundToTwoDecimals(currentAmount + dto.amount());
            } else if (existing.amount == null && dto.amount() != null) {
                existing.amount = dto.amount();
                existing.unit = dto.unit();
            }

            if (dto.recipeTitle() != null && !dto.recipeTitle().isBlank()) {
                if (existing.recipeTitle == null || existing.recipeTitle.isBlank()) {
                    existing.recipeTitle = dto.recipeTitle().trim();
                } else if (!existing.recipeTitle.contains(dto.recipeTitle().trim())) {
                    existing.recipeTitle = existing.recipeTitle + ", " + dto.recipeTitle().trim();
                }
            }

            return toDto(existing);
        }

        // New item
        ShoppingListItem item = new ShoppingListItem();
        item.name = cleanName;
        item.category = dto.category() != null ? dto.category() : IngredientCategory.OTHER;
        item.amount = dto.amount() != null ? roundToTwoDecimals(dto.amount()) : null;
        item.unit = dto.unit() != null ? dto.unit().trim() : null;
        item.recipeTitle = dto.recipeTitle() != null ? dto.recipeTitle().trim() : null;
        item.checked = false;

        if (dto.ingredientId() != null) {
            item.ingredient = ingredientRepository.findById(dto.ingredientId());
        } else {
            // Check if ingredient exists by name
            item.ingredient = ingredientRepository.findByNameIgnoreCase(cleanName).orElse(null);
        }

        shoppingListItemRepository.persist(item);
        return toDto(item);
    }

    @Transactional
    public List<ShoppingListItemResponseDto> createBatch(List<CreateShoppingListItemDto> dtos) {
        List<ShoppingListItemResponseDto> results = new ArrayList<>();
        if (dtos == null || dtos.isEmpty()) {
            return results;
        }

        for (CreateShoppingListItemDto dto : dtos) {
            results.add(create(dto));
        }
        return results;
    }

    @Transactional
    public ShoppingListItemResponseDto update(Long id, UpdateShoppingListItemDto dto) {
        ShoppingListItem item = shoppingListItemRepository.findByIdOptional(id)
                .orElseThrow(() -> new NotFoundException("Shopping list item not found with id: " + id));

        if (dto.name() != null && !dto.name().isBlank()) {
            item.name = dto.name().trim();
        }

        if (dto.category() != null) {
            item.category = dto.category();
        }

        if (dto.amount() != null) {
            item.amount = roundToTwoDecimals(dto.amount());
        }

        if (dto.unit() != null) {
            item.unit = dto.unit().trim();
        }

        if (dto.checked() != null) {
            item.checked = dto.checked();
        }

        if (dto.recipeTitle() != null) {
            item.recipeTitle = dto.recipeTitle().trim();
        }

        if (dto.ingredientId() != null) {
            item.ingredient = ingredientRepository.findById(dto.ingredientId());
        }

        return toDto(item);
    }

    @Transactional
    public ShoppingListItemResponseDto toggleChecked(Long id) {
        ShoppingListItem item = shoppingListItemRepository.findByIdOptional(id)
                .orElseThrow(() -> new NotFoundException("Shopping list item not found with id: " + id));

        item.checked = !item.checked;
        return toDto(item);
    }

    @Transactional
    public void delete(Long id) {
        boolean deleted = shoppingListItemRepository.deleteById(id);
        if (!deleted) {
            throw new NotFoundException("Shopping list item not found with id: " + id);
        }
    }

    @Transactional
    public long clearCompleted() {
        return shoppingListItemRepository.deleteChecked();
    }

    @Transactional
    public long clearAll() {
        return shoppingListItemRepository.deleteAll();
    }

    @Transactional
    public int syncCheckedToPantry() {
        List<ShoppingListItem> checkedItems = shoppingListItemRepository.findByChecked(true);
        if (checkedItems.isEmpty()) {
            return 0;
        }

        int syncedCount = 0;
        for (ShoppingListItem item : checkedItems) {
            CreatePantryItemDto pantryDto = new CreatePantryItemDto(
                    item.name,
                    item.category,
                    item.amount != null ? item.amount : 1.0,
                    item.unit != null ? item.unit : "st",
                    true
            );
            pantryService.addOrUpdateItem(pantryDto);
            syncedCount++;
        }

        shoppingListItemRepository.deleteChecked();
        return syncedCount;
    }

    public ShoppingListItemResponseDto toDto(ShoppingListItem item) {
        return new ShoppingListItemResponseDto(
                item.id,
                item.ingredient != null ? item.ingredient.id : null,
                item.name,
                item.category,
                item.amount,
                item.unit,
                item.checked,
                item.recipeTitle,
                item.createdAt
        );
    }

    private double roundToTwoDecimals(double val) {
        return Math.round(val * 100.0) / 100.0;
    }
}
