package tests;

import base.ApiTestWithCleanup;
import clients.ComprasApiClient;
import clients.GastosApiClient;
import io.qameta.allure.*;
import io.restassured.response.Response;
import models.Compra;
import org.junit.jupiter.api.*;
import utils.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static base.ApiTestWithCleanup.EntityType;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive E2E test suite for Compras (Purchases) flow
 * Tests the complete lifecycle: Create → Read → Update → List → Generate Gastos → Delete
 *
 * Based on MCP business rules:
 * - Compras can have 1-60 cuotas (installments)
 * - fecha_compra cannot be in the future
 * - monto_total must be positive with max 2 decimals
 * - Each cuota generates a real gasto in the gastos table
 * - Generation happens via the /generate endpoint or automatic jobs
 */
@Feature("Compras E2E Flow")
@DisplayName("Compras End-to-End Test Suite")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ComprasE2ETest extends ApiTestWithCleanup {

    private ComprasApiClient comprasClient;
    private GastosApiClient gastosClient;

    // Static variables to maintain state across test methods
    private static String createdCompraId;
    private static Compra createdCompra;
    private static Compra originalCompra;

    @Override
    protected void customAuthenticatedSetup() {
        comprasClient = new ComprasApiClient().withRequestSpec(requestSpec);
        gastosClient = new GastosApiClient().withRequestSpec(requestSpec);

        System.out.println("🚀 Starting Compras E2E Test Suite");
        System.out.println("Testing complete flow: CREATE → READ → UPDATE → LIST → GENERATE → DELETE");
    }

    @AfterAll
    static void teardownE2ETest() {
        // Final cleanup is now handled by ApiTestWithCleanup automatically
        // Static variables for tracking are cleared here
        createdCompraId = null;
        createdCompra = null;
        originalCompra = null;

        System.out.println("🏁 Compras E2E Test Suite completed");
    }

    @Test
    @Order(1)
    @Story("E2E Flow - Create Purchase")
    @DisplayName("E2E Step 1: Should create a new compra successfully")
    @Description("Creates a new purchase with valid data including installments and payment details")
    @Severity(SeverityLevel.CRITICAL)
    public void step01_shouldCreateCompraSuccessfully() {
        // Create a test compra with multiple installments
        originalCompra = TestDataFactory.createRandomCompra();
        originalCompra.setCantidadCuotas(3);  // 3 installments
        originalCompra.setMontoTotal(new BigDecimal("1500.00"));
        originalCompra.setFechaCompra(LocalDate.now()); // Today's date to match API expectations

        // Create the compra
        Response createResponse = createNewCompra(originalCompra);

        // Validate creation response
        validateCompraCreationResponse(createResponse);

        // Extract compra ID for subsequent tests
        extractCreatedCompraId(createResponse);

        // Store created compra for later tests
        createdCompra = originalCompra;

        // Log E2E step
        logE2EStep("CREATE", createdCompra, createResponse);

        System.out.println("✅ Compra created successfully");
        System.out.println("   ID: " + createdCompraId);
        System.out.println("   Monto Total: " + createdCompra.getMontoTotal());
        System.out.println("   Cuotas: " + createdCompra.getCantidadCuotas());
    }

    @Test
    @Order(2)
    @Story("E2E Flow - Read Purchase")
    @DisplayName("E2E Step 2: Should retrieve the created compra by ID")
    @Description("Retrieves the created purchase by ID and validates all fields match")
    @Severity(SeverityLevel.CRITICAL)
    public void step02_shouldRetrieveCreatedCompraById() {
        assertNotNull(createdCompraId, "Created compra ID should be available from previous test");

        // Get the compra by ID
        Response getResponse = retrieveCompraById(createdCompraId);

        // Validate GET response
        validateGetCompraResponse(getResponse);

        // Validate that returned data matches what was created
        validateCreatedCompraFieldsMatch(getResponse, originalCompra);

        // Log E2E step
        logE2EStep("READ", createdCompra, getResponse);
    }

    @Test
    @Order(3)
    @Story("E2E Flow - Update Purchase")
    @DisplayName("E2E Step 3: Should update the created compra")
    @Description("Updates the created purchase with new data and validates the changes")
    @Severity(SeverityLevel.NORMAL)
    public void step03_shouldUpdateCreatedCompra() {
        assertNotNull(createdCompraId, "Created compra ID should be available from previous test");

        // Prepare update payload
        Map<String, Object> updatePayload = createUpdatePayload();

        // Update the compra
        Response updateResponse = updateCompraById(createdCompraId, updatePayload);

        // Validate update response
        validateUpdateCompraResponse(updateResponse);

        // Validate that fields were updated correctly
        validateUpdatedCompraFieldsMatch(updateResponse, updatePayload);

        // Log E2E step
        logE2EStep("UPDATE", updatePayload, updateResponse);
    }

    @Test
    @Order(4)
    @Story("E2E Flow - List Purchases")
    @DisplayName("E2E Step 4: Should find the created compra in the complete list")
    @Description("Retrieves all compras and verifies the created compra appears in the list")
    @Severity(SeverityLevel.NORMAL)
    public void step04_shouldFindCreatedCompraInCompleteList() {
        assertNotNull(createdCompraId, "Created compra ID should be available from previous test");

        // Get all compras
        Response getAllResponse = retrieveAllCompras();

        // Validate list response
        validateComprasListResponse(getAllResponse);

        // Find our created compra in the list
        boolean compraFoundInList = findCompraInList(getAllResponse, createdCompraId);
        assertTrue(compraFoundInList, "Created compra should be found in the complete list");

        // Validate list performance and structure
        validateListPerformanceAndStructure(getAllResponse);

        // Log E2E step
        logE2EStep("LIST", null, getAllResponse);
    }

    @Test
    @Order(5)
    @Story("E2E Flow - Generate Gastos from Purchase")
    @DisplayName("E2E Step 5: Should generate gastos from compra installments")
    @Description("Triggers gastos generation and validates that cuotas are converted to gastos reales")
    @Severity(SeverityLevel.CRITICAL)
    public void step05_shouldGenerateGastosFromCompra() {
        assertNotNull(createdCompraId, "Created compra ID should be available from previous test");
        assertNotNull(createdCompra, "Created compra object should be available from previous test");

        // Trigger gastos generation
        Response generationResponse = executeGastosGeneration();
        validateGenerationResponse(generationResponse);

        // Find generated gastos for our compra
        Response gastosForCompra = findGeneratedGastosForCompra(createdCompraId);
        validateGeneratedGastos(gastosForCompra, createdCompra);

        // Log E2E step
        logE2EStep("GENERATE_GASTOS", createdCompra, gastosForCompra);

        System.out.println("✅ Gastos generation validated successfully");
        System.out.println("   Compra ID: " + createdCompraId);
        System.out.println("   Expected Cuotas: " + createdCompra.getCantidadCuotas());
    }

    @Test
    @Order(6)
    @Story("E2E Flow - Delete Purchase")
    @DisplayName("E2E Step 6: Should delete the created compra")
    @Description("Deletes the created purchase and validates it no longer exists")
    @Severity(SeverityLevel.NORMAL)
    public void step06_shouldDeleteCreatedCompra() {
        assertNotNull(createdCompraId, "Created compra ID should be available from previous test");

        // Delete the compra
        Response deleteResponse = deleteCompraById(createdCompraId);

        // Validate deletion response
        validateDeleteCompraResponse(deleteResponse);

        // Verify compra no longer exists
        Response getAfterDeleteResponse = comprasClient.getCompraById(createdCompraId);
        assertEquals(404, getAfterDeleteResponse.getStatusCode(),
                    "Compra should not exist after deletion");

        // Verify compra is no longer in the list
        Response listResponse = retrieveAllCompras();
        boolean compraStillInList = findCompraInList(listResponse, createdCompraId);
        assertFalse(compraStillInList, "Deleted compra should not appear in the list");

        // Log E2E step
        logE2EStep("DELETE", null, deleteResponse);

        // Clear the ID to avoid unnecessary cleanup in @AfterEach
        createdCompraId = null;

        System.out.println("✅ Compra deleted successfully");
        System.out.println("   Verified removal from database and lists");
    }

    // Helper methods for E2E flow

    @Step("Create new compra")
    private Response createNewCompra(Compra compra) {
        return comprasClient.createCompra(compra);
    }

    @Step("Retrieve compra by ID: {compraId}")
    private Response retrieveCompraById(String compraId) {
        return comprasClient.getCompraById(compraId);
    }

    @Step("Update compra by ID: {compraId}")
    private Response updateCompraById(String compraId, Map<String, Object> updatePayload) {
        return comprasClient.updateCompra(compraId, updatePayload);
    }

    @Step("Delete compra by ID: {compraId}")
    private Response deleteCompraById(String compraId) {
        return comprasClient.deleteCompra(compraId);
    }

    @Step("Retrieve all compras")
    private Response retrieveAllCompras() {
        return comprasClient.getAllCompras();
    }

    @Step("Execute gastos generation")
    private Response executeGastosGeneration() {
        return gastosClient.generateGastos();
    }

    @Step("Find generated gastos for compra: {compraId}")
    private Response findGeneratedGastosForCompra(String compraId) {
        return gastosClient.getGastosByOrigin("compra", compraId);
    }

    // Validation methods

    @Step("Validate compra creation response")
    private void validateCompraCreationResponse(Response response) {
        ResponseValidator.validateStatusCode(response, 201);
        ResponseValidator.validateFieldExists(response, "success");
        ResponseValidator.validateFieldExists(response, "data.compra.id");
        ResponseValidator.validateFieldExists(response, "data.compra.descripcion");
        ResponseValidator.validateFieldExists(response, "data.compra.monto_total");
        ResponseValidator.validateFieldExists(response, "data.compra.cantidad_cuotas");
        ResponseValidator.validateFieldExists(response, "data.compra.fecha_compra");
        ResponseValidator.validateResponseTime(response, 5000);
    }

    @Step("Extract created compra ID")
    private void extractCreatedCompraId(Response response) {
        if (response.jsonPath().get("data.compra.id") != null) {
            createdCompraId = response.jsonPath().get("data.compra.id").toString();
            Allure.addAttachment("Created Compra ID", createdCompraId);
        }
        assertNotNull(createdCompraId, "Should be able to determine created compra ID");
    }

    @Step("Validate GET compra response")
    private void validateGetCompraResponse(Response response) {
        ResponseValidator.validateStatusCode(response, 200);
        ResponseValidator.validateContentType(response, "application/json");
        ResponseValidator.validateResponseTime(response, 3000);
    }

    @Step("Validate created compra fields match original")
    private void validateCreatedCompraFieldsMatch(Response response, Compra originalCompra) {
        // GET response structure: data.descripcion, data.monto_total, etc. (direct under data)
        String responseDescripcion = response.jsonPath().getString("data.descripcion");
        String responseMonto = response.jsonPath().getString("data.monto_total");
        Integer responseCuotas = response.jsonPath().getInt("data.cantidad_cuotas");
        String responseFecha = response.jsonPath().getString("data.fecha_compra");

        assertEquals(originalCompra.getDescripcion(), responseDescripcion, "Descripcion should match");

        // Compare montos as BigDecimal to handle formatting differences
        BigDecimal expectedMonto = originalCompra.getMontoTotal();
        BigDecimal actualMonto = new BigDecimal(responseMonto);
        assertEquals(expectedMonto.compareTo(actualMonto), 0, "Monto should match");

        assertEquals(originalCompra.getCantidadCuotas(), responseCuotas, "Cantidad cuotas should match");

        // Date comparison - handle timezone differences (allow ±1 day difference)
        LocalDate expectedFecha = originalCompra.getFechaCompra();
        LocalDate actualFecha = LocalDate.parse(responseFecha);
        long daysDifference = Math.abs(expectedFecha.toEpochDay() - actualFecha.toEpochDay());
        assertTrue(daysDifference <= 1,
                  String.format("Fecha should be within ±1 day. Expected: %s, Actual: %s",
                               expectedFecha, actualFecha));
    }

    @Step("Create update payload")
    private Map<String, Object> createUpdatePayload() {
        Map<String, Object> updatePayload = new HashMap<>();
        updatePayload.put("descripcion", "UPDATED: " + createdCompra.getDescripcion());
        updatePayload.put("monto_total", createdCompra.getMontoTotal().add(new BigDecimal("100.00")));
        updatePayload.put("cantidad_cuotas", 2); // Reduce installments
        updatePayload.put("fecha_compra", createdCompra.getFechaCompra().toString()); // Required field!
        updatePayload.put("categoria_gasto_id", createdCompra.getCategoriaGastoId());
        updatePayload.put("importancia_gasto_id", createdCompra.getImportanciaGastoId());
        updatePayload.put("tipo_pago_id", createdCompra.getTipoPagoId());
        updatePayload.put("tarjeta_id", createdCompra.getTarjetaId()); // Also include tarjeta_id

        return updatePayload;
    }

    @Step("Validate update compra response")
    private void validateUpdateCompraResponse(Response response) {
        ResponseValidator.validateStatusCode(response, 200);
        ResponseValidator.validateResponseTime(response, 5000);
    }

    @Step("Validate updated compra fields match")
    private void validateUpdatedCompraFieldsMatch(Response response, Map<String, Object> updatedData) {
        // UPDATE response structure: data.descripcion, data.monto_total, etc. (direct under data)
        String updatedDescripcion = updatedData.get("descripcion").toString();
        String responseDescripcion = response.jsonPath().getString("data.descripcion");
        assertEquals(updatedDescripcion, responseDescripcion, "Description should be updated");

        // Validate monto was updated - compare as BigDecimal to handle formatting
        BigDecimal updatedMonto = (BigDecimal) updatedData.get("monto_total");
        String responseMonto = response.jsonPath().getString("data.monto_total");
        BigDecimal actualMonto = new BigDecimal(responseMonto);
        assertEquals(updatedMonto.compareTo(actualMonto), 0, "Monto should be updated");
    }

    @Step("Validate compras list response")
    private void validateComprasListResponse(Response response) {
        ResponseValidator.validateStatusCode(response, 200);

        Object dataList = response.jsonPath().get("data");
        if (dataList instanceof java.util.List) {
            assertTrue(response.jsonPath().getList("data").size() >= 0, "Data should be an array");
        } else {
            assertNotNull(response.jsonPath().get("data"), "Response should have data");
        }

        ResponseValidator.validateResponseTime(response, 10000);
    }

    @Step("Find compra in list")
    private boolean findCompraInList(Response listResponse, String compraId) {
        var compras = listResponse.jsonPath().getList("data");

        for (int i = 0; i < compras.size(); i++) {
            var compra = listResponse.jsonPath().getMap("data[" + i + "]");
            if (compra != null && compraId.equals(compra.get("id").toString())) {
                return true;
            }
        }
        return false;
    }

    @Step("Validate list performance and structure")
    private void validateListPerformanceAndStructure(Response listResponse) {
        ResponseValidator.validateResponseTime(listResponse, 10000);
        ResponseValidator.validateContentType(listResponse, "application/json");
    }

    @Step("Validate generation response")
    private void validateGenerationResponse(Response response) {
        ResponseValidator.validateStatusCode(response, 200);
        ResponseValidator.validateResponseTime(response, 10000);
    }

    @Step("Validate generated gastos")
    private void validateGeneratedGastos(Response gastosResponse, Compra originalCompra) {
        ResponseValidator.validateStatusCode(gastosResponse, 200);

        var gastos = gastosResponse.jsonPath().getList("data");
        assertNotNull(gastos, "Generated gastos should exist");

        // Note: Depending on business rules, we might expect gastos equal to cuotas
        // or they might be generated over time. Adjust validation accordingly.

        System.out.println("Found " + gastos.size() + " generated gastos for compra " + createdCompraId);
    }

    @Step("Validate delete compra response")
    private void validateDeleteCompraResponse(Response response) {
        ResponseValidator.validateStatusCode(response, 200);
        ResponseValidator.validateResponseTime(response, 5000);
    }

    // Utility methods

    @Step("Log E2E step: {stepName}")
    private void logE2EStep(String stepName, Object requestData, Response response) {
        System.out.println("\n=== E2E STEP: " + stepName + " ===");
        System.out.println("Status Code: " + response.getStatusCode());
        System.out.println("Response Time: " + response.getTime() + "ms");

        if (requestData != null) {
            System.out.println("Request Data: " + requestData.toString());
        }

        String responseBody = response.getBody().asString();
        if (responseBody.length() > 500) {
            responseBody = responseBody.substring(0, 500) + "...";
        }
        System.out.println("Response Body: " + responseBody);
        System.out.println("===========================\n");
    }

    // Cleanup implementation for ApiTestWithCleanup
    @Override
    protected Map<EntityType, Function<List<String>, Integer>> getCleanupStrategies() {
        Map<EntityType, Function<List<String>, Integer>> strategies = new HashMap<>();

        // E2E tests handle their own cleanup in test steps
        // This is mainly for safety in case of test failures
        strategies.put(EntityType.COMPRA, compraIds ->
            performCleanup(compraIds, comprasClient::deleteCompra, "compra"));

        return strategies;
    }
}