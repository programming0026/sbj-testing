package com.juniverse.testing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.juniverse.testing.exception.PaymentFailedException;
import com.juniverse.testing.model.CustomerTier;
import com.juniverse.testing.model.Item;
import com.juniverse.testing.model.Order;
import com.juniverse.testing.model.PlaceOrderRequest;
import com.juniverse.testing.service.NotificationService;
import com.juniverse.testing.service.OrderService;
import com.juniverse.testing.service.PaymentGateway;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

/**
 * Prueba de integración con el contexto de Spring: los beans reales se mezclan con dobles de Mockito
 * registrados mediante {@code @MockitoBean} (en Spring Boot 3.4 y anteriores se llamaba {@code @MockBean}).
 * Es más lenta que las pruebas unitarias, por eso se etiqueta y se usa con moderación.
 */
@SpringBootTest
@Tag("integration")
@DisplayName("Contexto de Spring con @MockitoBean")
class TestingApplicationTests {

    @Autowired
    private OrderService service;

    @MockitoBean
    private PaymentGateway paymentGateway;

    @MockitoBean
    private NotificationService notifications;

    @Test
    @DisplayName("el contexto arranca y se inyecta el servicio real")
    void contextLoads() {
        assertNotNull(service);
    }

    @Test
    @DisplayName("servicio real + inventario real + pasarela simulada: el pedido se crea y se avisa")
    void placesOrderWithMockedGateway() {
        when(paymentGateway.charge(anyString(), any(BigDecimal.class))).thenReturn(new PaymentGateway.PaymentResult(true, "OK"));

        Order order = service.place(new PlaceOrderRequest("ana@mail.com", CustomerTier.GOLD,
                List.of(new Item("RATON", 2, new BigDecimal("100.00")))));

        assertEquals(new BigDecimal("180.00"), order.total());
        verify(notifications).sendConfirmation("ana@mail.com", order.id());
        assertEquals(order, service.get(order.id()), "el repositorio real guardó el pedido");
    }

    @Test
    @DisplayName("pasarela que rechaza: el servicio real lanza PaymentFailedException")
    void rejectedPayment() {
        when(paymentGateway.charge(anyString(), any(BigDecimal.class))).thenReturn(new PaymentGateway.PaymentResult(false, "DECLINED"));

        assertThrows(PaymentFailedException.class, () -> service.place(new PlaceOrderRequest("ana@mail.com", CustomerTier.STANDARD,
                List.of(new Item("TECLADO", 1, BigDecimal.TEN)))));
    }
}
