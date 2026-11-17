# 04 — Contratos REST de Fila Virtual

## 1. Descripción general

El microservicio `msvc-fila-virtual` expone una API REST para gestionar el ingreso de clientes a filas virtuales, consultar posiciones, habilitar turnos y controlar su utilización.

Las operaciones permiten administrar las autorizaciones temporales correspondientes a eventos de alta demanda.

La API utiliza JSON para el intercambio de datos y está documentada mediante Swagger/OpenAPI.

**URL base local:**

`http://localhost:8086`

**Ruta principal:**

`/filas`

## 2. Resumen de endpoints

| Método | Endpoint | Descripción |
|---|---|---|
| POST | `/filas/{eventoId}/ingresar` | Registra un cliente en la fila virtual. |
| GET | `/filas/{eventoId}/clientes/{clienteId}` | Consulta el estado y posición del cliente. |
| POST | `/filas/{eventoId}/habilitar` | Habilita una tanda de clientes. |
| GET | `/filas/{eventoId}/clientes/{clienteId}/validar` | Verifica si el turno está habilitado y vigente. |
| POST | `/filas/{eventoId}/clientes/{clienteId}/finalizar` | Finaliza un turno utilizado. |

## 3. Ingresar a una fila virtual

### POST /filas/{eventoId}/ingresar

Registra un cliente en la fila virtual de un evento publicado y configurado como de alta demanda.

El ingreso es idempotente: si el cliente ya tiene un registro para ese evento, se devuelve el registro existente.

### Parámetros

| Parámetro | Ubicación | Tipo | Obligatorio |
|---|---|---|---|
| `eventoId` | Path | Long | Sí |
| `clienteId` | Body | Long | Sí |

Ambos identificadores deben ser positivos.

### Request

```http
POST http://localhost:8086/filas/1/ingresar
Content-Type: application/json
```

```json
{
  "clienteId": 10
}
```

### Respuesta exitosa — HTTP 200

```json
{
  "id": 1,
  "eventoId": 1,
  "clienteId": 10,
  "fechaIngreso": "2026-11-17T13:05:08",
  "estado": "EN_ESPERA",
  "posicion": 1,
  "fechaHabilitacion": null,
  "fechaVencimiento": null
}
```

El sistema registra la fecha de ingreso y asigna el estado inicial `EN_ESPERA`.

La posición se calcula considerando los clientes que permanecen esperando y que ingresaron anteriormente.

### Respuestas de error

| Código | Situación |
|---|---|
| 400 | Identificadores inválidos o solicitud incorrecta. |
| 404 | El evento no existe. |
| 409 | El evento no está publicado o no requiere fila virtual. |
| 503 | No se puede obtener la información de Eventos y Lugares. |

## 4. Consultar el estado de un cliente

### GET /filas/{eventoId}/clientes/{clienteId}

Obtiene el registro de un cliente dentro de la fila virtual de un evento.

### Parámetros

| Parámetro | Ubicación | Tipo | Obligatorio |
|---|---|---|---|
| `eventoId` | Path | Long | Sí |
| `clienteId` | Path | Long | Sí |

### Request

```http
GET http://localhost:8086/filas/1/clientes/10
```

### Respuesta exitosa — HTTP 200

```json
{
  "id": 1,
  "eventoId": 1,
  "clienteId": 10,
  "fechaIngreso": "2026-11-17T13:05:08",
  "estado": "EN_ESPERA",
  "posicion": 1,
  "fechaHabilitacion": null,
  "fechaVencimiento": null
}
```

Cuando el cliente se encuentra en estado `EN_ESPERA`, se informa su posición actual.

Para otros estados, el campo `posicion` se devuelve como `null`.

### Respuestas de error

| Código | Situación |
|---|---|
| 400 | Identificadores inválidos o cliente no registrado en la fila. |

## 5. Habilitar una tanda de clientes

### POST /filas/{eventoId}/habilitar

Habilita una cantidad determinada de clientes respetando el orden de llegada.

Los clientes seleccionados pasan al estado `HABILITADO` y reciben una autorización temporal de 10 minutos.

### Parámetros

| Parámetro | Ubicación | Tipo | Obligatorio |
|---|---|---|---|
| `eventoId` | Path | Long | Sí |
| `cantidad` | Body | Integer | Sí |

La cantidad debe encontrarse entre 1 y 100.

### Request

```http
POST http://localhost:8086/filas/1/habilitar
Content-Type: application/json
```

```json
{
  "cantidad": 2
}
```

### Respuesta exitosa — HTTP 200

```json
[
  {
    "id": 1,
    "eventoId": 1,
    "clienteId": 10,
    "fechaIngreso": "2026-11-17T13:05:08",
    "estado": "HABILITADO",
    "posicion": null,
    "fechaHabilitacion": "2026-11-17T13:10:00",
    "fechaVencimiento": "2026-11-17T13:20:00"
  },
  {
    "id": 2,
    "eventoId": 1,
    "clienteId": 20,
    "fechaIngreso": "2026-11-17T13:06:00",
    "estado": "HABILITADO",
    "posicion": null,
    "fechaHabilitacion": "2026-11-17T13:10:00",
    "fechaVencimiento": "2026-11-17T13:20:00"
  }
]
```

La respuesta contiene los clientes habilitados.

Si existen menos clientes esperando que la cantidad solicitada, se habilitan únicamente los disponibles.

### Respuestas de error

| Código | Situación |
|---|---|
| 400 | Identificador de evento o cantidad inválidos. |
| 409 | Existe una tanda activa o no quedan clientes esperando. |

## 6. Validar un turno

### GET /filas/{eventoId}/clientes/{clienteId}/validar

Comprueba si un cliente se encuentra habilitado y si su autorización continúa vigente.

La validación no consume ni finaliza el turno.

### Parámetros

| Parámetro | Ubicación | Tipo | Obligatorio |
|---|---|---|---|
| `eventoId` | Path | Long | Sí |
| `clienteId` | Path | Long | Sí |

### Request

```http
GET http://localhost:8086/filas/1/clientes/10/validar
```

### Respuesta exitosa — HTTP 200

```json
{
  "eventoId": 1,
  "clienteId": 10,
  "autorizado": true,
  "estado": "HABILITADO",
  "fechaVencimiento": "2026-11-17T13:20:00"
}
```

El atributo `autorizado` indica si el cliente puede utilizar su turno.

### Criterios de autorización

| Estado del cliente | Resultado |
|---|---|
| `EN_ESPERA` | `false` |
| `HABILITADO` y vigente | `true` |
| `VENCIDO` | `false` |
| `FINALIZADO` | `false` |

Si un turno habilitado superó su fecha de vencimiento, se considera vencido y no se autoriza su utilización.

### Respuestas de error

| Código | Situación |
|---|---|
| 400 | Identificadores inválidos. |
| 404 | El cliente no está registrado en la fila del evento. |

## 7. Finalizar un turno

### POST /filas/{eventoId}/clientes/{clienteId}/finalizar

Marca como `FINALIZADO` un turno que se encuentra habilitado y vigente.

Esta operación permite cerrar una autorización utilizada durante el proceso de reserva.

### Parámetros

| Parámetro | Ubicación | Tipo | Obligatorio |
|---|---|---|---|
| `eventoId` | Path | Long | Sí |
| `clienteId` | Path | Long | Sí |

### Request

```http
POST http://localhost:8086/filas/1/clientes/10/finalizar
```

No requiere cuerpo JSON.

### Respuesta exitosa — HTTP 200

```json
{
  "id": 1,
  "eventoId": 1,
  "clienteId": 10,
  "fechaIngreso": "2026-11-17T13:05:08",
  "estado": "FINALIZADO",
  "posicion": null,
  "fechaHabilitacion": "2026-11-17T13:10:00",
  "fechaVencimiento": "2026-11-17T13:20:00"
}
```

La operación es idempotente.

Si el turno ya se encuentra `FINALIZADO`, una solicitud repetida devuelve el registro existente sin modificar nuevamente su estado.

### Respuestas de error

| Código | Situación |
|---|---|
| 400 | Identificadores inválidos. |
| 404 | El cliente no está registrado. |
| 409 | El turno no está habilitado o su período de validez terminó. |

## 8. DTO utilizados

La API utiliza los siguientes objetos de transferencia de datos.

### 8.1. IngresarFilaDTO

Representa la solicitud de ingreso a una fila.

| Atributo | Tipo | Descripción |
|---|---|---|
| `clienteId` | Long | Identificador del cliente. Obligatorio y positivo. |

### 8.2. HabilitarTandaDTO

Representa una solicitud de habilitación.

| Atributo | Tipo | Descripción |
|---|---|---|
| `cantidad` | Integer | Cantidad de clientes que se desea habilitar. Valor entre 1 y 100. |

### 8.3. ClienteEnFilaDTO

Representa la información de un cliente registrado en una fila.

| Atributo | Tipo | Descripción |
|---|---|---|
| `id` | Long | Identificador del registro. |
| `eventoId` | Long | Identificador del evento. |
| `clienteId` | Long | Identificador del cliente. |
| `fechaIngreso` | LocalDateTime | Fecha y hora de ingreso. |
| `estado` | EstadoFila | Estado actual. |
| `posicion` | Long | Posición actual, cuando corresponde. |
| `fechaHabilitacion` | LocalDateTime | Fecha y hora de habilitación. |
| `fechaVencimiento` | LocalDateTime | Fecha y hora de vencimiento. |

### 8.4. ValidacionTurnoDTO

Representa el resultado de la validación de una autorización.

| Atributo | Tipo | Descripción |
|---|---|---|
| `eventoId` | Long | Identificador del evento. |
| `clienteId` | Long | Identificador del cliente. |
| `autorizado` | boolean | Indica si el turno puede utilizarse. |
| `estado` | EstadoFila | Estado actual del turno. |
| `fechaVencimiento` | LocalDateTime | Fecha límite de la autorización. |

### 8.5. EventoDTO

Representa los datos utilizados para validar un evento mediante la integración con Eventos y Lugares.

| Atributo | Tipo | Descripción |
|---|---|---|
| `id` | Long | Identificador del evento. |
| `nombre` | String | Nombre del evento. |
| `estado` | String | Estado actual del evento. |
| `altaDemanda` | boolean | Indica si requiere fila virtual. |

## 9. Validaciones generales

La API aplica las siguientes reglas:

- Los identificadores de evento y cliente deben ser positivos.
- Los clientes solo pueden ingresar a filas de eventos publicados y de alta demanda.
- Una combinación de evento y cliente no puede generar registros duplicados.
- La habilitación debe respetar el orden de llegada.
- No pueden habilitarse tandas superpuestas para el mismo evento.
- Los turnos habilitados tienen una vigencia de 10 minutos.
- Los turnos vencidos o finalizados no autorizan nuevas operaciones.
- La validación no consume el turno.
- La finalización repetida de un turno es idempotente.

## 10. Documentación OpenAPI

Los endpoints se encuentran documentados mediante anotaciones de Swagger/OpenAPI.

La documentación interactiva puede consultarse con el microservicio ejecutándose en:

**Swagger UI:**

`http://localhost:8086/swagger-ui/index.html`

**Especificación OpenAPI:**

`http://localhost:8086/v3/api-docs`

Swagger permite consultar los contratos HTTP, parámetros, estructuras de respuesta y códigos de estado definidos para las operaciones.
