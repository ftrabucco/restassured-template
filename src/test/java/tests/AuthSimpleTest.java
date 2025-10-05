package tests;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * Simple Authentication test without BaseTest to verify basic functionality
 */
public class AuthSimpleTest {

    private String uniqueTimestamp;

    @BeforeEach
    void setup() {
        RestAssured.baseURI = "http://localhost:3030";
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();

        // Generate unique timestamp for this test run
        uniqueTimestamp = String.valueOf(System.currentTimeMillis());
    }

    @Test
    void shouldRegisterUserWithSimpleTest() {
        String uniqueEmail = "simple" + uniqueTimestamp + "@test.com";

        String requestBody = String.format("""
            {
                "nombre": "Simple Test User",
                "email": "%s",
                "password": "SimplePass123"
            }
            """, uniqueEmail);

        Response response = given()
                .header("Content-Type", "application/json")
                .body(requestBody)
                .when()
                .post("/api/auth/register")
                .then()
                .statusCode(201)
                .body("success", equalTo(true))
                .body("data.user.id", notNullValue())
                .body("data.user.nombre", equalTo("Simple Test User"))
                .body("data.user.email", equalTo(uniqueEmail))
                .extract()
                .response();

        System.out.println("✅ Registration response: " + response.getBody().asString());
    }

    @Test
    void shouldLoginUserWithSimpleTest() {
        String uniqueEmail = "login" + uniqueTimestamp + "@test.com";

        // First register a user
        String registerBody = String.format("""
            {
                "nombre": "Login Test User",
                "email": "%s",
                "password": "LoginPass123"
            }
            """, uniqueEmail);

        given()
                .header("Content-Type", "application/json")
                .body(registerBody)
                .when()
                .post("/api/auth/register")
                .then()
                .statusCode(201);

        // Then login
        String loginBody = String.format("""
            {
                "email": "%s",
                "password": "LoginPass123"
            }
            """, uniqueEmail);

        Response response = given()
                .header("Content-Type", "application/json")
                .body(loginBody)
                .when()
                .post("/api/auth/login")
                .then()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("data.token", notNullValue())
                .body("data.user.email", equalTo(uniqueEmail))
                .extract()
                .response();

        System.out.println("✅ Login response: " + response.getBody().asString());
    }
}