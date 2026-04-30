package com.ecommerce.tests.api;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.Matchers.*;

/**
 * Data-driven tests for https://jsonplaceholder.typicode.com/posts
 *
 * Covers GET list, GET single, GET filtered, POST (data-driven), and DELETE.
 */
public class PostApiTest extends BaseApiTest {

    // ------------------------------------------------------------------
    // DataProviders (inline)
    // ------------------------------------------------------------------

    @DataProvider(name = "postCreateData")
    public Object[][] postCreateData() {
        return new Object[][]{
                {"Introduction to REST APIs",     "REST stands for Representational State Transfer.", 1},
                {"Data-Driven Testing Guide",     "Use data providers to drive test inputs.",          2},
                {"API Schema Validation Tips",    "Always validate response bodies against a schema.", 1},
        };
    }

    @DataProvider(name = "postIdProvider")
    public Object[][] postIdProvider() {
        return new Object[][]{{1}, {25}, {50}, {100}};
    }

    // ------------------------------------------------------------------
    // Tests
    // ------------------------------------------------------------------

    @Test(description = "GET /posts returns list of 100 posts")
    public void testGetAllPosts() {
        given()
            .spec(requestSpec)
        .when()
            .get("/posts")
        .then()
            .statusCode(200)
            .body("$",       hasSize(100))
            .body("[0].id",  notNullValue())
            .body("[0].title", notNullValue());
    }

    @Test(description = "GET /posts/{id} returns single post matching schema",
          dataProvider = "postIdProvider")
    public void testGetSinglePost(int postId) {
        given()
            .spec(requestSpec)
        .when()
            .get("/posts/" + postId)
        .then()
            .statusCode(200)
            .body(matchesJsonSchemaInClasspath("schemas/post-schema.json"))
            .body("id",     equalTo(postId))
            .body("userId", notNullValue())
            .body("title",  notNullValue())
            .body("body",   notNullValue());
    }

    @Test(description = "GET /posts?userId=1 returns posts filtered by user")
    public void testGetPostsByUser() {
        given()
            .spec(requestSpec)
            .queryParam("userId", 1)
        .when()
            .get("/posts")
        .then()
            .statusCode(200)
            .body("$",            not(empty()))
            .body("[0].userId",   equalTo(1));
    }

    @Test(description = "GET /posts/{id} returns 404 for unknown post")
    public void testGetNonExistentPost() {
        given()
            .spec(requestSpec)
        .when()
            .get("/posts/9999")
        .then()
            .statusCode(404);
    }

    @Test(description = "POST /posts creates post – data driven",
          dataProvider = "postCreateData")
    public void testCreatePost(String title, String body, int userId) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("title",  title);
        payload.put("body",   body);
        payload.put("userId", userId);

        given()
            .spec(requestSpec)
            .body(payload)
        .when()
            .post("/posts")
        .then()
            .statusCode(201)
            .body("title",  equalTo(title))
            .body("body",   equalTo(body))
            .body("userId", equalTo(userId))
            .body("id",     notNullValue());
    }

    @Test(description = "DELETE /posts/{id} returns 200")
    public void testDeletePost() {
        given()
            .spec(requestSpec)
        .when()
            .delete("/posts/1")
        .then()
            .statusCode(200);
    }
}
