package com.juniverse.testing.repository;

import com.juniverse.testing.model.Order;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Repository;

/** Implementación en memoria (suficiente para el ejemplo). */
@Repository
public class InMemoryOrderRepository implements OrderRepository {

    private final Map<String, Order> data = new ConcurrentHashMap<>();

    @Override
    public Order save(Order order) {
        data.put(order.id(), order);
        return order;
    }

    @Override
    public Optional<Order> findById(String id) {
        return Optional.ofNullable(data.get(id));
    }
}
