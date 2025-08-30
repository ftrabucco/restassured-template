package utils;

import io.qameta.allure.Allure;
import io.qameta.allure.Step;
import io.restassured.response.Response;

/**
 * Utility class for adding attachments to Allure reports
 */
public class AllureAttachments {

    @Step("Attach request URL and parameters")
    public static void attachRequestUrl(String method, String url) {
        Allure.addAttachment("Request URL", "text/plain", method + " " + url);
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
            Allure.addAttachment("Request Body", "application/json", body.toString());
        }
    }

    @Step("Attach response body")
    public static void attachResponseBody(Response response) {
        String responseBody = response.getBody().asString();
        if (responseBody != null && !responseBody.isEmpty()) {
            Allure.addAttachment("Response Body", "application/json", responseBody);
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
