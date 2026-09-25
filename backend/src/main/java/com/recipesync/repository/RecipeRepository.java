package com.recipesync.repository;

import com.recipesync.entity.Recipe;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class RecipeRepository implements PanacheRepository<Recipe> {
    // Custom query methods can be defined here as the domain evolves
}
