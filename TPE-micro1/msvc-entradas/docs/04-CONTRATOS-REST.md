# 04 — Contratos REST de Entradas

## 1. Descripción general

El microservicio `msvc-entradas` expone una API REST para gestionar la emisión, consulta, invalidación y devolución de entradas.

La comunicación se realiza mediante HTTP y los datos se intercambian en formato JSON.

La API está organizada en dos controladores:

- `EntradaController`: administra las operaciones relacionadas con las entradas.
- `DevolucionController`: administra las solicitudes de devolución y su resolución.

## 2. Endpoints de entradas

### 2.1. Emitir entradas

**Método:** `POST`

**Ruta:** `/entradas/emitir`

**Descripción:**

Solicita la emisión de las entradas correspondientes a una compra con pago aprobado y reserva confirmada. Se genera una entrada por cada lugar incluido en los detalles de la reserva.

**Request Body:**

```json
{
  "compraId": 1
}
```

El cuerpo utiliza el DTO `EmitirEntradasRequestDTO`.

**Respuesta:**

Devuelve una lista de objetos `EntradaDTO`.

Cada entrada contiene:

| Campo | Descripción |
|---|---|
| `id` | Identificador de la entrada. |
| `compraId` | Compra asociada. |
| `reservaId` | Reserva asociada. |
| `detalleReservaId` | Detalle de reserva correspondiente. |
| `indiceLugar` | Posición del lugar dentro del detalle. |
| `clienteId` | Cliente titular de la entrada. |
| `eventoId` | Evento al que pertenece. |
| `sectorId` | Sector del evento. |
| `asientoId` | Identificador del asiento, si corresponde. |
| `codigoQr` | Código único asociado a la entrada. |
| `estado` | Estado actual de la entrada. |
| `fechaEmision` | Fecha y hora de emisión. |
| `qrImagenBase64` | Imagen del código QR codificada en Base64. |

**Códigos HTTP:**

- `201 Created`: se crearon nuevas entradas.
- `200 OK`: la compra ya tenía entradas emitidas y se devuelven las existentes.

La operación es idempotente: si se solicita nuevamente la emisión para una compra que ya tiene entradas, no se generan duplicados.

### 2.2. Obtener una entrada

**Método:** `GET`

**Ruta:** `/entradas/{id}`

**Descripción:**

Permite consultar una entrada específica.

**Parámetro de ruta:**

- `id`: identificador de la entrada.

**Encabezado requerido:**

- `X-Cliente-Id`: identificador del cliente que solicita la entrada.

**Respuesta:**

Devuelve un objeto `EntradaDTO`, incluyendo la imagen QR codificada en Base64.

El servicio verifica que la entrada exista y que pertenezca al cliente indicado en el encabezado.

### 2.3. Obtener entradas de un cliente

**Método:** `GET`

**Ruta:** `/entradas/cliente/{clienteId}`

**Descripción:**

Permite obtener todas las entradas asociadas a un cliente.

**Parámetro de ruta:**

- `clienteId`: identificador del cliente.

**Respuesta:**

Devuelve una lista de objetos `EntradaDTO`, ordenados por fecha de emisión descendente y, en caso de empate, por identificador descendente.

Durante la primera entrega, el cliente se identifica mediante el parámetro de ruta `clienteId`, conforme al mecanismo de identificación definido para esta etapa del sistema.

### 2.4. Invalidar entradas de un evento

**Método:** `POST`

**Ruta:** `/entradas/evento/{id}/invalidar`

**Descripción:**

Invalida las entradas emitidas de un evento que se encuentra cancelado.

**Parámetro de ruta:**

- `id`: identificador del evento.

**Request Body:** no requiere.

**Respuesta:**

No devuelve un cuerpo de respuesta. El controlador utiliza la respuesta HTTP predeterminada de Spring.

El servicio consulta el estado del evento mediante `EventoFeignClient`. Si el evento está cancelado, busca sus entradas en estado `EMITIDA` y las cambia a `INVALIDADA`.

La invalidación de entradas constituye una de las operaciones del proceso de cancelación de eventos. El microservicio Eventos y Lugares coordina las comunicaciones con Reservas, Entradas y Compras para gestionar la liberación de lugares, la invalidación de entradas y los reintegros correspondientes.

## 3. Endpoints de devoluciones

### 3.1. Solicitar devolución

**Método:** `POST`

**Ruta:** `/devoluciones`

**Descripción:**

Permite solicitar la devolución de una entrada.

**Request Body:**

```json
{
  "entradaId": 1,
  "motivo": "No puedo asistir al evento"
}
```

El cuerpo utiliza el DTO `DevolucionRequestDTO`.

Antes de registrar la solicitud, el servicio verifica:

- Que la entrada exista.
- Que su estado sea `EMITIDA`.
- Que falten al menos 72 horas para el evento.
- Que no exista otra solicitud pendiente para esa entrada.

**Respuesta:**

Devuelve un objeto `DevolucionResponseDTO` con los siguientes campos:

| Campo | Descripción |
|---|---|
| `id` | Identificador de la solicitud. |
| `entradaId` | Entrada asociada a la solicitud. |
| `fechaSolicitud` | Fecha y hora en que se realizó. |
| `motivo` | Motivo de la devolución. |
| `estado` | Estado de la solicitud. |
| `fechaResolucion` | Fecha en que se resolvió, si corresponde. |
| `administradorId` | Identificador del administrador que la resolvió, si corresponde. |

La solicitud se registra inicialmente con estado `PENDIENTE`.

### 3.2. Obtener solicitudes pendientes

**Método:** `GET`

**Ruta:** `/devoluciones/pendientes`

**Descripción:**

Permite consultar las solicitudes de devolución que se encuentran pendientes de resolución.

**Respuesta:**

Devuelve una lista de objetos `DevolucionResponseDTO` correspondientes a solicitudes con estado `PENDIENTE`.

### 3.3. Aprobar una devolución

**Método:** `PATCH`

**Ruta:** `/devoluciones/{id}/aprobar`

**Descripción:**

Permite que un administrador apruebe una solicitud de devolución.

**Parámetros:**

- `id`: identificador de la solicitud, recibido como parámetro de ruta.
- `administradorId`: identificador del administrador, recibido como parámetro de consulta.

**Ejemplo de solicitud:**

```http
PATCH /devoluciones/1/aprobar?administradorId=10
```

**Respuesta:**

Devuelve un objeto `DevolucionResponseDTO` con los datos actualizados de la solicitud.

Al aprobar una devolución, el servicio:

1. Actualiza la solicitud a estado `APROBADA`.
2. Registra el administrador y la fecha de resolución.
3. Invalida la entrada.
4. Solicita al microservicio Reservas la devolución del lugar correspondiente.
5. Solicita al microservicio Compras el reintegro correspondiente.

La actualización de la solicitud y la invalidación de la entrada se realizan mediante una transacción local.

Las comunicaciones con Reservas y Compras se efectúan posteriormente. La operación permite reintentar estas llamadas si la solicitud ya se encuentra aprobada.

### 3.4. Rechazar una devolución

**Método:** `PATCH`

**Ruta:** `/devoluciones/{id}/rechazar`

**Descripción:**

Permite que un administrador rechace una solicitud de devolución.

**Parámetros:**

- `id`: identificador de la solicitud, recibido como parámetro de ruta.
- `administradorId`: identificador del administrador, recibido como parámetro de consulta.

**Ejemplo de solicitud:**

```http
PATCH /devoluciones/1/rechazar?administradorId=10
```

**Respuesta:**

Devuelve un objeto `DevolucionResponseDTO` con los datos actualizados.

Al rechazar una devolución:

- La solicitud cambia a estado `RECHAZADA`.
- Se registra el administrador y la fecha de resolución.
- La entrada permanece en estado `EMITIDA`.
- No se solicita la liberación del lugar ni el reintegro correspondiente.

## 4. Comunicación con otros microservicios

`msvc-entradas` se comunica con otros microservicios para validar compras y reservas, consultar eventos y procesar devoluciones.

Estas comunicaciones se realizan mediante `RestClient` y OpenFeign.

### 4.1. Comunicación con Compras

| Cliente | Método HTTP | Ruta | Descripción |
|---|---|---|---|
| `CompraClient` | GET | `/compras/{id}/confirmacion` | Consulta el estado de confirmación de una compra y verifica si tiene un pago aprobado. |
| `CompraFeignClient` | POST | `/compras/reintegros` | Solicita el reintegro correspondiente a una entrada devuelta. |

**DTO utilizado para consultar la compra (`CompraDTO`):**

- `id`
- `clienteId`
- `reservaId`
- `estado`
- `pagoAprobado`

**DTO utilizado para solicitar el reintegro (`DevolucionRequestDTO`):**

- `entradaId`
- `motivo`

La consulta de la compra se realiza mediante `RestClient`, mientras que la solicitud de reintegro se realiza mediante OpenFeign.

### 4.2. Comunicación con Reservas

| Cliente | Método HTTP | Ruta | Descripción |
|---|---|---|---|
| `ReservaClient` | GET | `/reservas/{id}` | Consulta la información de una reserva. |
| `ReservaFeignClient` | POST | `/reservas/{id}/devolver-entrada` | Solicita la devolución del lugar correspondiente a una entrada. |

**DTO utilizado para consultar la reserva (`ReservaDTO`):**

- `id`
- `clienteId`
- `eventoId`
- `estado`
- `detalles`

Cada detalle de reserva (`DetalleReservaDTO`) contiene:

- `id`
- `sectorId`
- `cantidad`
- `asientoIds`
- `cantidadLiberada`

**DTO utilizado para devolver una entrada (`DevolverEntradaDTO`):**

- `entradaId`
- `sectorId`
- `asientoId`

La consulta de la reserva se realiza mediante `RestClient`, mientras que la devolución del lugar se realiza mediante OpenFeign.

### 4.3. Comunicación con Eventos y Lugares

La comunicación se realiza mediante `EventoFeignClient`.

| Método HTTP | Ruta | Descripción |
|---|---|---|
| GET | `/eventos/{id}` | Consulta la información de un evento. |

**DTO utilizado (`EventoDTO`):**

- `id`
- `fechaHora`
- `estado`

Este cliente permite verificar el estado del evento y controlar que las solicitudes de devolución se realicen con la anticipación mínima de 72 horas.

## 5. Consideraciones generales

- Los datos intercambiados entre microservicios se representan mediante DTO, evitando depender directamente de las entidades internas de otros servicios.
- Los identificadores permiten relacionar compras, reservas, eventos, clientes y entradas.
- La emisión de entradas es idempotente y evita generar duplicados para una misma compra.
- Cada entrada posee un código QR único cuya imagen se entrega codificada en Base64.
- Las solicitudes de devolución utilizan estados para controlar su aprobación o rechazo.
- Las comunicaciones entre microservicios se realizan mediante clientes HTTP.
- Las operaciones distribuidas utilizan transacciones locales y mecanismos de reintento para gestionar fallas en las comunicaciones.
- La aprobación de una devolución invalida la entrada y solicita la devolución del lugar a Reservas y el reintegro a Compras.
- Ante la cancelación de un evento, Entradas se responsabiliza de invalidar sus entradas. El proceso general es coordinado por Eventos y Lugares, en comunicación con Reservas, Entradas y Compras.