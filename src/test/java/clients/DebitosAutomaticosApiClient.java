package clients;

import io.qameta.allure.Step;
import io.restassured.response.Response;
import models.DebitoAutomatico;

/**
 * API client for Débitos Automáticos (Automatic Debits) endpoints
 * Provides all CRUD operations for automatic debit management
 */
public class DebitosAutomaticosApiClient extends ApiClient {
    private final String baseEndpoint;

    public DebitosAutomaticosApiClient() {
        super();
        this.baseEndpoint = config.getEndpoint("debitos_automaticos");
    }

    @Step("Get all débitos automáticos")
    public Response getAllDebitosAutomaticos() {
        return get(baseEndpoint);
    }

    @Step("Get débito automático by ID: {debitoAutomaticoId}")
    public Response getDebitoAutomaticoById(String debitoAutomaticoId) {
        return get(baseEndpoint + "/" + debitoAutomaticoId);
    }

    @Step("Create new débito automático")
    public Response createDebitoAutomatico(DebitoAutomatico debitoAutomatico) {
        return post(baseEndpoint, debitoAutomatico);
    }

    @Step("Update débito automático with ID: {debitoAutomaticoId}")
    public Response updateDebitoAutomatico(String debitoAutomaticoId, DebitoAutomatico debitoAutomatico) {
        String endpoint = baseEndpoint + "/" + debitoAutomaticoId;
        return put(endpoint, debitoAutomatico);
    }

    @Step("Update débito automático with ID: {debitoAutomaticoId} using Map payload")
    public Response updateDebitoAutomatico(String debitoAutomaticoId, java.util.Map<String, Object> updatePayload) {
        String endpoint = baseEndpoint + "/" + debitoAutomaticoId;
        return put(endpoint, updatePayload);
    }

    @Step("Delete débito automático with ID: {debitoAutomaticoId}")
    public Response deleteDebitoAutomatico(String debitoAutomaticoId) {
        return delete(baseEndpoint + "/" + debitoAutomaticoId);
    }

    @Step("Activate/Deactivate débito automático with ID: {debitoAutomaticoId}, active: {active}")
    public Response activateDeactivateDebitoAutomatico(String debitoAutomaticoId, boolean active) {
        // Get current data and update only the activo field (similar to gastos recurrentes)
        Response getCurrentResponse = getDebitoAutomaticoById(debitoAutomaticoId);
        if (getCurrentResponse.getStatusCode() != 200) {
            return getCurrentResponse;
        }
        
        // Extract current data and update only the activo field
        java.util.Map<String, Object> currentData = getCurrentResponse.jsonPath().getMap("data");
        java.util.Map<String, Object> updatePayload = new java.util.HashMap<>();
        
        // Copy required fields from current data
        updatePayload.put("descripcion", currentData.get("descripcion"));
        updatePayload.put("monto", currentData.get("monto"));
        updatePayload.put("dia_de_pago", currentData.get("dia_de_pago"));
        updatePayload.put("categoria_gasto_id", currentData.get("categoria_gasto_id"));
        updatePayload.put("importancia_gasto_id", currentData.get("importancia_gasto_id"));
        updatePayload.put("frecuencia_gasto_id", currentData.get("frecuencia_gasto_id"));
        updatePayload.put("tipo_pago_id", currentData.get("tipo_pago_id"));
        if (currentData.get("tarjeta_id") != null) {
            updatePayload.put("tarjeta_id", currentData.get("tarjeta_id"));
        }
        updatePayload.put("activo", active); // Update only this field
        
        String endpoint = baseEndpoint + "/" + debitoAutomaticoId;
        return put(endpoint, updatePayload);
    }

    @Step("Get débitos automáticos by category: {categoriaId}")
    public Response getDebitosAutomaticosByCategory(Long categoriaId) {
        String endpoint = baseEndpoint + "?categoria_gasto_id=" + categoriaId;
        return get(endpoint);
    }

    @Step("Get active débitos automáticos")
    public Response getActiveDebitosAutomaticos() {
        String endpoint = baseEndpoint + "?activo=true";
        return get(endpoint);
    }

    @Step("Get inactive débitos automáticos")
    public Response getInactiveDebitosAutomaticos() {
        String endpoint = baseEndpoint + "?activo=false";
        return get(endpoint);
    }
}