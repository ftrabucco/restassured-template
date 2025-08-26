# REST API Testing Framework

A comprehensive and reusable REST API testing framework built with Java, RestAssured, and JUnit 5. Originally designed for personal finance microservices but architected to be generic and adaptable for any REST API testing needs.

## 🏗️ Architecture Overview

This framework follows a layered architecture with clear separation of concerns:

```
src/test/java/
├── base/           # Base test classes and common setup
├── clients/        # API client classes (one per service/domain)
├── config/         # Configuration management
├── models/         # Data models and DTOs
├── tests/          # Actual test classes
└── utils/          # Utilities and helpers
```

## 🎯 Design Patterns Implemented

### 1. **Singleton Pattern** - Configuration Management
- **Where**: `ConfigManager` class
- **Why**: Ensures single instance of configuration throughout test execution, preventing multiple file reads and maintaining consistency across all tests.

```java
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
```

### 2. **Builder Pattern** - Test Data Creation
- **Where**: Model classes (`Gasto.Builder`, `Ingreso.Builder`)
- **Why**: Provides flexible and readable way to create test objects with optional parameters, making test data setup more maintainable.

```java
Gasto gasto = new Gasto.Builder()
    .withDescripcion("Test expense")
    .withMonto(BigDecimal.valueOf(100.50))
    .withCategoria("Food")
    .build();
```

### 3. **Factory Pattern** - Test Data Generation
- **Where**: `TestDataFactory` class
- **Why**: Centralizes test data creation logic, provides consistent fake data generation, and makes it easy to create different variations of test data.

```java
public static Gasto createRandomGasto() {
    return new Gasto.Builder()
        .withDescripcion(faker.commerce().productName())
        .withMonto(BigDecimal.valueOf(faker.number().randomDouble(2, 10, 1000)))
        // ...
        .build();
}
```

### 4. **Template Method Pattern** - Base Test Structure
- **Where**: `BaseTest` class
- **Why**: Defines the skeleton of test setup while allowing subclasses to override specific steps. Ensures consistent test initialization across all test classes.

```java
@BeforeEach
void setUp(TestInfo testInfo) {
    // Common setup for all tests
    setupRequestSpec();
    setupResponseSpec();
    
    // Hook for custom setup in subclasses
    customSetup();
}
```

### 5. **Strategy Pattern** - Response Validation
- **Where**: `ResponseValidator` class
- **Why**: Encapsulates different validation algorithms, making it easy to add new validation strategies without modifying existing code.

### 6. **Inheritance** - API Client Hierarchy
- **Where**: `ApiClient` base class with specific implementations
- **Why**: Promotes code reuse for common HTTP operations while allowing specialized behavior for different API domains.

## 🚀 Key Features

### Environment Management
- **Multi-environment support** (local, dev, staging, prod)
- **YAML-based configuration** for easy maintenance
- **Runtime environment switching** via system properties

### Comprehensive Reporting
- **Allure integration** for beautiful HTML reports
- **Step-by-step execution tracking** with `@Step` annotations
- **Automatic screenshot capture** on failures (configurable)
- **Detailed logging** with SLF4J

### Test Data Management
- **Faker integration** for realistic test data generation
- **Builder pattern** for flexible object creation
- **Factory methods** for common test scenarios
- **Locale support** (Spanish faker for realistic financial data)

### API Client Architecture
- **Domain-specific clients** (GastosApiClient, IngresosApiClient)
- **Fluent API design** for readable test code
- **Built-in request/response logging**
- **Automatic authentication handling**

## 📋 Prerequisites

- Java 21+
- Maven 3.6+
- Your API service running (for integration tests)

## 🛠️ Setup

1. **Clone the repository**
```bash
git clone <your-repo-url>
cd RestAssuredTemplate
```

2. **Install dependencies**
```bash
mvn clean install
```

3. **Configure your environment**
Edit `src/test/resources/config.yml` with your API endpoints:
```yaml
environments:
  local:
    base_url: "http://localhost:3030"
    timeout: 30000
```

## 🏃‍♂️ Running Tests

### Run all tests
```bash
mvn test
```

### Run tests for specific environment
```bash
mvn test -Denv=dev
```

### Run specific test class
```bash
mvn test -Dtest=GastosApiTest
```

### Generate Allure report
```bash
mvn allure:serve
```

## 📝 Writing Tests

### 1. Create a new test class
```java
public class GastosApiTest extends BaseTest {
    private GastosApiClient gastosClient;
    
    @Override
    protected void customSetup() {
        gastosClient = new GastosApiClient().withRequestSpec(requestSpec);
    }
    
    @Test
    @DisplayName("Should create a new gasto successfully")
    void shouldCreateGastoSuccessfully() {
        // Arrange
        Gasto newGasto = TestDataFactory.createRandomGasto();
        
        // Act
        Response response = gastosClient.createGasto(newGasto);
        
        // Assert
        ResponseValidator.validateStatusCode(response, 201);
        ResponseValidator.validateFieldExists(response, "id");
    }
}
```

### 2. Create test data
```java
// Using factory for random data
Gasto randomGasto = TestDataFactory.createRandomGasto();

// Using builder for specific data
Gasto specificGasto = new Gasto.Builder()
    .withDescripcion("Lunch expense")
    .withMonto(BigDecimal.valueOf(25.50))
    .withCategoria("Food")
    .build();
```

### 3. Use validation utilities
```java
// Status code validation
ResponseValidator.validateStatusCode(response, 200);

// Field validations
ResponseValidator.validateFieldExists(response, "data.id");
ResponseValidator.validateFieldValue(response, "data.categoria", "Food");
ResponseValidator.validateArrayNotEmpty(response, "data");

// Performance validation
ResponseValidator.validateResponseTime(response, 2000);
```

## 🏗️ Extending the Framework

### Adding a new API client
1. Create a new client class extending `ApiClient`
2. Define domain-specific methods with `@Step` annotations
3. Add endpoint configuration to `config.yml`

### Adding new validation strategies
1. Add methods to `ResponseValidator` class
2. Use `@Step` annotation for reporting
3. Follow the existing naming convention

### Adding new test data factories
1. Create factory methods in `TestDataFactory`
2. Use Faker for realistic data generation
3. Provide both random and specific data creation methods

## 📊 Reporting

The framework generates comprehensive reports using Allure:

- **Test execution timeline**
- **Step-by-step breakdown** of each test
- **Request/Response details** for API calls
- **Environment information**
- **Test data used**
- **Failure screenshots** and logs

Access reports by running: `mvn allure:serve`

## 🔧 Configuration Options

### Environment Variables
- `env`: Target environment (local, dev, staging, prod)
- `allure.results.directory`: Custom path for Allure results

### System Properties
- `-Denv=staging`: Run tests against staging environment
- `-Dtest.timeout=60000`: Override default timeout

## 🤝 Contributing

1. Follow the established patterns and conventions
2. Add appropriate `@Step` annotations for new methods
3. Include comprehensive JavaDoc documentation
4. Write tests for new utilities and validators
5. Update this README for significant changes

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

---

**Built with ❤️ for robust API testing**
