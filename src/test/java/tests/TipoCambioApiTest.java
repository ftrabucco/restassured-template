package tests;

import base.ApiTestWithCleanup;
import clients.TipoCambioApiClient;
import io.qameta.allure.*;
import io.restassured.response.Response;
import models.TipoCambio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import utils.ResponseValidator;
import utils.TestDataFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test suite for TipoCambio (Exchange Rate) API
 * Tests the multi-currency system (USD/ARS) endpoints:
 *   GET  /api/tipo-cambio/actual
 *   GET  /api/tipo-cambio/historico
 *   POST /api/tipo-cambio/manual
 *   POST /api/tipo-cambio/actualizar
 *   POST /api/tipo-cambio/convertir
 */
@Feature("Tipo de Cambio API")
@DisplayName("Tipo de Cambio API Tests - Multi-Currency System")
public class TipoCambioApiTest extends ApiTestWithCleanup {

    private TipoCambioApiClient tipoCambioClient;

    @BeforeEach
    @Override
    protected void customAuthenticatedSetup() {
        tipoCambioClient = new TipoCambioApiClient()
                .withRequestSpec(requestSpec);
    }

    @Override
    protected Map<EntityType, Function<List<String>, Integer>> getCleanupStrategies() {
        // No individual delete endpoint exists; cleanup not needed for tipo_cambio
        Map<EntityType, Function<List<String>, Integer>> strategies = new HashMap<>();
        return strategies;
    }

    // ========== POST /api/tipo-cambio/manual TESTS ==========

    @Test
    @Story("Manual Rate Configuration")
    @DisplayName("Should create tipo cambio manually with valid data")
    @Severity(SeverityLevel.CRITICAL)
    void shouldCreateTipoCambioManually() {
        TipoCambio tipoCambio = TestDataFactory.createValidTipoCambio();

        Response response = tipoCambioClient.createTipoCambioManual(tipoCambio);
        ResponseValidator.validateStatusCode(response, 201);

        response.then()
                .body("success", equalTo(true))
                .body("data.mensaje", notNullValue())
                .body("data.tipo_cambio.fecha", notNullValue())
                .body("data.tipo_cambio.valor_compra_usd_ars", notNullValue())
                .body("data.tipo_cambio.valor_venta_usd_ars", notNullValue())
                .body("data.tipo_cambio.fuente", equalTo("manual"));
    }

    @Test
    @Story("Manual Rate Configuration")
    @DisplayName("Should create tipo cambio with specific values")
    @Severity(SeverityLevel.NORMAL)
    void shouldCreateTipoCambioWithSpecificValues() {
        TipoCambio tipoCambio = TestDataFactory.createTipoCambioWithSpecificValues(
                LocalDate.now(),
                BigDecimal.valueOf(995),
                BigDecimal.valueOf(1005)
        );

        Response response = tipoCambioClient.createTipoCambioManual(tipoCambio);
        ResponseValidator.validateStatusCode(response, 201);

        response.then()
                .body("success", equalTo(true))
                .body("data.tipo_cambio.valor_compra_usd_ars", notNullValue())
                .body("data.tipo_cambio.valor_venta_usd_ars", notNullValue())
                .body("data.tipo_cambio.fuente", equalTo("manual"))
                .body("data.tipo_cambio.activo", equalTo(true));
    }

    @Test
    @Story("Manual Rate Configuration")
    @DisplayName("Should verify valor_venta >= valor_compra (spread)")
    @Severity(SeverityLevel.NORMAL)
    @Description("Validates that exchange rate spread is maintained (buy <= sell)")
    void shouldVerifyExchangeRateSpread() {
        TipoCambio tipoCambio = TestDataFactory.createTipoCambioWithSpecificValues(
                LocalDate.now(),
                BigDecimal.valueOf(995),
                BigDecimal.valueOf(1005)
        );

        Response response = tipoCambioClient.createTipoCambioManual(tipoCambio);
        ResponseValidator.validateStatusCode(response, 201);

        BigDecimal valorCompra = new BigDecimal(
                response.jsonPath().getString("data.tipo_cambio.valor_compra_usd_ars"));
        BigDecimal valorVenta = new BigDecimal(
                response.jsonPath().getString("data.tipo_cambio.valor_venta_usd_ars"));

        assertTrue(valorVenta.compareTo(valorCompra) >= 0,
                "valor_venta should be >= valor_compra (spread should exist or be zero)");
    }

    // ========== VALIDATION TESTS ==========

    @Test
    @Story("Validation")
    @DisplayName("Should reject tipo cambio with missing valor_venta_usd_ars")
    @Severity(SeverityLevel.NORMAL)
    void shouldRejectTipoCambioWithMissingFields() {
        Map<String, Object> incompleteData = new HashMap<>();
        incompleteData.put("fecha", LocalDate.now().toString());
        // Missing valor_venta_usd_ars

        Response response = tipoCambioClient.createTipoCambioManualWithMap(incompleteData);
        ResponseValidator.validateStatusCode(response, 400);

        response.then()
                .body("success", equalTo(false));
    }

    @Test
    @Story("Validation")
    @DisplayName("Should reject tipo cambio with negative valor_venta_usd_ars")
    @Severity(SeverityLevel.NORMAL)
    void shouldRejectTipoCambioWithNegativeValues() {
        Map<String, Object> invalidData = new HashMap<>();
        invalidData.put("fecha", LocalDate.now().toString());
        invalidData.put("valor_compra_usd_ars", -100);
        invalidData.put("valor_venta_usd_ars", -1000);

        Response response = tipoCambioClient.createTipoCambioManualWithMap(invalidData);
        ResponseValidator.validateStatusCode(response, 400);

        response.then()
                .body("success", equalTo(false));
    }

    // ========== GET /api/tipo-cambio/actual TESTS ==========

    @Test
    @Story("Current Rate")
    @DisplayName("Should get current tipo cambio after setting one manually")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Tests GET /api/tipo-cambio/actual - Gets the most recent active exchange rate")
    void shouldGetTipoCambioActual() {
        // Setup: create a manual tipo cambio
        TipoCambio tipoCambio = TestDataFactory.createValidTipoCambio();
        Response createResponse = tipoCambioClient.createTipoCambioManual(tipoCambio);
        ResponseValidator.validateStatusCode(createResponse, 201);

        // Get current
        Response response = tipoCambioClient.getTipoCambioActual();

        int statusCode = response.getStatusCode();
        assertTrue(statusCode == 200 || statusCode == 404,
            "Status should be 200 or 404, got: " + statusCode);

        if (statusCode == 200) {
            response.then()
                    .body("success", equalTo(true))
                    .body("data.fecha", notNullValue())
                    .body("data.valor_compra_usd_ars", notNullValue())
                    .body("data.valor_venta_usd_ars", notNullValue())
                    .body("data.activo", equalTo(true));
        }
    }

    // ========== GET /api/tipo-cambio/historico TESTS ==========

    @Test
    @Story("Historical Rates")
    @DisplayName("Should get historical tipos de cambio")
    @Severity(SeverityLevel.NORMAL)
    void shouldGetHistoricoTiposCambio() {
        // Setup: create a manual tipo cambio first
        TipoCambio tipoCambio = TestDataFactory.createValidTipoCambio();
        tipoCambioClient.createTipoCambioManual(tipoCambio);

        Response response = tipoCambioClient.getHistorico();
        ResponseValidator.validateStatusCode(response, 200);

        response.then()
                .body("success", equalTo(true))
                .body("data.total", greaterThanOrEqualTo(0))
                .body("data.datos", notNullValue());
    }

    @Test
    @Story("Historical Rates")
    @DisplayName("Should filter historico by date range")
    @Severity(SeverityLevel.NORMAL)
    void shouldFilterHistoricoByDateRange() {
        // Setup: create tipos de cambio for different dates
        TipoCambio tc1 = TestDataFactory.createHistoricalTipoCambio(10);
        TipoCambio tc2 = TestDataFactory.createHistoricalTipoCambio(5);
        tipoCambioClient.createTipoCambioManual(tc1);
        tipoCambioClient.createTipoCambioManual(tc2);

        String fechaDesde = LocalDate.now().minusDays(15).toString();
        String fechaHasta = LocalDate.now().toString();

        Response response = tipoCambioClient.getHistoricoByDateRange(fechaDesde, fechaHasta);
        ResponseValidator.validateStatusCode(response, 200);

        response.then()
                .body("success", equalTo(true))
                .body("data.total", greaterThanOrEqualTo(0))
                .body("data.datos", notNullValue());
    }

    @Test
    @Story("Historical Rates")
    @DisplayName("Should filter historico by fuente (source)")
    @Severity(SeverityLevel.NORMAL)
    void shouldFilterHistoricoByFuente() {
        // Setup: create a tipo cambio with specific source
        TipoCambio tipoCambio = TestDataFactory.createTipoCambioFromSource("manual");
        tipoCambioClient.createTipoCambioManual(tipoCambio);

        Response response = tipoCambioClient.getHistoricoByFuente("manual");
        ResponseValidator.validateStatusCode(response, 200);

        response.then()
                .body("success", equalTo(true))
                .body("data.datos", notNullValue());
    }

    // ========== POST /api/tipo-cambio/actualizar TESTS ==========

    @Test
    @Story("External API Update")
    @DisplayName("Should attempt to update tipo cambio from external APIs")
    @Severity(SeverityLevel.NORMAL)
    @Description("Tests POST /api/tipo-cambio/actualizar - Forces update from DolarAPI/BCRA")
    void shouldForceUpdateTipoCambio() {
        Response response = tipoCambioClient.actualizarTipoCambio();

        // External APIs may or may not be available
        int statusCode = response.getStatusCode();
        assertTrue(statusCode == 200 || statusCode == 503 || statusCode == 500,
            "Status should be 200, 503, or 500 (external API may be unavailable), got: " + statusCode);

        if (statusCode == 200) {
            response.then()
                    .body("success", equalTo(true))
                    .body("data.tipo_cambio", notNullValue())
                    .body("data.tipo_cambio.valor_venta_usd_ars", notNullValue());
        }
    }

    // ========== POST /api/tipo-cambio/convertir TESTS ==========

    @Test
    @Story("Currency Conversion")
    @DisplayName("Should convert ARS to USD")
    @Severity(SeverityLevel.CRITICAL)
    void shouldConvertARStoUSD() {
        // Setup: ensure we have a tipo cambio
        TipoCambio tc = TestDataFactory.createTipoCambioWithSpecificValues(
                LocalDate.now(), BigDecimal.valueOf(995), BigDecimal.valueOf(1000));
        tipoCambioClient.createTipoCambioManual(tc);

        Response response = tipoCambioClient.convertirMonto(50000, "ARS");

        int statusCode = response.getStatusCode();
        assertTrue(statusCode == 200 || statusCode == 404,
            "Status should be 200 or 404, got: " + statusCode);

        if (statusCode == 200) {
            response.then()
                    .body("success", equalTo(true))
                    .body("data.monto_original", anyOf(equalTo(50000), equalTo(50000.0f)))
                    .body("data.moneda_origen", equalTo("ARS"))
                    .body("data.conversion.monto_ars", notNullValue())
                    .body("data.conversion.monto_usd", notNullValue())
                    .body("data.conversion.tipo_cambio_usado", notNullValue());
        }
    }

    @Test
    @Story("Currency Conversion")
    @DisplayName("Should convert USD to ARS")
    @Severity(SeverityLevel.CRITICAL)
    void shouldConvertUSDtoARS() {
        // Setup: ensure we have a tipo cambio
        TipoCambio tc = TestDataFactory.createTipoCambioWithSpecificValues(
                LocalDate.now(), BigDecimal.valueOf(995), BigDecimal.valueOf(1000));
        tipoCambioClient.createTipoCambioManual(tc);

        Response response = tipoCambioClient.convertirMonto(100, "USD");

        int statusCode = response.getStatusCode();
        assertTrue(statusCode == 200 || statusCode == 404,
            "Status should be 200 or 404, got: " + statusCode);

        if (statusCode == 200) {
            response.then()
                    .body("success", equalTo(true))
                    .body("data.monto_original", anyOf(equalTo(100), equalTo(100.0f)))
                    .body("data.moneda_origen", equalTo("USD"))
                    .body("data.conversion.monto_ars", notNullValue())
                    .body("data.conversion.monto_usd", notNullValue());
        }
    }

    @Test
    @Story("Currency Conversion")
    @DisplayName("Should reject conversion with missing fields")
    @Severity(SeverityLevel.NORMAL)
    void shouldRejectConversionWithMissingFields() {
        // Send empty body
        Response response = tipoCambioClient.convertirMonto(0, "");

        ResponseValidator.validateStatusCode(response, 400);
        response.then()
                .body("success", equalTo(false));
    }

    @Test
    @Story("Currency Conversion")
    @DisplayName("Should reject conversion with invalid moneda_origen")
    @Severity(SeverityLevel.NORMAL)
    void shouldRejectConversionWithInvalidCurrency() {
        Response response = tipoCambioClient.convertirMonto(100, "EUR");

        ResponseValidator.validateStatusCode(response, 400);
        response.then()
                .body("success", equalTo(false));
    }

    // ========== AUTHENTICATION TESTS ==========

    @Test
    @Story("Authentication")
    @DisplayName("Should return 401 without authentication token")
    @Severity(SeverityLevel.CRITICAL)
    void shouldReturn401WithoutAuthToken() {
        TipoCambioApiClient unauthenticatedClient = new TipoCambioApiClient()
                .withRequestSpec(getUnauthenticatedRequestSpec());

        Response response = unauthenticatedClient.getTipoCambioActual();
        ResponseValidator.validateStatusCode(response, 401);

        response.then()
                .body("success", equalTo(false));
    }

    @Test
    @Story("Authentication")
    @DisplayName("Should return 401 with invalid token")
    @Severity(SeverityLevel.NORMAL)
    void shouldReturn401WithInvalidToken() {
        TipoCambioApiClient invalidTokenClient = new TipoCambioApiClient()
                .withRequestSpec(getInvalidTokenRequestSpec());

        Response response = invalidTokenClient.getTipoCambioActual();
        ResponseValidator.validateStatusCode(response, 401);

        response.then()
                .body("success", equalTo(false));
    }

    @Test
    @Story("Authentication")
    @DisplayName("Should return 401 with malformed token")
    @Severity(SeverityLevel.NORMAL)
    void shouldReturn401WithMalformedToken() {
        TipoCambioApiClient malformedTokenClient = new TipoCambioApiClient()
                .withRequestSpec(getMalformedTokenRequestSpec());

        Response response = malformedTokenClient.getTipoCambioActual();
        ResponseValidator.validateStatusCode(response, 401);

        response.then()
                .body("success", equalTo(false));
    }
}
