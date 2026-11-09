# 06 — Plan de testing y validación general

## 1. Descripción general

Este documento describe la estrategia de testing utilizada para verificar el funcionamiento de TicketYa y sus principales reglas de negocio.

El sistema está compuesto por cuatro microservicios funcionales:

- `msvc-eventosylugares`
- `msvc-reservas`
- `msvc-compras`
- `msvc-entradas`

Las pruebas se orientan a comprobar el comportamiento de los servicios, las validaciones de negocio, la persistencia, el control de concurrencia, los contratos REST y los principales circuitos funcionales.

La estrategia incluye pruebas unitarias, pruebas de integración con bases de datos, pruebas de API y verificaciones manuales mediante Postman.

## 2. Estrategia de testing

### 2.1. Niveles de prueba

| Nivel | Objetivo | Herramientas |
|---|---|---|
| Pruebas unitarias | Verificar reglas de negocio y comportamiento de los servicios. | JUnit 5, Mockito |
| Pruebas de integración con base de datos | Verificar persistencia, transacciones y concurrencia. | Spring Boot Test, MySQL, H2 |
| Pruebas de API REST | Verificar endpoints, parámetros, estados y respuestas HTTP. | Spring Boot Test, Postman |
| Pruebas manuales | Comprobar flujos funcionales y respuestas del sistema. | Postman, PowerShell |
| Pruebas entre microservicios | Evaluar la comunicación y coordinación de los servicios durante los circuitos de negocio. | REST, OpenFeign, RestClient |

Las pruebas unitarias utilizan dependencias simuladas cuando corresponde, lo que permite verificar las reglas de negocio de cada servicio sin depender de la disponibilidad de los demás microservicios.

Las pruebas de integración con base de datos comprueban el comportamiento de las operaciones transaccionales y de persistencia.

Las pruebas manuales permiten observar el comportamiento de los endpoints y las respuestas del sistema durante su ejecución local.

## 3. Pruebas de Eventos y Lugares

El microservicio `msvc-eventosylugares` cuenta con pruebas orientadas a verificar la administración de eventos, lugares, sectores, asientos, precios y cancelaciones.

### 3.1. Clases de prueba

| Clase | Funcionalidad |
|---|---|
| `EventoServiceTest` | Reglas de publicación, modificación, cancelación y búsqueda de eventos. |
| `AsientoServiceTest` | Validaciones de asientos, capacidad y duplicaciones. |
| `SectorServiceTest` | Creación y validación de sectores. |
| `EventoApiIntegrationTest` | Pruebas de endpoints REST y respuestas HTTP. |
| `NotificadorCancelacionesTest` | Procesamiento y reintento de notificaciones de cancelación. |

### 3.2. Escenarios verificados

Las pruebas contemplan los siguientes comportamientos:

- Publicación de eventos con configuración completa.
- Rechazo de publicaciones sin sectores, precios o asientos necesarios.
- Validación de modificaciones según el estado del evento.
- Cancelación de eventos y prevención de notificaciones duplicadas.
- Búsquedas por ciudad, participante, fechas y precios.
- Ordenamiento por el menor precio configurado de cada evento.
- Validación de sectores y asientos duplicados.
- Control de capacidad de sectores numerados.
- Respuestas HTTP ante solicitudes inválidas.
- Procesamiento de notificaciones de cancelación con dependencias simuladas.

Las pruebas de API utilizan H2 en memoria, mediante una configuración independiente de la base utilizada durante la ejecución local.

### 3.3. Ejecución

Desde el directorio `msvc-eventosylugares`:

```powershell
mvn clean test
```

Este comando ejecuta las pruebas automatizadas reconocidas por Maven.

## 4. Pruebas de Reservas

El microservicio `msvc-reservas` cuenta con pruebas destinadas a verificar el ciclo de vida de las reservas, la ocupación de lugares, el vencimiento, la idempotencia y la concurrencia.

### 4.1. Pruebas unitarias

La clase `ReservaServiceImplTest` verifica escenarios como:

| Caso de prueba | Comportamiento verificado |
|---|---|
| `rechazaSolicitudIncompletaSinGuardar` | Las solicitudes incompletas se rechazan sin persistir reservas. |
| `rechazaReservaSiEventoCanceladoLocalmente` | No se permite reservar un evento cancelado. |
| `noConfirmaReservaVencida` | Una reserva vencida no puede confirmarse. |
| `cancelacionMarcaElEventoYCancelaActivas` | La cancelación registra el evento como cancelado y actualiza las reservas temporales activas. |

### 4.2. Prueba de concurrencia

La clase `ConcurrenciaMySQLIT` permite comprobar el control de concurrencia sobre un mismo asiento.

El escenario utiliza una base de datos MySQL y ejecuta diez solicitudes concurrentes que intentan reservar el mismo lugar.

La prueba verifica que:

1. Solamente una solicitud consiga crear la reserva.
2. Las demás solicitudes no puedan reservar el asiento ocupado.
3. No se registren asignaciones incompatibles.
4. El bloqueo pesimista y las transacciones mantengan la consistencia de la ocupación.

El escenario fue ejecutado con MySQL durante el desarrollo y obtuvo una única reserva exitosa.

### 4.3. Pruebas manuales

Las pruebas manuales de Reservas contemplan:

- Creación y consulta de reservas temporales.
- Consulta de disponibilidad en sectores numerados y no numerados.
- Consulta de asientos disponibles y ocupados.
- Confirmación de reservas activas y vigentes.
- Liberación de reservas.
- Vencimiento lógico de reservas después de 10 minutos.
- Cancelación de reservas temporales por cancelación de eventos.
- Devolución individual de lugares.
- Idempotencia de solicitudes repetidas.
- Respuestas HTTP ante errores de validación y conflictos de disponibilidad.

El vencimiento lógico permite que una reserva activa cuyo plazo terminó deje de bloquear lugares, aunque su estado persistido todavía sea `ACTIVA`.

### 4.4. Ejecución

Desde el directorio `msvc-reservas`:

```powershell
mvn test
```

Para ejecutar específicamente la prueba de concurrencia:

```powershell
mvn "-Dtest=ConcurrenciaMySQLIT" test
```

La prueba de concurrencia utiliza una base MySQL independiente, denominada `ticketya_reservas_test`, cuya configuración se encuentra detallada en la documentación de testing de Reservas.

## 5. Pruebas de Compras y Pagos

El microservicio `msvc-compras` dispone de pruebas unitarias y pruebas de concurrencia destinadas a verificar las compras, el procesamiento de pagos y los reintegros.

### 5.1. Clases de prueba

| Clase | Funcionalidad |
|---|---|
| `CompraServiceImplTest` | Creación, validación y confirmación de compras. |
| `PagoServiceImplTest` | Procesamiento de pagos e idempotencia. |
| `ReintegroServiceImplTest` | Gestión de reintegros individuales y por cancelación. |
| `PagoConcurrenciaMySQLIT` | Control de concurrencia en operaciones de pago. |

La suite documentada incluye once pruebas unitarias y dos escenarios de concurrencia con MySQL.

### 5.2. Escenarios de negocio

Las pruebas verifican:

- Cálculo del importe total de las compras.
- Creación de detalles con precios históricos.
- Prevención de compras duplicadas para una misma reserva.
- Confirmación de compras y coordinación de emisión de entradas.
- Idempotencia en el procesamiento de pagos.
- Prevención de cobros duplicados.
- Rechazo de reutilización incompatible de claves de idempotencia.
- Cálculo de reintegros utilizando importes históricos.
- Procesamiento de reintegros parciales.
- Prevención de reintegros duplicados.
- Cancelaciones repetidas de eventos.

### 5.3. Pruebas de concurrencia

La clase `PagoConcurrenciaMySQLIT` contempla dos escenarios principales.

**Escenario 1: pago simultáneo de una misma compra**

Dos solicitudes intentan procesar simultáneamente el pago de una misma compra utilizando la misma clave de idempotencia.

Se verifica que se registre un único pago y que la pasarela simulada sea invocada una sola vez.

**Escenario 2: reutilización de una clave de idempotencia**

Dos compras diferentes intentan procesar pagos utilizando la misma clave.

Se verifica que solamente una operación pueda utilizarla correctamente, evitando registros incompatibles.

Las pruebas utilizan MySQL para comprobar la persistencia y el control de concurrencia, mientras que las dependencias externas se simulan durante la ejecución.

### 5.4. Ejecución

Desde el directorio `msvc-compras`:

```powershell
mvn test
```

Para ejecutar específicamente la prueba de concurrencia:

```powershell
mvn "-Dtest=PagoConcurrenciaMySQLIT" test
```

La prueba de concurrencia requiere una base MySQL exclusiva para testing, denominada `ticketya_compras_test`.

## 6. Pruebas de Entradas

El microservicio `msvc-entradas` incluye pruebas unitarias para verificar la emisión, consulta, devolución e invalidación de entradas digitales.

### 6.1. Clases de prueba

| Clase | Funcionalidad |
|---|---|
| `EntradaServiceTest` | Validaciones de emisión y consulta de entradas. |
| `EntradaServiceImplTest` | Emisión idempotente e invalidación de entradas. |
| `DevolucionServiceImplTest` | Solicitudes, aprobación y rechazo de devoluciones. |
| `QrCodeServiceImplTest` | Generación de códigos QR. |

También se incluye una prueba de inicialización del contexto de Spring Boot mediante `@SpringBootTest`.

### 6.2. Escenarios de negocio

Las pruebas contemplan:

- Rechazo de emisión para compras inválidas o no confirmadas.
- Emisión de una entrada por cada lugar adquirido.
- Generación de códigos QR únicos.
- Prevención de entradas duplicadas ante solicitudes repetidas.
- Validación de titularidad durante la consulta individual de entradas.
- Invalidación de entradas correspondientes a eventos cancelados.
- Prevención de solicitudes de devolución pendientes duplicadas.
- Validación de la anticipación de 72 horas para solicitar devoluciones.
- Aprobación y rechazo de solicitudes.
- Solicitud de devolución de lugares a Reservas.
- Solicitud de reintegros a Compras.
- Generación de imágenes QR en formato PNG codificadas en Base64.

### 6.3. Ejecución

Desde el directorio `msvc-entradas`:

```powershell
mvn test
```

Las pruebas unitarias utilizan mocks para simular repositorios y dependencias externas, permitiendo evaluar las reglas de negocio de manera aislada.

## 7. Pruebas de integración funcional

Los circuitos principales de TicketYa requieren la coordinación de varios microservicios.

La validación funcional del sistema contempla los siguientes escenarios:

| Flujo | Servicios involucrados | Comportamiento esperado |
|---|---|---|
| Consulta de eventos | Gateway y Eventos y Lugares | Obtener eventos, sectores y precios. |
| Consulta de disponibilidad | Reservas y Eventos y Lugares | Informar los lugares disponibles y ocupados. |
| Creación de reserva | Reservas y Eventos y Lugares | Validar el evento y registrar una reserva temporal. |
| Inicio de compra | Compras, Reservas y Eventos y Lugares | Validar la reserva y calcular el importe. |
| Pago aprobado | Compras, Reservas y Entradas | Confirmar la reserva y emitir entradas digitales. |
| Pago rechazado | Compras y Reservas | Liberar la reserva sin emitir entradas. |
| Devolución individual | Entradas, Reservas y Compras | Invalidar la entrada, liberar el lugar y gestionar el reintegro. |
| Cancelación de evento | Eventos y Lugares, Reservas, Entradas y Compras | Cancelar reservas, invalidar entradas y procesar reintegros. |
| Acceso mediante Gateway | Gateway y microservicios | Enrutar las solicitudes hacia los servicios correspondientes. |

Estos escenarios constituyen el circuito de validación funcional del sistema completo.

Las pruebas individuales con dependencias simuladas verifican las reglas de cada componente. La ejecución conjunta de los servicios permite comprobar, adicionalmente, la compatibilidad de sus contratos HTTP y la coordinación de sus operaciones.

## 8. Validación de concurrencia y prevención de sobreventa

Uno de los requisitos principales de TicketYa es evitar que varios usuarios puedan reservar o adquirir simultáneamente el mismo lugar.

### 8.1. Reserva simultánea de un asiento

El escenario de concurrencia consiste en:

1. Preparar un evento con un único asiento disponible.
2. Ejecutar diez solicitudes simultáneas de reserva sobre ese asiento.
3. Comprobar que solamente una solicitud consiga reservarlo.
4. Verificar que las restantes no puedan registrar una reserva incompatible.
5. Consultar la persistencia para confirmar que el asiento no tenga asignaciones activas duplicadas.

**Resultado esperado:** una única reserva exitosa, sin doble asignación del asiento.

Este escenario se encuentra representado en la prueba `ConcurrenciaMySQLIT` de Reservas.

### 8.2. Capacidad de sectores no numerados

En los sectores no numerados, la disponibilidad se determina utilizando la capacidad máxima del sector y la cantidad de lugares ocupados.

El escenario de validación consiste en ejecutar solicitudes concurrentes sobre los últimos lugares disponibles.

El control transaccional debe impedir que la cantidad de reservas confirmables supere la capacidad del sector.

### 8.3. Prevención de cobros duplicados

Compras incorpora mecanismos de idempotencia y control de concurrencia durante el procesamiento de pagos.

Las pruebas contemplan solicitudes simultáneas con una misma clave de idempotencia, verificando que no se registren cobros duplicados.

La prevención de reservas incompatibles corresponde a Reservas, mientras que la prevención de pagos duplicados corresponde a Compras.

## 9. Pruebas manuales con Postman

El repositorio incluye colecciones Postman para ejecutar solicitudes HTTP y comprobar los principales circuitos de TicketYa.

### 9.1. Preparación

Antes de comenzar las pruebas:

1. Iniciar las bases de datos requeridas.
2. Levantar Eventos y Lugares en el puerto `8081`.
3. Levantar Reservas en el puerto `8080`.
4. Levantar Compras y Pagos en el puerto `8082`.
5. Levantar Entradas en el puerto `8083`.
6. Levantar el API Gateway en el puerto `8084`.
7. Importar las colecciones Postman del repositorio.
8. Configurar las variables de entorno necesarias.

### 9.2. Circuito de prueba

El circuito funcional comprende:

1. Crear los organizadores, participantes y lugares necesarios.
2. Configurar sectores y asientos.
3. Crear un evento y asignar sus precios.
4. Publicar el evento.
5. Consultar los eventos publicados.
6. Consultar disponibilidad de lugares.
7. Crear una reserva temporal.
8. Iniciar una compra.
9. Procesar el pago.
10. Verificar la confirmación de la reserva y la emisión de entradas.
11. Consultar las entradas y sus códigos QR.
12. Ejecutar una solicitud de devolución.
13. Aprobar o rechazar la devolución.
14. Verificar la actualización de la entrada, la disponibilidad y los reintegros.
15. Ejecutar una cancelación de evento y consultar el procesamiento de sus notificaciones.

### 9.3. Pruebas de error

Las colecciones permiten comprobar respuestas HTTP ante operaciones inválidas, entre ellas:

| Escenario | Resultado esperado |
|---|---|
| Datos de solicitud incompletos. | `400 Bad Request` |
| Consulta de un recurso inexistente. | `404 Not Found` |
| Publicación de un evento con configuración incompleta. | `409 Conflict` |
| Intento de reservar un asiento ocupado. | `409 Conflict` |
| Intento de confirmar una reserva vencida. | `409 Conflict` |
| Solicitud de devolución fuera del plazo permitido. | Rechazo de la operación. |
| Intento de publicar un evento cancelado. | `409 Conflict` |
| Dependencia externa no disponible. | Respuesta de error según el contrato del servicio. |

Las respuestas obtenidas permiten verificar que las reglas de negocio se apliquen correctamente y que los errores sean comunicados mediante los códigos HTTP correspondientes.

## 10. Registro de resultados

Los resultados de las pruebas pueden documentarse utilizando los reportes y registros generados por las herramientas de ejecución.

### 10.1. Pruebas automatizadas

Maven genera reportes de ejecución que permiten consultar:

- Cantidad de pruebas ejecutadas.
- Pruebas aprobadas.
- Pruebas fallidas.
- Errores de ejecución.
- Tiempo utilizado durante las pruebas.

Los reportes se encuentran en los directorios correspondientes de `target/` según la configuración de cada proyecto.

### 10.2. Pruebas manuales

Las verificaciones mediante Postman y PowerShell permiten registrar:

- Método y ruta utilizados.
- Datos enviados en la solicitud.
- Código HTTP obtenido.
- Cuerpo de respuesta.
- Estado de los recursos antes y después de la operación.

### 10.3. Concurrencia

Las pruebas con MySQL permiten comprobar el resultado de las solicitudes simultáneas y la consistencia de los registros persistidos.

La documentación específica de Reservas y Compras contiene la configuración y los comandos necesarios para ejecutar estos escenarios.

## 11. Conclusión

La estrategia de testing de TicketYa contempla las principales reglas de negocio de sus cuatro microservicios funcionales.

Eventos y Lugares dispone de pruebas para publicación, búsqueda, configuración y cancelación de eventos.

Reservas incorpora validaciones de disponibilidad, vencimiento lógico, confirmación, liberación e idempotencia, además de una prueba de concurrencia con MySQL.

Compras y Pagos incluye pruebas relacionadas con importes, confirmaciones, idempotencia de pagos, concurrencia y reintegros.

Entradas cuenta con pruebas para emisión, consulta, invalidación, solicitudes de devolución y generación de códigos QR.

Las pruebas automatizadas de cada servicio y los procedimientos de validación funcional permiten evaluar tanto las operaciones individuales como los principales circuitos que componen el proceso de venta y gestión de entradas.

La documentación de testing de cada microservicio complementa este plan con sus clases de prueba, escenarios y procedimientos de ejecución.