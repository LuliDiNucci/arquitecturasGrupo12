# 05 — Contratos de integración de TicketYa

## 1. Descripción general

Este documento reúne los principales contratos REST utilizados por los microservicios de TicketYa y describe las comunicaciones necesarias para completar los procesos de reserva, compra, emisión de entradas, devolución y cancelación.

Se distinguen dos tipos de operaciones:

- **Endpoints propios:** operaciones expuestas mediante los controladores REST de cada microservicio.
- **Contratos de integración:** solicitudes HTTP realizadas entre microservicios mediante OpenFeign o RestClient.

Las rutas se expresan sin incluir el host ni el puerto, excepto cuando se especifica la configuración local.

Los datos se intercambian principalmente en formato JSON, utilizando DTO que permiten comunicar información sin compartir directamente las entidades persistentes de cada servicio.

## 2. Endpoints REST por microservicio

### 2.1. Eventos y Lugares

**URL base:** `http://localhost:8081`

El microservicio Eventos y Lugares administra lugares, sectores, asientos, organizadores, participantes, eventos y precios. También coordina las notificaciones de cancelación.

| Método | Ruta | Operación |
|---|---|---|
| POST | `/lugares` | Crear un lugar. |
| GET | `/lugares` | Listar lugares. |
| GET | `/lugares/{id}` | Consultar un lugar. |
| POST | `/lugares/{id}/sectores` | Crear un sector para un lugar. |
| GET | `/lugares/{id}/sectores` | Consultar los sectores de un lugar. |
| GET | `/sectores/{id}` | Consultar un sector por su identificador. |
| POST | `/sectores/{id}/asientos` | Registrar asientos de un sector. |
| GET | `/sectores/{id}/asientos` | Consultar los asientos de un sector. |
| GET | `/asientos/{id}` | Consultar un asiento por su identificador. |
| POST | `/organizadores` | Registrar un organizador. |
| GET | `/organizadores` | Listar organizadores. |
| POST | `/participantes` | Registrar un participante. |
| GET | `/participantes` | Listar participantes. |
| POST | `/eventos` | Crear un evento. |
| GET | `/eventos` | Obtener el listado general de eventos. |
| GET | `/eventos/publicados` | Consultar eventos publicados. |
| GET | `/eventos/buscar` | Buscar eventos utilizando filtros. |
| GET | `/eventos/{id}` | Consultar un evento. |
| PUT | `/eventos/{id}` | Modificar un evento. |
| PUT | `/eventos/{id}/precios` | Configurar los precios de los sectores. |
| PATCH | `/eventos/{id}/publicar` | Publicar un evento. |
| PATCH | `/eventos/{id}/suspender` | Suspender un evento. |
| PATCH | `/eventos/{id}/cancelar` | Cancelar un evento. |
| PATCH | `/eventos/{id}/agotar` | Marcar un evento como agotado. |
| PATCH | `/eventos/{id}/reabrir` | Reabrir un evento. |
| PATCH | `/eventos/{id}/finalizar` | Finalizar un evento. |
| GET | `/eventos/{id}/sectores` | Consultar sectores, asientos y precios de un evento. |
| GET | `/eventos/{id}/habilitado` | Consultar si un evento está habilitado para recibir reservas. |
| GET | `/eventos/{id}/notificaciones-cancelacion` | Consultar el procesamiento de las notificaciones de cancelación. |

La búsqueda pública se realiza mediante `GET /eventos/buscar`, que por defecto incluye eventos en estado `PUBLICADO` y `AGOTADO`.

La búsqueda admite filtros por tipo de evento, organizador, participante, ciudad, fechas y texto, además de distintos criterios de ordenamiento.

El endpoint `GET /eventos` proporciona el listado general de eventos para uso operativo.

### 2.2. Reservas

**URL base:** `http://localhost:8080`

El microservicio Reservas administra la disponibilidad de lugares, las reservas temporales y confirmadas y las operaciones de liberación y cancelación.

| Método | Ruta | Operación |
|---|---|---|
| GET | `/disponibilidad` | Consultar disponibilidad de un evento y sector. |
| POST | `/reservas` | Crear una reserva temporal. |
| GET | `/reservas/{id}` | Consultar una reserva. |
| POST | `/reservas/{id}/confirmar` | Confirmar una reserva. |
| POST | `/reservas/{id}/liberar` | Liberar una reserva. |
| POST | `/reservas/{id}/devolver-entrada` | Liberar el lugar correspondiente a una entrada devuelta. |
| POST | `/reservas/evento/{eventoId}/cancelar` | Cancelar las reservas temporales de un evento e impedir nuevas reservas. |
| POST | `/reservas/evento/{eventoId}/habilitar` | Preparar la habilitación de reservas para un evento. |

Las reservas temporales tienen una vigencia de 10 minutos.

La disponibilidad considera las reservas confirmadas y las reservas activas que todavía no vencieron. Para sectores numerados, la consulta informa los asientos disponibles y ocupados.

Reservas utiliza transacciones y bloqueo pesimista para evitar que dos solicitudes simultáneas ocupen el mismo lugar o superen la capacidad de un sector.

Las operaciones de habilitación y cancelación por evento forman parte de la integración con Eventos y Lugares.

### 2.3. Compras y Pagos

**URL base:** `http://localhost:8082`

El microservicio Compras administra el inicio de compras, el procesamiento de pagos, las confirmaciones, los reintegros y las operaciones de compensación.

| Método | Ruta | Operación |
|---|---|---|
| POST | `/compras` | Iniciar una compra. |
| GET | `/compras/{id}` | Consultar una compra. |
| POST | `/compras/{id}/confirmar` | Confirmar una compra. |
| GET | `/compras/{id}/confirmacion` | Consultar el estado de confirmación. |
| POST | `/compras/{id}/pagar` | Procesar el pago. |
| POST | `/compras/{id}/reintentar-liberacion` | Reintentar la liberación de una reserva. |
| POST | `/compras/reintegros` | Procesar un reintegro solicitado por Entradas. |
| GET | `/compras/{id}/reintegros` | Consultar los reintegros de una compra. |
| POST | `/compras/evento/{eventoId}/cancelar` | Procesar las compras afectadas por la cancelación de un evento. |
| POST | `/compras/{id}/compensar` | Ejecutar o reintentar la compensación de una compra. |

El procesamiento de pagos utiliza una pasarela simulada.

Compras conserva los precios históricos de los artículos y utiliza mecanismos de idempotencia para evitar cobros y reintegros duplicados.

Una compra se considera `CONFIRMADA` cuando se completan correctamente la confirmación de la reserva y la emisión de las entradas.

Las operaciones de recuperación y compensación permiten administrar fallas parciales en las comunicaciones con otros microservicios.

### 2.4. Entradas

**URL base:** `http://localhost:8083`

El microservicio Entradas administra la emisión de tickets digitales, la generación de códigos QR y las solicitudes de devolución.

| Método | Ruta | Operación |
|---|---|---|
| POST | `/entradas/emitir` | Emitir entradas para una compra. |
| GET | `/entradas/{id}` | Consultar una entrada individual. |
| GET | `/entradas/cliente/{clienteId}` | Consultar las entradas de un cliente. |
| POST | `/entradas/evento/{id}/invalidar` | Invalidar las entradas de un evento cancelado. |
| POST | `/devoluciones` | Solicitar la devolución de una entrada. |
| GET | `/devoluciones/pendientes` | Consultar las solicitudes de devolución pendientes. |
| PATCH | `/devoluciones/{id}/aprobar` | Aprobar una devolución. |
| PATCH | `/devoluciones/{id}/rechazar` | Rechazar una devolución. |

La consulta individual de una entrada requiere el encabezado `X-Cliente-Id`, utilizado para comprobar que la entrada pertenece al cliente indicado.

La consulta por cliente recibe el identificador mediante el parámetro de ruta `clienteId`.

La emisión es idempotente: si se solicita nuevamente emitir las entradas de una compra que ya tiene tickets registrados, se devuelven las entradas existentes sin generar duplicados.

Las solicitudes de devolución se encuentran sujetas a las reglas de estado y anticipación definidas por el servicio.

### 2.5. API Gateway

**URL base:** `http://localhost:8084`

El API Gateway constituye el punto de entrada para las solicitudes externas.

Recibe las peticiones de los clientes y las dirige al microservicio correspondiente según sus rutas configuradas.

El Gateway no administra entidades de negocio ni reemplaza las validaciones de los microservicios.

## 3. Contratos de comunicación entre microservicios

Esta sección describe las solicitudes HTTP que realizan los servicios para consultar información o coordinar operaciones.

### 3.1. Reservas → Eventos y Lugares

**Cliente:** `EventoClient` — OpenFeign.

| Método | Ruta | Finalidad |
|---|---|---|
| GET | `/eventos/{id}` | Consultar información y estado de un evento. |
| GET | `/sectores/{id}` | Consultar un sector. |
| GET | `/asientos/{id}` | Consultar un asiento. |
| GET | `/sectores/{id}/asientos` | Obtener los asientos de un sector. |
| GET | `/eventos/{eventoId}/sectores/{sectorId}/precio` | Consultar el precio de un sector para un evento. |

Reservas utiliza esta información para validar el evento, la pertenencia de sectores y asientos y los datos necesarios para administrar la ocupación.

La disponibilidad efectiva de los lugares permanece bajo responsabilidad de Reservas.

### 3.2. Compras → Reservas

**Cliente:** `ReservaClient` — OpenFeign.

| Método | Ruta | Finalidad |
|---|---|---|
| GET | `/reservas/{id}` | Consultar y validar una reserva. |
| POST | `/reservas/{id}/confirmar` | Confirmar la reserva después de un pago aprobado. |
| POST | `/reservas/{id}/liberar` | Liberar la reserva cuando el pago fue rechazado. |

Antes de continuar con una compra, se verifica que la reserva exista, corresponda al cliente y se encuentre en un estado compatible con la operación.

La confirmación de una reserva requiere que todavía se encuentre activa y vigente.

### 3.3. Compras → Eventos y Lugares

**Cliente:** `EventoClient` — OpenFeign.

| Método | Ruta | Finalidad |
|---|---|---|
| GET | `/eventos/{id}` | Consultar información y estado del evento. |
| GET | `/eventos/{eventoId}/sectores/{sectorId}/precio` | Obtener el precio del sector para el evento. |

Compras consulta estos datos para validar el evento y calcular el importe de la operación.

Los precios utilizados se conservan como valores históricos en los detalles de compra.

### 3.4. Compras → Entradas

**Cliente:** `EntradaClient` — OpenFeign.

| Método | Ruta | Finalidad |
|---|---|---|
| POST | `/entradas/emitir` | Solicitar la emisión de entradas de una compra. |
| POST | `/entradas/evento/{eventoId}/invalidar` | Solicitar la invalidación de entradas de un evento. |
| GET | `/entradas/cliente/{clienteId}` | Consultar las entradas asociadas a un cliente. |
| GET | `/entradas/{id}` | Consultar una entrada individual utilizando `X-Cliente-Id`. |

La emisión se solicita cuando el pago fue aprobado y la reserva se encuentra confirmada.

Durante las cancelaciones, Compras puede solicitar la invalidación de las entradas antes de procesar los reintegros.

### 3.5. Entradas → Compras

Entradas utiliza dos mecanismos de comunicación con Compras.

| Cliente | Método | Ruta | Finalidad |
|---|---|---|---|
| `CompraClient` — RestClient | GET | `/compras/{id}/confirmacion` | Consultar la confirmación de la compra y verificar el pago. |
| `CompraFeignClient` — OpenFeign | POST | `/compras/reintegros` | Solicitar el reintegro correspondiente a una entrada devuelta. |

La solicitud de reintegro contiene los datos de la entrada y el motivo de la devolución.

Compras valida la operación, identifica el importe correspondiente y administra el procesamiento del reintegro.

### 3.6. Entradas → Reservas

Entradas utiliza dos mecanismos de comunicación con Reservas.

| Cliente | Método | Ruta | Finalidad |
|---|---|---|---|
| `ReservaClient` — RestClient | GET | `/reservas/{id}` | Consultar la información de una reserva. |
| `ReservaFeignClient` — OpenFeign | POST | `/reservas/{id}/devolver-entrada` | Liberar el lugar de una entrada cuya devolución fue aprobada. |

La solicitud de devolución individual incluye el identificador de la entrada, el sector y el asiento cuando corresponde.

Reservas registra el identificador de la entrada procesada para evitar una segunda liberación ante reintentos.

### 3.7. Entradas → Eventos y Lugares

**Cliente:** `EventoFeignClient` — OpenFeign.

| Método | Ruta | Finalidad |
|---|---|---|
| GET | `/eventos/{id}` | Consultar el estado y la fecha del evento. |

La consulta permite comprobar las condiciones necesarias para solicitar una devolución y verificar el estado del evento durante una invalidación.

Las devoluciones individuales requieren una anticipación mínima de 72 horas.

### 3.8. Eventos y Lugares → Reservas

| Método | Ruta | Finalidad |
|---|---|---|
| POST | `/reservas/evento/{id}/cancelar` | Notificar la cancelación de un evento. |
| POST | `/reservas/evento/{id}/habilitar` | Habilitar las reservas de un evento. |

La habilitación prepara el registro local utilizado para controlar las reservas del evento.

La cancelación registra el estado cancelado localmente, cancela las reservas temporales activas e impide nuevas reservas.

### 3.9. Eventos y Lugares → Entradas

| Método | Ruta | Finalidad |
|---|---|---|
| POST | `/entradas/evento/{id}/invalidar` | Solicitar la invalidación de las entradas de un evento cancelado. |

Entradas verifica el estado del evento y actualiza a `INVALIDADA` las entradas que se encuentran en estado `EMITIDA`.

La invalidación de entradas no realiza por sí misma los reintegros ni la cancelación de reservas, ya que esas operaciones corresponden a otros microservicios.

### 3.10. Eventos y Lugares → Compras

| Método | Ruta | Finalidad |
|---|---|---|
| POST | `/compras/evento/{id}/cancelar` | Informar la cancelación de un evento y solicitar el procesamiento de reintegros. |

La solicitud incluye el encabezado `X-Operacion-Id`, que identifica la operación de cancelación.

Compras verifica el estado del evento, solicita la invalidación de las entradas y procesa los reintegros de las compras con pagos aprobados.

Los importes se calculan utilizando los precios históricos registrados en los detalles de compra.

## 4. Encabezados e identificadores de integración

### 4.1. X-Cliente-Id

El encabezado `X-Cliente-Id` se utiliza en:

`GET /entradas/{id}`

Permite que Entradas compruebe que la entrada solicitada pertenece al cliente indicado.

### 4.2. X-Operacion-Id

El encabezado `X-Operacion-Id` identifica una operación de cancelación de evento y permite reconocerla durante los reintentos.

Eventos y Lugares lo transmite a los servicios involucrados en el proceso.

El tratamiento depende del receptor:

- **Reservas:** recibe el identificador, pero no lo utiliza como clave de deduplicación. Controla las operaciones mediante sus registros y validaciones de estado.
- **Entradas:** recibe la solicitud de invalidación, pero no procesa directamente el encabezado. Evita repetir la invalidación de entradas que ya cambiaron de estado.
- **Compras:** exige y valida el encabezado. La prevención de reintegros duplicados se apoya en los registros de reintegro por ítem y la verificación de operaciones ya aprobadas.

Por lo tanto, la prevención de operaciones duplicadas depende de los mecanismos específicos de cada microservicio y no exclusivamente del encabezado.

### 4.3. Identificadores compartidos

Los DTO de integración utilizan identificadores para relacionar las operaciones entre servicios.

Entre los principales se encuentran:

- `eventoId`
- `sectorId`
- `asientoId`
- `clienteId`
- `reservaId`
- `compraId`
- `entradaId`

Estos identificadores permiten consultar y relacionar información sin compartir entidades JPA ni establecer claves foráneas entre bases de datos independientes.

## 5. Flujos de integración principales

### 5.1. Compra con pago aprobado

1. El cliente consulta el evento y la disponibilidad.
2. Reservas consulta la configuración correspondiente en Eventos y Lugares.
3. Reservas verifica la ocupación y registra una reserva temporal.
4. Compras consulta y valida la reserva.
5. Compras obtiene los precios correspondientes desde Eventos y Lugares.
6. Compras procesa el pago mediante la pasarela simulada.
7. Cuando el pago es aprobado, Compras solicita confirmar la reserva.
8. Una vez confirmada, Compras solicita a Entradas la emisión de los tickets.
9. Entradas valida la información de la compra, el pago y la reserva, y genera los códigos QR.
10. Compras confirma la operación cuando se completa la emisión de todas las entradas correspondientes.

### 5.2. Pago rechazado

1. Compras recibe el resultado rechazado de la pasarela simulada.
2. Solicita a Reservas liberar la reserva temporal.
3. Reservas libera los lugares correspondientes.
4. Compras registra el resultado rechazado una vez completadas las operaciones necesarias.

No se emiten entradas para una compra cuyo pago fue rechazado.

### 5.3. Devolución individual de una entrada

1. El cliente solicita la devolución mediante Entradas.
2. Entradas valida la existencia y el estado de la entrada y la anticipación mínima requerida.
3. Se registra una solicitud con estado `PENDIENTE`.
4. Un administrador aprueba o rechaza la solicitud.
5. Si se aprueba, Entradas invalida la entrada.
6. Entradas solicita a Reservas liberar el lugar correspondiente.
7. Entradas solicita a Compras procesar el reintegro.

Si la devolución se rechaza, la entrada permanece en estado `EMITIDA` y no se realizan las operaciones externas de liberación ni reintegro.

### 5.4. Cancelación de un evento

1. Eventos y Lugares cambia el estado del evento a `CANCELADO`.
2. Registra una notificación pendiente dentro de la misma transacción local.
3. Devuelve la respuesta al administrador.
4. `NotificadorCancelaciones` procesa las notificaciones mediante un scheduler configurado con un intervalo de 30 segundos.
5. Reservas recibe la cancelación y cancela sus reservas temporales activas.
6. Entradas recibe la cancelación e invalida las entradas emitidas.
7. Cuando las notificaciones correspondientes a Reservas y Entradas se completan, se notifica a Compras.
8. Compras procesa los reintegros de las compras afectadas.
9. Las comunicaciones que no se completan correctamente permanecen pendientes para su posterior reintento.

El estado del procesamiento puede consultarse mediante:

`GET /eventos/{id}/notificaciones-cancelacion`

Las operaciones se realizan mediante transacciones locales y comunicaciones HTTP. No existe una transacción distribuida única entre las bases de datos de todos los microservicios.

## 6. Configuración de las integraciones

Las direcciones base de los microservicios se configuran mediante propiedades y variables de entorno.

### 6.1. Puertos locales

| Componente | Puerto |
|---|---|
| Eventos y Lugares | `8081` |
| Reservas | `8080` |
| Compras y Pagos | `8082` |
| Entradas | `8083` |
| API Gateway | `8084` |

### 6.2. Direcciones de integración de Eventos y Lugares

| Variable | Valor predeterminado |
|---|---|
| `RESERVAS_URL` | `http://localhost:8080` |
| `ENTRADAS_URL` | `http://localhost:8083` |
| `COMPRAS_URL` | `http://localhost:8082` |

### 6.3. Configuración de notificaciones

| Variable | Valor predeterminado |
|---|---|
| `INTEGRACIONES_HABILITADAS` | `true` |
| `ENTRADAS_HABILITADAS` | `true` |
| `COMPRAS_HABILITADAS` | `true` |

Las configuraciones pueden modificarse según el entorno de ejecución.

Los puertos y las direcciones indicados corresponden al entorno local del proyecto.

## 7. Consideraciones de integración

### 7.1. Independencia de los servicios

Cada microservicio administra sus propias entidades, reglas de negocio y persistencia.

La información compartida se intercambia mediante DTO y contratos REST.

No se realizan consultas directas a las bases de datos de otros servicios.

### 7.2. Manejo de fallos

Las comunicaciones HTTP pueden presentar errores de conexión, servicios temporalmente no disponibles o respuestas que impidan completar una operación.

Los procesos de compra, devolución y cancelación utilizan mecanismos de recuperación y reintento según las responsabilidades de cada microservicio.

### 7.3. Idempotencia

Las operaciones sensibles incorporan mecanismos para evitar efectos duplicados ante solicitudes repetidas.

Entre ellos se encuentran:

- Claves de idempotencia para crear reservas.
- Control de emisión duplicada de entradas.
- Registro de entradas liberadas.
- Verificación de pagos y reintegros previamente procesados.
- Registro persistente del estado de las notificaciones de cancelación.

### 7.4. Consistencia entre microservicios

Las operaciones distribuidas se coordinan mediante llamadas HTTP, estados intermedios, registros persistentes, reintentos y compensaciones cuando corresponde.

Cada microservicio administra sus propias transacciones locales.

Los procesos que involucran múltiples servicios no utilizan una transacción ACID distribuida única.

## 8. Documentación relacionada

Los contratos REST completos, incluyendo parámetros, DTO, ejemplos JSON y códigos HTTP, se encuentran en los documentos específicos de cada servicio:

- [Contratos REST de Eventos y Lugares](../msvc-eventosylugares/docs/04-CONTRATOS-API-REST.md)
- [Contratos REST de Reservas](../msvc-reservas/docs/04-CONTRATOS-REST-RESERVAS.md)
- [Contratos REST de Compras y Pagos](../msvc-compras/docs/03-CONTRATOS-REST.md)
- [Contratos REST de Entradas](../msvc-entradas/docs/04-CONTRATOS-REST.md)

También pueden consultarse:

- [Arquitectura general](02-ARQUITECTURA-GENERAL.md)
- [Diagrama de secuencia de compra](03-DIAGRAMA-SECUENCIA-COMPRA.md)
- [Relaciones entre microservicios](04-RELACIONES-ENTRE-MICROSERVICIOS.md)
- [README principal](../README.md)

La documentación OpenAPI/Swagger de cada microservicio complementa estos contratos con los esquemas de datos y las operaciones expuestas por sus controladores.