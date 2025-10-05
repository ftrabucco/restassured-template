package clients;

import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import models.Tarjeta;
import utils.AllureAttachments;
import utils.AllureLogger;

import java.util.Map;

import static io.restassured.RestAssured.given;

/**
 * API client for Tarjetas (Cards) endpoints
 * Based on MCP server test scenarios for tarjetas endpoint
 */
public class TarjetasApiClient extends ApiClient {

    private static final String TARJETAS_ENDPOINT = "/api/tarjetas";

    public TarjetasApiClient() {
        super();
    }

    public TarjetasApiClient(RequestSpecification requestSpec) {
        super();
        this.requestSpec = requestSpec;
    }

    public TarjetasApiClient withRequestSpec(RequestSpecification requestSpec) {
        return new TarjetasApiClient(requestSpec);
    }

    public Response getAllTarjetas() {
        AllureLogger.logStep("Getting all tarjetas");

        Response response = given(requestSpec)
                .when()
                .get(TARJETAS_ENDPOINT);

        AllureLogger.attachResponse(response);
        return response;
    }

    public Response getTarjetaById(String id) {
        AllureLogger.logStep("Getting tarjeta by ID: " + id);

        Response response = given(requestSpec)
                .pathParam("id", id)
                .when()
                .get(TARJETAS_ENDPOINT + "/{id}");

        AllureLogger.attachResponse(response);
        return response;
    }

    public Response createTarjeta(Tarjeta tarjeta) {
        AllureLogger.logStep("Creating tarjeta");
        AllureAttachments.attachRequestBody(tarjeta);

        Response response = given(requestSpec)
                .body(tarjeta)
                .when()
                .post(TARJETAS_ENDPOINT);

        AllureLogger.attachResponse(response);
        return response;
    }

    public Response updateTarjeta(String id, Tarjeta tarjeta) {
        AllureLogger.logStep("Updating tarjeta ID: " + id);
        AllureAttachments.attachRequestBody(tarjeta);

        Response response = given(requestSpec)
                .pathParam("id", id)
                .body(tarjeta)
                .when()
                .put(TARJETAS_ENDPOINT + "/{id}");

        AllureLogger.attachResponse(response);
        return response;
    }

    public Response deleteTarjeta(String id) {
        AllureLogger.logStep("Deleting tarjeta ID: " + id);

        Response response = given(requestSpec)
                .pathParam("id", id)
                .when()
                .delete(TARJETAS_ENDPOINT + "/{id}");

        AllureLogger.attachResponse(response);
        return response;
    }

    public Response getTarjetasByUser(String userId) {
        AllureLogger.logStep("Getting tarjetas for user ID: " + userId);

        Response response = given(requestSpec)
                .queryParam("usuario_id", userId)
                .when()
                .get(TARJETAS_ENDPOINT);

        AllureLogger.attachResponse(response);
        return response;
    }

    public Response getTarjetasByTipo(String tipo) {
        AllureLogger.logStep("Getting tarjetas by tipo: " + tipo);

        Response response = given(requestSpec)
                .queryParam("tipo", tipo)
                .when()
                .get(TARJETAS_ENDPOINT);

        AllureLogger.attachResponse(response);
        return response;
    }

    public Response getTarjetasByBanco(String banco) {
        AllureLogger.logStep("Getting tarjetas by banco: " + banco);

        Response response = given(requestSpec)
                .queryParam("banco", banco)
                .when()
                .get(TARJETAS_ENDPOINT);

        AllureLogger.attachResponse(response);
        return response;
    }

    public Response updateLimiteCredito(String id, String nuevoLimite) {
        AllureLogger.logStep("Updating limite credito for tarjeta ID: " + id + " to: " + nuevoLimite);

        String requestBody = String.format("{\"limite_credito\": \"%s\"}", nuevoLimite);
        AllureAttachments.attachRequestBody(requestBody);

        Response response = given(requestSpec)
                .pathParam("id", id)
                .body(requestBody)
                .when()
                .patch(TARJETAS_ENDPOINT + "/{id}/limite");

        AllureLogger.attachResponse(response);
        return response;
    }

    public Response createTarjetaWithInvalidData(Object invalidData) {
        AllureLogger.logStep("Creating tarjeta with invalid data");
        AllureAttachments.attachRequestBody(invalidData);

        Response response = given(requestSpec)
                .body(invalidData)
                .when()
                .post(TARJETAS_ENDPOINT);

        AllureLogger.attachResponse(response);
        return response;
    }

    public Response createCardWithMissingFields(Map<String, Object> incompleteData) {
        AllureLogger.logStep("Creating card with missing required fields");
        AllureAttachments.attachRequestBody(incompleteData);

        Response response = given(requestSpec)
                .body(incompleteData)
                .when()
                .post(TARJETAS_ENDPOINT);

        AllureLogger.attachResponse(response);
        return response;
    }

    public Response updateNonExistentCard(String nonExistentId, Tarjeta tarjeta) {
        AllureLogger.logStep("Updating non-existent card ID: " + nonExistentId);
        AllureAttachments.attachRequestBody(tarjeta);

        Response response = given(requestSpec)
                .pathParam("id", nonExistentId)
                .body(tarjeta)
                .when()
                .put(TARJETAS_ENDPOINT + "/{id}");

        AllureLogger.attachResponse(response);
        return response;
    }

    public Response deleteNonExistentCard(String nonExistentId) {
        AllureLogger.logStep("Deleting non-existent card ID: " + nonExistentId);

        Response response = given(requestSpec)
                .pathParam("id", nonExistentId)
                .when()
                .delete(TARJETAS_ENDPOINT + "/{id}");

        AllureLogger.attachResponse(response);
        return response;
    }
}