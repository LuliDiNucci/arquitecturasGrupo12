# Contratos REST — Recomendaciones

## Acceso

- Puerto del microservicio: `8086`.
- Acceso habitual desde el frontend: API Gateway, puerto `8084`.
- Las operaciones de clientes requieren `Authorization: Bearer <JWT>`.

Los usuarios con rol `CLIENTE` solo pueden acceder a los datos asociados a su identificador. El rol `ADMINISTRADOR` permite gestionar preferencias y consultar recomendaciones de otros clientes.

## Endpoints

| Método | Ruta | Descripción | Respuesta |
|---|---|---|---|
| `POST` | `/api/preferencias/{clienteId}` | Registrar preferencias | `201` |
| `GET` | `/api/preferencias/{clienteId}` | Consultar preferencias | `200` |
| `PUT` | `/api/preferencias/{clienteId}` | Modificar preferencias | `200` |
| `GET` | `/api/recomendaciones/{clienteId}` | Obtener recomendaciones | `200` |

### Ejemplo de preferencias

Solicitud para registrar o modificar:

```json
{
  "tiposEventos": ["RECITAL"],
  "artistasFavoritos": ["Banda Ejemplo"],
  "ciudad": "Tandil"
}
```

La respuesta incorpora el campo `clienteId`.

### Ejemplo de recomendación

```json
{
  "id": 1,
  "nombre": "Recital de prueba",
  "tipo": "RECITAL",
  "artista": "Banda Ejemplo",
  "ciudad": "Tandil",
  "fechaHora": "2027-02-20T21:00:00",
  "precio": 20000.0
}
```

El endpoint de recomendaciones devuelve un arreglo de objetos con esa estructura. Cuando no hay coincidencias, responde `200` con `[]`.

## Endpoint interno

| Método | Ruta | Función | Autorización |
|---|---|---|---|
| `GET` | `/internal/clientes-interesados?nombreArtista={nombre}&ciudadEvento={ciudad}` | Devuelve IDs de clientes con coincidencia de artista y ciudad | Token de servicio `SERVICE_eventos` |

Ejemplo de respuesta:

```json
[3, 8, 15]
```

## Integraciones salientes

- Eventos y Lugares: `GET /eventos/publicados` y `GET /eventos/{id}`.
- Compras y Pagos: `GET /compras/cliente/{clienteId}/eventos-recomendados`.

La consulta a Compras reenvía el JWT del usuario autenticado.

## Respuestas de error

| Código | Significado |
|---|---|
| `400` | Datos de preferencias inválidos |
| `401` | Token ausente, inválido o vencido |
| `403` | Permisos insuficientes o acceso a otro cliente |
| `404` | Preferencias inexistentes |
| `409` | Intento de registrar preferencias existentes |
| `503` | Catálogo de Eventos no disponible |

## Documentación interactiva

Con el servicio ejecutándose:

- Swagger UI: `http://localhost:8086/swagger-ui.html`
- OpenAPI: `http://localhost:8086/v3/api-docs`

La especificación exportada se conserva en `docs/openapi.json`.