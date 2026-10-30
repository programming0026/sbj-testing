package com.juniverse.testing.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.atMost;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.mockingDetails;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.juniverse.testing.model.CustomerTier;
import com.juniverse.testing.model.Item;
import com.juniverse.testing.util.EmailValidator;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;

/**
 * Catálogo de técnicas de Mockito, una por prueba. No usa la extensión de Mockito, así que los dobles se crean a mano
 * con {@code mock()} y {@code spy()} (otra forma válida de usar la biblioteca).
 */
@DisplayName("Técnicas de Mockito")
class MockitoTechniquesTest {

    @Nested
    @DisplayName("Configurar respuestas (stubbing)")
    class Stubbing {

        @Test
        @DisplayName("when/thenReturn con varios valores consecutivos")
        void consecutiveCalls() {
            IdGenerator ids = mock(IdGenerator.class);
            when(ids.next()).thenReturn("A", "B", "C");

            assertEquals("A", ids.next());
            assertEquals("B", ids.next());
            assertEquals("C", ids.next());
            assertEquals("C", ids.next(), "después de la última, repite el último valor");
        }

        @Test
        @DisplayName("thenReturn y luego thenThrow encadenados")
        void returnThenThrow() {
            InventoryClient inventory = mock(InventoryClient.class);
            when(inventory.available("X")).thenReturn(3).thenThrow(new IllegalStateException("servicio caído"));

            assertEquals(3, inventory.available("X"));
            assertThrows(IllegalStateException.class, () -> inventory.available("X"));
        }

        @Test
        @DisplayName("thenAnswer calcula la respuesta a partir de los argumentos")
        void answerUsesArguments() {
            InventoryClient inventory = mock(InventoryClient.class);
            when(inventory.available(anyString())).thenAnswer(invocation -> ((String) invocation.getArgument(0)).length() * 10);

            assertEquals(30, inventory.available("abc"));
            assertEquals(50, inventory.available("abcde"));
        }

        @Test
        @DisplayName("sin configurar, un mock devuelve valores por defecto (0, false, null, Optional.empty)")
        void defaultAnswers() {
            InventoryClient inventory = mock(InventoryClient.class);
            com.juniverse.testing.repository.OrderRepository repository = mock(com.juniverse.testing.repository.OrderRepository.class);

            assertEquals(0, inventory.available("cualquiera"));
            assertTrue(repository.findById("x").isEmpty());
            assertTrue(mockingDetails(inventory).isMock());
        }

        @Test
        @DisplayName("métodos void: doNothing, doThrow y doAnswer")
        void voidMethods() {
            NotificationService notifications = mock(NotificationService.class);
            AtomicInteger calls = new AtomicInteger();
            doNothing().when(notifications).sendConfirmation("ok@mail.com", "1");
            doThrow(new IllegalStateException("rechazado")).when(notifications).sendConfirmation(eq("mal@mail.com"), anyString());
            doAnswer(invocation -> {
                calls.incrementAndGet();
                return null;
            }).when(notifications).sendConfirmation(eq("cuenta@mail.com"), anyString());

            notifications.sendConfirmation("ok@mail.com", "1");
            assertThrows(IllegalStateException.class, () -> notifications.sendConfirmation("mal@mail.com", "2"));
            notifications.sendConfirmation("cuenta@mail.com", "3");
            notifications.sendConfirmation("cuenta@mail.com", "4");

            assertEquals(2, calls.get());
        }
    }

    @Nested
    @DisplayName("Comodines de argumentos (matchers)")
    class Matchers {

        @Test
        @DisplayName("anyString, anyInt, eq, startsWith y argThat")
        void matchers() {
            InventoryClient inventory = mock(InventoryClient.class);
            when(inventory.available(startsWith("TEC"))).thenReturn(10);
            when(inventory.available(argThat(sku -> sku != null && sku.endsWith("-AGOTADO")))).thenReturn(0);

            assertEquals(10, inventory.available("TECLADO"));
            assertEquals(0, inventory.available("RATON-AGOTADO"));

            inventory.reserve("TECLADO", 3);
            verify(inventory).reserve(anyString(), anyInt());
            verify(inventory).reserve(eq("TECLADO"), eq(3));
            // Regla: si usas un matcher en un argumento, TODOS los argumentos deben ser matchers (usa eq(...) para los valores fijos)
        }
    }

    @Nested
    @DisplayName("Verificar interacciones")
    class Verification {

        @Test
        @DisplayName("times, never, atLeast, atMost y verifyNoMoreInteractions")
        void counts() {
            InventoryClient inventory = mock(InventoryClient.class);

            inventory.reserve("A", 1);
            inventory.reserve("A", 1);
            inventory.reserve("B", 5);

            verify(inventory, times(2)).reserve("A", 1);
            verify(inventory, atLeast(1)).reserve(eq("B"), anyInt());
            verify(inventory, atMost(3)).reserve(anyString(), anyInt());
            verify(inventory, never()).available(anyString());
            verify(inventory).reserve("B", 5);
            verifyNoMoreInteractions(inventory);
        }

        @Test
        @DisplayName("ArgumentCaptor: todos los valores con los que se llamó")
        void captorAllValues() {
            InventoryClient inventory = mock(InventoryClient.class);
            inventory.reserve("A", 1);
            inventory.reserve("B", 2);
            ArgumentCaptor<String> skus = ArgumentCaptor.forClass(String.class);
            ArgumentCaptor<Integer> quantities = ArgumentCaptor.forClass(Integer.class);

            verify(inventory, times(2)).reserve(skus.capture(), quantities.capture());

            assertEquals(List.of("A", "B"), skus.getAllValues());
            assertEquals(List.of(1, 2), quantities.getAllValues());
        }

        @Test
        @DisplayName("timeout: espera a que otro hilo haga la llamada")
        void timeoutWaitsForAsyncCall() {
            NotificationService notifications = mock(NotificationService.class);

            Thread.ofVirtual().start(() -> {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                notifications.sendConfirmation("a@b.c", "1");
            });

            verify(notifications, timeout(2000)).sendConfirmation("a@b.c", "1");
        }
    }

    @Nested
    @DisplayName("Spy: objeto real vigilado")
    class Spies {

        @Test
        @DisplayName("spy de una lista real: usa el código real salvo lo que se sustituya")
        void spyOnRealList() {
            List<String> real = new ArrayList<>();
            List<String> spyList = spy(real);

            spyList.add("uno");
            spyList.add("dos");
            org.mockito.Mockito.doReturn(100).when(spyList).size(); // solo size() está sustituido

            assertEquals(100, spyList.size());
            assertEquals("uno", spyList.get(0), "get() sigue siendo el real");
            verify(spyList, times(2)).add(anyString());
        }

        @Test
        @DisplayName("CALLS_REAL_METHODS: un mock que ejecuta el código real salvo lo configurado")
        void partialMock() {
            PricingService pricing = mock(PricingService.class, CALLS_REAL_METHODS);
            List<Item> items = List.of(new Item("A", 1, new BigDecimal("100.00")));

            assertEquals(new BigDecimal("100.00"), pricing.total(items, CustomerTier.STANDARD));

            org.mockito.Mockito.doReturn(BigDecimal.TEN).when(pricing).subtotal(items);
            assertEquals(0, new BigDecimal("10.00").compareTo(pricing.total(items, CustomerTier.STANDARD)));
        }
    }

    @Nested
    @DisplayName("BDDMockito: given / when / then")
    class Bdd {

        @Test
        @DisplayName("el mismo mock escrito como Dado-Cuando-Entonces")
        void givenWhenThen() {
            InventoryClient inventory = mock(InventoryClient.class);
            given(inventory.available("TECLADO")).willReturn(7);

            int available = inventory.available("TECLADO");
            inventory.reserve("TECLADO", 2);

            assertEquals(7, available);
            then(inventory).should().available("TECLADO");
            then(inventory).should().reserve("TECLADO", 2);
            then(inventory).shouldHaveNoMoreInteractions();
        }
    }

    @Nested
    @DisplayName("Métodos estáticos (mockStatic)")
    class StaticMethods {

        @Test
        @DisplayName("mockStatic sustituye un método estático solo dentro del bloque try-with-resources")
        void staticMock() {
            assertTrue(EmailValidator.isValid("real@mail.com"));

            try (MockedStatic<EmailValidator> mocked = mockStatic(EmailValidator.class)) {
                mocked.when(() -> EmailValidator.isValid("real@mail.com")).thenReturn(false);

                assertFalse(EmailValidator.isValid("real@mail.com"), "dentro del bloque está sustituido");
                mocked.verify(() -> EmailValidator.isValid("real@mail.com"));
            }

            assertTrue(EmailValidator.isValid("real@mail.com"), "al cerrar el bloque vuelve a ser el real");
        }
    }
}
