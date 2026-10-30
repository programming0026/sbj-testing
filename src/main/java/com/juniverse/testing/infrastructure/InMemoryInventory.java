package com.juniverse.testing.infrastructure;

import com.juniverse.testing.service.InventoryClient;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

/** Inventario de ejemplo con existencias fijas. */
@Component
public class InMemoryInventory implements InventoryClient {

    private final Map<String, Integer> stock = new ConcurrentHashMap<>(Map.of("TECLADO", 10, "RATON", 25, "MONITOR", 3));

    @Override
    public int available(String sku) {
        return stock.getOrDefault(sku, 0);
    }

    @Override
    public void reserve(String sku, int quantity) {
        stock.merge(sku, -quantity, Integer::sum);
    }
}
