package tests;

import base.BaseTest;
import clients.GastosApiClient;
import io.qameta.allure.Description;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import io.restassured.response.Response;
import models.Gasto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import utils.ResponseValidator;
import utils.TestDataFactory;

import java.math.BigDecimal;

/**
 * Test class for Gastos (Expenses) API endpoints
 * Demonstrates the framework usage with real test scenarios
 */
@Feature("Gastos API")
public class GastosApiTest extends BaseTest {
    private GastosApiClient gastosClient;

    @Override
    protected void customSetup() {
        gastosClient = new GastosApiClient().withRequestSpec(requestSpec);
    }

    @Test
    @Story("Get all gastos")
    @DisplayName("Should retrieve all gastos successfully")
    @Description("Verify that the API returns all gastos with proper structure and status code")
    void shouldGetAllGastosSuccessfully() {
        // Act
        Response response = gastosClient.getAllGastos();

        // Assert
        ResponseValidator.validateStatusCode(response, 200);
        ResponseValidator.validateResponseTime(response, 5000);
        ResponseValidator.validateContentType(response, "application/json");
        ResponseValidator.validateResponseBodyNotEmpty(response);
    }

    @Test
    @Story("Create gasto")
    @DisplayName("Should create a new gasto successfully")
    @Description("Verify that a new gasto can be created with valid data")
    void shouldCreateGastoSuccessfully() {
        // Arrange
        Gasto newGasto = TestDataFactory.createRandomGasto();

        // Act
        Response response = gastosClient.createGasto(newGasto);

        // Assert
        ResponseValidator.validateStatusCode(response, 201);
        ResponseValidator.validateFieldExists(response, "id");
        ResponseValidator.validateFieldValue(response, "descripcion", newGasto.getDescripcion());
        ResponseValidator.validateFieldValue(response, "categoria", newGasto.getCategoria());
    }

    @Test
    @Story("Create gasto")
    @DisplayName("Should create gasto with specific amount")
    @Description("Verify that a gasto can be created with a specific monetary amount")
    void shouldCreateGastoWithSpecificAmount() {
        // Arrange
        BigDecimal testAmount = BigDecimal.valueOf(150.75);
        Gasto gasto = TestDataFactory.createGastoWithSpecificAmount(testAmount);

        // Act
        Response response = gastosClient.createGasto(gasto);

        // Assert
        ResponseValidator.validateStatusCode(response, 201);
        ResponseValidator.validateFieldExists(response, "id");
        ResponseValidator.validatePositiveNumber(response, "monto");
    }

    @Test
    @Story("Get gasto by category")
    @DisplayName("Should retrieve gastos by category")
    @Description("Verify that gastos can be filtered by category")
    void shouldGetGastosByCategory() {
        // Arrange
        String categoria = "Alimentación";

        // Act
        Response response = gastosClient.getGastosByCategoria(categoria);

        // Assert
        ResponseValidator.validateStatusCode(response, 200);
        ResponseValidator.validateContentType(response, "application/json");
        // Note: Array might be empty if no gastos exist for this category
    }

    @Test
    @Story("Get gasto by ID")
    @DisplayName("Should handle non-existent gasto gracefully")
    @Description("Verify that requesting a non-existent gasto returns appropriate error")
    void shouldHandleNonExistentGasto() {
        // Arrange
        String nonExistentId = "999999";

        // Act
        Response response = gastosClient.getGastoById(nonExistentId);

        // Assert
        ResponseValidator.validateStatusCode(response, 404);
    }

    @Test
    @Story("Get gastos by date range")
    @DisplayName("Should retrieve gastos within date range")
    @Description("Verify that gastos can be filtered by date range")
    void shouldGetGastosByDateRange() {
        // Arrange
        String fechaInicio = "2024-01-01";
        String fechaFin = "2024-12-31";

        // Act
        Response response = gastosClient.getGastosByDateRange(fechaInicio, fechaFin);

        // Assert
        ResponseValidator.validateStatusCode(response, 200);
        ResponseValidator.validateContentType(response, "application/json");
        ResponseValidator.validateResponseTime(response, 3000);
    }
}
