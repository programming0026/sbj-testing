package com.juniverse.testing.model;

import java.math.BigDecimal;

/**
 * Nivel del cliente; define el porcentaje base de descuento.
 */
public enum CustomerTier {
    STANDARD(0), SILVER(5), GOLD(10);

    private final BigDecimal discountPercent;

    CustomerTier(int percent) {
        this.discountPercent = BigDecimal.valueOf(percent);
    }

    /**
     * Porcentaje de descuento del nivel.
     *
     * @return porcentaje (0, 5 o 10)
     */
    public BigDecimal discountPercent() {
        return discountPercent;
    }
}
