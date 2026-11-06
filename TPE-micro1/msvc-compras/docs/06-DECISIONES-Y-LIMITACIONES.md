# 06 — Decisiones, supuestos y limitaciones

**Estado:** propuesta v0.1. Revisar esta página al cambiar cualquier decisión.

## Decisiones

1. **Un servicio, una base:** `ticketya_compras` separada de Reservas, Eventos y Entradas; IDs remotos sin FK entre esquemas.
2. **Java 21 y Spring Boot 4.1.2:** misma base tecnológica que Reservas, ya utilizada por el grupo; Swagger desde el primer commit.
3. **Puertos:** Reservas 8080, Eventos 8081, Compras 8082 y Entradas 8083.
4. **Precio congelado:** el servidor consulta Eventos al iniciar la compra y persiste `BigDecimal` unitario, subtotal, total y valor por entrada individual.
5. **Compra única por reserva:** `UNIQUE(reserva_id)` evita múltiples compras por la misma reserva, incluso con solicitudes simultáneas.
6. **Pago idempotente:** `UNIQUE(clave_idempotencia)` + huella de contenido; mismo request devuelve resultado registrado y clave reutilizada con contenido distinto genera conflicto. La pasarela mock también debe admitir identificación idempotente.
7. **Pago seguro con varias instancias:** una transacción reserva el derecho de procesar el intento; una segunda petición nunca puede lanzar un nuevo cobro sobre el mismo pago. Se evaluará bloqueo pesimista y estados `EN_PROCESO/INDETERMINADO`.
8. **Compra exitosa solo tras completar todos los pasos:** si hay cobro aprobado pero no se confirmó la reserva o faltan entradas, guardar estado intermedio y gestionar reparación; no falsificar `CONFIRMADA`.
9. **Reintegro por entrada individual:** `ItemCompra` y `Reintegro` con restricción única por item; permite parciales, cancelación del evento y evitar duplicados.
10. **Mock de pasarela:** adaptador con resultados configurables y sin datos de tarjeta sensibles. Entrega 1; integración real futura.
11. **Sin JWT en Entrega 1:** restringir rutas internas por red/Gateway, no son de uso público.

## Pendientes y riesgos conocidos

- Los endpoints de Entradas aún no están acordados. Definir contrato de emisión idempotente y devolución aprobada antes de implementar US04/US07.
- La pasarela mock debe definir un resultado determinista y una referencia única, incluido qué pasa si la respuesta se pierde tras aprobar un cobro.
- Conciliación/compensación ante fallos parciales. La consigna sitúa el diseño completo en Entrega 2, pero US04 requiere ya no declarar éxito incorrectamente.
- El estado ACTIVA de Reservas no basta: también comparar `fechaVencimiento` con la hora actual, porque Reservas usa vencimiento lógico y su transición en MySQL puede diferirse.
- La devolución requiere verificar aprobación de Entradas; no aceptar reintegros arbitrarios desde un cliente externo.
- Eventos tiene un `ComprasFeignClient` que ya espera `POST /compras/evento/{id}/cancelar`, con encabezado `X-Operacion-Id`. Actualizar su URL de Compras a `8082` al integrar.
- `ddl-auto=update` solo para desarrollo; definir migraciones antes del despliegue.

## Proceso para no reescribir documentación al final

Por cada user story: (1) criterio + test antes del código, (2) entidades y cambios DER, (3) DTO y contrato OpenAPI, (4) repositorio/service/controller/Feign, (5) test con evidencia, (6) actualizar estado en README, (7) actualizar Postman. Ninguna historia pasa a «completa» sin los siete pasos.
