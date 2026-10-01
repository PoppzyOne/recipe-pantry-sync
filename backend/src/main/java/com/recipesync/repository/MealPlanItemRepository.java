package com.recipesync.repository;

import com.recipesync.entity.MealPlanItem;
import com.recipesync.entity.MealType;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import java.time.LocalDate;
import java.util.List;

@ApplicationScoped
public class MealPlanItemRepository implements PanacheRepository<MealPlanItem> {

    public List<MealPlanItem> findBetweenDates(LocalDate startDate, LocalDate endDate) {
        return list("planDate >= ?1 and planDate <= ?2 order by planDate asc, mealType asc", startDate, endDate);
    }

    public List<MealPlanItem> findByDate(LocalDate date) {
        return list("planDate = ?1 order by mealType asc", date);
    }

    public List<MealPlanItem> findByDateAndMealType(LocalDate date, MealType mealType) {
        return list("planDate = ?1 and mealType = ?2", date, mealType);
    }
}
