package com.juniverse.testing.dto;

import com.juniverse.testing.model.CustomerTier;
import com.juniverse.testing.model.Item;
import java.util.List;

/**
 * Petición para crear un pedido.
 *
 * @param customerEmail correo del cliente
 * @param tier nivel del cliente (si es nulo se usa STANDARD)
 * @param items líneas del pedido
 */
public record PlaceOrderRequest(String customerEmail, CustomerTier tier, List<Item> items) {
}
