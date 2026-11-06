# 05 — Plan y evidencia de testing

**Estado actual: pruebas pendientes.** No atribuir resultados hasta ejecutar tests reales.

## Estrategia

- **JUnit/Mockito:** reglas de negocio por Service, Clients simulados, pasarela mock determinista.
- **Integración MySQL:** claves únicas, transacciones y simultaneidad sobre varias solicitudes/instancias; evitar confiar solamente en mocks para la no duplicación.
- **Pruebas manuales:** Swagger/Postman/PowerShell; verificar estado de tablas después.
- **Integración real:** con Reservas y Eventos ya disponibles; con Entradas cuando su contrato esté implementado.

## Casos por historia

| ID | Caso | Comprobación esperada | Estado |
|---|---|---|---|
| US01-01 | Reserva ACTIVA y vigente | Compra PENDIENTE | Pendiente |
| US01-02 | Reserva VENCIDA, LIBERADA o CANCELADA | No crea compra | Pendiente |
| US01-03 | Dos solicitudes para misma reserva | Una compra por `reservaId` | Pendiente |
| US02-01 | Dos sectores y N asientos | Detalles e items correctos; precios congelados | Pendiente |
| US02-02 | Diferencias por redondeo | Totales exactos con `BigDecimal` | Pendiente |
| US03-01 | Pasarela aprueba | Pago APROBADO; fecha, medio y monto guardados | Pendiente |
| US03-02 | Pasarela rechaza | Pago RECHAZADO; aviso de rechazo | Pendiente |
| US03-03 | Reserva vencida al pagar | No cobra | Pendiente |
| US04-01 | Pago aprobado + emisión completa | Compra CONFIRMADA con todos los entradaId | Pendiente |
| US04-02 | Fallo de confirmación o emisión | Compra no declara éxito; guarda estado de recuperación | Pendiente |
| US05-01 | Pago rechazado | Compra RECHAZADA, Reservas libera, sin tickets | Pendiente |
| US06-01 | Reintento misma clave/datos | Igual resultado y un cobro | Pendiente |
| US06-02 | Misma clave con datos diferentes | 409, sin nuevo cobro | Pendiente |
| US06-03 | 10 pagos simultáneos | Un único cobro; prueba con MySQL real | Pendiente |
| US07-01 | Devolución aprobada de 1 de 3 entradas | Solo 1 reintegro con precio histórico | Pendiente |
| US07-02 | Devolución repetida | No reintegra dos veces | Pendiente |
| US08-01 | Cancelación de evento con varias compras | Reintegro por item elegible | Pendiente |
| US08-02 | Aviso de cancelación repetido | No duplica reintegros | Pendiente |
| US08-03 | Una entrada ya reintegrada antes de cancelar | Excluida del nuevo reintegro | Pendiente |

## Evidencia a registrar al implementar

Registrar fecha, comando o request, entrada, salida HTTP/JSON, estado de MySQL, versión o commit y resultado. Documentar casos pendientes y fallidos sin marcarlos como aprobados. Para la prueba de concurrencia registrar cantidad de hilos, cantidad de cobros creados y restricciones únicas verificadas.
