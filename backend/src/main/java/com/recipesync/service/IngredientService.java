package com.recipesync.service;

import com.recipesync.entity.Ingredient;
import com.recipesync.entity.IngredientCategory;
import com.recipesync.repository.IngredientRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.List;

@ApplicationScoped
public class IngredientService {

    private final IngredientRepository ingredientRepository;

    @Inject
    public IngredientService(IngredientRepository ingredientRepository) {
        this.ingredientRepository = ingredientRepository;
    }

    @Transactional
    public Ingredient getOrCreate(String name, IngredientCategory category) {
        String cleanName = name.trim();
        return ingredientRepository.findByNameIgnoreCase(cleanName)
                .orElseGet(() -> {
                    Ingredient newIngredient = new Ingredient();
                    newIngredient.name = cleanName;
                    newIngredient.category = category != null ? category : IngredientCategory.PANTRY;
                    ingredientRepository.persist(newIngredient);
                    return newIngredient;
                });
    }

    public List<Ingredient> listAll() {
        return ingredientRepository.listAll();
    }
}
