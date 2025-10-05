package tests;

import base.ApiTestWithCleanup;
import clients.DebitosAutomaticosApiClient;
import com.fasterxml.jackson.databind.JsonNode;
import io.qameta.allure.*;
import io.restassured.response.Response;
import models.DebitoAutomatico;
import org.junit.jupiter.api.*;
import utils.McpServerConnector;
import utils.ResponseValidator;
import utils.TestDataFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static base.ApiTestWithCleanup.EntityType;
import static org.junit.jupiter.api.Assertions.*;

/**
 * End-to-End test suite for Débitos Automáticos (Automatic Debits)
 * Tests complete lifecycle including debit processing and business logic
 */
@Feature("Débitos Automáticos E2E with Real Processing")
@DisplayName("Débitos Automáticos End-to-End Tests with Real Processing")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DebitosAutomaticosE2ETest extends ApiTestWithCleanup {

    private DebitosAutomaticosApiClient debitosAutomaticosClient;
    private static McpServerConnector mcpConnector;
    private static JsonNode businessRules;

    // Shared test data
    private static DebitoAutomatico createdDebitoAutomatico;
    private static String createdDebitoAutomaticoId;

    @BeforeAll
    public static void setUpE2ETest() {
        mcpConnector = McpServerConnector.getInstance();
        businessRules = mcpConnector.getBusinessRules();
    }

    @Override
    protected void customAuthenticatedSetup() {
        debitosAutomaticosClient = new DebitosAutomaticosApiClient().withRequestSpec(requestSpec);
    }


    @Test
    @Order(1)
    @Story("E2E Flow - Create Automatic Debit")
    @DisplayName("E2E Step 1: Should create a new débito automático")
    @Description("Creates a new automatic debit that should generate gastos únicos")
    @Severity(SeverityLevel.CRITICAL)
    public void step02_shouldCreateDebitoAutomaticoSuccessfully() {
        // Create débito automático with processing date soon
        createdDebitoAutomatico = createDebitoAutomaticoForTesting();
        
        // Execute creation
        Response createResponse = debitosAutomaticosClient.createDebitoAutomatico(createdDebitoAutomatico);
        
        // Validate creation
        ResponseValidator.validateStatusCode(createResponse, 201);
        ResponseValidator.validateFieldExists(createResponse, "data.debitoAutomatico.descripcion");
        ResponseValidator.validateFieldExists(createResponse, "data.debitoAutomatico.monto");
        ResponseValidator.validateFieldExists(createResponse, "data.debitoAutomatico.activo");
        
        // Extract ID
        if (createResponse.jsonPath().get("data.debitoAutomatico.id") != null) {
            createdDebitoAutomaticoId = createResponse.jsonPath().get("data.debitoAutomatico.id").toString();
        } else {
            createdDebitoAutomaticoId = findDebitoAutomaticoIdByDescription(createdDebitoAutomatico.getDescripcion());
        }
        
        assertNotNull(createdDebitoAutomaticoId, "Should have created débito automático ID");
        
        logE2EStep("CREATE AUTOMATIC DEBIT", createdDebitoAutomatico, createResponse);
    }

    @Test
    @Order(3)
    @Story("E2E Flow - Verify Automatic Debit")
    @DisplayName("E2E Step 3: Should retrieve and validate the created débito automático")
    @Description("Retrieves the created automatic debit and validates all fields")
    public void step03_shouldVerifyCreatedDebitoAutomatico() {
        assertNotNull(createdDebitoAutomaticoId, "Débito automático ID should be available");
        
        Response getResponse = debitosAutomaticosClient.getDebitoAutomaticoById(createdDebitoAutomaticoId);
        ResponseValidator.validateStatusCode(getResponse, 200);
        
        // Validate all fields match
        validateDebitoAutomaticoFieldsMatch(getResponse, createdDebitoAutomatico);
        
        // Validate it's active
        assertTrue(getResponse.jsonPath().getBoolean("data.activo"), "Débito automático should be active");
        
        logE2EStep("VERIFY AUTOMATIC DEBIT", null, getResponse);
    }

    @Test
    @Order(4)
    @Story("E2E Flow - Update Automatic Debit")
    @DisplayName("E2E Step 4: Should update the débito automático")
    @Description("Updates the automatic debit and validates the changes")
    public void step06_shouldUpdateDebitoAutomatico() {
        assertNotNull(createdDebitoAutomaticoId, "Débito automático ID should be available");
        
        // Create updated data
        DebitoAutomatico updatedDebitoAutomatico = createUpdatedDebitoAutomaticoData();
        
        // Execute update
        Response updateResponse = debitosAutomaticosClient.updateDebitoAutomatico(createdDebitoAutomaticoId, updatedDebitoAutomatico);
        ResponseValidator.validateStatusCode(updateResponse, 200);
        
        // Verify update was applied
        Response verificationResponse = debitosAutomaticosClient.getDebitoAutomaticoById(createdDebitoAutomaticoId);
        validateDebitoAutomaticoFieldsMatch(verificationResponse, updatedDebitoAutomatico);
        
        logE2EStep("UPDATE AUTOMATIC DEBIT", updatedDebitoAutomatico, updateResponse);
    }

    @Test
    @Order(5)
    @Story("E2E Flow - Deactivate and Reactivate")
    @DisplayName("E2E Step 5: Should deactivate and reactivate the débito automático")
    @Description("Tests the activation/deactivation functionality")
    public void step07_shouldDeactivateAndReactivateDebitoAutomatico() {
        assertNotNull(createdDebitoAutomaticoId, "Débito automático ID should be available");
        
        // Deactivate
        Response deactivateResponse = debitosAutomaticosClient.activateDeactivateDebitoAutomatico(createdDebitoAutomaticoId, false);
        validateActivationResponse(deactivateResponse, false);
        
        // Verify deactivation
        Response verifyDeactivatedResponse = debitosAutomaticosClient.getDebitoAutomaticoById(createdDebitoAutomaticoId);
        assertFalse(verifyDeactivatedResponse.jsonPath().getBoolean("data.activo"), 
                   "Débito automático should be deactivated");
        
        // Reactivate
        Response reactivateResponse = debitosAutomaticosClient.activateDeactivateDebitoAutomatico(createdDebitoAutomaticoId, true);
        validateActivationResponse(reactivateResponse, true);
        
        // Verify reactivation
        Response verifyReactivatedResponse = debitosAutomaticosClient.getDebitoAutomaticoById(createdDebitoAutomaticoId);
        assertTrue(verifyReactivatedResponse.jsonPath().getBoolean("data.activo"), 
                  "Débito automático should be reactivated");
        
        logE2EStep("DEACTIVATE/REACTIVATE", null, reactivateResponse);
    }

    @Test
    @Order(6)
    @Story("E2E Flow - Filter Operations")
    @DisplayName("E2E Step 6: Should test filtering operations")
    @Description("Tests filtering by category and active status")
    public void step09_shouldTestFilteringOperations() {
        assertNotNull(createdDebitoAutomaticoId, "Débito automático ID should be available");
        
        // Test filtering by category
        Response categoryFilterResponse = debitosAutomaticosClient.getDebitosAutomaticosByCategory(createdDebitoAutomatico.getCategoriaGastoId());
        ResponseValidator.validateStatusCode(categoryFilterResponse, 200);
        
        // Test filtering active débitos
        Response activeFilterResponse = debitosAutomaticosClient.getActiveDebitosAutomaticos();
        ResponseValidator.validateStatusCode(activeFilterResponse, 200);
        
        // Test filtering inactive débitos
        Response inactiveFilterResponse = debitosAutomaticosClient.getInactiveDebitosAutomaticos();
        ResponseValidator.validateStatusCode(inactiveFilterResponse, 200);
        
        logE2EStep("FILTER OPERATIONS", null, activeFilterResponse);
    }

    @Test
    @Order(7)
    @Story("E2E Flow - Cleanup")
    @DisplayName("E2E Step 7: Should delete the débito automático")
    @Description("Cleans up by deleting the created automatic debit")
    @Severity(SeverityLevel.CRITICAL)
    public void step10_shouldDeleteDebitoAutomatico() {
        assertNotNull(createdDebitoAutomaticoId, "Débito automático ID should be available");
        
        // Execute deletion
        Response deleteResponse = debitosAutomaticosClient.deleteDebitoAutomatico(createdDebitoAutomaticoId);
        ResponseValidator.validateStatusCode(deleteResponse, 200);
        
        // Verify deletion
        Response verificationResponse = debitosAutomaticosClient.getDebitoAutomaticoById(createdDebitoAutomaticoId);
        ResponseValidator.validateStatusCode(verificationResponse, 404);
        
        logE2EStep("DELETE AUTOMATIC DEBIT", null, deleteResponse);

        // Clear the ID to avoid unnecessary cleanup in @AfterEach
        createdDebitoAutomaticoId = null;

        System.out.println("=== E2E TEST COMPLETED ===");
        System.out.println("All steps completed successfully!");
        System.out.println("===========================");
    }

    // Helper methods

    @Step("Create débito automático for testing")
    private DebitoAutomatico createDebitoAutomaticoForTesting() {
        DebitoAutomatico debitoAutomatico = TestDataFactory.createRandomDebitoAutomatico();
        
        // Set it to process soon (within next few days)
        debitoAutomatico.setDiaDePago(LocalDate.now().getDayOfMonth());
        debitoAutomatico.setActivo(true);
        
        // Apply business rules
        JsonNode gastosRules = businessRules.get("gastos");
        if (gastosRules != null && gastosRules.has("monto_minimo")) {
            double minAmount = gastosRules.get("monto_minimo").asDouble();
            if (debitoAutomatico.getMonto().compareTo(BigDecimal.valueOf(minAmount)) < 0) {
                debitoAutomatico.setMonto(BigDecimal.valueOf(minAmount + 50));
            }
        }
        
        return debitoAutomatico;
    }

    @Step("Find débito automático ID by description")
    private String findDebitoAutomaticoIdByDescription(String description) {
        Response listResponse = debitosAutomaticosClient.getAllDebitosAutomaticos();
        if (listResponse.getStatusCode() == 200) {
            var debitos = listResponse.jsonPath().getList("data");
            for (int i = 0; i < debitos.size(); i++) {
                var debito = listResponse.jsonPath().getMap("data[" + i + "]");
                if (description.equals(debito.get("descripcion"))) {
                    return debito.get("id").toString();
                }
            }
        }
        return null;
    }

    @Step("Validate débito automático fields match")
    private void validateDebitoAutomaticoFieldsMatch(Response response, DebitoAutomatico expectedDebitoAutomatico) {
        java.util.Map<String, Object> responseData = response.jsonPath().getMap("data");
        
        assertEquals(expectedDebitoAutomatico.getDescripcion(), 
                    responseData.get("descripcion").toString(),
                    "Descripcion should match");
        
        // Validate monto
        BigDecimal responseMonto;
        Object montoValue = responseData.get("monto");
        if (montoValue instanceof String) {
            responseMonto = new BigDecimal((String) montoValue);
        } else if (montoValue instanceof Number) {
            responseMonto = BigDecimal.valueOf(((Number) montoValue).doubleValue());
        } else {
            throw new IllegalArgumentException("Unexpected monto type: " + montoValue.getClass());
        }
        
        assertEquals(expectedDebitoAutomatico.getMonto().compareTo(responseMonto), 0,
                    "Monto should match");
        
        assertEquals(expectedDebitoAutomatico.getDiaDePago(),
                    ((Number) responseData.get("dia_de_pago")).intValue(),
                    "Dia de pago should match");
    }


    @Step("Create updated débito automático data")
    private DebitoAutomatico createUpdatedDebitoAutomaticoData() {
        DebitoAutomatico updated = new DebitoAutomatico.Builder()
                .descripcion("UPDATED: " + createdDebitoAutomatico.getDescripcion())
                .monto(createdDebitoAutomatico.getMonto().add(BigDecimal.valueOf(30)))
                .diaDePago(20) // Change payment day
                .categoriaGastoId(createdDebitoAutomatico.getCategoriaGastoId())
                .importanciaGastoId(createdDebitoAutomatico.getImportanciaGastoId())
                .frecuenciaGastoId(createdDebitoAutomatico.getFrecuenciaGastoId())
                .tipoPagoId(createdDebitoAutomatico.getTipoPagoId())
                .tarjetaId(createdDebitoAutomatico.getTarjetaId())
                .activo(true)
                .build();
        return updated;
    }

    @Step("Validate activation response")
    private void validateActivationResponse(Response response, boolean shouldBeActive) {
        ResponseValidator.validateStatusCode(response, 200);
        ResponseValidator.validateResponseTime(response, 5000);
    }

    @Step("Validate compliance with business rules")
    private void validateComplianceWithBusinessRules(Response response) {
        JsonNode gastosRules = businessRules.get("gastos");
        java.util.Map<String, Object> responseData = response.jsonPath().getMap("data");
        
        // Validate monto rules
        if (gastosRules.has("monto_minimo")) {
            double minAmount = gastosRules.get("monto_minimo").asDouble();
            BigDecimal responseMonto;
            Object montoValue = responseData.get("monto");
            if (montoValue instanceof String) {
                responseMonto = new BigDecimal((String) montoValue);
            } else if (montoValue instanceof Number) {
                responseMonto = BigDecimal.valueOf(((Number) montoValue).doubleValue());
            } else {
                throw new IllegalArgumentException("Unexpected monto type: " + montoValue.getClass());
            }
            
            assertTrue(responseMonto.compareTo(BigDecimal.valueOf(minAmount)) >= 0,
                      "Monto should meet minimum business rule");
        }
        
        // Validate required fields
        assertNotNull(responseData.get("categoria_gasto_id"), "Categoria should be set");
        assertNotNull(responseData.get("importancia_gasto_id"), "Importancia should be set");
        assertNotNull(responseData.get("frecuencia_gasto_id"), "Frecuencia should be set");
    }

    @Step("Validate frequency logic")
    private void validateFrequencyLogic(Response response) {
        java.util.Map<String, Object> responseData = response.jsonPath().getMap("data");
        
        // Validate frequency exists
        assertNotNull(responseData.get("frecuencia_gasto_id"), "Frequency should be set");
        
        // Validate day of payment is valid (1-31)
        int diaDePago = ((Number) responseData.get("dia_de_pago")).intValue();
        assertTrue(diaDePago >= 1 && diaDePago <= 31, "Dia de pago should be valid (1-31)");
        
        // If monthly frequency, validate day makes sense
        if (responseData.containsKey("frecuencia") && responseData.get("frecuencia") instanceof java.util.Map) {
            @SuppressWarnings("unchecked")
            java.util.Map<String, Object> frecuencia = (java.util.Map<String, Object>) responseData.get("frecuencia");
            if (frecuencia.containsKey("nombre")) {
                String frecuenciaNombre = frecuencia.get("nombre").toString();
                if ("Mensual".equalsIgnoreCase(frecuenciaNombre)) {
                    assertTrue(diaDePago <= 28, "For monthly frequency, day should be <= 28 to avoid issues");
                }
            }
        }
    }

    @Step("Validate processing rules")
    private void validateProcessingRules(Response response) {
        java.util.Map<String, Object> responseData = response.jsonPath().getMap("data");
        
        // Validate that active débitos can be processed
        boolean activo = (Boolean) responseData.get("activo");
        if (activo) {
            assertNotNull(responseData.get("dia_de_pago"), "Active débito should have valid payment day");
            assertNotNull(responseData.get("frecuencia_gasto_id"), "Active débito should have frequency");
        }
        
        // Validate last generation date format if present
        if (responseData.containsKey("ultima_fecha_generado") && responseData.get("ultima_fecha_generado") != null) {
            String fechaStr = responseData.get("ultima_fecha_generado").toString();
            assertDoesNotThrow(() -> LocalDate.parse(fechaStr), 
                             "Ultima fecha generado should be valid date format");
        }
    }

    @Step("Log E2E step: {action}")
    private void logE2EStep(String action, DebitoAutomatico debitoData, Response response) {
        StringBuilder logEntry = new StringBuilder();
        logEntry.append("=== E2E STEP: ").append(action).append(" ===\n");
        logEntry.append("Status Code: ").append(response.getStatusCode()).append("\n");
        logEntry.append("Response Time: ").append(response.getTime()).append("ms\n");
        
        if (debitoData != null) {
            logEntry.append("Request Data: ").append(debitoData.toString()).append("\n");
        }
        
        logEntry.append("Response Body: ").append(response.getBody().asString()).append("\n");
        logEntry.append("===========================\n");
        
        Allure.addAttachment("E2E Step: " + action, "text/plain", logEntry.toString());
        System.out.println(logEntry.toString());
    }

    @AfterAll
    static void teardownE2ETest() {
        // Final cleanup is now handled by ApiTestWithCleanup automatically
        // Static variables for tracking are cleared here
        createdDebitoAutomaticoId = null;
        createdDebitoAutomatico = null;

        System.out.println("🏁 Débitos Automáticos E2E Test Suite completed");
    }

    // Cleanup implementation for ApiTestWithCleanup
    @Override
    protected Map<EntityType, Function<List<String>, Integer>> getCleanupStrategies() {
        Map<EntityType, Function<List<String>, Integer>> strategies = new HashMap<>();

        // E2E tests handle their own cleanup in test steps
        // This is mainly for safety in case of test failures
        strategies.put(EntityType.DEBITO_AUTOMATICO, debitoAutomaticoIds ->
            performCleanup(debitoAutomaticoIds, debitosAutomaticosClient::deleteDebitoAutomatico, "débito automático"));

        return strategies;
    }
}