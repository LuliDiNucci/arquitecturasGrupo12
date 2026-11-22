# Pruebas — Reservas

## 1. Objetivo

Se realizaron pruebas para verificar disponibilidad, concurrencia, idempotencia, vencimiento, confirmación, liberación, cancelaciones, seguridad e integración con otros microservicios.

Se utilizaron JUnit 5, Mockito, Spring Boot Test, MySQL, Postman y PowerShell.

## 2. Pruebas automatizadas

En la última ejecución completa de Maven, Reservas registró **63 pruebas satisfactorias**, sin fallos ni errores.

| Clase | Pruebas | Cobertura |
|---|---:|---|
| `ReservaServiceImplTest` | 10 | Reglas de negocio, vencimiento, cancelación y Fila Virtual. |
| `ServiceSecurityIntegrationTest` | 50 | Autenticación, roles y autorización de endpoints. |
| `InternalCredentialsTest` | 3 | Credenciales y autenticación de comunicaciones internas. |

Las pruebas de integración de seguridad utilizan Spring Boot y una base H2 en memoria.

Ejecución desde la raíz del proyecto:

```powershell
mvn -B test
```

## 3. Concurrencia

La clase `ConcurrenciaMySQLIT` permite verificar el comportamiento transaccional sobre MySQL.

Se ejecutan diez solicitudes concurrentes que intentan reservar el mismo asiento.

**Resultado verificado:** una sola solicitud consigue reservarlo, mientras que las demás son rechazadas.

La prueba utiliza una base MySQL independiente y se ejecuta explícitamente con su configuración de testing:

```powershell
mvn "-Dtest=ConcurrenciaMySQLIT" test
```

## 4. Pruebas funcionales e integración

Se verificaron los siguientes escenarios:

| Escenario | Resultado |
|---|---|
| Consulta de disponibilidad en sectores numerados y no numerados | Satisfactorio |
| Creación de reserva temporal | Satisfactorio |
| Confirmación después de pago aprobado | Satisfactorio |
| Liberación por pago rechazado | Satisfactorio |
| Vencimiento de reservas | Satisfactorio |
| Idempotencia de creación y liberación | Satisfactorio |
| Rechazo de lugares ocupados | Satisfactorio |
| Cancelación de reservas por evento | Satisfactorio |
| Liberación individual por devolución | Satisfactorio |
| Validación de turnos de Fila Virtual | Satisfactorio |
| Rechazo de turnos inexistentes, vencidos o no autorizados | Satisfactorio |
| Restricciones de acceso mediante JWT | Satisfactorio |

También se comprobaron respuestas de error cuando Eventos y Lugares no estaba disponible y la propagación del identificador de correlación durante llamadas internas.

## 5. Integración con TicketYa

Reservas participó en las pruebas del flujo completo:

1. Consulta de evento y disponibilidad.
2. Creación de reserva temporal.
3. Procesamiento de compra y pago.
4. Confirmación de reserva.
5. Emisión de entradas.
6. Devolución y liberación de lugares.
7. Cancelación de eventos y bloqueo de nuevas reservas.

Las pruebas confirmaron la coordinación con Eventos y Lugares, Compras, Entradas y Fila Virtual.

## 6. Conclusión

Los resultados comprobaron las principales reglas de negocio de Reservas, el control de concurrencia, la protección de sus endpoints y su integración con los demás componentes de TicketYa.