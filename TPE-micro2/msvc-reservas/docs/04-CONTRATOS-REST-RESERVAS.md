# 04 — Contratos REST de Reservas

## 1. Descripción general

El microservicio `msvc-reservas` expone una API REST para gestionar la disponibilidad de lugares, la creación de reservas temporales, su confirmación, liberación y cancelación.

También proporciona operaciones internas para coordinar la habilitación de eventos y la devolución individual de entradas con los demás microservicios de TicketYa.

La API utiliza HTTP y JSON como formato de intercambio de información.

**Configuración local:**

- **URL base:** `http://localhost:8080`
- **Formato de intercambio:** `application/json`
- **Swagger UI:** `http://localhost:8080/swagger-ui.html`
- **Especificación OpenAPI:** `http://localhost:8080/v3/api-docs`
- **Duración de una reserva temporal:** 10 minutos.

La API está organizada en dos controladores:

- `ReservaController`: administra la creación, consulta, confirmación, liberación y cancelación de reservas.
- `DisponibilidadController`: permite consultar la disponibilidad de lugares y asientos de un sector.

## 2. Endpoints REST

| Método | Ruta | Parámetros / DTO | Éxito | Errores |
|---|---|---|---|---|
| GET | `/disponibilidad` | `eventoId`, `sectorId` | 200 | 400, 404, 409, 503 |
| POST | `/reservas` | `CrearReservaDTO` | 201 | 400, 404, 409, 503 |
| GET | `/reservas/{id}` | id: Long | 200 | 404 |
| POST | `/reservas/{id}/confirmar` | id: Long | 200 | 404, 409 |
| POST | `/reservas/{id}/liberar` | id: Long | 200 | 404, 409 |
| POST | `/reservas/evento/{eventoId}/habilitar` | eventoId: Long | 204 | 400, 404, 409, 503 |
| POST | `/reservas/evento/{eventoId}/cancelar` | eventoId: Long | 204 | 409 |
| POST | `/reservas/{id}/devolver-entrada` | id + `DevolverEntradaDTO` | 200 | 400, 404, 409 |

Los códigos indicados corresponden a las respuestas contempladas por los controladores y las reglas de negocio. Los errores internos no controlados pueden producir una respuesta `500 Internal Server Error`.

## 3. Consulta de disponibilidad

### 3.1. Consultar disponibilidad de un sector

**Método:** `GET`

**Ruta:** `/disponibilidad`

**Descripción:**

Permite consultar la cantidad de lugares disponibles en un sector de un evento.

En sectores numerados devuelve los identificadores de los asientos disponibles y ocupados. En sectores no numerados informa la capacidad restante.

**Parámetros de consulta:**

| Parámetro | Tipo | Obligatorio | Descripción |
|---|---|---|---|
| `eventoId` | Long | Sí | Identificador del evento. |
| `sectorId` | Long | Sí | Identificador del sector. |

**Ejemplo de solicitud:**

```http
GET /disponibilidad?eventoId=20&sectorId=5
```

**Respuesta — `200 OK`:**

```json
{
  "eventoId": 20,
  "sectorId": 5,
  "lugaresDisponibles": 2,
  "asientosDisponibles": [25, 27],
  "asientosOcupados": [26]
}
```

La respuesta utiliza `DisponibilidadDTO`.

| Campo | Descripción |
|---|---|
| `eventoId` | Identificador del evento consultado. |
| `sectorId` | Identificador del sector consultado. |
| `lugaresDisponibles` | Cantidad de lugares disponibles. |
| `asientosDisponibles` | Identificadores de asientos libres, cuando el sector es numerado. |
| `asientosOcupados` | Identificadores de asientos ocupados por reservas activas vigentes o confirmadas. |

En sectores no numerados, las listas de asientos se devuelven vacías y la disponibilidad se expresa mediante `lugaresDisponibles`.

**Códigos HTTP:**

- `200 OK`: disponibilidad consultada correctamente.
- `400 Bad Request`: parámetros inválidos.
- `404 Not Found`: evento o sector inexistente.
- `409 Conflict`: evento cancelado.
- `503 Service Unavailable`: no se pudo consultar Eventos y Lugares.

La consulta no realiza una reserva ni bloquea lugares. La disponibilidad puede modificarse por operaciones concurrentes, por lo que se verifica nuevamente al crear la reserva.

## 4. Gestión de reservas

### 4.1. Crear una reserva temporal

**Método:** `POST`

**Ruta:** `/reservas`

**Descripción:**

Permite crear una reserva temporal para uno o varios lugares de un evento.

El servicio valida la información del evento, los sectores, los asientos solicitados, la disponibilidad, el límite máximo de entradas por cliente y la clave de idempotencia.

**Request Body:**

```json
{
  "clienteId": 1,
  "eventoId": 20,
  "claveIdempotencia": "RESERVA-001",
  "detalles": [
    {
      "sectorId": 5,
      "cantidad": 2,
      "asientoIds": [25, 26]
    }
  ]
}
```

El cuerpo utiliza `CrearReservaDTO`.

**Campos de la solicitud:**

| Campo | Tipo | Obligatorio | Descripción |
|---|---|---|---|
| `clienteId` | Long | Sí | Identificador del cliente. |
| `eventoId` | Long | Sí | Identificador del evento. |
| `claveIdempotencia` | String | Sí | Clave para reconocer solicitudes repetidas. |
| `detalles` | Array | Sí | Sectores y lugares solicitados. |
| `detalles[].sectorId` | Long | Sí | Identificador del sector. |
| `detalles[].cantidad` | Integer | Sí | Cantidad de lugares solicitados, mayor que cero. |
| `detalles[].asientoIds` | Array de Long | Según sector | Asientos seleccionados para sectores numerados. |

Para sectores numerados se deben indicar los asientos específicos. En sectores no numerados se utiliza la cantidad solicitada y puede enviarse una lista de asientos vacía.

**Respuesta — `201 Created`:**

```json
{
  "id": 1,
  "clienteId": 1,
  "eventoId": 20,
  "fechaCreacion": "2026-11-09T15:30:00",
  "fechaVencimiento": "2026-11-09T15:40:00",
  "estado": "ACTIVA",
  "detalles": [
    {
      "id": 1,
      "sectorId": 5,
      "cantidad": 2,
      "asientoIds": [25, 26],
      "cantidadLiberada": 0
    }
  ]
}
```

La respuesta utiliza `ReservaDTO`.

**Códigos HTTP:**

- `201 Created`: reserva creada correctamente o recuperada mediante una solicitud idempotente.
- `400 Bad Request`: información de la solicitud inválida.
- `404 Not Found`: evento, sector o asiento inexistente.
- `409 Conflict`: conflicto de disponibilidad, estado o integridad.
- `503 Service Unavailable`: no se pudo consultar Eventos y Lugares.

**Idempotencia:**

La creación utiliza `claveIdempotencia` para reconocer reintentos y evitar reservas duplicadas.

Cuando se repite una solicitud con la misma clave y los mismos datos, se recupera la reserva correspondiente sin registrar otra.

**Concurrencia:**

La creación utiliza bloqueo pesimista y operaciones transaccionales para controlar la ocupación de los lugares.

Esto evita que dos solicitudes simultáneas reserven el mismo asiento o superen la capacidad de un sector.

### 4.2. Consultar una reserva

**Método:** `GET`

**Ruta:** `/reservas/{id}`

**Descripción:**

Obtiene la información y el estado actual de una reserva mediante su identificador.

**Parámetro de ruta:**

- `id`: identificador de la reserva.

**Ejemplo de solicitud:**

```http
GET /reservas/1
```

**Respuesta — `200 OK`:**

```json
{
  "id": 1,
  "clienteId": 1,
  "eventoId": 20,
  "fechaCreacion": "2026-11-09T15:30:00",
  "fechaVencimiento": "2026-11-09T15:40:00",
  "estado": "ACTIVA",
  "detalles": [
    {
      "id": 1,
      "sectorId": 5,
      "cantidad": 2,
      "asientoIds": [25, 26],
      "cantidadLiberada": 0
    }
  ]
}
```

**Códigos HTTP:**

- `200 OK`: reserva encontrada.
- `404 Not Found`: reserva inexistente.

**Campos de `ReservaDTO`:**

| Campo | Descripción |
|---|---|
| `id` | Identificador de la reserva. |
| `clienteId` | Cliente asociado. |
| `eventoId` | Evento asociado. |
| `fechaCreacion` | Fecha y hora de creación. |
| `fechaVencimiento` | Fecha y hora de vencimiento temporal. |
| `estado` | Estado actual de la reserva. |
| `detalles` | Sectores, cantidades y asientos de la reserva. |

Cada elemento de `detalles` contiene:

- `id`
- `sectorId`
- `cantidad`
- `asientoIds`
- `cantidadLiberada`

El atributo `cantidadLiberada` indica cuántos lugares individuales ya fueron liberados. La lista `asientoIds` representa los asientos que continúan ocupados por la reserva.

### 4.3. Confirmar una reserva

**Método:** `POST`

**Ruta:** `/reservas/{id}/confirmar`

**Descripción:**

Permite confirmar una reserva después de que el microservicio Compras informa la aprobación del pago.

Es una operación de integración utilizada por Compras.

**Parámetro de ruta:**

- `id`: identificador de la reserva.

**Request Body:** no requiere.

**Ejemplo de solicitud:**

```http
POST /reservas/1/confirmar
```

**Respuesta — `200 OK`:**

```json
{
  "id": 1,
  "clienteId": 1,
  "eventoId": 20,
  "fechaCreacion": "2026-11-09T15:30:00",
  "fechaVencimiento": "2026-11-09T15:40:00",
  "estado": "CONFIRMADA",
  "detalles": [
    {
      "id": 1,
      "sectorId": 5,
      "cantidad": 2,
      "asientoIds": [25, 26],
      "cantidadLiberada": 0
    }
  ]
}
```

**Códigos HTTP:**

- `200 OK`: reserva confirmada.
- `404 Not Found`: reserva inexistente.
- `409 Conflict`: la reserva no puede confirmarse por su estado o vencimiento.

La confirmación requiere una reserva activa y vigente. Una reserva vencida, liberada o cancelada no puede confirmarse.

Una vez confirmada, sus lugares permanecen ocupados.

### 4.4. Liberar una reserva

**Método:** `POST`

**Ruta:** `/reservas/{id}/liberar`

**Descripción:**

Permite liberar una reserva temporal y los lugares asociados.

Se utiliza principalmente cuando el microservicio Compras informa que el pago fue rechazado.

**Parámetro de ruta:**

- `id`: identificador de la reserva.

**Request Body:** no requiere.

**Ejemplo de solicitud:**

```http
POST /reservas/1/liberar
```

**Respuesta — `200 OK`:**

```json
{
  "id": 1,
  "clienteId": 1,
  "eventoId": 20,
  "fechaCreacion": "2026-11-09T15:30:00",
  "fechaVencimiento": "2026-11-09T15:40:00",
  "estado": "LIBERADA",
  "detalles": [
    {
      "id": 1,
      "sectorId": 5,
      "cantidad": 2,
      "asientoIds": [25, 26],
      "cantidadLiberada": 0
    }
  ]
}
```

**Códigos HTTP:**

- `200 OK`: liberación procesada correctamente.
- `404 Not Found`: reserva inexistente.
- `409 Conflict`: conflicto con el estado de la reserva.

Los lugares asociados a una reserva liberada dejan de considerarse ocupados.

La operación contempla solicitudes repetidas para evitar liberaciones duplicadas y modificaciones incompatibles sobre reservas confirmadas.

## 5. Operaciones de integración con Eventos y Lugares

### 5.1. Habilitar un evento

**Método:** `POST`

**Ruta:** `/reservas/evento/{eventoId}/habilitar`

**Descripción:**

Permite preparar el registro de bloqueo correspondiente a un evento para administrar las reservas concurrentes.

La operación es utilizada por Eventos y Lugares cuando se habilita la venta.

**Parámetro de ruta:**

- `eventoId`: identificador del evento.

**Request Body:** no requiere.

**Ejemplo de solicitud:**

```http
POST /reservas/evento/20/habilitar
```

**Respuesta — `204 No Content`:**

La operación no devuelve cuerpo de respuesta.

**Códigos HTTP:**

- `204 No Content`: evento habilitado correctamente.
- `400 Bad Request`: identificador inválido.
- `404 Not Found`: evento inexistente.
- `409 Conflict`: evento no publicado o cancelado.
- `503 Service Unavailable`: no se pudo consultar Eventos y Lugares.

El servicio verifica que el evento exista, se encuentre en estado `PUBLICADO` y no esté cancelado.

La operación es idempotente, por lo que puede repetirse sin crear registros de bloqueo duplicados.

### 5.2. Cancelar reservas de un evento

**Método:** `POST`

**Ruta:** `/reservas/evento/{eventoId}/cancelar`

**Descripción:**

Permite registrar la cancelación local de un evento, impedir nuevas reservas y cancelar las reservas temporales activas que le corresponden.

Es una operación utilizada por Eventos y Lugares durante la coordinación del proceso de cancelación.

**Parámetro de ruta:**

- `eventoId`: identificador del evento.

**Request Body:** no requiere.

**Ejemplo de solicitud:**

```http
POST /reservas/evento/20/cancelar
```

**Respuesta — `204 No Content`:**

La operación no devuelve cuerpo de respuesta.

**Códigos HTTP:**

- `204 No Content`: cancelación local aplicada.
- `409 Conflict`: conflicto de estado.

Reservas registra la cancelación del evento, bloquea nuevas operaciones y actualiza las reservas temporales correspondientes.

Las entradas emitidas y los reintegros son administrados por Entradas y Compras, respectivamente.

## 6. Devolución individual de entradas

### 6.1. Liberar el lugar de una entrada devuelta

**Método:** `POST`

**Ruta:** `/reservas/{id}/devolver-entrada`

**Descripción:**

Permite liberar individualmente el lugar correspondiente a una entrada cuya devolución fue aprobada.

El servicio que realiza la solicitud debe haber validado previamente la titularidad de la entrada, la aprobación de la devolución y el plazo correspondiente.

**Parámetro de ruta:**

- `id`: identificador de la reserva.

**Request Body:**

```json
{
  "entradaId": 1001,
  "sectorId": 5,
  "asientoId": 25
}
```

El cuerpo utiliza `DevolverEntradaDTO`.

**Campos de la solicitud:**

| Campo | Tipo | Obligatorio | Descripción |
|---|---|---|---|
| `entradaId` | Long | Sí | Identificador único de la entrada devuelta. |
| `sectorId` | Long | Sí | Sector al que pertenece la entrada. |
| `asientoId` | Long | Según sector | Asiento correspondiente, cuando el sector es numerado. |

Para sectores no numerados, `asientoId` puede ser `null`.

**Respuesta — `200 OK`:**

```json
{
  "id": 1,
  "clienteId": 1,
  "eventoId": 20,
  "fechaCreacion": "2026-11-09T15:30:00",
  "fechaVencimiento": "2026-11-09T15:40:00",
  "estado": "CONFIRMADA",
  "detalles": [
    {
      "id": 1,
      "sectorId": 5,
      "cantidad": 2,
      "asientoIds": [26],
      "cantidadLiberada": 1
    }
  ]
}
```

**Códigos HTTP:**

- `200 OK`: devolución individual procesada correctamente.
- `400 Bad Request`: solicitud inválida.
- `404 Not Found`: reserva inexistente.
- `409 Conflict`: la entrada no puede devolverse por las reglas de negocio o el estado de la reserva.

La operación actualiza la ocupación individual del lugar y registra el identificador de la entrada procesada.

Esto permite evitar que una misma devolución libere dos veces el lugar correspondiente.

El estado general de la reserva puede continuar siendo `CONFIRMADA`, aunque uno de sus lugares haya sido liberado.

## 7. Estados de las reservas

El microservicio administra los siguientes estados:

| Estado | Descripción |
|---|---|
| `ACTIVA` | Reserva temporal registrada, cuya vigencia depende de `fechaVencimiento`. |
| `CONFIRMADA` | Reserva confirmada después de la aprobación del pago. |
| `VENCIDA` | Reserva cuyo plazo terminó y cuyo estado persistido fue actualizado. |
| `LIBERADA` | Reserva liberada mediante una operación correspondiente. |
| `CANCELADA` | Reserva invalidada por la cancelación del evento. |

### 7.1. Vencimiento temporal

Cada reserva temporal posee una vigencia de 10 minutos desde su creación.

La disponibilidad considera como ocupadas las reservas `CONFIRMADA` y las reservas `ACTIVA` que todavía no vencieron.

Cuando una reserva activa supera su fecha de vencimiento, deja de bloquear lugares aunque su estado persistido todavía no haya cambiado a `VENCIDA`.

Este mecanismo permite recuperar disponibilidad sin requerir un proceso programado de liberación.

## 8. Manejo de errores

El microservicio utiliza un manejador global de excepciones para convertir los errores de validación, estado e integración en respuestas HTTP.

### 8.1. Formato de error

Las respuestas gestionadas mediante `ReservaExceptionHandler` utilizan el DTO `ErrorDTO`.

**Ejemplo — `409 Conflict`:**

```json
{
  "estado": 409,
  "mensaje": "Conflicto de unicidad o integridad de datos"
}
```

**Ejemplo — `400 Bad Request`:**

```json
{
  "estado": 400,
  "mensaje": "JSON inválido"
}
```

**Ejemplo — `503 Service Unavailable`:**

```json
{
  "estado": 503,
  "mensaje": "No se pudo consultar el microservicio Eventos y Lugares"
}
```

### 8.2. Códigos HTTP

| Código | Descripción |
|---|---|
| `200 OK` | Consulta o actualización realizada correctamente. |
| `201 Created` | Reserva creada o recuperada mediante una solicitud idempotente. |
| `204 No Content` | Operación interna procesada sin cuerpo de respuesta. |
| `400 Bad Request` | JSON inválido, parámetros incorrectos o datos de solicitud no válidos. |
| `404 Not Found` | Reserva o recurso externo inexistente. |
| `409 Conflict` | Conflicto de disponibilidad, estado, unicidad o integridad de datos. |
| `503 Service Unavailable` | Error de comunicación con Eventos y Lugares. |

Los mensajes de error dependen de la validación o excepción que origine la respuesta.

## 9. Comunicación con otros microservicios

Reservas utiliza OpenFeign para consultar la información necesaria de Eventos y Lugares.

También expone operaciones internas utilizadas por Compras, Entradas y Eventos y Lugares.

### 9.1. Endpoints externos consumidos

| Microservicio | Método | Ruta | Función |
|---|---|---|---|
| Eventos y Lugares | GET | `/eventos/{id}` | Consultar información de un evento. |
| Eventos y Lugares | GET | `/sectores/{id}` | Consultar información de un sector. |
| Eventos y Lugares | GET | `/asientos/{id}` | Consultar un asiento. |
| Eventos y Lugares | GET | `/sectores/{id}/asientos` | Obtener asientos de un sector. |
| Eventos y Lugares | GET | `/eventos/{eventoId}/sectores/{sectorId}/precio` | Consultar el precio configurado para un sector y evento. |

El cliente utilizado es `EventoClient`.

Su dirección base se configura mediante:

`msvc.eventos.url=http://localhost:8081`

### 9.2. Operaciones ofrecidas a otros servicios

| Microservicio consumidor | Método | Ruta | Función |
|---|---|---|---|
| Compras | POST | `/reservas/{id}/confirmar` | Confirmar una reserva después de aprobar el pago. |
| Compras | POST | `/reservas/{id}/liberar` | Liberar una reserva ante un pago rechazado. |
| Eventos y Lugares | POST | `/reservas/evento/{eventoId}/habilitar` | Preparar las reservas de un evento habilitado. |
| Eventos y Lugares | POST | `/reservas/evento/{eventoId}/cancelar` | Registrar la cancelación y bloquear nuevas reservas. |
| Entradas | POST | `/reservas/{id}/devolver-entrada` | Liberar individualmente un lugar por una devolución aprobada. |

Las operaciones de integración utilizan los identificadores de los recursos y los DTO necesarios para intercambiar información sin compartir entidades JPA ni bases de datos.

## 10. Consideraciones generales

- La creación de reservas utiliza una clave de idempotencia para evitar duplicados ante reintentos.
- La ocupación se controla mediante transacciones locales y bloqueo pesimista.
- La consulta de disponibilidad no garantiza por sí sola la reserva de un lugar.
- Las reservas temporales tienen una vigencia de 10 minutos.
- Los lugares de reservas vencidas dejan de considerarse ocupados.
- Las reservas confirmadas permanecen ocupando los lugares adquiridos.
- La liberación de reservas se utiliza para devolver lugares al inventario cuando corresponde.
- La cancelación de un evento impide nuevas reservas y afecta las reservas temporales existentes.
- Las devoluciones individuales utilizan `entradaId` para evitar liberar dos veces el mismo lugar.
- Los datos de eventos, sectores y asientos pertenecen a Eventos y Lugares.
- Las comunicaciones entre microservicios se realizan mediante APIs REST y clientes HTTP.
- Cada microservicio administra su propia base de datos.