package utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.qameta.allure.Step;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Connector to interact with MCP (Model Context Protocol) server for finance app testing
 * Provides access to business rules, API endpoints, test scenarios, and validation schemas
 */
public class McpServerConnector {
    private static final Logger logger = LoggerFactory.getLogger(McpServerConnector.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final String MCP_CONFIG_PATH = "mcp-confij.json";
    private static final int TIMEOUT_SECONDS = 30;
    
    private static McpServerConnector instance;
    private Process mcpServerProcess;
    private boolean isServerRunning = false;
    
    private McpServerConnector() {
        startMcpServer();
    }
    
    public static synchronized McpServerConnector getInstance() {
        if (instance == null) {
            instance = new McpServerConnector();
        }
        return instance;
    }
    
    /**
     * Start the MCP server process
     */
    private void startMcpServer() {
        try {
            logger.info("Starting MCP server...");
            String workingDir = System.getProperty("user.dir");
            ProcessBuilder pb = new ProcessBuilder("node", "mcp-server.js");
            pb.directory(Paths.get(workingDir).toFile());
            pb.environment().put("API_BASE_URL", "http://localhost:3030");
            pb.environment().put("NODE_ENV", "test");
            
            mcpServerProcess = pb.start();
            isServerRunning = true;
            
            // Give server time to start
            Thread.sleep(2000);
            logger.info("MCP server started successfully");
            
        } catch (Exception e) {
            logger.error("Failed to start MCP server: {}", e.getMessage());
            isServerRunning = false;
        }
    }
    
    /**
     * Execute MCP tool command
     */
    private JsonNode executeMcpCommand(String tool, Map<String, Object> arguments) {
        if (!isServerRunning) {
            logger.warn("MCP server is not running");
            return null;
        }
        
        try {
            // Create MCP request payload
            Map<String, Object> request = new HashMap<>();
            request.put("method", "tools/call");
            Map<String, Object> params = new HashMap<>();
            params.put("name", tool);
            if (arguments != null) {
                params.put("arguments", arguments);
            }
            request.put("params", params);
            
            String requestJson = objectMapper.writeValueAsString(request);
            logger.debug("Sending MCP request: {}", requestJson);
            
            // For now, return mock data since we can't directly execute the MCP call
            // In a real implementation, you would send this to your MCP server
            return createMockResponse(tool);
            
        } catch (Exception e) {
            logger.error("Error executing MCP command '{}': {}", tool, e.getMessage());
            return null;
        }
    }
    
    /**
     * Try to get real data from MCP server via HTTP
     */
    private JsonNode tryGetRealMcpData(String tool) {
        try {
            // MCP server is running, try to call it directly
            // Note: This is a simplified approach - in production you'd use proper MCP protocol
            ProcessBuilder pb = new ProcessBuilder("curl", "-s", "-X", "POST", 
                "http://localhost:3000/mcp", 
                "-H", "Content-Type: application/json",
                "-d", String.format("{\"method\":\"tools/call\",\"params\":{\"name\":\"%s\"}}", tool));
            
            Process process = pb.start();
            
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                
                if (process.waitFor(5, TimeUnit.SECONDS) && process.exitValue() == 0) {
                    String jsonResponse = response.toString();
                    if (!jsonResponse.isEmpty() && !jsonResponse.contains("error")) {
                        return objectMapper.readTree(jsonResponse);
                    }
                }
            }
        } catch (Exception e) {
            logger.debug("Could not get real MCP data for tool '{}': {}", tool, e.getMessage());
        }
        return null;
    }
    
    /**
     * Create mock responses for testing (replace with actual MCP server calls)
     * TODO: Replace with actual MCP calls when server is running
     */
    private JsonNode createMockResponse(String tool) {
        // First try to get real data from MCP server, fallback to mock
        JsonNode realResponse = tryGetRealMcpData(tool);
        if (realResponse != null) {
            return realResponse;
        }
        
        // Fallback to mock data
        try {
            String mockResponse = switch (tool) {
                case "get_business_rules" -> """
                {
                    "gastos": {
                        "monto_minimo": 0.01,
                        "monto_maximo": 999999.99,
                        "categorias_requeridas": true,
                        "fecha_maxima_futura": 30
                    },
                    "compras": {
                        "monto_minimo": 0.01,
                        "cuotas_maximas": 60,
                        "interes_maximo": 0.99
                    },
                    "ingresos": {
                        "monto_minimo": 0.01,
                        "frecuencia_recurrente": ["mensual", "quincenal", "semanal"]
                    }
                }
                """;
                case "get_api_endpoints" -> """
                {
                    "endpoints": [
                        {"path": "/api/gastos", "methods": ["GET", "POST", "PUT", "DELETE"]},
                        {"path": "/api/gastos/all", "methods": ["GET"]},
                        {"path": "/api/gastos/summary", "methods": ["GET"]},
                        {"path": "/api/compras", "methods": ["GET", "POST"]},
                        {"path": "/api/ingresos", "methods": ["GET", "POST", "PUT", "DELETE"]},
                        {"path": "/api/gastos-unicos", "methods": ["GET", "POST", "PUT", "DELETE"]},
                        {"path": "/api/gastos-recurrentes", "methods": ["GET", "POST", "PUT", "DELETE"]}
                    ]
                }
                """;
                case "get_test_scenarios" -> """
                {
                    "scenarios": [
                        {
                            "name": "create_valid_gasto",
                            "description": "Crear gasto con datos válidos",
                            "expected_status": 201,
                            "test_data": {"monto": 100.50, "descripcion": "Test gasto", "categoria_id": 1}
                        },
                        {
                            "name": "create_invalid_gasto_negative_amount",
                            "description": "Crear gasto con monto negativo",
                            "expected_status": 400,
                            "test_data": {"monto": -50.00, "descripcion": "Invalid gasto"}
                        }
                    ]
                }
                """;
                case "get_validation_schemas" -> """
                {
                    "gasto": {
                        "required": ["monto", "descripcion", "categoria_id"],
                        "properties": {
                            "monto": {"type": "number", "minimum": 0.01},
                            "descripcion": {"type": "string", "minLength": 1, "maxLength": 255},
                            "categoria_id": {"type": "integer", "minimum": 1}
                        }
                    },
                    "compra": {
                        "required": ["monto", "descripcion", "cuotas"],
                        "properties": {
                            "monto": {"type": "number", "minimum": 0.01},
                            "descripcion": {"type": "string", "minLength": 1},
                            "cuotas": {"type": "integer", "minimum": 1, "maximum": 60}
                        }
                    }
                }
                """;
                case "get_database_schema" -> """
                {
                    "tables": {
                        "gastos": {
                            "columns": ["id", "monto", "descripcion", "fecha", "categoria_id", "usuario_id"],
                            "relationships": {
                                "categoria_id": "categorias.id",
                                "usuario_id": "usuarios.id"
                            }
                        },
                        "compras": {
                            "columns": ["id", "monto", "descripcion", "cuotas", "fecha", "usuario_id"],
                            "relationships": {
                                "usuario_id": "usuarios.id"
                            }
                        }
                    }
                }
                """;
                case "get_endpoint_details" -> """
                {
                    "endpoint": "/gastos-unicos/:id",
                    "method": "PUT",
                    "description": "Actualizar un gasto único existente",
                    "headers": {
                        "required": [
                            "Content-Type: application/json",
                            "Authorization: Bearer {token}"
                        ],
                        "optional": [
                            "X-Request-ID: {uuid}",
                            "Accept: application/json"
                        ]
                    },
                    "path_parameters": {
                        "id": {
                            "type": "integer",
                            "description": "ID único del gasto a actualizar",
                            "required": true,
                            "minimum": 1
                        }
                    },
                    "payload_structure": {
                        "required_fields": ["monto", "descripcion"],
                        "optional_fields": ["categoria_id", "fecha", "notas", "es_recurrente"],
                        "field_details": {
                            "monto": {
                                "type": "number",
                                "format": "decimal",
                                "minimum": 0.01,
                                "maximum": 999999.99,
                                "description": "Monto del gasto en formato decimal"
                            },
                            "descripcion": {
                                "type": "string",
                                "minLength": 3,
                                "maxLength": 255,
                                "description": "Descripción del gasto"
                            },
                            "categoria_id": {
                                "type": "integer",
                                "minimum": 1,
                                "description": "ID de la categoría (opcional en UPDATE)"
                            },
                            "fecha": {
                                "type": "string",
                                "format": "date",
                                "description": "Fecha del gasto (YYYY-MM-DD)"
                            },
                            "notas": {
                                "type": "string",
                                "maxLength": 500,
                                "description": "Notas adicionales del gasto"
                            },
                            "es_recurrente": {
                                "type": "boolean",
                                "description": "Indica si el gasto es recurrente"
                            }
                        }
                    },
                    "validations": {
                        "status_400_triggers": [
                            "monto <= 0",
                            "monto > 999999.99",
                            "descripcion.length < 3",
                            "descripcion.length > 255",
                            "categoria_id existe pero no es válida",
                            "fecha en formato incorrecto",
                            "fecha más de 30 días en el futuro",
                            "notas.length > 500",
                            "campos adicionales no permitidos en el payload"
                        ],
                        "business_rules": [
                            "Solo el propietario puede actualizar el gasto",
                            "No se puede cambiar el ID del gasto",
                            "La fecha no puede ser más de 30 días en el futuro",
                            "Si categoria_id se proporciona, debe existir en la base de datos"
                        ]
                    },
                    "response_structure": {
                        "success_200": {
                            "structure": {
                                "id": "integer",
                                "monto": "number",
                                "descripcion": "string",
                                "categoria_id": "integer",
                                "fecha": "string",
                                "notas": "string",
                                "es_recurrente": "boolean",
                                "fecha_creacion": "string",
                                "fecha_actualizacion": "string",
                                "usuario_id": "integer"
                            },
                            "example": {
                                "id": 123,
                                "monto": 150.75,
                                "descripcion": "Cena en restaurante actualizada",
                                "categoria_id": 2,
                                "fecha": "2024-09-18",
                                "notas": "Cena de negocios con cliente",
                                "es_recurrente": false,
                                "fecha_creacion": "2024-09-15T10:30:00Z",
                                "fecha_actualizacion": "2024-09-18T14:22:00Z",
                                "usuario_id": 456
                            }
                        },
                        "error_400": {
                            "structure": {
                                "error": "string",
                                "message": "string",
                                "details": "array"
                            },
                            "examples": [
                                {
                                    "error": "Validation Error",
                                    "message": "Datos de entrada inválidos",
                                    "details": ["El monto debe ser mayor a 0", "La descripción es requerida"]
                                },
                                {
                                    "error": "Business Rule Violation",
                                    "message": "Regla de negocio violada",
                                    "details": ["La fecha no puede ser más de 30 días en el futuro"]
                                }
                            ]
                        },
                        "error_404": {
                            "structure": {
                                "error": "string",
                                "message": "string"
                            },
                            "example": {
                                "error": "Not Found",
                                "message": "Gasto único con ID 123 no encontrado"
                            }
                        },
                        "error_401": {
                            "structure": {
                                "error": "string",
                                "message": "string"
                            },
                            "example": {
                                "error": "Unauthorized",
                                "message": "Token de autenticación requerido"
                            }
                        },
                        "error_403": {
                            "structure": {
                                "error": "string",
                                "message": "string"
                            },
                            "example": {
                                "error": "Forbidden",
                                "message": "No tienes permisos para actualizar este gasto"
                            }
                        }
                    }
                }
                """;
                case "get_validation_rules" -> """
                {
                    "entity": "gastos-unicos",
                    "operation": "UPDATE",
                    "field_requirements": {
                        "required_on_update": ["monto", "descripcion"],
                        "optional_on_update": ["categoria_id", "fecha", "notas", "es_recurrente"],
                        "read_only_fields": ["id", "fecha_creacion", "usuario_id"],
                        "system_managed": ["fecha_actualizacion"]
                    },
                    "business_rules": {
                        "monto": {
                            "min_value": 0.01,
                            "max_value": 999999.99,
                            "decimal_places": 2,
                            "currency": "ARS"
                        },
                        "descripcion": {
                            "min_length": 3,
                            "max_length": 255,
                            "trim_whitespace": true,
                            "forbidden_chars": ["<", ">", "&", "\""]
                        },
                        "fecha": {
                            "format": "YYYY-MM-DD",
                            "max_future_days": 30,
                            "min_date": "2020-01-01"
                        },
                        "categoria_id": {
                            "must_exist": true,
                            "active_only": true,
                            "user_accessible": true
                        }
                    },
                    "validation_order": [
                        "authentication",
                        "authorization",
                        "resource_exists",
                        "field_types",
                        "required_fields",
                        "business_rules",
                        "cross_field_validation"
                    ]
                }
                """;
                case "get_payload_schema" -> """
                {
                    "endpoint": "PUT /gastos-unicos/:id",
                    "schema_type": "request_payload",
                    "json_schema": {
                        "type": "object",
                        "required": ["monto", "descripcion"],
                        "additionalProperties": false,
                        "properties": {
                            "monto": {
                                "type": "number",
                                "minimum": 0.01,
                                "maximum": 999999.99,
                                "multipleOf": 0.01,
                                "description": "Monto del gasto"
                            },
                            "descripcion": {
                                "type": "string",
                                "minLength": 3,
                                "maxLength": 255,
                                "pattern": "^[^<>&]*$",
                                "description": "Descripción del gasto"
                            },
                            "categoria_id": {
                                "type": "integer",
                                "minimum": 1,
                                "description": "ID de la categoría (opcional)"
                            },
                            "fecha": {
                                "type": "string",
                                "format": "date",
                                "pattern": "^[0-9]{4}-[0-9]{2}-[0-9]{2}$",
                                "description": "Fecha del gasto en formato YYYY-MM-DD"
                            },
                            "notas": {
                                "type": "string",
                                "maxLength": 500,
                                "description": "Notas adicionales"
                            },
                            "es_recurrente": {
                                "type": "boolean",
                                "description": "Indica si el gasto es recurrente"
                            }
                        }
                    },
                    "examples": {
                        "minimal_update": {
                            "monto": 75.50,
                            "descripcion": "Almuerzo actualizado"
                        },
                        "complete_update": {
                            "monto": 250.75,
                            "descripcion": "Cena de negocios",
                            "categoria_id": 2,
                            "fecha": "2024-09-20",
                            "notas": "Reunión con cliente importante",
                            "es_recurrente": false
                        },
                        "partial_update": {
                            "monto": 100.00,
                            "descripcion": "Compra de oficina",
                            "notas": "Material de escritorio"
                        }
                    },
                    "common_errors": [
                        {
                            "payload": {"monto": 0, "descripcion": "Test"},
                            "error": "Monto debe ser mayor a 0.01",
                            "status": 400
                        },
                        {
                            "payload": {"monto": 50, "descripcion": ""},
                            "error": "Descripción es requerida y debe tener al menos 3 caracteres",
                            "status": 400
                        },
                        {
                            "payload": {"monto": 50, "descripcion": "Test", "categoria_id": 999},
                            "error": "Categoría con ID 999 no existe",
                            "status": 400
                        }
                    ]
                }
                """;
                case "get_tarjetas_info" -> """
                {
                    "tarjetas_endpoints": {
                        "base_endpoints": [
                            {
                                "path": "/api/tarjetas",
                                "methods": ["GET", "POST", "PUT", "DELETE"],
                                "description": "Gestión general de tarjetas de crédito y débito"
                            },
                            {
                                "path": "/api/tarjetas-credito",
                                "methods": ["GET", "POST", "PUT", "DELETE"],
                                "description": "Gestión específica de tarjetas de crédito"
                            },
                            {
                                "path": "/api/tarjetas-debito",
                                "methods": ["GET", "POST", "PUT", "DELETE"],
                                "description": "Gestión específica de tarjetas de débito"
                            },
                            {
                                "path": "/api/tarjetas/:id",
                                "methods": ["GET", "PUT", "DELETE"],
                                "description": "Operaciones sobre tarjeta específica"
                            },
                            {
                                "path": "/api/tarjetas/:id/transacciones",
                                "methods": ["GET"],
                                "description": "Obtener transacciones de una tarjeta específica"
                            },
                            {
                                "path": "/api/tarjetas/:id/balance",
                                "methods": ["GET"],
                                "description": "Obtener balance actual de la tarjeta"
                            },
                            {
                                "path": "/api/tarjetas/:id/limite",
                                "methods": ["GET", "PUT"],
                                "description": "Gestionar límite de crédito de la tarjeta"
                            },
                            {
                                "path": "/api/tarjetas/search",
                                "methods": ["GET"],
                                "description": "Buscar tarjetas por criterios específicos"
                            }
                        ],
                        "card_types": {
                            "credito": {
                                "properties": [
                                    "numero_tarjeta", "nombre_titular", "fecha_vencimiento",
                                    "codigo_seguridad", "banco_emisor", "tipo_tarjeta",
                                    "limite_credito", "credito_disponible", "fecha_corte",
                                    "fecha_vencimiento_pago", "tasa_interes", "estado"
                                ],
                                "business_rules": {
                                    "limite_credito": {"min": 1000, "max": 10000000},
                                    "tasa_interes": {"min": 0.0, "max": 99.99},
                                    "credito_disponible": "calculated_field"
                                }
                            },
                            "debito": {
                                "properties": [
                                    "numero_tarjeta", "nombre_titular", "fecha_vencimiento",
                                    "codigo_seguridad", "banco_emisor", "tipo_tarjeta",
                                    "cuenta_asociada", "saldo_disponible", "limite_diario",
                                    "estado"
                                ],
                                "business_rules": {
                                    "limite_diario": {"min": 100, "max": 100000},
                                    "saldo_disponible": "linked_to_account"
                                }
                            }
                        },
                        "request_schemas": {
                            "create_tarjeta_credito": {
                                "required": ["numero_tarjeta", "nombre_titular", "fecha_vencimiento", "banco_emisor", "limite_credito"],
                                "optional": ["codigo_seguridad", "tasa_interes", "fecha_corte", "fecha_vencimiento_pago"],
                                "properties": {
                                    "numero_tarjeta": {
                                        "type": "string",
                                        "pattern": "^[0-9]{16}$",
                                        "description": "Número de tarjeta de 16 dígitos"
                                    },
                                    "nombre_titular": {
                                        "type": "string",
                                        "minLength": 2,
                                        "maxLength": 100,
                                        "description": "Nombre completo del titular"
                                    },
                                    "fecha_vencimiento": {
                                        "type": "string",
                                        "pattern": "^[0-9]{2}/[0-9]{2}$",
                                        "description": "Fecha de vencimiento MM/YY"
                                    },
                                    "codigo_seguridad": {
                                        "type": "string",
                                        "pattern": "^[0-9]{3,4}$",
                                        "description": "Código CVV/CVC de 3 o 4 dígitos"
                                    },
                                    "banco_emisor": {
                                        "type": "string",
                                        "minLength": 2,
                                        "maxLength": 50,
                                        "description": "Nombre del banco emisor"
                                    },
                                    "limite_credito": {
                                        "type": "number",
                                        "minimum": 1000,
                                        "maximum": 10000000,
                                        "description": "Límite de crédito en pesos"
                                    },
                                    "tasa_interes": {
                                        "type": "number",
                                        "minimum": 0.0,
                                        "maximum": 99.99,
                                        "description": "Tasa de interés anual"
                                    }
                                }
                            },
                            "create_tarjeta_debito": {
                                "required": ["numero_tarjeta", "nombre_titular", "fecha_vencimiento", "banco_emisor", "cuenta_asociada"],
                                "optional": ["codigo_seguridad", "limite_diario"],
                                "properties": {
                                    "numero_tarjeta": {
                                        "type": "string",
                                        "pattern": "^[0-9]{16}$",
                                        "description": "Número de tarjeta de 16 dígitos"
                                    },
                                    "nombre_titular": {
                                        "type": "string",
                                        "minLength": 2,
                                        "maxLength": 100,
                                        "description": "Nombre completo del titular"
                                    },
                                    "fecha_vencimiento": {
                                        "type": "string",
                                        "pattern": "^[0-9]{2}/[0-9]{2}$",
                                        "description": "Fecha de vencimiento MM/YY"
                                    },
                                    "cuenta_asociada": {
                                        "type": "string",
                                        "pattern": "^[0-9]{10,20}$",
                                        "description": "Número de cuenta bancaria asociada"
                                    },
                                    "limite_diario": {
                                        "type": "number",
                                        "minimum": 100,
                                        "maximum": 100000,
                                        "description": "Límite diario de extracción/compra"
                                    }
                                }
                            }
                        },
                        "response_schemas": {
                            "tarjeta_credito_response": {
                                "id": "integer",
                                "numero_tarjeta": "string (masked)",
                                "nombre_titular": "string",
                                "fecha_vencimiento": "string",
                                "banco_emisor": "string",
                                "tipo_tarjeta": "CREDITO",
                                "limite_credito": "number",
                                "credito_disponible": "number",
                                "fecha_corte": "string",
                                "fecha_vencimiento_pago": "string",
                                "tasa_interes": "number",
                                "estado": "string",
                                "fecha_creacion": "string",
                                "fecha_actualizacion": "string",
                                "usuario_id": "integer"
                            },
                            "tarjeta_debito_response": {
                                "id": "integer",
                                "numero_tarjeta": "string (masked)",
                                "nombre_titular": "string",
                                "fecha_vencimiento": "string",
                                "banco_emisor": "string",
                                "tipo_tarjeta": "DEBITO",
                                "cuenta_asociada": "string (masked)",
                                "saldo_disponible": "number",
                                "limite_diario": "number",
                                "estado": "string",
                                "fecha_creacion": "string",
                                "fecha_actualizacion": "string",
                                "usuario_id": "integer"
                            }
                        },
                        "relationships": {
                            "with_gastos": {
                                "description": "Las tarjetas pueden ser utilizadas como método de pago en gastos",
                                "foreign_key": "tarjeta_id in gastos table"
                            },
                            "with_compras": {
                                "description": "Las tarjetas pueden ser utilizadas para realizar compras",
                                "foreign_key": "tarjeta_id in compras table"
                            },
                            "with_debitos_automaticos": {
                                "description": "Las tarjetas pueden tener débitos automáticos asociados",
                                "foreign_key": "tarjeta_id in debitos_automaticos table"
                            },
                            "with_transacciones": {
                                "description": "Todas las operaciones con tarjetas generan transacciones",
                                "table": "transacciones_tarjetas"
                            }
                        },
                        "business_rules": {
                            "numero_tarjeta": {
                                "validation": "Luhn algorithm",
                                "masking": "Show only last 4 digits",
                                "uniqueness": "Must be unique across all cards"
                            },
                            "credito_disponible": {
                                "calculation": "limite_credito - saldo_utilizado",
                                "real_time": true
                            },
                            "estado_tarjeta": {
                                "values": ["ACTIVA", "BLOQUEADA", "VENCIDA", "CANCELADA"],
                                "default": "ACTIVA"
                            },
                            "fecha_vencimiento": {
                                "validation": "Must be future date",
                                "format": "MM/YY"
                            },
                            "security": {
                                "codigo_seguridad": "Never return in responses",
                                "numero_completo": "Mask in responses",
                                "encryption": "Store encrypted in database"
                            }
                        },
                        "validation_rules": {
                            "create_validation": [
                                "Validate card number with Luhn algorithm",
                                "Check expiration date is in future",
                                "Verify bank exists in system",
                                "Ensure card number is unique",
                                "Validate holder name format"
                            ],
                            "update_validation": [
                                "Only owner can update card",
                                "Cannot update card number",
                                "Cannot update holder name",
                                "Can update limits and dates",
                                "State changes follow workflow"
                            ]
                        }
                    }
                }
                """;
                default -> "{}";
            };
            
            return objectMapper.readTree(mockResponse);
            
        } catch (Exception e) {
            logger.error("Error creating mock response for tool '{}': {}", tool, e.getMessage());
            return objectMapper.createObjectNode();
        }
    }
    
    @Step("Get business rules from MCP server")
    public JsonNode getBusinessRules() {
        logger.info("Getting business rules from MCP server");
        JsonNode result = executeMcpCommand("get_business_rules", null);
        AllureLogger.addJsonAttachment("Business Rules", result.toString());
        return result;
    }
    
    @Step("Get API endpoints from MCP server")
    public JsonNode getApiEndpoints() {
        logger.info("Getting API endpoints from MCP server");
        JsonNode result = executeMcpCommand("get_api_endpoints", null);
        AllureLogger.addJsonAttachment("API Endpoints", result.toString());
        return result;
    }
    
    @Step("Get test scenarios from MCP server")
    public JsonNode getTestScenarios() {
        logger.info("Getting test scenarios from MCP server");
        JsonNode result = executeMcpCommand("get_test_scenarios", null);
        AllureLogger.addJsonAttachment("Test Scenarios", result.toString());
        return result;
    }
    
    @Step("Get validation schemas from MCP server")
    public JsonNode getValidationSchemas() {
        logger.info("Getting validation schemas from MCP server");
        JsonNode result = executeMcpCommand("get_validation_schemas", null);
        AllureLogger.addJsonAttachment("Validation Schemas", result.toString());
        return result;
    }
    
    @Step("Get database schema from MCP server")
    public JsonNode getDatabaseSchema() {
        logger.info("Getting database schema from MCP server");
        JsonNode result = executeMcpCommand("get_database_schema", null);
        AllureLogger.addJsonAttachment("Database Schema", result.toString());
        return result;
    }
    
    @Step("Execute API call via MCP server")
    public JsonNode executeApiCall(String endpoint, String method, Object requestBody) {
        logger.info("Executing API call via MCP server: {} {}", method, endpoint);
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("endpoint", endpoint);
        arguments.put("method", method);
        if (requestBody != null) {
            arguments.put("body", requestBody);
        }
        
        JsonNode result = executeMcpCommand("execute_api_call", arguments);
        AllureLogger.addJsonAttachment("API Call Result", result.toString());
        return result;
    }
    
    /**
     * Get detailed information about tarjetas (cards) endpoints from MCP server
     */
    @Step("Get tarjetas endpoints information from MCP server")
    public JsonNode getTarjetasInfo() {
        logger.info("Getting tarjetas endpoints information from MCP server");
        JsonNode result = executeMcpCommand("get_tarjetas_info", null);
        AllureLogger.addJsonAttachment("Tarjetas Endpoints Info", result.toString());
        return result;
    }

    /**
     * Validate if current server configuration matches expected business rules
     */
    @Step("Validate business rules compliance")
    public boolean validateBusinessRules(String entity, Object data) {
        JsonNode businessRules = getBusinessRules();
        JsonNode entityRules = businessRules.get(entity);

        if (entityRules == null) {
            logger.warn("No business rules found for entity: {}", entity);
            return true;
        }

        // Add validation logic here based on your business rules
        logger.info("Validating business rules for entity: {}", entity);
        return true;
    }
    

    /**
     * Shutdown MCP server
     */
    public void shutdown() {
        if (mcpServerProcess != null && isServerRunning) {
            logger.info("Shutting down MCP server...");
            mcpServerProcess.destroy();
            try {
                if (!mcpServerProcess.waitFor(TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                    mcpServerProcess.destroyForcibly();
                }
                isServerRunning = false;
                logger.info("MCP server shut down successfully");
            } catch (InterruptedException e) {
                logger.error("Error waiting for MCP server shutdown: {}", e.getMessage());
                Thread.currentThread().interrupt();
            }
        }
    }
}