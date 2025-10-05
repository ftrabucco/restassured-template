package tests;

import base.BaseTest;
import com.fasterxml.jackson.databind.JsonNode;
import io.qameta.allure.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import utils.McpServerConnector;

import static org.junit.jupiter.api.Assertions.*;

@Epic("Finance App API - Tarjetas Management")
@Feature("MCP Server Integration - Tarjetas Endpoint Information")
public class TarjetasEndpointInfoTest extends BaseTest {

    @Test
    @DisplayName("Connect to MCP server and retrieve tarjetas endpoint information")
    @Story("Tarjetas Endpoint Discovery")
    @Severity(SeverityLevel.CRITICAL)
    @Description("""
            This test connects to the MCP server to gather comprehensive information about
            the tarjetas (cards) endpoints for both credit and debit cards.

            Test validates:
            - MCP server connectivity
            - Tarjetas endpoint availability and structure
            - Request/response schemas for card operations
            - Business rules and validation requirements
            - Relationships with other endpoints
            """)
    void testGetTarjetasEndpointInfo() {
        McpServerConnector mcpConnector = McpServerConnector.getInstance();

        // Get tarjetas endpoint information from MCP server
        JsonNode tarjetasInfo = mcpConnector.getTarjetasInfo();

        // Validate that we received tarjetas information
        assertNotNull(tarjetasInfo, "Tarjetas information should not be null");
        assertTrue(tarjetasInfo.has("tarjetas_endpoints"), "Response should contain tarjetas_endpoints");

        JsonNode endpoints = tarjetasInfo.get("tarjetas_endpoints");

        // Validate base endpoints structure
        validateBaseEndpoints(endpoints);

        // Validate card types information
        validateCardTypes(endpoints);

        // Validate request schemas
        validateRequestSchemas(endpoints);

        // Validate response schemas
        validateResponseSchemas(endpoints);

        // Validate relationships with other endpoints
        validateRelationships(endpoints);

        // Validate business rules
        validateBusinessRules(endpoints);

        // Validate validation rules
        validateValidationRules(endpoints);

        // Log complete tarjetas information for analysis
        logTarjetasEndpointSummary(endpoints);
    }

    @Step("Validate base endpoints structure")
    private void validateBaseEndpoints(JsonNode endpoints) {
        assertTrue(endpoints.has("base_endpoints"), "Should contain base_endpoints");
        JsonNode baseEndpoints = endpoints.get("base_endpoints");
        assertTrue(baseEndpoints.isArray(), "base_endpoints should be an array");
        assertTrue(baseEndpoints.size() > 0, "Should have at least one endpoint");

        // Check for expected endpoints
        boolean hasMainTarjetasEndpoint = false;
        boolean hasCreditoEndpoint = false;
        boolean hasDebitoEndpoint = false;

        for (JsonNode endpoint : baseEndpoints) {
            String path = endpoint.get("path").asText();
            if ("/api/tarjetas".equals(path)) {
                hasMainTarjetasEndpoint = true;
                validateEndpointMethods(endpoint, "Main tarjetas endpoint");
            } else if ("/api/tarjetas-credito".equals(path)) {
                hasCreditoEndpoint = true;
                validateEndpointMethods(endpoint, "Credit cards endpoint");
            } else if ("/api/tarjetas-debito".equals(path)) {
                hasDebitoEndpoint = true;
                validateEndpointMethods(endpoint, "Debit cards endpoint");
            }
        }

        assertTrue(hasMainTarjetasEndpoint, "Should have main /api/tarjetas endpoint");
        assertTrue(hasCreditoEndpoint, "Should have /api/tarjetas-credito endpoint");
        assertTrue(hasDebitoEndpoint, "Should have /api/tarjetas-debito endpoint");
    }

    @Step("Validate endpoint methods: {endpointName}")
    private void validateEndpointMethods(JsonNode endpoint, String endpointName) {
        assertTrue(endpoint.has("methods"), endpointName + " should have methods");
        JsonNode methods = endpoint.get("methods");
        assertTrue(methods.isArray(), endpointName + " methods should be an array");
        assertTrue(methods.size() > 0, endpointName + " should have at least one method");

        // Check for common REST methods
        boolean hasGet = false, hasPost = false;
        for (JsonNode method : methods) {
            String methodName = method.asText();
            if ("GET".equals(methodName)) hasGet = true;
            if ("POST".equals(methodName)) hasPost = true;
        }

        assertTrue(hasGet, endpointName + " should support GET method");
        assertTrue(hasPost, endpointName + " should support POST method");
    }

    @Step("Validate card types information")
    private void validateCardTypes(JsonNode endpoints) {
        assertTrue(endpoints.has("card_types"), "Should contain card_types");
        JsonNode cardTypes = endpoints.get("card_types");

        // Validate credit card type
        assertTrue(cardTypes.has("credito"), "Should have credito card type");
        JsonNode creditCard = cardTypes.get("credito");
        assertTrue(creditCard.has("properties"), "Credit card should have properties");
        assertTrue(creditCard.has("business_rules"), "Credit card should have business_rules");

        // Validate debit card type
        assertTrue(cardTypes.has("debito"), "Should have debito card type");
        JsonNode debitCard = cardTypes.get("debito");
        assertTrue(debitCard.has("properties"), "Debit card should have properties");
        assertTrue(debitCard.has("business_rules"), "Debit card should have business_rules");

        // Validate essential properties for credit cards
        JsonNode creditProps = creditCard.get("properties");
        assertTrue(creditProps.isArray(), "Credit card properties should be an array");
        boolean hasNumeroTarjeta = false, hasLimiteCredito = false;
        for (JsonNode prop : creditProps) {
            String propName = prop.asText();
            if ("numero_tarjeta".equals(propName)) hasNumeroTarjeta = true;
            if ("limite_credito".equals(propName)) hasLimiteCredito = true;
        }
        assertTrue(hasNumeroTarjeta, "Credit card should have numero_tarjeta property");
        assertTrue(hasLimiteCredito, "Credit card should have limite_credito property");
    }

    @Step("Validate request schemas")
    private void validateRequestSchemas(JsonNode endpoints) {
        assertTrue(endpoints.has("request_schemas"), "Should contain request_schemas");
        JsonNode requestSchemas = endpoints.get("request_schemas");

        // Validate credit card creation schema
        assertTrue(requestSchemas.has("create_tarjeta_credito"), "Should have create_tarjeta_credito schema");
        JsonNode creditSchema = requestSchemas.get("create_tarjeta_credito");
        assertTrue(creditSchema.has("required"), "Credit card schema should have required fields");
        assertTrue(creditSchema.has("properties"), "Credit card schema should have properties");

        // Validate debit card creation schema
        assertTrue(requestSchemas.has("create_tarjeta_debito"), "Should have create_tarjeta_debito schema");
        JsonNode debitSchema = requestSchemas.get("create_tarjeta_debito");
        assertTrue(debitSchema.has("required"), "Debit card schema should have required fields");
        assertTrue(debitSchema.has("properties"), "Debit card schema should have properties");

        // Validate required fields for credit card
        JsonNode creditRequired = creditSchema.get("required");
        assertTrue(creditRequired.isArray(), "Credit card required should be an array");
        boolean hasNumero = false, hasLimite = false;
        for (JsonNode field : creditRequired) {
            String fieldName = field.asText();
            if ("numero_tarjeta".equals(fieldName)) hasNumero = true;
            if ("limite_credito".equals(fieldName)) hasLimite = true;
        }
        assertTrue(hasNumero, "Credit card should require numero_tarjeta");
        assertTrue(hasLimite, "Credit card should require limite_credito");
    }

    @Step("Validate response schemas")
    private void validateResponseSchemas(JsonNode endpoints) {
        assertTrue(endpoints.has("response_schemas"), "Should contain response_schemas");
        JsonNode responseSchemas = endpoints.get("response_schemas");

        // Validate credit card response schema
        assertTrue(responseSchemas.has("tarjeta_credito_response"), "Should have tarjeta_credito_response schema");
        JsonNode creditResponse = responseSchemas.get("tarjeta_credito_response");
        assertTrue(creditResponse.has("id"), "Credit card response should have id");
        assertTrue(creditResponse.has("numero_tarjeta"), "Credit card response should have numero_tarjeta");
        assertTrue(creditResponse.has("tipo_tarjeta"), "Credit card response should have tipo_tarjeta");

        // Validate debit card response schema
        assertTrue(responseSchemas.has("tarjeta_debito_response"), "Should have tarjeta_debito_response schema");
        JsonNode debitResponse = responseSchemas.get("tarjeta_debito_response");
        assertTrue(debitResponse.has("id"), "Debit card response should have id");
        assertTrue(debitResponse.has("numero_tarjeta"), "Debit card response should have numero_tarjeta");
        assertTrue(debitResponse.has("cuenta_asociada"), "Debit card response should have cuenta_asociada");
    }

    @Step("Validate relationships with other endpoints")
    private void validateRelationships(JsonNode endpoints) {
        assertTrue(endpoints.has("relationships"), "Should contain relationships");
        JsonNode relationships = endpoints.get("relationships");

        // Check relationships with other finance app entities
        assertTrue(relationships.has("with_gastos"), "Should have relationship with gastos");
        assertTrue(relationships.has("with_compras"), "Should have relationship with compras");
        assertTrue(relationships.has("with_debitos_automaticos"), "Should have relationship with debitos_automaticos");
        assertTrue(relationships.has("with_transacciones"), "Should have relationship with transacciones");

        // Validate relationship structure
        JsonNode gastosRel = relationships.get("with_gastos");
        assertTrue(gastosRel.has("description"), "Gastos relationship should have description");
        assertTrue(gastosRel.has("foreign_key"), "Gastos relationship should have foreign_key");
    }

    @Step("Validate business rules")
    private void validateBusinessRules(JsonNode endpoints) {
        assertTrue(endpoints.has("business_rules"), "Should contain business_rules");
        JsonNode businessRules = endpoints.get("business_rules");

        // Check essential business rules
        assertTrue(businessRules.has("numero_tarjeta"), "Should have numero_tarjeta business rules");
        assertTrue(businessRules.has("estado_tarjeta"), "Should have estado_tarjeta business rules");
        assertTrue(businessRules.has("security"), "Should have security business rules");

        // Validate security rules
        JsonNode security = businessRules.get("security");
        assertTrue(security.has("codigo_seguridad"), "Should have codigo_seguridad security rule");
        assertTrue(security.has("numero_completo"), "Should have numero_completo security rule");
        assertEquals("Never return in responses", security.get("codigo_seguridad").asText(),
                "Security code should never be returned");
    }

    @Step("Validate validation rules")
    private void validateValidationRules(JsonNode endpoints) {
        assertTrue(endpoints.has("validation_rules"), "Should contain validation_rules");
        JsonNode validationRules = endpoints.get("validation_rules");

        // Check creation and update validation rules
        assertTrue(validationRules.has("create_validation"), "Should have create_validation rules");
        assertTrue(validationRules.has("update_validation"), "Should have update_validation rules");

        JsonNode createValidation = validationRules.get("create_validation");
        assertTrue(createValidation.isArray(), "Create validation should be an array");
        assertTrue(createValidation.size() > 0, "Should have at least one create validation rule");

        // Check for Luhn algorithm validation
        boolean hasLuhnValidation = false;
        for (JsonNode rule : createValidation) {
            if (rule.asText().contains("Luhn algorithm")) {
                hasLuhnValidation = true;
                break;
            }
        }
        assertTrue(hasLuhnValidation, "Should validate card number with Luhn algorithm");
    }

    @Step("Log complete tarjetas endpoint summary")
    private void logTarjetasEndpointSummary(JsonNode endpoints) {
        StringBuilder summary = new StringBuilder();
        summary.append("\n=== TARJETAS ENDPOINT INFORMATION SUMMARY ===\n\n");

        // Base Endpoints
        summary.append("📍 BASE ENDPOINTS:\n");
        JsonNode baseEndpoints = endpoints.get("base_endpoints");
        for (JsonNode endpoint : baseEndpoints) {
            String path = endpoint.get("path").asText();
            String description = endpoint.get("description").asText();
            JsonNode methods = endpoint.get("methods");

            summary.append(String.format("   %s [%s]\n", path, String.join(", ",
                    methods.toString().replaceAll("[\\[\\]\"]", "").split(","))));
            summary.append(String.format("   └─ %s\n\n", description));
        }

        // Card Types
        summary.append("💳 CARD TYPES:\n");
        JsonNode cardTypes = endpoints.get("card_types");

        // Credit Cards
        summary.append("   CREDITO:\n");
        JsonNode creditCard = cardTypes.get("credito");
        JsonNode creditProps = creditCard.get("properties");
        summary.append(String.format("   └─ Properties: %s\n",
                creditProps.toString().replaceAll("[\\[\\]\"]", "")));

        JsonNode creditRules = creditCard.get("business_rules");
        if (creditRules.has("limite_credito")) {
            JsonNode limiteRule = creditRules.get("limite_credito");
            summary.append(String.format("   └─ Límite: $%s - $%s\n",
                    limiteRule.get("min").asText(), limiteRule.get("max").asText()));
        }

        // Debit Cards
        summary.append("   DEBITO:\n");
        JsonNode debitCard = cardTypes.get("debito");
        JsonNode debitProps = debitCard.get("properties");
        summary.append(String.format("   └─ Properties: %s\n",
                debitProps.toString().replaceAll("[\\[\\]\"]", "")));

        JsonNode debitRules = debitCard.get("business_rules");
        if (debitRules.has("limite_diario")) {
            JsonNode limiteRule = debitRules.get("limite_diario");
            summary.append(String.format("   └─ Límite Diario: $%s - $%s\n\n",
                    limiteRule.get("min").asText(), limiteRule.get("max").asText()));
        }

        // Relationships
        summary.append("🔗 RELATIONSHIPS:\n");
        JsonNode relationships = endpoints.get("relationships");
        relationships.fieldNames().forEachRemaining(fieldName -> {
            JsonNode relationship = relationships.get(fieldName);
            summary.append(String.format("   %s: %s\n",
                    fieldName, relationship.get("description").asText()));
        });

        summary.append("\n=== END SUMMARY ===\n");

        // Log the summary
        logger.info(summary.toString());

        // Also add as Allure attachment
        io.qameta.allure.Allure.addAttachment("Tarjetas Endpoints Summary", "text/plain", summary.toString());
    }
}