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

**Cobertura: IMPLEMENTADA LOCALMENTE.** Reserva `ACTIVA` con vencimiento de 10 minutos, validaciones de pertenencia de sector/asientos, precio y disponibilidad, habilitación previa del evento y límite de entradas configurable. La ocupación se registra en MySQL. **Pendiente:** prueba de extremo a extremo con Eventos real y Compras.

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

**Cobertura: PARCIAL.** Los lugares de una reserva `ACTIVA` vencida **se excluyen automáticamente del conteo de ocupación**, por lo que vuelven a ofrecerse; una reserva vencida no puede confirmarse. `actualizarVencimiento()` modifica el estado a `VENCIDA` cuando una operación procesa la reserva. **No hay scheduler ni otro proceso que actualice automáticamente el estado de todas las reservas vencidas en MySQL**. Si el criterio exige cambio persistido al vencer, esta parte falta implementar. Las reservas `CONFIRMADA` continúan ocupando sus lugares hasta una devolución autorizada o coordinación de cancelación.

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

**Cobertura: IMPLEMENTADA LOCALMENTE; INTEGRACIÓN PENDIENTE.** Solo confirma reservas vigentes en `ACTIVA`; cambia a `CONFIRMADA`; los lugares siguen ocupados sin vencimiento lógico. Confirmar nuevamente una reserva confirmada no crea otra venta. **Falta** conectar con pagos reales/simulados y comprobar la emisión de entradas desde el microservicio correspondiente.

## US06 – Liberar una reserva por pago rechazado

**Como cliente**, quiero que los lugares reservados se liberen cuando mi pago sea rechazado, para que vuelvan a estar disponibles para otros compradores.

**Criterios de aceptación:**

- El microservicio de Compras y Pagos debe informar el rechazo del pago.
- La reserva debe cambiar a estado LIBERADA o CANCELADA, según la nomenclatura definida.
- Los lugares deben volver a estar disponibles.
- No se deben emitir entradas asociadas a esa reserva.
- La liberación debe realizarse una sola vez, incluso ante solicitudes repetidas.

**Implementación actual:** `POST /reservas/{id}/liberar`, `ReservaServiceImpl.liberarReserva`.

**Cobertura: IMPLEMENTADA LOCALMENTE; INTEGRACIÓN PENDIENTE.** Cambia una reserva activa a `LIBERADA`, libera capacidad y admite reintentos sin repetir el cambio. Rechaza liberar una reserva confirmada por esta vía. **Falta** verificar que Compras avise el rechazo y que Entradas no emita tickets para dicha reserva.

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

**Cobertura: PARCIAL / INTEGRACIÓN PENDIENTE.** Se puede liberar **una** entrada de una reserva confirmada y registrar `entradaId` para evitar una segunda liberación. Al cancelar un evento se bloquean nuevas ventas, se invalidan las reservas activas y la API de disponibilidad rechaza consultas de ese evento. **Falta** garantizar por integración que la devolución esté aprobada y que el solicitante tenga derecho a pedirla; no basta con recibir un `entradaId` arbitrario. Entradas/Compras deben validar titularidad, plazo de 72 horas, aprobación y reintegro. Las entradas de reservas confirmadas en un evento cancelado requieren coordinación adicional.

---

## Trazabilidad resumida

| Historia | Endpoint principal | Evidencia local | Estado |
|---|---|---|---|
| US01 | `GET /disponibilidad` | Consulta de sector numerado/no numerado, ocupación y evento cancelado | **Parcial:** faltan ocupados explícitos |
| US02 | `POST /reservas` | Creación, 10 minutos, validaciones y límite de entradas | Implementada localmente |
| US03 | `POST /reservas` | Test MySQL 10 solicitudes; idempotencia secuencial/simultánea | Implementada; probar varias instancias |
| US04 | `GET /disponibilidad`, `GET /reservas/{id}`, confirmar | Vencimiento lógico; confirmación rechazada | **Parcial:** falta estado automático persistido |
| US05 | `POST /reservas/{id}/confirmar` | Confirmación y reintento | Integración con Compras/Entradas pendiente |
| US06 | `POST /reservas/{id}/liberar` | Liberación y reintento | Integración con Compras/Entradas pendiente |
| US07 | `POST /reservas/{id}/devolver-entrada`; `POST /reservas/evento/{id}/cancelar` | Devolución idempotente, cancelación y bloqueo de ventas | Integración de aprobación/reintegro pendiente |

## Acciones necesarias para cerrar criterios pendientes

1. **US01:** incluir `asientosOcupados` en la respuesta de disponibilidad de sectores numerados y añadir pruebas y ejemplos Swagger/OpenAPI; no basta con devolver únicamente `asientosDisponibles`.
2. **US04:** si se exige cambio persistido automático, incorporar un mecanismo periódico o equivalente para pasar reservas activas vencidas a `VENCIDA`, sin tocar las confirmadas. Mantener el filtro por fecha existente para garantizar liberación oportuna.
3. **US03:** ejecutar prueba de reserva concurrente con **más de una instancia** conectada a la misma base MySQL.
4. **US05–US07:** probar de extremo a extremo los avisos desde Compras, Entradas y Eventos, los casos de aprobación/rechazo y las compensaciones necesarias.
5. **Seguridad transversal:** restringir a servicios autorizados los endpoints internos de cambios de estado y devoluciones.

**Documentación complementaria:** [arquitectura](02-ARQUITECTURA-Y-DIAGRAMAS.md) · [modelo de datos](03-MODELO-DE-DATOS.md) · [testing](05-TESTING.md) · [decisiones y limitaciones](06-DECISIONES-Y-LIMITACIONES.md) · [contrato OpenAPI](openapi.yaml).
