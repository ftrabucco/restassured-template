package clients;

import io.qameta.allure.Step;
import io.restassured.response.Response;
import models.GastoUnico;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * API client for Gastos Únicos (One-time Expenses) endpoints
 */
public class GastosUnicosApiClient extends ApiClient {
    private final String baseEndpoint;
    private final ObjectMapper objectMapper;

    public GastosUnicosApiClient() {
        super();
        this.baseEndpoint = config.getEndpoint("gastos_unicos");
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Step("Get all gastos únicos")
    public Response getAllGastosUnicos() {
        return get(baseEndpoint);
    }

    @Step("Get gasto único by ID: {gastoUnicoId}")
    public Response getGastoUnicoById(String gastoUnicoId) {
        return get(baseEndpoint + "/" + gastoUnicoId);
    }

    @Step("Create new gasto único")
    public Response createGastoUnico(GastoUnico gastoUnico) {
        return post(baseEndpoint, gastoUnico);
    }

    @Step("Update gasto único with ID: {gastoUnicoId}")
    public Response updateGastoUnico(String gastoUnicoId, GastoUnico gastoUnico) {
        String endpoint = baseEndpoint + "/" + gastoUnicoId;
        return put(endpoint, gastoUnico);
    }

    @Step("Update gasto único with ID: {gastoUnicoId} using Map payload")
    public Response updateGastoUnico(String gastoUnicoId, java.util.Map<String, Object> updatePayload) {
        String endpoint = baseEndpoint + "/" + gastoUnicoId;
        return put(endpoint, updatePayload);
    }

    @Step("Delete gasto único with ID: {gastoUnicoId}")
    public Response deleteGastoUnico(String gastoUnicoId) {
        return delete(baseEndpoint + "/" + gastoUnicoId);
    }

    @Step("Search gastos únicos with filters")
    public Response getGastosUnicosWithFilters(Integer categoriaGastoId, String fechaDesde, String fechaHasta,
                                             Double montoMin, Double montoMax, Integer importanciaGastoId,
                                             Integer tipoPagoId, Boolean procesado) {
        StringBuilder url = new StringBuilder(baseEndpoint + "?");
        boolean hasParam = false;

        if (categoriaGastoId != null) {
            url.append("categoria_gasto_id=").append(categoriaGastoId);
            hasParam = true;
        }
        if (fechaDesde != null) {
            if (hasParam) url.append("&");
            url.append("fecha_desde=").append(fechaDesde);
            hasParam = true;
        }
        if (fechaHasta != null) {
            if (hasParam) url.append("&");
            url.append("fecha_hasta=").append(fechaHasta);
            hasParam = true;
        }
        if (montoMin != null) {
            if (hasParam) url.append("&");
            url.append("monto_min=").append(montoMin);
            hasParam = true;
        }
        if (montoMax != null) {
            if (hasParam) url.append("&");
            url.append("monto_max=").append(montoMax);
            hasParam = true;
        }
        if (importanciaGastoId != null) {
            if (hasParam) url.append("&");
            url.append("importancia_gasto_id=").append(importanciaGastoId);
            hasParam = true;
        }
        if (tipoPagoId != null) {
            if (hasParam) url.append("&");
            url.append("tipo_pago_id=").append(tipoPagoId);
            hasParam = true;
        }
        if (procesado != null) {
            if (hasParam) url.append("&");
            url.append("procesado=").append(procesado);
            hasParam = true;
        }

        return get(url.toString());
    }
}
