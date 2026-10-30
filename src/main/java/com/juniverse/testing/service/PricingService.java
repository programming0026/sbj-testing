package com.juniverse.testing.service;

import com.juniverse.testing.model.CustomerTier;
import com.juniverse.testing.model.Item;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * Cálculo de precios: lógica pura, sin dependencias (ideal para pruebas parametrizadas).
 *
 * <p>Reglas: descuento por nivel del cliente (0 %, 5 %, 10 %) y 2 % adicional si el subtotal es de 1000 o más.</p>
 */
@Service
public class PricingService {

    /** Subtotal a partir del cual se aplica el descuento adicional. */
    public static final BigDecimal BULK_THRESHOLD = new BigDecimal("1000");

    /**
     * Suma de las líneas.
     *
     * @param items líneas del pedido
     * @return subtotal sin descuentos
     */
    public BigDecimal subtotal(List<Item> items) {
        return items.stream().map(Item::subtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Porcentaje total de descuento.
     *
     * @param tier nivel del cliente
     * @param subtotal subtotal del pedido
     * @return porcentaje de descuento
     */
    public BigDecimal discountPercent(CustomerTier tier, BigDecimal subtotal) {
        BigDecimal percent = tier.discountPercent();
        if (subtotal.compareTo(BULK_THRESHOLD) >= 0) {
            percent = percent.add(BigDecimal.valueOf(2));
        }
        return percent;
    }

    /**
     * Total a cobrar, redondeado a dos decimales.
     *
     * @param items líneas del pedido
     * @param tier nivel del cliente
     * @return total con descuento
     */
    public BigDecimal total(List<Item> items, CustomerTier tier) {
        BigDecimal subtotal = subtotal(items);
        BigDecimal discount = subtotal.multiply(discountPercent(tier, subtotal)).divide(BigDecimal.valueOf(100));
        return subtotal.subtract(discount).setScale(2, RoundingMode.HALF_UP);
    }
}
