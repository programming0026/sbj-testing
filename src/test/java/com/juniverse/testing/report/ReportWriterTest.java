package com.juniverse.testing.report;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.juniverse.testing.model.Order;
import com.juniverse.testing.model.OrderStatus;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Pruebas con archivos: {@code @TempDir} crea una carpeta temporal que se borra sola al terminar. */
@DisplayName("ReportWriter")
class ReportWriterTest {

    @TempDir
    Path tempDir;

    @Test
    @DisplayName("escribe el encabezado y una línea por pedido")
    void writesCsv() throws IOException {
        Path file = tempDir.resolve("pedidos.csv");
        List<Order> orders = List.of(
                new Order("ORD-1", "ana@mail.com", List.of(), new BigDecimal("880.00"), OrderStatus.PAID, Instant.EPOCH),
                new Order("ORD-2", "luis@mail.com", List.of(), new BigDecimal("50.00"), OrderStatus.CANCELLED, Instant.EPOCH));

        new ReportWriter().write(orders, file);

        assertTrue(Files.exists(file));
        assertEquals(List.of("id,cliente,total,estado", "ORD-1,ana@mail.com,880.00,PAID", "ORD-2,luis@mail.com,50.00,CANCELLED"),
                Files.readAllLines(file));
    }

    @Test
    @DisplayName("sin pedidos solo escribe el encabezado")
    void emptyReport() throws IOException {
        Path file = tempDir.resolve("vacio.csv");

        new ReportWriter().write(List.of(), file);

        assertEquals(List.of("id,cliente,total,estado"), Files.readAllLines(file));
    }
}
