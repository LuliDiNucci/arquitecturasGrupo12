# 06 - Limitaciones, decisiones de diseño y mejoras futuras

## 1. Introducción

Este documento describe las principales decisiones de diseño tomadas durante el desarrollo del microservicio `msvc-entradas`, sus limitaciones actuales y las posibles mejoras que podrían incorporarse en futuras versiones.

El microservicio permite gestionar la emisión, consulta, invalidación y devolución de entradas para eventos, comunicándose con los microservicios de compras, reservas y eventos.

## 2. Decisiones de diseño

### 2.1 Separación por capas

Se utiliza una arquitectura organizada en capas para separar las responsabilidades:

- **Controllers:** reciben las solicitudes HTTP y devuelven las respuestas.
- **Services:** contienen las reglas de negocio y coordinan las operaciones.
- **Repositories:** permiten acceder y persistir la información en la base de datos.
- **Entities:** representan las entidades persistidas.
- **DTOs:** permiten intercambiar información sin exponer directamente las entidades internas.
- **Clients:** permiten comunicarse con otros microservicios.

Esta separación facilita el mantenimiento, las pruebas y la evolución del sistema.

### 2.2 Uso de DTOs

Se utilizan DTOs para intercambiar información entre los microservicios.

Esto permite que cada servicio mantenga sus propias entidades y exponga únicamente los datos necesarios para realizar una operación.

Por ejemplo, `EventoDTO` permite consultar información de un evento sin necesitar acceder directamente a la entidad `Evento` del microservicio de eventos.

### 2.3 Comunicación entre microservicios

Se utilizan dos mecanismos de comunicación HTTP:

- `RestClient`: utilizado para consultar información de compras y reservas.
- `FeignClient`: utilizado para consultar información de eventos y realizar determinadas operaciones sobre compras y reservas.

Esta separación permite encapsular las llamadas externas dentro de clientes específicos.

### 2.4 Idempotencia en la emisión

La emisión de entradas contempla que una misma compra pueda generar solicitudes repetidas.

Antes de crear nuevas entradas, el servicio consulta si ya existen entradas asociadas a esa compra.

Si existen, devuelve las entradas previamente generadas y evita crear duplicados.

Además, la base de datos establece una restricción de unicidad para la combinación de reserva, detalle de reserva e índice del lugar, evitando que se persista más de una entrada para el mismo lugar reservado.

### 2.5 Identificación mediante códigos QR

Cada entrada recibe un código QR generado a partir de un identificador único, generado mediante UUID.

La imagen se genera en formato PNG y se entrega codificada en Base64 para facilitar su intercambio mediante la API.

### 2.6 Gestión de devoluciones

Las devoluciones se gestionan mediante solicitudes que tienen estados propios:

- `PENDIENTE`
- `APROBADA`
- `RECHAZADA`

Se establece un plazo mínimo de 72 horas antes del evento para solicitar una devolución.

La aprobación involucra operaciones en otros microservicios. Para permitir recuperar operaciones ante fallos de comunicación, el servicio admite volver a ejecutar las llamadas externas cuando la solicitud ya se encuentra aprobada.

Las operaciones de devolución de lugares en Reservas y de reintegro en Compras contemplan mecanismos de idempotencia para evitar duplicar sus efectos.

## 3. Limitaciones actuales

### 3.1 Operaciones distribuidas entre microservicios

La aprobación de una devolución requiere actualizar información en `msvc-entradas`, `msvc-reservas` y `msvc-compras`.

Aunque se utiliza una transacción local para actualizar la solicitud y la entrada, esta transacción no abarca automáticamente las operaciones realizadas en los otros microservicios.

Por lo tanto, no existe una única transacción distribuida que garantice que todas las operaciones se confirmen o reviertan conjuntamente.

Si una de las llamadas externas falla, la solicitud puede permanecer aprobada y la entrada invalidada, mientras alguna operación externa todavía está pendiente.

Actualmente, la recuperación se realiza mediante un nuevo llamado al endpoint de aprobación, que vuelve a ejecutar las operaciones externas.

### 3.2 Invalidación por cancelación de eventos

Cuando un evento se cancela, el microservicio invalida las entradas que se encuentran en estado `EMITIDA`.

Sin embargo, la liberación de los lugares y los reintegros correspondientes no se realizan desde esta operación.

Estas acciones quedan pendientes de una integración adicional con los microservicios de reservas y compras.

### 3.3 Seguridad y autorización

La API recibe identificadores de clientes y administradores mediante parámetros o encabezados HTTP.

En la implementación actual, la validación de pertenencia de una entrada se realiza en la consulta individual mediante el encabezado `X-Cliente-Id`.

Sin embargo, este identificador es recibido directamente en la solicitud y no se encuentra respaldado por un mecanismo de autenticación que verifique la identidad del usuario.

Además, la consulta de entradas por cliente y las operaciones administrativas de aprobación y rechazo de devoluciones no cuentan actualmente con una validación de identidad y permisos.

Se puede mejorar la seguridad incorporando autenticación y autorización centralizadas para verificar la identidad y los permisos de cada usuario.

### 3.4 Gestión de errores externos

Las comunicaciones con otros microservicios pueden fallar por problemas de conectividad o indisponibilidad.

Los clientes implementados con `RestClient` contemplan el manejo de excepciones de comunicación y transforman esos errores en respuestas HTTP 502.

Para mejorar la tolerancia a fallos, se pueden incorporar mecanismos adicionales de recuperación, como reintentos automáticos controlados, circuit breakers y registro de operaciones pendientes.

## 4. Mejoras futuras

Se identifican las siguientes mejoras posibles:

| Mejora | Descripción |
|---|---|
| Transacciones distribuidas mediante Saga | Coordinar las operaciones entre compras, reservas y entradas, contemplando acciones compensatorias ante fallos. |
| Integración completa ante cancelaciones | Incorporar la liberación de lugares y los reintegros correspondientes cuando se cancela un evento. |
| Seguridad centralizada | Incorporar autenticación y autorización mediante tokens y roles. |
| Reintentos automáticos controlados | Mejorar la recuperación ante fallos temporales en las llamadas a otros microservicios. |
| Monitoreo y trazabilidad | Incorporar registros y métricas para facilitar el seguimiento de las operaciones. |
| Validación de códigos QR | Incorporar un mecanismo de validación de entradas al momento del ingreso al evento. |
| Mayor cobertura de pruebas | Incorporar pruebas de integración y pruebas de los endpoints REST, incluyendo escenarios de seguridad. |

## 5. Conclusión

El microservicio `msvc-entradas` implementa las funcionalidades principales para gestionar entradas asociadas a eventos.

Su diseño separa las responsabilidades por capas, utiliza DTOs para la comunicación y permite integrarse con otros microservicios mediante clientes HTTP.

Las funcionalidades de emisión, consulta, invalidación y devolución cuentan con reglas de negocio y pruebas automatizadas.

Las limitaciones identificadas permiten establecer una línea de trabajo para futuras mejoras, especialmente en relación con las operaciones distribuidas, la seguridad y la tolerancia a fallos.