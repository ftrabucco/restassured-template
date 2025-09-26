package utils;

import io.qameta.allure.Allure;
import io.qameta.allure.Step;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

/**
 * Utility class for adding attachments to Allure reports
 */
public class AllureAttachments {

    @Step("Attach request URL and parameters")
    public static void attachRequestUrl(String method, String url) {
        Allure.addAttachment("🌐 Request URL", "text/plain", method + " " + url);
    }

    @Step("Attach complete request details")
    public static void attachCompleteRequestDetails(String method, String baseUrl, String endpoint, Object body) {
        StringBuilder requestDetails = new StringBuilder();
        
        // Add method and full URL
        String fullUrl = baseUrl + endpoint;
        requestDetails.append("Method: ").append(method.toUpperCase()).append("\n");
        requestDetails.append("Full URL: ").append(fullUrl).append("\n");
        requestDetails.append("Base URL: ").append(baseUrl).append("\n");
        requestDetails.append("Endpoint: ").append(endpoint).append("\n");
        
        Allure.addAttachment("📋 Request Details", "text/plain", requestDetails.toString());
        
        // Attach request body separately as JSON if it exists
        if (body != null) {
            attachRequestBody(body);
        }
    }

    @Step("Attach request with path variables")
    public static void attachRequestWithPathVariables(String method, String baseUrl, String endpoint, 
                                                     String pathVariable, Object body) {
        StringBuilder requestDetails = new StringBuilder();
        
        String fullUrl = baseUrl + endpoint;
        if (pathVariable != null && !pathVariable.isEmpty()) {
            if (!endpoint.endsWith("/")) {
                fullUrl += "/";
            }
            fullUrl += pathVariable;
        }
        
        requestDetails.append("Method: ").append(method.toUpperCase()).append("\n");
        requestDetails.append("Full URL: ").append(fullUrl).append("\n");
        requestDetails.append("Base URL: ").append(baseUrl).append("\n");
        requestDetails.append("Endpoint: ").append(endpoint).append("\n");
        
        if (pathVariable != null && !pathVariable.isEmpty()) {
            requestDetails.append("Path Variable: ").append(pathVariable).append("\n");
        }
        
        Allure.addAttachment("📋 Request Details", "text/plain", requestDetails.toString());
        
        // Attach request body separately as JSON if it exists
        if (body != null) {
            attachRequestBody(body);
        }
    }

    @Step("Attach request with query parameters")
    public static void attachRequestWithQueryParams(String method, String baseUrl, String endpoint, 
                                                   String queryParams, Object body) {
        StringBuilder requestDetails = new StringBuilder();
        
        String fullUrl = baseUrl + endpoint;
        if (queryParams != null && !queryParams.isEmpty()) {
            fullUrl += "?" + queryParams;
        }
        
        requestDetails.append("Method: ").append(method.toUpperCase()).append("\n");
        requestDetails.append("Full URL: ").append(fullUrl).append("\n");
        requestDetails.append("Base URL: ").append(baseUrl).append("\n");
        requestDetails.append("Endpoint: ").append(endpoint).append("\n");
        
        if (queryParams != null && !queryParams.isEmpty()) {
            requestDetails.append("Query Parameters: ").append(queryParams).append("\n");
        }
        
        Allure.addAttachment("📋 Request Details", "text/plain", requestDetails.toString());
        
        // Attach request body separately as JSON if it exists
        if (body != null) {
            attachRequestBody(body);
        }
    }

    @Step("Attach response details")
    public static void attachResponseDetails(Response response) {
        Allure.addAttachment("Response Status", "text/plain", 
            "Status Code: " + response.getStatusCode() + "\n" +
            "Status Line: " + response.getStatusLine() + "\n" +
            "Response Time: " + response.getTime() + "ms");
    }

    @Step("Attach request body")
    public static void attachRequestBody(Object body) {
        if (body != null) {
            try {
                // Try to serialize as JSON if it's a Java object
                String jsonBody;
                if (body instanceof String) {
                    jsonBody = (String) body;
                } else {
                    // Use Jackson ObjectMapper to serialize to JSON
                    com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                    jsonBody = mapper.writeValueAsString(body);
                }
                Allure.addAttachment("📤 Request Body", "application/json", jsonBody);
            } catch (Exception e) {
                // Fallback to toString if JSON serialization fails
                Allure.addAttachment("📤 Request Body", "text/plain", body.toString());
            }
        }
    }

    @Step("Attach response body")
    public static void attachResponseBody(Response response) {
        String responseBody = response.getBody().asString();
        if (responseBody != null && !responseBody.isEmpty()) {
            Allure.addAttachment("Response Body", "application/json", responseBody);
        }
    }

    @Step("Attach success response")
    public static void attachSuccessResponse(Response response) {
        attachResponseDetails(response);
        String responseBody = response.getBody().asString();
        if (responseBody != null && !responseBody.isEmpty()) {
            Allure.addAttachment("✅ Response Body", "application/json", responseBody);
        }
    }

    @Step("Attach success response with request details")
    public static void attachSuccessResponseWithRequest(Response response, String method, String baseUrl, String endpoint, Object requestBody) {
        attachCompleteRequestDetails(method, baseUrl, endpoint, requestBody);
        attachSuccessResponse(response);
    }

    @Step("Attach success response with path variables")
    public static void attachSuccessResponseWithPathVars(Response response, String method, String baseUrl, String endpoint, String pathVariable, Object requestBody) {
        attachRequestWithPathVariables(method, baseUrl, endpoint, pathVariable, requestBody);
        attachSuccessResponse(response);
    }

    @Step("Attach expected error response")
    public static void attachExpectedError(Response response) {
        attachResponseDetails(response);
        String responseBody = response.getBody().asString();
        if (responseBody != null && !responseBody.isEmpty()) {
            Allure.addAttachment("⚠️ Response Body", "application/json", responseBody);
        }
    }

    @Step("Attach expected error with request details")
    public static void attachExpectedErrorWithRequest(Response response, String method, String baseUrl, String endpoint, Object requestBody) {
        attachCompleteRequestDetails(method, baseUrl, endpoint, requestBody);
        attachExpectedError(response);
    }

    @Step("Attach unexpected error response")
    public static void attachUnexpectedError(Response response) {
        attachResponseDetails(response);
        String responseBody = response.getBody().asString();
        if (responseBody != null && !responseBody.isEmpty()) {
            Allure.addAttachment("🚨 Response Body", "application/json", responseBody);
        }
    }

    @Step("Attach unexpected error with request details")
    public static void attachUnexpectedErrorWithRequest(Response response, String method, String baseUrl, String endpoint, Object requestBody) {
        attachCompleteRequestDetails(method, baseUrl, endpoint, requestBody);
        attachUnexpectedError(response);
    }

    @Step("Attach response by status code")
    public static void attachResponseByStatus(Response response) {
        int statusCode = response.getStatusCode();
        
        if (statusCode >= 200 && statusCode < 300) {
            attachSuccessResponse(response);
        } else if (statusCode >= 400 && statusCode < 500) {
            attachExpectedError(response);
        } else if (statusCode >= 500) {
            attachUnexpectedError(response);
        } else {
            // Fallback for other status codes
            attachResponseBody(response);
        }
    }

    @Step("Attach response by status code with request details")
    public static void attachResponseByStatusWithRequest(Response response, String method, String baseUrl, String endpoint, Object requestBody) {
        int statusCode = response.getStatusCode();
        
        if (statusCode >= 200 && statusCode < 300) {
            attachSuccessResponseWithRequest(response, method, baseUrl, endpoint, requestBody);
        } else if (statusCode >= 400 && statusCode < 500) {
            attachExpectedErrorWithRequest(response, method, baseUrl, endpoint, requestBody);
        } else if (statusCode >= 500) {
            attachUnexpectedErrorWithRequest(response, method, baseUrl, endpoint, requestBody);
        } else {
            // Fallback for other status codes
            attachCompleteRequestDetails(method, baseUrl, endpoint, requestBody);
            attachResponseBody(response);
        }
    }

    @Step("Attach response by status code with path variables")
    public static void attachResponseByStatusWithPathVars(Response response, String method, String baseUrl, String endpoint, String pathVariable, Object requestBody) {
        int statusCode = response.getStatusCode();
        
        if (statusCode >= 200 && statusCode < 300) {
            attachSuccessResponseWithPathVars(response, method, baseUrl, endpoint, pathVariable, requestBody);
        } else if (statusCode >= 400 && statusCode < 500) {
            attachRequestWithPathVariables(method, baseUrl, endpoint, pathVariable, requestBody);
            attachExpectedError(response);
        } else if (statusCode >= 500) {
            attachRequestWithPathVariables(method, baseUrl, endpoint, pathVariable, requestBody);
            attachUnexpectedError(response);
        } else {
            // Fallback for other status codes
            attachRequestWithPathVariables(method, baseUrl, endpoint, pathVariable, requestBody);
            attachResponseBody(response);
        }
    }

    @Step("Attach full request details")
    public static void attachFullRequestDetails(String method, String url, Object body) {
        attachRequestUrl(method, url);
        if (body != null) {
            attachRequestBody(body);
        }
    }
}
