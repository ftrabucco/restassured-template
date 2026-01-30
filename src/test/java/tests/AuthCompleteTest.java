package tests;

import base.BaseTest;
import clients.AuthApiClient;
import io.qameta.allure.*;
import io.restassured.response.Response;
import models.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import utils.ResponseValidator;
import utils.TestDataFactory;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive test suite for Authentication API
 * Covers all auth endpoints: register, login, profile, change-password
 * Complements AuthSimpleTest with more advanced scenarios
 * Based on MCP server documentation for auth endpoints
 */
@Feature("Authentication API - Complete")
@DisplayName("Authentication API Complete Tests")
public class AuthCompleteTest extends BaseTest {

    private AuthApiClient authClient;
    private String uniqueTimestamp;

    @BeforeEach
    void setupAuth() {
        authClient = new AuthApiClient().withRequestSpec(requestSpec);
        uniqueTimestamp = String.valueOf(System.currentTimeMillis());
    }

    // ========== REGISTRATION TESTS ==========

    @Test
    @Story("User Registration")
    @DisplayName("Should register user with valid data")
    @Severity(SeverityLevel.CRITICAL)
    void shouldRegisterUserWithValidData() {
        User user = TestDataFactory.createRandomUser();
        user.setEmail("register_" + uniqueTimestamp + "@test.com");

        Response response = authClient.registerUser(user);
        ResponseValidator.validateStatusCode(response, 201);

        response.then()
                .body("success", equalTo(true))
                .body("data.user.id", notNullValue())
                .body("data.user.nombre", equalTo(user.getNombre()))
                .body("data.user.email", equalTo(user.getEmail()))
                .body("data.user.password", nullValue()); // Password should not be returned

        // Note: Register endpoint does NOT return token, need to login to get token
    }

    @Test
    @Story("User Registration")
    @DisplayName("Should reject registration with weak password")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Password must be at least 6 chars with 1 uppercase, 1 lowercase, 1 number")
    void shouldRejectWeakPassword() {
        Map<String, Object> userData = new HashMap<>();
        userData.put("nombre", "Test User");
        userData.put("email", "weak_" + uniqueTimestamp + "@test.com");
        userData.put("password", "weak"); // Too weak

        // Need a helper method for this
        Response response = io.restassured.RestAssured.given(requestSpec)
                .body(userData)
                .when()
                .post("/api/auth/register");

        ResponseValidator.validateStatusCode(response, 400);

        response.then()
                .body("success", equalTo(false));
    }

    @Test
    @Story("User Registration")
    @DisplayName("Should reject registration with duplicate email")
    @Severity(SeverityLevel.CRITICAL)
    void shouldRejectDuplicateEmail() {
        String duplicateEmail = "duplicate_" + uniqueTimestamp + "@test.com";

        // Register first user
        User user1 = TestDataFactory.createRandomUser();
        user1.setEmail(duplicateEmail);
        authClient.registerUser(user1);

        // Try to register second user with same email
        User user2 = TestDataFactory.createRandomUser();
        user2.setEmail(duplicateEmail);
        Response response = authClient.registerUser(user2);

        ResponseValidator.validateStatusCode(response, 400);

        response.then()
                .body("success", equalTo(false));
    }

    @Test
    @Story("User Registration")
    @DisplayName("Should reject registration with invalid email format")
    @Severity(SeverityLevel.NORMAL)
    void shouldRejectInvalidEmailFormat() {
        Map<String, Object> userData = new HashMap<>();
        userData.put("nombre", "Test User");
        userData.put("email", "invalid-email-format"); // Invalid
        userData.put("password", "ValidPass123");

        Response response = io.restassured.RestAssured.given(requestSpec)
                .body(userData)
                .when()
                .post("/api/auth/register");

        ResponseValidator.validateStatusCode(response, 400);

        response.then()
                .body("success", equalTo(false));
    }

    @Test
    @Story("User Registration")
    @DisplayName("Should reject registration with missing required fields")
    @Severity(SeverityLevel.NORMAL)
    void shouldRejectMissingFields() {
        Map<String, Object> incompleteData = new HashMap<>();
        incompleteData.put("email", "incomplete_" + uniqueTimestamp + "@test.com");
        // Missing nombre and password

        Response response = io.restassured.RestAssured.given(requestSpec)
                .body(incompleteData)
                .when()
                .post("/api/auth/register");

        ResponseValidator.validateStatusCode(response, 400);

        response.then()
                .body("success", equalTo(false));
    }

    // ========== LOGIN TESTS ==========

    @Test
    @Story("User Login")
    @DisplayName("Should login with valid credentials")
    @Severity(SeverityLevel.CRITICAL)
    void shouldLoginWithValidCredentials() {
        // Register user first
        User user = TestDataFactory.createRandomUser();
        user.setEmail("login_" + uniqueTimestamp + "@test.com");
        authClient.registerUser(user);

        // Login
        Response response = authClient.loginUser(user.getEmail(), user.getPassword());
        ResponseValidator.validateStatusCode(response, 200);

        response.then()
                .body("success", equalTo(true))
                .body("data.token", notNullValue())
                .body("data.user.email", equalTo(user.getEmail()))
                .body("data.user.password", nullValue()); // Password should not be returned
    }

    @Test
    @Story("User Login")
    @DisplayName("Should reject login with wrong password")
    @Severity(SeverityLevel.CRITICAL)
    void shouldRejectWrongPassword() {
        // Register user first
        User user = TestDataFactory.createRandomUser();
        user.setEmail("wrongpass_" + uniqueTimestamp + "@test.com");
        authClient.registerUser(user);

        // Login with wrong password
        Response response = authClient.loginUser(user.getEmail(), "WrongPassword123");
        ResponseValidator.validateStatusCode(response, 401);

        response.then()
                .body("success", equalTo(false));
    }

    @Test
    @Story("User Login")
    @DisplayName("Should reject login with non-existent user")
    @Severity(SeverityLevel.NORMAL)
    void shouldRejectNonExistentUser() {
        Response response = authClient.loginUser("nonexistent_" + uniqueTimestamp + "@test.com", "Password123");
        ResponseValidator.validateStatusCode(response, 401);

        response.then()
                .body("success", equalTo(false));
    }

    @Test
    @Story("User Login")
    @DisplayName("Should reject login with missing credentials")
    @Severity(SeverityLevel.NORMAL)
    void shouldRejectMissingCredentials() {
        Map<String, Object> incompleteData = new HashMap<>();
        incompleteData.put("email", "test@test.com");
        // Missing password

        Response response = io.restassured.RestAssured.given(requestSpec)
                .body(incompleteData)
                .when()
                .post("/api/auth/login");

        ResponseValidator.validateStatusCode(response, 400);

        response.then()
                .body("success", equalTo(false));
    }

    // ========== PROFILE TESTS ==========

    @Test
    @Story("User Profile")
    @DisplayName("Should get profile with valid token")
    @Severity(SeverityLevel.CRITICAL)
    void shouldGetProfileWithValidToken() {
        // Register and login
        User user = TestDataFactory.createRandomUser();
        user.setEmail("profile_" + uniqueTimestamp + "@test.com");
        String token = authClient.registerAndLogin(user);

        // Get profile
        Response response = authClient.getUserProfile(token);
        ResponseValidator.validateStatusCode(response, 200);

        response.then()
                .body("success", equalTo(true))
                .body("data.user.email", equalTo(user.getEmail()))
                .body("data.user.nombre", equalTo(user.getNombre()))
                .body("data.user.password", nullValue());
    }

    @Test
    @Story("User Profile")
    @DisplayName("Should reject profile access without token")
    @Severity(SeverityLevel.CRITICAL)
    void shouldRejectProfileWithoutToken() {
        Response response = io.restassured.RestAssured.given(requestSpec)
                .when()
                .get("/api/auth/profile");

        ResponseValidator.validateStatusCode(response, 401);

        response.then()
                .body("success", equalTo(false))
                .body("message", equalTo("Token de acceso requerido"));
    }

    @Test
    @Story("User Profile")
    @DisplayName("Should reject profile access with invalid token")
    @Severity(SeverityLevel.NORMAL)
    void shouldRejectProfileWithInvalidToken() {
        Response response = io.restassured.RestAssured.given(requestSpec)
                .header("Authorization", "Bearer invalid_token_12345")
                .when()
                .get("/api/auth/profile");

        ResponseValidator.validateStatusCode(response, 401);

        response.then()
                .body("success", equalTo(false));
    }

    @Test
    @Story("User Profile")
    @DisplayName("Should update profile with valid data")
    @Severity(SeverityLevel.CRITICAL)
    void shouldUpdateProfileWithValidData() {
        // Register and login
        User user = TestDataFactory.createRandomUser();
        user.setEmail("update_" + uniqueTimestamp + "@test.com");
        String token = authClient.registerAndLogin(user);

        // Update profile
        String newNombre = "Updated Name";
        Response response = authClient.updateUserProfile(token, newNombre, null);
        ResponseValidator.validateStatusCode(response, 200);

        response.then()
                .body("success", equalTo(true))
                .body("data.user.nombre", equalTo(newNombre));
    }

    @Test
    @Story("User Profile")
    @DisplayName("Should reject profile update with duplicate email")
    @Severity(SeverityLevel.NORMAL)
    void shouldRejectProfileUpdateWithDuplicateEmail() {
        // Register two users
        User user1 = TestDataFactory.createRandomUser();
        user1.setEmail("user1_" + uniqueTimestamp + "@test.com");
        authClient.registerUser(user1);

        User user2 = TestDataFactory.createRandomUser();
        user2.setEmail("user2_" + uniqueTimestamp + "@test.com");
        String token2 = authClient.registerAndLogin(user2);

        // Try to update user2's email to user1's email
        Response response = authClient.updateUserProfile(token2, null, user1.getEmail());
        ResponseValidator.validateStatusCode(response, 400);

        response.then()
                .body("success", equalTo(false));
    }

    // ========== CHANGE PASSWORD TESTS ==========

    @Test
    @Story("Change Password")
    @DisplayName("Should change password with valid current password")
    @Severity(SeverityLevel.CRITICAL)
    void shouldChangePasswordSuccessfully() {
        // Register and login
        User user = TestDataFactory.createRandomUser();
        user.setEmail("changepass_" + uniqueTimestamp + "@test.com");
        String originalPassword = user.getPassword();
        String token = authClient.registerAndLogin(user);

        // Change password
        String newPassword = "NewPassword456";
        Response changeResponse = authClient.changePassword(token, originalPassword, newPassword);
        ResponseValidator.validateStatusCode(changeResponse, 200);

        changeResponse.then()
                .body("success", equalTo(true));

        // Verify can login with new password
        Response loginResponse = authClient.loginUser(user.getEmail(), newPassword);
        ResponseValidator.validateStatusCode(loginResponse, 200);

        loginResponse.then()
                .body("success", equalTo(true))
                .body("data.token", notNullValue());
    }

    @Test
    @Story("Change Password")
    @DisplayName("Should reject password change with wrong current password")
    @Severity(SeverityLevel.CRITICAL)
    void shouldRejectPasswordChangeWithWrongCurrentPassword() {
        // Register and login
        User user = TestDataFactory.createRandomUser();
        user.setEmail("wrongcurrent_" + uniqueTimestamp + "@test.com");
        String token = authClient.registerAndLogin(user);

        // Try to change password with wrong current password
        Response response = authClient.changePassword(token, "WrongCurrentPassword", "NewPassword456");
        ResponseValidator.validateStatusCode(response, 400);

        response.then()
                .body("success", equalTo(false));
    }

    @Test
    @Story("Change Password")
    @DisplayName("Should reject password change with weak new password")
    @Severity(SeverityLevel.NORMAL)
    void shouldRejectWeakNewPassword() {
        // Register and login
        User user = TestDataFactory.createRandomUser();
        user.setEmail("weaknew_" + uniqueTimestamp + "@test.com");
        String token = authClient.registerAndLogin(user);

        // Try to change to weak password
        Response response = authClient.changePassword(token, user.getPassword(), "weak");
        ResponseValidator.validateStatusCode(response, 400);

        response.then()
                .body("success", equalTo(false));
    }

    @Test
    @Story("Change Password")
    @DisplayName("Should reject password change without authentication")
    @Severity(SeverityLevel.NORMAL)
    void shouldRejectPasswordChangeWithoutAuth() {
        Map<String, Object> passwordData = new HashMap<>();
        passwordData.put("currentPassword", "Current123");
        passwordData.put("newPassword", "NewPassword456");

        Response response = io.restassured.RestAssured.given(requestSpec)
                .body(passwordData)
                .when()
                .post("/api/auth/change-password");

        ResponseValidator.validateStatusCode(response, 401);

        response.then()
                .body("success", equalTo(false))
                .body("message", equalTo("Token de acceso requerido"));
    }

    // ========== JWT TOKEN VALIDATION TESTS ==========

    @Test
    @Story("JWT Token Validation")
    @DisplayName("Should validate JWT token structure")
    @Severity(SeverityLevel.NORMAL)
    @Description("JWT token should follow the standard format: header.payload.signature")
    void shouldValidateJwtTokenStructure() {
        User user = TestDataFactory.createRandomUser();
        user.setEmail("jwtstructure_" + uniqueTimestamp + "@test.com");

        String token = authClient.registerAndLogin(user);

        assertNotNull(token, "Token should not be null");
        assertTrue(token.split("\\.").length == 3,
                "JWT should have 3 parts separated by dots (header.payload.signature)");
    }

    @Test
    @Story("JWT Token Validation")
    @DisplayName("Should reject malformed token")
    @Severity(SeverityLevel.NORMAL)
    void shouldRejectMalformedToken() {
        Response response = io.restassured.RestAssured.given(requestSpec)
                .header("Authorization", "Bearer malformed.token")
                .when()
                .get("/api/auth/profile");

        ResponseValidator.validateStatusCode(response, 401);

        response.then()
                .body("success", equalTo(false));
    }

    @Test
    @Story("JWT Token Validation")
    @DisplayName("Should accept token without Bearer prefix")
    @Severity(SeverityLevel.NORMAL)
    @Description("Backend accepts tokens with or without 'Bearer ' prefix")
    void shouldAcceptTokenWithoutBearerPrefix() {
        User user = TestDataFactory.createRandomUser();
        user.setEmail("nobearer_" + uniqueTimestamp + "@test.com");
        String token = authClient.registerAndLogin(user);

        // Send token without "Bearer " prefix - backend accepts this format
        Response response = io.restassured.RestAssured.given(requestSpec)
                .header("Authorization", token)
                .when()
                .get("/api/auth/profile");

        ResponseValidator.validateStatusCode(response, 200);

        response.then()
                .body("success", equalTo(true))
                .body("data.user.email", equalTo(user.getEmail()));
    }
}
