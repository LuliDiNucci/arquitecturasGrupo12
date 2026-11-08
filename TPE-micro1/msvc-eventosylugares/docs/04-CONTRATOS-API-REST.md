# Diseño de API REST — Eventos y Lugares

**Base local:** `http://localhost:8081` — Content-Type: `application/json`. Fecha/hora sin zona (`LocalDateTime`) en horario local de Argentina. Entrega 1: sin JWT. **Swagger:** `/swagger-ui.html`; especificación JSON: `/v3/api-docs`.

## Endpoints (27)

| Método | Ruta | Parámetros / DTO | Éxito | Errores posibles |
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
| GET | `/eventos/buscar` | filtros query | 200 | 400 |
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

La tabla muestra códigos contemplados por la lógica; Spring puede devolver 500 ante fallos no controlados de infraestructura.

## Búsqueda `/eventos/buscar`

Parámetros opcionales: `tipo` (`RECITAL`, `FESTIVAL`, `TEATRO`, `DEPORTE`, `CONFERENCIA`, `OTRO`), `organizadorId` Long, `participanteId` Long, `ciudad` String, `desde`/`hasta` (`YYYY-MM-DDTHH:mm:ss`), `q` String, `ordenar` (`FECHA_ASC`, `FECHA_DESC`, `PRECIO_ASC`, `PRECIO_DESC`, `RELEVANCIA`), `incluirNoPublicados` boolean, por defecto `false`.

Por defecto solo se listan PUBLICADO y AGOTADO. `GET /eventos` lista todos los eventos con propósito operativo: **no equivale a búsqueda pública**. La autenticación/roles se añade en Entrega 2.

### Ejemplo de búsqueda

`GET /eventos/buscar?tipo=RECITAL&ciudad=Tandil&ordenar=PRECIO_ASC`

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

## Ejemplos de requests

**1. Lugar** — `POST /lugares`

```json
{"nombre":"Estadio Municipal", "direccion":"Av. Central 150", "ciudad":"Tandil", "latitud":-37.321, "longitud":-59.137}
```

**Respuesta 201** (el ID asignado es ilustrativo):

```json
{"id":1, "nombre":"Estadio Municipal", "direccion":"Av. Central 150", "ciudad":"Tandil", "latitud":-37.321, "longitud":-59.137}
```

**2. Sector numerado** — `POST /lugares/1/sectores`

```json
{"nombre":"Platea", "numerado":true, "capacidad":2}
```

**Sector no numerado** — `POST /lugares/1/sectores`

```json
{"nombre":"Campo", "numerado":false, "capacidad":500}
```

**3. Asientos** — `POST /sectores/1/asientos`

```json
{"asientos":[{"fila":"A", "numero":1}, {"fila":"A", "numero":2}]}
```

**Respuesta 201:**

```json
[{"id":1, "fila":"A", "numero":1}, {"id":2, "fila":"A", "numero":2}]
```

**4. Organizador** — `POST /organizadores`

```json
{"nombre":"Producciones Sierra", "email":"contacto@ejemplo.com"}
```

**5. Participante** — `POST /participantes`

```json
{"nombre":"Banda Ejemplo", "tipo":"ARTISTA"}
```

**6. Evento** — `POST /eventos`

```json
{
  "nombre":"Recital de prueba", "descripcion":"Presentacion en vivo",
  "tipo":"RECITAL", "organizadorId":1, "lugarId":1,
  "fechaHora":"2027-12-20T21:00:00", "participantesIds":[1], "altaDemanda":false
}
```

**Respuesta 201 (fragmento representativo; valores de IDs de ejemplo):**

```json
{
  "id":1, "nombre":"Recital de prueba", "descripcion":"Presentacion en vivo",
  "tipo":"RECITAL", "estado":"BORRADOR",
  "organizador":{"id":1,"nombre":"Producciones Sierra","email":"contacto@ejemplo.com"},
  "participantes":[{"id":1,"nombre":"Banda Ejemplo","tipo":"ARTISTA"}],
  "fechaHora":"2027-12-20T21:00:00",
  "lugar":{"id":1,"nombre":"Estadio Municipal","direccion":"Av. Central 150","ciudad":"Tandil","latitud":-37.321,"longitud":-59.137},
  "altaDemanda":false,
  "sectores":[{"sectorId":1,"nombre":"Platea","numerado":true,"capacidad":2,"precio":null,"asientos":[{"id":1,"fila":"A","numero":1},{"id":2,"fila":"A","numero":2}]}],
  "version":0
}
```

`version` es gestionada por Hibernate y puede variar según cambios o configuración.

**7. Precios por evento** — `PUT /eventos/1/precios`

```json
{"precios":[{"sectorId":1,"precio":35000.00},{"sectorId":2,"precio":20000.00}]}
```

La lista debe incluir **exactamente los IDs de los sectores del lugar del evento**; los IDs aquí son ilustrativos.

**8. Publicar, suspender y cancelar** — `PATCH /eventos/1/publicar`, `PATCH /eventos/1/suspender`, `PATCH /eventos/1/cancelar` sin body. La respuesta es `EventoResponseDTO` completo, con el campo `estado` actualizado.

**9. Ofertas para Reservas** — `GET /eventos/1/sectores`

```json
[
  {"sectorId":1,"nombre":"Platea","numerado":true,"capacidad":2,"precio":35000.00,
    "asientos":[{"id":1,"fila":"A","numero":1},{"id":2,"fila":"A","numero":2}]},
  {"sectorId":2,"nombre":"Campo","numerado":false,"capacidad":500,"precio":20000.00,"asientos":[]}
]
```

**10. Habilitación** — `GET /eventos/1/habilitado`

```json
{"eventoId":1, "estado":"PUBLICADO", "habilitadoParaReservar":true, "fechaHora":"2027-12-20T21:00:00"}
```

**11. Estado de los avisos** — `GET /eventos/1/notificaciones-cancelacion`

```json
{"eventoId":1,"reservasNotificadas":false,"entradasNotificadas":false,"comprasNotificadas":false,"intentos":0,"ultimoError":null}
```

## Ejemplo de error JSON

**Publicación inválida: 409 Conflict**

```json
{
  "timestamp":"2026-11-04T15:00:00",
  "status":409,
  "error":"Conflict",
  "mensaje":"Faltan precios de uno o mas sectores del evento",
  "path":"/eventos/1/publicar",
  "detalles":[]
}
```

`timestamp` es ilustrativo, no un valor fijo. En validación de campos (400), `detalles` indica qué campos fallaron.


## Integraciones con otros microservicios

Esta sección distingue los endpoints propios de Eventos de los endpoints externos que consume mediante clientes Feign.

### Endpoints externos consumidos por Eventos

| Microservicio | Método y ruta | Uso | Estado verificado |
| Reservas | `POST /reservas/evento/{id}/cancelar` | Notificar la cancelación del evento | Implementado en el receptor |
| Reservas | `POST /reservas/evento/{id}/habilitar` | Habilitar reservas para un evento | Implementado en el receptor |
| Entradas | `POST /entradas/evento/{id}/invalidar` | Invalidar entradas de un evento cancelado | Implementado en el receptor |
| Compras | `POST /compras/evento/{id}/cancelar` | Gestionar los reintegros por cancelación | Implementado en el receptor |

Los clientes Feign de Eventos utilizan las siguientes direcciones configurables:

- `RESERVAS_URL`: por defecto `http://localhost:8080`.
- `ENTRADAS_URL`: por defecto `http://localhost:8083`.
- `COMPRAS_URL`: por defecto `http://localhost:8082`.

Los puertos corresponden a la configuración predeterminada actual de cada microservicio y pueden modificarse mediante variables de entorno.

### Notificación de cancelaciones

Cuando se ejecuta `PATCH /eventos/{id}/cancelar`:

1. Eventos cambia el estado del evento a `CANCELADO`.
2. Guarda una notificación pendiente en su propia base de datos.
3. Devuelve la respuesta al administrador sin esperar a que los otros microservicios terminen sus operaciones.
4. El componente `NotificadorCancelaciones` procesa los avisos mediante un scheduler con intervalo de 30 segundos.
5. Reservas se notifica primero. Entradas se notifica de forma independiente.
6. Compras se notifica cuando Reservas y Entradas fueron notificadas correctamente, siempre que esas integraciones estén habilitadas.

Si una notificación falla, se registra el error y permanece pendiente para un nuevo intento. El estado de cada aviso se puede consultar mediante:

`GET /eventos/{id}/notificaciones-cancelacion`

La configuración predeterminada de `application.yml` habilita las integraciones:

- `INTEGRACIONES_HABILITADAS=true`
- `ENTRADAS_HABILITADAS=true`
- `COMPRAS_HABILITADAS=true`

Estas opciones pueden modificarse mediante variables de entorno.

### Encabezado `X-Operacion-Id`

Eventos envía un identificador estable para la cancelación mediante el encabezado `X-Operacion-Id`.

Su tratamiento depende del microservicio receptor:

- **Reservas:** recibe el encabezado, pero no lo utiliza para deduplicar operaciones.
- **Entradas:** el cliente Feign lo envía, pero el endpoint receptor no lo procesa.
- **Compras:** exige y valida el encabezado. La prevención de reintegros duplicados se implementa mediante el registro de reintegros por ítem y la reutilización de los que ya fueron aprobados.

### Comportamiento de Compras ante una cancelación

Compras verifica que el evento esté cancelado y solicita la invalidación de sus entradas antes de procesar los reintegros.

Para cada compra con pago aprobado:

- Procesa los reintegros por ítem utilizando el precio histórico de cada uno.
- No vuelve a procesar un reintegro que ya esté aprobado.
- Actualiza la compra a `REINTEGRADA` cuando todos los ítems fueron reintegrados.
- La deja en `REINTEGRO_PARCIAL` cuando solamente algunos ítems fueron reintegrados.
- Si no se aprobó ningún reintegro, no asigna un nuevo estado a la compra.

Si alguna compra no puede completar su reintegro, la operación informa el error para permitir un nuevo intento.

**Importante:** las notificaciones y los reintegros no constituyen una única transacción distribuida entre las bases de datos. Un fallo en un microservicio no revierte automáticamente los cambios confirmados en los demás.