package com.juniverse.testing.controller;

import com.juniverse.testing.model.Order;
import com.juniverse.testing.dto.PlaceOrderRequest;
import com.juniverse.testing.service.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** API REST de pedidos. */
@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderService service;

    /**
     * Crea el controlador.
     *
     * @param service servicio de pedidos
     */
    public OrderController(OrderService service) {
        this.service = service;
    }

    /**
     * Crea un pedido (201).
     *
     * @param request datos del pedido
     * @return pedido confirmado
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Order create(@RequestBody PlaceOrderRequest request) {
        return service.place(request);
    }

    /**
     * Consulta un pedido.
     *
     * @param id identificador
     * @return el pedido
     */
    @GetMapping("/{id}")
    public Order get(@PathVariable String id) {
        return service.get(id);
    }
}
