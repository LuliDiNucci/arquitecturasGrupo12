# Contratos de API REST — Eventos y Lugares

## 1. Información general

El microservicio expone una API REST que utiliza JSON.

- Puerto local: `8081`
- Acceso mediante Gateway: `http://localhost:8084`
- Swagger UI: `http://localhost:8081/swagger-ui.html`
- OpenAPI: `http://localhost:8081/v3/api-docs`
- Fechas: formato ISO 8601, por ejemplo `2027-06-20T21:00:00`

Los endpoints administrativos requieren JWT con los roles correspondientes.

## 2. Lugares, sectores y asientos

| Método | Endpoint | Función |
|---|---|---|
| POST | `/lugares` | Registrar lugar. |
| GET | `/lugares` | Listar lugares. |
| GET | `/lugares/{id}` | Consultar lugar. |
| POST | `/lugares/{id}/sectores` | Crear sector. |
| GET | `/lugares/{id}/sectores` | Listar sectores. |
| GET | `/sectores/{id}` | Consultar sector. |
| POST | `/sectores/{id}/asientos` | Registrar asientos. |
| GET | `/sectores/{id}/asientos` | Listar asientos. |
| GET | `/asientos/{id}` | Consultar asiento. |

Las operaciones de creación requieren rol `ADMINISTRADOR`.

## 3. Organizadores y participantes

| Método | Endpoint | Función |
|---|---|---|
| POST | `/organizadores` | Crear organizador. |
| GET | `/organizadores` | Listar organizadores. |
| PATCH | `/organizadores/{id}/usuario` | Vincular organizador con usuario. |
| POST | `/participantes` | Registrar participante. |
| GET | `/participantes` | Listar participantes. |

La vinculación con Usuarios valida que la cuenta exista, esté activa y tenga rol `ORGANIZADOR`.

Ejemplo:

```http
PATCH /organizadores/1/usuario
Content-Type: application/json
Authorization: Bearer <JWT_ADMIN>
```

```json
{
  "usuarioId": 25
}
```

## 4. Eventos y estados

| Método | Endpoint | Función |
|---|---|---|
| POST | `/eventos` | Crear evento en borrador. |
| GET | `/eventos` | Listar todos los eventos (administrador). |
| GET | `/eventos/publicados` | Consultar catálogo público. |
| GET | `/eventos/buscar` | Buscar con filtros. |
| GET | `/eventos/{id}` | Consultar detalle. |
| PUT | `/eventos/{id}` | Modificar evento. |
| PUT | `/eventos/{id}/precios` | Configurar precios. |
| PATCH | `/eventos/{id}/publicar` | Publicar evento. |
| PATCH | `/eventos/{id}/suspender` | Suspender evento. |
| PATCH | `/eventos/{id}/cancelar` | Cancelar evento. |
| PATCH | `/eventos/{id}/agotar` | Marcar como agotado. |
| PATCH | `/eventos/{id}/reabrir` | Reabrir ventas. |
| PATCH | `/eventos/{id}/finalizar` | Finalizar evento. |

Las operaciones de modificación, precios, publicación y gestión de ventas requieren ser administrador u organizador propietario. Suspender y cancelar son operaciones exclusivas del administrador.

Ejemplo de creación:

```http
POST /eventos
Content-Type: application/json
Authorization: Bearer <JWT_ORGANIZADOR>
```

```json
{
  "nombre": "Recital de prueba",
  "descripcion": "Recital en Tandil",
  "tipo": "RECITAL",
  "organizadorId": 1,
  "lugarId": 1,
  "fechaHora": "2027-06-20T21:00:00",
  "participantesIds": [1],
  "altaDemanda": false
}
```

La creación exitosa devuelve `201 Created` y el evento en estado `BORRADOR`.

## 5. Consultas para integración

| Método | Endpoint | Función |
|---|---|---|
| GET | `/eventos/{id}/sectores` | Consultar sectores, asientos y precios. |
| GET | `/eventos/{id}/habilitado` | Verificar si admite reservas. |
| GET | `/eventos/{eventoId}/sectores/{sectorId}/precio` | Consultar precio de un sector. |
| GET | `/eventos/{id}/notificaciones-cancelacion` | Consultar el procesamiento de cancelaciones (administrador). |

El detalle de eventos no públicos está restringido a administradores, organizadores propietarios y servicios internos autorizados.

## 6. Búsquedas

`GET /eventos/buscar` acepta los siguientes parámetros opcionales:

| Parámetro | Función |
|---|---|
| `tipo` | Tipo de espectáculo. |
| `organizadorId` | Filtrar por organizador. |
| `participanteId` | Filtrar por participante. |
| `ciudad` | Filtrar por ciudad. |
| `desde`, `hasta` | Rango de fechas. |
| `q` | Buscar texto. |
| `ordenar` | Fecha, precio o relevancia. |
| `incluirNoPublicados` | Incluir otros estados, solo para administradores. |

Ejemplo:

```http
GET /eventos/buscar?tipo=RECITAL&ciudad=Tandil&ordenar=FECHA_ASC
```

El campo `precioDesde` representa el precio mínimo configurado para el evento, no la disponibilidad de entradas.

## 7. Reportes para organizadores

| Método | Endpoint | Información |
|---|---|---|
| GET | `/eventos/{eventoId}/reporte` | Resumen general de ventas, devoluciones y ocupación. |
| GET | `/eventos/{eventoId}/reporte/sectores` | Resultados por sector. |
| GET | `/eventos/{eventoId}/reporte/recaudacion` | Importes pagados, reintegrados y recaudación neta. |
| GET | `/eventos/{eventoId}/reporte/ocupacion` | Capacidad, lugares ocupados y porcentaje de ocupación. |

Los reportes integran información de Compras y Reservas y requieren permisos sobre el evento consultado.

## 8. Respuestas y errores

| HTTP | Significado |
|---|---|
| `200 OK` | Consulta o modificación exitosa. |
| `201 Created` | Recurso creado. |
| `400 Bad Request` | Datos inválidos. |
| `401 Unauthorized` | Autenticación ausente o inválida. |
| `403 Forbidden` | Permisos insuficientes. |
| `404 Not Found` | Recurso inexistente. |
| `409 Conflict` | Regla de negocio incumplida. |
| `503 Service Unavailable` | Dependencia necesaria no disponible. |

Los contratos detallados de los DTO y sus esquemas se encuentran disponibles mediante la especificación OpenAPI generada por Springdoc.