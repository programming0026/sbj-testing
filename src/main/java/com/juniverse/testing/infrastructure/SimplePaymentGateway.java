package com.juniverse.testing.infrastructure;

import com.juniverse.testing.service.PaymentGateway;
import java.math.BigDecimal;
import java.util.UUID;
import org.springframework.stereotype.Component;

/** Pasarela de pagos de ejemplo: rechaza los importes de 10,000 o más. */
@Component
public class SimplePaymentGateway implements PaymentGateway {

    @Override
    public PaymentResult charge(String customerEmail, BigDecimal amount) {
        boolean approved = amount.compareTo(new BigDecimal("10000")) < 0;
        return new PaymentResult(approved, UUID.randomUUID().toString().substring(0, 8));
    }
}
