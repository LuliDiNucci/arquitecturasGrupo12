# 05 — Contratos de integración de TicketYa

## 1. Objetivo y alcance

Este documento reúne los principales contratos REST utilizados por los microservicios de TicketYa y describe cómo se comunican entre sí.

Se distinguen tres elementos:

- **Endpoints propios:** operaciones expuestas por los controladores de cada microservicio.
- **Llamadas entre microservicios:** solicitudes HTTP realizadas mediante clientes Feign o RestClient.
- **Estado de integración:** diferencia entre operaciones implementadas y comunicaciones que todavía requieren validación conjunta.

Las rutas se expresan sin incluir el host ni el puerto. Las direcciones base se configuran según el entorno de ejecución.

Los datos se intercambian principalmente en formato JSON. Los DTO permiten comunicar información entre servicios sin compartir directamente sus entidades internas.

## 2. Endpoints propios de cada microservicio

### 2.1. Eventos y Lugares

Base local: `http://localhost:8081`

Es responsable de administrar lugares, sectores, asientos, organizadores, participantes, eventos y precios.

| Método | Ruta | Operación |
|---|---|---|
| POST | `/lugares` | Crear un lugar. |
| GET | `/lugares` | Listar lugares. |
| GET | `/lugares/{id}` | Consultar un lugar. |
| POST | `/lugares/{id}/sectores` | Crear sectores para un lugar. |
| GET | `/lugares/{id}/sectores` | Consultar sectores de un lugar. |
| POST | `/sectores/{id}/asientos` | Crear asientos de un sector. |
| GET | `/sectores/{id}/asientos` | Consultar asientos de un sector. |
| POST | `/organizadores` | Registrar organizadores. |
| GET | `/organizadores` | Listar organizadores. |
| POST | `/participantes` | Registrar participantes. |
| GET | `/participantes` | Listar participantes. |
| POST | `/eventos` | Crear un evento. |
| GET | `/eventos` | Listar eventos con propósito operativo. |
| GET | `/eventos/publicados` | Consultar eventos publicados. |
| GET | `/eventos/buscar` | Buscar eventos mediante filtros. |
| GET | `/eventos/{id}` | Consultar un evento. |
| PUT | `/eventos/{id}` | Modificar un evento. |
| PUT | `/eventos/{id}/precios` | Configurar precios por sector. |
| PATCH | `/eventos/{id}/publicar` | Publicar un evento. |
| PATCH | `/eventos/{id}/suspender` | Suspender un evento. |
| PATCH | `/eventos/{id}/cancelar` | Cancelar un evento. |
| PATCH | `/eventos/{id}/agotar` | Marcar un evento como agotado. |
| PATCH | `/eventos/{id}/reabrir` | Reabrir un evento. |
| PATCH | `/eventos/{id}/finalizar` | Finalizar un evento. |
| GET | `/eventos/{id}/sectores` | Consultar sectores, asientos y precios del evento. |
| GET | `/eventos/{id}/habilitado` | Consultar si el evento está habilitado para reservar. |
| GET | `/eventos/{id}/notificaciones-cancelacion` | Consultar el estado de las notificaciones de cancelación. |

La búsqueda pública se realiza mediante `/eventos/buscar`. Por defecto, contempla eventos en estado `PUBLICADO` y `AGOTADO`. El listado general `/eventos` tiene un propósito operativo y no equivale a una búsqueda pública.

La búsqueda admite filtros como tipo de evento, organizador, participante, ciudad, fechas, texto y ordenamiento.

### 2.2. Reservas

Es responsable de consultar disponibilidad, crear reservas temporales, confirmar o liberar reservas y administrar la ocupación de lugares.

| Método | Ruta | Operación |
|---|---|---|
| GET | `/disponibilidad` | Consultar disponibilidad de un evento y sector. |
| POST | `/reservas` | Crear una reserva temporal. |
| GET | `/reservas/{id}` | Consultar una reserva. |
| POST | `/reservas/{id}/confirmar` | Confirmar una reserva. |
| POST | `/reservas/{id}/liberar` | Liberar una reserva. |
| POST | `/reservas/{id}/devolver-entrada` | Liberar el lugar asociado a una entrada devuelta. |
| POST | `/reservas/evento/{eventoId}/cancelar` | Cancelar las reservas de un evento. |
| POST | `/reservas/evento/{eventoId}/habilitar` | Habilitar las reservas de un evento. |

Las reservas temporales tienen una vigencia limitada. La implementación utiliza mecanismos de bloqueo para evitar que dos solicitudes ocupen simultáneamente el mismo lugar.

La habilitación y cancelación de reservas por evento son operaciones utilizadas en la integración con Eventos y Lugares.

### 2.3. Compras

Base local predeterminada: `http://localhost:8082`

Es responsable de iniciar compras, procesar pagos, coordinar confirmaciones y registrar reintegros.

| Método | Ruta | Operación |
|---|---|---|
| POST | `/compras` | Iniciar una compra. |
| GET | `/compras/{id}` | Consultar una compra. |
| POST | `/compras/{id}/confirmar` | Confirmar una compra. |
| GET | `/compras/{id}/confirmacion` | Consultar el estado de confirmación. |
| POST | `/compras/{id}/pagar` | Procesar el pago. |
| POST | `/compras/{id}/reintentar-liberacion` | Reintentar la liberación de una reserva. |
| POST | `/compras/reintegros` | Registrar un reintegro solicitado por Entradas. |
| GET | `/compras/{id}/reintegros` | Consultar los reintegros de una compra. |
| POST | `/compras/evento/{eventoId}/cancelar` | Procesar compras afectadas por la cancelación de un evento. |
| POST | `/compras/{id}/compensar` | Ejecutar o reintentar la compensación de una compra. |

El procesamiento de pagos utiliza una pasarela simulada. La compra conserva los precios históricos de los artículos y contempla mecanismos de idempotencia para evitar cobros duplicados.

La compra solo alcanza el estado `CONFIRMADA` cuando se completan correctamente la confirmación de la reserva y la emisión de las entradas.

### 2.4. Entradas

Base local predeterminada: `http://localhost:8083`

Es responsable de emitir entradas, generar códigos QR, consultar tickets y administrar solicitudes de devolución.

| Método | Ruta | Operación |
|---|---|---|
| POST | `/entradas/emitir` | Emitir entradas para una compra. |
| GET | `/entradas/{id}` | Consultar una entrada individual. |
| GET | `/entradas/cliente/{clienteId}` | Consultar las entradas de un cliente. |
| POST | `/entradas/evento/{id}/invalidar` | Invalidar entradas de un evento cancelado. |
| POST | `/devoluciones` | Solicitar la devolución de una entrada. |
| GET | `/devoluciones/pendientes` | Consultar solicitudes pendientes. |
| PATCH | `/devoluciones/{id}/aprobar` | Aprobar una devolución. |
| PATCH | `/devoluciones/{id}/rechazar` | Rechazar una devolución. |

La consulta individual requiere el encabezado `X-Cliente-Id`, que se utiliza para verificar la titularidad de la entrada.

La consulta de entradas por cliente recibe el identificador en la URL, pero actualmente no verifica que quien realiza la solicitud sea efectivamente ese cliente.

La emisión es idempotente: una nueva solicitud para una compra que ya tiene entradas emitidas devuelve las existentes, sin generar duplicados.

### 2.5. API Gateway

El Gateway funciona como punto de entrada y deriva las solicitudes hacia el microservicio correspondiente.

Su configuración de rutas permite centralizar el acceso a los servicios sin trasladar al Gateway las responsabilidades de negocio de cada uno.

La documentación específica del Gateway contiene su configuración de enrutamiento.

## 3. Contratos de comunicación entre microservicios

Esta sección describe las llamadas salientes que realizan los servicios y sus respectivos destinatarios. No deben confundirse con los endpoints propios de los controladores.

### 3.1. Reservas → Eventos y Lugares

Cliente utilizado: `EventoClient` (Feign).

| Método | Ruta | Finalidad |
|---|---|---|
| GET | `/eventos/{id}` | Consultar información y estado del evento. |
| GET | `/sectores/{id}` | Consultar un sector. |
| GET | `/asientos/{id}` | Consultar un asiento. |
| GET | `/sectores/{id}/asientos` | Consultar los asientos de un sector. |
| GET | `/eventos/{eventoId}/sectores/{sectorId}/precio` | Consultar el precio de un sector para un evento. |

Reservas utiliza esta información para validar la existencia del evento, la pertenencia del sector y del asiento, y el precio correspondiente.

La disponibilidad y la ocupación de los lugares permanecen bajo responsabilidad de Reservas.

### 3.2. Compras → Reservas

Cliente utilizado: `ReservaClient` (Feign).

| Método | Ruta | Finalidad |
|---|---|---|
| GET | `/reservas/{id}` | Consultar y validar una reserva. |
| POST | `/reservas/{id}/confirmar` | Confirmar la reserva luego de un pago aprobado. |
| POST | `/reservas/{id}/liberar` | Liberar la reserva ante un pago rechazado. |

Compras necesita verificar que la reserva exista, corresponda al cliente y se encuentre activa y vigente antes de continuar con la operación.

### 3.3. Compras → Eventos y Lugares

Cliente utilizado: `EventoClient` (Feign).

| Método | Ruta | Finalidad |
|---|---|---|
| GET | `/eventos/{id}` | Consultar información y estado del evento. |
| GET | `/eventos/{eventoId}/sectores/{sectorId}/precio` | Obtener el precio del sector para el evento. |

Compras utiliza estos datos para validar el evento y calcular el importe de la operación. Los precios aplicados se conservan como valores históricos en los detalles de la compra.

### 3.4. Compras → Entradas

Cliente utilizado: `EntradaClient` (Feign).

| Método | Ruta | Finalidad |
|---|---|---|
| POST | `/entradas/emitir` | Solicitar la emisión de las entradas. |
| POST | `/entradas/evento/{eventoId}/invalidar` | Solicitar la invalidación de entradas de un evento. |
| GET | `/entradas/cliente/{clienteId}` | Consultar las entradas de un cliente. |
| GET | `/entradas/{id}` | Consultar una entrada individual, enviando `X-Cliente-Id`. |

La emisión se solicita después de la aprobación del pago y de la confirmación de la reserva.

### 3.5. Entradas → Compras

Se utilizan dos mecanismos de comunicación.

| Cliente | Método | Ruta | Finalidad |
|---|---|---|---|
| `CompraClient` (RestClient) | GET | `/compras/{id}/confirmacion` | Consultar el estado de confirmación y verificar el pago. |
| `CompraFeignClient` (Feign) | POST | `/compras/reintegros` | Solicitar el reintegro de una entrada devuelta. |

Para solicitar un reintegro se envían los datos de la entrada y el motivo de la devolución.

Compras valida las condiciones de la operación y procesa el reintegro correspondiente.

### 3.6. Entradas → Reservas

Se utilizan dos mecanismos de comunicación.

| Cliente | Método | Ruta | Finalidad |
|---|---|---|---|
| `ReservaClient` (RestClient) | GET | `/reservas/{id}` | Consultar la reserva asociada. |
| `ReservaFeignClient` (Feign) | POST | `/reservas/{id}/devolver-entrada` | Solicitar la devolución del lugar correspondiente a una entrada. |

La solicitud de devolución incluye el identificador de la entrada y los datos del lugar asociado.

### 3.7. Entradas → Eventos y Lugares

Cliente utilizado: `EventoFeignClient` (Feign).

| Método | Ruta | Finalidad |
|---|---|---|
| GET | `/eventos/{id}` | Consultar el estado y la fecha del evento. |

Esta consulta permite validar que el evento exista y controlar el plazo de anticipación requerido para solicitar una devolución.

### 3.8. Eventos y Lugares → Reservas

Cliente de integración de Eventos y Lugares.

| Método | Ruta | Finalidad |
|---|---|---|
| POST | `/reservas/evento/{id}/cancelar` | Notificar la cancelación del evento. |
| POST | `/reservas/evento/{id}/habilitar` | Habilitar las reservas del evento. |

La cancelación permite liberar las reservas temporales asociadas al evento. La habilitación permite coordinar la disponibilidad para comenzar a recibir reservas.

### 3.9. Eventos y Lugares → Entradas

| Método | Ruta | Finalidad |
|---|---|---|
| POST | `/entradas/evento/{id}/invalidar` | Solicitar la invalidación de las entradas de un evento cancelado. |

Entradas consulta el estado del evento y cambia a `INVALIDADA` las entradas que se encuentren en estado `EMITIDA`.

Esta operación no libera por sí sola los lugares ni procesa reintegros.

### 3.10. Eventos y Lugares → Compras

| Método | Ruta | Finalidad |
|---|---|---|
| POST | `/compras/evento/{id}/cancelar` | Informar la cancelación y solicitar el procesamiento de reintegros. |

La solicitud incluye el encabezado `X-Operacion-Id`, que identifica la operación de cancelación.

Compras verifica el estado del evento, solicita la invalidación de entradas y procesa los reintegros de los artículos pagados utilizando sus precios históricos.

## 4. Encabezados y datos relevantes

### 4.1. `X-Cliente-Id`

Se utiliza en la consulta individual de una entrada:

`GET /entradas/{id}`

Permite que Entradas verifique que la entrada consultada pertenece al cliente indicado.

### 4.2. `X-Operacion-Id`

Se utiliza en la notificación de cancelación enviada a Compras y también se transmite en las llamadas de notificación correspondientes a Reservas y Entradas.

Su finalidad es identificar una misma operación ante posibles reintentos.

Sin embargo, su tratamiento no es uniforme:

- **Reservas:** recibe el encabezado, pero no lo utiliza para deduplicar operaciones.
- **Entradas:** el cliente de Eventos lo envía, pero el endpoint receptor no lo procesa.
- **Compras:** exige y valida el encabezado. La prevención de reintegros duplicados se apoya además en el registro de reintegros por artículo.

Por lo tanto, no debe afirmarse que todos los receptores implementan actualmente idempotencia mediante este encabezado.

### 4.3. DTO e identificadores

Los microservicios intercambian DTO específicos de cada operación. Entre los identificadores utilizados se encuentran:

- `eventoId`
- `sectorId`
- `asientoId`
- `clienteId`
- `reservaId`
- `compraId`
- `entradaId`

Estos identificadores permiten relacionar información entre servicios sin compartir directamente sus entidades persistentes.

## 5. Flujos de integración principales

### 5.1. Compra aprobada

1. El cliente consulta el evento y su disponibilidad.
2. Reservas consulta Eventos y Lugares para obtener la configuración del evento.
3. Reservas verifica la disponibilidad y crea una reserva temporal.
4. Compras consulta y valida la reserva.
5. Compras obtiene los precios correspondientes desde Eventos y Lugares.
6. Compras procesa el pago mediante la pasarela simulada.
7. Si el pago es aprobado, Compras solicita confirmar la reserva.
8. Compras solicita a Entradas la emisión de los tickets.
9. Entradas valida la compra, el pago y la reserva, y genera los códigos QR.
10. Compras confirma la compra cuando verifica que se emitieron todas las entradas esperadas.

### 5.2. Pago rechazado

1. Compras recibe el resultado rechazado de la pasarela simulada.
2. Compras solicita liberar la reserva.
3. Reservas libera los lugares correspondientes.
4. Compras registra el resultado de la operación como rechazada una vez completada la liberación.

No se emiten entradas.

### 5.3. Devolución de una entrada

1. El cliente solicita una devolución en Entradas.
2. Entradas valida la entrada, su estado y el plazo de anticipación.
3. La solicitud queda pendiente de resolución.
4. Un administrador aprueba o rechaza la solicitud.
5. Si se aprueba, Entradas invalida la entrada.
6. Entradas solicita a Reservas devolver el lugar.
7. Entradas solicita a Compras procesar el reintegro.

Si se rechaza, la entrada permanece emitida y no se realizan las operaciones externas de devolución.

### 5.4. Cancelación de un evento

1. Eventos y Lugares cambia el estado del evento a `CANCELADO`.
2. Registra una notificación pendiente.
3. El componente `NotificadorCancelaciones` procesa los avisos mediante un scheduler con intervalo de 30 segundos.
4. Se notifica a Reservas para cancelar las reservas del evento.
5. Se notifica a Entradas para invalidar las entradas emitidas.
6. Una vez notificadas correctamente Reservas y Entradas, se notifica a Compras para procesar los reintegros, siempre que las integraciones correspondientes estén habilitadas.
7. Si una notificación falla, permanece pendiente para un nuevo intento.

El resultado de las notificaciones puede consultarse mediante:

`GET /eventos/{id}/notificaciones-cancelacion`

## 6. Configuración de las integraciones

Las direcciones base de los microservicios se configuran mediante propiedades y variables de entorno.

En la configuración predeterminada de Eventos y Lugares se utilizan:

| Variable | Valor predeterminado |
|---|---|
| `RESERVAS_URL` | `http://localhost:8080` |
| `ENTRADAS_URL` | `http://localhost:8083` |
| `COMPRAS_URL` | `http://localhost:8082` |

Las integraciones de cancelación contemplan las siguientes opciones:

| Variable | Valor predeterminado |
|---|---|
| `INTEGRACIONES_HABILITADAS` | `true` |
| `ENTRADAS_HABILITADAS` | `true` |
| `COMPRAS_HABILITADAS` | `true` |

Los valores pueden modificarse según el entorno de ejecución.

Los puertos indicados corresponden a la configuración predeterminada documentada y no deben considerarse direcciones fijas para todos los despliegues.

## 7. Estado y limitaciones de las integraciones

La existencia de un endpoint o de un cliente HTTP no implica que el flujo completo entre los servicios haya sido probado.

Se identifican las siguientes consideraciones:

- **Compras:** cuenta con contratos propios y clientes para comunicarse con Reservas, Eventos y Entradas. Algunas pruebas de integración de extremo a extremo permanecen pendientes.
- **Reservas:** cuenta con el cliente Feign para consultar Eventos y Lugares y con endpoints para habilitar y cancelar reservas. La integración conjunta requiere validación.
- **Entradas:** cuenta con clientes para consultar Compras, Reservas y Eventos, y para solicitar reintegros y devoluciones de lugares. La invalidación por cancelación no coordina actualmente la liberación de reservas ni los reintegros desde su propio flujo.
- **Eventos y Lugares:** implementa el mecanismo de notificaciones pendientes y reintentos. El tratamiento de `X-Operacion-Id` difiere según el receptor.
- **Gateway:** debe verificarse que las rutas expuestas correspondan a los controladores actuales y que las operaciones internas tengan los controles de acceso adecuados.

La comunicación entre microservicios no constituye una única transacción distribuida. Por ese motivo, los procesos que involucran varios servicios deben contemplar fallos parciales, reintentos y operaciones de compensación.

## 8. Documentación específica relacionada

Para consultar los contratos completos, los ejemplos de request y response y los DTO de cada servicio:

- [Contratos REST de Eventos y Lugares](../msvc-eventosylugares/docs/04-CONTRATOS-API-REST.md)
- [Contrato de comunicación de Reservas](../msvc-reservas/docs/contrato-eventos.md)
- [Contratos REST de Compras](../msvc-compras/docs/04-CONTRATOS-REST.md)
- [Contratos REST de Entradas](../msvc-entradas/docs/04-CONTRATOS-REST.md)
- [README del Gateway](../msvc-gateway/README.md)