package clients;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import models.User;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;

/**
 * Standalone API Client for Authentication operations
 * Does not depend on BaseTest configuration
 */
public class AuthApiClientStandalone {

    private final String baseUrl;

    public AuthApiClientStandalone(String baseUrl) {
        this.baseUrl = baseUrl;
        RestAssured.baseURI = baseUrl;
    }

    /**
     * Register a new user
     * POST /api/auth/register
     */
    public Response registerUser(User user) {
        Map<String, Object> registerPayload = new HashMap<>();
        registerPayload.put("nombre", user.getNombre());
        registerPayload.put("email", user.getEmail());
        registerPayload.put("password", user.getPassword());

        return given()
                .header("Content-Type", "application/json")
                .body(registerPayload)
                .when()
                .post("/api/auth/register");
    }

    /**
     * Login user and get JWT token
     * POST /api/auth/login
     */
    public Response loginUser(String email, String password) {
        Map<String, Object> loginPayload = new HashMap<>();
        loginPayload.put("email", email);
        loginPayload.put("password", password);

        return given()
                .header("Content-Type", "application/json")
                .body(loginPayload)
                .when()
                .post("/api/auth/login");
    }

    /**
     * Login user using User object
     * POST /api/auth/login
     */
    public Response loginUser(User user) {
        return loginUser(user.getEmail(), user.getPassword());
    }

    /**
     * Get user profile (requires JWT token)
     * GET /api/auth/profile
     */
    public Response getUserProfile(String jwtToken) {
        return given()
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + jwtToken)
                .when()
                .get("/api/auth/profile");
    }

    /**
     * Update user profile (requires JWT token)
     * PUT /api/auth/profile
     */
    public Response updateUserProfile(String jwtToken, String nombre, String email) {
        Map<String, Object> updatePayload = new HashMap<>();
        if (nombre != null) {
            updatePayload.put("nombre", nombre);
        }
        if (email != null) {
            updatePayload.put("email", email);
        }

        return given()
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + jwtToken)
                .body(updatePayload)
                .when()
                .put("/api/auth/profile");
    }

    /**
     * Change user password (requires JWT token)
     * POST /api/auth/change-password
     */
    public Response changePassword(String jwtToken, String currentPassword, String newPassword) {
        Map<String, Object> changePasswordPayload = new HashMap<>();
        changePasswordPayload.put("currentPassword", currentPassword);
        changePasswordPayload.put("newPassword", newPassword);

        return given()
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + jwtToken)
                .body(changePasswordPayload)
                .when()
                .post("/api/auth/change-password");
    }

    /**
     * Logout user (requires JWT token)
     * POST /api/auth/logout
     */
    public Response logoutUser(String jwtToken) {
        return given()
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + jwtToken)
                .when()
                .post("/api/auth/logout");
    }

    /**
     * Helper method to extract JWT token from login response
     */
    public String extractJwtToken(Response loginResponse) {
        if (loginResponse.getStatusCode() == 200) {
            return loginResponse.jsonPath().getString("data.token");
        }
        return null;
    }

    /**
     * Helper method to extract user ID from response
     */
    public String extractUserId(Response response) {
        if (response.getStatusCode() == 200 || response.getStatusCode() == 201) {
            return response.jsonPath().getString("data.user.id");
        }
        return null;
    }

    /**
     * Convenience method for full login flow (register + login + get token)
     */
    public String registerAndLogin(User user) {
        // Register user
        Response registerResponse = registerUser(user);
        if (registerResponse.getStatusCode() != 201) {
            throw new RuntimeException("Failed to register user: " + registerResponse.getBody().asString());
        }

        // Login user
        Response loginResponse = loginUser(user);
        if (loginResponse.getStatusCode() != 200) {
            throw new RuntimeException("Failed to login user: " + loginResponse.getBody().asString());
        }

        return extractJwtToken(loginResponse);
    }
}