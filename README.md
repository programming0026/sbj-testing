# Pruebas unitarias con JUnit 5 y Mockito en Spring Boot 4

Proyecto de ejemplo para aprender a **probar código Java** con JUnit 5, Mockito y JaCoCo sobre una pequeña aplicación de pedidos hecha con **Spring Boot 4** y **Java 21**. Tiene 94 pruebas y una cobertura de líneas superior al 90 %.

## La aplicación que se prueba

`OrderService` crea un pedido coordinando varios colaboradores, lo que lo hace ideal para practicar con dobles de prueba:

```text
OrderController ──► OrderService ──► InventoryClient   (existencias y reservas)
                                 ├─► PricingService    (descuentos: lógica pura)
                                 ├─► PaymentGateway    (cobro)
                                 ├─► OrderRepository   (guardar)
                                 ├─► NotificationService (avisar al cliente)
                                 └─► IdGenerator y Clock (id y hora, inyectables para poder fijarlos)
```

Reglas de negocio: descuento por nivel del cliente (STANDARD 0 %, SILVER 5 %, GOLD 10 %) más 2 % si el subtotal es de 1000 o más; no se cobra si falta stock; si el cobro es rechazado no se guarda nada; si falla el aviso, el pedido se conserva.

## Qué técnica se practica en cada clase de prueba

| Clase de prueba | Qué enseña |
|-----------------|------------|
| `PricingServiceTest` | **JUnit 5 puro y pruebas parametrizadas**: `@CsvSource`, `@CsvFileSource` (archivo en `src/test/resources`), `@MethodSource`, `@EnumSource` (con `names`), `@Nested`, `@DisplayName`, `@Tag`, `assertAll` |
| `EmailValidatorTest` | `@ValueSource`, `@NullAndEmptySource` y nombres de caso personalizados |
| `ItemTest` | `assertThrows`, validación de un `record` y `@CsvSource` con `nullValues` |
| `OrderServiceTest` | **Mockito**: `@Mock`, `@Spy`, `@Captor`, `when/thenReturn`, `thenAnswer`, `doThrow` en métodos `void`, `verify` con `never()`/`times()`, `InOrder`, `verifyNoInteractions`, `doReturn` sobre un spy y reloj fijo (`Clock.fixed`) |
| `MockitoTechniquesTest` | Catálogo de técnicas: respuestas consecutivas, `thenThrow`, matchers (`anyString`, `eq`, `startsWith`, `argThat`), `doNothing`/`doAnswer`, `atLeast`/`atMost`/`timeout`, `ArgumentCaptor.getAllValues`, spy de una lista real, mock parcial (`CALLS_REAL_METHODS`), **BDDMockito** (`given/then`) y `mockStatic` |
| `TestDoublesTest` | Los **tipos de dobles de prueba escritos a mano**: dummy, stub, fake, spy y mock |
| `ReportWriterTest` | Archivos con `@TempDir` |
| `JUnitFeaturesTest` | Ciclo de vida (`@BeforeAll`, `@BeforeEach`...), `@TestMethodOrder`, `@RepeatedTest`, `@TestFactory` (pruebas dinámicas), `assumeTrue`, `assertTimeout`, `@Disabled` |
| `OrderControllerTest` | Capa web **sin levantar Spring**: `MockMvc` en modo standalone con el servicio simulado |
| `TestingApplicationTests` | Integración con el contexto de Spring y **`@MockitoBean`** (reemplaza a `@MockBean` en Spring Boot 3.4+) |

## Los tipos de dobles de prueba

| Doble | Para qué sirve | Ejemplo en el proyecto |
|-------|----------------|------------------------|
| **Dummy** | Rellenar un parámetro que no se usa | `IdGenerator` en `TestDoublesTest` |
| **Stub** | Devolver respuestas fijas | `StubInventory`, `ApprovingGateway` |
| **Fake** | Implementación real pero simplificada | `InMemoryOrderRepository` |
| **Spy** | Registrar cómo se le llamó (o vigilar un objeto real) | `RecordingNotifications`, `@Spy PricingService` |
| **Mock** | Verificar que se hicieron ciertas llamadas | `@Mock PaymentGateway` con `verify(...)` |

## Consejos

- **Un doble por colaborador externo** (red, base de datos, reloj, azar); la lógica pura se prueba sin dobles.
- Con spies usa `doReturn(x).when(spy).metodo()`: `when(spy.metodo())` ejecutaría el método real al configurarlo.
- Si usas un matcher en un argumento, **todos** los argumentos deben ser matchers (`eq(...)` para los valores fijos).
- `MockitoExtension` activa el **modo estricto**: un `when(...)` que no se usa hace fallar la prueba y evita código muerto.
- Inyecta por constructor y pasa `Clock`, generadores de ids, etc.: así las pruebas son deterministas.
- Prefiere pruebas unitarias rápidas y reserva `@SpringBootTest` (lento) para unas pocas pruebas de integración.

## Cobertura con JaCoCo

```bash
mvn verify
```

- El informe HTML queda en `target/site/jacoco/index.html`.
- `mvn verify` falla si la cobertura de líneas baja del **80 %** (regla en el `pom.xml`).

## Requisitos

- JDK 21 y Maven 3.9 o superior.

## Ejecutar

```bash
mvn test                              # todas las pruebas
mvn test -Dtest=OrderServiceTest      # una clase
mvn test -Dgroups=fast                # solo las pruebas etiquetadas con @Tag("fast")
mvn spring-boot:run                    # arranca la API en http://localhost:8080
```

Con la API en marcha:

```bash
curl -X POST http://localhost:8080/orders -H "Content-Type: application/json" \
  -d '{"customerEmail":"ana@mail.com","tier":"GOLD","items":[{"sku":"TECLADO","quantity":2,"unitPrice":500.00}]}'
```

El inventario de ejemplo tiene `TECLADO` (10), `RATON` (25) y `MONITOR` (3). Para generar el fat jar ejecutable: `mvn clean package` y `java -jar target/sbj-testing-0.0.1-SNAPSHOT.jar`.

## Estructura

```text
src/main/java/com/juniverse/testing/
├── model/ exception/ util/        # datos, errores y validación de correos
├── service/                       # OrderService, PricingService y los puertos
├── repository/                    # OrderRepository y su versión en memoria
├── infrastructure/                # implementaciones de ejemplo de los puertos
├── report/                        # ReportWriter (CSV)
└── web/                           # OrderController y manejo de errores
src/test/java/...                  # las pruebas descritas arriba
src/test/resources/discounts.csv   # datos para @CsvFileSource
```

## Licencia

Código de ejemplo de uso libre con fines educativos.
