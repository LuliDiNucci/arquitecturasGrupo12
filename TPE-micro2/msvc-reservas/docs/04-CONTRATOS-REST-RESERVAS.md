# Contratos REST — Reservas

## 1. Información general

El microservicio ofrece una API REST con intercambio de datos JSON.

- Puerto: `8080`
- Acceso mediante Gateway: `http://localhost:8084`
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI: `http://localhost:8080/v3/api-docs`

## 2. Endpoints

| Método | Endpoint | Función | Acceso |
|---|---|---|---|
| GET | `/disponibilidad` | Consultar disponibilidad por evento y sector. | Público |
| POST | `/reservas` | Crear reserva temporal. | Cliente autenticado |
| GET | `/reservas/{id}` | Consultar reserva. | Propietario, administrador o servicio autorizado |
| POST | `/reservas/{id}/confirmar` | Confirmar reserva después del pago. | Compras |
| POST | `/reservas/{id}/liberar` | Liberar reserva temporal. | Compras |
| POST | `/reservas/{id}/devolver-entrada` | Liberar el lugar de una entrada devuelta. | Compras o Entradas |
| POST | `/reservas/evento/{eventoId}/habilitar` | Habilitar reservas para un evento. | Eventos y Lugares |
| POST | `/reservas/evento/{eventoId}/cancelar` | Cancelar reservas temporales y bloquear nuevas reservas. | Eventos y Lugares |

## 3. Consultar disponibilidad

```http
GET /disponibilidad?eventoId=4&sectorId=2
```

Respuesta:

```json
{
  "eventoId": 4,
  "sectorId": 2,
  "lugaresDisponibles": 3,
  "asientosDisponibles": [25, 26, 27],
  "asientosOcupados": []
}
```

En sectores no numerados, las listas de asientos están vacías y se informa la capacidad disponible.

## 4. Crear una reserva

```http
POST /reservas
Authorization: Bearer <JWT_CLIENTE>
Content-Type: application/json
```

Ejemplo:

```json
{
  "clienteId": 27,
  "eventoId": 4,
  "claveIdempotencia": "reserva-cliente-27-001",
  "detalles": [
    {
      "sectorId": 2,
      "cantidad": 1,
      "asientoIds": []
    }
  ]
}
```

La respuesta exitosa es `201 Created`.

La reserva incluye su identificador, estado `ACTIVA`, fecha de creación, fecha de vencimiento y detalles.

Para sectores numerados, `asientoIds` debe contener los identificadores de los asientos seleccionados.

## 5. Operaciones internas

**Confirmar reserva:**

```http
POST /reservas/{id}/confirmar
```

Cambia una reserva activa y vigente a `CONFIRMADA`.

**Liberar reserva:**

```http
POST /reservas/{id}/liberar
```

Libera los lugares ocupados temporalmente. No permite liberar reservas confirmadas mediante esta operación.

**Devolver entrada:**

```http
POST /reservas/{id}/devolver-entrada
Content-Type: application/json
```

```json
{
  "entradaId": 17,
  "sectorId": 2,
  "asientoId": null
}
```

Libera una entrada individual. En sectores numerados se indica el asiento correspondiente.

**Habilitar y cancelar eventos:**

```http
POST /reservas/evento/{eventoId}/habilitar
POST /reservas/evento/{eventoId}/cancelar
```

Ambas operaciones devuelven `204 No Content` cuando se completan correctamente.

Las operaciones internas requieren un token de servicio válido.

## 6. Validaciones y errores

| HTTP | Significado |
|---|---|
| `200 OK` | Consulta o modificación exitosa. |
| `201 Created` | Reserva creada. |
| `204 No Content` | Operación interna completada sin cuerpo. |
| `400 Bad Request` | Solicitud inválida. |
| `401 Unauthorized` | JWT ausente o inválido. |
| `403 Forbidden` | Permisos insuficientes. |
| `404 Not Found` | Recurso inexistente. |
| `409 Conflict` | Reserva vencida, conflicto de disponibilidad o estado incompatible. |
| `503 Service Unavailable` | Dependencia necesaria no disponible. |

La especificación OpenAPI incluye los DTO y las respuestas detalladas de cada operación.