package com.juniverse.testing.model;

import java.math.BigDecimal;

/**
 * Línea de un pedido.
 *
 * @param sku código del producto
 * @param quantity cantidad (mayor a cero)
 * @param unitPrice precio unitario (no negativo)
 */
public record Item(String sku, int quantity, BigDecimal unitPrice) {

    /** Valida los datos al crear la línea. */
    public Item {
        if (sku == null || sku.isBlank()) {
            throw new IllegalArgumentException("El sku es obligatorio");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser positiva");
        }
        if (unitPrice == null || unitPrice.signum() < 0) {
            throw new IllegalArgumentException("El precio no puede ser negativo");
        }
    }

    /**
     * Importe de la línea.
     *
     * @return cantidad por precio unitario
     */
    public BigDecimal subtotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}
