# 03 — Contratos REST de msvc-compras

## 1. Alcance

Este documento describe los contratos de la API REST del microservicio Compras y Pagos (`msvc-compras`), incluyendo los endpoints disponibles, los parámetros de entrada, los formatos de respuesta, los códigos HTTP y las comunicaciones con otros microservicios.

Se distinguen:

- **Endpoints propios:** operaciones expuestas por los controladores de Compras.
- **Comunicación entre microservicios:** operaciones invocadas mediante clientes Feign.
- **Validaciones y reglas:** condiciones de negocio que afectan las respuestas.

---

## 2. Endpoints propios de Compras

| Operación | Método y ruta | Entrada | Respuesta |
|---|---|---|---|
| Iniciar compra | `POST /compras` | `IniciarCompraDTO` | `201 Created` — `CompraDTO` |
| Consultar compra | `GET /compras/{id}` | ID de compra | `200 OK` — `CompraDTO` |
| Confirmar compra | `POST /compras/{id}/confirmar` | ID de compra | `200 OK` — `CompraDTO` |
| Consultar estado de confirmación | `GET /compras/{id}/confirmacion` | ID de compra | `200 OK` — `ConfirmacionCompraDTO` |
| Procesar pago | `POST /compras/{id}/pagar` | `ProcesarPagoDTO` | `200 OK` — `PagoDTO` |
| Reintentar liberación de reserva | `POST /compras/{id}/reintentar-liberacion` | ID de compra | `204 No Content` |
| Registrar reintegro | `POST /compras/reintegros` | `SolicitudReintegroDTO` | `200 OK` — `ReintegroDTO` |
| Consultar reintegros | `GET /compras/{id}/reintegros` | ID de compra | `200 OK` — lista de `ReintegroDTO` |
| Cancelar compras de un evento | `POST /compras/evento/{eventoId}/cancelar` | ID de evento y encabezado `X-Operacion-Id` | `204 No Content` |
| Compensar compra | `POST /compras/{id}/compensar` | ID de compra | `204 No Content` |

### 2.1. Iniciar compra

**Request**

```http
POST /compras
Content-Type: application/json
```

```json
{
  "clienteId": 200,
  "reservaId": 9
}
```

El cuerpo utiliza `IniciarCompraDTO`:

| Campo | Tipo | Descripción |
|---|---|---|
| `clienteId` | Long | Cliente que inicia la compra. Debe coincidir con el cliente de la reserva. |
| `reservaId` | Long | Reserva temporal que se desea comprar. |

**Response — `201 Created`**

Devuelve un `CompraDTO` con la información de la compra creada:

```json
{
  "id": 1,
  "clienteId": 200,
  "reservaId": 9,
  "eventoId": 21,
  "fechaCompra": "2026-11-05",
  "total": 30000.00,
  "estado": "PENDIENTE",
  "detalles": []
}
```

El ejemplo muestra la estructura general. Los elementos internos de `detalles` corresponden a `DetalleCompraRespuestaDTO`.

El cliente no envía precios ni el importe total. Compras obtiene la información de la reserva y consulta los precios de los sectores en Eventos. Luego calcula el importe y conserva los precios utilizados para la compra.

**Respuestas HTTP:**

- `201`: compra iniciada.
- `400`: solicitud o datos inválidos.
- `404`: reserva inexistente.
- `409`: reserva vencida, inactiva o ya asociada a otra compra.
- `503`: microservicio externo no disponible.

### 2.2. Consultar compra

**Request**

```http
GET /compras/1
```

**Response — `200 OK`**

Devuelve un `CompraDTO`, con los siguientes campos:

| Campo | Tipo |
|---|---|
| `id` | Long |
| `clienteId` | Long |
| `reservaId` | Long |
| `eventoId` | Long |
| `fechaCompra` | LocalDate |
| `total` | BigDecimal |
| `estado` | String |
| `detalles` | List de `DetalleCompraRespuestaDTO` |

**Respuestas HTTP:**

- `200`: compra encontrada.
- `400`: identificador inválido.
- `404`: compra inexistente.

### 2.3. Confirmar compra

**Request**

```http
POST /compras/1/confirmar
```

No requiere un cuerpo específico.

Esta operación coordina la confirmación de la reserva y la emisión de las entradas correspondientes. La compra no debe quedar en estado `CONFIRMADA` hasta completar correctamente el proceso.

**Response — `200 OK`**

Devuelve un `CompraDTO` actualizado.

**Respuestas HTTP:**

- `200`: compra confirmada o ya confirmada.
- `400`: identificador inválido.
- `404`: compra o reserva inexistente.
- `409`: pago no aprobado, reserva vencida o problema con la emisión.
- `503`: microservicio de Reservas o Entradas no disponible.

### 2.4. Consultar estado de confirmación

**Request**

```http
GET /compras/1/confirmacion
```

**Response — `200 OK`**

Devuelve un `ConfirmacionCompraDTO`:

```json
{
  "id": 1,
  "clienteId": 200,
  "reservaId": 10,
  "estado": "CONFIRMACION_PENDIENTE",
  "pagoAprobado": true
}
```

| Campo | Tipo | Descripción |
|---|---|---|
| `id` | Long | Identificador de la compra. |
| `clienteId` | Long | Identificador del cliente. |
| `reservaId` | Long | Identificador de la reserva. |
| `estado` | String | Estado actual de la compra. |
| `pagoAprobado` | Boolean | Indica si el pago fue aprobado. |

**Respuestas HTTP:**

- `200`: información de confirmación.
- `404`: compra inexistente.

### 2.5. Procesar pago

**Request**

```http
POST /compras/1/pagar
Content-Type: application/json
```

```json
{
  "medioPago": "TARJETA",
  "claveIdempotencia": "PAGO-001"
}
```

El cuerpo utiliza `ProcesarPagoDTO`:

| Campo | Tipo | Descripción |
|---|---|---|
| `medioPago` | String | Medio de pago seleccionado. |
| `claveIdempotencia` | String | Clave única utilizada para reconocer reintentos de una misma operación. |

**Response — `200 OK`**

Devuelve un `PagoDTO`:

```json
{
  "id": 1,
  "compraId": 1,
  "importe": 30000.00,
  "medioPago": "TARJETA",
  "estado": "APROBADO",
  "fechaSolicitud": "2026-11-05T21:15:00",
  "fechaPago": "2026-11-05T21:15:02",
  "referenciaExterna": "MOCK-PAGO-001"
}
```

| Campo | Tipo | Descripción |
|---|---|---|
| `id` | Long | Identificador del pago. |
| `compraId` | Long | Compra asociada. |
| `importe` | BigDecimal | Importe procesado. |
| `medioPago` | String | Medio de pago utilizado. |
| `estado` | String | Estado del pago: `PENDIENTE`, `PROCESANDO`, `APROBADO`, `RECHAZADO` o `ERROR`. |
| `fechaSolicitud` | LocalDateTime | Fecha y hora de solicitud. |
| `fechaPago` | LocalDateTime | Fecha y hora del resultado. |
| `referenciaExterna` | String | Identificador de la operación en la pasarela simulada. |

**Respuestas HTTP:**

- `200`: resultado del pago.
- `400`: solicitud inválida o medio de pago no válido.
- `404`: compra o reserva inexistente.
- `409`: compra no pendiente, reserva vencida o conflicto de idempotencia.
- `503`: microservicio de Reservas no disponible.

No se envían ni almacenan datos sensibles de tarjetas, como número o CVV. La pasarela utilizada es simulada.

### 2.6. Reintentar liberación de reserva

**Request**

```http
POST /compras/1/reintentar-liberacion
```

Permite reintentar la liberación de una reserva cuando una operación anterior no pudo completarla.

**Response — `204 No Content`**

No devuelve cuerpo.

**Respuestas HTTP:**

- `204`: operación realizada.
- `400`: identificador inválido.
- `404`: compra o reserva inexistente.
- `409`: no existe una liberación pendiente.
- `503`: microservicio de Reservas no disponible.

### 2.7. Registrar reintegro

**Request**

```http
POST /compras/reintegros
Content-Type: application/json
```

```json
{
  "entradaId": 100,
  "motivo": "DEVOLUCION_CLIENTE"
}
```

El cuerpo utiliza `SolicitudReintegroDTO`:

| Campo | Tipo | Descripción |
|---|---|---|
| `entradaId` | Long | Identificador de la entrada cuya devolución fue aprobada. |
| `motivo` | String | Motivo informado por Entradas. |

Este endpoint recibe el aviso de una devolución aprobada por el microservicio de Entradas. Compras verifica la operación y registra el reintegro correspondiente.

**Response — `200 OK`**

Devuelve un `ReintegroDTO`:

```json
{
  "id": 1,
  "pagoId": 1,
  "compraId": 1,
  "entradaId": 100,
  "importe": 10000.00,
  "estado": "APROBADO",
  "motivo": "DEVOLUCION_CLIENTE",
  "fechaSolicitud": "2026-11-05T22:30:00",
  "fechaReintegro": "2026-11-05T22:30:02",
  "referenciaExterna": "MOCK-REINTEGRO-001"
}
```

**Respuestas HTTP:**

- `200`: reintegro registrado o existente.
- `400`: identificador de entrada inválido.
- `404`: entrada inexistente en Entradas.
- `409`: devolución no aprobada o compra no confirmada.
- `503`: microservicio de Entradas no disponible.

### 2.8. Consultar reintegros

**Request**

```http
GET /compras/1/reintegros
```

**Response — `200 OK`**

Devuelve una lista de objetos `ReintegroDTO`, con los campos descriptos en el apartado anterior.

**Respuestas HTTP:**

- `200`: lista de reintegros.
- `404`: compra inexistente.

### 2.9. Cancelar compras de un evento

**Request**

```http
POST /compras/evento/21/cancelar
X-Operacion-Id: CANCELACION-EVENTO-21
```

Esta operación recibe la notificación de cancelación de un evento y procesa los reintegros correspondientes.

El encabezado `X-Operacion-Id` identifica la operación de cancelación. El procesamiento evita reintegros duplicados mediante la verificación de los reintegros registrados para cada ítem de compra, permitiendo repetir la solicitud sin devolver nuevamente importes ya reintegrados.

**Response — `204 No Content`**

No devuelve cuerpo.

**Respuestas HTTP:**

- `204`: operación procesada.
- `400`: datos inválidos.
- `409`: existe una compra pagada que todavía no tiene las entradas emitidas.

### 2.10. Compensar compra

**Request**

```http
POST /compras/1/compensar
```

Permite ejecutar o reintentar la compensación de una compra ante una situación en la que no se pudo completar el proceso de confirmación.

**Response — `204 No Content`**

No devuelve cuerpo.

**Respuestas HTTP:**

- `204`: compensación realizada o ya completada.
- `400`: identificador de compra inválido.
- `404`: compra inexistente.
- `409`: compra no compensable o con entradas ya emitidas.

---

## 3. Comunicación con otros microservicios

Compras utiliza clientes Feign para comunicarse con Reservas, Eventos y Entradas.

Estas rutas representan llamadas salientes desde Compras, no endpoints propios de sus controladores.

### 3.1. Comunicación con Reservas

Cliente: `ReservaClient`.

| Método | Ruta | Uso |
|---|---|---|
| GET | `/reservas/{id}` | Consultar una reserva. |
| POST | `/reservas/{id}/confirmar` | Confirmar una reserva. |
| POST | `/reservas/{id}/liberar` | Liberar una reserva. |

La información de la reserva permite validar su estado, vigencia, cliente y detalles antes de continuar con la compra.

### 3.2. Comunicación con Eventos

Cliente: `EventoClient`.

| Método | Ruta | Uso |
|---|---|---|
| GET | `/eventos/{eventoId}/sectores/{sectorId}/precio` | Obtener el precio de un sector para un evento. |
| GET | `/eventos/{id}` | Consultar información del evento. |

Compras utiliza el precio obtenido de Eventos para calcular el total y conservar el importe histórico de los ítems de la compra.

### 3.3. Comunicación con Entradas

Cliente: `EntradaClient`.

| Método | Ruta | Uso |
|---|---|---|
| POST | `/entradas/emitir` | Solicitar la emisión de entradas. |
| POST | `/entradas/evento/{eventoId}/invalidar` | Solicitar la invalidación de entradas de un evento. |
| GET | `/entradas/cliente/{clienteId}` | Consultar las entradas de un cliente. |
| GET | `/entradas/{id}` | Consultar una entrada, enviando el encabezado `X-Cliente-Id`. |

La emisión utiliza una solicitud `SolicitudEmisionDTO` y devuelve una lista de `EntradaDTO`.

La consulta individual de una entrada requiere el encabezado `X-Cliente-Id`.

---

## 4. Validaciones y reglas de negocio relacionadas con los contratos

- Al iniciar una compra, la reserva debe existir, estar `ACTIVA` y no haber vencido.
- El cliente informado debe coincidir con el cliente asociado a la reserva.
- No se debe iniciar más de una compra para la misma reserva.
- El importe se calcula en Compras a partir de los precios consultados en Eventos. El cliente no puede establecer el precio ni el total.
- Para procesar el pago, la compra debe encontrarse en un estado que permita realizar la operación.
- La clave de idempotencia permite reconocer reintentos y evitar cobros duplicados.
- Una compra no debe pasar a `CONFIRMADA` hasta completar la confirmación de la reserva y la emisión de todas las entradas esperadas.
- Los reintegros se calculan utilizando el importe histórico pagado por la entrada.
- Las operaciones de devolución y cancelación deben poder reconocer reintentos para evitar reintegros duplicados.
- Las operaciones de compensación y liberación permiten recuperar procesos que quedaron pendientes por fallas en las comunicaciones entre microservicios.