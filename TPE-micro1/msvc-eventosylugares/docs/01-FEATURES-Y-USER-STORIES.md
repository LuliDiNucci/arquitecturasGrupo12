# TicketYa — Features, User Stories y criterios de aceptación

**Alcance:** microservicio **Eventos y Lugares**, funcionalidades **1, 2, 3 y 9** del TPE y la parte de **7** que informa cancelaciones. La consigna del **grupo** también exige historias para el circuito completo (Reservas, Compras, Entradas); no se atribuyen a este microservicio.

Los criterios están redactados en formato Dado / Cuando / Entonces, para convertirlos en pruebas.

## Feature F-EV-01 — Administrar lugares, sectores y asientos

### US-EV-01 — Crear lugar
Como **administrador**, quiero registrar el nombre, dirección, ciudad y coordenadas de un lugar para usarlo al crear eventos.
- Dado un JSON válido, cuando hago `POST /lugares`, entonces obtengo **201** y un ID.
- Dado un lugar con coordenadas fuera de rango o campos vacíos, cuando intento crearlo, entonces obtengo **400**.
- Dado un lugar existente, cuando lo consulto, entonces obtengo sus datos con **200**; si no existe, **404**.

### US-EV-02 — Crear sectores
Como **administrador u organizador**, quiero definir sectores numerados o con capacidad general.
- Dado un lugar existente sin eventos activos, cuando creo un sector, entonces se guarda `numerado` y una `capacidad` positiva.
- Cuando repito el nombre de un sector en el mismo lugar, se rechaza con **409**.
- Cuando el lugar tiene eventos publicados, agotados o suspendidos, no puedo agregar sectores (**409**).

### US-EV-03 — Cargar asientos numerados
Como **administrador**, quiero registrar la fila y número de cada asiento.
- Dado un sector numerado, cuando cargo una lista de asientos, entonces sus IDs son únicos y reciben **201**.
- No puedo repetir `fila` + `numero` dentro del sector, ignorando mayúsculas/minúsculas (**409**).
- No puedo cargar asientos en un sector no numerado (**400**) ni superar la capacidad (**409**).

## Feature F-EV-02 — Organizar eventos y precios

### US-EV-04 — Registrar organizadores y participantes
Como **organizador**, quiero identificar quién organiza y quién participa para asociarlos a cada evento.
- Se pueden registrar organizadores y participantes con nombre válido y tipo del enum.
- No puedo crear un evento con IDs inexistentes o participantes repetidos.

### US-EV-05 — Crear un borrador de evento
Como **organizador**, quiero crear un evento sin habilitar todavía su venta.
- Dado un evento con nombre, descripción, tipo, organizador, participantes, lugar y fecha, cuando lo creo, entonces queda **BORRADOR** y responde **201**.
- Un borrador no aparece en búsquedas públicas por defecto.

### US-EV-06 — Asignar precios por sector y evento
Como **organizador**, quiero definir un precio propio por sector para cada evento.
- Para un evento en BORRADOR, debo enviar **exactamente un precio por cada sector** de su lugar.
- El precio debe ser no negativo y no puede repetirse el mismo sector en el request.
- Un mismo sector puede tener precios distintos en dos eventos: `PrecioSector` vincula `evento` + `sector`.
- Una actualización de precios solo se permite en BORRADOR; desde otros estados devuelve **409**.

### US-EV-07 — Publicar evento
Como **organizador**, quiero publicar un evento solamente cuando esté completo.
- Dado BORRADOR (o SUSPENDIDO) con fecha futura, participante, sectores y precios completos, al publicar el estado cambia a PUBLICADO (o vuelve a AGOTADO si correspondía).
- Si faltan precios, participantes, sectores o asientos de un sector numerado, o la fecha ya pasó, devuelve **409** y no cambia el estado.

### US-EV-08 — Modificar evento
Como **organizador**, quiero modificar su información sin romper reservas existentes.
- BORRADOR permite modificar nombre, descripción, tipo, fecha, lugar, organizador y participantes.
- PUBLICADO / AGOTADO / SUSPENDIDO permiten nombre, descripción, tipo y participantes, pero no fecha, lugar, organizador ni altaDemanda.
- CANCELADO / FINALIZADO no son editables (**409**).

## Feature F-EV-03 — Buscar y consultar eventos

### US-EV-09 — Buscar por criterios combinables
Como **cliente**, quiero buscar eventos por tipo, organizador, participante, ciudad, fechas y texto, y ordenar por fecha, precio o relevancia.
- Los filtros son opcionales, se pueden combinar y se aplican al mismo resultado.
- Por defecto se devuelven PUBLICADO y AGOTADO, no BORRADOR, SUSPENDIDO, CANCELADO ni FINALIZADO.
- El precio usado para ordenar es el **menor precio configurado** de cada evento (`precioDesde`); no es la disponibilidad real.
- `desde > hasta` responde **400**. `incluirNoPublicados=true` es uso administrativo provisional; JWT queda para entrega 2.

### US-EV-10 — Consultar ofertas para Reservas y Compras
Como **microservicio Reservas/Compras**, necesito conocer estado, fecha, sectores, precios, tipos y asientos.
- `GET /eventos/{id}/habilitado` indica si se permite reservar (`PUBLICADO` y fecha futura).
- `GET /eventos/{id}/sectores` devuelve configuración y precios, **no** ocupación ni stock restante.
- Reservas **siempre** valida evento, sector y asiento antes de intentar reservar y mantiene su propio inventario.

## Feature F-EV-04 — Estados, suspensión y cancelación

### US-EV-11 — Suspender evento
Como **administrador**, quiero suspender un evento publicado o agotado para que no admita nuevas reservas.
- PUBLICADO/AGOTADO → SUSPENDIDO; `habilitadoParaReservar=false`.
- Una re-publicación comprueba de nuevo toda la configuración y recupera PUBLICADO o AGOTADO.

### US-EV-12 — Cancelar evento y avisar a otros servicios
Como **administrador**, quiero cancelar eventos y detener ventas, invalidar entradas y desencadenar reintegros.
- CANCELADO queda persistido y Reservas debe rechazar nuevas operaciones.
- Se crea una notificación de cancelación **en la misma transacción** que actualiza el estado.
- El aviso se reintenta para **Reservas, Entradas y Compras** cuando sus URLs estén configuradas.
- Repetir cancelar no genera una segunda notificación. Las APIs receptoras deben procesar `X-Operacion-Id` idempotentemente.
- El microservicio Eventos **no** ejecuta reintegros ni invalida QR directamente: eso corresponde a los otros servicios.

## Pendiente de documentación compartida por el grupo

Según la página 4 del PDF del TPE, el **grupo entero** debe entregar también US sobre reservas temporales, no doble venta, pagos simulados, QR y devoluciones; diagrama de secuencia de compra y prueba de concurrencia. Aquí se incluyen los contratos y el diagrama de secuencia como apoyo, pero **el código y el test de doble venta son responsabilidad de Reservas/Compras**.
