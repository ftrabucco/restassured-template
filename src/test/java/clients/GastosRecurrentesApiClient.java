package clients;

import io.qameta.allure.Step;
import io.restassured.response.Response;
import models.GastoRecurrente;
import utils.AllureLogger;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * API client for Gastos Recurrentes (Recurring Expenses) endpoints
 */
public class GastosRecurrentesApiClient extends ApiClient {
    private final String baseEndpoint;
    private final ObjectMapper objectMapper;

    public GastosRecurrentesApiClient() {
        super();
        this.baseEndpoint = config.getEndpoint("gastos_recurrentes");
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Step("Get all gastos recurrentes")
    public Response getAllGastosRecurrentes() {
        return get(baseEndpoint);
    }

    @Step("Get gasto recurrente by ID: {gastoRecurrenteId}")
    public Response getGastoRecurrenteById(String gastoRecurrenteId) {
        return get(baseEndpoint + "/" + gastoRecurrenteId);
    }

    @Step("Create new gasto recurrente")
    public Response createGastoRecurrente(GastoRecurrente gastoRecurrente) {
        try {
            String requestBody = objectMapper.writeValueAsString(gastoRecurrente);
            AllureLogger.addRequestBodyAttachment(requestBody);
        } catch (JsonProcessingException e) {
            AllureLogger.addRequestBodyAttachment("Error serializing request: " + e.getMessage());
        }
        
        Response response = post(baseEndpoint, gastoRecurrente);
        AllureLogger.attachResponse(response);
        return response;
    }

    @Step("Update gasto recurrente with ID: {gastoRecurrenteId}")
    public Response updateGastoRecurrente(String gastoRecurrenteId, GastoRecurrente gastoRecurrente) {
        try {
            String requestBody = objectMapper.writeValueAsString(gastoRecurrente);
            AllureLogger.addRequestBodyAttachment(requestBody);
        } catch (JsonProcessingException e) {
            AllureLogger.addRequestBodyAttachment("Error serializing request: " + e.getMessage());
        }
        
        String endpoint = baseEndpoint + "/" + gastoRecurrenteId;
        Response response = put(endpoint, gastoRecurrente);
        AllureLogger.attachResponse(response);
        return response;
    }

    @Step("Delete gasto recurrente with ID: {gastoRecurrenteId}")
    public Response deleteGastoRecurrente(String gastoRecurrenteId) {
        Response response = delete(baseEndpoint + "/" + gastoRecurrenteId);
        AllureLogger.attachResponse(response);
        return response;
    }
}
