# 01 - Funcionalidades y User Stories

## 1. Descripción general

El microservicio **msvc-entradas** forma parte del sistema TicketYa y se encarga de gestionar las entradas correspondientes a los eventos.

Sus principales responsabilidades son:

- Emitir entradas a partir de compras y reservas confirmadas.
- Generar un código QR único para cada entrada.
- Consultar las entradas de un cliente.
- Validar que una entrada pertenezca al cliente que solicita su consulta individual.
- Gestionar solicitudes de devolución de entradas.
- Invalidar las entradas correspondientes a eventos cancelados.

Además, se comunica con los microservicios de **Compras**, **Reservas** y **Eventos y Lugares** para obtener información y coordinar determinadas operaciones.

## 2. Actores involucrados

| Actor | Responsabilidad |
|---|---|
| Cliente | Consulta sus entradas y solicita devoluciones. |
| Administrador | Aprueba o rechaza solicitudes de devolución. |
| Microservicio Compras | Proporciona información sobre el estado de las compras y procesa solicitudes de reintegro. |
| Microservicio Reservas | Proporciona información sobre las reservas y gestiona la devolución de lugares. |
| Microservicio Eventos y Lugares | Proporciona información sobre los eventos, incluyendo su fecha y estado. |

## 3. Funcionalidades principales

### 3.1. Emisión de entradas

El sistema permite emitir las entradas correspondientes a una compra.

Antes de generar las entradas, el microservicio verifica:

- Que el identificador de la compra sea válido.
- Que la compra tenga un pago aprobado.
- Que la compra se encuentre en un estado habilitado para emitir entradas.
- Que la reserva asociada esté confirmada.
- Que la reserva corresponda al mismo cliente de la compra.
- Que la reserva contenga lugares válidos y no liberados.

Una vez realizadas las verificaciones, se genera una entrada por cada lugar adquirido.

Cada entrada almacena información sobre la compra, la reserva, el cliente, el evento, el sector y, cuando corresponde, el asiento.

También se genera un código QR único para cada entrada.

**Comportamiento ante solicitudes repetidas:**

Si la compra ya tiene entradas emitidas, el sistema devuelve las entradas existentes en lugar de generar nuevas. De esta manera se evita la duplicación de entradas.

### 3.2. Consulta de entradas

El sistema permite consultar una entrada individual mediante su identificador.

Para realizar esta operación, se verifica que la entrada exista y que pertenezca al cliente indicado en el encabezado `X-Cliente-Id`.

Si la validación es correcta, se devuelve la información de la entrada junto con la imagen de su código QR codificada en Base64.

También permite consultar todas las entradas asociadas a un cliente, ordenadas desde las más recientes hasta las más antiguas.

### 3.3. Generación de códigos QR

Cada entrada emitida posee un código QR único.

El microservicio utiliza un servicio específico para generar una imagen PNG a partir del código QR almacenado.

La imagen generada tiene un tamaño de 260 × 260 píxeles y se devuelve codificada en Base64 dentro del DTO de la entrada.

El código QR se genera nuevamente al construir la respuesta, por lo que la imagen no se almacena como un archivo en la base de datos.

### 3.4. Solicitud de devolución

El cliente puede solicitar la devolución de una entrada.

Para aceptar la solicitud, el sistema verifica:

- Que la entrada exista.
- Que la entrada se encuentre en estado `EMITIDA`.
- Que falten al menos 72 horas para la fecha del evento.
- Que no exista otra solicitud de devolución pendiente para esa misma entrada.

Si se cumplen las condiciones, se registra una solicitud con:

- Identificador de la entrada.
- Fecha de solicitud.
- Motivo.
- Estado inicial `PENDIENTE`.

### 3.5. Gestión de solicitudes de devolución

El sistema permite consultar las solicitudes que se encuentran pendientes de resolución.

El administrador puede aprobar o rechazar una solicitud.

**Aprobación:**

Cuando se aprueba una devolución:

1. La solicitud cambia su estado a `APROBADA`.
2. Se registra el identificador del administrador que realizó la operación.
3. Se registra la fecha de resolución.
4. La entrada asociada cambia su estado a `INVALIDADA`.
5. Se solicita al microservicio Reservas la devolución del lugar correspondiente.
6. Se solicita al microservicio Compras el reintegro correspondiente.

La invalidación de la entrada y la actualización de la solicitud se realizan dentro de una transacción local. Las comunicaciones con los otros microservicios se realizan después de finalizar esa transacción.

**Rechazo:**

Cuando se rechaza una solicitud:

1. La solicitud cambia su estado a `RECHAZADA`.
2. Se registra el administrador responsable.
3. Se registra la fecha de resolución.
4. La entrada conserva su estado `EMITIDA`.

No se realizan operaciones de liberación del lugar ni de reintegro.

### 3.6. Invalidación de entradas por cancelación de eventos

El sistema permite invalidar las entradas correspondientes a un evento cancelado.

Para realizar esta operación, el microservicio consulta el estado del evento mediante el microservicio Eventos y Lugares.

Si el evento se encuentra en estado `CANCELADO`, todas las entradas asociadas que estén en estado `EMITIDA` pasan a estado `INVALIDADA`.

Las entradas que ya se encuentren en otros estados no son modificadas.

Esta operación no realiza actualmente la liberación de reservas ni la coordinación de reintegros por cancelación del evento.

## 4. User Stories

### US-01: Emitir entradas para una compra

**Como** sistema de TicketYa,

**quiero** generar las entradas correspondientes a una compra confirmada y pagada,

**para** que el cliente pueda acceder a los lugares adquiridos.

**Criterios de aceptación:**

- La compra debe existir y tener un pago aprobado.
- La compra debe encontrarse en un estado habilitado para la emisión.
- La reserva asociada debe estar confirmada.
- La reserva debe corresponder al cliente de la compra.
- Debe generarse una entrada por cada lugar adquirido.
- Cada entrada debe tener un código QR único.
- No deben generarse entradas duplicadas para una misma compra.

### US-02: Consultar una entrada individual

**Como** cliente,

**quiero** consultar una entrada mediante su identificador,

**para** visualizar sus datos y su código QR.

**Criterios de aceptación:**

- La entrada debe existir.
- La entrada debe pertenecer al cliente indicado.
- La respuesta debe incluir los datos de la entrada y la imagen QR en Base64.
- Si la entrada no existe, la operación debe rechazarse.
- Si la entrada pertenece a otro cliente, la operación debe rechazarse.

### US-03: Consultar las entradas de un cliente

**Como** cliente,

**quiero** obtener el listado de mis entradas,

**para** visualizar los lugares que tengo adquiridos.

**Criterios de aceptación:**

- El sistema debe devolver las entradas asociadas al identificador del cliente recibido.
- Las entradas deben estar ordenadas desde la más reciente hasta la más antigua.
- Cada entrada debe incluir la información de su código QR.

### US-04: Solicitar la devolución de una entrada

**Como** cliente,

**quiero** solicitar la devolución de una entrada,

**para** iniciar el proceso de reintegro del importe correspondiente.

**Criterios de aceptación:**

- La entrada debe existir.
- La entrada debe encontrarse en estado `EMITIDA`.
- La solicitud debe realizarse al menos 72 horas antes del evento.
- No debe existir otra solicitud pendiente para la misma entrada.
- La solicitud debe registrarse con estado `PENDIENTE`.
- Debe almacenarse el motivo y la fecha de solicitud.

### US-05: Consultar solicitudes pendientes

**Como** administrador,

**quiero** consultar las solicitudes de devolución pendientes,

**para** revisar cuáles necesitan una resolución.

**Criterios de aceptación:**

- El sistema debe devolver las solicitudes cuyo estado sea `PENDIENTE`.
- Cada resultado debe incluir los datos de la solicitud y el identificador de la entrada asociada.

### US-06: Aprobar una devolución

**Como** administrador,

**quiero** aprobar una solicitud de devolución,

**para** invalidar la entrada y solicitar la devolución del lugar y el reintegro correspondiente.

**Criterios de aceptación:**

- La solicitud debe existir.
- La solicitud debe encontrarse pendiente o haber sido aprobada previamente para permitir reintentar las comunicaciones externas.
- La solicitud debe quedar en estado `APROBADA`.
- Deben registrarse el administrador y la fecha de resolución.
- La entrada debe quedar en estado `INVALIDADA`.
- Debe solicitarse a Reservas la devolución del lugar.
- Debe solicitarse a Compras el reintegro correspondiente.
- Una solicitud rechazada no puede aprobarse.

### US-07: Rechazar una devolución

**Como** administrador,

**quiero** rechazar una solicitud de devolución,

**para** impedir que se realice el reintegro solicitado.

**Criterios de aceptación:**

- La solicitud debe existir.
- La solicitud debe encontrarse en estado `PENDIENTE`.
- Debe cambiar a estado `RECHAZADA`.
- Deben registrarse el administrador y la fecha de resolución.
- La entrada debe conservar su estado `EMITIDA`.
- No deben realizarse llamadas a los servicios de liberación de lugares ni de reintegros.

### US-08: Invalidar entradas por cancelación de un evento

**Como** sistema de TicketYa,

**quiero** invalidar las entradas de un evento cancelado,

**para** impedir que las entradas emitidas sigan considerándose válidas.

**Criterios de aceptación:**

- El sistema debe consultar el estado del evento.
- El evento debe encontrarse en estado `CANCELADO`.
- Las entradas del evento que estén en estado `EMITIDA` deben pasar a `INVALIDADA`.
- Las entradas que ya estén en otros estados no deben modificarse.
- Si el evento no está cancelado, la operación debe rechazarse.
- La operación no debe generar nuevas acciones sobre entradas que ya fueron invalidadas.

## 5. Estados utilizados

### Estados de las entradas

| Estado | Descripción |
|---|---|
| `EMITIDA` | La entrada fue generada y se encuentra emitida. |
| `INVALIDADA` | La entrada fue invalidada por una devolución aprobada o por la cancelación de un evento. |

### Estados de las solicitudes de devolución

| Estado | Descripción |
|---|---|
| `PENDIENTE` | La solicitud fue registrada y todavía no fue resuelta. |
| `APROBADA` | La solicitud fue aprobada por un administrador. |
| `RECHAZADA` | La solicitud fue rechazada por un administrador. |

## 6. Alcance y limitaciones actuales

El microservicio implementa la emisión, consulta, generación de códigos QR, gestión de solicitudes de devolución e invalidación de entradas por cancelación de eventos.

Actualmente se identifican las siguientes limitaciones:

- La invalidación por cancelación de eventos no libera las reservas ni coordina los reintegros correspondientes.
- La consulta individual verifica que la entrada pertenezca al cliente indicado, mientras que el listado recibe el identificador del cliente directamente en la URL.
- La generación y devolución de la imagen QR está implementada, pero no existe en el código revisado una funcionalidad para registrar el uso de una entrada.
- Las operaciones de devolución dependen de la disponibilidad de los microservicios externos de Reservas y Compras para completar las acciones correspondientes.

