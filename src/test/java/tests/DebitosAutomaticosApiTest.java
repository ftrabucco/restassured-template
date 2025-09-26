package tests;

import base.ApiTestWithCleanup;
import clients.DebitosAutomaticosApiClient;
import io.qameta.allure.Description;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import io.restassured.response.Response;
import models.DebitoAutomatico;
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
 * Test class for Débitos Automáticos (Automatic Debits) API endpoints
 * Demonstrates comprehensive CRUD testing scenarios for automatic debit management
 */
@Feature("Débitos Automáticos API")
public class DebitosAutomaticosApiTest extends ApiTestWithCleanup {
    private DebitosAutomaticosApiClient debitosAutomaticosClient;

    @Override
    protected void customSetup() {
        debitosAutomaticosClient = new DebitosAutomaticosApiClient().withRequestSpec(requestSpec);
    }

    @Test
    @Story("Get all débitos automáticos")
    @DisplayName("Should retrieve all débitos automáticos successfully")
    @Description("Verify that the API returns all débitos automáticos with proper structure and status code")
    void shouldGetAllDebitosAutomaticosSuccessfully() {
        // Act
        Response response = debitosAutomaticosClient.getAllDebitosAutomaticos();

        // Assert
        ResponseValidator.validateStatusCode(response, 200);
        ResponseValidator.validateResponseTime(response, 5000);
        ResponseValidator.validateContentType(response, "application/json");
        ResponseValidator.validateResponseBodyNotEmpty(response);
    }

    @Test
    @Story("Create débito automático")
    @DisplayName("Should create a new débito automático successfully")
    @Description("Verify that a new débito automático can be created with valid data")
    void shouldCreateDebitoAutomaticoSuccessfully() {
        // Arrange
        DebitoAutomatico newDebitoAutomatico = TestDataFactory.createRandomDebitoAutomatico();

        // Act
        Response response = debitosAutomaticosClient.createDebitoAutomatico(newDebitoAutomatico);

        // Assert
        ResponseValidator.validateStatusCode(response, 201);
        
        // Debug: Log response structure
        System.out.println("=== DÉBITOS AUTOMÁTICOS CREATE RESPONSE ===");
        System.out.println(response.getBody().asString());
        System.out.println("==========================================");
        
        ResponseValidator.validateFieldExists(response, "data.debitoAutomatico.id");
        ResponseValidator.validateStringFieldValue(response, "data.debitoAutomatico.descripcion", newDebitoAutomatico.getDescripcion());
        ResponseValidator.validateNumericFieldValue(response, "data.debitoAutomatico.categoria_gasto_id", newDebitoAutomatico.getCategoriaGastoId());
        ResponseValidator.validateNumericFieldValue(response, "data.debitoAutomatico.importancia_gasto_id", newDebitoAutomatico.getImportanciaGastoId());
        ResponseValidator.validateNumericFieldValue(response, "data.debitoAutomatico.tipo_pago_id", newDebitoAutomatico.getTipoPagoId());
        ResponseValidator.validateNumericFieldValue(response, "data.debitoAutomatico.dia_de_pago", newDebitoAutomatico.getDiaDePago());
        ResponseValidator.validateNumericFieldValue(response, "data.debitoAutomatico.frecuencia_gasto_id", newDebitoAutomatico.getFrecuenciaGastoId());
        ResponseValidator.validateFieldValue(response, "data.debitoAutomatico.activo", true);
    }

    @Test
    @Story("Create débito automático")
    @DisplayName("Should create débito automático with specific amount")
    @Description("Verify that a débito automático can be created with a specific monetary amount")
    void shouldCreateDebitoAutomaticoWithSpecificAmount() {
        // Arrange
        BigDecimal testAmount = BigDecimal.valueOf(150.00);
        DebitoAutomatico debitoAutomatico = TestDataFactory.createDebitoAutomaticoWithSpecificAmount(testAmount);

        // Act
        Response response = debitosAutomaticosClient.createDebitoAutomatico(debitoAutomatico);

        // Assert
        ResponseValidator.validateStatusCode(response, 201);
        ResponseValidator.validateFieldExists(response, "data.debitoAutomatico.id");
        ResponseValidator.validatePositiveNumber(response, "data.debitoAutomatico.monto");
        ResponseValidator.validateStringFieldValue(response, "data.debitoAutomatico.descripcion", "Pago automático mensual");
        ResponseValidator.validateNumericFieldValue(response, "data.debitoAutomatico.dia_de_pago", 15);
        ResponseValidator.validateNumericFieldValue(response, "data.debitoAutomatico.frecuencia_gasto_id", 2);
    }

    @Test
    @Story("Get débito automático by ID")
    @DisplayName("Should retrieve débito automático by ID")
    @Description("Verify that a specific débito automático can be retrieved by its ID")
    void shouldGetDebitoAutomaticoById() {
        // Arrange
        DebitoAutomatico newDebitoAutomatico = TestDataFactory.createRandomDebitoAutomatico();
        Response createResponse = debitosAutomaticosClient.createDebitoAutomatico(newDebitoAutomatico);
        String debitoAutomaticoId = createResponse.jsonPath().getString("data.debitoAutomatico.id");

        // Act
        Response response = debitosAutomaticosClient.getDebitoAutomaticoById(debitoAutomaticoId);

        // Assert
        ResponseValidator.validateStatusCode(response, 200);
        ResponseValidator.validateContentType(response, "application/json");
        ResponseValidator.validateFieldExists(response, "data.id");
        ResponseValidator.validateStringFieldValue(response, "data.id", debitoAutomaticoId);
    }

    @Test
    @Story("Update débito automático")
    @DisplayName("Should update débito automático successfully")
    @Description("Verify that an existing débito automático can be updated with new data")
    void shouldUpdateDebitoAutomaticoSuccessfully() {
        // Arrange
        DebitoAutomatico originalDebitoAutomatico = TestDataFactory.createRandomDebitoAutomatico();
        Response createResponse = debitosAutomaticosClient.createDebitoAutomatico(originalDebitoAutomatico);
        String debitoAutomaticoId = createResponse.jsonPath().getString("data.debitoAutomatico.id");

        DebitoAutomatico updatedDebitoAutomatico = TestDataFactory.createDebitoAutomaticoWithSpecificAmount(BigDecimal.valueOf(200.00));
        updatedDebitoAutomatico.setDescripcion("Débito automático actualizado");
        updatedDebitoAutomatico.setActivo(false); // Test deactivation

        // Act
        Response response = debitosAutomaticosClient.updateDebitoAutomatico(debitoAutomaticoId, updatedDebitoAutomatico);

        // Assert
        ResponseValidator.validateStatusCode(response, 200);
        ResponseValidator.validateStringFieldValue(response, "data.descripcion", "Débito automático actualizado");
        ResponseValidator.validateFieldExists(response, "data.id");
        ResponseValidator.validateFieldValue(response, "data.activo", false);
    }

    @Test
    @Story("Delete débito automático")
    @DisplayName("Should delete débito automático successfully")
    @Description("Verify that an existing débito automático can be deleted")
    void shouldDeleteDebitoAutomaticoSuccessfully() {
        // Arrange
        DebitoAutomatico newDebitoAutomatico = TestDataFactory.createRandomDebitoAutomatico();
        Response createResponse = debitosAutomaticosClient.createDebitoAutomatico(newDebitoAutomatico);
        String debitoAutomaticoId = createResponse.jsonPath().getString("data.debitoAutomatico.id");

        // Act
        Response response = debitosAutomaticosClient.deleteDebitoAutomatico(debitoAutomaticoId);

        // Assert
        ResponseValidator.validateStatusCode(response, 200);
        
        // Verify the débito automático is actually deleted
        Response getResponse = debitosAutomaticosClient.getDebitoAutomaticoById(debitoAutomaticoId);
        ResponseValidator.validateStatusCode(getResponse, 404);
    }

    @Test
    @Story("Get débito automático by ID")
    @DisplayName("Should handle non-existent débito automático gracefully")
    @Description("Verify that requesting a non-existent débito automático returns appropriate error")
    void shouldHandleNonExistentDebitoAutomatico() {
        // Arrange
        String nonExistentId = "999999";

        // Act
        Response response = debitosAutomaticosClient.getDebitoAutomaticoById(nonExistentId);

        // Assert
        ResponseValidator.validateStatusCode(response, 404);
    }

    @Test
    @Story("Create débito automático")
    @DisplayName("Should handle invalid débito automático data")
    @Description("Verify that creating a débito automático with invalid data returns appropriate error")
    void shouldHandleInvalidDebitoAutomaticoData() {
        // Arrange
        DebitoAutomatico invalidDebitoAutomatico = new DebitoAutomatico.Builder()
                .descripcion("") // Empty description should be invalid
                .monto(BigDecimal.valueOf(-100)) // Negative amount should be invalid
                .diaDePago(35) // Invalid day (>31) should be invalid
                .build();

        // Act
        Response response = debitosAutomaticosClient.createDebitoAutomatico(invalidDebitoAutomatico);

        // Assert
        ResponseValidator.validateStatusCode(response, 400);
    }

    @Test
    @Story("Create débito automático")
    @DisplayName("Should create débito automático for specific category")
    @Description("Verify that a débito automático can be created for a specific category")
    void shouldCreateDebitoAutomaticoForSpecificCategory() {
        // Arrange
        Long categoriaGastoId = 6L; // Utilities category
        DebitoAutomatico debitoAutomatico = TestDataFactory.createDebitoAutomaticoForCategory(categoriaGastoId);

        // Act
        Response response = debitosAutomaticosClient.createDebitoAutomatico(debitoAutomatico);

        // Assert
        ResponseValidator.validateStatusCode(response, 201);
        ResponseValidator.validateFieldExists(response, "data.debitoAutomatico.id");
        ResponseValidator.validateNumericFieldValue(response, "data.debitoAutomatico.categoria_gasto_id", categoriaGastoId);
    }

    @Test
    @Story("Update débito automático")
    @DisplayName("Should activate/deactivate débito automático")
    @Description("Verify that a débito automático can be activated or deactivated")
    void shouldActivateDeactivateDebitoAutomatico() {
        // Arrange
        DebitoAutomatico debitoAutomatico = TestDataFactory.createRandomDebitoAutomatico();
        Response createResponse = debitosAutomaticosClient.createDebitoAutomatico(debitoAutomatico);
        String debitoAutomaticoId = createResponse.jsonPath().getString("data.debitoAutomatico.id");

        // Act - Deactivate
        Response deactivateResponse = debitosAutomaticosClient.activateDeactivateDebitoAutomatico(debitoAutomaticoId, false);

        // Assert - Deactivation
        ResponseValidator.validateStatusCode(deactivateResponse, 200);
        ResponseValidator.validateFieldValue(deactivateResponse, "data.activo", false);

        // Act - Reactivate
        Response reactivateResponse = debitosAutomaticosClient.activateDeactivateDebitoAutomatico(debitoAutomaticoId, true);

        // Assert - Reactivation
        ResponseValidator.validateStatusCode(reactivateResponse, 200);
        ResponseValidator.validateFieldValue(reactivateResponse, "data.activo", true);
    }

    @Test
    @Story("Filter débitos automáticos")
    @DisplayName("Should get débitos automáticos by category")
    @Description("Verify that débitos automáticos can be filtered by category")
    void shouldGetDebitosAutomaticosByCategory() {
        // Arrange
        Long categoriaGastoId = 8L; // Health category
        DebitoAutomatico debitoAutomatico = TestDataFactory.createDebitoAutomaticoForCategory(categoriaGastoId);
        debitosAutomaticosClient.createDebitoAutomatico(debitoAutomatico);

        // Act
        Response response = debitosAutomaticosClient.getDebitosAutomaticosByCategory(categoriaGastoId);

        // Assert
        ResponseValidator.validateStatusCode(response, 200);
        ResponseValidator.validateContentType(response, "application/json");
        ResponseValidator.validateResponseBodyNotEmpty(response);
    }

    @Test
    @Story("Filter débitos automáticos")
    @DisplayName("Should get active débitos automáticos")
    @Description("Verify that only active débitos automáticos are returned when filtering by active status")
    void shouldGetActiveDebitosAutomaticos() {
        // Act
        Response response = debitosAutomaticosClient.getActiveDebitosAutomaticos();

        // Assert
        ResponseValidator.validateStatusCode(response, 200);
        ResponseValidator.validateContentType(response, "application/json");
    }

    @Test
    @Story("Filter débitos automáticos")
    @DisplayName("Should get inactive débitos automáticos")
    @Description("Verify that only inactive débitos automáticos are returned when filtering by inactive status")
    void shouldGetInactiveDebitosAutomaticos() {
        // Arrange - Create an inactive debit first
        DebitoAutomatico inactiveDebitoAutomatico = TestDataFactory.createInactiveDebitoAutomatico();
        debitosAutomaticosClient.createDebitoAutomatico(inactiveDebitoAutomatico);

        // Act
        Response response = debitosAutomaticosClient.getInactiveDebitosAutomaticos();

        // Assert
        ResponseValidator.validateStatusCode(response, 200);
        ResponseValidator.validateContentType(response, "application/json");
    }

    // Cleanup implementation using Strategy Pattern (SOLID principles)
    @Override
    protected Map<EntityType, Function<List<String>, Integer>> getCleanupStrategies() {
        Map<EntityType, Function<List<String>, Integer>> strategies = new HashMap<>();

        // Only register cleanup strategy for entities this test creates
        strategies.put(EntityType.DEBITO_AUTOMATICO, debitoAutomaticoIds ->
            performCleanup(debitoAutomaticoIds, debitosAutomaticosClient::deleteDebitoAutomatico, "débito automático"));

        return strategies;
    }
}