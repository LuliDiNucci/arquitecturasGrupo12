# 04 — Contratos REST

## Acceso a la API

- Servicio: `msvc-fila-virtual`
- Puerto: `8088`
- Acceso mediante Gateway: `http://localhost:8084`
- Formato: JSON
- Autenticación: JWT

## Endpoints

| Método | Ruta | Operación | Acceso |
|---|---|---|---|
| POST | `/filas/{eventoId}/ingresar` | Ingresar a la fila | CLIENTE |
| GET | `/filas/{eventoId}/clientes/{clienteId}` | Consultar estado y posición | CLIENTE titular o ADMINISTRADOR |
| POST | `/filas/{eventoId}/habilitar` | Habilitar una tanda | ADMINISTRADOR |
| GET | `/filas/{eventoId}/clientes/{clienteId}/validar` | Validar autorización | Servicio Reservas |
| POST | `/filas/{eventoId}/clientes/{clienteId}/finalizar` | Finalizar autorización | Servicio Reservas |

## Operaciones principales

### Ingresar a la fila

**POST `/filas/{eventoId}/ingresar`**

```json
{
  "clienteId": 10
}
```

El identificador debe coincidir con el cliente autenticado.

Se verifica que el evento esté publicado y sea de alta demanda.

Respuesta: `200 OK`, con el registro del cliente.

### Consultar posición

**GET `/filas/{eventoId}/clientes/{clienteId}`**

Devuelve el estado y la posición del cliente, junto con las fechas de habilitación y vencimiento cuando corresponda.

### Habilitar tanda

**POST `/filas/{eventoId}/habilitar`**

```json
{
  "cantidad": 3
}
```

Requiere el rol `ADMINISTRADOR`.

La cantidad debe estar entre 1 y 100.

Respuesta: `200 OK`, con los clientes habilitados.

### Validar turno

**GET `/filas/{eventoId}/clientes/{clienteId}/validar`**

Operación interna de Reservas.

Respuesta de ejemplo:

```json
{
  "eventoId": 1,
  "clienteId": 10,
  "autorizado": true,
  "estado": "HABILITADO",
  "fechaVencimiento": "2026-12-15T20:10:00"
}
```

La validación verifica la vigencia del turno, pero no lo consume.

### Finalizar turno

**POST `/filas/{eventoId}/clientes/{clienteId}/finalizar`**

Operación interna de Reservas.

Finaliza una autorización utilizada y permite reintentos idempotentes.

Se admiten turnos `HABILITADO` y `VENCIDO` cuando corresponde registrar una finalización tardía.

## Respuestas HTTP

| Código | Significado |
|---|---|
| `200` | Operación o consulta exitosa |
| `400` | Datos inválidos |
| `401` | JWT ausente o inválido |
| `403` | Permisos insuficientes |
| `404` | Evento o registro inexistente |
| `409` | Conflicto con una regla de negocio |
| `503` | Servicio externo no disponible |

## OpenAPI

La especificación completa de endpoints, parámetros, respuestas y DTO se encuentra en `docs/openapi.json`, generada mediante Springdoc OpenAPI.