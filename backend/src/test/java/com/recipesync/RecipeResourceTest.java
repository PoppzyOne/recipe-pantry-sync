package com.recipesync;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;

@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class RecipeResourceTest {

    @Test
    @Order(1)
    void testListRecipesEndpoint() {
        given()
                .when().get("/api/recipes")
                .then()
                .statusCode(200)
                .body("size()", greaterThanOrEqualTo(2));
    }

    @Test
    @Order(2)
    void testGetRecipeByIdSuccess() {
        given()
                .when().get("/api/recipes/1")
                .then()
                .statusCode(200)
                .body("id", is(1))
                .body("title", is("Krämig Pasta Carbonara"))
                .body("servings", is(4));
    }

    @Test
    @Order(3)
    void testGetRecipeByIdNotFound() {
        given()
                .when().get("/api/recipes/99999")
                .then()
                .statusCode(404);
    }

    @Test
    @Order(4)
    void testCreateRecipeEndpointSuccess() {
        String newRecipeJson = """
                {
                    "title": "Pannkakor",
                    "description": "Frasiga och tunna pannkakor.",
                    "instructions": "Vispa ihop och stek i smör.",
                    "servings": 4,
                    "prepTimeMinutes": 5,
                    "cookTimeMinutes": 15
                }
                """;

        given()
                .contentType(ContentType.JSON)
                .body(newRecipeJson)
                .when().post("/api/recipes")
                .then()
                .statusCode(201)
                .header("Location", notNullValue())
                .body("title", is("Pannkakor"))
                .body("servings", is(4))
                .body("createdAt", notNullValue());
    }

    @Test
    @Order(5)
    void testCreateRecipeWithIngredientsSuccess() {
        String json = """
                {
                    "title": "Bolognese",
                    "description": "Klassisk italiensk köttfärssås.",
                    "instructions": "1. Fräs lök och färs. 2. Låt puttra.",
                    "servings": 4,
                    "prepTimeMinutes": 10,
                    "cookTimeMinutes": 60,
                    "ingredients": [
                        {
                            "name": "Nötfärs",
                            "category": "MEAT",
                            "amount": 500.0,
                            "unit": "g"
                        },
                        {
                            "name": "Morot",
                            "category": "PRODUCE",
                            "amount": 2.0,
                            "unit": "st",
                            "notes": "finriven"
                        }
                    ]
                }
                """;

        given()
                .contentType(ContentType.JSON)
                .body(json)
                .when().post("/api/recipes")
                .then()
                .statusCode(201)
                .body("title", is("Bolognese"))
                .body("ingredients.size()", is(2))
                .body("ingredients[0].name", is("Nötfärs"))
                .body("ingredients[1].name", is("Morot"));
    }

    @Test
    @Order(6)
    void testCreateRecipeWithMissingTitle() {
        String invalidRecipeJson = """
                {
                    "description": "Missing title recipe",
                    "servings": 2
                }
                """;

        given()
                .contentType(ContentType.JSON)
                .body(invalidRecipeJson)
                .when().post("/api/recipes")
                .then()
                .statusCode(400);
    }

    @Test
    @Order(6)
    void testCreateRecipeWithBlankTitle() {
        String invalidRecipeJson = """
                {
                    "title": "   ",
                    "description": "Blank title recipe",
                    "servings": 2
                }
                """;

        given()
                .contentType(ContentType.JSON)
                .body(invalidRecipeJson)
                .when().post("/api/recipes")
                .then()
                .statusCode(400);
    }

    @Test
    @Order(7)
    void testCreateRecipeWithNegativeServings() {
        String invalidRecipeJson = """
                {
                    "title": "Vegetarisk Lasagne",
                    "servings": -2
                }
                """;

        given()
                .contentType(ContentType.JSON)
                .body(invalidRecipeJson)
                .when().post("/api/recipes")
                .then()
                .statusCode(400);
    }

    @Test
    @Order(8)
    void testUpdateRecipeSuccess() {
        String updateJson = """
                {
                    "title": "Krämig Pasta Carbonara (Extra Pecorino)",
                    "description": "Uppdaterad med extra ost och nymalen svartpeppar.",
                    "instructions": "1. Koka al dente. 2. Blanda fläsk och ägg.",
                    "servings": 2,
                    "prepTimeMinutes": 12,
                    "cookTimeMinutes": 18
                }
                """;

        given()
                .contentType(ContentType.JSON)
                .body(updateJson)
                .when().put("/api/recipes/1")
                .then()
                .statusCode(200)
                .body("id", is(1))
                .body("title", is("Krämig Pasta Carbonara (Extra Pecorino)"))
                .body("servings", is(2));
    }

    @Test
    @Order(9)
    void testUpdateRecipeWithIngredientsSuccess() {
        String updateJson = """
                {
                    "title": "Carbonara Moderna",
                    "description": "Modifierat recept",
                    "instructions": "Enkel tillagning",
                    "servings": 3,
                    "prepTimeMinutes": 5,
                    "cookTimeMinutes": 12,
                    "ingredients": [
                        {
                            "name": "Spaghetti",
                            "category": "PANTRY",
                            "amount": 350.0,
                            "unit": "g"
                        },
                        {
                            "name": "Pancetta",
                            "category": "MEAT",
                            "amount": 150.0,
                            "unit": "g"
                        }
                    ]
                }
                """;

        given()
                .contentType(ContentType.JSON)
                .body(updateJson)
                .when().put("/api/recipes/1")
                .then()
                .statusCode(200)
                .body("id", is(1))
                .body("title", is("Carbonara Moderna"))
                .body("ingredients.size()", is(2))
                .body("ingredients[0].name", is("Spaghetti"))
                .body("ingredients[1].name", is("Pancetta"));
    }

    @Test
    @Order(10)
    void testUpdateRecipeWithBlankTitleReturns400() {
        String updateJson = """
                {
                    "title": "",
                    "servings": 2
                }
                """;

        given()
                .contentType(ContentType.JSON)
                .body(updateJson)
                .when().put("/api/recipes/1")
                .then()
                .statusCode(400);
    }

    @Test
    @Order(10)
    void testUpdateRecipeNotFound() {
        String updateJson = """
                {
                    "title": "Finns Inte",
                    "servings": 2
                }
                """;

        given()
                .contentType(ContentType.JSON)
                .body(updateJson)
                .when().put("/api/recipes/99999")
                .then()
                .statusCode(404);
    }

    @Test
    @Order(11)
    void testDeleteRecipeSuccess() {
        // First delete recipe with ID 2 (Chili con Carne from seed)
        given()
                .when().delete("/api/recipes/2")
                .then()
                .statusCode(204);

        // Verify that recipe with ID 2 is no longer found
        given()
                .when().get("/api/recipes/2")
                .then()
                .statusCode(404);
    }

    @Test
    @Order(12)
    void testDeleteRecipeNotFound() {
        given()
                .when().delete("/api/recipes/99999")
                .then()
                .statusCode(404);
    }
}
