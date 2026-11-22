# 04 — Contratos REST

## Acceso

- Microservicio: `msvc-entradas`
- Puerto: `8083`
- API Gateway: `http://localhost:8084`
- Formato: JSON
- Autenticación: `Authorization: Bearer <token>`

## Endpoints

| Método | Ruta | Operación |
|---|---|---|
| POST | `/entradas/emitir` | Emitir entradas de una compra |
| GET | `/entradas/{id}` | Consultar una entrada |
| GET | `/entradas/cliente/{clienteId}` | Consultar entradas de un cliente |
| POST | `/entradas/validar` | Validar un ingreso mediante QR |
| POST | `/entradas/evento/{id}/invalidar` | Invalidar entradas de un evento cancelado |
| GET | `/entradas/evento/{eventoId}/reporte` | Consultar entradas para reportes |
| POST | `/devoluciones` | Solicitar devolución |
| GET | `/devoluciones/pendientes` | Consultar solicitudes pendientes |
| PATCH | `/devoluciones/{id}/aprobar` | Aprobar devolución |
| PATCH | `/devoluciones/{id}/rechazar` | Rechazar devolución |
| PUT | `/entradas/accesos/eventos/{eventoId}/personal/{usuarioId}` | Asignar personal |
| DELETE | `/entradas/accesos/eventos/{eventoId}/personal/{usuarioId}` | Revocar asignación |

## Solicitudes principales

### Emitir entradas

**POST `/entradas/emitir`**

```json
{
  "compraId": 1
}
```

La operación verifica la compra y la reserva mediante sus respectivos microservicios.

Respuestas:
- `201 Created`: entradas emitidas.
- `200 OK`: entradas existentes devueltas en un reintento.

### Consultar entrada

**GET `/entradas/{id}`**

Requiere el encabezado `X-Cliente-Id`. Se verifica la identidad y la titularidad de la entrada.

### Validar QR

**POST `/entradas/validar`**

```json
{
  "codigoQr": "codigo-unico-de-entrada",
  "eventoId": 1
}
```

Respuesta exitosa: `200 OK`, con la entrada en estado `UTILIZADA`.

Si la entrada fue utilizada, está invalidada o no corresponde al evento, se rechaza la validación.

### Solicitar devolución

**POST `/devoluciones`**

```json
{
  "entradaId": 1,
  "motivo": "No puedo asistir"
}
```

La entrada debe estar emitida y la solicitud debe realizarse al menos 72 horas antes del evento.

### Administrar personal de acceso

**PUT `/entradas/accesos/eventos/{eventoId}/personal/{usuarioId}`**

Asigna un usuario al evento.

**DELETE `/entradas/accesos/eventos/{eventoId}/personal/{usuarioId}`**

Revoca la asignación.

Ambas operaciones requieren el rol `ADMINISTRADOR` y responden `204 No Content` cuando finalizan correctamente.

## Permisos

| Operación | Acceso |
|---|---|
| Emisión de entradas | Servicio Compras |
| Consulta de entradas | Cliente titular, administrador o servicio Compras |
| Validación QR | Personal de acceso asignado al evento |
| Solicitud de devolución | Cliente titular |
| Gestión de devoluciones | Administrador |
| Asignación de personal | Administrador |
| Invalidación por cancelación | Servicios Eventos o Compras |
| Reporte de entradas | Servicio Eventos |

## Respuestas HTTP habituales

- `200`: operación o consulta realizada.
- `201`: entradas creadas.
- `204`: operación completada sin contenido.
- `400`: solicitud inválida.
- `401`: autenticación ausente o inválida.
- `403`: permisos insuficientes.
- `404`: recurso inexistente.
- `409`: conflicto con una regla de negocio.

## OpenAPI

La especificación completa de endpoints y DTO se encuentra en `docs/openapi.json`, generada a partir de la aplicación mediante Springdoc OpenAPI.