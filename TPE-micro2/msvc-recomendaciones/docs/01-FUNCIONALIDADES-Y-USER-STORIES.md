# 01 — Funcionalidades y User Stories

## 1. Descripción general

El microservicio `msvc-recomendaciones` administra las preferencias de los clientes y sugiere eventos de TicketYa combinando coincidencias explícitas con sugerencias colaborativas basadas en compras confirmadas.

Las preferencias se almacenan en MongoDB. La información de eventos y compras se consulta a sus respectivos microservicios mediante Spring Cloud OpenFeign. Las operaciones externas se exponen por el API Gateway y requieren JWT.

## 2. Funcionalidades

### Preferencias

- Registrar los tipos de eventos preferidos, artistas favoritos y ciudad de interés de un cliente.
- Consultar las preferencias registradas.
- Modificar las preferencias existentes.
- Mantener un único registro de preferencias por cliente; rechazar un registro duplicado.

### Recomendaciones

Para cada cliente se consultan sus preferencias y se evalúan eventos **PUBLICADOS** con fecha futura. Un evento puede recomendarse cuando se cumple al menos uno de estos criterios:

- Coincide el tipo de evento.
- Coincide la ciudad.
- Alguno de sus participantes coincide con un artista favorito.
- Su identificador figura entre las sugerencias derivadas del historial de compras confirmadas.

Para el criterio colaborativo, Compras identifica clientes que adquirieron entradas para eventos comprados por el cliente consultado y obtiene otros eventos que esos compradores también adquirieron. Se excluyen de las sugerencias de historial los eventos ya comprados por el cliente. El resultado final no duplica eventos que coincidan por varios criterios.

La consulta devuelve una lista vacía si no hay coincidencias. Si no existen preferencias registradas, se informa que el recurso no existe. La versión implementada necesita preferencias registradas incluso cuando las sugerencias provienen del historial.

## 3. User Stories

### US-01 — Registrar preferencias

**Como** cliente, **quiero** registrar mis intereses **para** recibir recomendaciones personalizadas.

**Criterios de aceptación:**

- Se registran tipos de eventos, artistas favoritos y ciudad asociados al identificador del cliente.
- Los tipos de evento no pueden estar vacíos; la lista de artistas puede estar vacía, pero no ser nula; la ciudad es obligatoria.
- Se persisten las preferencias en MongoDB.
- No se permite un segundo registro para el mismo cliente.
- El cliente debe autenticarse y no puede registrar preferencias para otra persona; un administrador puede acceder a otros clientes.

### US-02 — Consultar preferencias

**Como** cliente, **quiero** consultar mis preferencias **para** conocer los criterios de recomendación.

**Criterios de aceptación:**

- La respuesta contiene tipos de eventos, artistas favoritos y ciudad.
- Si no existe el registro, se informa `404`.
- Solo el propietario o un administrador puede consultar el registro.

### US-03 — Modificar preferencias

**Como** cliente, **quiero** actualizar mis intereses **para** recibir sugerencias pertinentes.

**Criterios de aceptación:**

- Los cambios se almacenan y se reflejan en consultas posteriores.
- Se rechazan los datos inválidos y la modificación de preferencias inexistentes.
- Solo el propietario o un administrador puede modificarlas.

### US-04 — Obtener recomendaciones por preferencias

**Como** cliente, **quiero** recibir eventos relacionados con mis gustos **para** descubrir espectáculos de interés.

**Criterios de aceptación:**

- Se consultan los eventos publicados y los detalles de sus participantes en Eventos y Lugares.
- Se consideran coincidencias por tipo, artista o ciudad.
- Solo se incluyen eventos publicados cuya fecha de realización sea futura.
- La respuesta contiene el identificador, nombre, tipo, artista, ciudad, fecha y precio del evento.
- Sin coincidencias, se devuelve `200` con una lista vacía.
- Sin preferencias registradas, se informa `404`.

### US-05 — Recomendar según historial de compras

**Como** cliente, **quiero** recibir sugerencias basadas en compras de clientes con eventos en común **para** descubrir otros eventos.

**Criterios de aceptación:**

- Solo se consideran compras en estado `CONFIRMADA`.
- Se identifican otros clientes que hayan comprado entradas para alguno de los mismos eventos.
- Se sugieren otros eventos comprados por esos clientes, excluyendo los que ya compró el cliente consultado.
- Compras devuelve los identificadores sugeridos mediante una API protegida.
- Recomendaciones combina las sugerencias históricas con las preferencias, sin duplicados, y aplica el filtro de eventos publicados y futuros.

### US-06 — Acceder de forma segura

**Como** cliente, **quiero** que mis preferencias e historial se consulten de forma segura **para** proteger mis datos.

**Criterios de aceptación:**

- Las operaciones requieren un JWT válido con rol `CLIENTE` o `ADMINISTRADOR`.
- El cliente solo puede acceder a su información; el administrador puede consultar preferencias y recomendaciones de otros clientes.
- Las solicitudes sin JWT válido se rechazan con `401`; el acceso a datos ajenos se rechaza con `403`.
- Recomendaciones reenvía el JWT recibido cuando consulta el endpoint protegido de Compras.

## 4. Alcance

La estrategia colaborativa implementada compara eventos comprados en común y no utiliza modelos predictivos ni aprendizaje automático. Cada microservicio conserva la propiedad de sus datos: Recomendaciones almacena preferencias, Eventos administra el catálogo y Compras administra las compras.
