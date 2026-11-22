# Funcionalidades y user stories — Reservas

## 1. Objetivo

El microservicio Reservas administra la disponibilidad y la ocupación temporal o definitiva de los lugares de un evento.

Permite crear, consultar, confirmar y liberar reservas, controlar solicitudes concurrentes y gestionar lugares devueltos. También participa en la integración con Fila Virtual para eventos de alta demanda.

## 2. Funcionalidades e historias de usuario

### US01 — Consultar disponibilidad

**Como cliente**, quiero conocer los lugares disponibles de un evento antes de realizar una reserva.

**Criterios de aceptación:**
- Se valida la existencia del evento y del sector, y su correspondencia.
- En sectores numerados se informan `asientosDisponibles` y `asientosOcupados`.
- En sectores no numerados se informa la cantidad de lugares disponibles.
- Las reservas activas vigentes y confirmadas ocupan lugares.
- Las reservas vencidas, liberadas o canceladas no bloquean disponibilidad.

### US02 — Crear una reserva temporal

**Como cliente**, quiero reservar lugares temporalmente mientras realizo el pago.

**Criterios de aceptación:**
- El evento debe estar habilitado para reservar.
- Se validan sector, cantidad y asientos solicitados.
- La reserva se crea en estado `ACTIVA` y tiene una vigencia de 10 minutos.
- Se permite reservar un máximo de 4 entradas por cliente y evento, considerando las reservas activas vigentes y confirmadas.
- Se utiliza una clave de idempotencia para evitar reservas duplicadas.

### US03 — Controlar la concurrencia

**Como cliente**, quiero que otros usuarios no puedan reservar simultáneamente los mismos lugares.

**Criterios de aceptación:**
- Un asiento no puede ser ocupado por dos reservas vigentes del mismo evento.
- En sectores no numerados no se puede superar la capacidad.
- Se utilizan transacciones y bloqueos pesimistas sobre el evento.
- Las solicitudes incompatibles se rechazan sin generar sobreventa.

### US04 — Gestionar vencimientos

**Como sistema**, quiero liberar la disponibilidad asociada a reservas cuyo plazo haya terminado.

**Criterios de aceptación:**
- Las reservas activas vencen después de 10 minutos.
- Una reserva vencida deja de ocupar lugares.
- No se puede confirmar una reserva vencida.
- Las reservas confirmadas no vencen por este mecanismo.

### US05 — Confirmar reservas

**Como cliente**, quiero que mi reserva se confirme cuando se aprueba el pago.

**Criterios de aceptación:**
- Compras solicita la confirmación mediante una llamada interna autenticada.
- Solo se confirma una reserva activa y vigente.
- La reserva pasa a `CONFIRMADA`.
- Los lugares confirmados permanecen ocupados.
- Una confirmación repetida no duplica sus efectos.

### US06 — Liberar reservas

**Como sistema**, quiero liberar una reserva cuando se rechaza el pago.

**Criterios de aceptación:**
- Compras solicita la liberación mediante una operación interna.
- La reserva pasa a `LIBERADA`.
- Los lugares vuelven a estar disponibles.
- La liberación es idempotente.
- No puede utilizarse esta operación para liberar una reserva confirmada.

### US07 — Gestionar devoluciones y cancelaciones

**Como sistema**, quiero mantener la disponibilidad consistente cuando se devuelven entradas o cancelan eventos.

**Criterios de aceptación:**
- Se permite liberar individualmente el lugar de una entrada cuya devolución fue aprobada.
- Una misma entrada no puede liberar dos veces su lugar.
- La cancelación de un evento impide nuevas reservas y cancela las reservas temporales activas.
- Las reservas confirmadas se coordinan con los servicios Compras y Entradas.

### US08 — Integración con Fila Virtual

**Como cliente de un evento de alta demanda**, quiero reservar únicamente cuando tenga un turno habilitado.

**Criterios de aceptación:**
- Los eventos normales no requieren turno.
- En eventos con `altaDemanda=true` se verifica el turno del cliente mediante Fila Virtual.
- El turno debe pertenecer al cliente y al evento, estar habilitado y no haber vencido.
- Una reserva exitosa registra una operación persistente para finalizar el turno utilizado.
- Si falla la comunicación al finalizar el turno, el sistema reintenta la operación.
- Si Fila Virtual no está disponible para validar el turno, la reserva se rechaza.

## 3. Seguridad

El servicio utiliza JWT para autenticar usuarios y llamadas internas.

- Los clientes solo pueden crear reservas propias.
- Los clientes solo pueden consultar sus propias reservas.
- Compras puede confirmar y liberar reservas.
- Compras y Entradas pueden comunicar devoluciones.
- Eventos y Lugares puede habilitar o cancelar reservas de un evento.

La consulta de disponibilidad es pública.