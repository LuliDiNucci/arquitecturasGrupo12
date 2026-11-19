# 04 — Contratos REST de Fila Virtual

## 1. Descripción general

El microservicio `msvc-fila-virtual` expone una API REST para gestionar el ingreso de clientes a filas virtuales, consultar posiciones, habilitar turnos y controlar su utilización.

Las operaciones permiten administrar las autorizaciones temporales correspondientes a eventos de alta demanda.

La API utiliza JSON para el intercambio de datos y está documentada mediante Swagger/OpenAPI.

### Dirección del microservicio

**URL local directa:**

`http://localhost:8088`

**URL mediante API Gateway:**

`http://localhost:8084`

**Ruta principal:**

`/filas`

En Docker, los demás microservicios pueden comunicarse con Fila Virtual mediante:

`http://fila-virtual:8088`

Las solicitudes externas deben utilizar el API Gateway.

### Seguridad

Los endpoints están protegidos mediante autenticación JWT.

Las solicitudes externas autenticadas deben incluir:

`Authorization: Bearer <token>`

Las operaciones internas de validación y finalización utilizan credenciales de servicio correspondientes a Reservas.

## 2. Resumen de endpoints

| Método | Endpoint                                           | Descripción                                     | Acceso              |
| ------ | -------------------------------------------------- | ----------------------------------------------- | ------------------- |
| POST   | `/filas/{eventoId}/ingresar`                       | Registra un cliente en la fila.                 | Cliente autenticado |
| GET    | `/filas/{eventoId}/clientes/{clienteId}`           | Consulta el estado y posición del cliente.      | Cliente autenticado |
| POST   | `/filas/{eventoId}/habilitar`                      | Habilita una tanda de clientes.                 | Administrador       |
| GET    | `/filas/{eventoId}/clientes/{clienteId}/validar`   | Verifica si el turno está habilitado y vigente. | Servicio Reservas   |
| POST   | `/filas/{eventoId}/clientes/{clienteId}/finalizar` | Finaliza un turno utilizado.                    | Servicio Reservas   |

Las operaciones de cliente están sujetas a las verificaciones de identidad correspondientes para impedir el acceso a registros de otros usuarios.

## 3. Ingresar a una fila virtual

### POST /filas/{eventoId}/ingresar

Registra un cliente en la fila virtual de un evento publicado y configurado como de alta demanda.

El ingreso es idempotente: si el cliente ya tiene un registro para ese evento, se devuelve el registro existente.

### Parámetros

| Parámetro   | Ubicación | Tipo | Obligatorio |
| ----------- | --------- | ---- | ----------- |
| `eventoId`  | Path      | Long | Sí          |
| `clienteId` | Body      | Long | Sí          |

Ambos identificadores deben ser positivos.

### Request

```http
POST /filas/1/ingresar
Authorization: Bearer <tokenCliente>
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
  "fechaIngreso": "2026-11-19T13:05:08",
  "estado": "EN_ESPERA",
  "posicion": 1,
  "fechaHabilitacion": null,
  "fechaVencimiento": null
}
```

El sistema registra la fecha de ingreso y asigna el estado inicial `EN_ESPERA`.

La posición se calcula considerando los clientes que permanecen esperando y que ingresaron anteriormente.

Si el cliente ya está registrado, se devuelve el registro existente sin crear uno nuevo.

### Respuestas de error

| Código | Situación                                                       |
| ------ | --------------------------------------------------------------- |
| 400    | Identificadores inválidos o solicitud incorrecta.               |
| 401    | No se proporcionó una autenticación válida.                     |
| 403    | El usuario no tiene permiso para registrar al cliente indicado. |
| 404    | El evento no existe.                                            |
| 409    | El evento no está publicado o no requiere fila virtual.         |
| 503    | No se puede obtener la información de Eventos y Lugares.        |

## 4. Consultar el estado de un cliente

### GET /filas/{eventoId}/clientes/{clienteId}

Obtiene el registro de un cliente dentro de la fila virtual de un evento.

### Parámetros

| Parámetro   | Ubicación | Tipo | Obligatorio |
| ----------- | --------- | ---- | ----------- |
| `eventoId`  | Path      | Long | Sí          |
| `clienteId` | Path      | Long | Sí          |

### Request

```http
GET /filas/1/clientes/10
Authorization: Bearer <tokenCliente>
```

### Respuesta exitosa — HTTP 200

```json
{
  "id": 1,
  "eventoId": 1,
  "clienteId": 10,
  "fechaIngreso": "2026-11-19T13:05:08",
  "estado": "EN_ESPERA",
  "posicion": 1,
  "fechaHabilitacion": null,
  "fechaVencimiento": null
}
```

Cuando el cliente se encuentra en estado `EN_ESPERA`, se informa su posición actual.

Para otros estados, el campo `posicion` se devuelve como `null`.

La consulta no modifica el orden de ingreso.

### Respuestas de error

| Código | Situación                                                       |
| ------ | --------------------------------------------------------------- |
| 400    | Identificadores inválidos.                                      |
| 401    | No se proporcionó una autenticación válida.                     |
| 403    | El usuario no tiene permiso para consultar al cliente indicado. |
| 404    | El cliente no está registrado en la fila del evento.            |

## 5. Habilitar una tanda de clientes

### POST /filas/{eventoId}/habilitar

Habilita una cantidad determinada de clientes respetando el orden de llegada.

Los clientes seleccionados pasan al estado `HABILITADO` y reciben una autorización temporal de 10 minutos.

Esta operación requiere el rol `ADMINISTRADOR`.

### Parámetros

| Parámetro  | Ubicación | Tipo    | Obligatorio |
| ---------- | --------- | ------- | ----------- |
| `eventoId` | Path      | Long    | Sí          |
| `cantidad` | Body      | Integer | Sí          |

La cantidad debe encontrarse entre 1 y 100.

### Request

```http
POST /filas/1/habilitar
Authorization: Bearer <tokenAdmin>
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
    "fechaIngreso": "2026-11-19T13:05:08",
    "estado": "HABILITADO",
    "posicion": null,
    "fechaHabilitacion": "2026-11-19T13:10:00",
    "fechaVencimiento": "2026-11-19T13:20:00"
  },
  {
    "id": 2,
    "eventoId": 1,
    "clienteId": 20,
    "fechaIngreso": "2026-11-19T13:06:00",
    "estado": "HABILITADO",
    "posicion": null,
    "fechaHabilitacion": "2026-11-19T13:10:00",
    "fechaVencimiento": "2026-11-19T13:20:00"
  }
]
```

La respuesta contiene los clientes habilitados.

Si existen menos clientes esperando que la cantidad solicitada, se habilitan únicamente los disponibles.

No se permite iniciar otra tanda mientras exista una tanda activa para el mismo evento.

### Respuestas de error

| Código | Situación                                               |
| ------ | ------------------------------------------------------- |
| 400    | Identificador de evento o cantidad inválidos.           |
| 401    | No se proporcionó una autenticación válida.             |
| 403    | El usuario no tiene permisos de administrador.          |
| 409    | Existe una tanda activa o no quedan clientes esperando. |

## 6. Validar un turno

### GET /filas/{eventoId}/clientes/{clienteId}/validar

Comprueba si un cliente se encuentra habilitado y si su autorización continúa vigente.

La validación no consume ni finaliza el turno.

Esta operación se utiliza internamente desde Reservas mediante OpenFeign.

### Parámetros

| Parámetro   | Ubicación | Tipo | Obligatorio |
| ----------- | --------- | ---- | ----------- |
| `eventoId`  | Path      | Long | Sí          |
| `clienteId` | Path      | Long | Sí          |

### Request

```http
GET /filas/1/clientes/10/validar
Authorization: Bearer <tokenServicioReservas>
```

### Respuesta exitosa — HTTP 200

```json
{
  "eventoId": 1,
  "clienteId": 10,
  "autorizado": true,
  "estado": "HABILITADO",
  "fechaVencimiento": "2026-11-19T13:20:00"
}
```

El atributo `autorizado` indica si el cliente puede continuar con la creación de una reserva.

### Criterios de autorización

| Estado del cliente             | Resultado |
| ------------------------------ | --------- |
| `EN_ESPERA`                    | `false`   |
| `HABILITADO` y vigente         | `true`    |
| `HABILITADO` con plazo vencido | `false`   |
| `VENCIDO`                      | `false`   |
| `FINALIZADO`                   | `false`   |

Si un turno habilitado superó su fecha de vencimiento, se considera vencido y no se autoriza su utilización.

La validación no modifica el estado de un turno vigente ni consume su autorización.

### Respuestas de error

| Código | Situación                                                            |
| ------ | -------------------------------------------------------------------- |
| 400    | Identificadores inválidos.                                           |
| 401    | No se proporcionó una autenticación válida.                          |
| 403    | La identidad no está autorizada para realizar la validación interna. |
| 404    | El cliente no está registrado en la fila del evento.                 |

Un cliente no autorizado para reservar no genera necesariamente un error HTTP en este endpoint: la validación puede devolver `200` con `autorizado: false`.

Es el microservicio Reservas quien interpreta ese resultado y rechaza la creación de la reserva.

## 7. Finalizar un turno

### POST /filas/{eventoId}/clientes/{clienteId}/finalizar

Marca como `FINALIZADO` un turno utilizado durante la creación de una reserva temporal.

Esta operación es interna y se realiza desde Reservas.

Reservas registra la finalización pendiente después de crear correctamente la reserva y la procesa de manera asíncrona.

Si la comunicación falla, la operación puede reintentarse.

### Parámetros

| Parámetro   | Ubicación | Tipo | Obligatorio |
| ----------- | --------- | ---- | ----------- |
| `eventoId`  | Path      | Long | Sí          |
| `clienteId` | Path      | Long | Sí          |

### Request

```http
POST /filas/1/clientes/10/finalizar
Authorization: Bearer <tokenServicioReservas>
```

No requiere cuerpo JSON.

### Estados admitidos

La operación puede finalizar:

- Un turno `HABILITADO`.
- Un turno `VENCIDO`, cuando Reservas comunica tardíamente la finalización correspondiente a una reserva creada anteriormente.

No permite finalizar un turno en estado `EN_ESPERA`.

Si el turno ya está `FINALIZADO`, se devuelve su registro sin modificar nuevamente el estado.

### Respuesta exitosa — HTTP 200

```json
{
  "id": 1,
  "eventoId": 1,
  "clienteId": 10,
  "fechaIngreso": "2026-11-19T13:05:08",
  "estado": "FINALIZADO",
  "posicion": null,
  "fechaHabilitacion": "2026-11-19T13:10:00",
  "fechaVencimiento": "2026-11-19T13:20:00"
}
```

La operación es idempotente.

Una solicitud repetida sobre un turno finalizado conserva el resultado anterior.

Un turno finalizado no autoriza nuevas reservas.

### Respuestas de error

| Código | Situación                                                          |
| ------ | ------------------------------------------------------------------ |
| 400    | Identificadores inválidos.                                         |
| 401    | No se proporcionó una autenticación válida.                        |
| 403    | La identidad no está autorizada para finalizar turnos.             |
| 404    | El cliente no está registrado.                                     |
| 409    | El turno se encuentra en un estado incompatible, como `EN_ESPERA`. |

## 8. DTO utilizados

La API utiliza los siguientes objetos de transferencia de datos.

### 8.1. IngresarFilaDTO

Representa la solicitud de ingreso a una fila.

| Atributo    | Tipo | Descripción                                        |
| ----------- | ---- | -------------------------------------------------- |
| `clienteId` | Long | Identificador del cliente. Obligatorio y positivo. |

### 8.2. HabilitarTandaDTO

Representa una solicitud de habilitación.

| Atributo   | Tipo    | Descripción                                                       |
| ---------- | ------- | ----------------------------------------------------------------- |
| `cantidad` | Integer | Cantidad de clientes que se desea habilitar. Valor entre 1 y 100. |

### 8.3. ClienteEnFilaDTO

Representa la información de un cliente registrado en una fila.

| Atributo            | Tipo          | Descripción                          |
| ------------------- | ------------- | ------------------------------------ |
| `id`                | Long          | Identificador del registro.          |
| `eventoId`          | Long          | Identificador del evento.            |
| `clienteId`         | Long          | Identificador del cliente.           |
| `fechaIngreso`      | LocalDateTime | Fecha y hora de ingreso.             |
| `estado`            | EstadoFila    | Estado actual.                       |
| `posicion`          | Long          | Posición actual, cuando corresponde. |
| `fechaHabilitacion` | LocalDateTime | Fecha y hora de habilitación.        |
| `fechaVencimiento`  | LocalDateTime | Fecha y hora de vencimiento.         |

### 8.4. ValidacionTurnoDTO

Representa el resultado de la validación de una autorización.

| Atributo           | Tipo          | Descripción                          |
| ------------------ | ------------- | ------------------------------------ |
| `eventoId`         | Long          | Identificador del evento.            |
| `clienteId`        | Long          | Identificador del cliente.           |
| `autorizado`       | boolean       | Indica si el turno puede utilizarse. |
| `estado`           | EstadoFila    | Estado actual del turno.             |
| `fechaVencimiento` | LocalDateTime | Fecha límite de la autorización.     |

### 8.5. EventoDTO

Representa los datos utilizados para validar un evento mediante la integración con Eventos y Lugares.

| Atributo      | Tipo    | Descripción                      |
| ------------- | ------- | -------------------------------- |
| `id`          | Long    | Identificador del evento.        |
| `nombre`      | String  | Nombre del evento.               |
| `estado`      | String  | Estado actual del evento.        |
| `altaDemanda` | boolean | Indica si requiere fila virtual. |

## 9. Validaciones generales

La API aplica las siguientes reglas:

- Los identificadores de evento y cliente deben ser positivos.
- Los clientes solo pueden ingresar a filas de eventos publicados y de alta demanda.
- Una combinación de evento y cliente no puede generar registros duplicados.
- La habilitación debe respetar el orden de llegada.
- No pueden habilitarse tandas superpuestas para el mismo evento.
- Los turnos habilitados tienen una vigencia de 10 minutos.
- Los turnos vencidos o finalizados no autorizan nuevas reservas.
- La validación no consume el turno.
- La finalización repetida es idempotente.
- Una finalización asíncrona tardía puede cerrar un turno vencido.
- Los endpoints internos requieren credenciales autorizadas de servicio.
- Los clientes autenticados están sujetos a verificaciones de identidad y permisos.

## 10. Documentación OpenAPI

Los endpoints se encuentran documentados mediante anotaciones de Swagger/OpenAPI.

### Swagger UI — Acceso local directo

`http://localhost:8088/swagger-ui/index.html`

### Especificación OpenAPI — Acceso local directo

`http://localhost:8088/v3/api-docs`

### Especificación OpenAPI mediante Gateway

`http://localhost:8084/filas/v3/api-docs`

Swagger permite consultar los contratos HTTP, parámetros, estructuras de respuesta y códigos de estado de las operaciones.

Para ejecutar endpoints protegidos se requiere proporcionar un JWT con los permisos correspondientes.