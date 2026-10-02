package com.recipesync;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.recipesync.dto.ImportedRecipeDto;
import com.recipesync.entity.IngredientCategory;
import com.recipesync.service.RecipeImportService;
import jakarta.ws.rs.BadRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RecipeImportServiceTest {

    private RecipeImportService importService;

    @BeforeEach
    void setUp() {
        importService = new RecipeImportService(new ObjectMapper());
    }

    @Test
    void testParseRecipeFromJsonLd() {
        String html = """
                <!DOCTYPE html>
                <html>
                <head>
                    <title>Recept</title>
                    <script type="application/ld+json">
                    {
                      "@context": "https://schema.org",
                      "@type": "Recipe",
                      "name": "Krämig laxpasta med spenat",
                      "description": "En fantastisk pasta med lax och färsk spenat.",
                      "image": "https://example.com/laxpasta.jpg",
                      "prepTime": "PT10M",
                      "cookTime": "PT15M",
                      "totalTime": "PT25M",
                      "recipeYield": "4 portioner",
                      "recipeIngredient": [
                        "400 g laxfilé",
                        "300 g tagliatelle",
                        "2.5 dl matlagningsgrädde",
                        "1 st rödlök",
                        "65 g babyspenat",
                        "salt och nymalen svartpeppar"
                      ],
                      "recipeInstructions": [
                        {
                          "@type": "HowToStep",
                          "text": "Koka pastan enligt anvisningen på förpackningen."
                        },
                        {
                          "@type": "HowToStep",
                          "text": "Tärna laxen och hacka löken."
                        },
                        {
                          "@type": "HowToStep",
                          "text": "Fräs lök och lax hastigt i lite smör, häll på grädden och vänd ner spenaten."
                        }
                      ]
                    }
                    </script>
                </head>
                <body>
                    <h1>Krämig laxpasta</h1>
                </body>
                </html>
                """;

        ImportedRecipeDto dto = importService.parseRecipeFromHtml(html, "https://example.com/recept/laxpasta");

        assertNotNull(dto);
        assertEquals("Krämig laxpasta med spenat", dto.title());
        assertEquals("En fantastisk pasta med lax och färsk spenat.", dto.description());
        assertEquals(4, dto.servings());
        assertEquals(10, dto.prepTimeMinutes());
        assertEquals(15, dto.cookTimeMinutes());
        assertEquals("https://example.com/recept/laxpasta", dto.sourceUrl());
        assertEquals("https://example.com/laxpasta.jpg", dto.imageUrl());

        assertEquals(6, dto.ingredients().size());
        assertEquals("Laxfilé", dto.ingredients().get(0).name());
        assertEquals(400.0, dto.ingredients().get(0).amount());
        assertEquals("g", dto.ingredients().get(0).unit());
        assertEquals(IngredientCategory.MEAT, dto.ingredients().get(0).category());

        assertEquals("Tagliatelle", dto.ingredients().get(1).name());
        assertEquals(IngredientCategory.PANTRY, dto.ingredients().get(1).category());

        assertEquals("Matlagningsgrädde", dto.ingredients().get(2).name());
        assertEquals(IngredientCategory.DAIRY, dto.ingredients().get(2).category());

        assertNotNull(dto.instructions());
        assertTrue(dto.instructions().contains("1. Koka pastan"));
        assertTrue(dto.instructions().contains("2. Tärna laxen"));
    }

    @Test
    void testParseRecipeFromGraphFormat() {
        String html = """
                <!DOCTYPE html>
                <html>
                <head>
                    <script type="application/ld+json">
                    {
                      "@context": "https://schema.org",
                      "@graph": [
                        {
                          "@type": "WebSite",
                          "name": "Mina Recept"
                        },
                        {
                          "@type": "Recipe",
                          "name": "Klassiska pannkakor",
                          "recipeYield": 4,
                          "totalTime": "PT20M",
                          "recipeIngredient": [
                            "3 ägg",
                            "6 dl mjölk",
                            "2.5 dl vetemjöl",
                            "1/2 tsk salt",
                            "smör till stekning"
                          ],
                          "recipeInstructions": [
                            "Vispa samman mjöl och hälften av mjölken till en slät smet.",
                            "Tillsätt resten av mjölken och äggen samt salt.",
                            "Stek tunna pannkakor i smör."
                          ]
                        }
                      ]
                    }
                    </script>
                </head>
                </html>
                """;

        ImportedRecipeDto dto = importService.parseRecipeFromHtml(html, "https://example.com/pannkakor");

        assertNotNull(dto);
        assertEquals("Klassiska pannkakor", dto.title());
        assertEquals(4, dto.servings());
        assertEquals(20, dto.cookTimeMinutes());
        assertEquals(5, dto.ingredients().size());
        assertTrue(dto.instructions().contains("1. Vispa"));
    }

    @Test
    void testMissingRecipeThrowsBadRequest() {
        String html = """
                <!DOCTYPE html>
                <html>
                <head><title>Bara en vanlig bloggpost</title></head>
                <body><p>Inget recept här</p></body>
                </html>
                """;

        assertThrows(BadRequestException.class, () -> importService.parseRecipeFromHtml(html, "https://example.com"));
    }

    @Test
    void testImportFromRawText() {
        String rawText = """
                Krämig Kycklinggryta
                4 portioner
                30 min

                Ingredienser:
                500 g kycklingfilé
                2 dl crème fraiche
                1 st gul lök
                1 klyfta vitlök
                salt och peppar

                Gör så här:
                1. Skär kycklingen och fräs i olja.
                2. Tillsätt lök och vitlök.
                3. Blanda i crème fraiche och låt puttra.
                """;

        ImportedRecipeDto dto = importService.importFromText(rawText);

        assertNotNull(dto);
        assertEquals("Krämig Kycklinggryta", dto.title());
        assertEquals(4, dto.servings());
        assertEquals(30, dto.cookTimeMinutes());
        assertEquals(5, dto.ingredients().size());
        assertEquals("Kycklingfilé", dto.ingredients().get(0).name());
        assertEquals(500.0, dto.ingredients().get(0).amount());
        assertEquals(IngredientCategory.MEAT, dto.ingredients().get(0).category());
        assertTrue(dto.instructions().contains("Skär kycklingen"));
    }
}
