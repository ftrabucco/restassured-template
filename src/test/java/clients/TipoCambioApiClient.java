package clients;

import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import models.TipoCambio;
import utils.AllureAttachments;
import utils.AllureLogger;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;

/**
 * API client for TipoCambio (Exchange Rate) endpoints
 * Maps to actual backend routes:
 *   GET  /api/tipo-cambio/actual      - Get current exchange rate
 *   GET  /api/tipo-cambio/historico    - Get historical rates with filters
 *   POST /api/tipo-cambio/manual      - Set exchange rate manually
 *   POST /api/tipo-cambio/actualizar  - Update from external API
 *   POST /api/tipo-cambio/convertir   - Convert amount between currencies
 */
public class TipoCambioApiClient extends ApiClient {

    private static final String BASE_ENDPOINT = "/api/tipo-cambio";

    public TipoCambioApiClient() {
        super();
    }

    public TipoCambioApiClient(RequestSpecification requestSpec) {
        super();
        this.requestSpec = requestSpec;
    }

    @Override
    @SuppressWarnings("unchecked")
    public TipoCambioApiClient withRequestSpec(RequestSpecification requestSpec) {
        this.requestSpec = requestSpec;
        return this;
    }

    /**
     * GET /api/tipo-cambio/actual - Get current (most recent active) exchange rate
     */
    public Response getTipoCambioActual() {
        AllureLogger.logStep("Getting current tipo cambio");

        Response response = given(requestSpec)
                .when()
                .get(BASE_ENDPOINT + "/actual");

        AllureLogger.attachResponse(response);
        return response;
    }

    /**
     * GET /api/tipo-cambio/historico - Get historical exchange rates with optional filters
     */
    public Response getHistorico() {
        AllureLogger.logStep("Getting historical tipos de cambio");

        Response response = given(requestSpec)
                .when()
                .get(BASE_ENDPOINT + "/historico");

        AllureLogger.attachResponse(response);
        return response;
    }

    /**
     * GET /api/tipo-cambio/historico?fecha_desde=X&fecha_hasta=Y - Get exchange rates by date range
     */
    public Response getHistoricoByDateRange(String fechaDesde, String fechaHasta) {
        AllureLogger.logStep("Getting historico by date range: " + fechaDesde + " to " + fechaHasta);

        Map<String, Object> queryParams = new HashMap<>();
        if (fechaDesde != null) queryParams.put("fecha_desde", fechaDesde);
        if (fechaHasta != null) queryParams.put("fecha_hasta", fechaHasta);

        Response response = given(requestSpec)
                .queryParams(queryParams)
                .when()
                .get(BASE_ENDPOINT + "/historico");

        AllureLogger.attachResponse(response);
        return response;
    }

    /**
     * GET /api/tipo-cambio/historico?fuente=X - Filter historical by source
     */
    public Response getHistoricoByFuente(String fuente) {
        AllureLogger.logStep("Getting historico by fuente: " + fuente);

        Response response = given(requestSpec)
                .queryParam("fuente", fuente)
                .when()
                .get(BASE_ENDPOINT + "/historico");

        AllureLogger.attachResponse(response);
        return response;
    }

    /**
     * POST /api/tipo-cambio/manual - Create exchange rate manually
     */
    public Response createTipoCambioManual(TipoCambio tipoCambio) {
        AllureLogger.logStep("Creating tipo cambio manually");
        AllureAttachments.attachRequestBody(tipoCambio);

        Response response = given(requestSpec)
                .body(tipoCambio)
                .when()
                .post(BASE_ENDPOINT + "/manual");

        AllureLogger.attachResponse(response);
        return response;
    }

    /**
     * POST /api/tipo-cambio/manual - Create exchange rate manually with raw Map data
     */
    public Response createTipoCambioManualWithMap(Map<String, Object> data) {
        AllureLogger.logStep("Creating tipo cambio manually with raw data");
        AllureAttachments.attachRequestBody(data);

        Response response = given(requestSpec)
                .body(data)
                .when()
                .post(BASE_ENDPOINT + "/manual");

        AllureLogger.attachResponse(response);
        return response;
    }

    /**
     * POST /api/tipo-cambio/actualizar - Force update from external APIs (DolarAPI/BCRA)
     */
    public Response actualizarTipoCambio() {
        AllureLogger.logStep("Forcing tipo cambio update from external APIs");

        Response response = given(requestSpec)
                .when()
                .post(BASE_ENDPOINT + "/actualizar");

        AllureLogger.attachResponse(response);
        return response;
    }

    /**
     * POST /api/tipo-cambio/convertir - Convert amount between currencies
     */
    public Response convertirMonto(double monto, String monedaOrigen) {
        AllureLogger.logStep("Converting " + monto + " " + monedaOrigen);

        Map<String, Object> body = new HashMap<>();
        body.put("monto", monto);
        body.put("moneda_origen", monedaOrigen);

        Response response = given(requestSpec)
                .body(body)
                .when()
                .post(BASE_ENDPOINT + "/convertir");

        AllureLogger.attachResponse(response);
        return response;
    }
}
