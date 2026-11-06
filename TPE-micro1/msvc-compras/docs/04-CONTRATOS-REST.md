# 04 — Contratos REST propuestos (no implementados todavía)

## Endpoints propios de Compras

| Operación | Método / ruta | Entrada | Resultado esperado |
|---|---|---|---|
| Iniciar compra | `POST /compras` | `{clienteId,reservaId}` | `201 CompraDTO` PENDIENTE; 400/404/409/503 |
| Consultar compra | `GET /compras/{id}` | ID | `200 CompraDTO`; 404 |
| Procesar pago | `POST /compras/{id}/pagos` | `{medioPago,claveIdempotencia}` | `200 PagoDTO` final, `202` en proceso, `409` por conflicto; 503 dependencia |
| Consultar pagos | `GET /compras/{id}/pagos` | ID | `200 [PagoDTO]` |
| Reintegrar una entrada (interno) | `POST /compras/reintegros` | `{entradaId,operacionId,motivo}`; SOLO aprobación verificada | `200/202 ReintegroDTO`, 400/404/409 |
| Consultar reintegros | `GET /compras/{id}/reintegros` | ID | `200 [ReintegroDTO]` |
| Notificar cancelación (interno) | `POST /compras/evento/{eventoId}/cancelar` | encabezado `X-Operacion-Id` | `204`, idempotente |

**Estos códigos y cuerpos son una propuesta del servicio nuevo, no una API existente.** Ajustar Swagger y colección Postman junto con la implementación; documentar siempre request, response, errores y ejemplos.

### Ejemplo iniciar compra

```http
POST /compras
Content-Type: application/json

{"clienteId": 10, "reservaId": 15}
```

La solicitud **no** acepta precios ni total desde el cliente. El servicio obtiene detalles y fecha de vencimiento de Reservas y el precio de Eventos, luego guarda la instantánea de precios.

### Ejemplo pago

```http
POST /compras/1/pagos
Content-Type: application/json

{"medioPago":"TARJETA", "claveIdempotencia":"PAGO-1-001"}
```

No enviar ni persistir número de tarjeta, CVV ni otros datos sensibles. La pasarela mock decide el resultado según una configuración de prueba documentada y reproducible; nunca por el valor de `medioPago`.

## Contratos ya conocidos de los otros servicios

**Reservas (observados en código previamente proporcionado):**
- `GET /reservas/{id}` → `ReservaDTO {id,clienteId,eventoId,fechaCreacion,fechaVencimiento,estado,detalles:[{id,sectorId,cantidad,cantidadLiberada,asientoIds}]}`.
- `POST /reservas/{id}/confirmar` → reserva actualizada, exige estado ACTIVA y vigencia.
- `POST /reservas/{id}/liberar` → reserva liberada.

**Eventos (ruta acordada en la integración anterior; comprobar en la rama actual):**
- `GET /eventos/{eventoId}/sectores/{sectorId}/precio` → `{eventoId,sectorId,precio}`; 404 si la relación no existe.
- Desde Eventos a Compras: `POST /compras/evento/{id}/cancelar` con `X-Operacion-Id` (ruta ya anticipada en su `ComprasFeignClient`).

**Entradas: PENDIENTE DE ACORDAR.** Falta definir emisión idempotente, retorno de una entrada por cada item de compra, asociación de ID de entrada y aviso de devolución aprobada. No inventar una ruta definitiva.

## Validaciones clave

- Al iniciar y justo antes del cobro: reserva `ACTIVA`, `fechaVencimiento > ahora`, cliente consistente y cantidades válidas.
- Una compra por `reservaId`, garantizada por restricción única en MySQL.
- Pago: misma clave + mismos datos = mismo resultado; misma clave + datos distintos = 409; concurrencia sin cobros duplicados.
- No declarar `CONFIRMADA` antes de confirmar Reservas y asociar todas las entradas esperadas.
- Reintegro basado en `ItemCompra.precioPagado` y único por item, aunque llegue dos veces un aviso de cancelación o devolución.
