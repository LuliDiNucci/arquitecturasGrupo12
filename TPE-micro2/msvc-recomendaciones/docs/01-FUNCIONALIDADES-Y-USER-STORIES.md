# Funcionalidades e historias de usuario — Recomendaciones

## Descripción

El microservicio de Recomendaciones administra las preferencias de los clientes y sugiere eventos de TicketYa de acuerdo con sus intereses y el historial de compras.

Las preferencias se almacenan en MongoDB. Los eventos y las sugerencias basadas en compras se obtienen mediante consultas REST a otros microservicios.

## Funcionalidades

- Registrar, consultar y modificar preferencias de clientes.
- Guardar tipos de eventos preferidos, artistas favoritos y ciudad de interés.
- Recomendar eventos publicados y futuros cuando coincida al menos uno de los siguientes criterios:
  - Tipo de evento.
  - Ciudad.
  - Artista participante.
  - Sugerencia recibida desde Compras según el historial de compras confirmadas.
- Identificar clientes interesados en un artista y una ciudad para las notificaciones de eventos.
- Proteger la información mediante autenticación JWT y control de acceso por cliente.

## Historias de usuario

### US-01 — Administrar preferencias

**Como** cliente, **quiero** registrar y actualizar mis preferencias **para** recibir recomendaciones relacionadas con mis intereses.

**Criterios de aceptación:**

- Debe indicarse al menos un tipo de evento y una ciudad.
- La lista de artistas favoritos puede estar vacía, pero no ser nula.
- Las preferencias se almacenan en MongoDB.
- No se permite registrar preferencias nuevamente si el cliente ya posee un registro.
- El cliente solo puede acceder a sus propios datos; el administrador puede gestionar los de otros clientes.

### US-02 — Consultar recomendaciones

**Como** cliente, **quiero** obtener sugerencias de eventos **para** descubrir espectáculos que puedan interesarme.

**Criterios de aceptación:**

- Se consideran únicamente eventos en estado `PUBLICADO` y con fecha futura.
- Un evento se recomienda si coincide con al menos uno de los criterios disponibles.
- Los eventos se devuelven una sola vez, aunque coincidan con varios criterios.
- Si no hay coincidencias, se devuelve una lista vacía.
- Si el cliente no tiene preferencias registradas, se devuelve `404`.

### US-03 — Incorporar historial de compras

**Como** cliente, **quiero** recibir recomendaciones basadas en compras anteriores **para** descubrir otros eventos.

**Criterios de aceptación:**

- Recomendaciones consulta los identificadores sugeridos por Compras y Pagos.
- Las sugerencias se combinan con las coincidencias por preferencias.
- Si Compras no responde por un error de servidor, se mantienen las recomendaciones que puedan obtenerse por preferencias.
- El resultado conserva el filtro de eventos publicados y futuros.

### US-04 — Identificar clientes interesados

**Como** sistema, **quiero** identificar clientes interesados en un artista de una ciudad determinada **para** facilitar el envío de avisos de nuevos eventos.

**Criterios de aceptación:**

- Deben coincidir el nombre del artista favorito y la ciudad del cliente con los datos consultados.
- Las comparaciones ignoran diferencias entre mayúsculas y minúsculas y espacios al inicio o al final.
- Se devuelven identificadores de clientes sin duplicados.
- La consulta está restringida al microservicio autorizado de Eventos y Lugares.