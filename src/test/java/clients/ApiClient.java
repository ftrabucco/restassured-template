package clients;

import config.ConfigManager;
import io.qameta.allure.Step;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import utils.AllureAttachments;

import static io.restassured.RestAssured.given;

/**
 * Base API client implementing Builder pattern for request construction
 */
public abstract class ApiClient {
    protected ConfigManager config;
    protected RequestSpecification requestSpec;

    public ApiClient() {
        this.config = ConfigManager.getInstance();
        // Initialize with basic request specification
        initializeRequestSpec();
    }
    
    private void initializeRequestSpec() {
        if (this.requestSpec == null) {
            this.requestSpec = io.restassured.RestAssured.given()
                .baseUri(config.getBaseUrl())
                .contentType("application/json")
                .accept("application/json");
        }
    }

    @SuppressWarnings("unchecked")
    public <T extends ApiClient> T withRequestSpec(RequestSpecification requestSpec) {
        this.requestSpec = requestSpec;
        return (T) this;
    }

    @Step("Execute GET request to {endpoint}")
    protected Response get(String endpoint) {
        Response response = given(requestSpec)
                .when()
                .get(endpoint)
                .then()
                .extract()
                .response();
        
        // Auto-attach request and response details
        if (endpoint.contains("/") && !endpoint.startsWith("/")) {
            // Extract path variable from endpoint like "baseEndpoint/123"
            String[] parts = endpoint.split("/");
            if (parts.length >= 2) {
                String baseEndpoint = "/" + parts[0];
                String pathVariable = parts[parts.length - 1];
                AllureAttachments.attachResponseByStatusWithPathVars(response, "GET", config.getBaseUrl(), baseEndpoint, pathVariable, null);
            } else {
                AllureAttachments.attachResponseByStatusWithRequest(response, "GET", config.getBaseUrl(), endpoint, null);
            }
        } else {
            AllureAttachments.attachResponseByStatusWithRequest(response, "GET", config.getBaseUrl(), endpoint, null);
        }
        
        return response;
    }

    @Step("Execute POST request to {endpoint}")
    protected Response post(String endpoint, Object body) {
        Response response = given(requestSpec)
                .body(body)
                .when()
                .post(endpoint)
                .then()
                .extract()
                .response();
        
        // Auto-attach request and response details
        AllureAttachments.attachResponseByStatusWithRequest(response, "POST", config.getBaseUrl(), endpoint, body);
        return response;
    }

    @Step("Execute PUT request to {endpoint}")
    protected Response put(String endpoint, Object body) {
        Response response = given(requestSpec)
                .body(body)
                .when()
                .put(endpoint)
                .then()
                .extract()
                .response();
        
        // Auto-attach request and response details
        if (endpoint.contains("/") && !endpoint.startsWith("/")) {
            // Extract path variable from endpoint like "baseEndpoint/123"
            String[] parts = endpoint.split("/");
            if (parts.length >= 2) {
                String baseEndpoint = "/" + parts[0];
                String pathVariable = parts[parts.length - 1];
                AllureAttachments.attachResponseByStatusWithPathVars(response, "PUT", config.getBaseUrl(), baseEndpoint, pathVariable, body);
            } else {
                AllureAttachments.attachResponseByStatusWithRequest(response, "PUT", config.getBaseUrl(), endpoint, body);
            }
        } else {
            AllureAttachments.attachResponseByStatusWithRequest(response, "PUT", config.getBaseUrl(), endpoint, body);
        }
        
        return response;
    }

    @Step("Execute DELETE request to {endpoint}")
    protected Response delete(String endpoint) {
        Response response = given(requestSpec)
                .when()
                .delete(endpoint)
                .then()
                .extract()
                .response();
        
        // Auto-attach request and response details
        if (endpoint.contains("/") && !endpoint.startsWith("/")) {
            // Extract path variable from endpoint like "baseEndpoint/123"
            String[] parts = endpoint.split("/");
            if (parts.length >= 2) {
                String baseEndpoint = "/" + parts[0];
                String pathVariable = parts[parts.length - 1];
                AllureAttachments.attachResponseByStatusWithPathVars(response, "DELETE", config.getBaseUrl(), baseEndpoint, pathVariable, null);
            } else {
                AllureAttachments.attachResponseByStatusWithRequest(response, "DELETE", config.getBaseUrl(), endpoint, null);
            }
        } else {
            AllureAttachments.attachResponseByStatusWithRequest(response, "DELETE", config.getBaseUrl(), endpoint, null);
        }
        
        return response;
    }

    @Step("Execute PATCH request to {endpoint}")
    protected Response patch(String endpoint, Object body) {
        Response response = given(requestSpec)
                .body(body)
                .when()
                .patch(endpoint)
                .then()
                .extract()
                .response();
        
        // Auto-attach request and response details
        if (endpoint.contains("/") && !endpoint.startsWith("/")) {
            // Extract path variable from endpoint like "baseEndpoint/123"
            String[] parts = endpoint.split("/");
            if (parts.length >= 2) {
                String baseEndpoint = "/" + parts[0];
                String pathVariable = parts[parts.length - 1];
                AllureAttachments.attachResponseByStatusWithPathVars(response, "PATCH", config.getBaseUrl(), baseEndpoint, pathVariable, body);
            } else {
                AllureAttachments.attachResponseByStatusWithRequest(response, "PATCH", config.getBaseUrl(), endpoint, body);
            }
        } else {
            AllureAttachments.attachResponseByStatusWithRequest(response, "PATCH", config.getBaseUrl(), endpoint, body);
        }
        
        return response;
    }
}
