package utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class to gather information about Débitos Automáticos from MCP server
 */
public class McpDebitosAutomaticosInfoGatherer {
    private static final Logger logger = LoggerFactory.getLogger(McpDebitosAutomaticosInfoGatherer.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    public static void main(String[] args) {
        logger.info("Starting Débitos Automáticos information gathering from MCP server...");
        
        try {
            McpServerConnector mcpConnector = McpServerConnector.getInstance();
            
            // Gather information about débitos automáticos
            System.out.println("=== DÉBITOS AUTOMÁTICOS INFORMATION GATHERING ===");
            System.out.println();
            
            // 1. Get API endpoints
            System.out.println("1. API ENDPOINTS:");
            JsonNode endpoints = mcpConnector.getApiEndpoints();
            System.out.println(endpoints.toPrettyString());
            System.out.println();
            
            // 2. Get business rules
            System.out.println("2. BUSINESS RULES:");
            JsonNode businessRules = mcpConnector.getBusinessRules();
            System.out.println(businessRules.toPrettyString());
            System.out.println();
            
            // 3. Get database schema
            System.out.println("3. DATABASE SCHEMA:");
            JsonNode databaseSchema = mcpConnector.getDatabaseSchema();
            System.out.println(databaseSchema.toPrettyString());
            System.out.println();
            
            // 4. Get test scenarios
            System.out.println("4. TEST SCENARIOS:");
            JsonNode testScenarios = mcpConnector.getTestScenarios();
            System.out.println(testScenarios.toPrettyString());
            System.out.println();
            
            // 5. Get validation schemas
            System.out.println("5. VALIDATION SCHEMAS:");
            JsonNode validationSchemas = mcpConnector.getValidationSchemas();
            System.out.println(validationSchemas.toPrettyString());
            System.out.println();
            
            logger.info("Information gathering completed successfully");
            
        } catch (Exception e) {
            logger.error("Error gathering information from MCP server: {}", e.getMessage());
            e.printStackTrace();
        }
    }
}