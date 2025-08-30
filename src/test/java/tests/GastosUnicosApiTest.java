package tests;

import base.BaseTest;
import clients.GastosUnicosApiClient;
import io.qameta.allure.Description;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import io.restassured.response.Response;
import models.GastoUnico;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import utils.ResponseValidator;
import utils.TestDataFactory;

import java.math.BigDecimal;

/**
 * Test class for Gastos Únicos (One-time Expenses) API endpoints
 * Demonstrates comprehensive CRUD testing scenarios
 */
@Feature("Gastos Únicos API")
public class GastosUnicosApiTest extends BaseTest {
    private GastosUnicosApiClient gastosUnicosClient;

    @Override
    protected void customSetup() {
        gastosUnicosClient = new GastosUnicosApiClient().withRequestSpec(requestSpec);
    }

    @Test
    @Story("Get all gastos únicos")
    @DisplayName("Should retrieve all gastos únicos successfully")
    @Description("Verify that the API returns all gastos únicos with proper structure and status code")
    void shouldGetAllGastosUnicosSuccessfully() {
        // Act
        Response response = gastosUnicosClient.getAllGastosUnicos();

        // Assert
        ResponseValidator.validateStatusCode(response, 200);
        ResponseValidator.validateResponseTime(response, 5000);
        ResponseValidator.validateContentType(response, "application/json");
        ResponseValidator.validateResponseBodyNotEmpty(response);
    }

    @Test
    @Story("Create gasto único")
    @DisplayName("Should create a new gasto único successfully")
    @Description("Verify that a new gasto único can be created with valid data")
    void shouldCreateGastoUnicoSuccessfully() {
        // Arrange
        GastoUnico newGastoUnico = TestDataFactory.createRandomGastoUnico();

        // Act
        Response response = gastosUnicosClient.createGastoUnico(newGastoUnico);

        // Assert
        ResponseValidator.validateStatusCode(response, 201);
        ResponseValidator.validateFieldExists(response, "id");
        ResponseValidator.validateStringFieldValue(response, "descripcion", newGastoUnico.getDescripcion());
        ResponseValidator.validateNumericFieldValue(response, "categoria_gasto_id", newGastoUnico.getCategoriaGastoId());
        ResponseValidator.validateNumericFieldValue(response, "importancia_gasto_id", newGastoUnico.getImportanciaGastoId());
        ResponseValidator.validateNumericFieldValue(response, "tipo_pago_id", newGastoUnico.getTipoPagoId());
    }

    @Test
    @Story("Create gasto único")
    @DisplayName("Should create gasto único with specific amount")
    @Description("Verify that a gasto único can be created with a specific monetary amount")
    void shouldCreateGastoUnicoWithSpecificAmount() {
        // Arrange
        BigDecimal testAmount = BigDecimal.valueOf(500.00);
        GastoUnico gastoUnico = TestDataFactory.createGastoUnicoWithSpecificAmount(testAmount);

        // Act
        Response response = gastosUnicosClient.createGastoUnico(gastoUnico);

        // Assert
        ResponseValidator.validateStatusCode(response, 201);
        ResponseValidator.validateFieldExists(response, "id");
        ResponseValidator.validatePositiveNumber(response, "monto");
        ResponseValidator.validateStringFieldValue(response, "descripcion", "Reparación auto");
    }

    @Test
    @Story("Get gasto único by ID")
    @DisplayName("Should retrieve gasto único by ID")
    @Description("Verify that a specific gasto único can be retrieved by its ID")
    void shouldGetGastoUnicoById() {
        // Arrange
        GastoUnico newGastoUnico = TestDataFactory.createRandomGastoUnico();
        Response createResponse = gastosUnicosClient.createGastoUnico(newGastoUnico);
        String gastoUnicoId = createResponse.jsonPath().getString("id");

        // Act
        Response response = gastosUnicosClient.getGastoUnicoById(gastoUnicoId);

        // Assert
        ResponseValidator.validateStatusCode(response, 200);
        ResponseValidator.validateContentType(response, "application/json");
        ResponseValidator.validateFieldExists(response, "id");
        ResponseValidator.validateStringFieldValue(response, "id", gastoUnicoId);
    }

    @Test
    @Story("Update gasto único")
    @DisplayName("Should not update gasto único successfully as is not allowed")
    @Description("Verify that an existing gasto único can not be updated with new data")
    void shouldNotUpdateGastoUnicoSuccessfully() {
        // Arrange
        GastoUnico originalGastoUnico = TestDataFactory.createRandomGastoUnico();
        Response createResponse = gastosUnicosClient.createGastoUnico(originalGastoUnico);
        String gastoUnicoId = createResponse.jsonPath().getString("id");

        GastoUnico updatedGastoUnico = TestDataFactory.createGastoUnicoWithSpecificAmount(BigDecimal.valueOf(750.00));
        updatedGastoUnico.setDescripcion("Reparación auto actualizada");

        // Act
        Response response = gastosUnicosClient.updateGastoUnico(gastoUnicoId, updatedGastoUnico);

        // Assert
        ResponseValidator.validateStatusCode(response, 400);
        //ResponseValidator.validateStringFieldValue(response, "descripcion", "Reparación auto actualizada");
        //ResponseValidator.validateFieldExists(response, "id");
    }

    @Test
    @Story("Delete gasto único")
    @DisplayName("Should not delete gasto único successfully as is not allowed")
    @Description("Verify that an existing gasto único can not be deleted")
    void shouldNotDeleteGastoUnicoSuccessfully() {
        // Arrange
        GastoUnico newGastoUnico = TestDataFactory.createRandomGastoUnico();
        Response createResponse = gastosUnicosClient.createGastoUnico(newGastoUnico);
        String gastoUnicoId = createResponse.jsonPath().getString("id");

        // Act
        Response response = gastosUnicosClient.deleteGastoUnico(gastoUnicoId);

        // Assert
        ResponseValidator.validateStatusCode(response, 400);
        
        // Verify the gasto único is actually deleted
        //Response getResponse = gastosUnicosClient.getGastoUnicoById(gastoUnicoId);
        //ResponseValidator.validateStatusCode(getResponse, 404);
    }

    @Test
    @Story("Get gasto único by ID")
    @DisplayName("Should handle non-existent gasto único gracefully")
    @Description("Verify that requesting a non-existent gasto único returns appropriate error")
    void shouldHandleNonExistentGastoUnico() {
        // Arrange
        String nonExistentId = "999999";

        // Act
        Response response = gastosUnicosClient.getGastoUnicoById(nonExistentId);

        // Assert
        ResponseValidator.validateStatusCode(response, 404);
    }

    @Test
    @Story("Create gasto único")
    @DisplayName("Should handle invalid gasto único data")
    @Description("Verify that creating a gasto único with invalid data returns appropriate error")
    void shouldHandleInvalidGastoUnicoData() {
        // Arrange
        GastoUnico invalidGastoUnico = new GastoUnico.Builder()
                .descripcion("") // Empty description should be invalid
                .monto(BigDecimal.valueOf(-100)) // Negative amount should be invalid
                .build();

        // Act
        Response response = gastosUnicosClient.createGastoUnico(invalidGastoUnico);

        // Assert
        ResponseValidator.validateStatusCode(response, 400);
    }

    @Test
    @Story("Create gasto único")
    @DisplayName("Should create gasto único for specific category")
    @Description("Verify that a gasto único can be created for a specific category")
    void shouldCreateGastoUnicoForSpecificCategory() {
        // Arrange
        Long categoriaGastoId = 5L; // Automotive category
        GastoUnico gastoUnico = TestDataFactory.createGastoUnicoForCategory(categoriaGastoId);

        // Act
        Response response = gastosUnicosClient.createGastoUnico(gastoUnico);

        // Assert
        ResponseValidator.validateStatusCode(response, 201);
        ResponseValidator.validateFieldExists(response, "id");
        ResponseValidator.validateNumericFieldValue(response, "categoria_gasto_id", categoriaGastoId);
    }
}
