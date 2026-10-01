package com.recipesync.repository;

import com.recipesync.entity.ShoppingListItem;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class ShoppingListItemRepository implements PanacheRepository<ShoppingListItem> {

    public List<ShoppingListItem> listAllOrdered() {
        return list("order by checked asc, id desc");
    }

    public List<ShoppingListItem> findByChecked(boolean checked) {
        return list("checked = ?1 order by id desc", checked);
    }

    public Optional<ShoppingListItem> findByNameAndUnchecked(String name) {
        return find("lower(trim(name)) = ?1 and checked = false", name.trim().toLowerCase()).firstResultOptional();
    }

    public long deleteChecked() {
        return delete("checked = true");
    }
}
