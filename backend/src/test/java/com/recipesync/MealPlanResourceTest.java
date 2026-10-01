package com.recipesync;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.time.LocalDate;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;

@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class MealPlanResourceTest {

    private static Long createdId;

    @Test
    @Order(1)
    void testListMealPlans() {
        given()
                .when().get("/api/meal-plans")
                .then()
                .statusCode(200)
                .body("size()", greaterThanOrEqualTo(1));
    }

    @Test
    @Order(2)
    void testCreateMealPlanWithRecipe() {
        String today = LocalDate.now().toString();
        String json = """
                {
                    "planDate": "%s",
                    "mealType": "DINNER",
                    "recipeId": 2,
                    "servings": 4,
                    "notes": "Chili con carne kväll"
                }
                """.formatted(today);

        Number id = given()
                .contentType(ContentType.JSON)
                .body(json)
                .when().post("/api/meal-plans")
                .then()
                .statusCode(201)
                .body("id", notNullValue())
                .body("recipeId", equalTo(2))
                .body("recipeTitle", equalTo("Klassisk Chili con Carne"))
                .body("servings", equalTo(4))
                .body("notes", equalTo("Chili con carne kväll"))
                .extract().path("id");

        createdId = id.longValue();
    }

    @Test
    @Order(3)
    void testGetMealPlanById() {
        given()
                .when().get("/api/meal-plans/" + createdId)
                .then()
                .statusCode(200)
                .body("id", equalTo(createdId.intValue()))
                .body("recipeTitle", equalTo("Klassisk Chili con Carne"));
    }

    @Test
    @Order(4)
    void testUpdateMealPlan() {
        String tomorrow = LocalDate.now().plusDays(1).toString();
        String json = """
                {
                    "planDate": "%s",
                    "mealType": "LUNCH",
                    "recipeId": null,
                    "customTitle": "Rester från gårdagen",
                    "servings": 2,
                    "notes": "Värm i mikron"
                }
                """.formatted(tomorrow);

        given()
                .contentType(ContentType.JSON)
                .body(json)
                .when().put("/api/meal-plans/" + createdId)
                .then()
                .statusCode(200)
                .body("id", equalTo(createdId.intValue()))
                .body("mealType", equalTo("LUNCH"))
                .body("customTitle", equalTo("Rester från gårdagen"))
                .body("servings", equalTo(2));
    }

    @Test
    @Order(5)
    void testCalculateShoppingList() {
        String startDate = LocalDate.now().minusDays(1).toString();
        String endDate = LocalDate.now().plusDays(7).toString();

        given()
                .queryParam("startDate", startDate)
                .queryParam("endDate", endDate)
                .when().get("/api/meal-plans/shopping-list")
                .then()
                .statusCode(200)
                .body(notNullValue());
    }

    @Test
    @Order(6)
    void testDeleteMealPlan() {
        given()
                .when().delete("/api/meal-plans/" + createdId)
                .then()
                .statusCode(204);

        given()
                .when().get("/api/meal-plans/" + createdId)
                .then()
                .statusCode(404);
    }

    @Test
    @Order(7)
    void testCreateMealPlanValidation_MissingDate() {
        String json = """
                {
                    "mealType": "DINNER",
                    "recipeId": 1
                }
                """;

        given()
                .contentType(ContentType.JSON)
                .body(json)
                .when().post("/api/meal-plans")
                .then()
                .statusCode(400);
    }

    @Test
    @Order(8)
    void testCreateMealPlanValidation_MissingMealType() {
        String today = LocalDate.now().toString();
        String json = """
                {
                    "planDate": "%s",
                    "recipeId": 1
                }
                """.formatted(today);

        given()
                .contentType(ContentType.JSON)
                .body(json)
                .when().post("/api/meal-plans")
                .then()
                .statusCode(400);
    }

    @Test
    @Order(9)
    void testCreateMealPlanValidation_MissingRecipeAndCustomTitle() {
        String today = LocalDate.now().toString();
        String json = """
                {
                    "planDate": "%s",
                    "mealType": "DINNER"
                }
                """.formatted(today);

        given()
                .contentType(ContentType.JSON)
                .body(json)
                .when().post("/api/meal-plans")
                .then()
                .statusCode(400);
    }

    @Test
    @Order(10)
    void testCreateMealPlanValidation_InvalidServings() {
        String today = LocalDate.now().toString();
        String json = """
                {
                    "planDate": "%s",
                    "mealType": "DINNER",
                    "recipeId": 1,
                    "servings": -2
                }
                """.formatted(today);

        given()
                .contentType(ContentType.JSON)
                .body(json)
                .when().post("/api/meal-plans")
                .then()
                .statusCode(400);
    }

    @Test
    @Order(11)
    void testCalculateShoppingListValidation_MissingDates() {
        given()
                .when().get("/api/meal-plans/shopping-list")
                .then()
                .statusCode(400);
    }

    @Test
    @Order(12)
    void testCalculateShoppingListValidation_InvalidDateRange() {
        String today = LocalDate.now().toString();
        String yesterday = LocalDate.now().minusDays(1).toString();

        given()
                .queryParam("startDate", today)
                .queryParam("endDate", yesterday)
                .when().get("/api/meal-plans/shopping-list")
                .then()
                .statusCode(400);
    }

    @Test
    @Order(13)
    void testCreateMealPlanWithCustomTitle() {
        String today = LocalDate.now().toString();
        String json = """
                {
                    "planDate": "%s",
                    "mealType": "LUNCH",
                    "customTitle": "Restfest och mackor",
                    "servings": 2,
                    "notes": "Ät upp rester"
                }
                """.formatted(today);

        Number id = given()
                .contentType(ContentType.JSON)
                .body(json)
                .when().post("/api/meal-plans")
                .then()
                .statusCode(201)
                .body("id", notNullValue())
                .body("customTitle", equalTo("Restfest och mackor"))
                .body("servings", equalTo(2))
                .extract().path("id");

        // Clean up
        given()
                .when().delete("/api/meal-plans/" + id.longValue())
                .then()
                .statusCode(204);
    }
}

