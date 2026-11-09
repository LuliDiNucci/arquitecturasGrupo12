# 01 — Funcionalidades e historias de usuario de TicketYa

## 1. Descripción general

TicketYa es un sistema de venta y gestión de entradas para eventos, que permite consultar eventos y lugares, reservar temporalmente ubicaciones, realizar compras y pagos, emitir entradas digitales con códigos QR y gestionar devoluciones y cancelaciones.

La solución está organizada en cuatro microservicios funcionales y un API Gateway:

- **Eventos y Lugares:** administra lugares, sectores, asientos, organizadores, participantes, eventos y precios. También gestiona los estados de los eventos y coordina sus cancelaciones.
- **Reservas:** administra la disponibilidad y ocupación de lugares, crea reservas temporales, controla su vencimiento y gestiona confirmaciones y liberaciones.
- **Compras y Pagos:** registra compras, calcula importes, procesa pagos mediante una pasarela simulada, coordina confirmaciones y gestiona reintegros.
- **Entradas:** emite entradas digitales, genera códigos QR, permite consultar entradas y administra solicitudes de devolución e invalidaciones.
- **API Gateway:** centraliza el acceso al sistema y enruta las solicitudes a los microservicios correspondientes.

Este documento presenta las funcionalidades e historias de usuario del sistema completo. Los criterios de aceptación específicos se encuentran desarrollados en la documentación de cada microservicio.

## 2. Actores involucrados

| Actor | Responsabilidad |
|---|---|
| Cliente | Consulta eventos, selecciona lugares, realiza reservas, compra entradas y solicita devoluciones. |
| Administrador | Gestiona lugares y eventos, consulta solicitudes de devolución y aprueba o rechaza las solicitudes. |
| Organizador | Gestiona la información y configuración de los eventos. |
| Sistema TicketYa | Coordina las operaciones entre microservicios y ejecuta procesos automáticos de gestión. |

Los microservicios también actúan como consumidores de las APIs de otros servicios cuando necesitan consultar información o solicitar operaciones.

## 3. Funcionalidades por microservicio

### 3.1. Eventos y Lugares

**Objetivo:** administrar la información de los lugares donde se realizan los eventos y la configuración necesaria para su publicación, consulta y gestión.

**Funcionalidades principales:**

- Registrar y consultar lugares, incluyendo nombre, dirección, ciudad y coordenadas.
- Crear sectores numerados y no numerados, estableciendo sus capacidades.
- Registrar asientos para sectores numerados.
- Registrar organizadores y participantes.
- Crear eventos en estado `BORRADOR`.
- Configurar precios por sector para cada evento.
- Publicar, modificar, suspender, reabrir y cancelar eventos.
- Consultar eventos mediante filtros de tipo, organizador, participante, ciudad, fechas y texto.
- Ordenar eventos por fecha, precio o relevancia.
- Consultar si un evento está habilitado para recibir reservas.
- Consultar los sectores, asientos y precios configurados para cada evento.
- Registrar notificaciones de cancelación y coordinar su procesamiento con los demás servicios.

**Historias de usuario:**

| Historia | Funcionalidad |
|---|---|
| US01 | Crear un lugar. |
| US02 | Crear sectores numerados y no numerados. |
| US03 | Registrar asientos numerados. |
| US04 | Registrar organizadores y participantes. |
| US05 | Crear un evento en borrador. |
| US06 | Configurar precios por sector. |
| US07 | Publicar un evento. |
| US08 | Modificar un evento. |
| US09 | Buscar eventos mediante criterios combinables. |
| US10 | Consultar información para Reservas y Compras. |
| US11 | Suspender un evento. |
| US12 | Cancelar un evento y notificar a otros microservicios. |

**Reglas principales:**

- Los eventos no publicados no aparecen en las búsquedas públicas por defecto.
- La publicación requiere que el evento tenga una configuración completa y una fecha futura.
- Los precios se establecen por combinación de evento y sector.
- La consulta de sectores y precios no informa la ocupación real ni la disponibilidad restante.
- La cancelación de un evento se registra junto con una notificación persistente.
- Las notificaciones a Reservas, Entradas y Compras se procesan mediante operaciones reintentables.
- Eventos y Lugares coordina las cancelaciones, mientras que los demás servicios ejecutan las operaciones correspondientes a sus responsabilidades.

### 3.2. Reservas

**Objetivo:** administrar la disponibilidad y ocupación temporal o confirmada de los lugares de cada evento.

**Funcionalidades principales:**

- Consultar disponibilidad de sectores numerados y no numerados.
- Informar asientos disponibles y ocupados en sectores numerados.
- Crear reservas temporales con una vigencia de 10 minutos.
- Validar que el evento esté habilitado y que los sectores y asientos correspondan al evento seleccionado.
- Evitar reservas simultáneas incompatibles sobre un mismo lugar.
- Controlar la capacidad máxima de sectores no numerados.
- Evitar reservas duplicadas mediante claves de idempotencia.
- Liberar capacidad de reservas vencidas mediante vencimiento lógico.
- Confirmar reservas después de la aprobación del pago.
- Liberar reservas cuando el pago es rechazado.
- Liberar lugares correspondientes a entradas devueltas.
- Cancelar reservas temporales cuando se cancela un evento.
- Impedir nuevas reservas para eventos cancelados.

**Historias de usuario:**

| Historia | Funcionalidad |
|---|---|
| US01 | Consultar disponibilidad de entradas. |
| US02 | Realizar una reserva temporal. |
| US03 | Evitar reservas simultáneas del mismo lugar. |
| US04 | Liberar lugares de reservas vencidas. |
| US05 | Confirmar una reserva después del pago. |
| US06 | Liberar una reserva por pago rechazado. |
| US07 | Liberar lugares por devolución o cancelación del evento. |

**Reglas principales:**

- Las reservas temporales tienen una duración de 10 minutos.
- Las reservas activas y vigentes bloquean los lugares seleccionados.
- Las reservas confirmadas continúan ocupando los lugares adquiridos.
- Las reservas activas vencidas dejan de bloquear lugares, aunque su estado persistido todavía sea `ACTIVA`.
- El control de concurrencia utiliza transacciones y bloqueo pesimista en MySQL.
- Las claves de idempotencia evitan crear reservas duplicadas ante reintentos.
- La devolución individual registra el identificador de la entrada procesada para evitar una segunda liberación.

### 3.3. Compras y Pagos

**Objetivo:** gestionar las compras de entradas, procesar pagos y coordinar la confirmación de reservas, la emisión de entradas y los reintegros.

**Funcionalidades principales:**

- Iniciar compras a partir de reservas temporales vigentes.
- Consultar la reserva y los precios de los sectores correspondientes.
- Calcular el importe total y registrar los detalles de cada compra.
- Conservar el precio histórico de cada entrada adquirida.
- Procesar pagos mediante una pasarela simulada.
- Registrar el medio de pago, importe, fecha, resultado y referencia externa.
- Confirmar compras cuando el pago es aprobado.
- Solicitar la confirmación de la reserva y la emisión de entradas.
- Liberar reservas cuando el pago es rechazado.
- Evitar cobros duplicados mediante mecanismos de idempotencia y concurrencia.
- Gestionar reintegros individuales por devoluciones aprobadas.
- Gestionar reintegros derivados de cancelaciones de eventos.
- Procesar reintegros parciales cuando una compra contiene varias entradas.

**Historias de usuario:**

| Historia | Funcionalidad |
|---|---|
| US01 | Iniciar una compra. |
| US02 | Registrar los detalles de una compra. |
| US03 | Procesar un pago. |
| US04 | Confirmar una compra aprobada. |
| US05 | Gestionar un pago rechazado. |
| US06 | Evitar cobros duplicados. |
| US07 | Procesar el reintegro de una entrada devuelta. |
| US08 | Gestionar reintegros por cancelación de eventos. |

**Estados principales de compra:**

- `PENDIENTE`: compra iniciada, a la espera del pago.
- `CONFIRMACION_PENDIENTE`: pago aprobado con operaciones de confirmación por completar.
- `LIBERACION_PENDIENTE`: pago rechazado con liberación de reserva por completar.
- `COMPENSACION_PENDIENTE`: operación que requiere completar acciones de compensación.
- `CONFIRMADA`: compra completada correctamente.
- `RECHAZADA`: compra rechazada después del procesamiento correspondiente.

El proceso de compra contempla operaciones intermedias para administrar las comunicaciones con Reservas y Entradas.

Los reintegros se calculan utilizando los importes históricos registrados en la compra. Las operaciones ya aprobadas no se procesan nuevamente, evitando duplicaciones.

### 3.4. Entradas

**Objetivo:** administrar las entradas digitales adquiridas por los clientes y sus correspondientes solicitudes de devolución.

**Funcionalidades principales:**

- Emitir una entrada por cada lugar adquirido en una compra.
- Verificar que la compra tenga un pago aprobado y que la reserva esté confirmada.
- Generar un código QR único por entrada.
- Evitar la emisión duplicada de entradas para una misma compra.
- Consultar entradas individuales y verificar su pertenencia al cliente indicado.
- Consultar las entradas asociadas a un cliente.
- Generar imágenes QR en formato PNG y entregarlas codificadas en Base64.
- Registrar solicitudes de devolución.
- Consultar solicitudes pendientes.
- Aprobar o rechazar solicitudes de devolución.
- Invalidar entradas correspondientes a eventos cancelados.

**Historias de usuario:**

| Historia | Funcionalidad |
|---|---|
| US01 | Emitir entradas para una compra. |
| US02 | Consultar una entrada individual. |
| US03 | Consultar las entradas de un cliente. |
| US04 | Solicitar la devolución de una entrada. |
| US05 | Consultar solicitudes pendientes. |
| US06 | Aprobar una devolución. |
| US07 | Rechazar una devolución. |
| US08 | Invalidar entradas por cancelación de un evento. |

**Reglas principales:**

Una solicitud de devolución requiere que la entrada se encuentre en estado `EMITIDA` y que falten al menos 72 horas para la realización del evento.

Cuando una devolución se aprueba:

1. La solicitud cambia a estado `APROBADA`.
2. La entrada cambia a estado `INVALIDADA`.
3. Entradas solicita a Reservas la liberación del lugar correspondiente.
4. Entradas solicita a Compras el reintegro correspondiente.

Cuando una devolución se rechaza:

1. La solicitud cambia a estado `RECHAZADA`.
2. La entrada conserva el estado `EMITIDA`.
3. No se solicita la liberación del lugar ni el reintegro.

Ante una cancelación de evento, Entradas invalida las entradas emitidas. La coordinación general de cancelaciones corresponde a Eventos y Lugares, que también comunica la operación a Reservas y Compras.

## 4. Circuito general de compra

El circuito principal de TicketYa involucra a los cuatro microservicios funcionales.

1. El cliente consulta eventos y selecciona un evento, sector y, cuando corresponde, sus asientos.
2. Eventos y Lugares proporciona la información del evento, sectores y precios.
3. Reservas consulta la disponibilidad y crea una reserva temporal para los lugares seleccionados.
4. Compras recibe la reserva, verifica su vigencia y registra una compra en estado `PENDIENTE`.
5. Compras procesa el pago mediante la pasarela simulada.
6. Si el pago es rechazado, Compras solicita a Reservas liberar la reserva.
7. Si el pago es aprobado y la reserva continúa vigente, Compras solicita su confirmación.
8. Una vez confirmada la reserva, Compras solicita a Entradas emitir los tickets.
9. Entradas genera una entrada por cada lugar adquirido y asigna un código QR único.
10. Compras asocia las entradas emitidas a los detalles de la compra y completa la confirmación.

La compra se considera completada cuando se confirma la reserva y se reciben las entradas correspondientes.

Cuando una operación no puede completarse, Compras utiliza estados intermedios para administrar los reintentos o compensaciones necesarias.

## 5. Circuito general de devoluciones y cancelaciones

### 5.1. Devolución individual de una entrada

1. El cliente solicita la devolución de una entrada.
2. Entradas verifica que la entrada esté emitida y que se cumpla la anticipación mínima de 72 horas.
3. Se registra una solicitud con estado `PENDIENTE`.
4. El administrador aprueba o rechaza la solicitud.
5. Si se aprueba, Entradas invalida la entrada y solicita a Reservas liberar el lugar.
6. Entradas solicita a Compras procesar el reintegro.
7. Compras determina el importe utilizando el precio histórico de la entrada.
8. Se procesa y registra el reintegro mediante la pasarela simulada.

### 5.2. Cancelación de un evento

1. El administrador cancela el evento desde Eventos y Lugares.
2. El evento cambia a estado `CANCELADO` y se registra una notificación persistente.
3. Eventos y Lugares comunica la cancelación a Reservas y Entradas.
4. Reservas cancela las reservas temporales correspondientes e impide nuevas reservas para ese evento.
5. Entradas invalida las entradas emitidas asociadas al evento.
6. Una vez procesadas las notificaciones correspondientes, Eventos y Lugares comunica la cancelación a Compras.
7. Compras identifica las compras con pagos aprobados y procesa los reintegros correspondientes.
8. Los reintegros ya aprobados se reconocen para impedir devoluciones duplicadas.

El procesamiento de las notificaciones utiliza reintentos y registros persistentes para controlar las comunicaciones entre microservicios.

Cada servicio administra sus propias transacciones y datos. La cancelación no constituye una única transacción distribuida entre todas las bases de datos.

## 6. Estados principales del sistema

| Elemento | Estados principales |
|---|---|
| Evento | `BORRADOR`, `PUBLICADO`, `AGOTADO`, `SUSPENDIDO`, `CANCELADO`, `FINALIZADO` |
| Reserva | `ACTIVA`, `VENCIDA`, `CONFIRMADA`, `LIBERADA`, `CANCELADA` |
| Compra | `PENDIENTE`, `CONFIRMACION_PENDIENTE`, `LIBERACION_PENDIENTE`, `COMPENSACION_PENDIENTE`, `CONFIRMADA`, `RECHAZADA`, `REINTEGRADA`, `REINTEGRO_PARCIAL` |
| Pago | `APROBADO`, `RECHAZADO` |
| Entrada | `EMITIDA`, `INVALIDADA` |
| Solicitud de devolución | `PENDIENTE`, `APROBADA`, `RECHAZADA` |

Los estados representan las distintas situaciones administradas por cada microservicio.

Las transiciones se realizan mediante operaciones REST y reglas de negocio específicas.

## 7. Documentación específica de cada microservicio

Los documentos de cada microservicio desarrollan las historias de usuario, criterios de aceptación, arquitectura, modelo de datos, contratos REST y pruebas correspondientes.

- [Eventos y Lugares](../msvc-eventosylugares/docs/01-FEATURES-Y-USER-STORIES.md)
- [Reservas](../msvc-reservas/docs/01-FUNCIONALIDADES-Y-USER-STORIES.md)
- [Compras y Pagos](../msvc-compras/docs/01-FUNCIONALIDADES-Y-USER-STORIES.md)
- [Entradas](../msvc-entradas/docs/01-FUNCIONALIDADES-Y-USER-STORIES.md)

La arquitectura general, el diagrama de secuencia de compra, los contratos de integración y el plan de testing se encuentran en los demás documentos de la carpeta `docs/`.