# 01 — Funcionalidades e historias de usuario

## Objetivo

El microservicio Entradas administra la emisión, consulta, validación e invalidación de entradas de TicketYa, así como las solicitudes de devolución y el control de acceso a los eventos.

## Historias de usuario

### US01 — Emitir entradas

**Como cliente**, quiero recibir una entrada por cada lugar adquirido.

**Criterios de aceptación:**
- La compra debe tener un pago aprobado y la reserva debe estar confirmada.
- Se emite una entrada por cada lugar reservado, con un código QR único.
- Los sectores numerados conservan el asiento asignado.
- Repetir la emisión devuelve las entradas existentes sin duplicarlas.

### US02 — Consultar mis entradas

**Como cliente**, quiero consultar mis entradas y sus códigos QR.

**Criterios de aceptación:**
- Se pueden consultar las entradas individualmente o por cliente.
- Cada respuesta incluye los datos del evento, sector, asiento cuando corresponde, estado y QR.
- Se verifica la identidad y titularidad del cliente.

### US03 — Validar el ingreso mediante QR

**Como personal de acceso**, quiero escanear una entrada para autorizar el ingreso a un evento.

**Criterios de aceptación:**
- El usuario debe tener el rol `PERSONAL_ACCESO` y estar asignado al evento.
- La entrada debe existir, corresponder al evento y estar `EMITIDA`.
- La primera validación cambia su estado a `UTILIZADA` y registra la fecha de uso.
- Las entradas utilizadas, invalidadas o correspondientes a otro evento se rechazan.
- Se emplea bloqueo pesimista para controlar validaciones simultáneas.

### US04 — Administrar personal de acceso

**Como administrador**, quiero asignar o revocar personal autorizado para validar entradas.

**Criterios de aceptación:**
- Se registra la asociación entre un usuario y un evento.
- No se duplican asignaciones existentes.
- El personal solamente puede validar entradas de los eventos asignados.
- El administrador puede revocar una asignación.

### US05 — Solicitar devolución

**Como cliente**, quiero solicitar la devolución de una entrada que no utilizaré.

**Criterios de aceptación:**
- La entrada debe pertenecer al cliente y encontrarse `EMITIDA`.
- La solicitud debe realizarse al menos 72 horas antes del evento.
- No puede existir otra solicitud pendiente para la misma entrada.
- La devolución se registra inicialmente como `PENDIENTE`.

### US06 — Resolver devoluciones

**Como administrador**, quiero aprobar o rechazar las solicitudes de devolución.

**Criterios de aceptación:**
- El administrador puede consultar las solicitudes pendientes.
- Al aprobar, se invalida la entrada y se solicita a Reservas la liberación del lugar y a Compras el reintegro.
- La aprobación admite reintentos ante fallas de comunicación.
- Al rechazar, la entrada conserva su estado y no se libera el lugar.

### US07 — Invalidar entradas por cancelación

**Como sistema**, quiero invalidar las entradas de los eventos cancelados.

**Criterios de aceptación:**
- Se comprueba que el evento esté cancelado.
- Se invalidan las entradas que continúan `EMITIDA`.
- Una entrada invalidada no puede utilizarse para ingresar.
- Repetir la operación no produce invalidaciones adicionales.

### US08 — Proporcionar información para reportes

**Como organizador**, quiero conocer las entradas y sus estados para analizar la ocupación de un evento.

**Criterios de aceptación:**
- Se pueden consultar las entradas asociadas a un evento.
- Se informa el identificador de entrada, compra, evento, sector, asiento y estado.
- Los datos se proporcionan a Eventos y Lugares mediante una API interna protegida.