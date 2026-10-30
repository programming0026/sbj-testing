package com.juniverse.testing.service;

import java.util.UUID;
import org.springframework.stereotype.Component;

/** Genera identificadores de pedido. */
@Component
public class IdGenerator {

    /**
     * Nuevo identificador.
     *
     * @return identificador único
     */
    public String next() {
        return "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
