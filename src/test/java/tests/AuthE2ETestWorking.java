package tests;

import clients.AuthApiClientStandalone;
import io.qameta.allure.Description;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import io.restassured.response.Response;
import models.User;
import org.junit.jupiter.api.*;
import utils.ResponseValidator;
import utils.TestDataFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Working End-to-End test for Authentication flow using standalone client
 * Tests complete user lifecycle: REGISTER → LOGIN → PROFILE → UPDATE → CHANGE_PASSWORD → LOGOUT
 */
@Feature("Authentication E2E Working")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AuthE2ETestWorking {
    private static AuthApiClientStandalone authClient;
    private static User testUser;
    private static String jwtToken;
    private static String userId;

    // Track created users for cleanup
    private static final List<String> createdUserIds = new ArrayList<>();

    @BeforeAll
    static void setupClass() {
        authClient = new AuthApiClientStandalone("http://localhost:3030");
        testUser = TestDataFactory.createUserForRegistration();
        System.out.println("🔐 Starting Auth E2E test with user: " + testUser.getEmail());
    }

    @Test
    @Order(1)
    @Story("Complete Authentication Flow")
    @DisplayName("Step 1: Should register new user successfully")
    @Description("Register a new user and verify the response contains user data without password")
    void step01_shouldRegisterUserSuccessfully() {
        System.out.println("=== AUTH E2E STEP 1: REGISTER ===");

        // Act
        Response response = authClient.registerUser(testUser);

        // Assert
        ResponseValidator.validateStatusCode(response, 201);
        ResponseValidator.validateFieldExists(response, "data.user.id");
        ResponseValidator.validateStringFieldValue(response, "data.user.nombre", testUser.getNombre());
        ResponseValidator.validateStringFieldValue(response, "data.user.email", testUser.getEmail());
        ResponseValidator.validateFieldNotExists(response, "data.user.password");

        // Track user for cleanup
        userId = response.jsonPath().getString("data.user.id");
        createdUserIds.add(userId);

        System.out.println("✅ User registered successfully with ID: " + userId);
        System.out.println("Response: " + response.getBody().asString());
        System.out.println("==================================");
    }

    @Test
    @Order(2)
    @Story("Complete Authentication Flow")
    @DisplayName("Step 2: Should login with registered credentials")
    @Description("Login with the registered user credentials and receive JWT token")
    void step02_shouldLoginSuccessfully() {
        System.out.println("=== AUTH E2E STEP 2: LOGIN ===");

        // Act
        Response response = authClient.loginUser(testUser);

        // Assert
        ResponseValidator.validateStatusCode(response, 200);
        ResponseValidator.validateFieldExists(response, "data.token");
        ResponseValidator.validateFieldExists(response, "data.user.id");
        ResponseValidator.validateStringFieldValue(response, "data.user.email", testUser.getEmail());
        ResponseValidator.validateFieldNotExists(response, "data.user.password");

        // Extract JWT token for subsequent requests
        jwtToken = authClient.extractJwtToken(response);
        Assertions.assertNotNull(jwtToken, "JWT token should not be null");

        System.out.println("✅ Login successful, JWT token received");
        System.out.println("Token preview: " + jwtToken.substring(0, Math.min(20, jwtToken.length())) + "...");
        System.out.println("===============================");
    }

    @Test
    @Order(3)
    @Story("Complete Authentication Flow")
    @DisplayName("Step 3: Should retrieve user profile with JWT token")
    @Description("Get user profile using the JWT token from login")
    void step03_shouldGetUserProfileSuccessfully() {
        System.out.println("=== AUTH E2E STEP 3: GET PROFILE ===");

        // Act
        Response response = authClient.getUserProfile(jwtToken);

        // Assert
        ResponseValidator.validateStatusCode(response, 200);
        ResponseValidator.validateFieldExists(response, "data.user.id");
        ResponseValidator.validateStringFieldValue(response, "data.user.email", testUser.getEmail());
        ResponseValidator.validateStringFieldValue(response, "data.user.nombre", testUser.getNombre());
        ResponseValidator.validateFieldNotExists(response, "data.user.password");

        System.out.println("✅ Profile retrieved successfully");
        System.out.println("Response: " + response.getBody().asString());
        System.out.println("=====================================");
    }

    @Test
    @Order(4)
    @Story("Complete Authentication Flow")
    @DisplayName("Step 4: Should update user profile successfully")
    @Description("Update user profile information (name and email)")
    void step04_shouldUpdateUserProfileSuccessfully() {
        System.out.println("=== AUTH E2E STEP 4: UPDATE PROFILE ===");

        String updatedNombre = "Updated " + testUser.getNombre();
        String updatedEmail = "updated." + testUser.getEmail();

        // Act
        Response response = authClient.updateUserProfile(jwtToken, updatedNombre, updatedEmail);

        // Assert
        ResponseValidator.validateStatusCode(response, 200);
        ResponseValidator.validateStringFieldValue(response, "data.user.nombre", updatedNombre);
        ResponseValidator.validateStringFieldValue(response, "data.user.email", updatedEmail);

        // Update test user data for subsequent tests
        testUser.setNombre(updatedNombre);
        testUser.setEmail(updatedEmail);

        System.out.println("✅ Profile updated successfully");
        System.out.println("New name: " + updatedNombre);
        System.out.println("New email: " + updatedEmail);
        System.out.println("Response: " + response.getBody().asString());
        System.out.println("========================================");
    }

    @Test
    @Order(5)
    @Story("Complete Authentication Flow")
    @DisplayName("Step 5: Should change password successfully")
    @Description("Change user password and verify login works with new password")
    void step05_shouldChangePasswordSuccessfully() {
        System.out.println("=== AUTH E2E STEP 5: CHANGE PASSWORD ===");

        String currentPassword = testUser.getPassword();
        String newPassword = "NewPassword" + System.currentTimeMillis() % 1000;

        // Act
        Response response = authClient.changePassword(jwtToken, currentPassword, newPassword);

        // Assert
        ResponseValidator.validateStatusCode(response, 200);

        // Verify login works with new password
        Response loginResponse = authClient.loginUser(testUser.getEmail(), newPassword);
        ResponseValidator.validateStatusCode(loginResponse, 200);
        ResponseValidator.validateFieldExists(loginResponse, "data.token");

        // Update test user password
        testUser.setPassword(newPassword);

        System.out.println("✅ Password changed successfully");
        System.out.println("Login with new password verified");
        System.out.println("=========================================");
    }

    @Test
    @Order(6)
    @Story("Complete Authentication Flow")
    @DisplayName("Step 6: Should logout successfully")
    @Description("Logout user and invalidate JWT token")
    void step06_shouldLogoutSuccessfully() {
        System.out.println("=== AUTH E2E STEP 6: LOGOUT ===");

        // Act
        Response response = authClient.logoutUser(jwtToken);

        // Assert
        ResponseValidator.validateStatusCode(response, 200);

        System.out.println("✅ Logout successful");
        System.out.println("Authentication E2E flow completed successfully! 🎉");
        System.out.println("================================");
    }

    @Test
    @Order(7)
    @Story("Negative Testing")
    @DisplayName("Step 7: Should handle duplicate email registration")
    @Description("Verify that attempting to register with existing email fails")
    void step07_shouldHandleDuplicateEmailRegistration() {
        System.out.println("=== AUTH E2E STEP 7: DUPLICATE EMAIL TEST ===");

        // Try to register with same email
        User duplicateUser = TestDataFactory.createUserWithSpecificData(
                "Another User",
                testUser.getEmail(), // Same email
                "AnotherPassword123"
        );

        // Act
        Response response = authClient.registerUser(duplicateUser);

        // Assert
        ResponseValidator.validateStatusCode(response, 400);
        ResponseValidator.validateFieldExists(response, "message");

        System.out.println("✅ Duplicate email registration properly rejected");
        System.out.println("===============================================");
    }

    @Test
    @Order(8)
    @Story("Negative Testing")
    @DisplayName("Step 8: Should handle invalid token access")
    @Description("Verify that invalid JWT token is properly rejected")
    void step08_shouldHandleInvalidTokenAccess() {
        System.out.println("=== AUTH E2E STEP 8: INVALID TOKEN TEST ===");

        String invalidToken = "invalid.jwt.token";

        // Act
        Response response = authClient.getUserProfile(invalidToken);

        // Assert
        ResponseValidator.validateStatusCode(response, 401);

        System.out.println("✅ Invalid token properly rejected");
        System.out.println("============================================");
    }

    @AfterAll
    static void cleanupCreatedUsers() {
        System.out.println("🔐 Auth E2E cleanup: " + createdUserIds.size() + " users were created during E2E tests");
        // Note: In real scenario, you might want to call admin cleanup endpoint
        // or use database cleanup script
        for (String userId : createdUserIds) {
            System.out.println("📝 User created during E2E test: " + userId);
        }
        createdUserIds.clear();
    }
}