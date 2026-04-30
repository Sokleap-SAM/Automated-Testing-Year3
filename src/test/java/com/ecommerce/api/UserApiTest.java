package com.ecommerce.tests.api;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.Matchers.*;

/**
 * Data-driven tests for https://jsonplaceholder.typicode.com/users
 *
 * JSONPlaceholder is a free, no-auth public REST API.
 * Test data is provided inline via @DataProvider.
 */
public class UserApiTest extends BaseApiTest {

    // ------------------------------------------------------------------
    // DataProviders (inline – no external DB needed)
    // ------------------------------------------------------------------

    @DataProvider(name = "userCreateData")
    public Object[][] userCreateData() {
        return new Object[][]{
                {"Alice Smith",  "Software Engineer"},
                {"Bob Johnson",  "QA Engineer"},
                {"Carol White",  "Product Manager"},
                {"Dave Brown",   "DevOps Engineer"},
                {"Eve Davis",    "Data Scientist"}
        };
    }

    @DataProvider(name = "existingUserIds")
    public Object[][] existingUserIds() {
        // JSONPlaceholder has users with IDs 1-10
        return new Object[][]{{1}, {2}, {5}, {10}};
    }

    // ------------------------------------------------------------------
    // Tests
    // ------------------------------------------------------------------

    @Test(description = "GET /users returns list of 10 users matching schema")
    public void testGetAllUsers() {
        given()
            .spec(requestSpec)
        .when()
            .get("/users")
        .then()
            .statusCode(200)
            .body(matchesJsonSchemaInClasspath("schemas/users-list-schema.json"))
            .body("$",       hasSize(10))
            .body("[0].id",  notNullValue())
            .body("[0].name", notNullValue());
    }

    @Test(description = "GET /users/{id} returns single user matching schema",
          dataProvider = "existingUserIds")
    public void testGetSingleUser(int userId) {
        given()
            .spec(requestSpec)
        .when()
            .get("/users/" + userId)
        .then()
            .statusCode(200)
            .body(matchesJsonSchemaInClasspath("schemas/user-schema.json"))
            .body("id",       equalTo(userId))
            .body("name",     notNullValue())
            .body("username", notNullValue())
            .body("email",    notNullValue());
    }

    @Test(description = "GET /users/{id} returns 404 for unknown user")
    public void testGetNonExistentUser() {
        given()
            .spec(requestSpec)
        .when()
            .get("/users/9999")
        .then()
            .statusCode(404);
    }

    @Test(description = "POST /users creates user – data driven",
          dataProvider = "userCreateData")
    public void testCreateUser(String name, String job) {
        Map<String, String> payload = new HashMap<>();
        payload.put("name", name);
        payload.put("job",  job);

        given()
            .spec(requestSpec)
            .body(payload)
        .when()
            .post("/users")
        .then()
            .statusCode(201)
            .body(matchesJsonSchemaInClasspath("schemas/create-user-schema.json"))
            .body("name", equalTo(name))
            .body("job",  equalTo(job))
            .body("id",   notNullValue());
    }

    @Test(description = "PUT /users/{id} updates user and returns updated fields")
    public void testUpdateUser() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("id",       1);
        payload.put("name",     "Leanne Graham Updated");
        payload.put("username", "Bret");
        payload.put("email",    "leanne.updated@example.com");

        given()
            .spec(requestSpec)
            .body(payload)
        .when()
            .put("/users/1")
        .then()
            .statusCode(200)
            .body("name", equalTo("Leanne Graham Updated"));
    }

    @Test(description = "PATCH /users/{id} partially updates user")
    public void testPatchUser() {
        Map<String, String> payload = new HashMap<>();
        payload.put("name", "Leanne Patched");

        given()
            .spec(requestSpec)
            .body(payload)
        .when()
            .patch("/users/1")
        .then()
            .statusCode(200)
            .body("name", equalTo("Leanne Patched"));
    }

    @Test(description = "DELETE /users/{id} returns 200")
    public void testDeleteUser() {
        given()
            .spec(requestSpec)
        .when()
            .delete("/users/1")
        .then()
            .statusCode(200);
    }
}


/**
 * Data-driven tests covering our own Spring Boot /api/users endpoint.
 *
 * Test data is read from the SQLite database seeded by DatabaseUtil.
 * Schema validation is performed on every response that returns a body.
 * The server is started/stopped by SpringBootSuiteListener.
 */
