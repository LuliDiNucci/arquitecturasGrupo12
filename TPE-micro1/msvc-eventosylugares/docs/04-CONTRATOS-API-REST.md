# Diseño de API REST — Eventos y Lugares

**Base local:** `http://localhost:8081` — Content-Type: `application/json`. Fecha/hora sin zona (`LocalDateTime`) en horario local de Argentina. Entrega 1: sin JWT. **Swagger:** `/swagger-ui.html`; especificación JSON: `/v3/api-docs`.

## Endpoints (26)

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

## Contratos de cancelacion verificados

Reservas implementa `POST /reservas/evento/{id}/cancelar` (puerto 8080).
Entradas y Compras tienen clientes Feign preparados para `POST /entradas/evento/{id}/invalidar` y `POST /compras/evento/{id}/cancelar`. Estas rutas son propuestas y aun no existen en los receptores. Con integraciones habilitadas se envia HTTP; ante errores, los avisos quedan pendientes y se reintentan.
`X-Operacion-Id` se envia a Reservas, pero el receptor no implementa deduplicacion por esa cabecera.

## Listado publico

`GET /eventos/publicados` devuelve PUBLICADO y AGOTADO con el mismo criterio de `/eventos/buscar`. `GET /eventos` conserva todos los estados para uso administrativo. El detalle y las operaciones de administracion no cambian.