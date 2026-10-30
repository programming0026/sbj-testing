package com.juniverse.testing.service;

import com.juniverse.testing.exception.OrderNotFoundException;
import com.juniverse.testing.exception.OutOfStockException;
import com.juniverse.testing.exception.PaymentFailedException;
import com.juniverse.testing.model.CustomerTier;
import com.juniverse.testing.model.Item;
import com.juniverse.testing.model.Order;
import com.juniverse.testing.model.OrderStatus;
import com.juniverse.testing.model.PlaceOrderRequest;
import com.juniverse.testing.repository.OrderRepository;
import com.juniverse.testing.util.EmailValidator;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Caso de uso "crear pedido": valida, comprueba existencias, calcula el total, cobra, guarda, reserva y avisa al cliente.
 * Tiene muchas colaboraciones, por eso es el candidato ideal para practicar con Mockito.
 */
@Service
public class OrderService {

    private static final Logger LOG = LoggerFactory.getLogger(OrderService.class);

    private final InventoryClient inventory;
    private final PricingService pricing;
    private final PaymentGateway payments;
    private final OrderRepository repository;
    private final NotificationService notifications;
    private final IdGenerator ids;
    private final Clock clock;

    /**
     * Crea el servicio con todas sus dependencias (inyección por constructor: facilita sustituirlas en las pruebas).
     *
     * @param inventory inventario
     * @param pricing cálculo de precios
     * @param payments pasarela de pagos
     * @param repository almacén de pedidos
     * @param notifications avisos al cliente
     * @param ids generador de identificadores
     * @param clock reloj (se inyecta para poder fijar la hora en las pruebas)
     */
    public OrderService(InventoryClient inventory, PricingService pricing, PaymentGateway payments,
            OrderRepository repository, NotificationService notifications, IdGenerator ids, Clock clock) {
        this.inventory = inventory;
        this.pricing = pricing;
        this.payments = payments;
        this.repository = repository;
        this.notifications = notifications;
        this.ids = ids;
        this.clock = clock;
    }

    /**
     * Crea un pedido.
     *
     * @param request datos del pedido
     * @return pedido confirmado
     * @throws IllegalArgumentException si el correo no es válido o no hay líneas
     * @throws OutOfStockException si falta stock de algún producto
     * @throws PaymentFailedException si el cobro es rechazado
     */
    public Order place(PlaceOrderRequest request) {
        if (!EmailValidator.isValid(request.customerEmail())) {
            throw new IllegalArgumentException("Correo inválido: " + request.customerEmail());
        }
        if (request.items() == null || request.items().isEmpty()) {
            throw new IllegalArgumentException("El pedido no tiene líneas");
        }
        for (Item item : request.items()) {
            if (inventory.available(item.sku()) < item.quantity()) {
                throw new OutOfStockException(item.sku());
            }
        }

        CustomerTier tier = request.tier() == null ? CustomerTier.STANDARD : request.tier();
        BigDecimal total = pricing.total(request.items(), tier);

        PaymentGateway.PaymentResult result = payments.charge(request.customerEmail(), total);
        if (!result.approved()) {
            throw new PaymentFailedException("Cobro rechazado (" + result.reference() + ")");
        }

        Order order = new Order(ids.next(), request.customerEmail(), request.items(), total, OrderStatus.PAID, Instant.now(clock));
        repository.save(order);
        request.items().forEach(i -> inventory.reserve(i.sku(), i.quantity()));

        try {
            notifications.sendConfirmation(order.customerEmail(), order.id());
        } catch (RuntimeException e) {
            // Un fallo al avisar NO debe deshacer un pedido ya cobrado
            LOG.warn("No se pudo enviar la confirmación del pedido {}: {}", order.id(), e.getMessage());
        }
        return order;
    }

    /**
     * Busca un pedido.
     *
     * @param id identificador
     * @return el pedido
     * @throws OrderNotFoundException si no existe
     */
    public Order get(String id) {
        return repository.findById(id).orElseThrow(() -> new OrderNotFoundException(id));
    }

    /**
     * Cancela un pedido.
     *
     * @param id identificador
     * @return pedido cancelado
     * @throws OrderNotFoundException si no existe
     */
    public Order cancel(String id) {
        Order current = get(id);
        Order cancelled = new Order(current.id(), current.customerEmail(), current.items(), current.total(), OrderStatus.CANCELLED, current.createdAt());
        return repository.save(cancelled);
    }
}
