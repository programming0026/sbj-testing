package com.juniverse.testing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeout;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.RepetitionInfo;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.TestMethodOrder;

/**
 * Recorrido por las características de JUnit 5 que no son de Mockito: ciclo de vida, orden, repeticiones,
 * pruebas dinámicas, suposiciones, límites de tiempo, etiquetas y pruebas deshabilitadas.
 */
@DisplayName("Características de JUnit 5")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class JUnitFeaturesTest {

    private static final List<String> LOG = new ArrayList<>();
    private List<Integer> numbers;

    @BeforeAll
    static void beforeAll() {
        LOG.add("beforeAll (una vez, antes de todas las pruebas; debe ser static)");
    }

    @AfterAll
    static void afterAll() {
        LOG.add("afterAll (una vez, al final)");
    }

    @BeforeEach
    void beforeEach(TestInfo info) {
        numbers = new ArrayList<>(List.of(3, 1, 2)); // estado limpio para cada prueba
        LOG.add("beforeEach -> " + info.getDisplayName());
    }

    @AfterEach
    void afterEach() {
        numbers.clear();
    }

    @Test
    @Order(1)
    @DisplayName("1. el ciclo de vida da a cada prueba un estado nuevo")
    void freshStatePerTest() {
        numbers.add(99);

        assertEquals(4, numbers.size());
    }

    @Test
    @Order(2)
    @DisplayName("2. la prueba anterior NO afectó a esta (se creó una instancia nueva)")
    void stateDoesNotLeak() {
        assertEquals(3, numbers.size());
        assertNotNull(LOG.get(0));
    }

    @Test
    @Order(3)
    @DisplayName("3. assertIterableEquals y assertThrows con el mensaje")
    void collectionsAndExceptions() {
        assertIterableEquals(List.of(1, 2, 3), numbers.stream().sorted().toList());
        IndexOutOfBoundsException e = assertThrows(IndexOutOfBoundsException.class, () -> numbers.get(10));
        assertTrue(e.getMessage().contains("10"));
    }

    @RepeatedTest(value = 3, name = "repetición {currentRepetition} de {totalRepetitions}")
    @Order(4)
    @Tag("slow")
    @DisplayName("4. @RepeatedTest ejecuta la misma prueba varias veces")
    void repeated(RepetitionInfo info) {
        assertTrue(info.getCurrentRepetition() <= info.getTotalRepetitions());
    }

    @Test
    @Order(5)
    @DisplayName("5. assertTimeout falla si el código tarda más del límite")
    void timeLimit() {
        String result = assertTimeout(Duration.ofSeconds(2), () -> "terminó a tiempo");

        assertEquals("terminó a tiempo", result);
    }

    @Test
    @Order(6)
    @DisplayName("6. assumeTrue: si la suposición falla la prueba se OMITE (no falla)")
    void assumptions() {
        assumeTrue(Runtime.version().feature() >= 21, "esta prueba solo aplica a Java 21 o superior");

        assertEquals(21, Math.min(21, Runtime.version().feature()));
    }

    @Test
    @Order(7)
    @Disabled("Ejemplo de prueba deshabilitada: aparece como omitida en el informe")
    void disabledTest() {
        throw new AssertionError("no se ejecuta");
    }

    @TestFactory
    @Order(8)
    @DisplayName("8. @TestFactory genera pruebas dinámicas en tiempo de ejecución")
    Stream<DynamicTest> dynamicTests() {
        return Stream.of(2, 4, 6, 8).map(n -> DynamicTest.dynamicTest(n + " es par", () -> assertEquals(0, n % 2)));
    }
}
