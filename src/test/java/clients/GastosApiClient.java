package clients;

import io.qameta.allure.Step;
import io.restassured.response.Response;
import models.Gasto;

/**
 * API client for Gastos (Expenses) endpoints
 */
public class GastosApiClient extends ApiClient {
    private final String baseEndpoint;

    public GastosApiClient() {
        super();
        this.baseEndpoint = config.getEndpoint("gastos");
    }

    @Step("Get all gastos")
    public Response getAllGastos() {
        return get(baseEndpoint);
    }

    @Step("Get gasto by ID: {gastoId}")
    public Response getGastoById(String gastoId) {
        return get(baseEndpoint + "/" + gastoId);
    }

    @Step("Create new gasto")
    public Response createGasto(Gasto gasto) {
        return post(baseEndpoint, gasto);
    }

    @Step("Update gasto with ID: {gastoId}")
    public Response updateGasto(String gastoId, Gasto gasto) {
        return put(baseEndpoint + "/" + gastoId, gasto);
    }

    @Step("Delete gasto with ID: {gastoId}")
    public Response deleteGasto(String gastoId) {
        return delete(baseEndpoint + "/" + gastoId);
    }

    @Step("Get gastos with filters")
    public Response getGastosWithFilters(String categoriaId, String fechaDesde, String fechaHasta, 
                                       String montoMin, String montoMax, String tipoPagoId, 
                                       String tarjetaId, String page, String limit) {
        StringBuilder url = new StringBuilder(baseEndpoint);
        boolean hasParams = false;
        
        if (categoriaId != null) {
            url.append(hasParams ? "&" : "?").append("categoria_gasto_id=").append(categoriaId);
            hasParams = true;
        }
        if (fechaDesde != null) {
            url.append(hasParams ? "&" : "?").append("fecha_desde=").append(fechaDesde);
            hasParams = true;
        }
        if (fechaHasta != null) {
            url.append(hasParams ? "&" : "?").append("fecha_hasta=").append(fechaHasta);
            hasParams = true;
        }
        if (montoMin != null) {
            url.append(hasParams ? "&" : "?").append("monto_min=").append(montoMin);
            hasParams = true;
        }
        if (montoMax != null) {
            url.append(hasParams ? "&" : "?").append("monto_max=").append(montoMax);
            hasParams = true;
        }
        if (tipoPagoId != null) {
            url.append(hasParams ? "&" : "?").append("tipo_pago_id=").append(tipoPagoId);
            hasParams = true;
        }
        if (tarjetaId != null) {
            url.append(hasParams ? "&" : "?").append("tarjeta_id=").append(tarjetaId);
            hasParams = true;
        }
        if (page != null) {
            url.append(hasParams ? "&" : "?").append("page=").append(page);
            hasParams = true;
        }
        if (limit != null) {
            url.append(hasParams ? "&" : "?").append("limit=").append(limit);
        }
        
        return get(url.toString());
    }

    @Step("Get expense summary")
    public Response getGastosSummary(String fechaDesde, String fechaHasta) {
        String url = baseEndpoint + "/summary";
        if (fechaDesde != null || fechaHasta != null) {
            url += "?";
            if (fechaDesde != null) {
                url += "fecha_desde=" + fechaDesde;
                if (fechaHasta != null) {
                    url += "&fecha_hasta=" + fechaHasta;
                }
            } else if (fechaHasta != null) {
                url += "fecha_hasta=" + fechaHasta;
            }
        }
        return get(url);
    }

    @Step("Generate pending expenses")
    public Response generateGastos() {
        return get(baseEndpoint + "/generate");
    }

    @Step("Search gastos by tipo_origen and id_origen")
    public Response getGastosByOrigin(String tipoOrigen, String idOrigen) {
        String url = baseEndpoint + "?tipo_origen=" + tipoOrigen + "&id_origen=" + idOrigen;
        return get(url);
    }
}
