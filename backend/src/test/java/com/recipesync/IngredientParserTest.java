package com.recipesync;

import com.recipesync.dto.CreateRecipeIngredientDto;
import com.recipesync.entity.IngredientCategory;
import com.recipesync.service.IngredientParser;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class IngredientParserTest {

    @Test
    void testStandardWeightAndVolume() {
        CreateRecipeIngredientDto chicken = IngredientParser.parse("400 g kycklingfilé");
        assertNotNull(chicken);
        assertEquals(400.0, chicken.amount());
        assertEquals("g", chicken.unit());
        assertEquals("Kycklingfilé", chicken.name());
        assertEquals(IngredientCategory.MEAT, chicken.category());

        CreateRecipeIngredientDto cream = IngredientParser.parse("2.5 dl vispgrädde");
        assertNotNull(cream);
        assertEquals(2.5, cream.amount());
        assertEquals("dl", cream.unit());
        assertEquals("Vispgrädde", cream.name());
        assertEquals(IngredientCategory.DAIRY, cream.category());
    }

    @Test
    void testFractionsAndDecimals() {
        CreateRecipeIngredientDto salt = IngredientParser.parse("1/2 tsk salt");
        assertNotNull(salt);
        assertEquals(0.5, salt.amount());
        assertEquals("tsk", salt.unit());
        assertEquals("Salt", salt.name());
        assertEquals(IngredientCategory.SPICES, salt.category());

        CreateRecipeIngredientDto unicodeFraction = IngredientParser.parse("½ tsk cayennepeppar");
        assertNotNull(unicodeFraction);
        assertEquals(0.5, unicodeFraction.amount());
        assertEquals("tsk", unicodeFraction.unit());
        assertEquals(IngredientCategory.SPICES, unicodeFraction.category());

        CreateRecipeIngredientDto mixed = IngredientParser.parse("1 1/2 dl vetemjöl");
        assertNotNull(mixed);
        assertEquals(1.5, mixed.amount());
        assertEquals("dl", mixed.unit());
        assertEquals("Vetemjöl", mixed.name());
        assertEquals(IngredientCategory.PANTRY, mixed.category());

        CreateRecipeIngredientDto comma = IngredientParser.parse("1,5 msk olivolja");
        assertNotNull(comma);
        assertEquals(1.5, comma.amount());
        assertEquals("msk", comma.unit());
        assertEquals("Olivolja", comma.name());
        assertEquals(IngredientCategory.PANTRY, comma.category());
    }

    @Test
    void testCountableItemsWithoutUnit() {
        CreateRecipeIngredientDto eggs = IngredientParser.parse("3 ägg");
        assertNotNull(eggs);
        assertEquals(3.0, eggs.amount());
        assertEquals("st", eggs.unit());
        assertEquals("Ägg", eggs.name());
        assertEquals(IngredientCategory.DAIRY, eggs.category());

        CreateRecipeIngredientDto carrots = IngredientParser.parse("2 morötter");
        assertNotNull(carrots);
        assertEquals(2.0, carrots.amount());
        assertEquals("st", carrots.unit());
        assertEquals("Morötter", carrots.name());
        assertEquals(IngredientCategory.PRODUCE, carrots.category());
    }

    @Test
    void testUnitsWithNotesInParentheses() {
        CreateRecipeIngredientDto cans = IngredientParser.parse("2 burkar krossade tomater (à 400 g)");
        assertNotNull(cans);
        assertEquals(2.0, cans.amount());
        assertEquals("burk", cans.unit());
        assertEquals("Krossade tomater", cans.name());
        assertEquals("à 400 g", cans.notes());
        assertEquals(IngredientCategory.PANTRY, cans.category());

        CreateRecipeIngredientDto onion = IngredientParser.parse("1 st rödlök (finhackad)");
        assertNotNull(onion);
        assertEquals(1.0, onion.amount());
        assertEquals("st", onion.unit());
        assertEquals("Rödlök", onion.name());
        assertEquals("finhackad", onion.notes());
        assertEquals(IngredientCategory.PRODUCE, onion.category());
    }

    @Test
    void testItemsWithoutAmount() {
        CreateRecipeIngredientDto seasoning = IngredientParser.parse("salt och nymalen svartpeppar");
        assertNotNull(seasoning);
        assertNull(seasoning.amount());
        assertNull(seasoning.unit());
        assertEquals("Salt och nymalen svartpeppar", seasoning.name());
        assertEquals(IngredientCategory.SPICES, seasoning.category());
    }

    @Test
    void testGarlicCloves() {
        CreateRecipeIngredientDto garlic = IngredientParser.parse("2 klyftor vitlök");
        assertNotNull(garlic);
        assertEquals(2.0, garlic.amount());
        assertEquals("klyfta", garlic.unit());
        assertEquals("Vitlök", garlic.name());
        assertEquals(IngredientCategory.PRODUCE, garlic.category());
    }
}
