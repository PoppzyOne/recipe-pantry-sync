package com.recipesync.service;

import com.recipesync.dto.CreateMealPlanItemDto;
import com.recipesync.dto.MealPlanItemResponseDto;
import com.recipesync.dto.MealPlanShoppingItemDto;
import com.recipesync.dto.UpdateMealPlanItemDto;
import com.recipesync.entity.MealPlanItem;
import com.recipesync.entity.PantryItem;
import com.recipesync.entity.Recipe;
import com.recipesync.entity.RecipeIngredient;
import com.recipesync.repository.MealPlanItemRepository;
import com.recipesync.repository.PantryItemRepository;
import com.recipesync.repository.RecipeRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@ApplicationScoped
public class MealPlanService {

    private final MealPlanItemRepository mealPlanItemRepository;
    private final RecipeRepository recipeRepository;
    private final PantryItemRepository pantryItemRepository;

    @Inject
    public MealPlanService(
            MealPlanItemRepository mealPlanItemRepository,
            RecipeRepository recipeRepository,
            PantryItemRepository pantryItemRepository
    ) {
        this.mealPlanItemRepository = mealPlanItemRepository;
        this.recipeRepository = recipeRepository;
        this.pantryItemRepository = pantryItemRepository;
    }

    public List<MealPlanItemResponseDto> getMealPlans(LocalDate startDate, LocalDate endDate) {
        List<MealPlanItem> items;
        if (startDate != null && endDate != null) {
            items = mealPlanItemRepository.findBetweenDates(startDate, endDate);
        } else if (startDate != null) {
            items = mealPlanItemRepository.findByDate(startDate);
        } else {
            items = mealPlanItemRepository.listAll();
        }
        return items.stream().map(this::toDto).toList();
    }

    public MealPlanItemResponseDto getById(Long id) {
        return mealPlanItemRepository.findByIdOptional(id)
                .map(this::toDto)
                .orElseThrow(() -> new NotFoundException("Meal plan item not found with id: " + id));
    }

    @Transactional
    public MealPlanItemResponseDto create(CreateMealPlanItemDto dto) {
        if (dto.planDate() == null) {
            throw new BadRequestException("Plan date is required");
        }
        if (dto.mealType() == null) {
            throw new BadRequestException("Meal type is required");
        }
        if (dto.recipeId() == null && (dto.customTitle() == null || dto.customTitle().trim().isEmpty())) {
            throw new BadRequestException("Either recipeId or customTitle must be provided");
        }
        if (dto.servings() != null && dto.servings() <= 0) {
            throw new BadRequestException("Servings must be positive");
        }

        MealPlanItem item = new MealPlanItem();
        item.planDate = dto.planDate();
        item.mealType = dto.mealType();
        item.customTitle = dto.customTitle() != null ? dto.customTitle().trim() : null;
        item.notes = dto.notes() != null ? dto.notes().trim() : null;

        if (dto.recipeId() != null) {
            Recipe recipe = recipeRepository.findByIdOptional(dto.recipeId())
                    .orElseThrow(() -> new NotFoundException("Recipe not found with id: " + dto.recipeId()));
            item.recipe = recipe;
            item.servings = dto.servings() != null ? dto.servings() : (recipe.servings != null ? recipe.servings : 4);
        } else {
            item.servings = dto.servings() != null ? dto.servings() : 4;
        }

        mealPlanItemRepository.persist(item);
        return toDto(item);
    }

    @Transactional
    public MealPlanItemResponseDto update(Long id, UpdateMealPlanItemDto dto) {
        MealPlanItem item = mealPlanItemRepository.findByIdOptional(id)
                .orElseThrow(() -> new NotFoundException("Meal plan item not found with id: " + id));

        if (dto.planDate() != null) {
            item.planDate = dto.planDate();
        }
        if (dto.mealType() != null) {
            item.mealType = dto.mealType();
        }
        if (dto.servings() != null) {
            if (dto.servings() <= 0) {
                throw new BadRequestException("Servings must be positive");
            }
            item.servings = dto.servings();
        }
        if (dto.customTitle() != null) {
            item.customTitle = dto.customTitle().trim();
        }
        if (dto.notes() != null) {
            item.notes = dto.notes().trim();
        }

        if (dto.recipeId() != null) {
            Recipe recipe = recipeRepository.findByIdOptional(dto.recipeId())
                    .orElseThrow(() -> new NotFoundException("Recipe not found with id: " + dto.recipeId()));
            item.recipe = recipe;
        } else if (dto.recipeId() == null && dto.customTitle() != null) {
            // Cleared recipe, keep or set custom title
            if (item.customTitle == null || item.customTitle.isEmpty()) {
                throw new BadRequestException("Either recipeId or customTitle must be provided");
            }
            item.recipe = null;
        }

        return toDto(item);
    }

    @Transactional
    public void delete(Long id) {
        boolean deleted = mealPlanItemRepository.deleteById(id);
        if (!deleted) {
            throw new NotFoundException("Meal plan item not found with id: " + id);
        }
    }

    public List<MealPlanShoppingItemDto> calculateShoppingList(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw new BadRequestException("Both startDate and endDate are required");
        }
        if (startDate.isAfter(endDate)) {
            throw new BadRequestException("startDate must be before or equal to endDate");
        }

        List<MealPlanItem> mealPlans = mealPlanItemRepository.findBetweenDates(startDate, endDate);

        // Group required ingredients by ingredient ID + unit
        Map<String, AggregatedIngredient> aggregatedMap = new LinkedHashMap<>();

        for (MealPlanItem planItem : mealPlans) {
            if (planItem.recipe == null || planItem.recipe.ingredients == null) {
                continue;
            }

            int baseServings = (planItem.recipe.servings != null && planItem.recipe.servings > 0)
                    ? planItem.recipe.servings
                    : 4;
            int currentServings = (planItem.servings != null && planItem.servings > 0)
                    ? planItem.servings
                    : 4;
            double scaleFactor = (double) currentServings / baseServings;

            for (RecipeIngredient ri : planItem.recipe.ingredients) {
                String key = ri.ingredient.id + "_" + (ri.unit != null ? ri.unit.toLowerCase().trim() : "");
                double amount = ri.amount != null ? ri.amount * scaleFactor : 0.0;

                AggregatedIngredient agg = aggregatedMap.computeIfAbsent(key, k -> new AggregatedIngredient(
                        ri.ingredient.id,
                        ri.ingredient.name,
                        ri.ingredient.category,
                        ri.unit
                ));

                agg.totalNeeded += amount;
                if (!agg.recipeTitles.contains(planItem.recipe.title)) {
                    agg.recipeTitles.add(planItem.recipe.title);
                }
            }
        }

        // Compare against pantry stock
        List<MealPlanShoppingItemDto> result = new ArrayList<>();
        for (AggregatedIngredient agg : aggregatedMap.values()) {
            Optional<PantryItem> pantryItemOpt = pantryItemRepository.findByIngredientId(agg.ingredientId);

            double pantryAmount = 0.0;
            boolean hasStock = false;
            Double pQty = null;
            String pUnit = null;

            if (pantryItemOpt.isPresent()) {
                PantryItem pi = pantryItemOpt.get();
                hasStock = pi.inStock;
                pQty = pi.quantity;
                pUnit = pi.unit;
                if (pQty != null) {
                    pantryAmount = pQty;
                }
            }

            double roundedNeeded = roundToTwoDecimals(agg.totalNeeded);
            double missingAmount;

            if (hasStock) {
                if (pQty != null) {
                    Double convertedPantryQty = convertQuantity(pQty, pUnit, agg.unit);
                    if (convertedPantryQty != null) {
                        pantryAmount = convertedPantryQty;
                        missingAmount = Math.max(0.0, roundToTwoDecimals(roundedNeeded - convertedPantryQty));
                    } else if (isCompatibleStockUnit(pUnit, agg.unit)) {
                        missingAmount = Math.max(0.0, roundToTwoDecimals(roundedNeeded - pQty));
                    } else {
                        // In stock, but packaging unit is not directly convertible (e.g. 1 burk vs 2 msk)
                        missingAmount = 0.0;
                    }
                } else {
                    // In stock without specific numeric quantity tracked (qualitative)
                    missingAmount = 0.0;
                }
            } else {
                missingAmount = roundedNeeded > 0 ? roundedNeeded : 1.0;
            }

            result.add(new MealPlanShoppingItemDto(
                    agg.ingredientId,
                    agg.name,
                    agg.category,
                    roundedNeeded,
                    roundToTwoDecimals(pantryAmount),
                    missingAmount,
                    agg.unit,
                    agg.recipeTitles
            ));
        }

        return result;
    }


    public MealPlanItemResponseDto toDto(MealPlanItem item) {
        return new MealPlanItemResponseDto(
                item.id,
                item.planDate,
                item.mealType,
                item.recipe != null ? item.recipe.id : null,
                item.recipe != null ? item.recipe.title : null,
                item.customTitle,
                item.servings,
                item.notes,
                item.createdAt,
                item.updatedAt
        );
    }

    private Double convertQuantity(double quantity, String fromUnit, String toUnit) {
        if (fromUnit == null || toUnit == null) {
            return quantity;
        }
        String from = fromUnit.trim().toLowerCase();
        String to = toUnit.trim().toLowerCase();
        if (from.equals(to)) {
            return quantity;
        }

        // Weight conversions: g <-> kg
        if (from.equals("kg") && to.equals("g")) {
            return quantity * 1000.0;
        }
        if (from.equals("g") && to.equals("kg")) {
            return quantity / 1000.0;
        }

        // Volume conversions: ml, cl, dl, l, msk, tsk, krm
        Double fromMl = toMilliliters(quantity, from);
        if (fromMl != null) {
            return fromMilliliters(fromMl, to);
        }

        return null;
    }

    private Double toMilliliters(double qty, String unit) {
        return switch (unit) {
            case "ml", "krm" -> qty;
            case "cl" -> qty * 10.0;
            case "msk" -> qty * 15.0;
            case "tsk" -> qty * 5.0;
            case "dl" -> qty * 100.0;
            case "l" -> qty * 1000.0;
            default -> null;
        };
    }

    private Double fromMilliliters(double ml, String targetUnit) {
        return switch (targetUnit) {
            case "ml", "krm" -> ml;
            case "cl" -> ml / 10.0;
            case "msk" -> ml / 15.0;
            case "tsk" -> ml / 5.0;
            case "dl" -> ml / 100.0;
            case "l" -> ml / 1000.0;
            default -> null;
        };
    }

    private boolean isCompatibleStockUnit(String pUnit, String recipeUnit) {
        if (pUnit == null || recipeUnit == null) {
            return true;
        }
        return pUnit.trim().equalsIgnoreCase(recipeUnit.trim());
    }

    private double roundToTwoDecimals(double val) {
        return Math.round(val * 100.0) / 100.0;
    }

    private static class AggregatedIngredient {
        final Long ingredientId;
        final String name;
        final com.recipesync.entity.IngredientCategory category;
        final String unit;
        double totalNeeded = 0.0;
        final List<String> recipeTitles = new ArrayList<>();

        AggregatedIngredient(Long ingredientId, String name, com.recipesync.entity.IngredientCategory category, String unit) {
            this.ingredientId = ingredientId;
            this.name = name;
            this.category = category;
            this.unit = unit;
        }
    }
}
