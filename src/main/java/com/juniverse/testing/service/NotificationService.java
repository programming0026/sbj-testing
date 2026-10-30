package com.juniverse.testing.service;

/** Puerto para avisar al cliente. */
public interface NotificationService {

    /**
     * Envía la confirmación de un pedido.
     *
     * @param email destinatario
     * @param orderId pedido confirmado
     */
    void sendConfirmation(String email, String orderId);
}
