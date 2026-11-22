# 02 — Arquitectura y flujos

## Arquitectura

`msvc-entradas` está desarrollado con Java 21, Spring Boot, Spring Data JPA, Spring Security y OpenFeign. Utiliza MySQL como base de datos independiente.

Se ejecuta en el puerto **8083** y es accesible mediante el API Gateway de TicketYa.

La implementación se organiza en:

- **Controllers:** endpoints HTTP.
- **Services y ServiceImpl:** interfaces y lógica de negocio.
- **Repositories:** persistencia mediante JPA.
- **Entities:** modelo de datos.
- **DTOs:** intercambio de información.
- **Clients:** comunicación con otros microservicios.
- **Security:** autenticación JWT y autorización.
- **QR Service:** generación de imágenes QR.

## Integración con otros microservicios

| Servicio | Responsabilidad |
|---|---|
| Compras y Pagos | Verificar pagos aprobados y solicitar reintegros |
| Reservas | Verificar reservas confirmadas y liberar lugares devueltos |
| Eventos y Lugares | Consultar fecha y estado de eventos |
| Usuarios | Proporcionar la identidad y los roles mediante JWT |
| API Gateway | Centralizar el acceso al sistema |

Las comunicaciones se implementan mediante clientes HTTP y OpenFeign.

## Emisión de entradas

1. Compras solicita emitir las entradas de una compra.
2. Entradas verifica si ya existen registros para evitar duplicaciones.
3. Consulta que la compra tenga un pago aprobado.
4. Comprueba que la reserva esté confirmada y corresponda a la compra.
5. Genera una entrada por cada lugar adquirido.
6. Registra un código QR único y el estado `EMITIDA`.
7. Devuelve las entradas generadas.

Los datos de cada entrada se almacenan en MySQL. La imagen del QR se genera a partir del código único al preparar la respuesta.

## Validación de acceso

El administrador asigna personal autorizado a cada evento.

Cuando una persona del equipo de acceso escanea un QR, el servicio verifica:

- El rol y la asignación del usuario.
- La existencia del código QR.
- La correspondencia entre la entrada y el evento.
- Que la entrada se encuentre `EMITIDA`.

Si todas las condiciones se cumplen, se registra el ingreso y el estado cambia a `UTILIZADA`.

La búsqueda utiliza un bloqueo `PESSIMISTIC_WRITE` dentro de una transacción para evitar que dos escaneos simultáneos habiliten el mismo ingreso.

## Devoluciones

El cliente solicita la devolución de una entrada emitida al menos 72 horas antes del evento.

El administrador puede:

- **Aprobar:** se registra la resolución, se invalida la entrada, se solicita la devolución del lugar a Reservas y el reintegro a Compras.
- **Rechazar:** se registra la resolución sin invalidar la entrada.

Las operaciones se coordinan mediante comunicaciones REST. Cada microservicio administra sus propias transacciones.

## Cancelación de eventos

Cuando un evento se cancela, Entradas invalida los tickets emitidos correspondientes.

La coordinación general de la cancelación y los reintegros involucra también a Eventos y Lugares, Reservas y Compras.

## Seguridad

El servicio utiliza JWT y controles de autorización:

- Los clientes consultan sus entradas y solicitan sus devoluciones.
- Los administradores resuelven devoluciones y asignan personal de acceso.
- El personal autorizado valida entradas únicamente en eventos asignados.
- Los servicios internos utilizan credenciales específicas para emisión, cancelaciones y reportes.

Las operaciones REST se documentan mediante Swagger/OpenAPI.