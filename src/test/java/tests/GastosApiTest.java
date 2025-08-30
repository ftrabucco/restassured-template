package tests;

import base.BaseTest;
import clients.GastosApiClient;
import io.qameta.allure.Description;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import utils.ResponseValidator;

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
    @Story("Get gasto by category")
    @DisplayName("Should retrieve gastos by category")
    @Description("Verify that gastos can be filtered by category")
    void shouldGetGastosByCategory() {
        // Arrange
        String categoriaId = "1";

        // Act
        Response response = gastosClient.getGastosWithFilters(categoriaId, null, null, null, null, null, null, null, null);

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
    @Story("Get gastos summary by date range")
    @DisplayName("Should retrieve gastos summary within date range")
    @Description("Verify that gastos summary can be filtered by date range using query parameters")
    void shouldGetGastosSummaryByDateRange() {
        // Arrange
        String fechaDesde = "2024-01-01";
        String fechaHasta = "2024-01-31";

        // Act
        Response response = gastosClient.getGastosSummary(fechaDesde, fechaHasta);

        // Assert
        ResponseValidator.validateStatusCode(response, 200);
        ResponseValidator.validateContentType(response, "application/json");
        ResponseValidator.validateResponseTime(response, 3000);
    }
}
