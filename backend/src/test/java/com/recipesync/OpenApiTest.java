package com.recipesync;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.startsWith;

@QuarkusTest
class OpenApiTest {

    @Test
    void testOpenApiEndpointReturnsOAS3Json() {
        given()
                .accept(ContentType.JSON)
                .when().get("/openapi")
                .then()
                .statusCode(200)
                .body("openapi", startsWith("3."))
                .body("info.title", is("Recipe & Pantry Sync API"))
                .body("paths.'/api/recipes'", org.hamcrest.Matchers.notNullValue());
    }

    @Test
    void testSwaggerUiIsAvailable() {
        given()
                .when().get("/swagger-ui")
                .then()
                .statusCode(200)
                .contentType(containsString("html"));
    }
}
