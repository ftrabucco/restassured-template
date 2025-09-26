package base;

import io.restassured.response.Response;
import org.junit.jupiter.api.AfterEach;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Base class for API tests that automatically handles cleanup of created test data
 * Follows SOLID principles with Strategy Pattern for cleanup operations
 * Implements DRY principle by centralizing common cleanup logic
 */
public abstract class ApiTestWithCleanup extends BaseTest {

    // Entity type enum for better type safety (Single Responsibility)
    public enum EntityType {
        COMPRA("compra"),
        GASTO_UNICO("gasto único"),
        GASTO_RECURRENTE("gasto recurrente"),
        DEBITO_AUTOMATICO("débito automático");

        private final String displayName;

        EntityType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    // Single data structure to track all entities (DRY principle)
    private final Map<EntityType, List<String>> trackedEntities = new HashMap<>();

    public ApiTestWithCleanup() {
        // Initialize tracking lists for all entity types
        for (EntityType type : EntityType.values()) {
            trackedEntities.put(type, new ArrayList<>());
        }
    }

    /**
     * Generic method to track created entities (DRY principle)
     * @param entityType The type of entity being tracked
     * @param entityId The ID of the created entity
     */
    protected void trackCreatedEntity(EntityType entityType, String entityId) {
        if (isValidId(entityId)) {
            trackedEntities.get(entityType).add(entityId);
            System.out.println("📝 Tracking " + entityType.getDisplayName() + " for cleanup: " + entityId);
        }
    }

    /**
     * Convenience methods for specific entity types (Interface Segregation)
     */
    protected void trackCreatedCompra(String compraId) {
        trackCreatedEntity(EntityType.COMPRA, compraId);
    }

    protected void trackCreatedGastoUnico(String gastoUnicoId) {
        trackCreatedEntity(EntityType.GASTO_UNICO, gastoUnicoId);
    }

    protected void trackCreatedGastoRecurrente(String gastoRecurrenteId) {
        trackCreatedEntity(EntityType.GASTO_RECURRENTE, gastoRecurrenteId);
    }

    protected void trackCreatedDebitoAutomatico(String debitoAutomaticoId) {
        trackCreatedEntity(EntityType.DEBITO_AUTOMATICO, debitoAutomaticoId);
    }

    /**
     * Helper method to extract and track entity ID from response (DRY principle)
     * @param response The API response
     * @param entityType The type of entity
     * @param idPath The JSON path to the ID field
     */
    protected void trackEntityFromResponse(Response response, EntityType entityType, String idPath) {
        if (response.getStatusCode() == 201) {
            String entityId = response.jsonPath().getString(idPath);
            trackCreatedEntity(entityType, entityId);
        }
    }

    /**
     * Automatic cleanup after each test method (Template Method Pattern)
     */
    @AfterEach
    final void cleanupTestData() {
        int totalCleaned = 0;

        // Get cleanup strategies from subclass (Strategy Pattern)
        Map<EntityType, Function<List<String>, Integer>> cleanupStrategies = getCleanupStrategies();

        // Execute cleanup for each entity type that has tracked entities
        for (Map.Entry<EntityType, List<String>> entry : trackedEntities.entrySet()) {
            EntityType entityType = entry.getKey();
            List<String> entityIds = entry.getValue();

            if (!entityIds.isEmpty() && cleanupStrategies.containsKey(entityType)) {
                totalCleaned += cleanupStrategies.get(entityType).apply(entityIds);
            }
        }

        if (totalCleaned > 0) {
            System.out.println("🧹 Cleaned up " + totalCleaned + " test entities");
        }

        // Clear all tracking lists for next test
        clearAllTrackedEntities();
    }

    /**
     * Subclasses must provide cleanup strategies for entity types they create
     * (Open/Closed Principle - open for extension, closed for modification)
     * @return Map of entity types to their cleanup functions
     */
    protected abstract Map<EntityType, Function<List<String>, Integer>> getCleanupStrategies();

    /**
     * Generic cleanup method that can be reused by subclasses (DRY principle)
     * @param entityIds List of entity IDs to clean up
     * @param deleteFunction Function that deletes a single entity by ID
     * @param entityTypeName Name of the entity type for logging
     * @return Number of entities successfully cleaned up
     */
    protected int performCleanup(List<String> entityIds, Function<String, Response> deleteFunction, String entityTypeName) {
        int cleaned = 0;
        for (String entityId : entityIds) {
            try {
                Response deleteResponse = deleteFunction.apply(entityId);
                if (isSuccessfulDeletion(deleteResponse)) {
                    cleaned++;
                    System.out.println("🗑️ Cleaned " + entityTypeName + ": " + entityId);
                }
            } catch (Exception e) {
                System.out.println("⚠️ Failed to cleanup " + entityTypeName + " " + entityId + ": " + e.getMessage());
            }
        }
        return cleaned;
    }

    /**
     * Validate if an ID is valid for tracking
     */
    private boolean isValidId(String id) {
        return id != null && !id.trim().isEmpty() && !id.equals("null");
    }

    /**
     * Check if deletion response indicates success
     */
    private boolean isSuccessfulDeletion(Response response) {
        int statusCode = response.getStatusCode();
        return statusCode == 200 || statusCode == 204 || statusCode == 404; // 404 means already deleted
    }

    /**
     * Clear all tracked entities
     */
    private void clearAllTrackedEntities() {
        trackedEntities.values().forEach(List::clear);
    }

    /**
     * Get tracked entities for a specific type (for testing/debugging)
     */
    protected List<String> getTrackedEntities(EntityType entityType) {
        return new ArrayList<>(trackedEntities.get(entityType));
    }
}