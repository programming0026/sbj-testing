package com.juniverse.testing.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.juniverse.testing.exception.OrderNotFoundException;
import com.juniverse.testing.exception.OutOfStockException;
import com.juniverse.testing.exception.PaymentFailedException;
import com.juniverse.testing.model.CustomerTier;
import com.juniverse.testing.model.Item;
import com.juniverse.testing.model.Order;
import com.juniverse.testing.model.OrderStatus;
import com.juniverse.testing.model.PlaceOrderRequest;
import com.juniverse.testing.repository.OrderRepository;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Pruebas unitarias de {@link OrderService} aislándolo de sus colaboradores con Mockito.
 *
 * <ul>
 *   <li>{@code @Mock}: dobles totalmente falsos (inventario, pagos, repositorio, avisos, generador de ids).</li>
 *   <li>{@code @Spy}: el {@code PricingService} REAL, pero vigilado (se puede verificar y, si hace falta, sustituir un método).</li>
 *   <li>{@code @Captor}: captura los argumentos con los que se llamó a un colaborador.</li>
 *   <li>{@code MockitoExtension} activa el modo estricto: un {@code when(...)} que no se usa hace fallar la prueba.</li>
 * </ul>
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("OrderService con Mockito")
class OrderServiceTest {

    private static final String EMAIL = "ana@mail.com";
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-01-15T10:00:00Z"), ZoneOffset.UTC);

    @Mock
    private InventoryClient inventory;
    @Mock
    private PaymentGateway payments;
    @Mock
    private OrderRepository repository;
    @Mock
    private NotificationService notifications;
    @Mock
    private IdGenerator ids;
    @Spy
    private PricingService pricing = new PricingService();

    @Captor
    private ArgumentCaptor<Order> orderCaptor;
    @Captor
    private ArgumentCaptor<BigDecimal> amountCaptor;

    private OrderService service;

    @BeforeEach
    void setUp() {
        service = new OrderService(inventory, pricing, payments, repository, notifications, ids, CLOCK);
    }

    private static PlaceOrderRequest request(CustomerTier tier) {
        return new PlaceOrderRequest(EMAIL, tier, List.of(new Item("TECLADO", 2, new BigDecimal("500.00"))));
    }

    /** Prepara los dobles para el camino feliz. */
    private void stubHappyPath() {
        when(inventory.available("TECLADO")).thenReturn(5);
        when(payments.charge(eq(EMAIL), any(BigDecimal.class))).thenReturn(new PaymentGateway.PaymentResult(true, "REF-1"));
        when(ids.next()).thenReturn("ORD-1");
    }

    @Test
    @DisplayName("crea el pedido: cobra, guarda, reserva y avisa")
    void placesOrder() {
        stubHappyPath();

        Order order = service.place(request(CustomerTier.GOLD));

        // 2 x 500 = 1000 -> GOLD 10 % + 2 % por volumen = 12 % -> 880.00
        assertEquals(new BigDecimal("880.00"), order.total());
        assertEquals("ORD-1", order.id());
        assertEquals(OrderStatus.PAID, order.status());
        assertEquals(CLOCK.instant(), order.createdAt(), "la hora sale del reloj inyectado, no del sistema");

        verify(repository).save(orderCaptor.capture());
        assertEquals(order, orderCaptor.getValue(), "lo guardado es lo devuelto");
        verify(inventory).reserve("TECLADO", 2);
        verify(notifications).sendConfirmation(EMAIL, "ORD-1");
    }

    @Test
    @DisplayName("el orden de las llamadas importa: cobrar -> guardar -> avisar (InOrder)")
    void callsHappenInOrder() {
        stubHappyPath();

        service.place(request(CustomerTier.STANDARD));

        InOrder inOrder = inOrder(payments, repository, notifications);
        inOrder.verify(payments).charge(eq(EMAIL), any(BigDecimal.class));
        inOrder.verify(repository).save(any(Order.class));
        inOrder.verify(notifications).sendConfirmation(anyString(), anyString());
    }

    @Test
    @DisplayName("el spy ejecuta el código REAL de PricingService y registra las llamadas")
    void spyRunsRealCodeAndRecordsCalls() {
        stubHappyPath();
        PlaceOrderRequest req = request(CustomerTier.SILVER);

        service.place(req);

        verify(pricing).total(req.items(), CustomerTier.SILVER);
        verify(pricing, times(1)).subtotal(req.items()); // total() llamó internamente a subtotal()
        verify(payments).charge(eq(EMAIL), amountCaptor.capture());
        assertEquals(new BigDecimal("930.00"), amountCaptor.getValue(), "SILVER 5 % + 2 % por volumen sobre 1000");
    }

    @Test
    @DisplayName("doReturn sobre un spy sustituye solo ese método (mock parcial)")
    void stubbingASpy() {
        when(inventory.available("TECLADO")).thenReturn(5);
        // Con spies se usa doReturn(...).when(spy): when(spy.total(...)) ejecutaría el método real al configurarlo
        doReturn(new BigDecimal("1.00")).when(pricing).total(any(), any());
        when(payments.charge(eq(EMAIL), any(BigDecimal.class))).thenReturn(new PaymentGateway.PaymentResult(true, "REF-2"));
        when(ids.next()).thenReturn("ORD-2");

        Order order = service.place(request(CustomerTier.GOLD));

        assertEquals(new BigDecimal("1.00"), order.total());
        verify(pricing, never()).subtotal(any()); // el método real ya no se ejecutó
    }

    @Test
    @DisplayName("sin existencias: lanza la excepción y NO cobra, guarda ni avisa")
    void outOfStock() {
        when(inventory.available("TECLADO")).thenReturn(1);

        OutOfStockException e = assertThrows(OutOfStockException.class, () -> service.place(request(CustomerTier.STANDARD)));

        assertTrue(e.getMessage().contains("TECLADO"));
        verifyNoInteractions(payments, repository, notifications, ids);
        verify(inventory, never()).reserve(anyString(), anyInt());
    }

    @Test
    @DisplayName("pago rechazado: lanza la excepción y no guarda ni avisa")
    void paymentRejected() {
        when(inventory.available("TECLADO")).thenReturn(5);
        when(payments.charge(eq(EMAIL), any(BigDecimal.class))).thenReturn(new PaymentGateway.PaymentResult(false, "DECLINED"));

        PaymentFailedException e = assertThrows(PaymentFailedException.class, () -> service.place(request(CustomerTier.STANDARD)));

        assertTrue(e.getMessage().contains("DECLINED"));
        verify(repository, never()).save(any());
        verifyNoInteractions(notifications);
    }

    @Test
    @DisplayName("doThrow en un método void: si falla el aviso, el pedido se conserva")
    void notificationFailureDoesNotUndoTheOrder() {
        stubHappyPath();
        doThrow(new IllegalStateException("SMTP caído")).when(notifications).sendConfirmation(anyString(), anyString());

        Order order = service.place(request(CustomerTier.STANDARD));

        assertEquals("ORD-1", order.id());
        verify(repository).save(any(Order.class));
    }

    @ParameterizedTest(name = "correo inválido: \"{0}\"")
    @NullAndEmptySource
    @ValueSource(strings = {"sin-arroba", "ana@", "@mail.com"})
    @DisplayName("un correo inválido se rechaza antes de tocar ningún colaborador")
    void invalidEmail(String email) {
        PlaceOrderRequest req = new PlaceOrderRequest(email, CustomerTier.STANDARD, List.of(new Item("TECLADO", 1, BigDecimal.TEN)));

        assertThrows(IllegalArgumentException.class, () -> service.place(req));

        verifyNoInteractions(inventory, payments, repository, notifications, ids);
    }

    @Test
    @DisplayName("un pedido sin líneas se rechaza")
    void emptyOrder() {
        assertThrows(IllegalArgumentException.class, () -> service.place(new PlaceOrderRequest(EMAIL, CustomerTier.STANDARD, List.of())));

        verifyNoInteractions(inventory, payments);
    }

    @Test
    @DisplayName("sin nivel de cliente se usa STANDARD")
    void defaultsToStandardTier() {
        stubHappyPath();
        PlaceOrderRequest req = new PlaceOrderRequest(EMAIL, null, List.of(new Item("TECLADO", 2, new BigDecimal("500.00"))));

        Order order = service.place(req);

        assertEquals(new BigDecimal("980.00"), order.total()); // solo el 2 % por volumen
    }

    @Test
    @DisplayName("get: devuelve el pedido o lanza OrderNotFoundException")
    void getOrder() {
        Order existing = new Order("ORD-9", EMAIL, List.of(), BigDecimal.TEN, OrderStatus.PAID, CLOCK.instant());
        when(repository.findById("ORD-9")).thenReturn(Optional.of(existing));
        when(repository.findById("NOPE")).thenReturn(Optional.empty());

        assertEquals(existing, service.get("ORD-9"));
        assertThrows(OrderNotFoundException.class, () -> service.get("NOPE"));
    }

    @Test
    @DisplayName("cancel: guarda el pedido con estado CANCELLED")
    void cancelOrder() {
        Order existing = new Order("ORD-9", EMAIL, List.of(), BigDecimal.TEN, OrderStatus.PAID, CLOCK.instant());
        when(repository.findById("ORD-9")).thenReturn(Optional.of(existing));
        when(repository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0)); // devuelve lo que recibe

        Order cancelled = service.cancel("ORD-9");

        assertEquals(OrderStatus.CANCELLED, cancelled.status());
        verify(repository).save(orderCaptor.capture());
        assertEquals(OrderStatus.CANCELLED, orderCaptor.getValue().status());
        assertEquals(existing.total(), orderCaptor.getValue().total(), "el resto de los datos no cambia");
    }
}
