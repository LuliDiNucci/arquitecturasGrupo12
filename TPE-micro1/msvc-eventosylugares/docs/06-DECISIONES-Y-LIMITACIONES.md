# Decisiones técnicas y límites — TicketYa Eventos

## Persistencia

Este microservicio usa una **base relacional propia**. Por defecto H2 persistente, para levantar sin instalar un servidor; con perfil `mysql` usa `ticketya_eventos`. Cada servicio del grupo debe tener su propia base/esquema. `ddl-auto=update` simplifica el desarrollo, pero en un entorno real se reemplazaría por migraciones versionadas.

## Separación de responsabilidades

- Eventos: configuración de lugares, sectores, asientos, precios, estados y búsquedas.
- Reservas: inventario real, reservas de 10 minutos, bloqueo simultáneo y límite por cliente.
- Compras: importe total, intentos de pago, idempotencia de cobros, reintegros.
- Entradas: emisión de QR, devolución, invalidación.

`GET /eventos/{id}/sectores` **no indica asientos libres**: muestra la disposición física y los precios. Para comprar, Reservas verifica el estado y opera sobre su inventario.

## Cancelaciones y consistencia

Cambiar el estado a CANCELADO y guardar el aviso pendiente ocurre dentro de la misma transacción JPA. Un scheduler utiliza clientes Feign para intentar entregar los avisos cuando `INTEGRACIONES_HABILITADAS=true`. Esta es una implementación **básica de outbox**, no mensajería garantizada ni una saga completa. El receptor debe aceptar repeticiones mediante `X-Operacion-Id`.

- Se añadieron avisos a **Compras** porque la cancelación también afecta reintegros.
- Un aviso marcado como enviado indica **HTTP 2xx**, no que el reintegro ya se ejecutó.
- Por defecto la integracion esta deshabilitada y los avisos quedan pendientes. Para habilitarla hay que configurar las URLs reales y la variable `INTEGRACIONES_HABILITADAS=true`.
- Los tres endpoints receptores requieren **acuerdo explícito del grupo**.
- El envío REST en transacción del scheduler es una simplificación local; en Entrega 2 conviene usar un patrón más robusto de reclamación de mensajes y deduplicación entre instancias.

## Seguridad y ejecución

En Entrega 1 la cátedra no requiere JWT ni roles, por lo tanto `PATCH /eventos/{id}/cancelar` y `incluirNoPublicados=true` todavía **no tienen controles de autorización**. Solo para uso local o en entorno controlado. La Entrega 2 exige JWT y roles apropiados.

## Alcance de Entrega 2

No se implementan: fila virtual, búsqueda personalizada, notificaciones a clientes, reportes para organizadores, escaneo/validación de QR, MongoDB, JWT, caché avanzada, logging centralizado, Docker Compose del sistema, pruebas de carga. Son actividades posteriores según la tabla de entregas del PDF.

## Observaciones para el equipo

1. Establecer el formato exacto de `eventoId`, `sectorId`, `asientoId` y `precio` en cada DTO REST interservicio.
2. Reservas debe consultar `habilitado` y `sectores`, pero su propia base define el inventario.
3. Compras debe almacenar el precio al momento de comprar, no recalcular precios históricos al confirmar.
4. El gateway lo configura otra persona; para probar Eventos solo no hace falta.
5. La documentación de features e historias del **sistema completo**, los diagramas globales y la prueba de no doble venta se consolidan en el repositorio del grupo.
