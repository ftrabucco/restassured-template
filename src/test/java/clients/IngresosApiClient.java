package clients;

import io.qameta.allure.Step;
import io.restassured.response.Response;
import models.Ingreso;

/**
 * API client for Ingresos (Income) endpoints
 */
public class IngresosApiClient extends ApiClient {
    private final String baseEndpoint;

    public IngresosApiClient() {
        super();
        this.baseEndpoint = config.getEndpoint("ingresos");
    }

    @Step("Get all ingresos")
    public Response getAllIngresos() {
        return get(baseEndpoint + "/all");
    }

    @Step("Get ingreso by ID: {ingresoId}")
    public Response getIngresoById(String ingresoId) {
        return get(baseEndpoint + "/" + ingresoId);
    }

    @Step("Create new ingreso")
    public Response createIngreso(Ingreso ingreso) {
        return post(baseEndpoint, ingreso);
    }

    @Step("Update ingreso with ID: {ingresoId}")
    public Response updateIngreso(String ingresoId, Ingreso ingreso) {
        return put(baseEndpoint + "/" + ingresoId, ingreso);
    }

    @Step("Delete ingreso with ID: {ingresoId}")
    public Response deleteIngreso(String ingresoId) {
        return delete(baseEndpoint + "/" + ingresoId);
    }

    @Step("Get ingresos by user: {userId}")
    public Response getIngresosByUser(String userId) {
        return get(baseEndpoint + "/usuario/" + userId);
    }

    @Step("Get ingresos by date range")
    public Response getIngresosByDateRange(String fechaInicio, String fechaFin) {
        return get(baseEndpoint + "/fecha?inicio=" + fechaInicio + "&fin=" + fechaFin);
    }
}
