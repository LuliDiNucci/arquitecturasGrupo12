# 06 — Plan de testing y validación general

## 1. Objetivo

Este documento reúne la estrategia de testing de los microservicios que componen TicketYa durante la Entrega 1:

- `msvc-eventosylugares`
- `msvc-reservas`
- `msvc-compras`
- `msvc-entradas`

El objetivo es verificar las reglas de negocio, la persistencia, la concurrencia, el comportamiento de los endpoints y las interacciones entre servicios.

Se distinguen las pruebas unitarias, las pruebas con bases de datos reales, las verificaciones manuales y las pruebas de integración entre microservicios.

**Importante:** la existencia de un caso de prueba implementado no significa necesariamente que se haya ejecutado en todos los entornos. Los resultados se identifican según la evidencia documentada por cada equipo.

## 2. Estrategia general

Se utilizan los siguientes niveles de prueba:

| Nivel | Objetivo | Herramientas |
| Unitarias | Verificar reglas de negocio y comportamientos individuales. | JUnit 5, Mockito |
| Integración con base de datos | Verificar persistencia, transacciones y concurrencia. | Spring Boot Test, MySQL |
| API y pruebas manuales | Verificar endpoints, respuestas HTTP y flujos de negocio. | Postman, PowerShell |
| Integración entre microservicios | Verificar la comunicación y coordinación entre servicios reales. | REST, Feign, servicios ejecutándose conjuntamente |

Las pruebas unitarias utilizan dependencias simuladas cuando corresponde. Esto permite verificar la lógica de cada servicio sin necesitar que todos los microservicios estén disponibles.

Las pruebas de integración entre servicios son necesarias para comprobar que los contratos, rutas, DTO y estados sean compatibles en el sistema completo.

## 3. Testing por microservicio

### 3.1. Eventos y Lugares

**Pruebas implementadas:**

Se cuenta con pruebas unitarias y de API que verifican:

- Publicación de eventos y validaciones de sectores, precios y asientos.
- Rechazo de configuraciones incompletas.
- Modificación de eventos publicados.
- Cancelación de eventos y prevención de avisos duplicados.
- Búsquedas y filtros por ciudad, participante, fechas y precio.
- Creación de sectores y validación de asientos.
- Respuestas HTTP ante solicitudes inválidas y conflictos.
- Notificación de cancelaciones hacia los otros tres microservicios, utilizando respuestas simuladas.

**Clases principales:** `EventoServiceTest`, `AsientoServiceTest`, `SectorServiceTest`, `EventoApiIntegrationTest` y `NotificadorCancelacionesTest`.

**Ejecución documentada:**

Desde la raíz del proyecto:

```bash
mvn clean test
```

Las pruebas de API utilizan H2 en memoria, separada de la base de datos local.

**Alcance y pendientes:**

- Las pruebas cubren el comportamiento interno de Eventos y Lugares.
- No representan pruebas de integración con los otros microservicios reales.
- Queda pendiente verificar la ejecución completa de la suite y documentar su resultado.
- La prueba de concurrencia para evitar la sobreventa corresponde principalmente a Reservas.

### 3.2. Reservas

**Pruebas unitarias implementadas:**

Se utiliza `ReservaServiceImplTest` para verificar:

- Rechazo de solicitudes incompletas.
- Rechazo de reservas para eventos cancelados.
- Imposibilidad de confirmar reservas vencidas.
- Cancelación de eventos y de reservas temporales activas.

**Prueba de concurrencia con MySQL:**

Se implementó `ConcurrenciaMySQLIT`, que envía diez solicitudes simultáneas para reservar el mismo asiento.

El resultado informado durante el desarrollo fue el esperado: **una sola solicitud consigue crear la reserva**, mientras que las restantes son rechazadas.

Esta prueba utiliza MySQL real y permite verificar el bloqueo pesimista y las transacciones para evitar que dos clientes ocupen el mismo lugar.

**Pruebas manuales informadas:**

- Crear y consultar reservas.
- Consultar disponibilidad.
- Confirmar y liberar reservas.
- Verificar el vencimiento lógico a los diez minutos.
- Cancelar eventos.
- Registrar devoluciones individuales.
- Comprobar idempotencia y respuestas ante errores.

**Ejecución:**

Desde `msvc-reservas`:

```powershell
mvn test
```

Para ejecutar específicamente la prueba de concurrencia:

```powershell
mvn "-Dtest=ConcurrenciaMySQLIT" test
```

Esta última requiere una base MySQL exclusiva para pruebas.

**Alcance y pendientes:**

- La prueba de concurrencia fue ejecutada con MySQL real.
- Queda pendiente probar varias instancias reales del microservicio conectadas simultáneamente.
- Las pruebas con simuladores no demuestran la integración completa con los demás servicios.
- La coordinación de pago, confirmación, emisión y devolución requiere pruebas conjuntas.
- La consulta de disponibilidad todavía no devuelve explícitamente la lista de asientos ocupados.

### 3.3. Compras

**Pruebas implementadas:**

Se identificaron trece métodos de prueba:

- Once pruebas unitarias con JUnit y Mockito.
- Dos pruebas de concurrencia con Spring Boot y MySQL.

Se cubren los siguientes escenarios:

- Cálculo del importe total y creación de detalles de compra.
- Prevención de compras duplicadas para una reserva.
- Confirmación de compras y emisión de entradas.
- Idempotencia de pagos.
- Prevención de cobros duplicados.
- Reutilización incorrecta de claves de idempotencia.
- Reintegros utilizando precios históricos.
- Reintegros parciales.
- Prevención de reintegros duplicados.
- Cancelación repetida de eventos.

**Clases principales:**

- `CompraServiceImplTest`
- `PagoServiceImplTest`
- `ReintegroServiceImplTest`
- `PagoConcurrenciaMySQLIT`

**Pruebas de concurrencia:**

Se verifican dos situaciones:

1. Dos solicitudes simultáneas para pagar la misma compra con la misma clave de idempotencia. Se espera un único pago y una sola invocación a la pasarela.
2. Dos compras diferentes que intentan utilizar la misma clave. Se espera que solamente una operación sea aceptada.

Estas pruebas utilizan MySQL para la persistencia y simulan las dependencias externas.

**Ejecución:**

Desde `msvc-compras`:

```bash
mvn test
```

Para ejecutar específicamente la prueba de concurrencia:

```bash
mvn -Dtest=PagoConcurrenciaMySQLIT test
```

La prueba de concurrencia requiere la base `ticketya_compras_test`. Debe ejecutarse exclusivamente contra una base de pruebas.

**Alcance y pendientes:**

- Existe cobertura de las principales reglas de compra, pago y reintegro.
- Falta una prueba específica del rechazo de pago y la liberación de la reserva.
- Quedan pendientes escenarios de fallos de comunicación, reservas vencidas y recuperación de operaciones.
- No se identificaron pruebas específicas de controladores con MockMvc ni pruebas de contrato HTTP con los demás microservicios.
- La ejecución efectiva de las pruebas debe respaldarse con los resultados de Maven.

### 3.4. Entradas

**Pruebas unitarias implementadas:**

Se verifican los principales comportamientos de emisión, consulta, invalidación y devolución de entradas.

Los escenarios incluyen:

- Rechazo de emisión para compras inválidas o no confirmadas.
- Emisión de una entrada por cada lugar de una reserva.
- Generación de códigos QR únicos.
- Prevención de entradas duplicadas ante solicitudes repetidas.
- Validación de que un cliente no pueda consultar una entrada perteneciente a otro.
- Invalidación de entradas cuando se cancela un evento.
- Prevención de solicitudes de devolución duplicadas.
- Validación del plazo de 72 horas para solicitar devoluciones.
- Aprobación y rechazo de solicitudes.
- Solicitud de liberación de lugares a Reservas y reintegros a Compras.
- Generación de imágenes QR PNG codificadas en Base64.

**Clases principales:**

- `EntradaServiceTest`
- `EntradaServiceImplTest`
- `DevolucionServiceImplTest`
- `QrCodeServiceImplTest`

También se incluye una prueba de inicialización del contexto de Spring mediante `@SpringBootTest`.

**Alcance y pendientes:**

- Las dependencias externas se simulan mediante mocks.
- No se cuenta con pruebas específicas de integración de los endpoints REST.
- No se identificaron pruebas de autenticación y autorización para todos los controladores.
- Falta verificar el reintento de operaciones externas después de aprobar una devolución.
- No se identificó una prueba específica del límite exacto de las 72 horas.
- La consulta de entradas por cliente no verifica actualmente la identidad del solicitante.

## 4. Pruebas de integración entre microservicios

Las pruebas individuales permiten verificar las reglas internas de cada servicio, pero no garantizan que el circuito completo de TicketYa funcione correctamente.

Por eso, se deben validar conjuntamente las siguientes integraciones:

| Flujo | Servicios involucrados | Validación |
| Consulta de eventos y sectores | Reservas → Eventos y Lugares | Obtener datos y precios correctos. |
| Creación de reserva | Reservas → Eventos y Lugares | Verificar disponibilidad y creación de la reserva temporal. |
| Inicio de compra | Compras → Reservas y Eventos | Validar reserva y obtener precios. |
| Pago aprobado | Compras → Reservas y Entradas | Confirmar reserva y emitir las entradas. |
| Pago rechazado | Compras → Reservas | Liberar la reserva sin generar entradas. |
| Devolución individual | Entradas → Reservas y Compras | Liberar el lugar y registrar el reintegro. |
| Cancelación de evento | Eventos y Lugares → Reservas, Entradas y Compras | Cancelar reservas, invalidar entradas y procesar reintegros. |
| Acceso a través del Gateway | Gateway → Microservicios | Verificar el enrutamiento y las restricciones de acceso. |

**Estado general:** los clientes y endpoints necesarios están desarrollados en los respectivos microservicios, pero las pruebas unitarias con mocks no reemplazan la validación conjunta contra los servicios reales.

Por lo tanto, los flujos completos deben considerarse pendientes de validación integrada cuando no se dispone de evidencia de una ejecución conjunta satisfactoria.

## 5. Prueba crítica de concurrencia y sobreventa

Uno de los requisitos centrales de TicketYa es evitar que varios usuarios consigan reservar o comprar simultáneamente el mismo asiento o el último lugar disponible.

Para verificarlo se debe realizar una prueba con múltiples solicitudes concurrentes.

**Escenario propuesto:**

1. Crear un evento con un único asiento disponible.
2. Iniciar N solicitudes simultáneas para reservar ese mismo asiento.
3. Verificar que solamente una solicitud consiga reservarlo.
4. Comprobar que las restantes sean rechazadas.
5. Confirmar que no existan reservas duplicadas.
6. Verificar que no se generen cobros ni entradas duplicadas.

**Resultado esperado:** exactamente una reserva exitosa y ninguna sobreventa.

Reservas cuenta con una prueba ejecutada con diez solicitudes concurrentes sobre el mismo asiento. Compras también cuenta con pruebas específicas para evitar cobros duplicados.

Sin embargo, la validación conjunta del circuito de reserva y compra, incluyendo varias instancias de los servicios, debe documentarse por separado. No se debe considerar demostrada únicamente por las pruebas individuales.

## 6. Pruebas pendientes prioritarias

Para completar la validación de la Entrega 1 se priorizan los siguientes escenarios:

| Prioridad | Prueba pendiente | Resultado esperado |
| Alta | Compra con pago aprobado | Reserva confirmada y entradas emitidas correctamente. |
| Alta | Compra con pago rechazado | Reserva liberada y ausencia de entradas emitidas. |
| Alta | Solicitudes simultáneas por el último lugar | Una sola reserva exitosa, sin sobreventa. |
| Alta | Cancelación de evento | Reservas canceladas, entradas invalidadas y reintegros correspondientes. |
| Alta | Devolución individual aprobada | Lugar liberado y reintegro registrado sin duplicaciones. |
| Media | Fallo de comunicación entre servicios | Operación recuperable, sin estados inconsistentes. |
| Media | Reintentos de notificaciones | Sin duplicar efectos de una misma operación. |
| Media | Acceso a través del Gateway | Rutas y restricciones de acceso correctas. |

Estos escenarios representan validaciones pendientes o ampliaciones recomendadas, no resultados de pruebas ya aprobadas.

## 7. Evidencias para la entrega

Para respaldar las pruebas realizadas se recomienda conservar:

- Resultado real de la ejecución de Maven en cada microservicio.
- Cantidad de pruebas ejecutadas, aprobadas y fallidas.
- Capturas o exportaciones de Postman con solicitudes exitosas y errores HTTP.
- Evidencia de las pruebas de concurrencia con MySQL.
- Registros de solicitudes y respuestas entre microservicios.
- Evidencia de los flujos completos de compra, devolución y cancelación.
- Versión o commit del código utilizado para cada ejecución.

Los escenarios pendientes deben permanecer identificados como tales hasta contar con evidencia de su ejecución satisfactoria.

## 8. Conclusión

Los cuatro microservicios cuentan con pruebas orientadas a verificar sus principales reglas de negocio.

Eventos y Lugares cubre la publicación, búsqueda y cancelación de eventos. Reservas verifica la disponibilidad, el vencimiento lógico y la concurrencia sobre un mismo asiento. Compras cubre la creación, el pago, la confirmación y los reintegros. Entradas verifica la emisión, invalidación, devolución y generación de códigos QR.

Se destacan las pruebas de concurrencia con MySQL implementadas en Reservas y Compras, que permiten comprobar mecanismos para evitar reservas y cobros duplicados.

Como validación final del sistema, es necesario completar y documentar las pruebas integradas entre microservicios, especialmente los circuitos de compra, pago, emisión, devolución y cancelación de eventos.

De esta manera, se diferencia la cobertura individual de cada servicio de la validación del funcionamiento conjunto de TicketYa.