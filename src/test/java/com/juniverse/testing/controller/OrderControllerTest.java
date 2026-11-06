package com.juniverse.testing.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.juniverse.testing.exception.OrderNotFoundException;
import com.juniverse.testing.exception.OutOfStockException;
import com.juniverse.testing.exception.PaymentFailedException;
import com.juniverse.testing.model.CustomerTier;
import com.juniverse.testing.model.Order;
import com.juniverse.testing.model.OrderStatus;
import com.juniverse.testing.dto.PlaceOrderRequest;
import com.juniverse.testing.service.OrderService;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import com.juniverse.testing.exception.ApiExceptionHandler;

/**
 * Prueba de la capa web SIN levantar Spring: {@code MockMvc} en modo "standalone" con el servicio simulado por Mockito.
 * Es rápida y comprueba rutas, códigos HTTP, JSON y el manejo de excepciones.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("OrderController (MockMvc standalone)")
class OrderControllerTest {

    private static final String BODY = """
            {"customerEmail":"ana@mail.com","tier":"GOLD","items":[{"sku":"TECLADO","quantity":2,"unitPrice":500.00}]}
            """;

    @Mock
    private OrderService service;

    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.standaloneSetup(new OrderController(service)).setControllerAdvice(new ApiExceptionHandler()).build();
    }

    private static Order order() {
        return new Order("ORD-1", "ana@mail.com", List.of(), new BigDecimal("880.00"), OrderStatus.PAID, Instant.parse("2026-01-15T10:00:00Z"));
    }

    @Test
    @DisplayName("POST /orders -> 201 con el pedido y envía la petición correcta al servicio")
    void create() throws Exception {
        when(service.place(any(PlaceOrderRequest.class))).thenReturn(order());

        mvc.perform(post("/orders").contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("ORD-1"))
                .andExpect(jsonPath("$.status").value("PAID"));

        ArgumentCaptor<PlaceOrderRequest> captor = ArgumentCaptor.forClass(PlaceOrderRequest.class);
        verify(service).place(captor.capture());
        assertEquals(CustomerTier.GOLD, captor.getValue().tier());
        assertEquals("TECLADO", captor.getValue().items().get(0).sku());
    }

    @Test
    @DisplayName("GET /orders/{id} -> 200")
    void getExisting() throws Exception {
        when(service.get("ORD-1")).thenReturn(order());

        mvc.perform(get("/orders/ORD-1")).andExpect(status().isOk()).andExpect(jsonPath("$.customerEmail").value("ana@mail.com"));
    }

    @Test
    @DisplayName("GET /orders/{id} inexistente -> 404 con el mensaje")
    void getMissing() throws Exception {
        when(service.get("X")).thenThrow(new OrderNotFoundException("X"));

        mvc.perform(get("/orders/X")).andExpect(status().isNotFound()).andExpect(jsonPath("$.error").value("Pedido no encontrado: X"));
    }

    @Test
    @DisplayName("sin existencias -> 409")
    void outOfStock() throws Exception {
        when(service.place(any(PlaceOrderRequest.class))).thenThrow(new OutOfStockException("TECLADO"));

        mvc.perform(post("/orders").contentType(MediaType.APPLICATION_JSON).content(BODY)).andExpect(status().isConflict());
    }

    @Test
    @DisplayName("pago rechazado -> 402")
    void paymentFailed() throws Exception {
        when(service.place(any(PlaceOrderRequest.class))).thenThrow(new PaymentFailedException("Cobro rechazado"));

        mvc.perform(post("/orders").contentType(MediaType.APPLICATION_JSON).content(BODY)).andExpect(status().isPaymentRequired());
    }

    @Test
    @DisplayName("datos inválidos -> 400")
    void badRequest() throws Exception {
        when(service.place(any(PlaceOrderRequest.class))).thenThrow(new IllegalArgumentException("Correo inválido: x"));

        mvc.perform(post("/orders").contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Correo inválido: x"));
    }
}
