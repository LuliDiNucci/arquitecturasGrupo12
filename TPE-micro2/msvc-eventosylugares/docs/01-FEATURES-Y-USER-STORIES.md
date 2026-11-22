# Funcionalidades y user stories — Eventos y Lugares

## 1. Responsabilidad del microservicio

Eventos y Lugares administra los espectáculos publicados en TicketYa, sus organizadores, participantes, lugares, sectores, asientos y precios.

También gestiona el ciclo de vida de los eventos, ofrece búsquedas públicas, genera reportes para organizadores y coordina notificaciones relacionadas con publicaciones, modificaciones y cancelaciones.

## 2. Funcionalidades e historias de usuario

### US01 — Registrar lugares

**Como administrador**, quiero registrar lugares con su dirección, ciudad y coordenadas, para utilizarlos en la organización de eventos.

**Criterios de aceptación:**
- Los datos obligatorios y las coordenadas deben ser válidos.
- El lugar creado recibe un identificador único.
- Los lugares registrados pueden consultarse mediante la API.

### US02 — Administrar sectores

**Como administrador**, quiero definir sectores numerados o de capacidad general dentro de un lugar.

**Criterios de aceptación:**
- Cada sector pertenece a un lugar existente y tiene capacidad positiva.
- No pueden repetirse nombres de sectores dentro del mismo lugar.
- No se permite modificar la estructura de sectores de un lugar con eventos activos.

### US03 — Registrar asientos

**Como administrador**, quiero identificar los asientos de los sectores numerados mediante fila y número.

**Criterios de aceptación:**
- Los asientos solo pueden registrarse en sectores numerados.
- No se permiten posiciones duplicadas ni superar la capacidad del sector.
- Los sectores numerados deben tener todos sus asientos registrados antes de publicar un evento.

### US04 — Registrar organizadores y participantes

**Como administrador**, quiero registrar organizadores y participantes para asociarlos a los eventos.

**Criterios de aceptación:**
- Los registros deben cumplir las validaciones de sus datos.
- Los participantes tienen un tipo definido.
- No pueden asociarse participantes inexistentes o duplicados a un mismo evento.

### US05 — Crear eventos

**Como organizador**, quiero crear eventos en estado `BORRADOR` para configurar su información antes de publicarlos.

**Criterios de aceptación:**
- Deben indicarse nombre, descripción, tipo, organizador, lugar, fecha y participantes.
- El organizador debe estar autorizado para administrar el evento.
- Los borradores no aparecen en las búsquedas públicas.

### US06 — Configurar precios

**Como organizador**, quiero establecer precios por sector para cada evento.

**Criterios de aceptación:**
- Los precios se configuran mientras el evento está en `BORRADOR`.
- Cada sector del lugar debe tener un precio válido.
- Un mismo sector puede tener precios diferentes en distintos eventos.

### US07 — Publicar eventos

**Como organizador**, quiero publicar un evento cuando su configuración esté completa.

**Criterios de aceptación:**
- La fecha debe ser futura.
- Deben existir participantes, sectores, precios y asientos requeridos.
- La publicación habilita el evento para las operaciones de reserva.
- Una configuración incompleta impide la publicación.

### US08 — Modificar eventos

**Como organizador**, quiero actualizar los datos permitidos de mis eventos.

**Criterios de aceptación:**
- Un borrador admite cambios en su configuración.
- En eventos publicados, agotados o suspendidos pueden modificarse nombre, descripción, tipo y participantes.
- No se permite cambiar fecha, lugar, organizador o condición de alta demanda una vez publicado.
- Las modificaciones relevantes generan avisos para los compradores.
- Los eventos cancelados o finalizados no pueden modificarse.

### US09 — Buscar eventos

**Como cliente**, quiero buscar espectáculos por distintos criterios.

**Criterios de aceptación:**
- Se pueden combinar filtros por tipo, organizador, participante, ciudad, fechas y texto.
- Los resultados pueden ordenarse por fecha, precio o relevancia.
- Las consultas públicas muestran eventos `PUBLICADO` y `AGOTADO`.
- Los administradores pueden consultar también los demás estados.

### US10 — Consultar información para otros servicios

**Como microservicio Reservas o Compras**, quiero consultar eventos, sectores y precios para validar operaciones.

**Criterios de aceptación:**
- La API permite consultar la configuración y el estado del evento.
- Un evento solo está habilitado para nuevas reservas cuando está `PUBLICADO` y su fecha es futura.
- La ocupación real y el stock disponible son responsabilidad de Reservas.

### US11 — Suspender y reactivar eventos

**Como administrador**, quiero suspender temporalmente un evento y permitir su posterior reactivación.

**Criterios de aceptación:**
- Pueden suspenderse eventos publicados o agotados.
- Mientras estén suspendidos no admiten nuevas reservas.
- Para republicarlos se valida nuevamente su configuración.
- El sistema conserva el estado anterior cuando corresponde.

### US12 — Cancelar eventos

**Como administrador**, quiero cancelar un evento y comunicarlo a los servicios involucrados.

**Criterios de aceptación:**
- El evento pasa a `CANCELADO` y deja de admitir reservas.
- Se registra persistentemente la operación de cancelación.
- Reservas, Entradas y Compras reciben las comunicaciones correspondientes.
- Los avisos pueden reintentarse y la cancelación repetida no duplica sus efectos.

### US13 — Vincular organizadores con usuarios

**Como administrador**, quiero asociar un organizador con una cuenta de usuario para controlar quién puede gestionar sus eventos.

**Criterios de aceptación:**
- El usuario debe existir, estar activo y tener rol `ORGANIZADOR`.
- La validación se realiza mediante el microservicio Usuarios.
- Un organizador solamente puede administrar sus propios eventos.
- Un administrador puede gestionar cualquier evento.

### US14 — Notificar publicaciones y cambios

**Como cliente**, quiero recibir avisos relacionados con eventos de mi interés.

**Criterios de aceptación:**
- Al publicar un evento se pueden notificar artistas favoritos que actúen en la ciudad del cliente.
- Se consulta a Recomendaciones para identificar clientes interesados.
- Los compradores reciben avisos por modificaciones y cancelaciones.
- Los avisos pendientes por cambios y cancelaciones se conservan para su procesamiento posterior.

### US15 — Consultar reportes

**Como organizador**, quiero conocer las ventas y la ocupación de mis eventos.

**Criterios de aceptación:**
- Se ofrecen reportes generales, por sector, de recaudación y de ocupación.
- Los resultados incluyen ventas, devoluciones, importes y porcentajes de ocupación.
- Los datos se obtienen mediante integración con Compras y Reservas.
- Solo el administrador o el organizador propietario pueden consultar estos reportes.

## 3. Reglas generales

- Los estados de un evento son `BORRADOR`, `PUBLICADO`, `AGOTADO`, `SUSPENDIDO`, `CANCELADO` y `FINALIZADO`.
- Los eventos de alta demanda se identifican mediante `altaDemanda`, utilizado por la integración con Fila Virtual.
- Eventos y Lugares administra la configuración de los espacios; Reservas controla su ocupación.
- Las operaciones administrativas y de organizadores requieren autenticación y autorización mediante JWT.