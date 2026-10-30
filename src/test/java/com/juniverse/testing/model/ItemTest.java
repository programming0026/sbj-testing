package com.juniverse.testing.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

/** Pruebas de un {@code record} con validación: excepciones esperadas con {@code assertThrows}. */
@DisplayName("Item")
class ItemTest {

    @Test
    void subtotalMultipliesQuantityByPrice() {
        assertEquals(new BigDecimal("30.00"), new Item("A", 3, new BigDecimal("10.00")).subtotal());
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1, -100})
    @DisplayName("la cantidad debe ser positiva")
    void rejectsNonPositiveQuantity(int quantity) {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> new Item("A", quantity, BigDecimal.ONE));

        assertEquals("La cantidad debe ser positiva", e.getMessage());
    }

    @ParameterizedTest
    @CsvSource(value = {"NIL,1,1", "'',1,1", "A,1,NIL", "A,1,-5"}, nullValues = "NIL")
    @DisplayName("sku y precio son obligatorios; el precio no puede ser negativo")
    void rejectsInvalidSkuOrPrice(String sku, int quantity, BigDecimal price) {
        assertThrows(IllegalArgumentException.class, () -> new Item(sku, quantity, price));
    }
}
