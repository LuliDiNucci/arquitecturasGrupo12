# Funcionalidades e historias de usuario — Notificaciones

## Descripción

El microservicio de Notificaciones registra y envía correos electrónicos relacionados con las operaciones de TicketYa.

Recibe solicitudes de otros microservicios, las almacena y procesa su envío en segundo plano. De esta manera, el envío de un correo no requiere mantener abierta la operación que lo originó.

## Funcionalidades

- Notificar compras confirmadas.
- Informar cancelaciones de eventos.
- Comunicar modificaciones de eventos.
- Avisar sobre eventos de artistas favoritos en la ciudad del cliente.
- Consultar el estado de una notificación.
- Reintentar envíos fallidos y evitar registros duplicados.
- Restringir el acceso mediante JWT.

## Historias de usuario

### US-01 — Confirmación de compra

**Como** cliente, **quiero** recibir un correo cuando se confirme mi compra **para** tener constancia de la operación.

**Criterios de aceptación:**

- Se registra una notificación asociada a la compra y al cliente.
- El correo informa la confirmación y el nombre del evento.
- La solicitud repetida no genera otra notificación para la misma compra y cliente.

### US-02 — Cambios y cancelaciones

**Como** cliente, **quiero** recibir avisos sobre cambios o cancelaciones **para** conocer las novedades de los eventos adquiridos.

**Criterios de aceptación:**

- Las cancelaciones generan un aviso asociado al evento y al cliente.
- Las modificaciones incluyen una descripción del cambio.
- La versión del evento permite distinguir modificaciones diferentes.
- Se evitan notificaciones duplicadas.

### US-03 — Artistas favoritos

**Como** cliente, **quiero** recibir avisos de eventos de mis artistas favoritos en mi ciudad **para** enterarme de nuevas presentaciones.

**Criterios de aceptación:**

- El artista debe figurar entre los favoritos del cliente.
- La ciudad del evento debe coincidir con la ciudad configurada.
- Si no se cumplen las condiciones, no se registra la notificación.
- Se registra como máximo una notificación de este tipo por evento y cliente.

### US-04 — Envío y seguimiento

**Como** sistema, **quiero** procesar las notificaciones de manera independiente **para** administrar reintentos y registrar el resultado.

**Criterios de aceptación:**

- Las solicitudes se almacenan inicialmente como `PENDIENTE`.
- Un proceso programado realiza los envíos mediante SMTP.
- Los correos enviados pasan a `ENVIADA`.
- Los fallos se reintentan hasta alcanzar el máximo configurado.
- Al agotarse los intentos, el estado pasa a `FALLIDA`.