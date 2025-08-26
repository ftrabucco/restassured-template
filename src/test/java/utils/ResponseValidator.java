package utils;

import io.qameta.allure.Step;
import io.restassured.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * Utility class for common response validations
 * Implements Strategy pattern for different validation approaches
 */
public class ResponseValidator {
    private static final Logger logger = LoggerFactory.getLogger(ResponseValidator.class);

    @Step("Validate status code is {expectedStatusCode}")
    public static void validateStatusCode(Response response, int expectedStatusCode) {
        int actualStatusCode = response.getStatusCode();
        logger.info("Validating status code. Expected: {}, Actual: {}", expectedStatusCode, actualStatusCode);
        assertThat("Status code validation failed", actualStatusCode, equalTo(expectedStatusCode));
    }

    @Step("Validate response time is less than {maxResponseTime}ms")
    public static void validateResponseTime(Response response, long maxResponseTime) {
        long actualResponseTime = response.getTime();
        logger.info("Validating response time. Expected: <{}ms, Actual: {}ms", maxResponseTime, actualResponseTime);
        assertThat("Response time validation failed", actualResponseTime, lessThan(maxResponseTime));
    }

    @Step("Validate field {fieldPath} exists in response")
    public static void validateFieldExists(Response response, String fieldPath) {
        logger.info("Validating field '{}' exists in response", fieldPath);
        assertThat("Field does not exist: " + fieldPath, response.jsonPath().get(fieldPath), notNullValue());
    }

    @Step("Validate field {fieldPath} equals {expectedValue}")
    public static void validateFieldValue(Response response, String fieldPath, Object expectedValue) {
        Object actualValue = response.jsonPath().get(fieldPath);
        logger.info("Validating field '{}'. Expected: {}, Actual: {}", fieldPath, expectedValue, actualValue);
        assertThat("Field value validation failed for: " + fieldPath, actualValue, equalTo(expectedValue));
    }

    @Step("Validate field {fieldPath} contains {expectedValue}")
    public static void validateFieldContains(Response response, String fieldPath, String expectedValue) {
        String actualValue = response.jsonPath().getString(fieldPath);
        logger.info("Validating field '{}' contains '{}'", fieldPath, expectedValue);
        assertThat("Field does not contain expected value", actualValue, containsString(expectedValue));
    }

    @Step("Validate array field {fieldPath} has size {expectedSize}")
    public static void validateArraySize(Response response, String fieldPath, int expectedSize) {
        List<?> actualArray = response.jsonPath().getList(fieldPath);
        int actualSize = actualArray != null ? actualArray.size() : 0;
        logger.info("Validating array '{}' size. Expected: {}, Actual: {}", fieldPath, expectedSize, actualSize);
        assertThat("Array size validation failed for: " + fieldPath, actualSize, equalTo(expectedSize));
    }

    @Step("Validate array field {fieldPath} is not empty")
    public static void validateArrayNotEmpty(Response response, String fieldPath) {
        List<?> actualArray = response.jsonPath().getList(fieldPath);
        logger.info("Validating array '{}' is not empty", fieldPath);
        assertThat("Array should not be empty: " + fieldPath, actualArray, not(empty()));
    }

    @Step("Validate response contains required headers")
    public static void validateRequiredHeaders(Response response, String... headerNames) {
        for (String headerName : headerNames) {
            logger.info("Validating header '{}' exists", headerName);
            assertThat("Required header missing: " + headerName, 
                      response.getHeader(headerName), notNullValue());
        }
    }

    @Step("Validate content type is {expectedContentType}")
    public static void validateContentType(Response response, String expectedContentType) {
        String actualContentType = response.getContentType();
        logger.info("Validating content type. Expected: {}, Actual: {}", expectedContentType, actualContentType);
        assertThat("Content type validation failed", actualContentType, containsString(expectedContentType));
    }

    @Step("Validate response body is not empty")
    public static void validateResponseBodyNotEmpty(Response response) {
        String responseBody = response.getBody().asString();
        logger.info("Validating response body is not empty");
        assertThat("Response body should not be empty", responseBody, not(isEmptyString()));
    }

    @Step("Validate numeric field {fieldPath} is positive")
    public static void validatePositiveNumber(Response response, String fieldPath) {
        Number actualValue = response.jsonPath().get(fieldPath);
        logger.info("Validating field '{}' is positive. Value: {}", fieldPath, actualValue);
        assertThat("Field should be positive: " + fieldPath, actualValue.doubleValue(), greaterThan(0.0));
    }

    @Step("Validate date field {fieldPath} format")
    public static void validateDateFormat(Response response, String fieldPath) {
        String dateValue = response.jsonPath().getString(fieldPath);
        logger.info("Validating date field '{}' format. Value: {}", fieldPath, dateValue);
        assertThat("Date field should not be null: " + fieldPath, dateValue, notNullValue());
        // Add more specific date format validation as needed
    }
}
