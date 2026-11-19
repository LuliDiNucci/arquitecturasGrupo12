# 01 — Funcionalidades y user stories de Fila Virtual

## US01 — Ingresar a una fila virtual

**Como cliente**, quiero ingresar a la fila virtual de un evento de alta demanda, para esperar mi turno antes de realizar una reserva.

**Criterios de aceptación:**

- El cliente debe indicar el evento al que desea ingresar.
- El evento debe existir, estar publicado y estar configurado como de alta demanda.
- El sistema debe registrar el identificador del cliente y del evento.
- Al ingresar por primera vez, el cliente debe quedar en estado `EN_ESPERA`.
- Se debe registrar la fecha y hora de ingreso.
- Un cliente no puede tener más de un registro en la fila del mismo evento.
- Si el cliente intenta ingresar nuevamente, el sistema debe devolver su registro existente.
- Las solicitudes simultáneas del mismo cliente para el mismo evento no deben generar registros duplicados.
- Un cliente puede participar en filas de distintos eventos.
- El ingreso debe realizarse mediante una solicitud autenticada.
- Un cliente no debe poder registrar a otro cliente utilizando una identidad que no le corresponde.

## US02 — Consultar la posición y el estado en la fila

**Como cliente**, quiero consultar mi posición y estado en la fila virtual, para saber cuándo podré realizar una reserva.

**Criterios de aceptación:**

- El cliente debe poder consultar su información mediante el identificador del evento y su identificador de cliente.
- El sistema debe informar el estado actual del cliente.
- Mientras el cliente se encuentre en estado `EN_ESPERA`, debe informarse su posición en la fila.
- La posición debe calcularse considerando los clientes que continúan esperando y que ingresaron anteriormente.
- Los clientes habilitados, finalizados o vencidos no deben ocupar posiciones dentro de la lista de espera.
- La consulta no debe modificar el orden de ingreso.
- Si el cliente no está registrado en la fila del evento, el sistema debe informar que no existe el registro.
- La consulta debe requerir autenticación.
- Un cliente no debe poder consultar la información de otro cliente sin los permisos correspondientes.

## US03 — Habilitar clientes respetando el orden de llegada

**Como administrador**, quiero habilitar una cantidad determinada de clientes de la fila virtual, para controlar el acceso a las reservas de eventos de alta demanda.

**Criterios de aceptación:**

- Se debe indicar el evento y la cantidad de clientes que se desea habilitar.
- La cantidad solicitada debe estar comprendida entre 1 y 100.
- Los clientes deben habilitarse respetando el orden de ingreso a la fila.
- Solo pueden habilitarse clientes que se encuentren en estado `EN_ESPERA`.
- Los clientes seleccionados deben pasar al estado `HABILITADO`.
- Se debe registrar la fecha y hora de habilitación.
- Cada turno habilitado debe tener una vigencia de 10 minutos.
- Si quedan menos clientes esperando que la cantidad solicitada, deben habilitarse únicamente los disponibles.
- No debe permitirse habilitar una nueva tanda mientras exista otra tanda con turnos vigentes.
- Las solicitudes simultáneas de habilitación no deben generar tandas superpuestas.
- La habilitación de tandas debe estar restringida a usuarios con rol `ADMINISTRADOR`.
- Los clientes no deben tener permisos para habilitar tandas.

## US04 — Validar la vigencia de un turno

**Como sistema de Reservas**, quiero verificar si un cliente tiene un turno habilitado y vigente, para determinar si puede continuar con el proceso de reserva de un evento de alta demanda.

**Criterios de aceptación:**

- La validación debe identificar al cliente y al evento.
- El sistema debe comprobar el estado del cliente en la fila.
- Solo debe autorizarse a los clientes que se encuentren en estado `HABILITADO` y cuyo turno no haya vencido.
- Los clientes en estado `EN_ESPERA`, `VENCIDO` o `FINALIZADO` no deben ser autorizados.
- La respuesta debe indicar si el cliente está autorizado, su estado y la fecha de vencimiento del turno.
- Si el cliente no está registrado, debe informarse que el registro no existe.
- La consulta de validación no debe consumir ni finalizar el turno.
- La operación debe estar protegida mediante autenticación interna entre microservicios.
- La validación debe permitir que Reservas determine si corresponde autorizar o rechazar la creación de una reserva temporal.

## US05 — Gestionar el vencimiento de turnos

**Como sistema**, quiero que los turnos habilitados venzan automáticamente al finalizar su período de validez, para evitar que los clientes utilicen autorizaciones fuera del tiempo permitido.

**Criterios de aceptación:**

- Cada turno habilitado debe tener una fecha y hora de vencimiento.
- El período de vigencia debe ser de 10 minutos desde su habilitación.
- Una vez superado ese plazo, el turno debe considerarse vencido.
- Los turnos vencidos deben pasar al estado `VENCIDO`.
- Un turno vencido no debe autorizar nuevas operaciones de reserva.
- El sistema debe verificar el vencimiento cuando se consulta o valida un turno.
- Debe existir un mecanismo automático que actualice periódicamente los turnos vencidos.
- El vencimiento de un turno no debe modificar el estado de otros clientes que continúan esperando.
- Los clientes con turnos vencidos no deben bloquear la habilitación de nuevas tandas.
- Un turno vencido puede ser finalizado mediante una operación interna tardía correspondiente a una reserva creada previamente con autorización vigente.
- La finalización tardía de un turno vencido no debe habilitar nuevas reservas.

## US06 — Finalizar un turno utilizado

**Como sistema de Reservas**, quiero finalizar el turno de un cliente después de crear correctamente su reserva temporal, para impedir que vuelva a utilizar la misma autorización.

La finalización se procesa de forma asíncrona, con reintentos ante fallos temporales de comunicación entre los microservicios.

**Criterios de aceptación:**

- La operación debe identificar al cliente y al evento.
- La finalización debe solicitarse después de crear correctamente la reserva temporal.
- Puede finalizarse un turno que se encuentre en estado `HABILITADO`.
- También puede finalizarse un turno en estado `VENCIDO` cuando Reservas procesa tardíamente la finalización correspondiente a una reserva ya creada.
- No debe permitirse finalizar un turno que todavía se encuentre en estado `EN_ESPERA`.
- La operación de finalización debe estar restringida a la comunicación interna autorizada desde Reservas.
- Al finalizarse, el turno debe pasar al estado `FINALIZADO`.
- Un turno finalizado no debe autorizar nuevas reservas.
- La finalización debe procesarse de forma asíncrona para evitar que los fallos temporales de comunicación con Fila Virtual bloqueen la creación de una reserva válida.
- Si la comunicación con Fila Virtual falla, Reservas debe conservar la operación pendiente y reintentar su procesamiento.
- La finalización debe ser idempotente: si se solicita nuevamente la finalización de un turno ya finalizado, el sistema debe conservar el resultado anterior.
- La finalización de un turno no debe modificar los registros de otros clientes.
- Si el cliente intenta crear otra reserva con una clave de idempotencia diferente utilizando un turno finalizado, el sistema debe rechazar la solicitud.