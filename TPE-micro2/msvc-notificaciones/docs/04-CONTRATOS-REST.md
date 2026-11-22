# Contratos REST — Notificaciones

## Acceso

El microservicio utiliza el puerto `8087`. Los endpoints de registro requieren autenticación mediante tokens JWT de servicio.

## Endpoints

| Método | Ruta | Función |
|---|---|---|
| `POST` | `/notificaciones/compras-confirmadas` | Registrar aviso de compra |
| `POST` | `/notificaciones/eventos-cancelados` | Registrar aviso de cancelación |
| `POST` | `/notificaciones/eventos-modificados` | Registrar aviso de modificación |
| `POST` | `/notificaciones/artistas-favoritos` | Registrar aviso de artista favorito |
| `GET` | `/notificaciones/{id}` | Consultar el estado del aviso |

## Ejemplos de solicitudes

### Compra confirmada

```json
{
  "destinatario": {
    "clienteId": 12,
    "email": "cliente@example.test"
  },
  "compraId": 101,
  "nombreEvento": "Recital en Tandil"
}
```

### Evento cancelado

```json
{
  "destinatario": {
    "clienteId": 12,
    "email": "cliente@example.test"
  },
  "eventoId": 45,
  "nombreEvento": "Recital en Tandil"
}
```

### Evento modificado

```json
{
  "destinatario": {
    "clienteId": 12,
    "email": "cliente@example.test"
  },
  "eventoId": 45,
  "versionEvento": 3,
  "nombreEvento": "Recital en Tandil",
  "detalleCambios": "El horario cambió a las 21:30."
}
```

### Artista favorito

```json
{
  "destinatario": {
    "clienteId": 12,
    "email": "cliente@example.test"
  },
  "eventoId": 45,
  "nombreEvento": "Recital en Tandil",
  "fechaHora": "2027-02-20T21:00:00",
  "ciudadEvento": "Tandil",
  "nombreArtista": "Banda Ejemplo",
  "ciudadCliente": "Tandil",
  "artistasFavoritos": ["Banda Ejemplo"]
}
```

## Respuestas

Las solicitudes nuevas devuelven `202 Accepted`, el detalle de la notificación y un encabezado `Location` con la ruta de consulta.

Si ya existe un registro equivalente, se reutiliza su identificador. La respuesta es `202` si continúa pendiente o `200` si tiene un estado final.

En artistas favoritos se devuelve `204 No Content` cuando no coinciden el artista o la ciudad.

La consulta `GET /notificaciones/{id}` devuelve el estado, los intentos y las fechas del procesamiento.

## Errores principales

| Código | Significado |
|---|---|
| `400` | Solicitud inválida |
| `401` | JWT ausente o inválido |
| `403` | Acceso no autorizado |
| `404` | Notificación inexistente |

## OpenAPI

- Swagger UI: `http://localhost:8087/swagger-ui.html`
- Especificación JSON: `http://localhost:8087/v3/api-docs`

La especificación exportada se conserva en `docs/openapi.json`.