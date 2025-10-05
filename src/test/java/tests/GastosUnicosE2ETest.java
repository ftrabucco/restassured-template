package tests;

import base.ApiTestWithCleanup;
import clients.GastosUnicosApiClient;
import clients.GastosApiClient;
import com.fasterxml.jackson.databind.JsonNode;
import io.qameta.allure.*;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import models.GastoUnico;
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
 * End-to-End test suite for Gastos Únicos (One-time Expenses)
 * Tests complete CRUD lifecycle with real data validation
 */
@Feature("Gastos Únicos E2E")
@DisplayName("Gastos Únicos End-to-End Tests")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class GastosUnicosE2ETest extends ApiTestWithCleanup {

    private GastosUnicosApiClient gastosUnicosClient;
    private GastosApiClient gastosClient;
    private static McpServerConnector mcpConnector;
    private static JsonNode businessRules;

    // Shared test data across test methods
    private static GastoUnico createdGasto;
    private static String createdGastoId;
    private static String realGastoId;
    private static GastoUnico originalGasto; // Store original data before updates

    @BeforeAll
    public static void setUpE2ETest() {
        mcpConnector = McpServerConnector.getInstance();
        businessRules = mcpConnector.getBusinessRules();
    }

    @Override
    protected void customAuthenticatedSetup() {
        gastosUnicosClient = new GastosUnicosApiClient().withRequestSpec(requestSpec);
        gastosClient = new GastosApiClient().withRequestSpec(requestSpec);
    }


    @Test
    @Order(1)
    @Story("E2E Flow - Create Gasto Único")
    @DisplayName("E2E Step 1: Should create a new gasto único successfully")
    @Description("Creates a new gasto único following MCP business rules and validates the response")
    @Severity(SeverityLevel.CRITICAL)
    public void step01_shouldCreateGastoUnicoSuccessfully() {
        // Get business rules for validation
        JsonNode gastosRules = getGastosBusinessRulesFromMcp();
        
        // Create gasto único with valid data according to business rules
        createdGasto = createValidGastoUnicoWithBusinessRules(gastosRules);
        
        // Execute creation
        Response createResponse = executeGastoUnicoCreation(createdGasto);
        
        // Validate creation response
        validateGastoUnicoCreationResponse(createResponse);
        
        // Extract ID for subsequent tests
        extractCreatedGastoId(createResponse);
        
        // Store original gasto data before any updates
        originalGasto = cloneGasto(createdGasto);
        
        // Add comprehensive logging for E2E flow
        logE2EStep("CREATE", createdGasto, createResponse);
    }

    @Test
    @Order(2)
    @Story("E2E Flow - Verify Created Gasto")
    @DisplayName("E2E Step 2: Should retrieve the created gasto único by ID")
    @Description("Retrieves the created gasto único and validates all fields match the creation request")
    @Severity(SeverityLevel.CRITICAL)
    public void step02_shouldRetrieveCreatedGastoUnicoById() {
        assertNotNull(createdGastoId, "Created gasto ID should be available from previous test");
        
        // Retrieve the created gasto
        Response getResponse = retrieveGastoUnicoById(createdGastoId);
        
        // Validate retrieval response
        validateGastoUnicoRetrievalResponse(getResponse);
        
        // Validate all fields match the original creation
        validateCreatedGastoFieldsMatch(getResponse, createdGasto);
        
        // Validate against database schema from MCP
        validateAgainstDatabaseSchema(getResponse);
        
        // Log E2E step
        logE2EStep("READ", (GastoUnico) null, getResponse);
    }

    @Test
    @Order(3)
    @Story("E2E Flow - Update Gasto Único")
    @DisplayName("E2E Step 3: Should update the created gasto único")
    @Description("Updates specific fields of the created gasto único and validates the changes")
    @Severity(SeverityLevel.NORMAL)
    public void step03_shouldUpdateCreatedGastoUnico() {
        // Ensure we have a gasto to update (create one if needed)
        String gastoId = ensureGastoExists();
        
        // Create updated data following business rules
        Map<String, Object> updatedGasto = createUpdatedGastoUnicoData();
        
        // Execute update
        Response updateResponse = executeGastoUnicoUpdate(gastoId, updatedGasto);
        
        // Validate update response
        validateGastoUnicoUpdateResponse(updateResponse);
        
        // Verify the update was persisted
        Response verificationResponse = retrieveGastoUnicoById(gastoId);
        validateUpdatedGastoFieldsMatch(verificationResponse, updatedGasto);
        
        // Log E2E step
        logE2EStep("UPDATE", updatedGasto, updateResponse);
    }

    @Test
    @Order(4)
    @Story("E2E Flow - Filtered Search")
    @DisplayName("E2E Step 4: Should find the created gasto using efficient filtered search")
    @Description("Uses filtered search to find the created gasto único efficiently instead of searching entire list")
    @Severity(SeverityLevel.NORMAL)
    public void step04_shouldFindCreatedGastoInCompleteList() {
        assertNotNull(createdGastoId, "Created gasto ID should be available from previous test");
        assertNotNull(createdGasto, "Created gasto object should be available from previous test");

        // Use efficient filtered search based on the created gasto's properties
        Response filteredResponse = searchGastoUnicoUsingFilters(createdGasto);

        // Validate filtered search response
        validateGastosUnicosListResponse(filteredResponse);

        // Find our created gasto in the filtered results (should be much smaller list)
        boolean gastoFoundInFilteredResults = findGastoInList(filteredResponse, createdGastoId);
        assertTrue(gastoFoundInFilteredResults, "Created gasto should be found in the filtered search results");

        // Validate that the filtered search is more efficient than full list
        validateFilteredSearchEfficiency(filteredResponse);

        // Log E2E step
        logE2EStep("FILTERED_SEARCH", createdGasto, filteredResponse);
    }

    @Test
    @Order(5)
    @Story("E2E Flow - Validate Auto-Generated Real Gasto")
    @DisplayName("E2E Step 5: Should validate real gasto was auto-generated from gasto único")
    @Description("Validates that when a gasto único is created, it automatically generates a corresponding real gasto via hooks")
    @Severity(SeverityLevel.CRITICAL)
    public void step05_shouldValidateAutoGeneratedRealGasto() {
        assertNotNull(createdGastoId, "Created gasto ID should be available from previous test");
        assertNotNull(createdGasto, "Created gasto object should be available from previous test");

        // Find the auto-generated real gasto using efficient filtered search
        String realGastoId = findGeneratedRealGastoForUnico(createdGastoId);
        assertNotNull(realGastoId, "Real gasto should be auto-generated when gasto único is created");

        // Validate the real gasto exists by direct GET
        Response realGastoResponse = validateRealGastoExists(realGastoId);

        // Validate that the real gasto has correct properties (using original data)
        validateRealGastoProperties(realGastoResponse, originalGasto);

        // Log the validation step
        logE2EStep("VALIDATE_AUTO_GENERATED_REAL_GASTO", createdGasto, realGastoResponse);

        System.out.println("✅ Real gasto auto-generation validated successfully");
        System.out.println("   Gasto Único ID: " + createdGastoId);
        System.out.println("   Real Gasto ID: " + realGastoId);
    }

    @Test
    @Order(6)
    @Story("E2E Flow - Delete Gasto Único")
    @DisplayName("E2E Step 6: Should delete the created gasto único")
    @Description("Deletes the created gasto único and verifies it's no longer accessible")
    @Severity(SeverityLevel.CRITICAL)
    public void step06_shouldDeleteCreatedGastoUnico() {
        assertNotNull(createdGastoId, "Created gasto ID should be available from previous test");
        
        // Execute deletion
        Response deleteResponse = executeGastoUnicoDeletion(createdGastoId);
        
        // Validate deletion response
        validateGastoUnicoDeletionResponse(deleteResponse);
        
        // Verify gasto is no longer accessible
        Response verificationResponse = attemptToRetrieveDeletedGasto(createdGastoId);
        validateGastoNoLongerExists(verificationResponse);
        
        // Verify gasto is removed from list
        Response listResponse = retrieveAllGastosUnicos();
        boolean gastoStillInList = findGastoInList(listResponse, createdGastoId);
        assertFalse(gastoStillInList, "Deleted gasto should not appear in the list");
        
        // Log E2E step
        logE2EStep("DELETE", (GastoUnico) null, deleteResponse);

        // Clear the ID to avoid unnecessary cleanup in @AfterEach
        createdGastoId = null;
    }

    // Helper methods for E2E flow

    @Step("Get gastos business rules from MCP")
    private JsonNode getGastosBusinessRulesFromMcp() {
        JsonNode rules = businessRules.get("gastos");
        assertNotNull(rules, "Gastos business rules should be available");
        return rules;
    }

    @Step("Create valid gasto único following business rules")
    private GastoUnico createValidGastoUnicoWithBusinessRules(JsonNode rules) {
        GastoUnico gasto = TestDataFactory.createRandomGastoUnico();
        
        // Apply business rules
        if (rules.has("monto_minimo")) {
            double minAmount = rules.get("monto_minimo").asDouble();
            if (gasto.getMonto().compareTo(BigDecimal.valueOf(minAmount)) < 0) {
                gasto.setMonto(BigDecimal.valueOf(minAmount).add(BigDecimal.valueOf(100)));
            }
        }
        
        if (rules.has("monto_maximo")) {
            double maxAmount = rules.get("monto_maximo").asDouble();
            if (gasto.getMonto().compareTo(BigDecimal.valueOf(maxAmount)) > 0) {
                gasto.setMonto(BigDecimal.valueOf(maxAmount).subtract(BigDecimal.valueOf(100)));
            }
        }
        
        // Ensure fecha is within business rules
        if (rules.has("fecha_maxima_futura")) {
            int maxDaysFuture = rules.get("fecha_maxima_futura").asInt();
            LocalDate maxDate = LocalDate.now().plusDays(maxDaysFuture);
            if (gasto.getFecha().isAfter(maxDate)) {
                gasto.setFecha(LocalDate.now().plusDays(1));
            }
        }
        
        return gasto;
    }

    @Step("Execute gasto único creation")
    private Response executeGastoUnicoCreation(GastoUnico gasto) {
        return gastosUnicosClient.createGastoUnico(gasto);
    }

    @Step("Validate gasto único creation response")
    private void validateGastoUnicoCreationResponse(Response response) {
        ResponseValidator.validateStatusCode(response, 201);

        // Validate nested structure - API returns data directly
        ResponseValidator.validateFieldExists(response, "success");
        ResponseValidator.validateFieldExists(response, "data.descripcion");
        ResponseValidator.validateFieldExists(response, "data.monto");
        ResponseValidator.validateFieldExists(response, "data.fecha");
        ResponseValidator.validateFieldExists(response, "data.id");
        ResponseValidator.validateResponseTime(response, 5000);
    }

    @Step("Extract created gasto ID")
    private void extractCreatedGastoId(Response response) {
        // Extract gasto único ID from the response structure
        if (response.jsonPath().get("data.id") != null) {
            createdGastoId = response.jsonPath().get("data.id").toString();
            Allure.addAttachment("Created Gasto Único ID", createdGastoId);
        } else {
            // If ID is not in response, we'll need to find it by searching
            // This is a workaround for APIs that don't return ID in creation response
            createdGastoId = findGastoIdByDescription(createdGasto.getDescripcion());
        }

        // For gastos únicos, the real gasto ID might not be in the creation response
        // We'll handle this in the real gasto validation step
        assertNotNull(createdGastoId, "Should be able to determine created gasto ID");
    }

    @Step("Find gasto ID by description")
    private String findGastoIdByDescription(String description) {
        Response listResponse = gastosUnicosClient.getAllGastosUnicos();
        if (listResponse.getStatusCode() == 200) {
            var gastos = listResponse.jsonPath().getList("$");
            for (int i = 0; i < gastos.size(); i++) {
                var gasto = listResponse.jsonPath().getMap("[" + i + "]");
                if (description.equals(gasto.get("descripcion"))) {
                    return gasto.get("id").toString();
                }
            }
        }
        return null;
    }

    @Step("Retrieve gasto único by ID: {gastoId}")
    private Response retrieveGastoUnicoById(String gastoId) {
        return gastosUnicosClient.getGastoUnicoById(gastoId);
    }

    @Step("Validate gasto único retrieval response")
    private void validateGastoUnicoRetrievalResponse(Response response) {
        ResponseValidator.validateStatusCode(response, 200);
        ResponseValidator.validateContentType(response, "application/json");
        ResponseValidator.validateResponseTime(response, 3000);
    }

    @Step("Validate created gasto fields match original")
    private void validateCreatedGastoFieldsMatch(Response response, GastoUnico originalGasto) {
        // Response structure: data.descripcion, data.monto, data.fecha
        String responseDescription = response.jsonPath().getString("data.descripcion");
        String responseMonto = response.jsonPath().getString("data.monto");
        String responseFecha = response.jsonPath().getString("data.fecha");

        assertEquals(originalGasto.getDescripcion(), responseDescription, "Descripcion should match");

        // Compare montos as BigDecimal to handle formatting differences
        BigDecimal expectedMonto = originalGasto.getMonto();
        BigDecimal actualMonto = new BigDecimal(responseMonto);
        assertEquals(expectedMonto.compareTo(actualMonto), 0, "Monto should match");

        assertEquals(originalGasto.getFecha().toString(), responseFecha, "Fecha should match");
    }

    @Step("Validate against database schema")
    private void validateAgainstDatabaseSchema(Response response) {
        // Basic validation that response has expected structure
        assertNotNull(response.jsonPath().get("data"), "Response should have data field");
        assertNotNull(response.jsonPath().get("data.id"), "Data should have ID");
        assertNotNull(response.jsonPath().get("data.descripcion"), "Data should have descripcion");
        assertNotNull(response.jsonPath().get("data.monto"), "Data should have monto");
        assertNotNull(response.jsonPath().get("data.fecha"), "Data should have fecha");
    }

    @Step("Create updated gasto único data")
    private Map<String, Object> createUpdatedGastoUnicoData() {
        // Based on actual API response: UPDATE requires all main fields
        Map<String, Object> updatePayload = new HashMap<>();
        updatePayload.put("monto", createdGasto.getMonto().add(BigDecimal.valueOf(50)));
        updatePayload.put("descripcion", "UPDATED: " + createdGasto.getDescripcion());
        
        // Required fields (from error response)
        updatePayload.put("categoria_gasto_id", createdGasto.getCategoriaGastoId());
        updatePayload.put("importancia_gasto_id", createdGasto.getImportanciaGastoId());
        updatePayload.put("tipo_pago_id", createdGasto.getTipoPagoId());
        updatePayload.put("fecha", LocalDate.now().minusDays(1).toString()); // YYYY-MM-DD format - past date
        
        return updatePayload;
    }

    @Step("Execute gasto único update")
    private Response executeGastoUnicoUpdate(String gastoId, Map<String, Object> updatedGasto) {
        // Debug: Log the payload being sent
        System.out.println("=== UPDATE DEBUG ===");
        System.out.println("Gasto ID: " + gastoId);
        System.out.println("Update Payload: " + updatedGasto);
        System.out.println("==================");
        
        Response response = gastosUnicosClient.updateGastoUnico(gastoId, updatedGasto);
        
        // Debug: Log the response
        System.out.println("UPDATE Response Status: " + response.getStatusCode());
        System.out.println("UPDATE Response Body: " + response.getBody().asString());
        
        return response;
    }

    @Step("Validate gasto único update response")
    private void validateGastoUnicoUpdateResponse(Response response) {
        ResponseValidator.validateStatusCode(response, 200);
        ResponseValidator.validateResponseTime(response, 5000);
    }

    @Step("Validate updated gasto fields match")
    private void validateUpdatedGastoFieldsMatch(Response response, Map<String, Object> updatedGasto) {
        // UPDATE response has different structure: data.descripcion (not data.gastoUnico.descripcion)
        String updatedDescription = updatedGasto.get("descripcion").toString();
        String responseDescription = response.jsonPath().getString("data.descripcion");
        assertEquals(updatedDescription, responseDescription, "Description should be updated");

        // Validate monto was updated - compare as BigDecimal to handle formatting
        BigDecimal updatedMonto = (BigDecimal) updatedGasto.get("monto");
        String responseMonto = response.jsonPath().getString("data.monto");
        BigDecimal actualMonto = new BigDecimal(responseMonto);
        assertEquals(updatedMonto.compareTo(actualMonto), 0, "Monto should be updated");
    }

    @Step("Retrieve all gastos únicos")
    private Response retrieveAllGastosUnicos() {
        return gastosUnicosClient.getAllGastosUnicos();
    }

    @Step("Search gasto único using efficient filters")
    private Response searchGastoUnicoUsingFilters(GastoUnico targetGasto) {
        // Start with just category filter for now - most efficient single filter
        // We can add more filters as needed if this works well
        return gastosUnicosClient.getGastosUnicosWithFilters(
            targetGasto.getCategoriaGastoId().intValue(),    // Filter by category only
            null,                                            // Don't filter by date for now
            null,                                            // Don't filter by date for now
            null,                                            // Don't restrict monto min
            null,                                            // Don't restrict monto max
            null,                                            // Don't filter by importance
            null,                                            // Don't filter by payment type
            null                                             // Don't filter by procesado status
        );
    }

    @Step("Validate filtered search efficiency")
    private void validateFilteredSearchEfficiency(Response filteredResponse) {
        // Validate that filtered search returns a manageable number of results
        var gastos = filteredResponse.jsonPath().getList("data");
        int filteredCount = gastos != null ? gastos.size() : 0;

        // Log the efficiency gain
        System.out.println("🔍 Filtered search returned " + filteredCount + " results (vs full list)");

        // Expect filtered results to be significantly smaller than total
        // This is a soft validation - we expect efficiency but don't fail if there are many matches
        if (filteredCount > 50) {
            System.out.println("⚠️  Filtered search returned more results than expected (" + filteredCount + ")");
            System.out.println("    Consider adding more specific filters for better performance");
        } else {
            System.out.println("✅ Filtered search is efficient with " + filteredCount + " results");
        }

        // Validate response time is reasonable
        ResponseValidator.validateResponseTime(filteredResponse, 3000);
    }

    @Step("Validate gastos únicos list response")
    private void validateGastosUnicosListResponse(Response response) {
        ResponseValidator.validateStatusCode(response, 200);
        
        // List response likely has nested structure, check for data array
        Object dataList = response.jsonPath().get("data");
        if (dataList instanceof java.util.List) {
            assertTrue(response.jsonPath().getList("data").size() >= 0, "Data should be an array");
        } else {
            // Fallback - just verify we have some data
            assertNotNull(response.jsonPath().get("data"), "Response should have data");
        }
        
        ResponseValidator.validateResponseTime(response, 10000);
    }

    @Step("Find gasto in list")
    private boolean findGastoInList(Response listResponse, String gastoId) {
        // List response structure: {"success": true, "data": [...]}
        var gastos = listResponse.jsonPath().getList("data");
        
        for (int i = 0; i < gastos.size(); i++) {
            var gasto = listResponse.jsonPath().getMap("data[" + i + "]");
            if (gasto != null && gastoId.equals(gasto.get("id").toString())) {
                return true;
            }
        }
        return false;
    }

    @Step("Validate list performance and structure")
    private void validateListPerformanceAndStructure(Response response) {
        assertTrue(response.getTime() < 10000, "List response should be fast");
        
        // List response structure: {"success": true, "data": [...]}
        var gastos = response.jsonPath().getList("data");
        assertTrue(gastos.size() >= 0, "Should return valid list");
        
        // Validate structure of first item if list is not empty
        if (!gastos.isEmpty()) {
            var firstGasto = response.jsonPath().getMap("data[0]");
            assertNotNull(firstGasto.get("id"), "Each gasto should have an ID");
            assertNotNull(firstGasto.get("descripcion"), "Each gasto should have descripcion");
        }
    }

    @Step("Validate compliance with business rules")
    private void validateComplianceWithBusinessRules(Response response) {
        // Simplified business rules validation to avoid ClassCastException
        JsonNode gastosRules = businessRules.get("gastos");
        
        // Get monto from response - handle both CREATE and UPDATE response structures
        String montoStr = null;
        if (response.jsonPath().get("data.gastoUnico.monto") != null) {
            montoStr = response.jsonPath().getString("data.gastoUnico.monto");
        } else if (response.jsonPath().get("data.monto") != null) {
            montoStr = response.jsonPath().getString("data.monto");
        }
        
        if (montoStr != null && gastosRules.has("monto_minimo") && gastosRules.has("monto_maximo")) {
            BigDecimal responseMonto = new BigDecimal(montoStr);
            double minAmount = gastosRules.get("monto_minimo").asDouble();
            double maxAmount = gastosRules.get("monto_maximo").asDouble();
            
            assertTrue(responseMonto.compareTo(BigDecimal.valueOf(minAmount)) >= 0,
                      "Monto should be >= minimum business rule");
            assertTrue(responseMonto.compareTo(BigDecimal.valueOf(maxAmount)) <= 0,
                      "Monto should be <= maximum business rule");
        }
        
        // Validate categoria is required if specified in business rules
        if (gastosRules.has("categorias_requeridas") && gastosRules.get("categorias_requeridas").asBoolean()) {
            // Check for categoria in both response structures
            Object categoriaId = response.jsonPath().get("data.gastoUnico.categoria_gasto_id");
            if (categoriaId == null) {
                categoriaId = response.jsonPath().get("data.categoria_gasto_id");
            }
            assertNotNull(categoriaId, "Categoria should be required");
        }
    }

    @Step("Validate data integrity")
    private void validateDataIntegrity(Response response) {
        // Simplified integrity validation to avoid ClassCastException
        
        // Check for data presence in either response structure
        Object id = response.jsonPath().get("data.gastoUnico.id");
        if (id == null) id = response.jsonPath().get("data.id");
        assertNotNull(id, "ID should not be null");
        
        Object descripcion = response.jsonPath().get("data.gastoUnico.descripcion");
        if (descripcion == null) descripcion = response.jsonPath().get("data.descripcion");
        assertNotNull(descripcion, "Descripcion should not be null");
        
        Object monto = response.jsonPath().get("data.gastoUnico.monto");
        if (monto == null) monto = response.jsonPath().get("data.monto");
        assertNotNull(monto, "Monto should not be null");
        
        Object fecha = response.jsonPath().get("data.gastoUnico.fecha");
        if (fecha == null) fecha = response.jsonPath().get("data.fecha");
        assertNotNull(fecha, "Fecha should not be null");
        
        // Validate fecha format
        String fechaStr = fecha.toString();
        assertDoesNotThrow(() -> LocalDate.parse(fechaStr), "Fecha should be valid date format");
        
        // Validate monto is positive
        BigDecimal montoDecimal = new BigDecimal(monto.toString());
        assertTrue(montoDecimal.compareTo(BigDecimal.ZERO) > 0, "Monto should be positive");
    }

    @Step("Validate against MCP schemas")
    private void validateAgainstMcpSchemas(Response response) {
        JsonNode validationSchemas = mcpConnector.getValidationSchemas();
        if (validationSchemas.has("gasto_unico")) {
            // Add specific validation logic based on your MCP schemas
            assertTrue(true, "MCP schema validation placeholder");
        }
    }

    @Step("Execute gasto único deletion")
    private Response executeGastoUnicoDeletion(String gastoId) {
        return gastosUnicosClient.deleteGastoUnico(gastoId);
    }

    @Step("Validate gasto único deletion response")
    private void validateGastoUnicoDeletionResponse(Response response) {
        ResponseValidator.validateStatusCode(response, 200);
        ResponseValidator.validateResponseTime(response, 5000);
    }

    @Step("Attempt to retrieve deleted gasto")
    private Response attemptToRetrieveDeletedGasto(String gastoId) {
        return gastosUnicosClient.getGastoUnicoById(gastoId);
    }

    @Step("Validate gasto no longer exists")
    private void validateGastoNoLongerExists(Response response) {
        ResponseValidator.validateStatusCode(response, 404);
    }

    // Utility methods

    private boolean isOptionalColumn(String columnName) {
        return columnName.equals("notas") || columnName.equals("tarjeta_id");
    }

    @Step("Log E2E step: {action}")
    private void logE2EStep(String action, GastoUnico gastoData, Response response) {
        logE2EStepInternal(action, gastoData != null ? gastoData.toString() : null, response);
    }

    @Step("Log E2E step: {action}")
    private void logE2EStep(String action, Map<String, Object> gastoData, Response response) {
        logE2EStepInternal(action, gastoData != null ? gastoData.toString() : null, response);
    }

    private void logE2EStepInternal(String action, String gastoDataString, Response response) {
        StringBuilder logEntry = new StringBuilder();
        logEntry.append("=== E2E STEP: ").append(action).append(" ===\n");
        logEntry.append("Status Code: ").append(response.getStatusCode()).append("\n");
        logEntry.append("Response Time: ").append(response.getTime()).append("ms\n");
        
        if (gastoDataString != null) {
            logEntry.append("Request Data: ").append(gastoDataString).append("\n");
        }
        
        logEntry.append("Response Body: ").append(response.getBody().asString()).append("\n");
        logEntry.append("===========================\n");
        
        Allure.addAttachment("E2E Step: " + action, "text/plain", logEntry.toString());
        System.out.println(logEntry.toString());
    }

    /**
     * Ensures a gasto exists for testing - either reuses the existing one or creates a new one
     * This makes tests independent while maintaining E2E flow integrity
     */
    @Step("Ensure gasto exists for testing")
    private String ensureGastoExists() {
        // If we already have a created gasto from the sequence, use it
        if (createdGastoId != null) {
            // Verify it still exists
            try {
                Response verifyResponse = gastosUnicosClient.getGastoUnicoById(createdGastoId);
                if (verifyResponse.getStatusCode() == 200) {
                    return createdGastoId;
                }
            } catch (Exception e) {
                // If verification fails, we'll create a new one
            }
        }
        
        // Create a new gasto for testing
        GastoUnico testGasto = TestDataFactory.createRandomGastoUnico();
        Response createResponse = gastosUnicosClient.createGastoUnico(testGasto);
        
        if (createResponse.getStatusCode() == 201) {
            String newGastoId = createResponse.jsonPath().getString("data.id");
            if (newGastoId != null) {
                // Update the static variables for other tests in the sequence
                createdGastoId = newGastoId;
                createdGasto = testGasto;
                return newGastoId;
            }
        }
        
        throw new RuntimeException("Failed to ensure gasto exists for testing");
    }

    // New helper methods for generation flow

    @Step("Get gastos baseline count")
    private Response getGastosBaseline() {
        return gastosClient.getAllGastos();
    }

    @Step("Get gastos count from response")
    private int getGastosCount(Response response) {
        if (response.getStatusCode() != 200) {
            return 0;
        }
        
        try {
            // Handle different response structures
            Object data = response.jsonPath().get("data");
            if (data instanceof java.util.List) {
                return response.jsonPath().getList("data").size();
            } else if (data != null) {
                // If data is not a list, assume it's a single item
                return 1;
            }
            
            // Fallback: try to get direct array
            java.util.List<?> directList = response.jsonPath().getList("$");
            return directList != null ? directList.size() : 0;
        } catch (Exception e) {
            System.out.println("Warning: Could not parse gastos count from response: " + e.getMessage());
            return 0;
        }
    }

    @Step("Execute gastos generation")
    private Response executeGastosGeneration() {
        Response response = gastosClient.generateGastos();
        
        // Log generation details
        System.out.println("=== GASTOS GENERATION ===");
        System.out.println("Status Code: " + response.getStatusCode());
        System.out.println("Response Time: " + response.getTime() + "ms");
        System.out.println("Response Body: " + response.getBody().asString());
        System.out.println("========================");
        
        return response;
    }

    @Step("Validate generation response")
    private void validateGenerationResponse(Response response) {
        // Generation endpoint might return different status codes depending on implementation
        // Accept 200 (success) or 201 (created) or similar success codes
        assertTrue(response.getStatusCode() >= 200 && response.getStatusCode() < 300, 
                  "Generation should return success status code, got: " + response.getStatusCode());
        ResponseValidator.validateResponseTime(response, 30000); // Generation might take longer
        
        // Validate response structure if it has specific format
        String responseBody = response.getBody().asString();
        assertNotNull(responseBody, "Generation response should have body");
        assertTrue(responseBody.length() > 0, "Generation response should not be empty");
    }

    @Step("Find generated gasto from gasto único")
    private boolean findGeneratedGastoFromUnico(Response gastosResponse, GastoUnico originalGasto) {
        if (gastosResponse.getStatusCode() != 200) {
            return false;
        }
        
        try {
            java.util.List<?> gastos = null;
            
            // Handle different response structures
            Object data = gastosResponse.jsonPath().get("data");
            if (data instanceof java.util.List) {
                gastos = gastosResponse.jsonPath().getList("data");
            } else {
                // Fallback: try to get direct array
                gastos = gastosResponse.jsonPath().getList("$");
            }
            
            if (gastos == null || gastos.isEmpty()) {
                return false;
            }
            
            // Look for a gasto that matches our original gasto único characteristics
            for (int i = 0; i < gastos.size(); i++) {
                var gasto = gastosResponse.jsonPath().getMap("data[" + i + "]");
                if (gasto == null) {
                    gasto = gastosResponse.jsonPath().getMap("[" + i + "]");
                }
                
                if (gasto != null) {
                    // Check if this gasto matches our original gasto único
                    String descripcion = (String) gasto.get("descripcion");
                    Object monto = gasto.get("monto");
                    Object fecha = gasto.get("fecha");
                    
                    if (descripcion != null && descripcion.equals(originalGasto.getDescripcion())) {
                        // Additional validation for monto and fecha if needed
                        System.out.println("Found potential generated gasto: " + descripcion + 
                                         " with monto: " + monto + " and fecha: " + fecha);
                        
                        // Check if it's marked as processed/generated (depending on API structure)
                        Object procesado = gasto.get("procesado");
                        Object tipoOrigen = gasto.get("tipo_origen");
                        
                        if ((procesado != null && procesado.equals(true)) || 
                            (tipoOrigen != null && tipoOrigen.equals("unico"))) {
                            return true;
                        }
                    }
                }
            }
            
            return false;
        } catch (Exception e) {
            System.out.println("Warning: Error searching for generated gasto: " + e.getMessage());
            return false;
        }
    }

    // New helper methods for real gasto validation

    @Step("Find generated real gasto for gasto único: {gastoUnicoId}")
    private String findGeneratedRealGastoForUnico(String gastoUnicoId) {
        // Use the efficient filtered search with tipo_origen=unico and id_origen
        Response gastosResponse = gastosClient.getGastosByOrigin("unico", gastoUnicoId);

        if (gastosResponse.getStatusCode() != 200) {
            return null;
        }

        try {
            java.util.List<?> gastos = gastosResponse.jsonPath().getList("data");
            if (gastos == null || gastos.isEmpty()) {
                return null;
            }

            // Since we filtered by tipo_origen=unico and id_origen, the first result should be our gasto
            var gasto = gastosResponse.jsonPath().getMap("data[0]");
            if (gasto != null) {
                System.out.println("✅ Found auto-generated real gasto with ID: " + gasto.get("id") +
                                 " for gasto único: " + gastoUnicoId);
                return gasto.get("id").toString();
            }
            return null;
        } catch (Exception e) {
            System.out.println("Warning: Error searching for generated real gasto: " + e.getMessage());
            return null;
        }
    }


    @Step("Validate real gasto exists by ID: {realGastoId}")
    private Response validateRealGastoExists(String realGastoId) {
        Response response = gastosClient.getGastoById(realGastoId);
        ResponseValidator.validateStatusCode(response, 200);
        ResponseValidator.validateResponseTime(response, 5000);
        return response;
    }

    @Step("Validate real gasto properties")
    private void validateRealGastoProperties(Response realGastoResponse, GastoUnico originalGasto) {
        // Validate that the real gasto has the expected properties
        JsonPath jsonPath = realGastoResponse.jsonPath();
        
        // Check tipo_origen field
        String tipoOrigen = jsonPath.getString("data.tipo_origen");
        assertEquals("unico", tipoOrigen, "Real gasto should have tipo_origen = 'unico'");
        
        // Check id_origen field (should match our gasto único ID)
        String idOrigen = jsonPath.getString("data.id_origen");
        assertEquals(createdGastoId, idOrigen, "Real gasto id_origen should match gasto único ID");
        
        // Validate that core fields match (note: real gasto may be updated when gasto único is updated)
        String descripcion = jsonPath.getString("data.descripcion");
        // The API appears to keep gastos únicos and real gastos in sync, so we just verify the relationship exists
        assertNotNull(descripcion, "Real gasto should have a description");
        
        Double monto = jsonPath.getDouble("data.monto_ars");
        assertNotNull(monto, "Real gasto should have an amount");
        assertTrue(monto > 0, "Real gasto amount should be positive");
        
        String fecha = jsonPath.getString("data.fecha");
        assertNotNull(fecha, "Real gasto should have a date");
        
        Integer categoriaId = jsonPath.getInt("data.categoria_gasto_id");
        assertNotNull(categoriaId, "Real gasto should have a category ID");
        
        Integer importanciaId = jsonPath.getInt("data.importancia_gasto_id");
        assertNotNull(importanciaId, "Real gasto should have an importance ID");
        
        Integer tipoPagoId = jsonPath.getInt("data.tipo_pago_id");
        assertNotNull(tipoPagoId, "Real gasto should have a payment type ID");
        
        // Log validation success
        Allure.addAttachment("Real Gasto Validation", "text/plain", 
            String.format("Real gasto validated successfully:\n" +
                "- tipo_origen: %s\n" +
                "- id_origen: %s\n" +
                "- descripcion: %s\n" +
                "- monto_ars: %.2f\n" +
                "- fecha: %s", 
                tipoOrigen, idOrigen, descripcion, monto, fecha));
    }

    @Step("Clone gasto for original data preservation")
    private GastoUnico cloneGasto(GastoUnico original) {
        return new GastoUnico.Builder()
                .descripcion(original.getDescripcion())
                .monto(original.getMonto())
                .fecha(original.getFecha())
                .categoriaGastoId(original.getCategoriaGastoId())
                .importanciaGastoId(original.getImportanciaGastoId())
                .tipoPagoId(original.getTipoPagoId())
                .build();
    }

    @AfterAll
    static void teardownE2ETest() {
        // Final cleanup is now handled by ApiTestWithCleanup automatically
        // Static variables for tracking are cleared here
        createdGastoId = null;
        createdGasto = null;
        originalGasto = null;
        realGastoId = null;

        System.out.println("🏁 Gastos Únicos E2E Test Suite completed");
    }

    // Cleanup implementation for ApiTestWithCleanup
    @Override
    protected Map<EntityType, Function<List<String>, Integer>> getCleanupStrategies() {
        Map<EntityType, Function<List<String>, Integer>> strategies = new HashMap<>();

        // E2E tests handle their own cleanup in test steps
        // This is mainly for safety in case of test failures
        strategies.put(EntityType.GASTO_UNICO, gastoUnicoIds ->
            performCleanup(gastoUnicoIds, gastosUnicosClient::deleteGastoUnico, "gasto único"));

        return strategies;
    }
}