# 01 — Funcionalidades y user stories de Eventos y Lugares

## US01 — Crear un lugar

**Como administrador**, quiero registrar un lugar con su nombre, dirección, ciudad y coordenadas geográficas, para utilizarlo al crear eventos.

**Criterios de aceptación:**

- El lugar debe registrarse con un nombre, dirección, ciudad y coordenadas válidas.
- Cuando se crea correctamente, el sistema debe responder `201 Created` y devolver su identificador.
- Si las coordenadas están fuera de rango o existen campos obligatorios vacíos, debe responder `400 Bad Request`.
- Se debe permitir consultar un lugar existente, devolviendo sus datos con `200 OK`.
- Si el lugar solicitado no existe, debe responder `404 Not Found`.

## US02 — Crear sectores

**Como administrador u organizador**, quiero definir sectores numerados o de capacidad general dentro de un lugar, para organizar los espacios disponibles para los eventos.

**Criterios de aceptación:**

- El sector debe estar asociado a un lugar existente.
- Debe indicarse si el sector es numerado y establecerse una capacidad positiva.
- No se deben permitir sectores con el mismo nombre dentro de un lugar.
- Si se intenta repetir un nombre de sector en el mismo lugar, debe responder `409 Conflict`.
- No se deben permitir nuevos sectores cuando el lugar tenga eventos publicados, agotados o suspendidos.

## US03 — Registrar asientos numerados

**Como administrador**, quiero registrar la fila y el número de cada asiento de un sector numerado, para identificar los lugares disponibles.

**Criterios de aceptación:**

- Los asientos deben pertenecer a un sector numerado.
- Al registrar correctamente una lista de asientos, deben generarse identificadores únicos y responder `201 Created`.
- No se debe permitir repetir la combinación de fila y número dentro del mismo sector, sin distinguir mayúsculas y minúsculas.
- Los asientos duplicados deben rechazarse con `409 Conflict`.
- No se deben registrar asientos en sectores no numerados, respondiendo `400 Bad Request`.
- No debe permitirse superar la capacidad máxima del sector, respondiendo `409 Conflict`.

## US04 — Registrar organizadores y participantes

**Como organizador**, quiero registrar organizadores y participantes, para asociarlos a los eventos correspondientes.

**Criterios de aceptación:**

- El sistema debe permitir registrar organizadores y participantes con nombres válidos.
- Los participantes deben tener un tipo válido según los valores definidos por el sistema.
- Al crear un evento, los identificadores de organizador y participantes deben existir.
- No se debe permitir asociar participantes repetidos a un mismo evento.

## US05 — Crear un evento en borrador

**Como organizador**, quiero crear un evento en estado borrador, para configurar sus datos antes de habilitar la venta de entradas.

**Criterios de aceptación:**

- El evento debe contener nombre, descripción, tipo, organizador, participantes, lugar y fecha.
- Al crearse correctamente, debe registrarse en estado `BORRADOR`.
- La creación exitosa debe responder `201 Created`.
- Los eventos en estado `BORRADOR` no deben aparecer en las búsquedas públicas por defecto.

## US06 — Configurar precios por sector

**Como organizador**, quiero definir el precio de cada sector para un evento, para establecer el valor de las entradas disponibles.

**Criterios de aceptación:**

- Los precios deben configurarse mientras el evento se encuentre en estado `BORRADOR`.
- Debe definirse exactamente un precio por cada sector del lugar asociado al evento.
- Los precios deben ser mayores o iguales a cero.
- No debe permitirse repetir un mismo sector dentro de una solicitud de configuración.
- Un mismo sector puede tener diferentes precios para distintos eventos.
- Los precios deben asociarse mediante la relación entre evento y sector.
- No se deben permitir modificaciones de precios cuando el evento no esté en estado `BORRADOR`, respondiendo `409 Conflict`.

## US07 — Publicar un evento

**Como organizador**, quiero publicar un evento cuando su configuración esté completa, para habilitar la venta de entradas.

**Criterios de aceptación:**

- El evento debe encontrarse en estado `BORRADOR` o `SUSPENDIDO`.
- La fecha del evento debe ser futura.
- El evento debe tener participantes, sectores y precios correctamente configurados.
- Los sectores numerados deben contar con sus asientos registrados.
- Cuando se cumplen las condiciones, el evento debe pasar a estado `PUBLICADO` o recuperar el estado `AGOTADO` cuando corresponda.
- Si faltan participantes, sectores, precios o asientos obligatorios, debe responder `409 Conflict`.
- Si la fecha del evento ya pasó, debe responder `409 Conflict`.
- Ante un error de validación, el estado del evento no debe modificarse.

## US08 — Modificar un evento

**Como organizador**, quiero modificar la información de un evento, para mantener sus datos actualizados sin afectar las reservas existentes.

**Criterios de aceptación:**

- Los eventos en estado `BORRADOR` deben permitir modificar nombre, descripción, tipo, fecha, lugar, organizador y participantes.
- Los eventos en estado `PUBLICADO`, `AGOTADO` o `SUSPENDIDO` deben permitir modificar nombre, descripción, tipo y participantes.
- En los estados `PUBLICADO`, `AGOTADO` o `SUSPENDIDO` no se debe permitir modificar fecha, lugar, organizador ni la propiedad `altaDemanda`.
- Los eventos en estado `CANCELADO` o `FINALIZADO` no deben permitir modificaciones, respondiendo `409 Conflict`.

## US09 — Buscar eventos por múltiples criterios

**Como cliente**, quiero buscar eventos por tipo, organizador, participante, ciudad, rango de fechas y texto, para encontrar eventos de mi interés.

**Criterios de aceptación:**

- Los filtros deben ser opcionales y permitir combinarse entre sí.
- El sistema debe permitir ordenar los resultados por fecha, precio o relevancia.
- Por defecto, las búsquedas públicas deben incluir únicamente eventos en estado `PUBLICADO` o `AGOTADO`.
- Los eventos en estado `BORRADOR`, `SUSPENDIDO`, `CANCELADO` o `FINALIZADO` no deben aparecer en las búsquedas públicas por defecto.
- Para ordenar por precio, debe utilizarse el menor precio configurado para el evento, identificado como `precioDesde`.
- El valor `precioDesde` representa el menor precio configurado y no la disponibilidad real de entradas.
- Si la fecha inicial del rango es posterior a la fecha final, debe responder `400 Bad Request`.
- El parámetro `incluirNoPublicados=true` debe permitir incluir otros estados en las consultas correspondientes.

## US10 — Consultar información de eventos para Reservas y Compras

**Como microservicio Reservas o Compras**, quiero consultar la información de un evento, sus sectores y sus precios, para validar las operaciones de reserva y compra.

**Criterios de aceptación:**

- El sistema debe permitir consultar la información de un evento mediante su identificador.
- `GET /eventos/{id}/habilitado` debe indicar si el evento permite realizar nuevas reservas.
- Un evento debe estar en estado `PUBLICADO` y tener fecha futura para estar habilitado para reservar.
- `GET /eventos/{id}/sectores` debe devolver la configuración y los precios de los sectores correspondientes.
- La información de los sectores no debe representar la ocupación ni el stock restante de lugares.
- Reservas debe validar la existencia y correspondencia del evento, sector y asiento antes de crear una reserva.
- La disponibilidad y ocupación de lugares deben ser administradas por el microservicio Reservas.

## US11 — Suspender un evento

**Como administrador**, quiero suspender un evento publicado o agotado, para impedir que se realicen nuevas reservas mientras se encuentre suspendido.

**Criterios de aceptación:**

- Se debe permitir suspender eventos en estado `PUBLICADO` o `AGOTADO`.
- Al suspenderse, el evento debe pasar a estado `SUSPENDIDO`.
- Un evento suspendido debe informar `habilitadoParaReservar=false`.
- La republicación debe validar nuevamente la configuración completa del evento.
- Si cumple las condiciones de publicación, debe recuperar el estado `PUBLICADO` o `AGOTADO`, según corresponda.

## US12 — Cancelar un evento y notificar a otros microservicios

**Como administrador**, quiero cancelar un evento y comunicar la cancelación a los demás microservicios, para detener las ventas y gestionar las reservas, entradas y reintegros correspondientes.

**Criterios de aceptación:**

- Al cancelar un evento, su estado debe cambiar a `CANCELADO` y persistirse en la base de datos.
- Un evento cancelado no debe permitir nuevas reservas.
- La actualización del estado y la creación de la notificación de cancelación deben realizarse dentro de la misma transacción.
- La cancelación debe comunicarse a Reservas, Entradas y Compras mediante las integraciones configuradas.
- Las notificaciones deben poder reintentarse cuando alguna comunicación no se completa.
- Repetir la cancelación del mismo evento no debe generar una segunda notificación.
- Las solicitudes de cancelación deben incluir el identificador de operación `X-Operacion-Id`.
- Los servicios receptores deben evitar duplicar los efectos de una cancelación repetida.
- Eventos y Lugares debe coordinar las notificaciones, mientras que Reservas administra sus lugares, Entradas invalida las entradas y Compras procesa los reintegros correspondientes.