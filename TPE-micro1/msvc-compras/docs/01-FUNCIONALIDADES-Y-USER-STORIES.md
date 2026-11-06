# 01 — Funcionalidades y user stories de Compras y Pagos

**Fuente:** historias de usuario provistas por el grupo para TicketYa, 2026. **Estado:** especificación, no implementación.

## US01 — Iniciar una compra

**Como cliente**, quiero iniciar la compra de las entradas que reservé, para poder realizar el pago y adquirirlas.

**Criterios de aceptación:**
- El cliente debe tener una reserva temporal vigente.
- El sistema debe consultar al microservicio Reservas para verificar que la reserva exista y esté activa.
- Se debe registrar la compra asociada al cliente y a la reserva.
- Se debe calcular el importe total según las entradas reservadas y los precios correspondientes.
- La compra debe crearse en estado `PENDIENTE`.
- No se debe permitir iniciar una compra sobre una reserva vencida o cancelada.
- Una misma reserva no debe generar múltiples compras activas.

## US02 — Registrar el detalle de una compra

**Como cliente**, quiero que se registre el detalle de las entradas que estoy comprando, para conocer el importe y los lugares seleccionados.

**Criterios de aceptación:**
- Cada compra debe tener uno o varios detalles.
- Cada detalle debe identificar el evento, sector y cantidad de entradas.
- Si corresponde, deben registrarse los asientos seleccionados.
- Se debe registrar el precio unitario correspondiente al momento de iniciar la compra.
- El importe total debe coincidir con la suma de los subtotales.
- El detalle debe conservarse para futuras consultas y devoluciones.

## US03 — Procesar el pago de una compra

**Como cliente**, quiero pagar las entradas que reservé mediante un medio de pago disponible, para confirmar mi compra.

**Criterios de aceptación:**
- El cliente debe seleccionar un medio de pago.
- El sistema debe registrar el medio de pago utilizado, sin almacenar datos sensibles como el número completo de tarjeta.
- El pago debe procesarse mediante una pasarela de pagos simulada (mock).
- El sistema debe registrar el importe, la fecha y el resultado del pago.
- Si el pago es aprobado, debe registrarse como `APROBADO`.
- Si es rechazado, debe registrarse como `RECHAZADO`.
- No se debe procesar el pago si la reserva ya está vencida o cancelada.

## US04 — Confirmar una compra aprobada

**Como cliente**, quiero que mi compra se confirme cuando el pago sea aprobado, para obtener mis entradas.

**Criterios de aceptación:**
- El pago debe encontrarse aprobado.
- La compra debe pasar al estado `CONFIRMADA`.
- El microservicio Reservas debe confirmar los lugares reservados.
- El microservicio Entradas debe emitir una entrada por cada lugar adquirido, con su QR único.
- La compra debe quedar asociada a las entradas emitidas.
- No se deben generar entradas duplicadas ante solicitudes repetidas.
- Si no se logra completar la confirmación de la reserva o la emisión de entradas, la compra no debe quedar registrada como completada exitosamente sin resolver esa situación.

## US05 — Gestionar un pago rechazado

**Como cliente**, quiero recibir información cuando mi pago sea rechazado, para saber que la compra no pudo completarse.

**Criterios de aceptación:**
- Cuando la pasarela simulada rechace el pago, debe registrarse el resultado.
- La compra debe pasar al estado `RECHAZADA`.
- El microservicio Reservas debe recibir la solicitud para liberar los lugares reservados.
- Los lugares deben volver a estar disponibles si no fueron vendidos y el evento continúa habilitado.
- No se deben emitir entradas.
- El cliente debe recibir una respuesta indicando que la operación fue rechazada.

## US06 — Evitar cobros duplicados

**Como cliente**, quiero que los reintentos de pago no generen cobros duplicados, para no pagar dos veces por las mismas entradas.

**Criterios de aceptación:**
- Cada operación de pago debe contar con una clave de idempotencia.
- Si el cliente repite una solicitud con la misma clave y los mismos datos, no debe generarse un nuevo cobro.
- El sistema debe recuperar el resultado de la operación original.
- Si se reutiliza la misma clave con datos diferentes, debe rechazarse la solicitud.
- Una compra confirmada no debe volver a cobrarse.
- Las solicitudes simultáneas del mismo pago no deben producir múltiples cobros.

## US07 — Procesar el reintegro de una entrada devuelta

**Como cliente**, quiero recibir el reintegro de mi dinero cuando se aprueba la devolución de una entrada, para recuperar el importe correspondiente.

**Criterios de aceptación:**
- El microservicio Entradas debe informar que una devolución fue aprobada.
- Debe verificarse que la entrada corresponda a una compra pagada.
- El sistema debe calcular el importe a reintegrar según el precio efectivamente abonado por esa entrada.
- Se debe solicitar el reintegro a la pasarela de pagos simulada.
- El reintegro debe registrar importe, fecha y estado.
- Una misma devolución no debe generar reintegros duplicados.
- Si la compra contiene varias entradas, debe poder reintegrarse solamente la entrada devuelta.

## US08 — Gestionar reintegros por cancelación de un evento

**Como administrador**, quiero que se gestionen los reintegros de las entradas de un evento cancelado, para devolver el dinero a los compradores afectados.

**Criterios de aceptación:**
- El microservicio debe recibir la información de que un evento fue cancelado.
- Se deben identificar las compras pagadas correspondientes al evento.
- Se debe determinar qué importes corresponde reintegrar, evitando reintegrar nuevamente entradas ya devueltas.
- Se debe solicitar el reintegro de cada importe a la pasarela simulada.
- Se debe registrar el estado de cada reintegro.
- No deben procesarse reintegros duplicados si la cancelación se informa más de una vez.
- La compra debe reflejar si sus entradas fueron reintegradas total o parcialmente.

## Correspondencia con la consigna

- Funcionalidad 4: reserva temporal, pago, detalle de compra, medio de pago y emisión de QR.
- Funcionalidad 5: no doble venta ni cobros duplicados.
- Funcionalidad 7: devolución aprobada, reintegro y cancelación de eventos.
- Entrega 1: pasarela simulada, API REST, bases separadas, Swagger, README, documentos y pruebas unitarias/concurrencia.
- Entrega 2: JWT, Docker, log centralizado, resiliencia avanzada y estrategias completas de consistencia/compensación. No obstante, para US04 ya debe evitarse marcar como exitosa una compra incompleta.

## Regla de trabajo

No marcar una historia como «implementada y probada» solo por existir un controller: registrar evidencia en `05-TESTING.md`, y actualizar el README y los contratos tras cada cambio.
