# 01 — Funcionalidades e historias de usuario de Reservas

**Proyecto:** TicketYa · Microservicio `msvc-reservas` · Entrega 1  
**Fuente de requisitos:** historias **US01–US07** proporcionadas por el equipo.  
**Feature:** Gestión de reservas y disponibilidad

## Objetivo

El microservicio administra la ocupación temporal y confirmada de lugares de eventos, calcula disponibilidad para sectores numerados o no numerados y expone operaciones internas para confirmar, liberar, cancelar o registrar una devolución. Obtiene los datos de eventos, sectores, asientos y precios desde Eventos y Lugares; no ejecuta pagos ni emite entradas.

Los criterios que siguen se conservan conforme a las historias proporcionadas. **Después de cada historia** se indica por separado el grado de cobertura actual, sin reinterpretar el requisito como si ya estuviera implementado.

---

## US01 – Consultar disponibilidad de entradas

**Como cliente**, quiero consultar los asientos o lugares disponibles de un sector para un evento, para poder elegir dónde realizar mi reserva.

**Criterios de aceptación:**

- El cliente debe seleccionar un evento y un sector.
- Si el sector es numerado, el sistema debe mostrar los asientos disponibles y ocupados.
- Si el sector no es numerado, debe mostrar la cantidad de lugares disponibles.
- No se deben ofrecer como disponibles los lugares reservados temporalmente ni los vendidos.
- La disponibilidad debe corresponder al evento seleccionado.

**Implementación actual:** `GET /disponibilidad?eventoId={id}&sectorId={id}`; `DisponibilidadController`, `DisponibilidadServiceImpl`, `DisponibilidadDTO`.

**Cobertura: PARCIAL.** Se exige evento y sector, se consulta la pertenencia del sector al lugar y se verifica que haya precio para ese evento. Para sectores numerados se devuelve `asientosDisponibles`; para los no numerados, `lugaresDisponibles`. Se excluyen ocupaciones confirmadas y reservas activas no vencidas. Un evento cancelado localmente responde `409`.

**Criterio pendiente:** en sectores numerados **no se devuelve la lista de `asientosOcupados`**; actualmente esos asientos solo se excluyen de `asientosDisponibles`. Para cumplir íntegramente esta historia hay que ampliar el DTO y el cálculo de disponibilidad, además de actualizar contratos y pruebas. La consulta es informativa; no bloquea asientos.

## US02 – Realizar una reserva temporal

**Como cliente**, quiero reservar temporalmente uno o varios lugares de un evento, para asegurar su disponibilidad mientras realizo el pago.

**Criterios de aceptación:**

- El cliente debe seleccionar un evento, un sector y la cantidad de entradas.
- Si el sector es numerado, debe seleccionar los asientos específicos.
- Solo se pueden reservar lugares disponibles.
- La reserva debe tener un tiempo de expiración, por ejemplo, 10 minutos.
- Una reserva activa debe bloquear temporalmente los lugares seleccionados.
- El sistema debe registrar el cliente, el evento, el sector, los lugares reservados y el vencimiento.
- El evento debe estar habilitado para la venta.

**Implementación actual:** `POST /reservas`; `ReservaController`, `ReservaServiceImpl`, `Reserva`, `DetalleReserva`, `AsignacionAsiento`.

**Cobertura: IMPLEMENTADA LOCALMENTE.** Reserva `ACTIVA` con vencimiento de 10 minutos, validaciones de pertenencia de sector/asientos, precio y disponibilidad, habilitación previa del evento y límite de entradas configurable. La ocupación se registra en MySQL. El cliente Feign de Eventos está implementado; falta comprobar compatibilidad y flujo completo contra los microservicios reales.

## US03 – Evitar reservas simultáneas del mismo lugar

**Como cliente**, quiero que los lugares seleccionados queden protegidos frente a reservas simultáneas, para asegurar que mi reserva no se superponga con la de otro cliente.

**Criterios de aceptación:**

- Un asiento no puede tener dos reservas activas para el mismo evento.
- En sectores no numerados, no se puede superar la capacidad máxima disponible.
- Si dos clientes intentan reservar simultáneamente el mismo asiento, solo uno debe conseguirlo.
- El sistema debe informar cuando no se pueda concretar una reserva por falta de disponibilidad.
- El mecanismo debe funcionar incluso cuando existan varias instancias del microservicio.
- La operación debe ser segura frente a solicitudes repetidas, evitando que un reintento genere reservas duplicadas.

Esta historia es especialmente importante porque la consigna exige una prueba de concurrencia para verificar que no se produzca una doble venta.

**Implementación actual:** `ReservaServiceImpl`, `BloqueoEventoRepository`, `ClaveIdempotenciaBloqueoRepository`, claves únicas, transacciones MySQL y `ConcurrenciaMySQLIT`.

**Cobertura: IMPLEMENTADA; PRUEBAS ADICIONALES PENDIENTES.** Se serializan operaciones para un evento con bloqueo pesimista `PESSIMISTIC_WRITE` sobre la base MySQL compartida; se valida la capacidad de sectores no numerados y se rechaza la falta de lugares. Hay control por `claveIdempotencia` para reintentos. **Se probó en MySQL** la competencia de 10 solicitudes por el mismo asiento, con una sola reserva exitosa, y reintentos secuenciales y simultáneos. **Todavía no se verificó ejecutando varias instancias reales de Reservas al mismo tiempo**; es una prueba de despliegue pendiente.

## US04 – Liberar reservas vencidas

**Como sistema**, quiero liberar automáticamente los lugares de una reserva cuando expire su tiempo de validez, para que otros clientes puedan reservarlos.

**Criterios de aceptación:**

- Cada reserva debe tener una fecha y hora de vencimiento.
- Si el tiempo de reserva finaliza sin confirmación, su estado debe cambiar a VENCIDA.
- Los asientos o lugares asociados deben volver a estar disponibles.
- Una reserva vencida no puede confirmarse posteriormente.
- El sistema debe actualizar correctamente la disponibilidad del sector.
- La liberación no debe afectar entradas que ya hayan sido vendidas.

**Implementación actual:** `fechaVencimiento` y `EstadoReserva.VENCIDA` en `ReservaServiceImpl`; consultas de ocupación filtradas por vencimiento en repositorios y en `DisponibilidadServiceImpl`.

**Cobertura: IMPLEMENTADA CON VENCIMIENTO LÓGICO.** Al cumplirse los 10 minutos, las reservas `ACTIVA` dejan de contarse como ocupación y los lugares vuelven a estar disponibles aunque el registro conserve temporalmente ese estado en MySQL. Una reserva vencida no puede confirmarse. `actualizarVencimiento()` persiste `VENCIDA` cuando una operación procesa la reserva. El equipo informó que verificó manualmente la liberación de capacidad después del vencimiento. **No se incorpora scheduler en Entrega 1**, porque la liberación de capacidad depende del filtro temporal y no de actualizar periódicamente el estado. Las reservas `CONFIRMADA` continúan ocupando sus lugares hasta una devolución autorizada o coordinación de cancelación.

## US05 – Confirmar una reserva después del pago

**Como cliente**, quiero que mi reserva quede confirmada cuando se apruebe el pago, para asegurar los lugares que compré.

**Criterios de aceptación:**

- El microservicio de Compras y Pagos debe informar la aprobación del pago.
- Solo se puede confirmar una reserva activa y vigente.
- El estado de la reserva debe cambiar a CONFIRMADA.
- Los lugares deben dejar de estar reservados temporalmente y pasar a considerarse vendidos.
- Se debe permitir que el microservicio Entradas emita los tickets correspondientes.
- Una confirmación repetida no debe generar ventas o confirmaciones duplicadas.

**Implementación actual:** `POST /reservas/{id}/confirmar`, `ReservaServiceImpl.confirmarReserva`.

**Cobertura: ENDPOINT IMPLEMENTADO; INTEGRACIÓN PENDIENTE DE PRUEBA INTEGRADA.** `POST /reservas/{id}/confirmar` confirma una reserva `ACTIVA` y vigente, y admite reintentos idempotentes. El cliente de Compras para invocar la confirmación está desarrollado. La coordinación completa pago aprobado → confirmación → emisión de entradas debe comprobarse con los microservicios reales funcionando conjuntamente.

## US06 – Liberar una reserva por pago rechazado

**Como cliente**, quiero que los lugares reservados se liberen cuando mi pago sea rechazado, para que vuelvan a estar disponibles para otros compradores.

**Criterios de aceptación:**

- El microservicio de Compras y Pagos debe informar el rechazo del pago.
- La reserva debe cambiar a estado LIBERADA o CANCELADA, según la nomenclatura definida.
- Los lugares deben volver a estar disponibles.
- No se deben emitir entradas asociadas a esa reserva.
- La liberación debe realizarse una sola vez, incluso ante solicitudes repetidas.

**Implementación actual:** `POST /reservas/{id}/liberar`, `ReservaServiceImpl.liberarReserva`.

**Cobertura: ENDPOINT IMPLEMENTADO; INTEGRACIÓN PENDIENTE DE PRUEBA INTEGRADA.** `POST /reservas/{id}/liberar` libera una reserva temporal, actualiza la disponibilidad y admite reintentos. El cliente de Compras para invocar la liberación está desarrollado. Falta comprobar el flujo completo de pago rechazado y la coordinación para que no se emitan entradas.

## US07 – Liberar lugares por devolución o cancelación del evento

**Como sistema**, quiero liberar los lugares correspondientes a entradas devueltas o eventos cancelados, para mantener actualizada la disponibilidad.

**Criterios de aceptación:**

- Cuando se aprueba una devolución, el microservicio correspondiente debe informar a Reservas.
- El asiento o lugar devuelto debe liberarse si el evento continúa habilitado para la venta.
- No se debe liberar un lugar por una solicitud de devolución rechazada.
- Si el evento se cancela, no deben permitirse nuevas reservas.
- Las reservas temporales pendientes de un evento cancelado deben invalidarse.
- Los lugares de un evento cancelado no deben volver a ofrecerse para la venta.
- La actualización de disponibilidad no debe realizarse dos veces ante solicitudes repetidas.

**Implementación actual:** `POST /reservas/{id}/devolver-entrada`, `POST /reservas/evento/{eventoId}/cancelar`, `EntradaLiberada`, `BloqueoEvento`.

**Cobertura: ENDPOINTS IMPLEMENTADOS; INTEGRACIÓN PENDIENTE DE PRUEBA INTEGRADA.** Reservas expone la liberación individual de una entrada y la cancelación de reservas temporales de un evento. El endpoint de devolución registra `entradaId` para evitar una segunda liberación, pero no verifica por sí solo titularidad, aprobación ni plazo de 72 horas: esas validaciones corresponden a los servicios responsables. La coordinación de entradas confirmadas y reintegros ante cancelación debe probarse con Entradas, Compras y Eventos funcionando conjuntamente.

---

## Trazabilidad resumida

| Historia | Endpoint principal | Evidencia local | Estado |
| US01 | `GET /disponibilidad` | Consulta de sector numerado/no numerado, ocupación y evento cancelado | **Parcial:** faltan ocupados explícitos |
| US02 | `POST /reservas` | Creación, 10 minutos, validaciones y límite de entradas | Implementada localmente |
| US03 | `POST /reservas` | Test MySQL 10 solicitudes; idempotencia secuencial/simultánea | Implementada; probar varias instancias |
| US04 | `GET /disponibilidad`, `GET /reservas/{id}`, confirmar | Vencimiento lógico y liberación de capacidad verificados manualmente | Implementada con vencimiento lógico; sin scheduler |
| US05 | `POST /reservas/{id}/confirmar` | Confirmación y reintento; cliente de Compras disponible | Endpoint y cliente implementados; falta prueba integrada de extremo a extremo |
| US06 | `POST /reservas/{id}/liberar` | Liberación y reintento; cliente de Compras disponible | Endpoint y cliente implementados; falta prueba integrada de extremo a extremo |
| US07 | `POST /reservas/{id}/devolver-entrada`; `POST /reservas/evento/{id}/cancelar` | Devolución idempotente, cancelación y bloqueo de ventas | Endpoints implementados; coordinación con Entradas/Compras/Eventos pendiente de prueba integrada |

## Acciones necesarias para cerrar criterios pendientes

1. **US01:** incluir `asientosOcupados` en la respuesta de disponibilidad de sectores numerados y añadir pruebas y ejemplos Swagger/OpenAPI; no basta con devolver únicamente `asientosDisponibles`.
2. **US04:** no se considera necesario incorporar un scheduler en Entrega 1. El vencimiento lógico excluye los registros vencidos del cálculo de ocupación y libera capacidad; el estado persistido se actualiza a `VENCIDA` cuando una operación procesa la reserva.
3. **US03:** ejecutar prueba de reserva concurrente con **más de una instancia** conectada a la misma base MySQL.
4. **US05–US07:** ejecutar y registrar pruebas integradas de extremo a extremo con Compras, Entradas y Eventos, incluyendo aprobación/rechazo, devolución y compensaciones.
5. **Seguridad transversal:** restringir a servicios autorizados los endpoints internos de cambios de estado y devoluciones.

**Documentación complementaria:** [arquitectura](02-ARQUITECTURA-Y-DIAGRAMAS-RESERVAS.md) · [modelo de datos](03-MODELO-DE-DATOS-RESERVAS.md) · [testing](05-TESTING-RESERVAS.md) · [decisiones y limitaciones](06-DECISIONES-Y-LIMITACIONES-RESERVAS.md) · [contrato OpenAPI](openapi.yaml).