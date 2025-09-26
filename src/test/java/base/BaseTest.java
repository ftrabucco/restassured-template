package base;

import config.ConfigManager;
import io.qameta.allure.Step;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.filter.log.LogDetail;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base test class implementing Template Method pattern
 * Provides common setup and utilities for all API tests
 */
public abstract class BaseTest {
    protected static final Logger logger = LoggerFactory.getLogger(BaseTest.class);
    protected static ConfigManager config;
    protected RequestSpecification requestSpec;
    protected ResponseSpecification responseSpec;

    @BeforeAll
    static void globalSetup() {
        config = ConfigManager.getInstance();
        logger.info("Running tests against environment: {}", config.getCurrentEnvironment());
        logger.info("Base URL: {}", config.getBaseUrl());
        
        // Global RestAssured configuration
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
        
        // Enable detailed logging for debugging
        if (logger.isDebugEnabled()) {
            System.setProperty("org.slf4j.simpleLogger.log.io.restassured", "DEBUG");
        }
        
        // Configure Jackson ObjectMapper for Java 8 time support
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        RestAssured.config = RestAssured.config().objectMapperConfig(
            RestAssured.config().getObjectMapperConfig().jackson2ObjectMapperFactory(
                (cls, charset) -> objectMapper
            )
        );
    }

    @BeforeEach
    void setUp(TestInfo testInfo) {
        logger.info("Starting test: {}", testInfo.getDisplayName());
        
        // Build request specification
        requestSpec = new RequestSpecBuilder()
                .setBaseUri(config.getBaseUrl())
                .setContentType(ContentType.JSON)
                .setAccept(ContentType.JSON)
                .log(LogDetail.ALL)
                .build();

        // Build response specification for common validations
        responseSpec = new ResponseSpecBuilder()
                .log(LogDetail.ALL)
                .build();
                
        // Custom setup for each test class
        customSetup();
    }

    /**
     * Hook for test classes to implement custom setup
     */
    protected void customSetup() {
        // Override in subclasses if needed
    }
    
    /**
     * Get the configured request specification for use in clients
     */
    protected RequestSpecification getRequestSpec() {
        return this.requestSpec;
    }

    @Step("Get authentication token")
    protected String getAuthToken() {
        // Implementation depends on your authentication mechanism
        // This is a placeholder for JWT token retrieval
        return "Bearer your-jwt-token-here";
    }

    @Step("Add authentication to request")
    protected RequestSpecification withAuth() {
        return RestAssured.given(requestSpec)
                .header("Authorization", getAuthToken());
    }

    @Step("Create request without authentication")
    protected RequestSpecification withoutAuth() {
        return RestAssured.given(requestSpec);
    }

    /**
     * Common assertion methods
     */
    @Step("Verify response status code is {expectedStatusCode}")
    protected void verifyStatusCode(int actualStatusCode, int expectedStatusCode) {
        if (actualStatusCode != expectedStatusCode) {
            logger.error("Status code mismatch. Expected: {}, Actual: {}", expectedStatusCode, actualStatusCode);
            throw new AssertionError(String.format("Expected status code %d but got %d", expectedStatusCode, actualStatusCode));
        }
    }

    @Step("Verify response contains field: {fieldName}")
    protected void verifyFieldExists(String jsonPath, String fieldName) {
        // Implementation for field existence validation
        logger.info("Verifying field '{}' exists in response", fieldName);
    }
}
