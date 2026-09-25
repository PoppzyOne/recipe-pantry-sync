package com.recipesync.repository;

import com.recipesync.entity.Ingredient;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.Optional;

@ApplicationScoped
public class IngredientRepository implements PanacheRepository<Ingredient> {

    public Optional<Ingredient> findByNameIgnoreCase(String name) {
        return find("LOWER(name) = ?1", name.toLowerCase().trim()).firstResultOptional();
    }
}
