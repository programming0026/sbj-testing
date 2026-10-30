package com.juniverse.testing.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * Pedido ya confirmado.
 *
 * @param id identificador
 * @param customerEmail correo del cliente
 * @param items líneas del pedido
 * @param total importe cobrado
 * @param status estado
 * @param createdAt momento de creación
 */
public record Order(String id, String customerEmail, List<Item> items, BigDecimal total, OrderStatus status, Instant createdAt) {
}
