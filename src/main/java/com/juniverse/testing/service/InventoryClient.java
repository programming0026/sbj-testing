package com.juniverse.testing.service;

/** Puerto hacia el sistema de inventario (dependencia externa que se sustituye en las pruebas). */
public interface InventoryClient {

    /**
     * Existencias disponibles.
     *
     * @param sku producto
     * @return unidades disponibles
     */
    int available(String sku);

    /**
     * Reserva unidades.
     *
     * @param sku producto
     * @param quantity unidades
     */
    void reserve(String sku, int quantity);
}
