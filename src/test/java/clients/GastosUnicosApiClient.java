package clients;

import io.qameta.allure.Step;
import io.restassured.response.Response;
import models.GastoUnico;
import utils.AllureLogger;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * API client for Gastos Únicos (One-time Expenses) endpoints
 */
public class GastosUnicosApiClient extends ApiClient {
    private final String baseEndpoint;
    private final ObjectMapper objectMapper;

    public GastosUnicosApiClient() {
        super();
        this.baseEndpoint = config.getEndpoint("gastos_unicos");
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Step("Get all gastos únicos")
    public Response getAllGastosUnicos() {
        return get(baseEndpoint);
    }

    @Step("Get gasto único by ID: {gastoUnicoId}")
    public Response getGastoUnicoById(String gastoUnicoId) {
        return get(baseEndpoint + "/" + gastoUnicoId);
    }

    @Step("Create new gasto único")
    public Response createGastoUnico(GastoUnico gastoUnico) {
        try {
            String requestBody = objectMapper.writeValueAsString(gastoUnico);
            AllureLogger.addRequestBodyAttachment(requestBody);
        } catch (JsonProcessingException e) {
            AllureLogger.addRequestBodyAttachment("Error serializing request: " + e.getMessage());
        }
        
        Response response = post(baseEndpoint, gastoUnico);
        AllureLogger.attachResponse(response);
        return response;
    }

    @Step("Update gasto único with ID: {gastoUnicoId}")
    public Response updateGastoUnico(String gastoUnicoId, GastoUnico gastoUnico) {
        try {
            String requestBody = objectMapper.writeValueAsString(gastoUnico);
            AllureLogger.addRequestBodyAttachment(requestBody);
        } catch (JsonProcessingException e) {
            AllureLogger.addRequestBodyAttachment("Error serializing request: " + e.getMessage());
        }
        
        String endpoint = baseEndpoint + "/" + gastoUnicoId;
        Response response = put(endpoint, gastoUnico);
        AllureLogger.attachResponse(response);
        return response;
    }

    @Step("Delete gasto único with ID: {gastoUnicoId}")
    public Response deleteGastoUnico(String gastoUnicoId) {
        return delete(baseEndpoint + "/" + gastoUnicoId);
    }
}
