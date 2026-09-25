package com.recipesync.repository;

import com.recipesync.entity.RecipeIngredient;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;

@ApplicationScoped
public class RecipeIngredientRepository implements PanacheRepository<RecipeIngredient> {

    public List<RecipeIngredient> listByRecipeId(Long recipeId) {
        return list("recipe.id", recipeId);
    }
}
