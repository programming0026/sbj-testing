package com.juniverse.testing.service;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.juniverse.testing.model.CustomerTier;
import com.juniverse.testing.model.Item;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Pruebas de lógica pura (sin dobles de prueba) con las distintas formas de JUnit 5 para parametrizar:
 * {@code @CsvSource}, {@code @CsvFileSource}, {@code @MethodSource} y {@code @EnumSource}.
 */
@Tag("fast")
@DisplayName("PricingService")
class PricingServiceTest {

    private PricingService pricing;

    @BeforeEach
    void setUp() {
        pricing = new PricingService(); // objeto nuevo en cada prueba: las pruebas no dependen unas de otras
    }

    @Nested
    @DisplayName("subtotal")
    class Subtotal {

        @Test
        @DisplayName("suma cantidad por precio de cada línea")
        void sumsLines() {
            List<Item> items = List.of(new Item("A", 2, new BigDecimal("10.50")), new Item("B", 1, new BigDecimal("4.00")));

            assertEquals(new BigDecimal("25.00"), pricing.subtotal(items));
        }

        @Test
        @DisplayName("una lista vacía suma cero")
        void emptyListIsZero() {
            assertEquals(BigDecimal.ZERO, pricing.subtotal(List.of()));
        }
    }

    @Nested
    @DisplayName("total con descuento")
    class Total {

        @ParameterizedTest(name = "{0} con subtotal {1} -> total {2}")
        @CsvSource({
            "STANDARD, 500.00, 500.00",
            "SILVER,   500.00, 475.00",
            "GOLD,     500.00, 450.00",
            "STANDARD, 1000.00, 980.00",
            "GOLD,     1000.00, 880.00"
        })
        @DisplayName("@CsvSource: nivel, subtotal y total esperado")
        void totalByTier(CustomerTier tier, String subtotal, String expected) {
            List<Item> items = List.of(new Item("X", 1, new BigDecimal(subtotal)));

            assertEquals(new BigDecimal(expected), pricing.total(items, tier));
        }

        @ParameterizedTest(name = "{0} {1} -> {2}")
        @CsvFileSource(resources = "/discounts.csv", numLinesToSkip = 1)
        @DisplayName("@CsvFileSource: los casos vienen de un archivo en src/test/resources")
        void totalFromFile(CustomerTier tier, String subtotal, String expected) {
            List<Item> items = List.of(new Item("X", 1, new BigDecimal(subtotal)));

            assertEquals(new BigDecimal(expected), pricing.total(items, tier));
        }

        @ParameterizedTest(name = "{0}")
        @MethodSource("bulkCases")
        @DisplayName("@MethodSource: casos construidos en código (umbral del descuento adicional)")
        void bulkDiscount(String description, BigDecimal subtotal, BigDecimal expectedPercent) {
            assertEquals(0, expectedPercent.compareTo(pricing.discountPercent(CustomerTier.STANDARD, subtotal)), description);
        }

        static Stream<Arguments> bulkCases() {
            return Stream.of(
                    Arguments.of("justo debajo del umbral", new BigDecimal("999.99"), BigDecimal.ZERO),
                    Arguments.of("exactamente en el umbral", new BigDecimal("1000"), BigDecimal.valueOf(2)),
                    Arguments.of("por encima del umbral", new BigDecimal("5000"), BigDecimal.valueOf(2)));
        }

        @ParameterizedTest
        @EnumSource(CustomerTier.class)
        @DisplayName("@EnumSource: con cualquier nivel el total nunca supera al subtotal")
        void totalNeverExceedsSubtotal(CustomerTier tier) {
            List<Item> items = List.of(new Item("X", 3, new BigDecimal("400.00")));

            assertTrue(pricing.total(items, tier).compareTo(pricing.subtotal(items)) <= 0);
        }

        @ParameterizedTest
        @EnumSource(value = CustomerTier.class, names = {"SILVER", "GOLD"})
        @DisplayName("@EnumSource con names: solo los niveles con descuento")
        void discountedTiersPayLess(CustomerTier tier) {
            List<Item> items = List.of(new Item("X", 1, new BigDecimal("100.00")));

            assertTrue(pricing.total(items, tier).compareTo(new BigDecimal("100.00")) < 0);
        }
    }

    @Test
    @DisplayName("assertAll comprueba varias condiciones y las informa todas juntas")
    void assertAllReportsEveryFailure() {
        List<Item> items = List.of(new Item("A", 1, new BigDecimal("2000")));

        assertAll("pedido grande de un cliente GOLD",
                () -> assertEquals(new BigDecimal("2000"), pricing.subtotal(items)),
                () -> assertEquals(BigDecimal.valueOf(12), pricing.discountPercent(CustomerTier.GOLD, pricing.subtotal(items))),
                () -> assertEquals(new BigDecimal("1760.00"), pricing.total(items, CustomerTier.GOLD)));
    }
}
