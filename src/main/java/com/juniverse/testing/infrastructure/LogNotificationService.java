package com.juniverse.testing.infrastructure;

import com.juniverse.testing.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/** Notificaciones de ejemplo: solo escribe en el log. */
@Component
public class LogNotificationService implements NotificationService {

    private static final Logger LOG = LoggerFactory.getLogger(LogNotificationService.class);

    @Override
    public void sendConfirmation(String email, String orderId) {
        LOG.info("Confirmación del pedido {} enviada a {}", orderId, email);
    }
}
