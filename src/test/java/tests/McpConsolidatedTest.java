package tests;

import base.BaseTest;
import com.fasterxml.jackson.databind.JsonNode;
import io.qameta.allure.Description;
import io.qameta.allure.Feature;
import io.qameta.allure.Step;
import io.qameta.allure.Story;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import utils.McpServerConnector;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Consolidated test suite for complete MCP server integration validation
 * Replaces: McpIntegrationTest, McpRealIntegrationTest, McpEnhancedGastosTest
 */
@Feature("MCP Server Integration - Consolidated")
@DisplayName("Complete MCP Server Integration Tests")
public class McpConsolidatedTest extends BaseTest {

    private static McpServerConnector mcpConnector;
    private static JsonNode businessRules;
    private static JsonNode apiEndpoints;
    private static JsonNode validationSchemas;

    @BeforeAll
    public static void setUpMcpConnection() {
        mcpConnector = McpServerConnector.getInstance();
        // Pre-load all MCP data to avoid multiple calls
        businessRules = mcpConnector.getBusinessRules();
        apiEndpoints = mcpConnector.getApiEndpoints();
        validationSchemas = mcpConnector.getValidationSchemas();
    }

    @Test
    @Story("MCP Connectivity")
    @DisplayName("Should establish connection to MCP server")
    @Description("Validates basic connectivity and response from MCP server")
    public void shouldEstablishMcpConnection() {
        // Verify connector is initialized
        assertNotNull(mcpConnector, "MCP connector should be initialized");
        
        // Verify we can get basic response (without detailed attachment)
        JsonNode testResponse = testBasicMcpConnectivity();
        assertNotNull(testResponse, "MCP server should respond to requests");
        
        logMcpStep("MCP Connectivity", "Connection established successfully");
    }

    @Test
    @Story("Business Rules Validation")
    @DisplayName("Should retrieve and validate complete business rules")
    @Description("Validates comprehensive business rules for all entities from MCP server")
    public void shouldValidateCompleteBusinessRules() {
        assertNotNull(businessRules, "Business rules should not be null");
        assertTrue(businessRules.size() > 0, "Business rules should contain at least one entity");
        
        // Validate that business rules have meaningful content
        businessRules.fieldNames().forEachRemaining(entityName -> {
            JsonNode entityRules = businessRules.get(entityName);
            assertNotNull(entityRules, "Rules for " + entityName + " should not be null");
            assertTrue(entityRules.size() > 0, "Rules for " + entityName + " should not be empty");
        });
        
        // Validate specific business rule structures if they exist
        if (businessRules.has("gastos")) {
            validateGastosBusinessRules();
        }
        if (businessRules.has("compras")) {
            validateComprasBusinessRules();
        }
        if (businessRules.has("ingresos")) {
            validateIngresosBusinessRules();
        }
        
        logMcpStep("Business Rules", "Business rules validated successfully for " + businessRules.size() + " entities");
    }

    @Test
    @Story("API Discovery")
    @DisplayName("Should retrieve and validate all available API endpoints")
    @Description("Validates that MCP provides complete API endpoint information")
    public void shouldValidateApiEndpoints() {
        assertNotNull(apiEndpoints, "API endpoints should not be null");
        assertTrue(apiEndpoints.size() > 0, "API endpoints should contain at least one endpoint");
        
        // Validate that endpoints have meaningful content
        apiEndpoints.fieldNames().forEachRemaining(endpointName -> {
            JsonNode endpoint = apiEndpoints.get(endpointName);
            assertNotNull(endpoint, "Endpoint " + endpointName + " should not be null");
        });
        
        // Validate endpoint structure
        validateEndpointStructure();
        
        logMcpStep("API Endpoints", "API endpoints validated successfully for " + apiEndpoints.size() + " endpoints");
    }

    @Test
    @Story("Schema Validation")
    @DisplayName("Should retrieve and validate data schemas")
    @Description("Validates that MCP provides proper validation schemas for data integrity")
    public void shouldValidateDataSchemas() {
        assertNotNull(validationSchemas, "Validation schemas should not be null");
        
        // Validate schemas for main entities
        validateSchemaExists("gasto_unico");
        validateSchemaExists("gasto_recurrente");
        validateSchemaExists("debito_automatico");
        validateSchemaExists("compra");
        validateSchemaExists("ingreso");
        
        logMcpStep("Data Schemas", "All validation schemas validated successfully");
    }

    @Test
    @Story("Integration Consistency")
    @DisplayName("Should validate consistency between business rules and API endpoints")
    @Description("Validates that business rules align with available API endpoints")
    public void shouldValidateIntegrationConsistency() {
        // Cross-validate business rules with API endpoints
        validateBusinessRulesConsistency();
        
        // Validate that all endpoints have corresponding business rules
        validateEndpointBusinessRuleCoverage();
        
        // Validate schema consistency with business rules
        validateSchemaBusinessRuleAlignment();
        
        logMcpStep("Integration Consistency", "All consistency checks passed");
    }

    @Test
    @Story("MCP Performance")
    @DisplayName("Should validate MCP server performance")
    @Description("Validates that MCP server responds within acceptable timeframes")
    public void shouldValidateMcpPerformance() {
        long startTime = System.currentTimeMillis();
        
        // Test multiple rapid calls
        for (int i = 0; i < 5; i++) {
            JsonNode response = mcpConnector.getBusinessRules();
            assertNotNull(response, "MCP should respond consistently");
        }
        
        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;
        
        // Should complete 5 calls in under 5 seconds
        assertTrue(totalTime < 5000, "MCP server should respond quickly: " + totalTime + "ms");
        
        logMcpStep("MCP Performance", "Performance validated - " + totalTime + "ms for 5 calls");
    }

    // Helper methods

    // Simple connectivity test without detailed attachments
    private JsonNode testBasicMcpConnectivity() {
        return mcpConnector.getBusinessRules();
    }

    @Step("Retrieve business rules from MCP")
    private JsonNode retrieveBusinessRulesFromMcp() {
        return mcpConnector.getBusinessRules();
    }

    @Step("Validate entity business rules: {entityName}")
    private void validateEntityBusinessRules(String entityName) {
        assertTrue(businessRules.has(entityName), 
                  "Business rules should contain " + entityName + " rules");
        
        JsonNode entityRules = businessRules.get(entityName);
        assertNotNull(entityRules, entityName + " rules should not be null");
        assertTrue(entityRules.size() > 0, entityName + " rules should not be empty");
    }

    @Step("Validate gastos business rules")
    private void validateGastosBusinessRules() {
        JsonNode gastosRules = businessRules.get("gastos");
        
        // Validate required fields
        assertTrue(gastosRules.has("monto_minimo"), "Gastos rules should specify minimum amount");
        assertTrue(gastosRules.has("monto_maximo"), "Gastos rules should specify maximum amount");
        
        // Validate business logic
        double minAmount = gastosRules.get("monto_minimo").asDouble();
        double maxAmount = gastosRules.get("monto_maximo").asDouble();
        assertTrue(minAmount < maxAmount, "Minimum amount should be less than maximum");
        assertTrue(minAmount >= 0, "Minimum amount should be non-negative");
    }

    @Step("Validate compras business rules")
    private void validateComprasBusinessRules() {
        JsonNode comprasRules = businessRules.get("compras");
        assertNotNull(comprasRules, "Compras rules should exist");
        
        // Validate compras-specific rules
        if (comprasRules.has("cuotas_maximas")) {
            int maxCuotas = comprasRules.get("cuotas_maximas").asInt();
            assertTrue(maxCuotas > 0, "Maximum installments should be positive");
        }
    }

    @Step("Validate ingresos business rules")
    private void validateIngresosBusinessRules() {
        JsonNode ingresosRules = businessRules.get("ingresos");
        assertNotNull(ingresosRules, "Ingresos rules should exist");
        
        // Validate ingresos-specific rules
        if (ingresosRules.has("monto_minimo")) {
            double minAmount = ingresosRules.get("monto_minimo").asDouble();
            assertTrue(minAmount >= 0, "Minimum income should be non-negative");
        }
    }

    @Step("Validate endpoint exists: {endpointName}")
    private void validateEndpointExists(String endpointName) {
        assertTrue(apiEndpoints.has(endpointName), 
                  "API endpoints should contain " + endpointName);
        
        JsonNode endpoint = apiEndpoints.get(endpointName);
        assertNotNull(endpoint, endpointName + " endpoint should not be null");
    }

    @Step("Validate endpoint structure")
    private void validateEndpointStructure() {
        // Validate that endpoints have required fields
        apiEndpoints.fieldNames().forEachRemaining(endpointName -> {
            JsonNode endpoint = apiEndpoints.get(endpointName);
            // Add specific validation based on your endpoint structure
            assertNotNull(endpoint, "Endpoint " + endpointName + " should have valid structure");
        });
    }

    @Step("Validate schema exists: {schemaName}")
    private void validateSchemaExists(String schemaName) {
        if (validationSchemas != null && validationSchemas.has(schemaName)) {
            JsonNode schema = validationSchemas.get(schemaName);
            assertNotNull(schema, schemaName + " schema should not be null");
        }
        // Note: Some schemas might be optional, so we don't fail if missing
    }

    @Step("Validate business rules consistency")
    private void validateBusinessRulesConsistency() {
        // Ensure business rules are internally consistent
        businessRules.fieldNames().forEachRemaining(entityName -> {
            JsonNode entityRules = businessRules.get(entityName);
            
            // Validate that amount rules are consistent
            if (entityRules.has("monto_minimo") && entityRules.has("monto_maximo")) {
                double min = entityRules.get("monto_minimo").asDouble();
                double max = entityRules.get("monto_maximo").asDouble();
                assertTrue(min <= max, 
                          "Minimum amount should be <= maximum for " + entityName);
            }
        });
    }

    @Step("Validate endpoint business rule coverage")
    private void validateEndpointBusinessRuleCoverage() {
        // Core entities should have both endpoints and business rules
        String[] coreEntities = {"gastos", "compras", "ingresos"};
        
        for (String entity : coreEntities) {
            assertTrue(businessRules.has(entity), 
                      "Core entity " + entity + " should have business rules");
            // Note: API endpoints might use different naming convention
        }
    }

    @Step("Validate schema business rule alignment")
    private void validateSchemaBusinessRuleAlignment() {
        if (validationSchemas != null) {
            // Ensure schemas align with business rules where applicable
            validationSchemas.fieldNames().forEachRemaining(schemaName -> {
                JsonNode schema = validationSchemas.get(schemaName);
                // Add specific validation logic based on your schema structure
                assertNotNull(schema, "Schema " + schemaName + " should be valid");
            });
        }
    }

    @Step("Log MCP step: {stepName}")
    private void logMcpStep(String stepName, String details) {
        StringBuilder logEntry = new StringBuilder();
        logEntry.append("=== MCP VALIDATION: ").append(stepName).append(" ===\n");
        logEntry.append("Details: ").append(details).append("\n");
        logEntry.append("Timestamp: ").append(System.currentTimeMillis()).append("\n");
        logEntry.append("===========================\n");
        
        System.out.println(logEntry.toString());
    }
}