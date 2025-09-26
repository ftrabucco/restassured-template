package utils;

import io.qameta.allure.Allure;
import io.qameta.allure.Attachment;
import io.restassured.response.Response;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

/**
 * Utility class for attaching request/response details to Allure reports
 */
public class AllureLogger {

    @Attachment(value = "Request Body", type = "application/json")
    public static String attachRequestBody(String requestBody) {
        return requestBody != null ? requestBody : "No request body";
    }

    @Attachment(value = "Response Body", type = "application/json")
    public static String attachResponseBody(String responseBody) {
        return responseBody != null ? responseBody : "No response body";
    }

    @Attachment(value = "Request Headers", type = "text/plain")
    public static String attachRequestHeaders(String headers) {
        return headers != null ? headers : "No headers";
    }

    @Attachment(value = "Response Headers", type = "text/plain")
    public static String attachResponseHeaders(String headers) {
        return headers != null ? headers : "No headers";
    }

    @Attachment(value = "Error Details", type = "text/plain")
    public static String attachErrorDetails(String errorDetails) {
        return errorDetails;
    }

    // Alternative method using Allure.addAttachment
    public static void addRequestBodyAttachment(String requestBody) {
        if (requestBody != null) {
            Allure.addAttachment("Request Body", "application/json", 
                new ByteArrayInputStream(requestBody.getBytes(StandardCharsets.UTF_8)), ".json");
        }
    }

    public static void addResponseBodyAttachment(String responseBody) {
        if (responseBody != null) {
            Allure.addAttachment("Response Body", "application/json", 
                new ByteArrayInputStream(responseBody.getBytes(StandardCharsets.UTF_8)), ".json");
        }
    }

    public static void addErrorDetailsAttachment(String errorDetails) {
        if (errorDetails != null) {
            Allure.addAttachment("Error Details", "text/plain", 
                new ByteArrayInputStream(errorDetails.getBytes(StandardCharsets.UTF_8)), ".txt");
        }
    }

    public static void attachResponse(Response response) {
        String responseBody = response.getBody().asString();
        String responseHeaders = response.getHeaders().toString();
        
        // Use only one method to avoid duplicates
        addResponseBodyAttachment(responseBody);
        attachResponseHeaders(responseHeaders);
        
        // If error status, attach error details
        if (response.getStatusCode() >= 400) {
            String errorDetails = String.format(
                "Status Code: %d\nResponse Time: %dms\nError Body: %s",
                response.getStatusCode(),
                response.getTime(),
                responseBody
            );
            addErrorDetailsAttachment(errorDetails);
        }
    }
    
    public static void addJsonAttachment(String name, String jsonContent) {
        if (jsonContent != null) {
            Allure.addAttachment(name, "application/json", 
                new ByteArrayInputStream(jsonContent.getBytes(StandardCharsets.UTF_8)), ".json");
        }
    }
}
