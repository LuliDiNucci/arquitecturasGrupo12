# 01 — Funcionalidades e historias de usuario

## Objetivo

El microservicio **Compras y Pagos** administra las compras de entradas, el procesamiento de pagos, su confirmación y los reintegros. Se integra con otros microservicios para verificar reservas, consultar precios, emitir entradas y comunicar las operaciones realizadas.

## Historias de usuario

### US01 — Iniciar compra

**Como cliente**, quiero iniciar una compra a partir de mi reserva temporal para adquirir las entradas seleccionadas.

**Criterios de aceptación:**
- La reserva debe existir, pertenecer al cliente, estar activa y no haber vencido.
- Se consultan los precios de los sectores y se calcula el importe total.
- Se registran los detalles y los ítems individuales de compra.
- La compra se crea en estado `PENDIENTE`.
- Una reserva no puede generar múltiples compras.

### US02 — Consultar compra

**Como cliente**, quiero consultar el estado y detalle de mis compras.

**Criterios de aceptación:**
- Se muestran el evento, la reserva, los sectores, las cantidades, los importes y el estado.
- Se identifican las entradas emitidas cuando corresponde.
- El cliente solamente puede consultar sus propias compras. El administrador dispone de acceso autorizado.

### US03 — Procesar pago

**Como cliente**, quiero pagar una compra pendiente para confirmar la adquisición de mis entradas.

**Criterios de aceptación:**
- La compra y la reserva deben encontrarse en condiciones válidas.
- El pago se procesa mediante una pasarela simulada.
- Se registra el medio de pago, importe, fecha, resultado y referencia de operación.
- Un pago aprobado permite continuar con la confirmación.
- Un pago rechazado inicia la liberación de la reserva.

### US04 — Confirmar compra y emitir entradas

**Como cliente**, quiero recibir mis entradas cuando se apruebe el pago.

**Criterios de aceptación:**
- Debe existir un pago aprobado.
- Se confirma la reserva en `msvc-reservas`.
- Se solicita a `msvc-entradas` una entrada por cada ítem comprado.
- La compra pasa a `CONFIRMADA` únicamente cuando todas las entradas fueron asociadas correctamente.
- Los reintentos no generan entradas duplicadas.

### US05 — Evitar cobros duplicados

**Como cliente**, quiero que los reintentos de pago no produzcan nuevos cobros.

**Criterios de aceptación:**
- Cada solicitud incluye una clave de idempotencia.
- Repetir una operación válida devuelve el resultado registrado.
- Reutilizar una clave con datos diferentes genera un conflicto.
- Se utilizan bloqueos transaccionales y restricciones de unicidad para controlar solicitudes concurrentes.

### US06 — Recuperar operaciones interrumpidas

**Como sistema**, quiero recuperar operaciones incompletas por fallas de comunicación para mantener la consistencia de las compras.

**Criterios de aceptación:**
- Se registran estados intermedios cuando la confirmación, liberación o compensación no finaliza.
- Se permiten reintentos de confirmación y liberación.
- Si un pago aprobado no puede convertirse en una compra válida, se contempla su compensación.
- No se repiten cobros, emisiones ni reintegros ya procesados.

### US07 — Reintegrar una entrada devuelta

**Como cliente**, quiero recuperar el importe de una entrada cuya devolución fue aprobada.

**Criterios de aceptación:**
- Se verifica la devolución aprobada en `msvc-entradas`.
- El importe corresponde al precio histórico registrado en la compra.
- Cada entrada puede generar un único reintegro.
- Una compra puede quedar en estado `REINTEGRO_PARCIAL` o `REINTEGRADA`.

### US08 — Reintegrar compras por cancelación de evento

**Como administrador**, quiero que se reintegren las compras de un evento cancelado.

**Criterios de aceptación:**
- Se recibe la cancelación desde Eventos y Lugares.
- Se comprueba la invalidación de las entradas correspondientes.
- Se procesan los reintegros por ítem, evitando duplicaciones.
- Los reintentos permiten completar operaciones interrumpidas.

### US09 — Notificar una compra confirmada

**Como cliente**, quiero recibir un aviso cuando mi compra quede confirmada.

**Criterios de aceptación:**
- Se registra un aviso asociado a la compra confirmada.
- Se obtiene el correo electrónico del cliente desde `msvc-usuarios`.
- El aviso se envía mediante `msvc-notificaciones`.
- Si el envío falla, se conserva el aviso y se reintenta posteriormente.

### US10 — Consultar reportes de ventas

**Como organizador**, quiero conocer las ventas y la recaudación de mis eventos.

**Criterios de aceptación:**
- Se proporcionan importes pagados, reintegrados y recaudación neta.
- Se calculan ventas y devoluciones por sector.
- Los datos se ponen a disposición de Eventos y Lugares mediante una API interna.

### US11 — Consultar recomendaciones por historial

**Como cliente**, quiero recibir sugerencias de eventos relacionadas con mis compras.

**Criterios de aceptación:**
- Se consideran compras confirmadas.
- Se identifican eventos adquiridos por otros clientes con historial de compras relacionado.
- El acceso al historial está protegido mediante autenticación y autorización.