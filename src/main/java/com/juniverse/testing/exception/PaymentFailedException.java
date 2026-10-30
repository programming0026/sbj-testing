package com.juniverse.testing.exception;

/** El cobro fue rechazado. */
public class PaymentFailedException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Crea la excepción.
     *
     * @param message motivo del rechazo
     */
    public PaymentFailedException(String message) {
        super(message);
    }
}
