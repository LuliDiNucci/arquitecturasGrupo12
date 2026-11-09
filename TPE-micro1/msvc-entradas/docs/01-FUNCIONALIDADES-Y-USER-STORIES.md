# 01 — Funcionalidades y user stories de Entradas

## US01 — Emitir entradas para una compra

**Como sistema de TicketYa**, quiero generar las entradas correspondientes a una compra con pago aprobado, para que el cliente pueda obtener los lugares adquiridos.

**Criterios de aceptación:**

- La compra debe existir y tener un pago aprobado.
- La compra debe encontrarse en un estado habilitado para la emisión.
- La reserva asociada debe estar confirmada.
- La reserva debe corresponder al cliente de la compra.
- Debe generarse una entrada por cada lugar adquirido.
- Cada entrada debe tener un código QR único.
- No deben generarse entradas duplicadas para una misma compra.

## US02 — Consultar una entrada individual

**Como cliente**, quiero consultar una entrada mediante su identificador, para visualizar sus datos y su código QR.

**Criterios de aceptación:**

- La entrada debe existir.
- La entrada debe pertenecer al cliente indicado.
- La respuesta debe incluir los datos de la entrada y la imagen QR en Base64.
- Si la entrada no existe, la operación debe rechazarse.
- Si la entrada pertenece a otro cliente, la operación debe rechazarse.

## US03 — Consultar las entradas de un cliente

**Como cliente**, quiero obtener el listado de mis entradas, para visualizar los lugares que tengo adquiridos.

**Criterios de aceptación:**

- El sistema debe devolver las entradas asociadas al identificador del cliente recibido.
- Las entradas deben estar ordenadas desde la más reciente hasta la más antigua.
- Cada entrada debe incluir la información de su código QR.

## US04 — Solicitar la devolución de una entrada

**Como cliente**, quiero solicitar la devolución de una entrada, para iniciar el proceso de reintegro del importe correspondiente.

**Criterios de aceptación:**

- La entrada debe existir.
- La entrada debe encontrarse en estado `EMITIDA`.
- La solicitud debe realizarse al menos 72 horas antes del evento.
- No debe existir otra solicitud pendiente para la misma entrada.
- La solicitud debe registrarse con estado `PENDIENTE`.
- Deben almacenarse el motivo y la fecha de solicitud.

## US05 — Consultar solicitudes pendientes

**Como administrador**, quiero consultar las solicitudes de devolución pendientes, para revisar cuáles necesitan una resolución.

**Criterios de aceptación:**

- El sistema debe devolver las solicitudes cuyo estado sea `PENDIENTE`.
- Cada resultado debe incluir los datos de la solicitud y el identificador de la entrada asociada.

## US06 — Aprobar una devolución

**Como administrador**, quiero aprobar una solicitud de devolución, para invalidar la entrada y solicitar la devolución del lugar y el reintegro correspondiente.

**Criterios de aceptación:**

- La solicitud debe existir.
- La solicitud debe encontrarse pendiente o haber sido aprobada previamente para permitir reintentar las comunicaciones externas.
- La solicitud debe quedar en estado `APROBADA`.
- Deben registrarse el administrador y la fecha de resolución.
- La entrada debe quedar en estado `INVALIDADA`.
- Debe solicitarse a Reservas la devolución del lugar.
- Debe solicitarse a Compras el reintegro correspondiente.
- Una solicitud rechazada no puede aprobarse.

## US07 — Rechazar una devolución

**Como administrador**, quiero rechazar una solicitud de devolución, para impedir que se realice el reintegro solicitado.

**Criterios de aceptación:**

- La solicitud debe existir.
- La solicitud debe encontrarse en estado `PENDIENTE`.
- Debe cambiar a estado `RECHAZADA`.
- Deben registrarse el administrador y la fecha de resolución.
- La entrada debe conservar su estado `EMITIDA`.
- No deben realizarse llamadas a los servicios de liberación de lugares ni de reintegros.

## US08 — Invalidar entradas por cancelación de un evento

**Como sistema de TicketYa**, quiero invalidar las entradas de un evento cancelado, para impedir que las entradas emitidas sigan considerándose válidas.

**Criterios de aceptación:**

- El sistema debe consultar el estado del evento.
- El evento debe encontrarse en estado `CANCELADO`.
- Las entradas del evento que estén en estado `EMITIDA` deben pasar a `INVALIDADA`.
- Las entradas que ya estén en otros estados no deben modificarse.
- Si el evento no está cancelado, la operación debe rechazarse.
- La operación no debe generar nuevas acciones sobre entradas que ya fueron invalidadas.