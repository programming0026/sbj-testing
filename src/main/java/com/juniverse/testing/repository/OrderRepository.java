package com.juniverse.testing.repository;

import com.juniverse.testing.model.Order;
import java.util.Optional;

/** Almacén de pedidos. */
public interface OrderRepository {

    /**
     * Guarda (o reemplaza) un pedido.
     *
     * @param order pedido
     * @return el pedido guardado
     */
    Order save(Order order);

    /**
     * Busca por identificador.
     *
     * @param id identificador
     * @return el pedido, si existe
     */
    Optional<Order> findById(String id);
}
