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
        return get(baseEndpoint + "/all");
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

    @Step("Get gastos by category: {categoria}")
    public Response getGastosByCategoria(String categoria) {
        return get(baseEndpoint + "/categoria/" + categoria);
    }

    @Step("Get gastos by date range")
    public Response getGastosByDateRange(String fechaInicio, String fechaFin) {
        return get(baseEndpoint + "/fecha?inicio=" + fechaInicio + "&fin=" + fechaFin);
    }

    @Step("Get gastos by user: {userId}")
    public Response getGastosByUser(String userId) {
        return get(baseEndpoint + "/usuario/" + userId);
    }
}
