package config;

import org.yaml.snakeyaml.Yaml;
import java.io.InputStream;
import java.util.Map;

/**
 * Configuration manager using Singleton pattern to handle environment-specific configurations
 */
public class ConfigManager {
    private static ConfigManager instance;
    private Map<String, Object> config;
    private String currentEnvironment;

    private ConfigManager() {
        loadConfiguration();
        this.currentEnvironment = System.getProperty("env", "local");
    }

    public static ConfigManager getInstance() {
        if (instance == null) {
            synchronized (ConfigManager.class) {
                if (instance == null) {
                    instance = new ConfigManager();
                }
            }
        }
        return instance;
    }

    @SuppressWarnings("unchecked")
    private void loadConfiguration() {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("config.yml")) {
            Yaml yaml = new Yaml();
            config = yaml.load(inputStream);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load configuration file", e);
        }
    }

    public String getBaseUrl() {
        return getEnvironmentProperty("base_url");
    }

    public int getTimeout() {
        return Integer.parseInt(getEnvironmentProperty("timeout"));
    }

    public String getEndpoint(String endpointName) {
        Map<String, Object> api = (Map<String, Object>) config.get("api");
        Map<String, String> endpoints = (Map<String, String>) api.get("endpoints");
        return endpoints.get(endpointName);
    }

    public String getFullEndpointUrl(String endpointName) {
        return getBaseUrl() + getEndpoint(endpointName);
    }

    @SuppressWarnings("unchecked")
    private String getEnvironmentProperty(String property) {
        Map<String, Object> environments = (Map<String, Object>) config.get("environments");
        Map<String, Object> currentEnv = (Map<String, Object>) environments.get(currentEnvironment);
        return currentEnv.get(property).toString();
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> getTestData() {
        return (Map<String, Object>) config.get("test_data");
    }

    public String getCurrentEnvironment() {
        return currentEnvironment;
    }

    public void setEnvironment(String environment) {
        this.currentEnvironment = environment;
    }
}
