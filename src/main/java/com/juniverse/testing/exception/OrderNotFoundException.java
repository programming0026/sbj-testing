package com.juniverse.testing.exception;

/** No existe el pedido solicitado. */
public class OrderNotFoundException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Crea la excepción.
     *
     * @param id identificador buscado
     */
    public OrderNotFoundException(String id) {
        super("Pedido no encontrado: " + id);
    }
}
