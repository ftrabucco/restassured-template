package utils;

import com.github.javafaker.Faker;
import models.Gasto;
import models.GastoUnico;
import models.Ingreso;
import models.Compra;
import models.GastoRecurrente;
import models.DebitoAutomatico;
import models.Tarjeta;
import models.User;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Locale;

/**
 * Factory class for generating test data using Faker library
 * Implements Factory pattern for test data creation
 */
public class TestDataFactory {
    private static final Faker faker = new Faker(new Locale("es"));

    // Gasto (Expense) test data generators
    public static Gasto createRandomGasto() {
        return new Gasto.Builder()
                .descripcion(faker.commerce().productName())
                .monto(BigDecimal.valueOf(faker.number().randomDouble(2, 10, 1000)))
                .categoriaId(getRandomCategoriaId())
                .tipoPagoId(getRandomTipoPagoId())
                .tarjetaId(getRandomTarjetaId())
                .notas(faker.lorem().sentence())
                .fecha(LocalDate.now().minusDays(faker.number().numberBetween(0, 30)))
                .build();
    }

    public static Gasto createGastoWithSpecificAmount(BigDecimal amount) {
        return new Gasto.Builder()
                .descripcion("Gasto de prueba")
                .monto(amount)
                .categoriaId(1L)
                .tipoPagoId(1L)
                .tarjetaId(1L)
                .notas("Gasto de prueba")
                .fecha(LocalDate.now())
                .build();
    }

    public static Gasto createGastoForCategory(Long categoriaId) {
        return new Gasto.Builder()
                .descripcion(faker.commerce().productName())
                .monto(BigDecimal.valueOf(faker.number().randomDouble(2, 10, 500)))
                .categoriaId(categoriaId)
                .tipoPagoId(getRandomTipoPagoId())
                .tarjetaId(getRandomTarjetaId())
                .notas(faker.lorem().sentence())
                .fecha(LocalDate.now())
                .build();
    }

    // Ingreso (Income) test data generators
    public static Ingreso createRandomIngreso() {
        return new Ingreso.Builder()
                .withDescripcion(faker.job().title())
                .withMonto(BigDecimal.valueOf(faker.number().randomDouble(2, 1000, 5000)))
                .withFuente(getRandomIngresoFuente())
                .withUsuarioId(faker.internet().uuid())
                .withRecurrente(faker.bool().bool())
                .withNotas(faker.lorem().sentence())
                .withFecha(LocalDateTime.now().minusDays(faker.number().numberBetween(0, 30)))
                .build();
    }

    public static Ingreso createIngresoWithSpecificAmount(BigDecimal amount) {
        return new Ingreso.Builder()
                .withDescripcion("Salario mensual")
                .withMonto(amount)
                .withFuente("Salario")
                .withUsuarioId("test-user-id")
                .withRecurrente(true)
                .build();
    }

    // Helper methods for random IDs
    private static Long getRandomCategoriaId() {
        return (long) faker.number().numberBetween(1, 10);
    }

    private static Long getRandomTipoPagoId() {
        return (long) faker.number().numberBetween(1, 5); // 4 tipos de pago (1-4)
    }

    private static Long getRandomTarjetaId() {
        return (long) faker.number().numberBetween(1, 3); // 2 tarjetas (1-2)
    }

    private static Long getRandomCategoriaGastoId() {
        return (long) faker.number().numberBetween(1, 18); // 17 categorías (1-17)
    }

    private static Long getRandomImportanciaGastoId() {
        return (long) faker.number().numberBetween(1, 5); // 4 importancias (1-4)
    }

    // GastoUnico (One-time Expense) test data generators
    public static GastoUnico createRandomGastoUnico() {
        return new GastoUnico.Builder()
                .descripcion(faker.commerce().productName())
                .monto(BigDecimal.valueOf(faker.number().randomDouble(2, 50, 2000)))
                .fecha(LocalDate.now().minusDays(faker.number().numberBetween(0, 30)))
                .categoriaGastoId(getRandomCategoriaGastoId())
                .importanciaGastoId(getRandomImportanciaGastoId())
                .tipoPagoId(getRandomTipoPagoId())
                .build();
    }

    public static GastoUnico createGastoUnicoWithSpecificAmount(BigDecimal amount) {
        return new GastoUnico.Builder()
                .descripcion("Reparación auto")
                .monto(amount)
                .fecha(LocalDate.now())
                .categoriaGastoId(5L)
                .importanciaGastoId(1L)
                .tipoPagoId(1L)
                .build();
    }

    public static GastoUnico createGastoUnicoForCategory(Long categoriaGastoId) {
        return new GastoUnico.Builder()
                .descripcion(faker.commerce().productName())
                .monto(BigDecimal.valueOf(faker.number().randomDouble(2, 100, 1500)))
                .fecha(LocalDate.now().minusDays(faker.number().numberBetween(0, 15)))
                .categoriaGastoId(categoriaGastoId)
                .importanciaGastoId(getRandomImportanciaGastoId())
                .tipoPagoId(getRandomTipoPagoId())
                .build();
    }


    private static String getRandomIngresoFuente() {
        String[] fuentes = {
                "Salario", "Freelance", "Inversiones", "Alquiler",
                "Bonificación", "Venta", "Regalo", "Otros"
        };
        return fuentes[faker.number().numberBetween(0, fuentes.length - 1)];
    }

    // Compra (Purchase) test data generators
    public static Compra createRandomCompra() {
        return new Compra.Builder()
                .descripcion(faker.commerce().productName())
                .montoTotal(BigDecimal.valueOf(faker.number().randomDouble(2, 100, 5000)))
                .cantidadCuotas(faker.number().numberBetween(1, 24))
                .fechaCompra(LocalDate.now().minusDays(faker.number().numberBetween(0, 90)))
                .categoriaGastoId(getRandomCategoriaGastoId())
                .importanciaGastoId(getRandomImportanciaGastoId())
                .tipoPagoId(getRandomTipoPagoId())
                .tarjetaId(getRandomTarjetaId())
                .build();
    }

    public static Compra createCompraWithSpecificAmount(BigDecimal amount) {
        return new Compra.Builder()
                .descripcion("Laptop")
                .montoTotal(amount)
                .cantidadCuotas(12)
                .fechaCompra(LocalDate.now())
                .categoriaGastoId(5L)
                .importanciaGastoId(2L)
                .tipoPagoId(3L)
                .tarjetaId(2L)
                .build();
    }

    public static Compra createCompraForCategory(Long categoriaGastoId) {
        return new Compra.Builder()
                .descripcion(faker.commerce().productName())
                .montoTotal(BigDecimal.valueOf(faker.number().randomDouble(2, 200, 3000)))
                .cantidadCuotas(faker.number().numberBetween(3, 18))
                .fechaCompra(LocalDate.now().minusDays(faker.number().numberBetween(0, 60)))
                .categoriaGastoId(categoriaGastoId)
                .importanciaGastoId(getRandomImportanciaGastoId())
                .tipoPagoId(getRandomTipoPagoId())
                .tarjetaId(getRandomTarjetaId())
                .build();
    }

    // GastoRecurrente (Recurring Expense) test data generators
    public static GastoRecurrente createRandomGastoRecurrente() {
        return new GastoRecurrente.Builder()
                .descripcion(faker.commerce().productName())
                .monto(BigDecimal.valueOf(faker.number().randomDouble(2, 20, 500)))
                .diaDePago(faker.number().numberBetween(1, 28))
                .fechaInicio(LocalDate.now().minusDays(faker.number().numberBetween(0, 365)))
                .frecuenciaGastoId(getRandomFrecuenciaGastoId())
                .categoriaGastoId(getRandomCategoriaGastoId())
                .importanciaGastoId(getRandomImportanciaGastoId())
                .tipoPagoId(getRandomTipoPagoId())
                .tarjetaId(getRandomTarjetaId())
                .activo(true)
                .build();
    }

    public static GastoRecurrente createGastoRecurrenteWithSpecificAmount(BigDecimal amount) {
        return new GastoRecurrente.Builder()
                .descripcion("test 2")
                .monto(amount)
                .diaDePago(15)
                .fechaInicio(LocalDate.of(2024, 1, 1))
                .frecuenciaGastoId(2L)
                .categoriaGastoId(3L)
                .importanciaGastoId(2L)
                .tipoPagoId(3L)
                .tarjetaId(2L)
                .activo(true)
                .build();
    }

    public static GastoRecurrente createGastoRecurrenteForCategory(Long categoriaGastoId) {
        return new GastoRecurrente.Builder()
                .descripcion(faker.commerce().productName())
                .monto(BigDecimal.valueOf(faker.number().randomDouble(2, 30, 300)))
                .diaDePago(faker.number().numberBetween(1, 28))
                .fechaInicio(LocalDate.now().minusDays(faker.number().numberBetween(0, 180)))
                .frecuenciaGastoId(getRandomFrecuenciaGastoId())
                .categoriaGastoId(categoriaGastoId)
                .importanciaGastoId(getRandomImportanciaGastoId())
                .tipoPagoId(getRandomTipoPagoId())
                .tarjetaId(getRandomTarjetaId())
                .activo(true)
                .build();
    }

    private static Long getRandomFrecuenciaGastoId() {
        return (long) faker.number().numberBetween(1, 3); // 3 frecuencias (1-3)
    }

    // User data generators
    public static User createRandomUser() {
        return new User.Builder()
                .nombre(faker.name().fullName())
                .email(faker.internet().emailAddress())
                .password(generateValidPassword())
                .build();
    }

    public static User createUserWithSpecificData(String nombre, String email, String password) {
        return new User.Builder()
                .nombre(nombre)
                .email(email)
                .password(password)
                .build();
    }

    public static User createTestUser() {
        return new User.Builder()
                .nombre("Test User")
                .email("test@example.com")
                .password("Password123")
                .build();
    }

    public static User createUserForRegistration() {
        return new User.Builder()
                .nombre(faker.name().fullName())
                .email(faker.internet().emailAddress())
                .password(generateValidPassword())
                .build();
    }

    /**
     * Generate a password that meets API requirements:
     * - Minimum 6 characters
     * - At least 1 uppercase letter
     * - At least 1 lowercase letter
     * - At least 1 digit
     */
    public static String generateValidPassword() {
        return "Test" + faker.number().numberBetween(100, 999) + "!";
    }

    public static String generateRandomEmail() {
        return faker.internet().emailAddress();
    }

    public static String generateRandomUserId() {
        return faker.internet().uuid();
    }

    public static String generateRandomPassword() {
        return faker.internet().password(8, 16, true, true, true);
    }

    // DebitoAutomatico (Automatic Debit) test data generators
    public static DebitoAutomatico createRandomDebitoAutomatico() {
        return new DebitoAutomatico.Builder()
                .descripcion(faker.commerce().productName())
                .monto(BigDecimal.valueOf(faker.number().randomDouble(2, 50, 1000)))
                .diaDePago(faker.number().numberBetween(1, 28))
                .categoriaGastoId(getRandomCategoriaGastoId())
                .importanciaGastoId(getRandomImportanciaGastoId())
                .frecuenciaGastoId(getRandomFrecuenciaGastoId())
                .tipoPagoId(getRandomTipoPagoId())
                .tarjetaId(getRandomTarjetaId())
                .activo(true)
                .build();
    }

    public static DebitoAutomatico createDebitoAutomaticoWithSpecificAmount(BigDecimal amount) {
        return new DebitoAutomatico.Builder()
                .descripcion("Pago automático mensual")
                .monto(amount)
                .diaDePago(15)
                .categoriaGastoId(1L)
                .importanciaGastoId(1L)
                .frecuenciaGastoId(2L)
                .tipoPagoId(1L)
                .tarjetaId(1L)
                .activo(true)
                .build();
    }

    public static DebitoAutomatico createDebitoAutomaticoForCategory(Long categoriaGastoId) {
        return new DebitoAutomatico.Builder()
                .descripcion(faker.commerce().productName())
                .monto(BigDecimal.valueOf(faker.number().randomDouble(2, 100, 800)))
                .diaDePago(faker.number().numberBetween(1, 28))
                .categoriaGastoId(categoriaGastoId)
                .importanciaGastoId(getRandomImportanciaGastoId())
                .frecuenciaGastoId(getRandomFrecuenciaGastoId())
                .tipoPagoId(getRandomTipoPagoId())
                .tarjetaId(getRandomTarjetaId())
                .activo(true)
                .build();
    }

    public static DebitoAutomatico createInactiveDebitoAutomatico() {
        return new DebitoAutomatico.Builder()
                .descripcion("Débito automático inactivo")
                .monto(BigDecimal.valueOf(faker.number().randomDouble(2, 30, 200)))
                .diaDePago(faker.number().numberBetween(1, 28))
                .categoriaGastoId(getRandomCategoriaGastoId())
                .importanciaGastoId(getRandomImportanciaGastoId())
                .frecuenciaGastoId(getRandomFrecuenciaGastoId())
                .tipoPagoId(getRandomTipoPagoId())
                .activo(false)
                .build();
    }

    // ====== TARJETA (CARD) TEST DATA GENERATORS - MCP FORMAT ======

    public static Tarjeta createValidCreditCard() {
        return Tarjeta.builder()
                .nombre("Tarjeta Crédito " + faker.number().digits(4))
                .tipo("credito")
                .banco(getRandomBanco())
                .diaMesCierre(faker.number().numberBetween(1, 28))
                .diaMesVencimiento(faker.number().numberBetween(1, 28))
                .permiteCuotas(true)
                .usuarioId(1)
                .build();
    }

    public static Tarjeta createValidDebitCard() {
        return Tarjeta.builder()
                .nombre("Tarjeta Débito " + faker.number().digits(4))
                .tipo("debito")
                .banco(getRandomBanco())
                .diaMesCierre(null)
                .diaMesVencimiento(null)
                .permiteCuotas(false)
                .usuarioId(1)
                .build();
    }

    public static Tarjeta createRandomCreditCard() {
        return createValidCreditCard();
    }

    public static Tarjeta createRandomDebitCard() {
        return createValidDebitCard();
    }

    public static Tarjeta createCreditCardForBanco(String banco) {
        return Tarjeta.builder()
                .nombre("Tarjeta " + banco)
                .tipo("credito")
                .banco(banco)
                .diaMesCierre(15)
                .diaMesVencimiento(25)
                .permiteCuotas(true)
                .usuarioId(1)
                .build();
    }

    public static Tarjeta createDebitCardForBanco(String banco) {
        return Tarjeta.builder()
                .nombre("Débito " + banco)
                .tipo("debito")
                .banco(banco)
                .diaMesCierre(null)
                .diaMesVencimiento(null)
                .permiteCuotas(false)
                .usuarioId(1)
                .build();
    }

    public static Tarjeta createTarjetaWithMissingRequiredFields() {
        return Tarjeta.builder()
                .banco("Banco Test")
                .tipo("credito")
                .usuarioId(1)
                .build();
    }

    public static Tarjeta createTarjetaWithInvalidTipo() {
        return Tarjeta.builder()
                .nombre("Tarjeta Inválida")
                .tipo("invalido")
                .banco("Banco Test")
                .usuarioId(1)
                .build();
    }

    private static String getRandomBanco() {
        String[] bancos = {
                "Banco Nación", "Banco Provincia", "Banco Ciudad",
                "BBVA", "Santander", "Macro", "Galicia", "ICBC",
                "Banco Piano", "Banco Supervielle", "Brubank"
        };
        return bancos[faker.number().numberBetween(0, bancos.length)];
    }

    // ====== TARJETA UTILITY METHODS ======

    /**
     * Generate a valid card number using Luhn algorithm
     */
    public static String generateValidCardNumber() {
        // Generate 15 digits and calculate Luhn checksum for the 16th
        String prefix = "4532"; // Visa prefix for testing
        StringBuilder cardNumber = new StringBuilder(prefix);

        // Add 11 more random digits
        for (int i = 0; i < 11; i++) {
            cardNumber.append(faker.number().numberBetween(0, 9));
        }

        // Calculate Luhn checksum
        int checksum = calculateLuhnChecksum(cardNumber.toString());
        cardNumber.append(checksum);

        return cardNumber.toString();
    }

    /**
     * Generate invalid card number (fails Luhn algorithm)
     */
    public static String generateInvalidCardNumber() {
        return "1234567890123456"; // Known invalid number
    }

    /**
     * Generate future expiry date in MM/YY format
     */
    public static String generateFutureExpiryDate() {
        int month = faker.number().numberBetween(1, 12);
        int year = LocalDate.now().getYear() + faker.number().numberBetween(1, 5);
        return String.format("%02d/%02d", month, year % 100);
    }

    /**
     * Generate past expiry date in MM/YY format
     */
    public static String generatePastExpiryDate() {
        int month = faker.number().numberBetween(1, 12);
        int year = LocalDate.now().getYear() - faker.number().numberBetween(1, 3);
        return String.format("%02d/%02d", month, year % 100);
    }

    /**
     * Generate CVV code
     */
    public static String generateCvv() {
        return String.format("%03d", faker.number().numberBetween(100, 999));
    }

    /**
     * Generate bank account number
     */
    public static String generateAccountNumber() {
        return String.format("%020d", faker.number().randomNumber(20, true));
    }

    /**
     * Calculate Luhn checksum for card number validation
     */
    private static int calculateLuhnChecksum(String cardNumber) {
        int sum = 0;
        boolean alternate = true;

        for (int i = cardNumber.length() - 1; i >= 0; i--) {
            int digit = Character.getNumericValue(cardNumber.charAt(i));

            if (alternate) {
                digit *= 2;
                if (digit > 9) {
                    digit = (digit % 10) + 1;
                }
            }

            sum += digit;
            alternate = !alternate;
        }

        return (10 - (sum % 10)) % 10;
    }

}
