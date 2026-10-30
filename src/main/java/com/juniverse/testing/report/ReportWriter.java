package com.juniverse.testing.report;

import com.juniverse.testing.model.Order;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/** Escribe un informe CSV de pedidos (sirve para practicar con {@code @TempDir}). */
public class ReportWriter {

    /**
     * Escribe el CSV con encabezado.
     *
     * @param orders pedidos
     * @param file archivo destino
     * @throws IOException si no se puede escribir
     */
    public void write(List<Order> orders, Path file) throws IOException {
        StringBuilder sb = new StringBuilder("id,cliente,total,estado\n");
        for (Order o : orders) {
            sb.append(o.id()).append(',').append(o.customerEmail()).append(',').append(o.total()).append(',').append(o.status()).append('\n');
        }
        Files.writeString(file, sb.toString(), StandardCharsets.UTF_8);
    }
}
