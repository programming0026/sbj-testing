package com.juniverse.testing.exception;

import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** Convierte las excepciones del dominio en respuestas HTTP. */
@RestControllerAdvice
public class ApiExceptionHandler {

    /**
     * 404: el pedido no existe.
     *
     * @param e excepción
     * @return respuesta de error
     */
    @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<Map<String, String>> notFound(OrderNotFoundException e) {
        return error(HttpStatus.NOT_FOUND, e);
    }

    /**
     * 409: no hay existencias.
     *
     * @param e excepción
     * @return respuesta de error
     */
    @ExceptionHandler(OutOfStockException.class)
    public ResponseEntity<Map<String, String>> outOfStock(OutOfStockException e) {
        return error(HttpStatus.CONFLICT, e);
    }

    /**
     * 402: pago rechazado.
     *
     * @param e excepción
     * @return respuesta de error
     */
    @ExceptionHandler(PaymentFailedException.class)
    public ResponseEntity<Map<String, String>> paymentFailed(PaymentFailedException e) {
        return error(HttpStatus.PAYMENT_REQUIRED, e);
    }

    /**
     * 400: datos inválidos.
     *
     * @param e excepción
     * @return respuesta de error
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> badRequest(IllegalArgumentException e) {
        return error(HttpStatus.BAD_REQUEST, e);
    }

    private ResponseEntity<Map<String, String>> error(HttpStatus status, Exception e) {
        return ResponseEntity.status(status).body(Map.of("error", e.getMessage()));
    }
}
