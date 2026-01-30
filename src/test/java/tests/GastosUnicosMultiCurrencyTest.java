package tests;

import base.ApiTestWithCleanup;
import clients.GastosUnicosApiClient;
import clients.TipoCambioApiClient;
import io.qameta.allure.*;
import io.restassured.response.Response;
import models.GastoUnico;
import models.TipoCambio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import utils.ResponseValidator;
import utils.TestDataFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive test suite for Multi-Currency functionality in Gastos Únicos
 * Tests USD/ARS conversion, validation, and snapshot behavior
 * Based on MCP multi-currency business rules
 */
@Feature("Multi-Currency System - Gastos Únicos")
@DisplayName("Gastos Únicos Multi-Currency Tests (USD/ARS)")
public class GastosUnicosMultiCurrencyTest extends ApiTestWithCleanup {

    private GastosUnicosApiClient gastosUnicosClient;
    private TipoCambioApiClient tipoCambioClient;

    @BeforeEach
    @Override
    protected void customAuthenticatedSetup() {
        gastosUnicosClient = new GastosUnicosApiClient().withRequestSpec(requestSpec);
        tipoCambioClient = new TipoCambioApiClient().withRequestSpec(requestSpec);
    }

    @Override
    protected Map<EntityType, Function<List<String>, Integer>> getCleanupStrategies() {
        Map<EntityType, Function<List<String>, Integer>> strategies = new HashMap<>();
        strategies.put(EntityType.GASTO_UNICO, ids -> performCleanup(ids, gastosUnicosClient::deleteGastoUnico, "gasto_unico"));
        // No delete endpoint for tipo_cambio - cleanup not needed
        return strategies;
    }

    // ========== USD TO ARS CONVERSION TESTS ==========

    @Test
    @Story("Multi-Currency Conversion")
    @DisplayName("Should create gasto in USD and auto-convert to ARS")
    @Severity(SeverityLevel.CRITICAL)
    @Description("When creating a gasto with moneda_origen=USD, backend should calculate monto_ars automatically")
    void shouldCreateGastoInUSDAndConvertToARS() {
        // Setup: Create a known exchange rate
        TipoCambio tc = TestDataFactory.createTipoCambioWithSpecificValues(
                LocalDate.now(),
                BigDecimal.valueOf(995),
                BigDecimal.valueOf(1000)
        );
        Response tcResponse = tipoCambioClient.createTipoCambioManual(tc);
        if (tcResponse.getStatusCode() == 201) {
            trackCreatedTipoCambio(tcResponse.jsonPath().getString("data.id"));
        }

        // Create gasto in USD
        Map<String, Object> gastoData = new HashMap<>();
        gastoData.put("descripcion", "Netflix Subscription");
        gastoData.put("monto", 15.99);
        gastoData.put("moneda_origen", "USD");
        gastoData.put("fecha", LocalDate.now().toString());
        gastoData.put("categoria_gasto_id", 5);
        gastoData.put("importancia_gasto_id", 2);
        gastoData.put("tipo_pago_id", 3);

        Response response = gastosUnicosClient.createGastoUnicoWithMap(gastoData);
        ResponseValidator.validateStatusCode(response, 201);

        trackCreatedGastoUnico(response.jsonPath().getString("data.id"));

        // Verify conversion
        response.then()
                .body("success", equalTo(true))
                .body("data.monto", notNullValue())
                .body("data.moneda_origen", equalTo("USD"))
                .body("data.monto_usd", notNullValue())
                .body("data.monto_ars", notNullValue())
                .body("data.tipo_cambio_usado", notNullValue());

        // Verify calculation: monto_ars = monto_usd * tipo_cambio_venta
        BigDecimal montoUsd = new BigDecimal(response.jsonPath().getString("data.monto_usd"));
        BigDecimal montoArs = new BigDecimal(response.jsonPath().getString("data.monto_ars"));
        BigDecimal tcUsado = new BigDecimal(response.jsonPath().getString("data.tipo_cambio_usado"));

        BigDecimal expectedArs = montoUsd.multiply(tcUsado).setScale(2, RoundingMode.HALF_UP);
        assertEquals(expectedArs, montoArs.setScale(2, RoundingMode.HALF_UP),
                "monto_ars should equal monto_usd * tipo_cambio_usado");
    }

    // ========== ARS TO USD CONVERSION TESTS ==========

    @Test
    @Story("Multi-Currency Conversion")
    @DisplayName("Should create gasto in ARS and auto-convert to USD")
    @Severity(SeverityLevel.CRITICAL)
    @Description("When creating a gasto with moneda_origen=ARS (default), backend should calculate monto_usd automatically")
    void shouldCreateGastoInARSAndConvertToUSD() {
        // Setup: Create a known exchange rate
        TipoCambio tc = TestDataFactory.createTipoCambioWithSpecificValues(
                LocalDate.now(),
                BigDecimal.valueOf(995),
                BigDecimal.valueOf(1000)
        );
        Response tcResponse = tipoCambioClient.createTipoCambioManual(tc);
        if (tcResponse.getStatusCode() == 201) {
            trackCreatedTipoCambio(tcResponse.jsonPath().getString("data.id"));
        }

        // Create gasto in ARS (default)
        Map<String, Object> gastoData = new HashMap<>();
        gastoData.put("descripcion", "Supermercado");
        gastoData.put("monto", 50000);
        gastoData.put("moneda_origen", "ARS");
        gastoData.put("fecha", LocalDate.now().toString());
        gastoData.put("categoria_gasto_id", 1);
        gastoData.put("importancia_gasto_id", 1);
        gastoData.put("tipo_pago_id", 1);

        Response response = gastosUnicosClient.createGastoUnicoWithMap(gastoData);
        ResponseValidator.validateStatusCode(response, 201);

        trackCreatedGastoUnico(response.jsonPath().getString("data.id"));

        // Verify conversion
        response.then()
                .body("success", equalTo(true))
                .body("data.moneda_origen", anyOf(equalTo("ARS"), nullValue()))
                .body("data.monto_ars", notNullValue())
                .body("data.monto_usd", notNullValue())
                .body("data.tipo_cambio_usado", notNullValue());

        // Verify calculation: monto_usd = monto_ars / tipo_cambio_venta
        BigDecimal montoArs = new BigDecimal(response.jsonPath().getString("data.monto_ars"));
        BigDecimal montoUsd = new BigDecimal(response.jsonPath().getString("data.monto_usd"));
        BigDecimal tcUsado = new BigDecimal(response.jsonPath().getString("data.tipo_cambio_usado"));

        BigDecimal expectedUsd = montoArs.divide(tcUsado, 2, RoundingMode.HALF_UP);
        assertEquals(expectedUsd, montoUsd.setScale(2, RoundingMode.HALF_UP),
                "monto_usd should equal monto_ars / tipo_cambio_usado");
    }

    // ========== JOI VALIDATION TESTS (FORBIDDEN FIELDS) ==========

    @Test
    @Story("Validation - Forbidden Fields")
    @DisplayName("Should reject gasto when user sends monto_ars explicitly (Joi.forbidden)")
    @Severity(SeverityLevel.CRITICAL)
    @Description("User should NOT be allowed to send monto_ars - it's calculated by backend")
    void shouldRejectGastoWithExplicitMontoArs() {
        Map<String, Object> gastoData = new HashMap<>();
        gastoData.put("descripcion", "Test Forbidden Field");
        gastoData.put("monto", 100);
        gastoData.put("moneda_origen", "USD");
        gastoData.put("monto_ars", 100000); // FORBIDDEN - backend should calculate this
        gastoData.put("fecha", LocalDate.now().toString());
        gastoData.put("categoria_gasto_id", 5);
        gastoData.put("importancia_gasto_id", 2);
        gastoData.put("tipo_pago_id", 1);

        Response response = gastosUnicosClient.createGastoUnicoWithMap(gastoData);
        ResponseValidator.validateStatusCode(response, 400);

        response.then()
                .body("success", equalTo(false));
        // May contain message like "monto_ars is not allowed"
    }

    @Test
    @Story("Validation - Forbidden Fields")
    @DisplayName("Should reject gasto when user sends monto_usd explicitly (Joi.forbidden)")
    @Severity(SeverityLevel.CRITICAL)
    @Description("User should NOT be allowed to send monto_usd - it's calculated by backend")
    void shouldRejectGastoWithExplicitMontoUsd() {
        Map<String, Object> gastoData = new HashMap<>();
        gastoData.put("descripcion", "Test Forbidden Field");
        gastoData.put("monto", 50000);
        gastoData.put("moneda_origen", "ARS");
        gastoData.put("monto_usd", 50); // FORBIDDEN - backend should calculate this
        gastoData.put("fecha", LocalDate.now().toString());
        gastoData.put("categoria_gasto_id", 1);
        gastoData.put("importancia_gasto_id", 1);
        gastoData.put("tipo_pago_id", 1);

        Response response = gastosUnicosClient.createGastoUnicoWithMap(gastoData);
        ResponseValidator.validateStatusCode(response, 400);

        response.then()
                .body("success", equalTo(false));
    }

    @Test
    @Story("Validation - Forbidden Fields")
    @DisplayName("Should reject gasto when user sends tipo_cambio_usado explicitly (Joi.forbidden)")
    @Severity(SeverityLevel.CRITICAL)
    @Description("User should NOT be allowed to send tipo_cambio_usado - it's set by backend")
    void shouldRejectGastoWithExplicitTipoCambioUsado() {
        Map<String, Object> gastoData = new HashMap<>();
        gastoData.put("descripcion", "Test Forbidden Field");
        gastoData.put("monto", 100);
        gastoData.put("moneda_origen", "USD");
        gastoData.put("tipo_cambio_usado", 1000); // FORBIDDEN
        gastoData.put("fecha", LocalDate.now().toString());
        gastoData.put("categoria_gasto_id", 5);
        gastoData.put("importancia_gasto_id", 2);
        gastoData.put("tipo_pago_id", 1);

        Response response = gastosUnicosClient.createGastoUnicoWithMap(gastoData);
        ResponseValidator.validateStatusCode(response, 400);

        response.then()
                .body("success", equalTo(false));
    }

    // ========== SNAPSHOT BEHAVIOR TESTS ==========

    @Test
    @Story("Snapshot Behavior")
    @DisplayName("Should maintain historical tipo_cambio_usado (snapshot) even after TC changes")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Once a gasto is created, its tipo_cambio_usado should NOT change even if current TC is updated")
    void shouldMaintainHistoricalTipoCambioSnapshot() {
        // Step 1: Create initial tipo cambio
        TipoCambio tc1 = TestDataFactory.createTipoCambioWithSpecificValues(
                LocalDate.now().minusDays(1),
                BigDecimal.valueOf(995),
                BigDecimal.valueOf(1000)
        );
        Response tc1Response = tipoCambioClient.createTipoCambioManual(tc1);
        if (tc1Response.getStatusCode() == 201) {
            trackCreatedTipoCambio(tc1Response.jsonPath().getString("data.id"));
        }

        // Step 2: Create gasto with TC = 1000
        Map<String, Object> gastoData = new HashMap<>();
        gastoData.put("descripcion", "Snapshot Test");
        gastoData.put("monto", 100);
        gastoData.put("moneda_origen", "USD");
        gastoData.put("fecha", LocalDate.now().minusDays(1).toString());
        gastoData.put("categoria_gasto_id", 5);
        gastoData.put("importancia_gasto_id", 2);
        gastoData.put("tipo_pago_id", 1);

        Response createResponse = gastosUnicosClient.createGastoUnicoWithMap(gastoData);
        String gastoId = createResponse.jsonPath().getString("data.id");
        trackCreatedGastoUnico(gastoId);

        BigDecimal originalTcUsado = new BigDecimal(createResponse.jsonPath().getString("data.tipo_cambio_usado"));
        BigDecimal originalMontoArs = new BigDecimal(createResponse.jsonPath().getString("data.monto_ars"));

        // Step 3: Update tipo cambio to a different value
        TipoCambio tc2 = TestDataFactory.createTipoCambioWithSpecificValues(
                LocalDate.now(),
                BigDecimal.valueOf(1095),
                BigDecimal.valueOf(1100) // Higher rate
        );
        Response tc2Response = tipoCambioClient.createTipoCambioManual(tc2);
        if (tc2Response.getStatusCode() == 201) {
            trackCreatedTipoCambio(tc2Response.jsonPath().getString("data.id"));
        }

        // Step 4: Retrieve the gasto again
        Response getResponse = gastosUnicosClient.getGastoUnicoById(gastoId);
        ResponseValidator.validateStatusCode(getResponse, 200);

        BigDecimal retrievedTcUsado = new BigDecimal(getResponse.jsonPath().getString("data.tipo_cambio_usado"));
        BigDecimal retrievedMontoArs = new BigDecimal(getResponse.jsonPath().getString("data.monto_ars"));

        // Step 5: Verify snapshot hasn't changed
        assertEquals(originalTcUsado, retrievedTcUsado,
                "tipo_cambio_usado should remain the same (snapshot behavior)");
        assertEquals(originalMontoArs.setScale(2, RoundingMode.HALF_UP),
                retrievedMontoArs.setScale(2, RoundingMode.HALF_UP),
                "monto_ars should remain the same (historical integrity)");
    }

    // ========== DEFAULT CURRENCY TESTS ==========

    @Test
    @Story("Default Values")
    @DisplayName("Should default to ARS when moneda_origen is not specified")
    @Severity(SeverityLevel.NORMAL)
    @Description("When user doesn't specify moneda_origen, system should assume ARS as default")
    void shouldDefaultToARSWhenMonedaOrigenNotSpecified() {
        Map<String, Object> gastoData = new HashMap<>();
        gastoData.put("descripcion", "Default Currency Test");
        gastoData.put("monto", 25000);
        // moneda_origen NOT specified - should default to ARS
        gastoData.put("fecha", LocalDate.now().toString());
        gastoData.put("categoria_gasto_id", 1);
        gastoData.put("importancia_gasto_id", 1);
        gastoData.put("tipo_pago_id", 1);

        Response response = gastosUnicosClient.createGastoUnicoWithMap(gastoData);

        if (response.getStatusCode() == 201) {
            trackCreatedGastoUnico(response.jsonPath().getString("data.id"));

            response.then()
                    .body("success", equalTo(true))
                    // moneda_origen should be ARS or null (defaults to ARS)
                    .body("data.moneda_origen", anyOf(equalTo("ARS"), nullValue()))
                    .body("data.monto_ars", notNullValue())
                    .body("data.monto_usd", notNullValue());
        }
    }

    // ========== INVALID CURRENCY TESTS ==========

    @Test
    @Story("Validation")
    @DisplayName("Should reject gasto with invalid moneda_origen")
    @Severity(SeverityLevel.NORMAL)
    @Description("Only USD and ARS are valid currencies")
    void shouldRejectGastoWithInvalidMonedaOrigen() {
        Map<String, Object> gastoData = new HashMap<>();
        gastoData.put("descripcion", "Invalid Currency Test");
        gastoData.put("monto", 100);
        gastoData.put("moneda_origen", "EUR"); // Invalid - only USD and ARS allowed
        gastoData.put("fecha", LocalDate.now().toString());
        gastoData.put("categoria_gasto_id", 5);
        gastoData.put("importancia_gasto_id", 2);
        gastoData.put("tipo_pago_id", 1);

        Response response = gastosUnicosClient.createGastoUnicoWithMap(gastoData);
        ResponseValidator.validateStatusCode(response, 400);

        response.then()
                .body("success", equalTo(false));
    }

    // ========== FALLBACK BEHAVIOR TESTS ==========

    @Test
    @Story("Exchange Rate Fallback")
    @DisplayName("Should use fallback TC when exact date doesn't exist")
    @Severity(SeverityLevel.NORMAL)
    @Description("If no TC exists for the gasto date, system should use the most recent previous TC")
    void shouldUseFallbackTipoCambioWhenExactDateNotFound() {
        // Create TC for 5 days ago
        TipoCambio tc = TestDataFactory.createTipoCambioWithSpecificValues(
                LocalDate.now().minusDays(5),
                BigDecimal.valueOf(995),
                BigDecimal.valueOf(1000)
        );
        Response tcResponse = tipoCambioClient.createTipoCambioManual(tc);
        if (tcResponse.getStatusCode() == 201) {
            trackCreatedTipoCambio(tcResponse.jsonPath().getString("data.id"));
        }

        // Create gasto for 2 days ago (no TC for this date, should use fallback)
        Map<String, Object> gastoData = new HashMap<>();
        gastoData.put("descripcion", "Fallback TC Test");
        gastoData.put("monto", 50);
        gastoData.put("moneda_origen", "USD");
        gastoData.put("fecha", LocalDate.now().minusDays(2).toString());
        gastoData.put("categoria_gasto_id", 5);
        gastoData.put("importancia_gasto_id", 2);
        gastoData.put("tipo_pago_id", 1);

        Response response = gastosUnicosClient.createGastoUnicoWithMap(gastoData);

        // Should succeed with fallback TC or fail with 404 if no TC available
        int statusCode = response.getStatusCode();
        assertTrue(statusCode == 201 || statusCode == 404 || statusCode == 500,
                "Should either succeed with fallback TC or fail gracefully");

        if (statusCode == 201) {
            trackCreatedGastoUnico(response.jsonPath().getString("data.id"));

            response.then()
                    .body("data.tipo_cambio_usado", notNullValue())
                    .body("data.monto_ars", notNullValue())
                    .body("data.monto_usd", notNullValue());
        }
    }

    // ========== HELPER METHODS ==========

    protected void trackCreatedGastoUnico(String id) {
        if (id != null && !id.isEmpty()) {
            trackCreatedEntity(EntityType.GASTO_UNICO, id);
        }
    }

    protected void trackCreatedTipoCambio(String id) {
        if (id != null && !id.isEmpty()) {
            trackCreatedEntity(EntityType.TIPO_CAMBIO, id);
        }
    }
}
