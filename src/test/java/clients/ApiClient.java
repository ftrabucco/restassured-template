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
    }

    @SuppressWarnings("unchecked")
    public <T extends ApiClient> T withRequestSpec(RequestSpecification requestSpec) {
        this.requestSpec = requestSpec;
        return (T) this;
    }

    @Step("Execute GET request to {endpoint}")
    protected Response get(String endpoint) {
        AllureAttachments.attachRequestUrl("GET", endpoint);
        Response response = given(requestSpec)
                .when()
                .get(endpoint)
                .then()
                .extract()
                .response();
        AllureAttachments.attachResponseDetails(response);
        return response;
    }

    @Step("Execute POST request to {endpoint}")
    protected Response post(String endpoint, Object body) {
        AllureAttachments.attachFullRequestDetails("POST", endpoint, body);
        Response response = given(requestSpec)
                .body(body)
                .when()
                .post(endpoint)
                .then()
                .extract()
                .response();
        AllureAttachments.attachResponseDetails(response);
        return response;
    }

    @Step("Execute PUT request to {endpoint}")
    protected Response put(String endpoint, Object body) {
        AllureAttachments.attachFullRequestDetails("PUT", endpoint, body);
        Response response = given(requestSpec)
                .body(body)
                .when()
                .put(endpoint)
                .then()
                .extract()
                .response();
        AllureAttachments.attachResponseDetails(response);
        return response;
    }

    @Step("Execute DELETE request to {endpoint}")
    protected Response delete(String endpoint) {
        AllureAttachments.attachRequestUrl("DELETE", endpoint);
        Response response = given(requestSpec)
                .when()
                .delete(endpoint)
                .then()
                .extract()
                .response();
        AllureAttachments.attachResponseDetails(response);
        return response;
    }

    @Step("Execute PATCH request to {endpoint}")
    protected Response patch(String endpoint, Object body) {
        AllureAttachments.attachFullRequestDetails("PATCH", endpoint, body);
        Response response = given(requestSpec)
                .body(body)
                .when()
                .patch(endpoint)
                .then()
                .extract()
                .response();
        AllureAttachments.attachResponseDetails(response);
        return response;
    }
}
