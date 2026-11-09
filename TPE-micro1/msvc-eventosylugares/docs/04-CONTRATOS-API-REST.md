# 04 — Contratos de API REST de Eventos y Lugares

## 1. Descripción general

El microservicio `msvc-eventosylugares` expone una API REST para administrar lugares, sectores, asientos, organizadores, participantes y eventos.

También permite configurar precios, gestionar estados de eventos, realizar búsquedas y coordinar notificaciones de cancelación con otros microservicios.

**Configuración local:**

- **URL base:** `http://localhost:8081`
- **Formato de intercambio:** JSON (`application/json`)
- **Formato de fecha y hora:** `LocalDateTime`, utilizando el horario local de Argentina.
- **Swagger UI:** `http://localhost:8081/swagger-ui.html`
- **Especificación OpenAPI:** `http://localhost:8081/v3/api-docs`

La API utiliza los métodos HTTP correspondientes a cada operación y devuelve códigos de respuesta según el resultado de las validaciones y reglas de negocio.

## 2. Endpoints REST

| Método | Ruta | Parámetros / DTO | Éxito | Errores |
|---|---|---|---|---|
| POST | `/lugares` | `LugarRequestDTO` | 201 | 400 |
| GET | `/lugares` | — | 200 | — |
| GET | `/lugares/{id}` | id: Long | 200 | 404 |
| POST | `/lugares/{id}/sectores` | id lugar + `SectorRequestDTO` | 201 | 400, 404, 409 |
| GET | `/lugares/{id}/sectores` | id lugar | 200 | 404 |
| POST | `/sectores/{id}/asientos` | id sector + `AsientosRequestDTO` | 201 | 400, 404, 409 |
| GET | `/sectores/{id}/asientos` | id sector | 200 | 404 |
| POST | `/organizadores` | `OrganizadorRequestDTO` | 201 | 400 |
| GET | `/organizadores` | — | 200 | — |
| POST | `/participantes` | `ParticipanteRequestDTO` | 201 | 400 |
| GET | `/participantes` | — | 200 | — |
| POST | `/eventos` | `EventoRequestDTO` | 201 | 400, 404 |
| GET | `/eventos` | — | 200 | — |
| GET | `/eventos/publicados` | — | 200 | — |
| GET | `/eventos/buscar` | Filtros de consulta | 200 | 400 |
| GET | `/eventos/{id}` | id evento | 200 | 404 |
| PUT | `/eventos/{id}` | `EventoRequestDTO` | 200 | 400, 404, 409 |
| PUT | `/eventos/{id}/precios` | `PreciosEventoRequestDTO` | 200 | 400, 404, 409 |
| PATCH | `/eventos/{id}/publicar` | id evento | 200 | 404, 409 |
| PATCH | `/eventos/{id}/suspender` | id evento | 200 | 404, 409 |
| PATCH | `/eventos/{id}/cancelar` | id evento | 200 | 404, 409 |
| PATCH | `/eventos/{id}/agotar` | id evento | 200 | 404, 409 |
| PATCH | `/eventos/{id}/reabrir` | id evento | 200 | 404, 409 |
| PATCH | `/eventos/{id}/finalizar` | id evento | 200 | 404, 409 |
| GET | `/eventos/{id}/sectores` | id evento | 200 | 404 |
| GET | `/eventos/{id}/habilitado` | id evento | 200 | 404 |
| GET | `/eventos/{id}/notificaciones-cancelacion` | id evento | 200 | 404 |

Los códigos indicados corresponden a las respuestas de negocio contempladas por los endpoints. Los errores de infraestructura no controlados pueden producir respuestas `500 Internal Server Error`.

## 3. Búsqueda de eventos

### 3.1. Endpoint

`GET /eventos/buscar`

Permite realizar búsquedas utilizando múltiples criterios combinables.

### 3.2. Parámetros de consulta

| Parámetro | Tipo | Descripción |
|---|---|---|
| `tipo` | String | Tipo de evento. |
| `organizadorId` | Long | Identificador del organizador. |
| `participanteId` | Long | Identificador de un participante. |
| `ciudad` | String | Ciudad del lugar donde se realiza el evento. |
| `desde` | LocalDateTime | Fecha y hora inicial del rango. |
| `hasta` | LocalDateTime | Fecha y hora final del rango. |
| `q` | String | Texto utilizado en la búsqueda. |
| `ordenar` | String | Criterio de ordenamiento. |
| `incluirNoPublicados` | Boolean | Permite incluir eventos de otros estados. Valor predeterminado: `false`. |

**Tipos de evento:**

- `RECITAL`
- `FESTIVAL`
- `TEATRO`
- `DEPORTE`
- `CONFERENCIA`
- `OTRO`

**Criterios de ordenamiento:**

- `FECHA_ASC`
- `FECHA_DESC`
- `PRECIO_ASC`
- `PRECIO_DESC`
- `RELEVANCIA`

Las fechas deben enviarse con el formato:

`YYYY-MM-DDTHH:mm:ss`

Por defecto, la búsqueda devuelve eventos en estado `PUBLICADO` o `AGOTADO`.

El endpoint `GET /eventos` permite obtener el listado general de eventos con fines operativos, independientemente de los filtros utilizados en la búsqueda pública.

### 3.3. Ejemplo de búsqueda

**Request:**

```http
GET /eventos/buscar?tipo=RECITAL&ciudad=Tandil&ordenar=PRECIO_ASC
```

**Response — `200 OK`:**

```json
[
  {
    "id": 1,
    "nombre": "Recital de prueba",
    "tipo": "RECITAL",
    "estado": "PUBLICADO",
    "fechaHora": "2027-12-20T21:00:00",
    "ciudad": "Tandil",
    "lugar": "Estadio Municipal",
    "organizador": "Producciones Sierra",
    "precioDesde": 35000.00,
    "altaDemanda": false
  }
]
```

El campo `precioDesde` representa el menor precio configurado entre los sectores del evento.

No representa la disponibilidad real de entradas, que es administrada por el microservicio Reservas.

Si el parámetro `desde` es posterior a `hasta`, la API responde `400 Bad Request`.

## 4. Ejemplos de solicitudes y respuestas

### 4.1. Crear un lugar

**Endpoint:** `POST /lugares`

**Request:**

```json
{
  "nombre": "Estadio Municipal",
  "direccion": "Av. Central 150",
  "ciudad": "Tandil",
  "latitud": -37.321,
  "longitud": -59.137
}
```

**Response — `201 Created`:**

```json
{
  "id": 1,
  "nombre": "Estadio Municipal",
  "direccion": "Av. Central 150",
  "ciudad": "Tandil",
  "latitud": -37.321,
  "longitud": -59.137
}
```

El identificador es asignado por el sistema.

### 4.2. Crear sectores

**Endpoint:** `POST /lugares/{id}/sectores`

**Sector numerado:**

```json
{
  "nombre": "Platea",
  "numerado": true,
  "capacidad": 2
}
```

**Sector no numerado:**

```json
{
  "nombre": "Campo",
  "numerado": false,
  "capacidad": 500
}
```

Cada sector pertenece a un lugar y posee una capacidad máxima.

Los sectores numerados permiten identificar asientos individuales, mientras que los no numerados utilizan su capacidad general.

### 4.3. Registrar asientos

**Endpoint:** `POST /sectores/{id}/asientos`

**Request:**

```json
{
  "asientos": [
    {
      "fila": "A",
      "numero": 1
    },
    {
      "fila": "A",
      "numero": 2
    }
  ]
}
```

**Response — `201 Created`:**

```json
[
  {
    "id": 1,
    "fila": "A",
    "numero": 1
  },
  {
    "id": 2,
    "fila": "A",
    "numero": 2
  }
]
```

No se permite registrar combinaciones duplicadas de fila y número dentro de un mismo sector ni superar su capacidad máxima.

### 4.4. Registrar un organizador

**Endpoint:** `POST /organizadores`

**Request:**

```json
{
  "nombre": "Producciones Sierra",
  "email": "contacto@ejemplo.com"
}
```

### 4.5. Registrar un participante

**Endpoint:** `POST /participantes`

**Request:**

```json
{
  "nombre": "Banda Ejemplo",
  "tipo": "ARTISTA"
}
```

### 4.6. Crear un evento

**Endpoint:** `POST /eventos`

**Request:**

```json
{
  "nombre": "Recital de prueba",
  "descripcion": "Presentacion en vivo",
  "tipo": "RECITAL",
  "organizadorId": 1,
  "lugarId": 1,
  "fechaHora": "2027-12-20T21:00:00",
  "participantesIds": [1],
  "altaDemanda": false
}
```

**Response — `201 Created`:**

```json
{
  "id": 1,
  "nombre": "Recital de prueba",
  "descripcion": "Presentacion en vivo",
  "tipo": "RECITAL",
  "estado": "BORRADOR",
  "organizador": {
    "id": 1,
    "nombre": "Producciones Sierra",
    "email": "contacto@ejemplo.com"
  },
  "participantes": [
    {
      "id": 1,
      "nombre": "Banda Ejemplo",
      "tipo": "ARTISTA"
    }
  ],
  "fechaHora": "2027-12-20T21:00:00",
  "lugar": {
    "id": 1,
    "nombre": "Estadio Municipal",
    "direccion": "Av. Central 150",
    "ciudad": "Tandil",
    "latitud": -37.321,
    "longitud": -59.137
  },
  "altaDemanda": false,
  "sectores": [
    {
      "sectorId": 1,
      "nombre": "Platea",
      "numerado": true,
      "capacidad": 2,
      "precio": null,
      "asientos": [
        {
          "id": 1,
          "fila": "A",
          "numero": 1
        },
        {
          "id": 2,
          "fila": "A",
          "numero": 2
        }
      ]
    }
  ],
  "version": 0
}
```

El evento se registra inicialmente en estado `BORRADOR`.

Los precios de los sectores se configuran mediante una operación independiente.

El campo `version` es administrado por Hibernate para el control de versiones de la entidad.

### 4.7. Configurar precios por evento

**Endpoint:** `PUT /eventos/{id}/precios`

**Request:**

```json
{
  "precios": [
    {
      "sectorId": 1,
      "precio": 35000.00
    },
    {
      "sectorId": 2,
      "precio": 20000.00
    }
  ]
}
```

La lista debe incluir exactamente un precio por cada sector del lugar asociado al evento.

Los identificadores mostrados son ilustrativos y corresponden a un evento cuyo lugar tiene dos sectores registrados.

La configuración de precios se realiza mientras el evento se encuentra en estado `BORRADOR`.

### 4.8. Publicar, suspender y cancelar un evento

**Endpoints:**

```http
PATCH /eventos/1/publicar
PATCH /eventos/1/suspender
PATCH /eventos/1/cancelar
```

Estas operaciones no requieren cuerpo JSON.

La respuesta devuelve un `EventoResponseDTO` con la información del evento y su estado actualizado.

Las transiciones se encuentran sujetas a las reglas de negocio correspondientes.

### 4.9. Consultar sectores y precios de un evento

**Endpoint:** `GET /eventos/{id}/sectores`

**Response — `200 OK`:**

```json
[
  {
    "sectorId": 1,
    "nombre": "Platea",
    "numerado": true,
    "capacidad": 2,
    "precio": 35000.00,
    "asientos": [
      {
        "id": 1,
        "fila": "A",
        "numero": 1
      },
      {
        "id": 2,
        "fila": "A",
        "numero": 2
      }
    ]
  },
  {
    "sectorId": 2,
    "nombre": "Campo",
    "numerado": false,
    "capacidad": 500,
    "precio": 20000.00,
    "asientos": []
  }
]
```

Este endpoint proporciona la configuración de los sectores y sus precios.

La información no incluye la ocupación ni la cantidad de lugares disponibles en tiempo real.

### 4.10. Consultar habilitación para reservar

**Endpoint:** `GET /eventos/{id}/habilitado`

**Response — `200 OK`:**

```json
{
  "eventoId": 1,
  "estado": "PUBLICADO",
  "habilitadoParaReservar": true,
  "fechaHora": "2027-12-20T21:00:00"
}
```

El campo `habilitadoParaReservar` indica si el evento reúne las condiciones para admitir nuevas reservas.

Para estar habilitado, el evento debe encontrarse en estado `PUBLICADO` y tener una fecha futura.

### 4.11. Consultar notificaciones de cancelación

**Endpoint:** `GET /eventos/{id}/notificaciones-cancelacion`

**Response — `200 OK`:**

```json
{
  "eventoId": 1,
  "reservasNotificadas": false,
  "entradasNotificadas": false,
  "comprasNotificadas": false,
  "intentos": 0,
  "ultimoError": null
}
```

La respuesta permite consultar el estado del procesamiento de las notificaciones asociadas a la cancelación de un evento.

Los indicadores muestran qué microservicios procesaron la cancelación y si se registraron errores durante los intentos de comunicación.

## 5. Manejo de errores

La API devuelve respuestas JSON para informar errores de validación y reglas de negocio.

### Ejemplo: publicación inválida

**Response — `409 Conflict`:**

```json
{
  "timestamp": "2026-11-04T15:00:00",
  "status": 409,
  "error": "Conflict",
  "mensaje": "Faltan precios de uno o mas sectores del evento",
  "path": "/eventos/1/publicar",
  "detalles": []
}
```

Los principales códigos HTTP utilizados son:

| Código | Descripción |
|---|---|
| `200 OK` | Operación de consulta o actualización realizada correctamente. |
| `201 Created` | Recurso creado correctamente. |
| `400 Bad Request` | Solicitud inválida, parámetros incorrectos o errores de validación. |
| `404 Not Found` | Recurso solicitado inexistente. |
| `409 Conflict` | Operación incompatible con las reglas de negocio o el estado del recurso. |
| `500 Internal Server Error` | Error interno no controlado. |

El campo `timestamp` corresponde al momento de generación de la respuesta.

Cuando existen errores de validación de campos, el atributo `detalles` permite informar las condiciones que no se cumplieron.

## 6. Integraciones con otros microservicios

Eventos y Lugares utiliza clientes Feign para comunicarse con Reservas, Entradas y Compras.

Estas operaciones forman parte de la coordinación del circuito de cancelación de eventos.

### 6.1. Endpoints externos

| Microservicio | Método | Ruta | Función |
|---|---|---|---|
| Reservas | POST | `/reservas/evento/{id}/cancelar` | Cancelar las reservas asociadas al evento. |
| Reservas | POST | `/reservas/evento/{id}/habilitar` | Habilitar reservas para un evento. |
| Entradas | POST | `/entradas/evento/{id}/invalidar` | Invalidar las entradas emitidas de un evento cancelado. |
| Compras | POST | `/compras/evento/{id}/cancelar` | Procesar los reintegros correspondientes a un evento cancelado. |

### 6.2. Configuración de clientes Feign

Las direcciones de los servicios externos se configuran mediante variables de entorno.

| Variable | Valor predeterminado |
|---|---|
| `RESERVAS_URL` | `http://localhost:8080` |
| `ENTRADAS_URL` | `http://localhost:8083` |
| `COMPRAS_URL` | `http://localhost:8082` |

Los valores corresponden a la configuración local y pueden modificarse según el entorno de ejecución.

## 7. Notificaciones de cancelación

Cuando se ejecuta:

`PATCH /eventos/{id}/cancelar`

se realiza el siguiente proceso:

1. Eventos cambia el estado del evento a `CANCELADO`.
2. Registra una notificación de cancelación en su propia base de datos.
3. Devuelve la respuesta al administrador sin esperar a que finalicen las comunicaciones externas.
4. El componente `NotificadorCancelaciones` procesa las notificaciones mediante un scheduler con un intervalo de 30 segundos.
5. Reservas recibe la notificación de cancelación. Entradas se notifica de forma independiente.
6. Compras recibe la notificación cuando Reservas y Entradas completaron sus operaciones, siempre que las integraciones estén habilitadas.

Si una comunicación falla, se registra el error y se conserva la notificación para su posterior reintento.

El estado de procesamiento puede consultarse mediante:

`GET /eventos/{id}/notificaciones-cancelacion`

### 7.1. Configuración de las integraciones

La configuración utiliza las siguientes variables:

- `INTEGRACIONES_HABILITADAS=true`
- `ENTRADAS_HABILITADAS=true`
- `COMPRAS_HABILITADAS=true`

Estas propiedades permiten controlar las comunicaciones entre microservicios según el entorno de ejecución.

### 7.2. Encabezado X-Operacion-Id

Las notificaciones de cancelación incluyen el encabezado `X-Operacion-Id`, que identifica la operación correspondiente.

Su tratamiento depende del microservicio receptor:

- **Reservas:** recibe el encabezado y controla las operaciones mediante las validaciones de estado de sus reservas.
- **Entradas:** recibe la solicitud de invalidación y evita modificar nuevamente entradas que ya se encuentran invalidadas.
- **Compras:** exige y valida el encabezado. La prevención de reintegros duplicados se realiza verificando los reintegros registrados para cada ítem de compra.

Este mecanismo permite repetir las operaciones de cancelación sin duplicar los efectos que ya fueron procesados correctamente.

## 8. Procesamiento de reintegros por cancelación

El microservicio Compras verifica que el evento se encuentre cancelado y solicita la invalidación de sus entradas antes de procesar los reintegros.

Para cada compra con un pago aprobado:

1. Identifica los ítems correspondientes.
2. Consulta los reintegros registrados.
3. Utiliza el precio histórico de cada ítem para determinar el importe.
4. Evita procesar nuevamente los reintegros ya aprobados.
5. Solicita los reintegros correspondientes a la pasarela simulada.
6. Actualiza los estados de los reintegros y de la compra.

Los estados principales son:

- `REINTEGRADA`: todos los ítems de la compra fueron reintegrados.
- `REINTEGRO_PARCIAL`: solamente algunos ítems fueron reintegrados.

Cuando ningún reintegro fue aprobado, el procesamiento no asigna un nuevo estado de reintegro a la compra.

Si alguna operación no puede completarse, se registra el error y la notificación de cancelación permite reintentar el procesamiento.

Las operaciones utilizan transacciones locales y mecanismos de idempotencia para evitar efectos duplicados durante las comunicaciones entre microservicios.