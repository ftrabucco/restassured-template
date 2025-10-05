package utils;

import clients.AuthApiClientStandalone;
import models.User;
import io.restassured.response.Response;

/**
 * Centralized authentication helper for API tests
 * Manages JWT tokens and provides consistent test credentials
 */
public class AuthenticationHelper {

    private static final String TEST_USER_NAME = "API Test User";
    private static final String TEST_USER_EMAIL = "apitest@restassured.com";
    private static final String TEST_USER_PASSWORD = "APITest123!";

    private static AuthenticationHelper instance;
    private static String cachedJwtToken;
    private static User testUser;
    private static AuthApiClientStandalone authClient;

    // Private constructor for singleton
    private AuthenticationHelper() {
        authClient = new AuthApiClientStandalone("http://localhost:3030");
        testUser = TestDataFactory.createUserWithSpecificData(
            TEST_USER_NAME,
            TEST_USER_EMAIL,
            TEST_USER_PASSWORD
        );
    }

    public static synchronized AuthenticationHelper getInstance() {
        if (instance == null) {
            instance = new AuthenticationHelper();
        }
        return instance;
    }

    /**
     * Get JWT token for API tests. Creates test user if needed.
     * Uses caching to avoid multiple registration/login calls.
     */
    public String getJwtToken() {
        if (cachedJwtToken != null && isTokenValid(cachedJwtToken)) {
            return cachedJwtToken;
        }

        // Try to login first (user might already exist)
        Response loginResponse = authClient.loginUser(testUser);

        if (loginResponse.getStatusCode() == 200) {
            // User exists, login successful
            cachedJwtToken = authClient.extractJwtToken(loginResponse);
            System.out.println("🔐 Using existing test user for authentication");
        } else if (loginResponse.getStatusCode() == 401) {
            // User doesn't exist, register first
            System.out.println("🔐 Registering new test user for API tests");
            Response registerResponse = authClient.registerUser(testUser);

            if (registerResponse.getStatusCode() != 201) {
                throw new RuntimeException("Failed to register test user: " +
                    registerResponse.getBody().asString());
            }

            // Now login
            loginResponse = authClient.loginUser(testUser);
            if (loginResponse.getStatusCode() != 200) {
                throw new RuntimeException("Failed to login test user: " +
                    loginResponse.getBody().asString());
            }

            cachedJwtToken = authClient.extractJwtToken(loginResponse);
        } else {
            throw new RuntimeException("Unexpected login response: " +
                loginResponse.getStatusCode() + " - " + loginResponse.getBody().asString());
        }

        if (cachedJwtToken == null) {
            throw new RuntimeException("Failed to extract JWT token from login response");
        }

        System.out.println("✅ JWT token obtained for API tests");
        return cachedJwtToken;
    }

    /**
     * Get test user credentials
     */
    public User getTestUser() {
        return testUser;
    }

    /**
     * Get invalid token for security testing
     */
    public String getInvalidToken() {
        return "invalid.jwt.token.for.testing";
    }

    /**
     * Get malformed token for security testing
     */
    public String getMalformedToken() {
        return "malformed-token-missing-dots";
    }

    /**
     * Get expired token for security testing (this is a mock expired token)
     */
    public String getExpiredToken() {
        return "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpZCI6OTk5LCJlbWFpbCI6ImV4cGlyZWRAZXhhbXBsZS5jb20iLCJleHAiOjE2MzI0ODc2MDB9.expired";
    }

    /**
     * Clear cached token (useful for testing token refresh scenarios)
     */
    public void clearCachedToken() {
        cachedJwtToken = null;
        System.out.println("🔄 Cleared cached JWT token");
    }

    /**
     * Basic token validation (checks if token is not null and has proper format)
     */
    private boolean isTokenValid(String token) {
        if (token == null || token.trim().isEmpty()) {
            return false;
        }

        // JWT tokens have 3 parts separated by dots
        String[] parts = token.split("\\.");
        if (parts.length != 3) {
            return false;
        }

        // You could add more validation here (check expiration, etc.)
        // For now, we assume the token is valid if it has the right format
        return true;
    }

    /**
     * Create Authorization header value with Bearer token
     */
    public String createBearerHeader(String token) {
        return "Bearer " + token;
    }

    /**
     * Create Authorization header with valid token
     */
    public String createValidAuthHeader() {
        return createBearerHeader(getJwtToken());
    }

    /**
     * Get test credentials for manual testing
     */
    public static class TestCredentials {
        public static final String EMAIL = TEST_USER_EMAIL;
        public static final String PASSWORD = TEST_USER_PASSWORD;
        public static final String NAME = TEST_USER_NAME;
    }
}