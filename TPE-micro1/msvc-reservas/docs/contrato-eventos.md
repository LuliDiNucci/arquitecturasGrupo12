# Contrato pendiente con msvc-eventos-lugares


| Operación Feign | Método / ruta | Respuesta requerida |
|---|---|---|
| obtenerEvento | `GET /eventos/{id}` | `{id,nombre,fechaHora,estado,lugarId}` (`estado=PUBLICADO`) |
| obtenerSector | `GET /sectores/{id}` | `{id,nombre,numerado,capacidad,lugarId}` |
| obtenerAsiento | `GET /asientos/{id}` | `{id,fila,numero,sectorId}` |
| obtenerAsientosPorSector | `GET /sectores/{id}/asientos` | `[{id,fila,numero,sectorId},...]` |
| obtenerPrecioSector | `GET /eventos/{eventoId}/sectores/{sectorId}/precio` | `{eventoId,sectorId,precio}`; 404 si no existe relación |

## Condiciones de integración obligatorias

- Configurar `msvc.eventos.url` con el puerto real del otro servicio.
- El servicio de Eventos debe rechazar compras de eventos cancelados y **llamar a** `POST /reservas/evento/{eventoId}/cancelar` como parte de su flujo de cancelación, coordinado con Compras/Entradas.
- La operación de cancelación debe detener ventas antes de procesar nuevas solicitudes; sin transacción entre bases **no hay atomicidad global automática**.
- Compras confirma / libera una reserva cuando se aprueba / rechaza el pago; un pago aprobado tras vencimiento requiere reversión/compensación.
- Entradas valida titularidad, ID de entrada, plazo de 72 horas y aprobación; solicita liberación de UNA entrada y Compras solicita reintegro.
- Los endpoints internos NO deben exponerse a clientes arbitrarios a través del Gateway.
- Concurrencia: todos los cambios de disponibilidad del mismo evento dentro de Reservas usan el bloqueo de su fila `bloqueo_evento`; el código requiere una prueba con MySQL real.

- Antes de permitir la venta de entradas de un evento, `msvc-eventos-lugares` debe llamar a `POST /reservas/evento/{eventoId}/habilitar`. Este endpoint es interno y habilita las reservas únicamente si el evento existe, está PUBLICADO y no fue cancelado. La operación es idempotente: puede repetirse sin crear registros duplicados.

