package tests;

import base.BaseTest;
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

/**
 * Test class for Compras (Purchases) API endpoints
 * Demonstrates comprehensive testing scenarios for purchase operations
 */
@Feature("Compras API")
public class ComprasApiTest extends BaseTest {
    private ComprasApiClient comprasClient;

    @Override
    protected void customSetup() {
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

        // Assert
        ResponseValidator.validateStatusCode(response, 201);
        ResponseValidator.validateFieldExists(response, "id");
        ResponseValidator.validateStringFieldValue(response, "descripcion", newCompra.getDescripcion());
        ResponseValidator.validateNumericFieldValue(response, "categoria_gasto_id", newCompra.getCategoriaGastoId());
        ResponseValidator.validateNumericFieldValue(response, "importancia_gasto_id", newCompra.getImportanciaGastoId());
        ResponseValidator.validateNumericFieldValue(response, "tipo_pago_id", newCompra.getTipoPagoId());
        ResponseValidator.validateNumericFieldValue(response, "tarjeta_id", newCompra.getTarjetaId());
        ResponseValidator.validateNumericFieldValue(response, "cantidad_cuotas", newCompra.getCantidadCuotas());
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

        // Assert
        ResponseValidator.validateStatusCode(response, 201);
        ResponseValidator.validateFieldExists(response, "id");
        ResponseValidator.validatePositiveNumber(response, "monto_total");
        ResponseValidator.validateStringFieldValue(response, "descripcion", "Laptop");
        ResponseValidator.validateNumericFieldValue(response, "cantidad_cuotas", 12);
    }

    @Test
    @Story("Get compra by ID")
    @DisplayName("Should retrieve compra by ID")
    @Description("Verify that a specific compra can be retrieved by its ID")
    void shouldGetCompraById() {
        // Arrange
        Compra newCompra = TestDataFactory.createRandomCompra();
        Response createResponse = comprasClient.createCompra(newCompra);
        String compraId = createResponse.jsonPath().getString("id");

        // Act
        Response response = comprasClient.getCompraById(compraId);

        // Assert
        ResponseValidator.validateStatusCode(response, 200);
        ResponseValidator.validateContentType(response, "application/json");
        ResponseValidator.validateFieldExists(response, "id");
        ResponseValidator.validateStringFieldValue(response, "id", compraId);
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

        // Assert
        ResponseValidator.validateStatusCode(response, 201);
        ResponseValidator.validateFieldExists(response, "id");
        ResponseValidator.validateNumericFieldValue(response, "categoria_gasto_id", categoriaGastoId);
    }
}
