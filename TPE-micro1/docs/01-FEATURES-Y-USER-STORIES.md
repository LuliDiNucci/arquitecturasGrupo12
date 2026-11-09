# 01 — Funcionalidades y User Stories de TicketYa

## 1. Descripción general

TicketYa es un sistema de gestión y venta de entradas para eventos, que permite consultar eventos y lugares, reservar temporalmente ubicaciones, realizar compras y pagos, emitir entradas digitales y gestionar devoluciones.

La solución está organizada en microservicios, cada uno con responsabilidades específicas:

- **Eventos y Lugares:** administra lugares, sectores, asientos, organizadores, participantes, eventos y precios. También gestiona los estados de los eventos y comunica sus cancelaciones.
- **Reservas:** administra la disponibilidad y ocupación de los lugares, crea reservas temporales, controla su vencimiento y gestiona su confirmación o liberación.
- **Compras y Pagos:** registra las compras, calcula importes, procesa pagos mediante una pasarela simulada, coordina la confirmación de compras y gestiona reintegros.
- **Entradas:** emite entradas digitales, genera códigos QR, permite consultar entradas y gestiona solicitudes de devolución e invalidación.
- **Gateway:** proporciona un punto de entrada para acceder a los microservicios mediante el enrutamiento correspondiente.

Este documento reúne las funcionalidades e historias de usuario de los distintos microservicios, indicando las responsabilidades de cada uno y las principales limitaciones identificadas en la implementación.

## 2. Actores involucrados

| Actor | Responsabilidad |
|---|---|
| Cliente | Consulta eventos, selecciona lugares, realiza reservas, compra entradas y solicita devoluciones. |
| Administrador | Gestiona lugares y eventos, consulta solicitudes de devolución y aprueba o rechaza las solicitudes. |
| Organizador | Participa en la gestión y organización de eventos según los permisos definidos. |
| Sistema TicketYa | Coordina las operaciones entre microservicios y ejecuta procesos automáticos de gestión. |
| Microservicios | Intercambian información y ejecutan las operaciones correspondientes a sus responsabilidades. |

## 3. Funcionalidades por microservicio

### 3.1. Eventos y Lugares

**Objetivo:** administrar la información de los lugares donde se realizan eventos y la configuración necesaria para su publicación y consulta.

**Funcionalidades principales:**

- Registrar y consultar lugares, incluyendo nombre, dirección, ciudad y coordenadas.
- Crear sectores numerados y no numerados, estableciendo sus capacidades.
- Registrar asientos para sectores numerados.
- Registrar organizadores y participantes.
- Crear eventos en estado borrador.
- Configurar precios por sector para cada evento.
- Publicar, modificar y suspender eventos.
- Consultar eventos mediante filtros de tipo, organizador, participante, ciudad, fechas y texto.
- Consultar si un evento está habilitado para realizar reservas.
- Consultar los sectores y precios configurados para un evento.
- Cancelar eventos y comunicar la cancelación a los microservicios relacionados.

**Historias de usuario principales:**

- **US-EV-01 a US-EV-03:** registrar lugares, sectores y asientos.
- **US-EV-04:** registrar organizadores y participantes.
- **US-EV-05 a US-EV-08:** crear, configurar, publicar y modificar eventos.
- **US-EV-09 y US-EV-10:** consultar eventos, sectores y precios.
- **US-EV-11:** suspender y volver a publicar eventos.
- **US-EV-12:** cancelar eventos y notificar a los servicios relacionados.

**Limitaciones y consideraciones:**

- Los eventos no publicados no se muestran en las consultas públicas por defecto.
- La consulta de sectores y precios no informa la ocupación ni el stock disponible: esa responsabilidad corresponde a Reservas.
- Eventos y Lugares no ejecuta directamente los reintegros ni invalida los códigos QR; comunica la cancelación para que los otros servicios realicen las operaciones que les corresponden.
- La integración con los servicios externos cuenta con mecanismos de reintento, pero no todos los receptores procesan actualmente `X-Operacion-Id` de manera idempotente.

### 3.2. Reservas

**Objetivo:** administrar la disponibilidad y ocupación temporal o confirmada de los lugares de cada evento.

**Funcionalidades principales:**

- Consultar disponibilidad de sectores numerados y no numerados.
- Crear reservas temporales con vencimiento de 10 minutos.
- Validar que el evento esté habilitado y que los sectores y asientos pertenezcan al evento seleccionado.
- Evitar reservas simultáneas sobre un mismo lugar.
- Controlar la capacidad máxima de los sectores no numerados.
- Evitar reservas duplicadas mediante claves de idempotencia.
- Liberar automáticamente la capacidad de reservas vencidas mediante vencimiento lógico.
- Confirmar reservas cuando se aprueba el pago.
- Liberar reservas cuando el pago es rechazado.
- Liberar lugares correspondientes a entradas devueltas.
- Invalidar reservas temporales cuando se cancela un evento.

**Historias de usuario principales:**

- **US01:** consultar disponibilidad.
- **US02:** realizar una reserva temporal.
- **US03:** evitar reservas simultáneas del mismo lugar.
- **US04:** liberar reservas vencidas.
- **US05:** confirmar una reserva después del pago.
- **US06:** liberar una reserva por pago rechazado.
- **US07:** liberar lugares por devolución o cancelación de eventos.

**Limitaciones y consideraciones:**

- La consulta de disponibilidad todavía no devuelve explícitamente la lista de asientos ocupados en sectores numerados; únicamente excluye esos asientos de los disponibles.
- El vencimiento se resuelve mediante un filtro temporal, sin incorporar un scheduler en Entrega 1.
- La concurrencia fue probada en MySQL con diez solicitudes sobre el mismo asiento, pero todavía falta verificar el comportamiento con varias instancias reales del microservicio.
- La coordinación completa con Compras, Entradas y Eventos requiere pruebas integradas adicionales.

### 3.3. Compras y Pagos

**Objetivo:** gestionar las compras de entradas, procesar los pagos y coordinar la confirmación de las reservas, emisión de entradas y reintegros.

**Funcionalidades principales:**

- Iniciar una compra a partir de una reserva temporal vigente.
- Consultar la información de la reserva y los precios correspondientes.
- Calcular el importe total y registrar el detalle de cada compra.
- Conservar el precio histórico de cada entrada adquirida.
- Procesar pagos mediante una pasarela simulada.
- Registrar el medio de pago, importe, fecha, resultado y referencia externa.
- Confirmar compras cuando el pago es aprobado.
- Solicitar la confirmación de la reserva y la emisión de entradas.
- Liberar reservas cuando el pago es rechazado.
- Evitar cobros duplicados mediante claves de idempotencia y bloqueos.
- Gestionar reintegros individuales por devolución aprobada.
- Gestionar reintegros por cancelación de eventos.
- Permitir reintegros parciales cuando una compra contiene varias entradas.

**Historias de usuario principales:**

- **US01:** iniciar una compra.
- **US02:** registrar el detalle de una compra.
- **US03:** procesar el pago.
- **US04:** confirmar una compra aprobada.
- **US05:** gestionar un pago rechazado.
- **US06:** evitar cobros duplicados.
- **US07:** procesar el reintegro de una entrada devuelta.
- **US08:** gestionar reintegros por cancelación de eventos.

**Consideraciones importantes:**

El proceso de compra utiliza estados intermedios para representar operaciones que todavía deben completarse o compensarse:

- `PENDIENTE`: compra iniciada, pendiente de pago.
- `CONFIRMACION_PENDIENTE`: pago aprobado, pendiente de completar la confirmación.
- `LIBERACION_PENDIENTE`: pago rechazado, pendiente de liberar la reserva.
- `COMPENSACION_PENDIENTE`: no se pudo completar correctamente la confirmación de la compra.
- `CONFIRMADA`: compra completada correctamente.
- `RECHAZADA`: pago rechazado y liberación completada.

Los estados de reintegro también permiten reflejar operaciones parciales o pendientes.

**Limitaciones y consideraciones:**

- La pasarela de pagos es simulada y no corresponde a una integración con una entidad financiera real.
- Se identificaron pruebas unitarias y de concurrencia para diferentes escenarios de pago e idempotencia.
- No se identificó una prueba integral específica que cubra el rechazo del pago y la liberación de la reserva.
- La coordinación completa entre pago, confirmación de reserva y emisión de entradas requiere pruebas integradas.

### 3.4. Entradas

**Objetivo:** gestionar las entradas digitales adquiridas por los clientes y sus correspondientes solicitudes de devolución.

**Funcionalidades principales:**

- Emitir una entrada por cada lugar adquirido en una compra.
- Verificar que la compra tenga un pago aprobado y que la reserva esté confirmada.
- Generar un código QR único por entrada.
- Evitar la emisión duplicada de entradas para una misma compra.
- Consultar una entrada individual y verificar su pertenencia al cliente.
- Consultar el listado de entradas de un cliente.
- Generar imágenes QR en formato PNG y devolverlas codificadas en Base64.
- Registrar solicitudes de devolución.
- Consultar solicitudes pendientes.
- Aprobar o rechazar solicitudes de devolución.
- Invalidar entradas correspondientes a eventos cancelados.

**Historias de usuario principales:**

- **US-01:** emitir entradas para una compra.
- **US-02:** consultar una entrada individual.
- **US-03:** consultar las entradas de un cliente.
- **US-04:** solicitar la devolución de una entrada.
- **US-05:** consultar solicitudes pendientes.
- **US-06:** aprobar una devolución.
- **US-07:** rechazar una devolución.
- **US-08:** invalidar entradas por cancelación de un evento.

**Consideraciones importantes:**

Una solicitud de devolución requiere que la entrada esté emitida y que falten al menos 72 horas para la fecha del evento.

Cuando se aprueba una devolución:

1. La solicitud pasa a estado `APROBADA`.
2. La entrada pasa a estado `INVALIDADA`.
3. Se solicita a Reservas la liberación del lugar.
4. Se solicita a Compras el reintegro correspondiente.

Cuando se rechaza:

1. La solicitud pasa a estado `RECHAZADA`.
2. La entrada conserva su estado `EMITIDA`.
3. No se solicitan liberaciones ni reintegros.

**Limitaciones y consideraciones:**

- La invalidación de entradas por cancelación de eventos no libera actualmente las reservas ni coordina los reintegros.
- La consulta individual verifica la pertenencia de la entrada al cliente, mientras que el listado recibe el identificador del cliente directamente en la URL.
- Se implementó la generación y consulta de códigos QR, pero no la funcionalidad de registrar el uso de una entrada para validar el ingreso al evento.
- Las operaciones de devolución dependen de la disponibilidad de Reservas y Compras para completar las acciones externas.

## 4. Circuito general de compra

El circuito principal de TicketYa involucra a los cuatro microservicios funcionales:

1. El cliente consulta eventos y selecciona un evento, un sector y, cuando corresponde, sus asientos.
2. Eventos y Lugares proporciona la información del evento, los sectores y los precios.
3. Reservas consulta la disponibilidad y crea una reserva temporal de los lugares seleccionados.
4. Compras recibe la reserva, verifica su vigencia y registra una compra pendiente.
5. Compras procesa el pago mediante la pasarela simulada.
6. Si el pago es rechazado, Compras solicita a Reservas liberar la reserva.
7. Si el pago es aprobado, Compras solicita confirmar la reserva.
8. Una vez confirmada, Compras solicita a Entradas emitir los tickets.
9. Entradas genera una entrada por cada lugar adquirido y asigna un código QR único.
10. Compras asocia las entradas emitidas a los detalles de la compra y la marca como confirmada.

La compra se considera completada cuando se confirma la reserva y se reciben todas las entradas esperadas.

## 5. Circuito general de devoluciones y cancelaciones

### 5.1. Devolución individual

1. El cliente solicita la devolución de una entrada.
2. Entradas verifica que la entrada esté emitida y que se cumpla el plazo mínimo de 72 horas.
3. Se registra una solicitud pendiente.
4. El administrador aprueba o rechaza la solicitud.
5. Si se aprueba, Entradas invalida la entrada y solicita a Reservas liberar el lugar.
6. Entradas solicita a Compras procesar el reintegro correspondiente.
7. Compras verifica la devolución aprobada y calcula el importe utilizando el precio histórico de la entrada.
8. Se registra el reintegro en la pasarela simulada.

### 5.2. Cancelación de un evento

1. El administrador cancela el evento desde Eventos y Lugares.
2. Se registra la cancelación y una notificación pendiente.
3. Eventos y Lugares comunica la cancelación a Reservas y Entradas.
4. Reservas invalida las reservas temporales pendientes.
5. Entradas invalida las entradas emitidas correspondientes al evento.
6. Eventos y Lugares comunica la cancelación a Compras.
7. Compras identifica las compras pagadas y procesa los reintegros correspondientes, evitando duplicar los ya realizados.

La cancelación y las notificaciones se gestionan mediante operaciones separadas entre microservicios. Por lo tanto, la cancelación del evento no implica que todas las operaciones externas se completen dentro de una única transacción.

## 6. Estados principales del sistema

| Elemento | Estados principales |
|---|---|
| Evento | `BORRADOR`, `PUBLICADO`, `AGOTADO`, `SUSPENDIDO`, `CANCELADO`, `FINALIZADO` |
| Reserva | `ACTIVA`, `VENCIDA`, `CONFIRMADA`, `LIBERADA` |
| Compra | `PENDIENTE`, `CONFIRMACION_PENDIENTE`, `LIBERACION_PENDIENTE`, `COMPENSACION_PENDIENTE`, `CONFIRMADA`, `RECHAZADA` |
| Pago | `APROBADO`, `RECHAZADO` |
| Entrada | `EMITIDA`, `INVALIDADA` |
| Solicitud de devolución | `PENDIENTE`, `APROBADA`, `RECHAZADA` |

Los estados representan situaciones propias de cada microservicio. La coordinación entre ellos se realiza mediante comunicaciones REST y no mediante una única transacción de base de datos compartida.

## 7. Consideraciones generales sobre implementación y pruebas

Las funcionalidades descriptas corresponden a las historias de usuario y a la implementación identificada en los respectivos microservicios.

Se identifican distintos niveles de cobertura:

- **Implementación local:** la funcionalidad está desarrollada dentro del microservicio.
- **Pruebas unitarias:** existen pruebas para verificar comportamientos específicos.
- **Pruebas de integración:** se verifican interacciones con bases de datos u otros componentes.
- **Pruebas de extremo a extremo pendientes:** todavía debe comprobarse el circuito completo con todos los microservicios funcionando conjuntamente.

Entre los aspectos relevantes que requieren seguimiento se encuentran:

- Completar la respuesta de disponibilidad de sectores numerados incluyendo los asientos ocupados.
- Verificar la concurrencia de reservas con varias instancias del servicio.
- Ejecutar pruebas integradas de aprobación y rechazo de pagos, confirmación de reservas y emisión de entradas.
- Verificar la coordinación completa de devoluciones individuales.
- Verificar los reintegros y la invalidación de entradas ante cancelaciones.
- Reforzar la seguridad de los endpoints internos para que solamente puedan invocarlos los servicios autorizados.
- Completar las validaciones e integraciones que correspondan a las siguientes entregas.

La existencia de métodos de prueba en el código no implica por sí sola que todas las pruebas hayan sido ejecutadas exitosamente en el entorno de entrega.

## 8. Documentación específica de cada microservicio

Para consultar el detalle de las historias, criterios de aceptación, arquitectura, modelo de datos, contratos y pruebas de cada servicio:

- [Eventos y Lugares](../msvc-eventosylugares/docs/01-FEATURES-Y-USER-STORIES.md)
- [Reservas](../msvc-reservas/docs/01-FUNCIONALIDADES-Y-USER-STORIES.md)
- [Compras y Pagos](../msvc-compras/docs/01-FUNCIONALIDADES-Y-USER-STORIES.md)
- [Entradas](../msvc-entradas/docs/01-FUNCIONALIDADES-Y-USER-STORIES.md)

Este documento constituye una visión funcional unificada del sistema TicketYa para la Entrega 1. Las documentaciones individuales mantienen el detalle específico de cada microservicio.