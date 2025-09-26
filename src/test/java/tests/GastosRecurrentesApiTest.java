package tests;

import base.ApiTestWithCleanup;
import clients.GastosRecurrentesApiClient;
import io.qameta.allure.Description;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import io.restassured.response.Response;
import models.GastoRecurrente;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import utils.ResponseValidator;
import utils.TestDataFactory;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static base.ApiTestWithCleanup.EntityType;

/**
 * Test class for Gastos Recurrentes (Recurring Expenses) API endpoints
 * Demonstrates comprehensive CRUD testing scenarios
 */
@Feature("Gastos Recurrentes API")
public class GastosRecurrentesApiTest extends ApiTestWithCleanup {
    private GastosRecurrentesApiClient gastosRecurrentesClient;

    @Override
    protected void customSetup() {
        gastosRecurrentesClient = new GastosRecurrentesApiClient().withRequestSpec(requestSpec);
    }

    @Test
    @Story("Get all gastos recurrentes")
    @DisplayName("Should retrieve all gastos recurrentes successfully")
    @Description("Verify that the API returns all gastos recurrentes with proper structure and status code")
    void shouldGetAllGastosRecurrentesSuccessfully() {
        // Act
        Response response = gastosRecurrentesClient.getAllGastosRecurrentes();

        // Assert
        ResponseValidator.validateStatusCode(response, 200);
        ResponseValidator.validateResponseTime(response, 5000);
        ResponseValidator.validateContentType(response, "application/json");
        ResponseValidator.validateResponseBodyNotEmpty(response);
    }

    @Test
    @Story("Create gasto recurrente")
    @DisplayName("Should create a new gasto recurrente successfully")
    @Description("Verify that a new gasto recurrente can be created with valid data")
    void shouldCreateGastoRecurrenteSuccessfully() {
        // Arrange
        GastoRecurrente newGastoRecurrente = TestDataFactory.createRandomGastoRecurrente();

        // Act
        Response response = gastosRecurrentesClient.createGastoRecurrente(newGastoRecurrente);

        // Assert
        ResponseValidator.validateStatusCode(response, 201);
        
        // Debug: Log response structure
        System.out.println("=== GASTOS RECURRENTES CREATE RESPONSE ===");
        System.out.println(response.getBody().asString());
        System.out.println("==========================================");
        
        ResponseValidator.validateFieldExists(response, "data.gastoRecurrente.id");
        ResponseValidator.validateStringFieldValue(response, "data.gastoRecurrente.descripcion", newGastoRecurrente.getDescripcion());
        ResponseValidator.validateNumericFieldValue(response, "data.gastoRecurrente.categoria_gasto_id", newGastoRecurrente.getCategoriaGastoId());
        ResponseValidator.validateNumericFieldValue(response, "data.gastoRecurrente.importancia_gasto_id", newGastoRecurrente.getImportanciaGastoId());
        ResponseValidator.validateNumericFieldValue(response, "data.gastoRecurrente.tipo_pago_id", newGastoRecurrente.getTipoPagoId());
        ResponseValidator.validateNumericFieldValue(response, "data.gastoRecurrente.tarjeta_id", newGastoRecurrente.getTarjetaId());
        ResponseValidator.validateNumericFieldValue(response, "data.gastoRecurrente.dia_de_pago", newGastoRecurrente.getDiaDePago());
        ResponseValidator.validateNumericFieldValue(response, "data.gastoRecurrente.frecuencia_gasto_id", newGastoRecurrente.getFrecuenciaGastoId());
    }

    @Test
    @Story("Create gasto recurrente")
    @DisplayName("Should create gasto recurrente with specific amount")
    @Description("Verify that a gasto recurrente can be created with a specific monetary amount")
    void shouldCreateGastoRecurrenteWithSpecificAmount() {
        // Arrange
        BigDecimal testAmount = BigDecimal.valueOf(50.00);
        GastoRecurrente gastoRecurrente = TestDataFactory.createGastoRecurrenteWithSpecificAmount(testAmount);

        // Act
        Response response = gastosRecurrentesClient.createGastoRecurrente(gastoRecurrente);

        // Assert
        ResponseValidator.validateStatusCode(response, 201);
        ResponseValidator.validateFieldExists(response, "data.gastoRecurrente.id");
        ResponseValidator.validatePositiveNumber(response, "data.gastoRecurrente.monto");
        ResponseValidator.validateStringFieldValue(response, "data.gastoRecurrente.descripcion", "test 2");
        ResponseValidator.validateNumericFieldValue(response, "data.gastoRecurrente.dia_de_pago", 15);
        ResponseValidator.validateNumericFieldValue(response, "data.gastoRecurrente.frecuencia_gasto_id", 2);
    }

    @Test
    @Story("Get gasto recurrente by ID")
    @DisplayName("Should retrieve gasto recurrente by ID")
    @Description("Verify that a specific gasto recurrente can be retrieved by its ID")
    void shouldGetGastoRecurrenteById() {
        // Arrange
        GastoRecurrente newGastoRecurrente = TestDataFactory.createRandomGastoRecurrente();
        Response createResponse = gastosRecurrentesClient.createGastoRecurrente(newGastoRecurrente);
        String gastoRecurrenteId = createResponse.jsonPath().getString("data.gastoRecurrente.id");

        // Act
        Response response = gastosRecurrentesClient.getGastoRecurrenteById(gastoRecurrenteId);

        // Assert
        ResponseValidator.validateStatusCode(response, 200);
        ResponseValidator.validateContentType(response, "application/json");
        ResponseValidator.validateFieldExists(response, "data.id");
        ResponseValidator.validateStringFieldValue(response, "data.id", gastoRecurrenteId);
    }

    @Test
    @Story("Update gasto recurrente")
    @DisplayName("Should update gasto recurrente successfully")
    @Description("Verify that an existing gasto recurrente can be updated with new data")
    void shouldUpdateGastoRecurrenteSuccessfully() {
        // Arrange
        GastoRecurrente originalGastoRecurrente = TestDataFactory.createRandomGastoRecurrente();
        Response createResponse = gastosRecurrentesClient.createGastoRecurrente(originalGastoRecurrente);
        String gastoRecurrenteId = createResponse.jsonPath().getString("data.gastoRecurrente.id");

        GastoRecurrente updatedGastoRecurrente = TestDataFactory.createGastoRecurrenteWithSpecificAmount(BigDecimal.valueOf(75.00));
        updatedGastoRecurrente.setDescripcion("Gasto recurrente actualizado");
        updatedGastoRecurrente.setActivo(false); // Test deactivation

        // Act
        Response response = gastosRecurrentesClient.updateGastoRecurrente(gastoRecurrenteId, updatedGastoRecurrente);

        // Assert
        ResponseValidator.validateStatusCode(response, 200);
        ResponseValidator.validateStringFieldValue(response, "data.descripcion", "Gasto recurrente actualizado");
        ResponseValidator.validateFieldExists(response, "data.id");
        ResponseValidator.validateFieldValue(response, "data.activo", false);
    }

    @Test
    @Story("Delete gasto recurrente")
    @DisplayName("Should delete gasto recurrente successfully")
    @Description("Verify that an existing gasto recurrente can be deleted")
    void shouldDeleteGastoRecurrenteSuccessfully() {
        // Arrange
        GastoRecurrente newGastoRecurrente = TestDataFactory.createRandomGastoRecurrente();
        Response createResponse = gastosRecurrentesClient.createGastoRecurrente(newGastoRecurrente);
        String gastoRecurrenteId = createResponse.jsonPath().getString("data.gastoRecurrente.id");

        // Act
        Response response = gastosRecurrentesClient.deleteGastoRecurrente(gastoRecurrenteId);

        // Assert
        ResponseValidator.validateStatusCode(response, 200);
        
        // Verify the gasto recurrente is actually deleted
        Response getResponse = gastosRecurrentesClient.getGastoRecurrenteById(gastoRecurrenteId);
        ResponseValidator.validateStatusCode(getResponse, 404);
    }

    @Test
    @Story("Get gasto recurrente by ID")
    @DisplayName("Should handle non-existent gasto recurrente gracefully")
    @Description("Verify that requesting a non-existent gasto recurrente returns appropriate error")
    void shouldHandleNonExistentGastoRecurrente() {
        // Arrange
        String nonExistentId = "999999";

        // Act
        Response response = gastosRecurrentesClient.getGastoRecurrenteById(nonExistentId);

        // Assert
        ResponseValidator.validateStatusCode(response, 404);
    }

    @Test
    @Story("Create gasto recurrente")
    @DisplayName("Should handle invalid gasto recurrente data")
    @Description("Verify that creating a gasto recurrente with invalid data returns appropriate error")
    void shouldHandleInvalidGastoRecurrenteData() {
        // Arrange
        GastoRecurrente invalidGastoRecurrente = new GastoRecurrente.Builder()
                .descripcion("") // Empty description should be invalid
                .monto(BigDecimal.valueOf(-50)) // Negative amount should be invalid
                .diaDePago(35) // Invalid day (>31) should be invalid
                .build();

        // Act
        Response response = gastosRecurrentesClient.createGastoRecurrente(invalidGastoRecurrente);

        // Assert
        ResponseValidator.validateStatusCode(response, 400);
    }

    @Test
    @Story("Create gasto recurrente")
    @DisplayName("Should create gasto recurrente for specific category")
    @Description("Verify that a gasto recurrente can be created for a specific category")
    void shouldCreateGastoRecurrenteForSpecificCategory() {
        // Arrange
        Long categoriaGastoId = 6L; // Utilities category
        GastoRecurrente gastoRecurrente = TestDataFactory.createGastoRecurrenteForCategory(categoriaGastoId);

        // Act
        Response response = gastosRecurrentesClient.createGastoRecurrente(gastoRecurrente);

        // Assert
        ResponseValidator.validateStatusCode(response, 201);
        ResponseValidator.validateFieldExists(response, "data.gastoRecurrente.id");
        ResponseValidator.validateNumericFieldValue(response, "data.gastoRecurrente.categoria_gasto_id", categoriaGastoId);
    }

    @Test
    @Story("Update gasto recurrente")
    @DisplayName("Should activate/deactivate gasto recurrente")
    @Description("Verify that a gasto recurrente can be activated or deactivated")
    void shouldActivateDeactivateGastoRecurrente() {
        // Arrange
        GastoRecurrente gastoRecurrente = TestDataFactory.createRandomGastoRecurrente();
        gastoRecurrente.setActivo(true);
        Response createResponse = gastosRecurrentesClient.createGastoRecurrente(gastoRecurrente);
        String gastoRecurrenteId = createResponse.jsonPath().getString("data.gastoRecurrente.id");

        // Act - Deactivate
        gastoRecurrente.setActivo(false);
        Response deactivateResponse = gastosRecurrentesClient.updateGastoRecurrente(gastoRecurrenteId, gastoRecurrente);

        // Assert - Deactivation
        ResponseValidator.validateStatusCode(deactivateResponse, 200);
        ResponseValidator.validateFieldValue(deactivateResponse, "data.activo", false);

        // Act - Reactivate
        gastoRecurrente.setActivo(true);
        Response reactivateResponse = gastosRecurrentesClient.updateGastoRecurrente(gastoRecurrenteId, gastoRecurrente);

        // Assert - Reactivation
        ResponseValidator.validateStatusCode(reactivateResponse, 200);
        ResponseValidator.validateFieldValue(reactivateResponse, "data.activo", true);
    }

    // Cleanup implementation using Strategy Pattern (SOLID principles)
    @Override
    protected Map<EntityType, Function<List<String>, Integer>> getCleanupStrategies() {
        Map<EntityType, Function<List<String>, Integer>> strategies = new HashMap<>();

        // Only register cleanup strategy for entities this test creates
        strategies.put(EntityType.GASTO_RECURRENTE, gastoRecurrenteIds ->
            performCleanup(gastoRecurrenteIds, gastosRecurrentesClient::deleteGastoRecurrente, "gasto recurrente"));

        return strategies;
    }
}
