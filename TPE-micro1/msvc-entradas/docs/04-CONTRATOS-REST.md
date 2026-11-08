# 04 - Contratos REST

## 1. Descripción general

El microservicio `msvc-entradas` expone endpoints REST para gestionar la emisión, consulta, invalidación y devolución de entradas.

La comunicación se realiza mediante HTTP y los datos se intercambian en formato JSON.

La API está organizada en dos controladores:

- `EntradaController`: administra las entradas.
- `DevolucionController`: administra las solicitudes de devolución.

## 2. Endpoints de entradas

### 2.1 Emitir entradas

**Método:** `POST`

**Ruta:** `/entradas/emitir`

**Descripción:** solicita la emisión de las entradas correspondientes a una compra confirmada. Se genera una entrada por cada lugar incluido en los detalles de la reserva.

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
| id | Identificador de la entrada. |
| compraId | Compra asociada. |
| reservaId | Reserva asociada. |
| detalleReservaId | Detalle de reserva correspondiente. |
| indiceLugar | Posición del lugar dentro del detalle. |
| clienteId | Cliente titular de la entrada. |
| eventoId | Evento al que pertenece. |
| sectorId | Sector del evento. |
| asientoId | Identificador del asiento, si corresponde. |
| codigoQr | Código único asociado a la entrada. |
| estado | Estado actual de la entrada. |
| fechaEmision | Fecha y hora de emisión. |
| qrImagenBase64 | Imagen del código QR codificada en Base64. |

**Códigos HTTP:**

- `201 CREATED`: se crearon nuevas entradas.
- `200 OK`: la compra ya tenía entradas emitidas y se devuelven las existentes.

La operación es idempotente: si se solicita nuevamente la emisión para una compra que ya tiene entradas, no se generan duplicados.

### 2.2 Obtener una entrada

**Método:** `GET`

**Ruta:** `/entradas/{id}`

**Descripción:** permite consultar una entrada específica.

**Parámetro de ruta:**

- `id`: identificador de la entrada.

**Header requerido:**

- `X-Cliente-Id`: identificador del cliente que solicita la entrada.

**Respuesta:**

Devuelve un objeto `EntradaDTO`, incluyendo la imagen QR codificada en Base64.

El servicio verifica que la entrada exista y que pertenezca al cliente indicado en el encabezado.

### 2.3 Obtener entradas de un cliente

**Método:** `GET`

**Ruta:** `/entradas/cliente/{clienteId}`

**Descripción:** permite obtener todas las entradas asociadas a un cliente.

**Parámetro de ruta:**

- `clienteId`: identificador del cliente.

**Respuesta:**

Devuelve una lista de objetos `EntradaDTO`, ordenados por fecha de emisión descendente y, en caso de empate, por identificador descendente.

**Consideración de seguridad:**

Actualmente, este endpoint consulta las entradas utilizando el identificador de cliente recibido en la URL, pero no verifica que quien realiza la solicitud sea efectivamente ese cliente.

Esta validación de autorización queda pendiente de una mejora de seguridad.

### 2.4 Invalidar entradas de un evento

**Método:** `POST`

**Ruta:** `/entradas/evento/{id}/invalidar`

**Descripción:** invalida las entradas emitidas de un evento que se encuentra cancelado.

**Parámetro de ruta:**

- `id`: identificador del evento.

**Request Body:** no requiere.

**Respuesta:** no devuelve un cuerpo de respuesta. El controlador no establece explícitamente un código HTTP personalizado.

El servicio consulta el estado del evento mediante `EventoFeignClient`. Si el evento está cancelado, busca sus entradas en estado `EMITIDA` y las cambia a `INVALIDADA`.

Actualmente, esta operación solamente invalida las entradas almacenadas en `msvc-entradas`. La liberación de los lugares asociados a las reservas y la coordinación de los reintegros correspondientes todavía no están implementadas en este flujo.

## 3. Endpoints de devoluciones

### 3.1 Solicitar devolución

**Método:** `POST`

**Ruta:** `/devoluciones`

**Descripción:** permite solicitar la devolución de una entrada.

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
| id | Identificador de la solicitud. |
| entradaId | Entrada asociada a la solicitud. |
| fechaSolicitud | Fecha y hora en que se realizó. |
| motivo | Motivo de la devolución. |
| estado | Estado de la solicitud. |
| fechaResolucion | Fecha en que se resolvió, si corresponde. |
| administradorId | Identificador del administrador que la resolvió, si corresponde. |

La solicitud se registra inicialmente con estado `PENDIENTE`.

### 3.2 Obtener solicitudes pendientes

**Método:** `GET`

**Ruta:** `/devoluciones/pendientes`

**Descripción:** permite consultar las solicitudes de devolución que todavía se encuentran pendientes de resolución.

**Respuesta:**

Devuelve una lista de objetos `DevolucionResponseDTO`.

### 3.3 Aprobar una devolución

**Método:** `PATCH`

**Ruta:** `/devoluciones/{id}/aprobar`

**Descripción:** permite que un administrador apruebe una solicitud de devolución.

**Parámetros:**

- `id`: identificador de la solicitud.
- `administradorId`: identificador del administrador, recibido como parámetro de consulta.

**Ejemplo de ruta:**

`PATCH /devoluciones/1/aprobar?administradorId=10`

**Respuesta:**

Devuelve un objeto `DevolucionResponseDTO` con los datos actualizados de la solicitud.

Al aprobar una devolución, el servicio:

1. Actualiza la solicitud a estado `APROBADA`.
2. Registra el administrador y la fecha de resolución.
3. Invalida la entrada.
4. Solicita al microservicio de reservas la devolución del lugar correspondiente.
5. Solicita al microservicio de compras el reintegro correspondiente.

La operación permite reintentar las llamadas externas si la solicitud ya se encuentra aprobada.

### 3.4 Rechazar una devolución

**Método:** `PATCH`

**Ruta:** `/devoluciones/{id}/rechazar`

**Descripción:** permite que un administrador rechace una solicitud de devolución.

**Parámetros:**

- `id`: identificador de la solicitud.
- `administradorId`: identificador del administrador, recibido como parámetro de consulta.

**Ejemplo de ruta:**

`PATCH /devoluciones/1/rechazar?administradorId=10`

**Respuesta:**

Devuelve un objeto `DevolucionResponseDTO` con los datos actualizados.

Al rechazar una devolución:

- La solicitud cambia a estado `RECHAZADA`.
- Se registra el administrador y la fecha de resolución.
- La entrada permanece en estado `EMITIDA`.

## 4. Comunicación con otros microservicios

`msvc-entradas` necesita comunicarse con otros microservicios para validar compras y reservas, consultar eventos y procesar devoluciones.

Para estas comunicaciones utiliza `RestClient` y `FeignClient`.

### 4.1 Comunicación con msvc-compras


| Cliente             | Método HTTP |        Ruta                  | Descripción |
|                     |             |                                            ||
| `CompraClient`      |    GET      | `/compras/{id}/confirmacion` | Consulta la información de una compra y verifica su confirmación y pago.       |
| `CompraFeignClient` |   POST      | `/compras/reintegros`        | Solicita el reintegro correspondiente a una entrada devuelta.                                |

**DTO utilizado para consultar la compra (`CompraDTO`):**

- `id`
- `clienteId`
- `reservaId`
- `estado`
- `pagoAprobado`

**DTO utilizado para solicitar el reintegro (`DevolucionRequestDTO`):**

- `entradaId`
- `motivo`

La consulta de la compra se realiza mediante `RestClient`, mientras que la solicitud de reintegro se realiza mediante `FeignClient`.

### 4.2 Comunicación con msvc-reservas

| Cliente             | Método HTTP |      Ruta                   |  Descripción |

| `ReservaClient`     |    GET      | `/reservas/{id}`            | Consulta la información de una reserva. |
| `ReservaFeignClient`|    POST     | `/reservas/{id}/devolver-entrada` | Solicita la devolución del lugar correspondiente a una entrada. |

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

La consulta de la reserva se realiza mediante `RestClient`, mientras que la devolución del lugar se realiza mediante `FeignClient`.
### 4.3 Comunicación con msvc-eventos

La comunicación se realiza mediante `EventoFeignClient`.

| Método HTTP | Ruta | Descripción |
|---|---|---|
| GET | `/eventos/{id}` | Consulta la información de un evento. |

**DTO utilizado (`EventoDTO`):**

- `id`
- `fechaHora`
- `estado`

Se utiliza para verificar el estado del evento y controlar que una solicitud de devolución se realice con la anticipación requerida.

## 5. Consideraciones generales

- Los datos intercambiados entre microservicios se representan mediante DTOs, evitando depender directamente de las entidades internas de otros servicios.
- Los identificadores permiten relacionar compras, reservas, eventos, clientes y entradas.
- La emisión de entradas evita generar duplicados para una misma compra.
- El código QR se genera y se entrega como una imagen codificada en Base64.
- Las solicitudes de devolución tienen estados que permiten controlar su aprobación o rechazo.
- Las llamadas a otros microservicios se realizan mediante clientes HTTP.
- Las operaciones que involucran más de un microservicio requieren considerar posibles fallos en las comunicaciones externas.
- La aprobación de una devolución invalida la entrada y luego solicita la devolución del lugar a Reservas y el reintegro a Compras.
- La invalidación de entradas por cancelación de un evento actualmente no incluye la liberación de reservas ni la coordinación de reintegros.