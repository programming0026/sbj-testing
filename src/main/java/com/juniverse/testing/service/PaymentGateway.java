package com.juniverse.testing.service;

import java.math.BigDecimal;

/** Puerto hacia la pasarela de pagos. */
public interface PaymentGateway {

    /**
     * Resultado de un cobro.
     *
     * @param approved si fue aprobado
     * @param reference referencia de la operación
     */
    record PaymentResult(boolean approved, String reference) {
    }

    /**
     * Cobra un importe.
     *
     * @param customerEmail cliente
     * @param amount importe
     * @return resultado del cobro
     */
    PaymentResult charge(String customerEmail, BigDecimal amount);
}
