package utils;

import com.github.javafaker.Faker;
import models.Gasto;
import models.GastoUnico;
import models.Ingreso;
import models.Compra;
import models.GastoRecurrente;

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
    public static String generateRandomEmail() {
        return faker.internet().emailAddress();
    }

    public static String generateRandomUserId() {
        return faker.internet().uuid();
    }

    public static String generateRandomPassword() {
        return faker.internet().password(8, 16, true, true, true);
    }

}
