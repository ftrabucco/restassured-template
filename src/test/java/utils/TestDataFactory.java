package utils;

import com.github.javafaker.Faker;
import models.Gasto;
import models.Ingreso;

import java.math.BigDecimal;
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
                .withDescripcion(faker.commerce().productName())
                .withMonto(BigDecimal.valueOf(faker.number().randomDouble(2, 10, 1000)))
                .withCategoria(getRandomGastoCategoria())
                .withUsuarioId(faker.internet().uuid())
                .withMetodoPago(getRandomMetodoPago())
                .withNotas(faker.lorem().sentence())
                .withFecha(LocalDateTime.now().minusDays(faker.number().numberBetween(0, 30)))
                .build();
    }

    public static Gasto createGastoWithSpecificAmount(BigDecimal amount) {
        return new Gasto.Builder()
                .withDescripcion("Gasto de prueba")
                .withMonto(amount)
                .withCategoria("Alimentación")
                .withUsuarioId("test-user-id")
                .withMetodoPago("Tarjeta de crédito")
                .build();
    }

    public static Gasto createGastoForCategory(String categoria) {
        return new Gasto.Builder()
                .withDescripcion(faker.commerce().productName())
                .withMonto(BigDecimal.valueOf(faker.number().randomDouble(2, 10, 500)))
                .withCategoria(categoria)
                .withUsuarioId("test-user-id")
                .withMetodoPago(getRandomMetodoPago())
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

    // Helper methods for random categories and payment methods
    private static String getRandomGastoCategoria() {
        String[] categorias = {
                "Alimentación", "Transporte", "Entretenimiento", "Salud",
                "Educación", "Hogar", "Ropa", "Tecnología", "Viajes", "Otros"
        };
        return categorias[faker.number().numberBetween(0, categorias.length)];
    }

    private static String getRandomMetodoPago() {
        String[] metodos = {
                "Efectivo", "Tarjeta de crédito", "Tarjeta de débito",
                "Transferencia bancaria", "PayPal", "Otros"
        };
        return metodos[faker.number().numberBetween(0, metodos.length)];
    }

    private static String getRandomIngresoFuente() {
        String[] fuentes = {
                "Salario", "Freelance", "Inversiones", "Alquiler",
                "Bonificación", "Venta", "Regalo", "Otros"
        };
        return fuentes[faker.number().numberBetween(0, fuentes.length)];
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
