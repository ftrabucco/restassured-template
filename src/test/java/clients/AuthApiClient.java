package clients;

import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import models.User;

import java.util.HashMap;
import java.util.Map;

/**
 * API Client for Authentication operations
 * Handles JWT-based authentication, user registration, profile management
 */
public class AuthApiClient extends ApiClient {

    public AuthApiClient() {
        super();
    }

    public AuthApiClient withRequestSpec(RequestSpecification requestSpec) {
        this.requestSpec = requestSpec;
        return this;
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

        return requestSpec
                .body(registerPayload)
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

        return requestSpec
                .body(loginPayload)
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
        return requestSpec
                .header("Authorization", "Bearer " + jwtToken)
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

        return requestSpec
                .header("Authorization", "Bearer " + jwtToken)
                .body(updatePayload)
                .put("/api/auth/profile");
    }

    /**
     * Update user profile using User object (requires JWT token)
     * PUT /api/auth/profile
     */
    public Response updateUserProfile(String jwtToken, User user) {
        return updateUserProfile(jwtToken, user.getNombre(), user.getEmail());
    }

    /**
     * Change user password (requires JWT token)
     * POST /api/auth/change-password
     */
    public Response changePassword(String jwtToken, String currentPassword, String newPassword) {
        Map<String, Object> changePasswordPayload = new HashMap<>();
        changePasswordPayload.put("currentPassword", currentPassword);
        changePasswordPayload.put("newPassword", newPassword);

        return requestSpec
                .header("Authorization", "Bearer " + jwtToken)
                .body(changePasswordPayload)
                .post("/api/auth/change-password");
    }

    /**
     * Logout user (requires JWT token)
     * POST /api/auth/logout
     */
    public Response logoutUser(String jwtToken) {
        return requestSpec
                .header("Authorization", "Bearer " + jwtToken)
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