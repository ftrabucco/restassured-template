package clients;

import io.qameta.allure.Step;
import io.restassured.response.Response;
import models.GastoRecurrente;
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
        return post(baseEndpoint, gastoRecurrente);
    }

    @Step("Update gasto recurrente with ID: {gastoRecurrenteId}")
    public Response updateGastoRecurrente(String gastoRecurrenteId, GastoRecurrente gastoRecurrente) {
        String endpoint = baseEndpoint + "/" + gastoRecurrenteId;
        return put(endpoint, gastoRecurrente);
    }

    @Step("Update gasto recurrente with ID: {gastoRecurrenteId} using Map payload")
    public Response updateGastoRecurrente(String gastoRecurrenteId, java.util.Map<String, Object> updatePayload) {
        String endpoint = baseEndpoint + "/" + gastoRecurrenteId;
        return put(endpoint, updatePayload);
    }

    @Step("Delete gasto recurrente with ID: {gastoRecurrenteId}")
    public Response deleteGastoRecurrente(String gastoRecurrenteId) {
        return delete(baseEndpoint + "/" + gastoRecurrenteId);
    }

    @Step("Activate/Deactivate gasto recurrente with ID: {gastoRecurrenteId}, active: {active}")
    public Response activateDeactivateGastoRecurrente(String gastoRecurrenteId, boolean active) {
        // First get the current gasto recurrente to have all required fields
        Response getCurrentResponse = getGastoRecurrenteById(gastoRecurrenteId);
        if (getCurrentResponse.getStatusCode() != 200) {
            return getCurrentResponse; // Return error if can't get current data
        }
        
        // Extract current data and update only the activo field
        java.util.Map<String, Object> currentData = getCurrentResponse.jsonPath().getMap("data");
        java.util.Map<String, Object> updatePayload = new java.util.HashMap<>();
        
        // Copy required fields from current data
        updatePayload.put("descripcion", currentData.get("descripcion"));
        updatePayload.put("monto", currentData.get("monto"));
        updatePayload.put("dia_de_pago", currentData.get("dia_de_pago"));
        updatePayload.put("frecuencia_gasto_id", currentData.get("frecuencia_gasto_id"));
        updatePayload.put("categoria_gasto_id", currentData.get("categoria_gasto_id"));
        updatePayload.put("importancia_gasto_id", currentData.get("importancia_gasto_id"));
        updatePayload.put("tipo_pago_id", currentData.get("tipo_pago_id"));
        updatePayload.put("tarjeta_id", currentData.get("tarjeta_id"));
        updatePayload.put("activo", active); // Update only this field
        
        String endpoint = baseEndpoint + "/" + gastoRecurrenteId;
        return put(endpoint, updatePayload);
    }

    @Step("Generate gastos from recurring gastos")
    public Response generateGastos() {
        // This calls the generation endpoint that processes recurring gastos
        String generateEndpoint = config.getEndpoint("gastos") + "/generate";
        return get(generateEndpoint);
    }
}