# 04 — Contratos REST

## 1. Acceso

El API Gateway publica las operaciones en `http://localhost:8084`. El microservicio también escucha directamente en `http://localhost:8086` para pruebas locales. Todas las operaciones de preferencias y recomendaciones requieren `Authorization: Bearer <JWT>`.

Un cliente solo puede acceder a su propio `clienteId`; un administrador puede acceder a preferencias y recomendaciones de otros clientes. La validación de propiedad se aplica en el microservicio, además de la autorización por roles del Gateway.

## 2. Endpoints de Recomendaciones

| Método | Ruta | Función | Respuesta principal |
|---|---|---|---|
| POST | `/api/preferencias/{clienteId}` | Registrar preferencias | `201` |
| GET | `/api/preferencias/{clienteId}` | Consultar preferencias | `200` |
| PUT | `/api/preferencias/{clienteId}` | Modificar preferencias | `200` |
| GET | `/api/recomendaciones/{clienteId}` | Consultar eventos recomendados | `200` |

### Registrar o modificar preferencias

**Solicitud JSON:**

```json
{
  "tiposEventos": ["RECITAL"],
  "artistasFavoritos": ["Banda de Prueba TicketYa"],
  "ciudad": "Tandil"
}
```

Las respuestas de preferencias contienen `clienteId`, `tiposEventos`, `artistasFavoritos` y `ciudad`.

### Consultar recomendaciones

`GET /api/recomendaciones/{clienteId}` devuelve una lista de eventos que coinciden con las preferencias o con sugerencias colaborativas del historial.

**Ejemplo de elemento de respuesta:**

```json
{
  "id": 1,
  "nombre": "Recital Alta Demanda TicketYa",
  "tipo": "RECITAL",
  "artista": "Banda de Prueba TicketYa",
  "ciudad": "Tandil",
  "fechaHora": "2027-02-20T21:00:00",
  "precio": 20000.0
}
```

Si no existen coincidencias, devuelve `200` con `[]`. Si el cliente no tiene preferencias registradas, devuelve `404`.

## 3. Contrato interno con Compras

**Endpoint en Compras:** `GET /compras/cliente/{clienteId}/eventos-recomendados`.

- Retorna una lista JSON de identificadores `Long`, por ejemplo `[2, 5]`, o `[]` si no hay sugerencias.
- Considera exclusivamente compras `CONFIRMADA`.
- Excluye los eventos que el cliente ya compró.
- Requiere JWT válido con rol `CLIENTE`; Compras verifica que el `clienteId` coincida con la identidad autenticada.
- Recomendaciones transmite el JWT original al llamar mediante Feign.

Este endpoint se utiliza en la comunicación interna; no se incorpora como ruta pública adicional en Gateway.

## 4. Códigos HTTP

| Código | Situación |
|---|---|
| `200` | Consulta o modificación correcta; incluye respuestas vacías de recomendaciones |
| `201` | Preferencias creadas |
| `400` | Datos de entrada inválidos |
| `401` | JWT ausente o inválido |
| `403` | Rol insuficiente o acceso a un cliente ajeno |
| `404` | Preferencias no registradas |
| `409` | Preferencias ya registradas al intentar crearlas nuevamente |

La indisponibilidad de una dependencia externa puede impedir completar las recomendaciones; la respuesta exacta depende del error propagado por Feign y del manejo de excepciones configurado.

## 5. OpenAPI

- Swagger UI del microservicio: `http://localhost:8086/swagger-ui.html`.
- Especificación OpenAPI a través de Gateway: `http://localhost:8084/recomendaciones/v3/api-docs`.
- Swagger UI de Gateway: `http://localhost:8084/swagger-ui.html` (incluye el grupo Recomendaciones).
