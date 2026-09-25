package com.recipesync.repository;

import com.recipesync.entity.PantryItem;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class PantryItemRepository implements PanacheRepository<PantryItem> {

    public Optional<PantryItem> findByIngredientId(Long ingredientId) {
        return find("ingredient.id", ingredientId).firstResultOptional();
    }

    public List<PantryItem> listInStock() {
        return list("inStock", true);
    }
}
