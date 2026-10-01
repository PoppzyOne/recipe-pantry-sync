package com.recipesync;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;

@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ShoppingListResourceTest {

    private static Long createdItemId;

    @Test
    @Order(1)
    void testListShoppingListInitially() {
        given()
                .when().get("/api/shopping-list")
                .then()
                .statusCode(200);
    }

    @Test
    @Order(2)
    void testAddShoppingListItemValidation_MissingName() {
        String json = """
                {
                    "category": "DAIRY",
                    "amount": 2.0,
                    "unit": "dl"
                }
                """;

        given()
                .contentType(ContentType.JSON)
                .body(json)
                .when().post("/api/shopping-list")
                .then()
                .statusCode(400);
    }

    @Test
    @Order(3)
    void testAddShoppingListItem() {
        String json = """
                {
                    "name": "Vispgrädde",
                    "category": "DAIRY",
                    "amount": 3.0,
                    "unit": "dl",
                    "recipeTitle": "Köttbullar med potatismos"
                }
                """;

        createdItemId = ((Number) given()
                .contentType(ContentType.JSON)
                .body(json)
                .when().post("/api/shopping-list")
                .then()
                .statusCode(201)
                .body("id", notNullValue())
                .body("name", equalTo("Vispgrädde"))
                .body("category", equalTo("DAIRY"))
                .body("amount", equalTo(3.0f))
                .body("unit", equalTo("dl"))
                .body("checked", equalTo(false))
                .body("recipeTitle", equalTo("Köttbullar med potatismos"))
                .extract().path("id")).longValue();
    }

    @Test
    @Order(4)
    void testAddShoppingListItem_MergeIdenticalUnchecked() {
        String json = """
                {
                    "name": "Vispgrädde",
                    "category": "DAIRY",
                    "amount": 2.0,
                    "unit": "dl",
                    "recipeTitle": "Jordgubbstårta"
                }
                """;

        given()
                .contentType(ContentType.JSON)
                .body(json)
                .when().post("/api/shopping-list")
                .then()
                .statusCode(201)
                .body("id", equalTo(createdItemId.intValue()))
                .body("name", equalTo("Vispgrädde"))
                .body("amount", equalTo(5.0f));
    }

    @Test
    @Order(5)
    void testGetItemById() {
        given()
                .when().get("/api/shopping-list/" + createdItemId)
                .then()
                .statusCode(200)
                .body("id", equalTo(createdItemId.intValue()))
                .body("name", equalTo("Vispgrädde"));
    }

    @Test
    @Order(6)
    void testUpdateShoppingListItem() {
        String json = """
                {
                    "name": "Vispgrädde 40%",
                    "category": "DAIRY",
                    "amount": 6.0,
                    "unit": "dl"
                }
                """;

        given()
                .contentType(ContentType.JSON)
                .body(json)
                .when().put("/api/shopping-list/" + createdItemId)
                .then()
                .statusCode(200)
                .body("name", equalTo("Vispgrädde 40%"))
                .body("amount", equalTo(6.0f));
    }

    @Test
    @Order(7)
    void testToggleChecked() {
        given()
                .when().patch("/api/shopping-list/" + createdItemId + "/toggle")
                .then()
                .statusCode(200)
                .body("checked", equalTo(true));

        // Toggle back to false
        given()
                .when().patch("/api/shopping-list/" + createdItemId + "/toggle")
                .then()
                .statusCode(200)
                .body("checked", equalTo(false));
    }

    @Test
    @Order(8)
    void testBatchAdd() {
        String json = """
                [
                    {
                        "name": "Färsk basilika",
                        "category": "PRODUCE",
                        "amount": 1.0,
                        "unit": "kruka"
                    },
                    {
                        "name": "Krossade tomater",
                        "category": "PANTRY",
                        "amount": 2.0,
                        "unit": "burk"
                    }
                ]
                """;

        given()
                .contentType(ContentType.JSON)
                .body(json)
                .when().post("/api/shopping-list/batch")
                .then()
                .statusCode(201)
                .body("size()", equalTo(2));
    }

    @Test
    @Order(9)
    void testSyncCheckedToPantry() {
        // Toggle createdItem to checked
        given()
                .when().patch("/api/shopping-list/" + createdItemId + "/toggle")
                .then()
                .statusCode(200)
                .body("checked", equalTo(true));

        // Sync checked items to pantry
        given()
                .when().post("/api/shopping-list/sync-to-pantry")
                .then()
                .statusCode(200)
                .body("syncedCount", greaterThanOrEqualTo(1));

        // Verify the checked item was removed from shopping list
        given()
                .when().get("/api/shopping-list/" + createdItemId)
                .then()
                .statusCode(404);
    }

    @Test
    @Order(10)
    void testClearCompleted() {
        // Add an item and check it
        String json = """
                {
                    "name": "Testvara att radera",
                    "category": "OTHER",
                    "amount": 1.0,
                    "unit": "st"
                }
                """;

        Number tempId = given()
                .contentType(ContentType.JSON)
                .body(json)
                .when().post("/api/shopping-list")
                .then()
                .statusCode(201)
                .extract().path("id");

        given()
                .when().patch("/api/shopping-list/" + tempId.longValue() + "/toggle")
                .then()
                .statusCode(200)
                .body("checked", equalTo(true));

        given()
                .when().delete("/api/shopping-list/completed")
                .then()
                .statusCode(200);

        given()
                .when().get("/api/shopping-list/" + tempId.longValue())
                .then()
                .statusCode(404);
    }

    @Test
    @Order(11)
    void testDeleteItem_NotFound() {
        given()
                .when().delete("/api/shopping-list/999999")
                .then()
                .statusCode(404);
    }
}
