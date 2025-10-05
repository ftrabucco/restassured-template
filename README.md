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
- **Domain-specific clients** (GastosApiClient, GastosUnicosApiClient, ComprasApiClient, GastosRecurrentesApiClient, DebitosAutomaticosApiClient, AuthApiClient)
- **JWT Authentication support** with token management and refresh
- **Fluent API design** for readable test code
- **Built-in request/response logging**
- **Automatic test data cleanup** with SOLID principles

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
mvn test -Dtest=ComprasE2ETest
```

### Run E2E test suites
```bash
# Run all E2E tests
mvn test -Dtest="*E2ETest"

# Run specific E2E test
mvn test -Dtest=GastosUnicosE2ETest

# Run Authentication E2E test
mvn test -Dtest=AuthE2ETestWorking
```

### Run Authentication tests
```bash
# Run simple authentication tests
mvn test -Dtest=AuthSimpleTest

# Run comprehensive auth API tests
mvn test -Dtest=AuthApiTest

# Run complete authentication E2E flow
mvn test -Dtest=AuthE2ETestWorking
```

### Generate Allure report
```bash
mvn allure:serve
```

## 📝 Writing Tests

### 1. Create a new test class
```java
public class ComprasApiTest extends ApiTestWithCleanup {
    private ComprasApiClient comprasClient;

    @Override
    protected void customSetup() {
        comprasClient = new ComprasApiClient().withRequestSpec(requestSpec);
    }

    @Test
    @DisplayName("Should create a new compra successfully")
    void shouldCreateCompraSuccessfully() {
        // Arrange
        Compra newCompra = TestDataFactory.createRandomCompra();

        // Act
        Response response = comprasClient.createCompra(newCompra);

        // Track for automatic cleanup
        trackEntityFromResponse(response, EntityType.COMPRA, "data.compra.id");

        // Assert
        ResponseValidator.validateStatusCode(response, 201);
        ResponseValidator.validateFieldExists(response, "data.compra.id");
    }

    // Implement cleanup strategies
    @Override
    protected Map<EntityType, Function<List<String>, Integer>> getCleanupStrategies() {
        Map<EntityType, Function<List<String>, Integer>> strategies = new HashMap<>();
        strategies.put(EntityType.COMPRA, compraIds ->
            performCleanup(compraIds, comprasClient::deleteCompra, "compra"));
        return strategies;
    }
}
```

### 2. Create test data
```java
// Using factory for random data
Compra randomCompra = TestDataFactory.createRandomCompra();
User randomUser = TestDataFactory.createRandomUser();

// Using builder for specific data
Compra specificCompra = TestDataFactory.createRandomCompra();
specificCompra.setDescripcion("Test Purchase");
specificCompra.setMontoTotal(BigDecimal.valueOf(299.99));
specificCompra.setCantidadCuotas(3);

// Create user with specific credentials
User testUser = TestDataFactory.createUserWithSpecificData(
    "Test User",
    "test@example.com",
    "Password123"
);
```

### 3. Use validation utilities
```java
// Status code validation
ResponseValidator.validateStatusCode(response, 201);

// Field validations
ResponseValidator.validateFieldExists(response, "data.compra.id");
ResponseValidator.validateFieldNotExists(response, "data.user.password"); // Security check
ResponseValidator.validateStringFieldValue(response, "data.user.email", "test@example.com");
ResponseValidator.validateNumericFieldValue(response, "data.compra.cantidad_cuotas", 12);
ResponseValidator.validateContentType(response, "application/json");

// Performance validation
ResponseValidator.validateResponseTime(response, 5000);
```

## 🔐 Authentication Testing

The framework provides comprehensive JWT-based authentication testing capabilities:

### Authentication Features
- **User Registration & Login** with password validation
- **JWT Token Management** with automatic extraction and handling
- **Profile Management** (get, update profile information)
- **Password Change** functionality with validation
- **Session Management** (logout, token invalidation)
- **Security Testing** (invalid tokens, unauthorized access)

### Authentication Test Examples

#### Simple Authentication Test
```java
@Test
void shouldRegisterAndLoginUser() {
    // Register new user
    String requestBody = """
        {
            "nombre": "Test User",
            "email": "test@example.com",
            "password": "Password123"
        }
        """;

    Response registerResponse = given()
        .header("Content-Type", "application/json")
        .body(requestBody)
        .when()
        .post("/api/auth/register")
        .then()
        .statusCode(201)
        .body("data.user.email", equalTo("test@example.com"))
        .extract().response();

    // Login with registered user
    String loginBody = """
        {
            "email": "test@example.com",
            "password": "Password123"
        }
        """;

    Response loginResponse = given()
        .header("Content-Type", "application/json")
        .body(loginBody)
        .when()
        .post("/api/auth/login")
        .then()
        .statusCode(200)
        .body("data.token", notNullValue())
        .extract().response();
}
```

#### Complete E2E Authentication Flow
```java
@Test
@Order(1)
void step01_shouldRegisterUserSuccessfully() {
    User testUser = TestDataFactory.createUserForRegistration();
    Response response = authClient.registerUser(testUser);

    ResponseValidator.validateStatusCode(response, 201);
    ResponseValidator.validateFieldExists(response, "data.user.id");
    ResponseValidator.validateFieldNotExists(response, "data.user.password");
}

@Test
@Order(2)
void step02_shouldLoginAndGetToken() {
    Response response = authClient.loginUser(testUser);
    jwtToken = authClient.extractJwtToken(response);

    ResponseValidator.validateStatusCode(response, 200);
    ResponseValidator.validateFieldExists(response, "data.token");
}

@Test
@Order(3)
void step03_shouldAccessProtectedResource() {
    Response response = authClient.getUserProfile(jwtToken);

    ResponseValidator.validateStatusCode(response, 200);
    ResponseValidator.validateStringFieldValue(response, "data.user.email", testUser.getEmail());
}
```

### Authentication API Endpoints

The framework tests the following authentication endpoints:

| Endpoint | Method | Description | Test Coverage |
|----------|--------|-------------|---------------|
| `/api/auth/register` | POST | User registration | ✅ Valid data, invalid email, weak password, duplicate email |
| `/api/auth/login` | POST | User authentication | ✅ Valid credentials, invalid credentials, non-existent user |
| `/api/auth/profile` | GET | Get user profile | ✅ Valid token, invalid token |
| `/api/auth/profile` | PUT | Update profile | ✅ Valid updates, invalid token |
| `/api/auth/change-password` | POST | Change password | ✅ Valid change, wrong current password, weak new password |
| `/api/auth/logout` | POST | Session logout | ✅ Valid logout, invalid token |

### Security Test Scenarios

The framework includes comprehensive security testing:

```java
@Test
void shouldRejectWeakPasswords() {
    User invalidUser = TestDataFactory.createUserWithSpecificData(
        "Test User",
        "test@example.com",
        "weak"  // Too short, no uppercase, no digits
    );

    Response response = authClient.registerUser(invalidUser);
    ResponseValidator.validateStatusCode(response, 400);
}

@Test
void shouldRejectInvalidTokens() {
    Response response = authClient.getUserProfile("invalid.jwt.token");
    ResponseValidator.validateStatusCode(response, 401);
}

@Test
void shouldPreventPasswordLeakage() {
    User user = TestDataFactory.createUserForRegistration();
    Response response = authClient.registerUser(user);

    // Ensure password is never returned in responses
    ResponseValidator.validateFieldNotExists(response, "data.user.password");
}
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
