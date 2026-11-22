# 03 — Contratos REST

## Acceso a la API

- Servicio: `msvc-compras`
- Puerto: `8082`
- Acceso a través del Gateway: `http://localhost:8084`
- Formato de intercambio: JSON
- Autenticación: JWT mediante `Authorization: Bearer <token>`

Las operaciones internas utilizan credenciales de servicio y permisos específicos.

## Endpoints

| Método | Ruta | Descripción |
|---|---|---|
| POST | `/compras` | Iniciar una compra |
| GET | `/compras/{id}` | Consultar una compra |
| POST | `/compras/{id}/pagar` | Procesar un pago |
| POST | `/compras/{id}/confirmar` | Confirmar la compra y emitir entradas |
| GET | `/compras/{id}/confirmacion` | Consultar el estado de pago y confirmación |
| POST | `/compras/{id}/reintentar-liberacion` | Reintentar la liberación de una reserva |
| POST | `/compras/reintegros` | Procesar el reintegro de una entrada |
| GET | `/compras/{id}/reintegros` | Consultar reintegros |
| POST | `/compras/evento/{eventoId}/cancelar` | Procesar cancelación de evento |
| POST | `/compras/{id}/compensar` | Compensar una compra pagada sin emisión válida |
| GET | `/compras/evento/{eventoId}/compradores` | Consultar compradores para avisos internos |
| GET | `/compras/evento/{eventoId}/reporte` | Consultar ventas y recaudación |
| GET | `/compras/cliente/{clienteId}/eventos-recomendados` | Obtener eventos sugeridos por historial |

## Operaciones principales

### Iniciar compra

**POST `/compras`**

```json
{
  "clienteId": 1,
  "reservaId": 1
}
```

Respuesta: `201 Created`, con el detalle de la compra creada en estado `PENDIENTE`.

### Procesar pago

**POST `/compras/{id}/pagar`**

```json
{
  "medioPago": "TARJETA",
  "claveIdempotencia": "PAGO-001"
}
```

Medios admitidos: `TARJETA`, `TRANSFERENCIA` y `BILLETERA`. También se admite `TARJETA_RECHAZADA` para simular un rechazo.

Respuesta: `200 OK`, con el resultado registrado del pago.

La clave de idempotencia identifica la operación y evita cobros duplicados.

### Confirmar compra

**POST `/compras/{id}/confirmar`**

Requiere un pago aprobado. Confirma la reserva y solicita la emisión de las entradas. Devuelve la compra con su estado actualizado.

### Procesar reintegro

**POST `/compras/reintegros`**

```json
{
  "entradaId": 1,
  "motivo": "DEVOLUCION_CLIENTE"
}
```

Esta operación es interna y requiere que Entradas haya aprobado previamente la devolución.

### Cancelar compras de un evento

**POST `/compras/evento/{eventoId}/cancelar`**

Requiere el encabezado `X-Operacion-Id`. Procesa los reintegros correspondientes a las compras del evento cancelado.

Respuesta exitosa: `204 No Content`.

## Permisos

| Operaciones | Acceso |
|---|---|
| Iniciar, pagar, confirmar y reintentar liberación | Cliente autenticado y titular |
| Consultar compras y reintegros | Cliente titular o administrador |
| Consultar confirmación | Cliente titular, administrador o servicio Entradas |
| Procesar devolución | Servicio Entradas |
| Cancelación, compradores y reportes | Servicio Eventos |
| Compensación administrativa | Administrador |
| Recomendaciones por historial | Cliente titular |

## Respuestas HTTP

| Código | Significado |
|---|---|
| `200` | Consulta u operación realizada correctamente |
| `201` | Compra creada |
| `204` | Operación completada sin cuerpo de respuesta |
| `400` | Datos de solicitud inválidos |
| `401` | Falta de autenticación o JWT inválido |
| `403` | Permisos o titularidad insuficientes |
| `404` | Recurso inexistente |
| `409` | Conflicto con una regla de negocio |
| `503` | Dependencia externa no disponible |

## Documentación OpenAPI

La especificación completa de los endpoints, parámetros y DTO se encuentra en `docs/openapi.json`, generada a partir de la aplicación Spring Boot.

Swagger UI permite consultar y probar la API durante la ejecución del servicio.