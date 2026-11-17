# 01 — Funcionalidades y user stories de Reservas

## US01 — Consultar disponibilidad de entradas

**Como cliente**, quiero consultar los asientos o lugares disponibles de un sector para un evento, para poder elegir dónde realizar mi reserva.

**Criterios de aceptación:**

- El cliente debe seleccionar un evento y un sector.
- El sistema debe verificar que el evento y el sector existan y que el sector corresponda al lugar del evento.
- Si el sector es numerado, el sistema debe mostrar los asientos disponibles y ocupados.
- Si el sector no es numerado, debe mostrar la cantidad de lugares disponibles.
- No se deben ofrecer como disponibles los lugares asociados a reservas activas vigentes ni a reservas confirmadas.
- Las reservas vencidas o liberadas no deben bloquear lugares.
- La disponibilidad debe corresponder al evento y sector seleccionados.

## US02 — Realizar una reserva temporal

**Como cliente**, quiero reservar temporalmente uno o varios lugares de un evento, para asegurar su disponibilidad mientras realizo el pago.

**Criterios de aceptación:**

- El cliente debe seleccionar un evento, un sector y la cantidad de entradas.
- Si el sector es numerado, debe seleccionar los asientos específicos.
- Solo se pueden reservar lugares disponibles.
- La reserva debe tener una vigencia de 10 minutos desde su creación.
- Una reserva activa y vigente debe bloquear temporalmente los lugares seleccionados.
- El sistema debe registrar el cliente, el evento, los detalles de los sectores y lugares reservados y la fecha de vencimiento.
- El evento debe encontrarse habilitado para la venta.
- Se debe respetar el límite máximo de entradas por cliente para un mismo evento.

## US03 — Evitar reservas simultáneas del mismo lugar

**Como cliente**, quiero que los lugares seleccionados queden protegidos frente a reservas simultáneas, para asegurar que mi reserva no se superponga con la de otro cliente.

**Criterios de aceptación:**

- Un asiento no puede estar asociado a dos reservas activas y vigentes para el mismo evento.
- En sectores no numerados, no se puede superar la capacidad máxima disponible.
- Si dos clientes intentan reservar simultáneamente el mismo asiento, solo uno debe conseguirlo.
- El sistema debe informar cuando no se pueda concretar una reserva por falta de disponibilidad.
- El control de concurrencia debe impedir la doble reserva incluso cuando existan varias instancias del microservicio.
- Las solicitudes repetidas con la misma clave de idempotencia y los mismos datos deben recuperar la reserva original, evitando duplicaciones.

## US04 — Liberar lugares de reservas vencidas

**Como sistema**, quiero que los lugares de una reserva vuelvan a estar disponibles cuando expire su tiempo de validez, para permitir que otros clientes puedan reservarlos.

**Criterios de aceptación:**

- Cada reserva debe registrar una fecha y hora de vencimiento.
- Las reservas temporales deben tener una vigencia de 10 minutos desde su creación.
- Una reserva en estado `ACTIVA` cuya fecha de vencimiento haya transcurrido debe dejar de bloquear lugares.
- Los asientos o lugares asociados a una reserva vencida deben volver a considerarse disponibles.
- Una reserva vencida no debe poder confirmarse posteriormente.
- El cálculo de disponibilidad debe excluir las reservas activas vencidas.
- El vencimiento no debe afectar las reservas que ya se encuentren en estado `CONFIRMADA`.

## US05 — Confirmar una reserva después del pago

**Como cliente**, quiero que mi reserva quede confirmada cuando se apruebe el pago, para asegurar los lugares que compré.

**Criterios de aceptación:**

- El microservicio Compras y Pagos debe solicitar la confirmación de la reserva después de aprobar el pago.
- Solo se puede confirmar una reserva activa y vigente.
- El estado de la reserva debe cambiar a `CONFIRMADA`.
- Los lugares confirmados deben permanecer ocupados y no volver a ofrecerse para nuevas reservas.
- El microservicio Entradas debe poder emitir las entradas correspondientes a la reserva confirmada.
- Una confirmación repetida no debe generar ventas ni confirmaciones duplicadas.

## US06 — Liberar una reserva por pago rechazado

**Como cliente**, quiero que los lugares reservados se liberen cuando mi pago sea rechazado, para que vuelvan a estar disponibles para otros compradores.

**Criterios de aceptación:**

- El microservicio Compras y Pagos debe solicitar la liberación de la reserva cuando el pago sea rechazado.
- La reserva debe cambiar al estado `LIBERADA`.
- Los lugares asociados deben volver a considerarse disponibles si el evento continúa habilitado para la venta.
- No se deben emitir entradas asociadas a una compra cuyo pago fue rechazado.
- La liberación debe ser idempotente, evitando efectos duplicados ante solicitudes repetidas.
- Una reserva confirmada no debe liberarse mediante el flujo correspondiente a un pago rechazado.

## US07 — Liberar lugares por devolución o cancelación del evento

**Como sistema**, quiero actualizar la disponibilidad de los lugares cuando se aprueba una devolución o se cancela un evento, para mantener la consistencia de las reservas.

**Criterios de aceptación:**

- Cuando se aprueba la devolución de una entrada, el microservicio Entradas debe solicitar a Reservas la devolución del lugar correspondiente.
- El asiento o lugar devuelto debe liberarse si el evento continúa habilitado para la venta.
- No se debe liberar un lugar por una solicitud de devolución rechazada.
- Si el evento se cancela, no deben permitirse nuevas reservas.
- Las reservas activas de un evento cancelado deben invalidarse para impedir su posterior confirmación.
- Los lugares de un evento cancelado no deben volver a ofrecerse para la venta.
- La devolución de un lugar y la cancelación de las reservas deben ser idempotentes, evitando duplicaciones ante solicitudes repetidas.