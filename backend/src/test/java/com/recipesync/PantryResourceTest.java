package com.recipesync;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;

@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PantryResourceTest {

    @Test
    @Order(1)
    void testListPantryItems() {
        given()
                .when().get("/api/pantry")
                .then()
                .statusCode(200)
                .body("size()", greaterThanOrEqualTo(6));
    }

    @Test
    @Order(2)
    void testListInStockPantryItems() {
        given()
                .queryParam("inStock", true)
                .when().get("/api/pantry")
                .then()
                .statusCode(200)
                .body("inStock", org.hamcrest.Matchers.everyItem(is(true)));
    }

    @Test
    @Order(3)
    void testAddPantryItem() {
        String json = """
                {
                    "ingredientName": "Havregryn",
                    "category": "PANTRY",
                    "quantity": 1000.0,
                    "unit": "g",
                    "inStock": true
                }
                """;

        given()
                .contentType(ContentType.JSON)
                .body(json)
                .when().post("/api/pantry")
                .then()
                .statusCode(201)
                .body("ingredientName", is("Havregryn"))
                .body("quantity", is(1000.0f))
                .body("inStock", is(true));
    }

    @Test
    @Order(4)
    void testToggleStockStatus() {
        // Initially Spaghetti (id 1) is in_stock = true
        given()
                .when().patch("/api/pantry/1/toggle")
                .then()
                .statusCode(200)
                .body("inStock", is(false));

        // Toggle back to true
        given()
                .when().patch("/api/pantry/1/toggle")
                .then()
                .statusCode(200)
                .body("inStock", is(true));
    }

    @Test
    @Order(5)
    void testMissingIngredientsForCarbonara() {
        // Carbonara has: Spaghetti (in stock), Guanciale (out of stock), Ägg (in stock), Pecorino (out of stock)
        // Missing ingredients should include Guanciale and Pecorino Romano
        given()
                .when().get("/api/recipes/1/missing-ingredients")
                .then()
                .statusCode(200)
                .body("name", hasItem("Guanciale"))
                .body("name", hasItem("Pecorino Romano"));
    }

    @Test
    @Order(6)
    void testUpdatePantryItemWithQuantityAndLevel() {
        String updateJson = """
                {
                    "quantity": 750.0,
                    "unit": "g",
                    "quantityLevel": "LOW",
                    "inStock": true
                }
                """;

        given()
                .contentType(ContentType.JSON)
                .body(updateJson)
                .when().put("/api/pantry/1")
                .then()
                .statusCode(200)
                .body("id", is(1))
                .body("quantity", is(750.0f))
                .body("unit", is("g"))
                .body("quantityLevel", is("LOW"))
                .body("inStock", is(true));
    }

    @Test
    @Order(7)
    void testUpdatePantryItemToEmptySetsInStockFalse() {
        String updateJson = """
                {
                    "quantityLevel": "EMPTY"
                }
                """;

        given()
                .contentType(ContentType.JSON)
                .body(updateJson)
                .when().put("/api/pantry/1")
                .then()
                .statusCode(200)
                .body("id", is(1))
                .body("quantityLevel", is("EMPTY"))
                .body("inStock", is(false));
    }

    @Test
    @Order(8)
    void testUpdatePantryItemWithCategoryAndNoQuantityLevel() {
        String updateJson = """
                {
                    "category": "PRODUCE",
                    "quantity": 600.0,
                    "unit": "g",
                    "quantityLevel": null,
                    "inStock": true
                }
                """;

        given()
                .contentType(ContentType.JSON)
                .body(updateJson)
                .when().put("/api/pantry/1")
                .then()
                .statusCode(200)
                .body("id", is(1))
                .body("category", is("PRODUCE"))
                .body("quantity", is(600.0f))
                .body("quantityLevel", org.hamcrest.CoreMatchers.nullValue())
                .body("inStock", is(true));
    }

    @Test
    @Order(9)
    void testDeletePantryItem() {
        // Delete item 5 (Gul lök)
        given()
                .when().delete("/api/pantry/5")
                .then()
                .statusCode(204);

        // Verify it is no longer found
        given()
                .when().get("/api/pantry/5")
                .then()
                .statusCode(404);
    }
}
