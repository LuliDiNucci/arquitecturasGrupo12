# Funcionalidades e historias de usuario — TicketYa

## Objetivo

TicketYa permite organizar eventos, vender entradas digitales y administrar el proceso completo de compra, desde la consulta de disponibilidad hasta la validación de acceso.

## Actores

- **Cliente:** consulta eventos, compra entradas y solicita devoluciones.
- **Organizador:** administra sus eventos y consulta reportes.
- **Administrador:** gestiona usuarios, eventos y operaciones administrativas.
- **Personal de acceso:** valida entradas mediante códigos QR.

## Funcionalidades principales

| Área | Funcionalidades |
|---|---|
| Usuarios | Registro, autenticación JWT y roles |
| Eventos y Lugares | Eventos, sectores, asientos, precios y búsquedas |
| Reservas | Disponibilidad, reservas temporales y control de concurrencia |
| Compras y Pagos | Compras, pagos simulados, confirmaciones y reintegros |
| Entradas | Emisión, QR, validación, devolución e invalidación |
| Fila Virtual | Orden de acceso a eventos de alta demanda |
| Recomendaciones | Preferencias, artistas favoritos e historial de compras |
| Notificaciones | Correos por compras y novedades de eventos |

## Historias de usuario generales

### US-01 — Explorar eventos

**Como** cliente, **quiero** buscar eventos y consultar sus sectores **para** elegir un espectáculo.

**Criterios de aceptación:** se muestran los eventos disponibles para consulta pública, con información de fecha, lugar, participantes y precios.

### US-02 — Comprar entradas

**Como** cliente, **quiero** reservar lugares y completar una compra **para** obtener entradas digitales.

**Criterios de aceptación:** la reserva tiene una vigencia de diez minutos, se controla la disponibilidad y la compra se confirma después del pago aprobado y las operaciones correspondientes.

### US-03 — Acceder a eventos de alta demanda

**Como** cliente, **quiero** ingresar a una fila virtual **para** acceder ordenadamente al proceso de compra.

**Criterios de aceptación:** cuando el evento lo requiere, la habilitación del turno se verifica antes de permitir las operaciones correspondientes.

### US-04 — Utilizar entradas digitales

**Como** cliente, **quiero** consultar mis entradas y presentar su QR **para** ingresar al evento.

**Criterios de aceptación:** la entrada debe ser válida, corresponder al evento y no haber sido utilizada previamente. La validación requiere personal autorizado para ese evento.

### US-05 — Gestionar cambios y devoluciones

**Como** cliente, **quiero** solicitar devoluciones y recibir avisos de cambios **para** conocer el estado de mis entradas y compras.

**Criterios de aceptación:** las devoluciones siguen las reglas de negocio; los reintegros e invalidaciones se coordinan entre los servicios responsables.

### US-06 — Administrar eventos

**Como** organizador, **quiero** gestionar mis eventos y consultar sus resultados **para** administrar los espectáculos.

**Criterios de aceptación:** las operaciones respetan el estado del evento y la titularidad del organizador.

### US-07 — Recibir recomendaciones

**Como** cliente, **quiero** recibir sugerencias según mis preferencias **para** descubrir eventos de interés.

**Criterios de aceptación:** se consideran eventos publicados y futuros, combinando intereses y sugerencias obtenidas del historial de compras.

Los criterios específicos y las validaciones de cada funcionalidad se documentan dentro de los microservicios correspondientes.