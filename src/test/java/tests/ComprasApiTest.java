package tests;

import base.ApiTestWithCleanup;
import clients.ComprasApiClient;
import io.qameta.allure.Description;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import io.restassured.response.Response;
import models.Compra;
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
 * Test class for Compras (Purchases) API endpoints
 * Demonstrates comprehensive testing scenarios for purchase operations
 */
@Feature("Compras API")
public class ComprasApiTest extends ApiTestWithCleanup {
    private ComprasApiClient comprasClient;

    @Override
    protected void customAuthenticatedSetup() {
        comprasClient = new ComprasApiClient().withRequestSpec(requestSpec);
    }

    @Test
    @Story("Get all compras")
    @DisplayName("Should retrieve all compras successfully")
    @Description("Verify that the API returns all compras with proper structure and status code")
    void shouldGetAllComprasSuccessfully() {
        // Act
        Response response = comprasClient.getAllCompras();

        // Assert
        ResponseValidator.validateStatusCode(response, 200);
        ResponseValidator.validateResponseTime(response, 5000);
        ResponseValidator.validateContentType(response, "application/json");
        ResponseValidator.validateResponseBodyNotEmpty(response);
    }

    @Test
    @Story("Create compra")
    @DisplayName("Should create a new compra successfully")
    @Description("Verify that a new compra can be created with valid data")
    void shouldCreateCompraSuccessfully() {
        // Arrange
        Compra newCompra = TestDataFactory.createRandomCompra();

        // Act
        Response response = comprasClient.createCompra(newCompra);

        // Track for cleanup using improved method
        trackEntityFromResponse(response, EntityType.COMPRA, "data.compra.id");

        // Assert
        ResponseValidator.validateStatusCode(response, 201);
        
        // Debug: Log response structure
        System.out.println("=== COMPRAS CREATE RESPONSE ===");
        System.out.println(response.getBody().asString());
        System.out.println("==============================");
        
        ResponseValidator.validateFieldExists(response, "data.compra.id");
        ResponseValidator.validateStringFieldValue(response, "data.compra.descripcion", newCompra.getDescripcion());
        ResponseValidator.validateNumericFieldValue(response, "data.compra.categoria_gasto_id", newCompra.getCategoriaGastoId());
        ResponseValidator.validateNumericFieldValue(response, "data.compra.importancia_gasto_id", newCompra.getImportanciaGastoId());
        ResponseValidator.validateNumericFieldValue(response, "data.compra.tipo_pago_id", newCompra.getTipoPagoId());
        ResponseValidator.validateNumericFieldValue(response, "data.compra.tarjeta_id", newCompra.getTarjetaId());
        ResponseValidator.validateNumericFieldValue(response, "data.compra.cantidad_cuotas", newCompra.getCantidadCuotas());
    }

    @Test
    @Story("Create compra")
    @DisplayName("Should create compra with specific amount")
    @Description("Verify that a compra can be created with a specific monetary amount")
    void shouldCreateCompraWithSpecificAmount() {
        // Arrange
        BigDecimal testAmount = BigDecimal.valueOf(299.99);
        Compra compra = TestDataFactory.createCompraWithSpecificAmount(testAmount);

        // Act
        Response response = comprasClient.createCompra(compra);

        // Track for cleanup using improved method
        trackEntityFromResponse(response, EntityType.COMPRA, "data.compra.id");

        // Assert
        ResponseValidator.validateStatusCode(response, 201);
        ResponseValidator.validateFieldExists(response, "data.compra.id");
        ResponseValidator.validatePositiveNumber(response, "data.compra.monto_total");
        ResponseValidator.validateStringFieldValue(response, "data.compra.descripcion", "Laptop");
        ResponseValidator.validateNumericFieldValue(response, "data.compra.cantidad_cuotas", 12);
    }

    @Test
    @Story("Get compra by ID")
    @DisplayName("Should retrieve compra by ID")
    @Description("Verify that a specific compra can be retrieved by its ID")
    void shouldGetCompraById() {
        // Arrange
        Compra newCompra = TestDataFactory.createRandomCompra();
        Response createResponse = comprasClient.createCompra(newCompra);
        String compraId = createResponse.jsonPath().getString("data.compra.id");

        // Track for cleanup
        trackCreatedCompra(compraId);

        // Act
        Response response = comprasClient.getCompraById(compraId);

        // Assert
        ResponseValidator.validateStatusCode(response, 200);
        ResponseValidator.validateContentType(response, "application/json");
        ResponseValidator.validateFieldExists(response, "data.id");
        ResponseValidator.validateStringFieldValue(response, "data.id", compraId);
    }

    @Test
    @Story("Get compra by ID")
    @DisplayName("Should handle non-existent compra gracefully")
    @Description("Verify that requesting a non-existent compra returns appropriate error")
    void shouldHandleNonExistentCompra() {
        // Arrange
        String nonExistentId = "999999";

        // Act
        Response response = comprasClient.getCompraById(nonExistentId);

        // Assert
        ResponseValidator.validateStatusCode(response, 404);
    }

    @Test
    @Story("Create compra")
    @DisplayName("Should handle invalid compra data")
    @Description("Verify that creating a compra with invalid data returns appropriate error")
    void shouldHandleInvalidCompraData() {
        // Arrange
        Compra invalidCompra = new Compra.Builder()
                .descripcion("") // Empty description should be invalid
                .montoTotal(BigDecimal.valueOf(-100)) // Negative amount should be invalid
                .cantidadCuotas(0) // Zero installments should be invalid
                .build();

        // Act
        Response response = comprasClient.createCompra(invalidCompra);

        // Assert
        ResponseValidator.validateStatusCode(response, 400);
    }

    @Test
    @Story("Create compra")
    @DisplayName("Should create compra for specific category")
    @Description("Verify that a compra can be created for a specific category")
    void shouldCreateCompraForSpecificCategory() {
        // Arrange
        Long categoriaGastoId = 15L; // Technology category
        Compra compra = TestDataFactory.createCompraForCategory(categoriaGastoId);

        // Act
        Response response = comprasClient.createCompra(compra);

        // Track for cleanup using improved method
        trackEntityFromResponse(response, EntityType.COMPRA, "data.compra.id");

        // Assert
        ResponseValidator.validateStatusCode(response, 201);
        ResponseValidator.validateFieldExists(response, "data.compra.id");
        ResponseValidator.validateNumericFieldValue(response, "data.compra.categoria_gasto_id", categoriaGastoId);
    }

    // ===============================
    // SECURITY TESTS - Authentication
    // ===============================

    @Test
    @Story("Security Testing")
    @DisplayName("Should return 401 when no authentication token provided")
    @Description("Verify that requests without JWT token are rejected with 401 Unauthorized")
    void shouldReturn401WithoutAuthToken() {
        // Arrange
        ComprasApiClient unauthenticatedClient = new ComprasApiClient()
            .withRequestSpec(getUnauthenticatedRequestSpec());

        // Act
        Response response = unauthenticatedClient.getAllCompras();

        // Assert
        ResponseValidator.validateStatusCode(response, 401);
    }

    @Test
    @Story("Security Testing")
    @DisplayName("Should return 401 with invalid authentication token")
    @Description("Verify that requests with invalid JWT token are rejected with 401 Unauthorized")
    void shouldReturn401WithInvalidToken() {
        // Arrange
        ComprasApiClient invalidTokenClient = new ComprasApiClient()
            .withRequestSpec(getInvalidTokenRequestSpec());

        // Act
        Response response = invalidTokenClient.getAllCompras();

        // Assert
        ResponseValidator.validateStatusCode(response, 401);
    }

    @Test
    @Story("Security Testing")
    @DisplayName("Should return 401 with malformed authentication token")
    @Description("Verify that requests with malformed JWT token are rejected with 401 Unauthorized")
    void shouldReturn401WithMalformedToken() {
        // Arrange
        ComprasApiClient malformedTokenClient = new ComprasApiClient()
            .withRequestSpec(getMalformedTokenRequestSpec());

        // Act
        Response response = malformedTokenClient.getAllCompras();

        // Assert
        ResponseValidator.validateStatusCode(response, 401);
    }

    @Test
    @Story("Security Testing")
    @DisplayName("Should return 401 when creating compra without authentication")
    @Description("Verify that creation operations require authentication")
    void shouldReturn401WhenCreatingWithoutAuth() {
        // Arrange
        ComprasApiClient unauthenticatedClient = new ComprasApiClient()
            .withRequestSpec(getUnauthenticatedRequestSpec());

        Compra newCompra = TestDataFactory.createRandomCompra();

        // Act
        Response response = unauthenticatedClient.createCompra(newCompra);

        // Assert
        ResponseValidator.validateStatusCode(response, 401);
    }

    // Cleanup implementation using Strategy Pattern (SOLID principles)
    @Override
    protected Map<EntityType, Function<List<String>, Integer>> getCleanupStrategies() {
        Map<EntityType, Function<List<String>, Integer>> strategies = new HashMap<>();

        // Only register cleanup strategy for entities this test creates
        strategies.put(EntityType.COMPRA, compraIds ->
            performCleanup(compraIds, comprasClient::deleteCompra, "compra"));

        return strategies;
    }
}
