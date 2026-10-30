package com.juniverse.testing.exception;

/** No hay existencias suficientes de un producto. */
public class OutOfStockException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Crea la excepción.
     *
     * @param sku producto sin existencias
     */
    public OutOfStockException(String sku) {
        super("Sin existencias de " + sku);
    }
}
