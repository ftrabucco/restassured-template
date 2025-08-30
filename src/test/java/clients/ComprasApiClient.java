package clients;

import io.qameta.allure.Step;
import io.restassured.response.Response;
import models.Compra;
import utils.AllureLogger;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * API client for Compras (Purchases) endpoints
 */
public class ComprasApiClient extends ApiClient {
    private final String baseEndpoint;
    private final ObjectMapper objectMapper;

    public ComprasApiClient() {
        super();
        this.baseEndpoint = config.getEndpoint("compras");
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Step("Get all compras")
    public Response getAllCompras() {
        return get(baseEndpoint);
    }

    @Step("Get compra by ID: {compraId}")
    public Response getCompraById(String compraId) {
        return get(baseEndpoint + "/" + compraId);
    }

    @Step("Create new compra")
    public Response createCompra(Compra compra) {
        try {
            String requestBody = objectMapper.writeValueAsString(compra);
            AllureLogger.addRequestBodyAttachment(requestBody);
        } catch (JsonProcessingException e) {
            AllureLogger.addRequestBodyAttachment("Error serializing request: " + e.getMessage());
        }
        
        Response response = post(baseEndpoint, compra);
        AllureLogger.attachResponse(response);
        return response;
    }
}
