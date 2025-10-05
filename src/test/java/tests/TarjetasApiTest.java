package tests;

import base.ApiTestWithCleanup;
import clients.TarjetasApiClient;
import io.restassured.response.Response;
import models.Tarjeta;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import utils.ResponseValidator;
import utils.TestDataFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static org.hamcrest.Matchers.*;

public class TarjetasApiTest extends ApiTestWithCleanup {

    private TarjetasApiClient tarjetasClient;

    @BeforeEach
    @Override
    protected void customAuthenticatedSetup() {
        tarjetasClient = new TarjetasApiClient()
                .withRequestSpec(requestSpec);
    }

    @Override
    protected Map<EntityType, Function<List<String>, Integer>> getCleanupStrategies() {
        Map<EntityType, Function<List<String>, Integer>> strategies = new HashMap<>();
        strategies.put(EntityType.TARJETA, ids -> performCleanup(ids, tarjetasClient::deleteTarjeta, "tarjeta"));
        return strategies;
    }

    @Test
    void shouldGetAllTarjetas() {
        Response response = tarjetasClient.getAllTarjetas();
        ResponseValidator.validateStatusCode(response, 200);
        ResponseValidator.validateFieldExists(response, "success");
    }

    @Test
    void shouldCreateCreditCard() {
        Tarjeta creditCard = TestDataFactory.createValidCreditCard();

        Response response = tarjetasClient.createTarjeta(creditCard);
        ResponseValidator.validateStatusCode(response, 201);
        ResponseValidator.validateFieldExists(response, "success");

        String tarjetaId = response.jsonPath().getString("data.id");
        trackCreatedTarjeta(tarjetaId);

        response.then()
                .body("data.nombre", equalTo(creditCard.getNombre()))
                .body("data.tipo", equalTo("credito"))
                .body("data.banco", equalTo(creditCard.getBanco()));
    }

    @Test
    void shouldCreateDebitCard() {
        Tarjeta debitCard = TestDataFactory.createValidDebitCard();

        Response response = tarjetasClient.createTarjeta(debitCard);
        ResponseValidator.validateStatusCode(response, 201);
        ResponseValidator.validateFieldExists(response, "success");

        String tarjetaId = response.jsonPath().getString("data.id");
        trackCreatedTarjeta(tarjetaId);

        response.then()
                .body("data.nombre", equalTo(debitCard.getNombre()))
                .body("data.tipo", equalTo("debito"))
                .body("data.banco", equalTo(debitCard.getBanco()));
    }

    @Test
    void shouldGetTarjetaById() {
        Tarjeta tarjeta = TestDataFactory.createValidCreditCard();
        Response createResponse = tarjetasClient.createTarjeta(tarjeta);
        String tarjetaId = createResponse.jsonPath().getString("data.id");
        trackCreatedTarjeta(tarjetaId);

        Response response = tarjetasClient.getTarjetaById(tarjetaId);
        ResponseValidator.validateStatusCode(response, 200);
        response.then()
                .body("data.id", equalTo(Integer.valueOf(tarjetaId)))
                .body("data.nombre", equalTo(tarjeta.getNombre()));
    }

    @Test
    void shouldUpdateTarjeta() {
        Tarjeta tarjeta = TestDataFactory.createValidCreditCard();
        Response createResponse = tarjetasClient.createTarjeta(tarjeta);
        String tarjetaId = createResponse.jsonPath().getString("data.id");
        trackCreatedTarjeta(tarjetaId);

        tarjeta.setNombre("Tarjeta Actualizada");
        Response response = tarjetasClient.updateTarjeta(tarjetaId, tarjeta);
        ResponseValidator.validateStatusCode(response, 200);
        response.then()
                .body("data.nombre", equalTo("Tarjeta Actualizada"));
    }

    @Test
    void shouldDeleteTarjeta() {
        Tarjeta tarjeta = TestDataFactory.createValidCreditCard();
        Response createResponse = tarjetasClient.createTarjeta(tarjeta);
        String tarjetaId = createResponse.jsonPath().getString("data.id");

        Response deleteResponse = tarjetasClient.deleteTarjeta(tarjetaId);
        ResponseValidator.validateStatusCode(deleteResponse, 200);

        Response getResponse = tarjetasClient.getTarjetaById(tarjetaId);
        ResponseValidator.validateStatusCode(getResponse, 404);
    }

    @Test
    void shouldFilterTarjetasByTipo() {
        Tarjeta creditCard = TestDataFactory.createValidCreditCard();
        Tarjeta debitCard = TestDataFactory.createValidDebitCard();

        Response creditResponse = tarjetasClient.createTarjeta(creditCard);
        Response debitResponse = tarjetasClient.createTarjeta(debitCard);

        trackCreatedTarjeta(creditResponse.jsonPath().getString("data.id"));
        trackCreatedTarjeta(debitResponse.jsonPath().getString("data.id"));

        Response creditFilterResponse = tarjetasClient.getTarjetasByTipo("credito");
        ResponseValidator.validateStatusCode(creditFilterResponse, 200);
        creditFilterResponse.then()
                .body("data", not(empty()))
                .body("data.findAll { it.tipo == 'credito' }.size()", greaterThan(0));

        Response debitFilterResponse = tarjetasClient.getTarjetasByTipo("debito");
        ResponseValidator.validateStatusCode(debitFilterResponse, 200);
        debitFilterResponse.then()
                .body("data", not(empty()))
                .body("data.findAll { it.tipo == 'debito' }.size()", greaterThan(0));
    }

    @Test
    void shouldFilterTarjetasByBanco() {
        Tarjeta tarjeta = TestDataFactory.createValidCreditCard();
        Response createResponse = tarjetasClient.createTarjeta(tarjeta);
        trackCreatedTarjeta(createResponse.jsonPath().getString("data.id"));

        Response response = tarjetasClient.getTarjetasByBanco(tarjeta.getBanco());
        ResponseValidator.validateStatusCode(response, 200);
        response.then()
                .body("data", not(empty()))
                .body("data.findAll { it.banco == '" + tarjeta.getBanco() + "' }.size()", greaterThan(0));
    }

    @Test
    void shouldValidateRequiredFields() {
        Map<String, Object> incompleteData = new HashMap<>();
        incompleteData.put("banco", "Banco Test");

        Response response = tarjetasClient.createCardWithMissingFields(incompleteData);
        ResponseValidator.validateStatusCode(response, 400);
        response.then()
                .body("success", equalTo(false));
    }

    @Test
    void shouldHandleInvalidTipoTarjeta() {
        Map<String, Object> invalidData = new HashMap<>();
        invalidData.put("nombre", "Tarjeta Inválida");
        invalidData.put("tipo", "invalido");
        invalidData.put("banco", "Banco Test");
        invalidData.put("usuario_id", 1);

        Response response = tarjetasClient.createTarjetaWithInvalidData(invalidData);
        ResponseValidator.validateStatusCode(response, 400);
        response.then()
                .body("success", equalTo(false));
    }

    @Test
    void shouldHandleNonExistentTarjeta() {
        Response response = tarjetasClient.getTarjetaById("99999");
        ResponseValidator.validateStatusCode(response, 404);
        response.then()
                .body("success", equalTo(false));
    }

    @Test
    void shouldUpdateNonExistentTarjeta() {
        Tarjeta tarjeta = TestDataFactory.createValidCreditCard();
        Response response = tarjetasClient.updateNonExistentCard("99999", tarjeta);
        ResponseValidator.validateStatusCode(response, 404);
        response.then()
                .body("success", equalTo(false));
    }

    @Test
    void shouldDeleteNonExistentTarjeta() {
        Response response = tarjetasClient.deleteNonExistentCard("99999");
        ResponseValidator.validateStatusCode(response, 404);
        response.then()
                .body("success", equalTo(false));
    }

    @Test
    void shouldReturn401WithoutAuthToken() {
        TarjetasApiClient unauthenticatedClient = new TarjetasApiClient()
                .withRequestSpec(getUnauthenticatedRequestSpec());

        Response response = unauthenticatedClient.getAllTarjetas();
        ResponseValidator.validateStatusCode(response, 401);
        response.then()
                .body("success", equalTo(false))
                .body("message", equalTo("Token de acceso requerido"));
    }

    @Test
    void shouldReturn401WithInvalidToken() {
        TarjetasApiClient invalidTokenClient = new TarjetasApiClient()
                .withRequestSpec(getInvalidTokenRequestSpec());

        Response response = invalidTokenClient.getAllTarjetas();
        ResponseValidator.validateStatusCode(response, 401);
        response.then()
                .body("success", equalTo(false));
    }

    @Test
    void shouldReturn401WithMalformedToken() {
        TarjetasApiClient malformedTokenClient = new TarjetasApiClient()
                .withRequestSpec(getMalformedTokenRequestSpec());

        Response response = malformedTokenClient.getAllTarjetas();
        ResponseValidator.validateStatusCode(response, 401);
        response.then()
                .body("success", equalTo(false));
    }
}