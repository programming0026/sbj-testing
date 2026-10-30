package com.juniverse.testing.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.juniverse.testing.model.CustomerTier;
import com.juniverse.testing.model.Item;
import com.juniverse.testing.model.Order;
import com.juniverse.testing.model.PlaceOrderRequest;
import com.juniverse.testing.repository.InMemoryOrderRepository;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Los cinco tipos de dobles de prueba escritos a mano (sin Mockito), para entender qué es cada uno:
 *
 * <ul>
 *   <li><b>Dummy</b>: se pasa para cumplir la firma, pero nunca se usa.</li>
 *   <li><b>Stub</b>: devuelve respuestas fijas.</li>
 *   <li><b>Fake</b>: implementación real pero simplificada (aquí, el repositorio en memoria).</li>
 *   <li><b>Spy</b>: registra cómo se le llamó para comprobarlo después.</li>
 *   <li><b>Mock</b>: sabe qué llamadas espera y falla si no ocurren (Mockito lo automatiza).</li>
 * </ul>
 */
@DisplayName("Dobles de prueba hechos a mano")
class TestDoublesTest {

    /** Stub: siempre hay existencias y no hace nada al reservar. */
    static class StubInventory implements InventoryClient {
        @Override
        public int available(String sku) {
            return 100;
        }

        @Override
        public void reserve(String sku, int quantity) {
            // sin efecto
        }
    }

    /** Stub: siempre aprueba el pago. */
    static class ApprovingGateway implements PaymentGateway {
        @Override
        public PaymentResult charge(String customerEmail, BigDecimal amount) {
            return new PaymentResult(true, "STUB");
        }
    }

    /** Spy escrito a mano: anota los avisos que recibe. */
    static class RecordingNotifications implements NotificationService {
        final List<String> sent = new ArrayList<>();

        @Override
        public void sendConfirmation(String email, String orderId) {
            sent.add(email + ":" + orderId);
        }
    }

    @Test
    @DisplayName("un pedido completo con stub, fake y spy manuales")
    void placeOrderWithHandWrittenDoubles() {
        InMemoryOrderRepository fakeRepository = new InMemoryOrderRepository(); // FAKE
        RecordingNotifications spyNotifications = new RecordingNotifications(); // SPY
        IdGenerator dummyIds = new IdGenerator();                                // se usa su implementación real
        OrderService service = new OrderService(new StubInventory(), new PricingService(), new ApprovingGateway(),
                fakeRepository, spyNotifications, dummyIds, Clock.fixed(Instant.EPOCH, ZoneOffset.UTC));

        Order order = service.place(new PlaceOrderRequest("ana@mail.com", CustomerTier.SILVER,
                List.of(new Item("TECLADO", 1, new BigDecimal("200.00")))));

        assertEquals(new BigDecimal("190.00"), order.total());
        assertEquals(order, fakeRepository.findById(order.id()).orElseThrow(), "el fake guarda de verdad");
        assertEquals(List.of("ana@mail.com:" + order.id()), spyNotifications.sent, "el spy registró el aviso");
        assertTrue(order.id().startsWith("ORD-"));
    }
}
