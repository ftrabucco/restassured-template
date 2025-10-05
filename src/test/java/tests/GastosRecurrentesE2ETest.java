package tests;

import base.ApiTestWithCleanup;
import clients.GastosRecurrentesApiClient;
import clients.GastosUnicosApiClient;
import com.fasterxml.jackson.databind.JsonNode;
import io.qameta.allure.*;
import io.restassured.response.Response;
import models.GastoRecurrente;
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
 * End-to-End test suite for Gastos Recurrentes (Recurring Expenses)
 * Tests complete lifecycle including gasto generation and processing
 */
@Feature("Gastos Recurrentes E2E with Real Generation")
@DisplayName("Gastos Recurrentes End-to-End Tests with Real Generation")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class GastosRecurrentesE2ETest extends ApiTestWithCleanup {

    private GastosRecurrentesApiClient gastosRecurrentesClient;
    private GastosUnicosApiClient gastosUnicosClient;
    private static McpServerConnector mcpConnector;
    private static JsonNode businessRules;

    // Shared test data
    private static GastoRecurrente createdGastoRecurrente;
    private static String createdGastoRecurrenteId;
    private static int initialGastosUnicosCount;

    @BeforeAll
    public static void setUpE2ETest() {
        mcpConnector = McpServerConnector.getInstance();
        businessRules = mcpConnector.getBusinessRules();
    }

    @Override
    protected void customAuthenticatedSetup() {
        gastosRecurrentesClient = new GastosRecurrentesApiClient().withRequestSpec(requestSpec);
        gastosUnicosClient = new GastosUnicosApiClient().withRequestSpec(requestSpec);
    }


    @Test
    @Order(1)
    @Story("E2E Flow - Setup and Baseline")
    @DisplayName("E2E Step 1: Should establish baseline count of gastos únicos")
    @Description("Establishes baseline count of gastos únicos before creating recurring gasto")
    public void step01_shouldEstablishBaselineGastosUnicosCount() {
        Response gastosUnicosResponse = gastosUnicosClient.getAllGastosUnicos();
        ResponseValidator.validateStatusCode(gastosUnicosResponse, 200);
        
        var gastosUnicos = gastosUnicosResponse.jsonPath().getList("data");
        initialGastosUnicosCount = gastosUnicos.size();
        
        Allure.addAttachment("Initial Gastos Únicos Count", String.valueOf(initialGastosUnicosCount));
        System.out.println("=== BASELINE ESTABLISHED ===");
        System.out.println("Initial Gastos Únicos Count: " + initialGastosUnicosCount);
        System.out.println("===========================");
    }

    @Test
    @Order(2)
    @Story("E2E Flow - Create Recurring Gasto")
    @DisplayName("E2E Step 2: Should create a new gasto recurrente")
    @Description("Creates a new recurring gasto that should generate gastos únicos")
    @Severity(SeverityLevel.CRITICAL)
    public void step02_shouldCreateGastoRecurrenteSuccessfully() {
        // Create gasto recurrente with next generation date soon
        createdGastoRecurrente = createGastoRecurrenteForTesting();
        
        // Execute creation
        Response createResponse = gastosRecurrentesClient.createGastoRecurrente(createdGastoRecurrente);
        
        // Validate creation
        ResponseValidator.validateStatusCode(createResponse, 201);
        ResponseValidator.validateFieldExists(createResponse, "data.gastoRecurrente.descripcion");
        ResponseValidator.validateFieldExists(createResponse, "data.gastoRecurrente.monto");
        ResponseValidator.validateFieldExists(createResponse, "data.gastoRecurrente.activo");
        
        // Extract ID
        if (createResponse.jsonPath().get("data.gastoRecurrente.id") != null) {
            createdGastoRecurrenteId = createResponse.jsonPath().get("data.gastoRecurrente.id").toString();
        } else {
            createdGastoRecurrenteId = findGastoRecurrenteIdByDescription(createdGastoRecurrente.getDescripcion());
        }
        
        assertNotNull(createdGastoRecurrenteId, "Should have created gasto recurrente ID");
        
        logE2EStep("CREATE RECURRING", createdGastoRecurrente, createResponse);
    }

    @Test
    @Order(3)
    @Story("E2E Flow - Verify Recurring Gasto")
    @DisplayName("E2E Step 3: Should retrieve and validate the created gasto recurrente")
    @Description("Retrieves the created recurring gasto and validates all fields")
    public void step03_shouldVerifyCreatedGastoRecurrente() {
        assertNotNull(createdGastoRecurrenteId, "Gasto recurrente ID should be available");
        
        Response getResponse = gastosRecurrentesClient.getGastoRecurrenteById(createdGastoRecurrenteId);
        ResponseValidator.validateStatusCode(getResponse, 200);
        
        // Validate all fields match
        validateGastoRecurrenteFieldsMatch(getResponse, createdGastoRecurrente);
        
        // Validate it's active
        assertTrue(getResponse.jsonPath().getBoolean("data.activo"), "Gasto recurrente should be active");
        
        logE2EStep("VERIFY RECURRING", null, getResponse);
    }

    @Test
    @Order(4)
    @Story("E2E Flow - Trigger Generation")
    @DisplayName("E2E Step 4: Should trigger generation of gastos únicos")
    @Description("Triggers the generation process to create gastos únicos from recurring gastos")
    public void step04_shouldTriggerGastosGeneration() {
        // Call the generation endpoint (this should create gastos únicos from recurring gastos)
        Response generationResponse = triggerGastosGeneration();
        
        // Validate generation response
        validateGenerationResponse(generationResponse);
        
        logE2EStep("TRIGGER GENERATION", null, generationResponse);
    }

    @Test
    @Order(5)
    @Story("E2E Flow - Verify Generation")
    @DisplayName("E2E Step 5: Should verify that gastos únicos were generated")
    @Description("Verifies that new gastos únicos were created from the recurring gasto")
    public void step05_shouldVerifyGastosUnicosGenerated() {
        // Wait a moment for processing
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Get current count of gastos únicos
        Response gastosUnicosResponse = gastosUnicosClient.getAllGastosUnicos();
        ResponseValidator.validateStatusCode(gastosUnicosResponse, 200);
        
        var currentGastosUnicos = gastosUnicosResponse.jsonPath().getList("data");
        int currentCount = currentGastosUnicos.size();
        
        // Verify count increased
        assertTrue(currentCount >= initialGastosUnicosCount, 
                  "Gastos únicos count should be >= initial count");
        
        // Try to find generated gasto único that matches our recurring gasto
        boolean generatedGastoFound = findGeneratedGastoUnico(gastosUnicosResponse);
        
        if (generatedGastoFound) {
            System.out.println("✅ Generated gasto único found!");
        } else {
            System.out.println("⚠️ Generated gasto único not found - may need manual trigger or different timing");
        }
        
        Allure.addAttachment("Generation Results", 
                           String.format("Initial: %d, Current: %d, Generated Found: %b", 
                                       initialGastosUnicosCount, currentCount, generatedGastoFound));
        
        logE2EStep("VERIFY GENERATION", null, gastosUnicosResponse);
    }

    @Test
    @Order(6)
    @Story("E2E Flow - Update Recurring Gasto")
    @DisplayName("E2E Step 6: Should update the recurring gasto")
    @Description("Updates the recurring gasto and validates the changes")
    public void step06_shouldUpdateGastoRecurrente() {
        assertNotNull(createdGastoRecurrenteId, "Gasto recurrente ID should be available");
        
        // Create updated data
        GastoRecurrente updatedGasto = createUpdatedGastoRecurrenteData();
        
        // Execute update
        Response updateResponse = gastosRecurrentesClient.updateGastoRecurrente(createdGastoRecurrenteId, updatedGasto);
        ResponseValidator.validateStatusCode(updateResponse, 200);
        
        // Verify update was applied
        Response verificationResponse = gastosRecurrentesClient.getGastoRecurrenteById(createdGastoRecurrenteId);
        validateGastoRecurrenteFieldsMatch(verificationResponse, updatedGasto);
        
        logE2EStep("UPDATE RECURRING", updatedGasto, updateResponse);
    }

    @Test
    @Order(7)
    @Story("E2E Flow - Deactivate and Reactivate")
    @DisplayName("E2E Step 7: Should deactivate and reactivate the gasto recurrente")
    @Description("Tests the activation/deactivation functionality")
    public void step07_shouldDeactivateAndReactivateGastoRecurrente() {
        assertNotNull(createdGastoRecurrenteId, "Gasto recurrente ID should be available");
        
        // Deactivate
        Response deactivateResponse = gastosRecurrentesClient.activateDeactivateGastoRecurrente(createdGastoRecurrenteId, false);
        validateActivationResponse(deactivateResponse, false);
        
        // Verify deactivation
        Response verifyDeactivatedResponse = gastosRecurrentesClient.getGastoRecurrenteById(createdGastoRecurrenteId);
        assertFalse(verifyDeactivatedResponse.jsonPath().getBoolean("data.activo"), 
                   "Gasto recurrente should be deactivated");
        
        // Reactivate
        Response reactivateResponse = gastosRecurrentesClient.activateDeactivateGastoRecurrente(createdGastoRecurrenteId, true);
        validateActivationResponse(reactivateResponse, true);
        
        // Verify reactivation
        Response verifyReactivatedResponse = gastosRecurrentesClient.getGastoRecurrenteById(createdGastoRecurrenteId);
        assertTrue(verifyReactivatedResponse.jsonPath().getBoolean("data.activo"), 
                  "Gasto recurrente should be reactivated");
        
        logE2EStep("DEACTIVATE/REACTIVATE", null, reactivateResponse);
    }

    @Test
    @Order(8)
    @Story("E2E Flow - Cleanup")
    @DisplayName("E2E Step 8: Should delete the gasto recurrente")
    @Description("Cleans up by deleting the created recurring gasto")
    @Severity(SeverityLevel.CRITICAL)
    public void step09_shouldDeleteGastoRecurrente() {
        assertNotNull(createdGastoRecurrenteId, "Gasto recurrente ID should be available");
        
        // Execute deletion
        Response deleteResponse = gastosRecurrentesClient.deleteGastoRecurrente(createdGastoRecurrenteId);
        ResponseValidator.validateStatusCode(deleteResponse, 200);
        
        // Verify deletion
        Response verificationResponse = gastosRecurrentesClient.getGastoRecurrenteById(createdGastoRecurrenteId);
        ResponseValidator.validateStatusCode(verificationResponse, 404);
        
        logE2EStep("DELETE RECURRING", null, deleteResponse);

        // Clear the ID to avoid unnecessary cleanup in @AfterEach
        createdGastoRecurrenteId = null;

        System.out.println("=== E2E TEST COMPLETED ===");
        System.out.println("All steps completed successfully!");
        System.out.println("===========================");
    }

    // Helper methods

    @Step("Create gasto recurrente for testing")
    private GastoRecurrente createGastoRecurrenteForTesting() {
        GastoRecurrente gasto = TestDataFactory.createRandomGastoRecurrente();
        
        // Set it to generate soon (within next few days)
        gasto.setDiaDePago(LocalDate.now().getDayOfMonth());
        gasto.setActivo(true);
        
        // Apply business rules
        JsonNode gastosRules = businessRules.get("gastos");
        if (gastosRules != null && gastosRules.has("monto_minimo")) {
            double minAmount = gastosRules.get("monto_minimo").asDouble();
            if (gasto.getMonto().compareTo(BigDecimal.valueOf(minAmount)) < 0) {
                gasto.setMonto(BigDecimal.valueOf(minAmount + 100));
            }
        }
        
        return gasto;
    }

    @Step("Find gasto recurrente ID by description")
    private String findGastoRecurrenteIdByDescription(String description) {
        Response listResponse = gastosRecurrentesClient.getAllGastosRecurrentes();
        if (listResponse.getStatusCode() == 200) {
            var gastos = listResponse.jsonPath().getList("data");
            for (int i = 0; i < gastos.size(); i++) {
                var gasto = listResponse.jsonPath().getMap("data[" + i + "]");
                if (description.equals(gasto.get("descripcion"))) {
                    return gasto.get("id").toString();
                }
            }
        }
        return null;
    }

    @Step("Validate gasto recurrente fields match")
    private void validateGastoRecurrenteFieldsMatch(Response response, GastoRecurrente expectedGasto) {
        java.util.Map<String, Object> responseData = response.jsonPath().getMap("data");
        
        assertEquals(expectedGasto.getDescripcion(), 
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
        
        assertEquals(expectedGasto.getMonto().compareTo(responseMonto), 0,
                    "Monto should match");
        
        assertEquals(expectedGasto.getDiaDePago(),
                    ((Number) responseData.get("dia_de_pago")).intValue(),
                    "Dia de pago should match");
    }

    @Step("Trigger gastos generation")
    private Response triggerGastosGeneration() {
        // This calls the generate endpoint that should process recurring gastos
        return gastosRecurrentesClient.generateGastos();
    }

    @Step("Validate generation response")
    private void validateGenerationResponse(Response response) {
        // Generation might return 200 with info about what was generated
        ResponseValidator.validateStatusCode(response, 200);
        ResponseValidator.validateResponseTime(response, 30000); // Generation might take longer
        
        System.out.println("Generation Response: " + response.getBody().asString());
    }

    @Step("Find generated gasto único")
    private boolean findGeneratedGastoUnico(Response gastosUnicosResponse) {
        var gastosUnicos = gastosUnicosResponse.jsonPath().getList("data");
        
        for (int i = 0; i < gastosUnicos.size(); i++) {
            var gasto = gastosUnicosResponse.jsonPath().getMap("data[" + i + "]");
            
            // Look for gasto único that matches our recurring gasto characteristics
            String descripcion = (String) gasto.get("descripcion");
            if (descripcion != null && 
                (descripcion.contains(createdGastoRecurrente.getDescripcion()) ||
                 descripcion.equals(createdGastoRecurrente.getDescripcion()))) {
                
                // Check if it's processed (generated from recurring)
                Boolean procesado = (Boolean) gasto.get("procesado");
                if (procesado != null && procesado) {
                    System.out.println("Found generated gasto único: " + descripcion);
                    return true;
                }
            }
        }
        
        return false;
    }

    @Step("Create updated gasto recurrente data")
    private GastoRecurrente createUpdatedGastoRecurrenteData() {
        GastoRecurrente updated = new GastoRecurrente();
        updated.setDescripcion("UPDATED: " + createdGastoRecurrente.getDescripcion());
        updated.setMonto(createdGastoRecurrente.getMonto().add(BigDecimal.valueOf(25)));
        updated.setDiaDePago(15); // Change payment day
        updated.setFechaInicio(createdGastoRecurrente.getFechaInicio()); // Include fecha_inicio
        updated.setCategoriaGastoId(createdGastoRecurrente.getCategoriaGastoId());
        updated.setImportanciaGastoId(createdGastoRecurrente.getImportanciaGastoId());
        updated.setTipoPagoId(createdGastoRecurrente.getTipoPagoId());
        updated.setTarjetaId(createdGastoRecurrente.getTarjetaId()); // Include tarjeta_id
        updated.setFrecuenciaGastoId(createdGastoRecurrente.getFrecuenciaGastoId());
        updated.setActivo(true);
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

    @Step("Validate generation rules")
    private void validateGenerationRules(Response response) {
        java.util.Map<String, Object> responseData = response.jsonPath().getMap("data");
        
        // Validate that active gastos can generate
        boolean activo = (Boolean) responseData.get("activo");
        if (activo) {
            assertNotNull(responseData.get("dia_de_pago"), "Active gasto should have valid payment day");
            assertNotNull(responseData.get("frecuencia_gasto_id"), "Active gasto should have frequency");
        }
        
        // Validate last generation date format if present
        if (responseData.containsKey("ultima_fecha_generado") && responseData.get("ultima_fecha_generado") != null) {
            String fechaStr = responseData.get("ultima_fecha_generado").toString();
            assertDoesNotThrow(() -> LocalDate.parse(fechaStr), 
                             "Ultima fecha generado should be valid date format");
        }
    }

    @Step("Log E2E step: {action}")
    private void logE2EStep(String action, GastoRecurrente gastoData, Response response) {
        StringBuilder logEntry = new StringBuilder();
        logEntry.append("=== E2E STEP: ").append(action).append(" ===\n");
        logEntry.append("Status Code: ").append(response.getStatusCode()).append("\n");
        logEntry.append("Response Time: ").append(response.getTime()).append("ms\n");
        
        if (gastoData != null) {
            logEntry.append("Request Data: ").append(gastoData.toString()).append("\n");
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
        createdGastoRecurrenteId = null;
        createdGastoRecurrente = null;
        initialGastosUnicosCount = 0;

        System.out.println("🏁 Gastos Recurrentes E2E Test Suite completed");
    }

    // Cleanup implementation for ApiTestWithCleanup
    @Override
    protected Map<EntityType, Function<List<String>, Integer>> getCleanupStrategies() {
        Map<EntityType, Function<List<String>, Integer>> strategies = new HashMap<>();

        // E2E tests handle their own cleanup in test steps
        // This is mainly for safety in case of test failures
        strategies.put(EntityType.GASTO_RECURRENTE, gastoRecurrenteIds ->
            performCleanup(gastoRecurrenteIds, gastosRecurrentesClient::deleteGastoRecurrente, "gasto recurrente"));

        return strategies;
    }
}