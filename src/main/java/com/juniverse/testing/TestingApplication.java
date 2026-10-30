package com.juniverse.testing;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Aplicación de ejemplo (pedidos) pensada para practicar pruebas unitarias con JUnit 5 y Mockito.
 */
@SpringBootApplication
public class TestingApplication {

    /**
     * Arranca la aplicación.
     *
     * @param args argumentos de la línea de comandos
     */
    public static void main(String[] args) {
        SpringApplication.run(TestingApplication.class, args);
    }
}
