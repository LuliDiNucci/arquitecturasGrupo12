# 06 — Decisiones, supuestos y limitaciones

**Estado:** revisión de decisiones implementadas en el código de `msvc-compras`.

Este documento registra las decisiones de arquitectura y diseño del microservicio de Compras, las limitaciones actuales y los aspectos que deberán revisarse durante la integración y las siguientes entregas.

## 1. Decisiones de arquitectura

### 1.1. Base de datos independiente por microservicio

Cada microservicio administra su propia base de datos:

- Compras: `ticketya_compras`.
- Reservas: base de datos propia de Reservas.
- Eventos y Lugares: base de datos propia.
- Entradas: base de datos propia.

Compras conserva identificadores de entidades pertenecientes a otros microservicios, como `clienteId`, `reservaId`, `eventoId` y `entradaId`, sin establecer relaciones JPA ni claves foráneas entre bases de datos.

La comunicación y validación de esos datos se realizan mediante clientes REST.

### 1.2. Tecnologías y configuración

El microservicio utiliza:

- Java 21.
- Spring Boot 4.1.2.
- Spring Data JPA e Hibernate.
- MySQL como base de datos.
- Spring Cloud OpenFeign para la comunicación entre microservicios.
- Springdoc OpenAPI para documentar los endpoints.

El puerto configurado para Compras es `8082`.

Las URL de los demás microservicios se configuran mediante propiedades, lo que permite ajustar los destinos durante la integración.

### 1.3. Comunicación entre microservicios

Compras utiliza clientes Feign para comunicarse con:

- **Reservas:** consultar, confirmar y liberar reservas.
- **Eventos y Lugares:** consultar información de eventos y precios de sectores.
- **Entradas:** emitir, consultar e invalidar entradas.

Los identificadores externos se almacenan como valores simples. La información necesaria se obtiene mediante llamadas REST.

No existe una transacción distribuida que abarque las bases de datos de los distintos microservicios.

Por este motivo, Compras coordina las operaciones y contempla estados intermedios para gestionar situaciones en las que una parte del proceso se completa y otra falla.

## 2. Decisiones sobre el modelo de datos

### 2.1. Congelamiento de precios

El precio se consulta al microservicio de Eventos cuando se inicia la compra.

Compras guarda el precio obtenido en `DetalleCompra`, junto con:

- `precioUnitario`.
- `cantidad`.
- `subtotal`.

El total de la compra se conserva en `Compra`.

Los importes utilizan `BigDecimal`. Los campos de precio unitario y subtotal tienen precisión 12 y escala 2.

De esta manera, una modificación posterior del precio del sector no altera el importe de una compra ya iniciada ni el cálculo de sus reintegros.

### 2.2. Una compra por reserva

La entidad `Compra` posee una restricción única sobre `reserva_id` (`uk_compra_reserva`).

Esto impide registrar más de una compra para la misma reserva, incluso si llegan solicitudes concurrentes.

La restricción de base de datos complementa la validación realizada desde el servicio.

### 2.3. Separación de detalles e ítems de compra

El modelo distingue:

- `DetalleCompra`: representa la cantidad y el precio de las entradas correspondientes a un sector.
- `ItemCompra`: representa cada entrada individual asociada a un detalle.

Esta separación permite identificar y gestionar reintegros individuales, sin obligar a reintegrar toda la compra.

### 2.4. Reintegros individuales

La entidad `Reintegro` se relaciona con un pago y puede asociarse a un `ItemCompra` y a una entrada.

Se utilizan restricciones únicas para evitar duplicados:

- `uk_reintegro_item`: un reintegro por ítem de compra.
- `uk_reintegro_entrada`: un reintegro por entrada.

Además, `ItemCompra` posee una restricción única sobre `entrada_id`.

El identificador de entrada puede ser nulo cuando todavía no se emitió la entrada, por ejemplo, durante una compensación por una reserva que no pudo confirmarse.

## 3. Decisiones sobre pagos e idempotencia

### 3.1. Clave de idempotencia

El procesamiento del pago requiere una clave de idempotencia.

La entidad `Pago` tiene una restricción única sobre `clave_idempotencia` (`uk_pago_idempotencia`).

Cuando llega una solicitud con una clave ya utilizada, el servicio verifica que corresponda a la misma compra, medio de pago e importe.

- Si los datos coinciden y existe un resultado registrado, se devuelve el resultado existente.
- Si la clave se reutiliza con datos diferentes, se rechaza la solicitud.
- Si el pago anterior continúa en estado `PROCESANDO`, se informa que todavía está en proceso.

La clave no se utiliza como sustituto de las validaciones del negocio: también se verifica que la compra esté en condiciones de ser pagada.

### 3.2. Bloqueo de compras concurrentes

El repositorio `CompraRepository` implementa `bloquearCompra()` utilizando:

`@Lock(LockModeType.PESSIMISTIC_WRITE)`

Esto permite bloquear una compra durante una operación transaccional y evitar que dos solicitudes procesen simultáneamente operaciones incompatibles sobre el mismo registro.

Se utiliza en operaciones sensibles, como el procesamiento de pagos, la confirmación y los reintegros.

El bloqueo complementa las restricciones únicas y las validaciones de estado, pero no reemplaza la idempotencia ni garantiza por sí solo la atomicidad de las llamadas a otros microservicios.

### 3.3. Pasarela de pago simulada

La implementación actual utiliza una pasarela mock (`PasarelaPagoMock`).

Su comportamiento es determinístico:

- Los pagos válidos se aprueban.
- El medio `TARJETA_RECHAZADA` permite simular un pago rechazado.
- La referencia externa se genera a partir de la clave de idempotencia.

La pasarela no realiza cobros reales ni representa una integración con un proveedor externo.

La referencia determinística permite reconocer una misma operación simulada, pero no equivale a disponer de persistencia o garantías de idempotencia propias de una pasarela real.

### 3.4. Estados de pago

Los estados definidos para `Pago` son:

- `PENDIENTE`.
- `PROCESANDO`.
- `APROBADO`.
- `RECHAZADO`.
- `ERROR`.

Estos estados permiten distinguir una solicitud pendiente, una operación en curso y su resultado.

## 4. Decisiones sobre confirmación y compensación

### 4.1. Confirmación de compra

Una compra no se marca como `CONFIRMADA` únicamente porque el pago haya sido aprobado.

Para confirmar la compra, el servicio debe:

1. Verificar que el pago esté aprobado.
2. Verificar el estado y la vigencia de la reserva.
3. Confirmar la reserva en el microservicio de Reservas.
4. Solicitar la emisión de entradas al microservicio de Entradas.
5. Validar la cantidad y los datos de las entradas recibidas.

Solamente cuando se completan y validan los pasos necesarios se establece el estado `CONFIRMADA`.

### 4.2. Estados intermedios de compra

La entidad `Compra` contempla los siguientes estados:

- `PENDIENTE`.
- `CONFIRMACION_PENDIENTE`.
- `COMPENSACION_PENDIENTE`.
- `LIBERACION_PENDIENTE`.
- `CONFIRMADA`.
- `RECHAZADA`.
- `REINTEGRO_PARCIAL`.
- `REINTEGRADA`.

Los estados intermedios permiten representar situaciones en las que una operación remota todavía debe completarse o compensarse.

Por ejemplo:

- `CONFIRMACION_PENDIENTE`: el pago fue aprobado, pero todavía falta completar la confirmación de la compra.
- `COMPENSACION_PENDIENTE`: el pago fue aprobado, pero no se pudo completar la operación y corresponde gestionar su reintegro.
- `LIBERACION_PENDIENTE`: el pago fue rechazado y todavía debe completarse la liberación de la reserva.

Esto evita informar una compra como confirmada cuando todavía no se emitieron correctamente sus entradas.

### 4.3. Vigencia de las reservas

Antes de continuar con una compra, Compras verifica que la reserva esté activa y que su fecha de vencimiento sea posterior al momento actual.

No alcanza con comprobar únicamente el estado `ACTIVA`, porque la expiración lógica de una reserva puede producirse antes de que se actualice físicamente su estado en la base de datos.

## 5. Decisiones sobre reintegros

### 5.1. Reintegro individual

El reintegro se procesa por entrada individual.

Antes de realizarlo, Compras consulta al microservicio de Entradas y verifica que la entrada corresponda al cliente y a la compra, y que tenga un estado que permita el reintegro (`DEVOLUCION_APROBADA` o `INVALIDADA`).

El importe se calcula utilizando el precio unitario congelado en `DetalleCompra`.

Si ya existe un reintegro aprobado para el ítem, se devuelve el resultado existente para evitar generar otro reintegro.

### 5.2. Reintegros parciales

Una compra puede tener reintegros aprobados para algunas de sus entradas sin que todas hayan sido reintegradas.

En ese caso, su estado pasa a `REINTEGRO_PARCIAL`.

Cuando todos los ítems tienen sus reintegros aprobados, pasa a `REINTEGRADA`.

### 5.3. Cancelación de eventos

Cuando se cancela un evento, Compras:

1. Verifica que el evento esté en estado `CANCELADO`.
2. Solicita al microservicio de Entradas la invalidación de las entradas correspondientes.
3. Busca las compras asociadas al evento.
4. Procesa los reintegros de cada compra.
5. Registra las compras que no pudieron completarse para permitir gestionar nuevamente la operación.

Cada compra se procesa en una transacción independiente, de modo que un fallo en una compra no impide intentar procesar las restantes.

El endpoint recibe el encabezado `X-Operacion-Id`, pero actualmente el identificador se valida y no se persiste ni se utiliza para deduplicar la operación completa.

### 5.4. Compensación cuando no se puede confirmar una reserva

Si el pago fue aprobado, pero la reserva no pudo confirmarse, Compras puede gestionar una compensación.

Antes de reintegrar, verifica que no se hayan emitido entradas para esa compra.

La compensación utiliza el precio congelado de cada ítem y actualiza el estado de la compra según el resultado de los reintegros.

### 5.5. Pasarela de reintegros simulada

La pasarela mock genera una referencia determinística a partir del identificador del pago y del ítem de compra.

Para una misma combinación de pago e ítem, la referencia generada es la misma.

Actualmente, la implementación aprueba los reintegros válidos. No se simulan rechazos de una entidad financiera real.

Los reintegros de importe cero se gestionan desde el servicio sin invocar el método de reintegro de la pasarela, generando una referencia local.

## 6. Seguridad y configuración

### 6.1. JWT

La autenticación y autorización mediante JWT no forman parte de la implementación actual de la primera entrega.

Las rutas internas deben considerarse endpoints de comunicación entre microservicios y no endpoints públicos destinados a ser utilizados directamente por clientes externos.

La protección efectiva de estas rutas deberá definirse e implementarse en la integración general, por ejemplo, mediante el Gateway y las políticas de seguridad correspondientes.

### 6.2. Configuración de base de datos

La configuración actual utiliza `spring.jpa.hibernate.ddl-auto=update`.

Esta opción facilita el desarrollo porque permite actualizar el esquema de la base de datos a partir de las entidades.

Para un entorno productivo deberá definirse un mecanismo controlado de migraciones y versionado del esquema.

## 7. Limitaciones y riesgos pendientes

Se identifican los siguientes aspectos para revisar durante la integración y las siguientes entregas:

1. **Consistencia entre microservicios:** no existe una transacción distribuida. Una operación puede completarse en un servicio y fallar en otro. Los estados intermedios y las compensaciones permiten gestionar estos casos, pero no eliminan la necesidad de conciliación.

2. **Resultado incierto de una operación externa:** si una pasarela real aprueba un pago o reintegro y se pierde la respuesta, será necesario consultar o reconciliar el resultado antes de volver a ejecutar la operación. La pasarela mock actual no reproduce completamente este escenario.

3. **Reintentos de reintegros:** las restricciones únicas evitan duplicar registros locales, pero no garantizan por sí solas que una pasarela externa no realice dos reintegros. La integración real deberá utilizar una clave de idempotencia reconocida por el proveedor.

4. **Identificador de cancelación:** `X-Operacion-Id` se recibe y valida, pero todavía no se utiliza para registrar y deduplicar las solicitudes de cancelación de eventos.

5. **Contratos con Entradas:** deben mantenerse coordinados los contratos de emisión, consulta e invalidación de entradas, especialmente para identificar correctamente cada `ItemCompra` y verificar los estados antes de reintegrar.

6. **Seguridad interna:** JWT y las políticas definitivas de autorización quedan pendientes de integración.

7. **Migraciones de base de datos:** `ddl-auto=update` es una configuración de desarrollo y deberá reemplazarse o complementarse con migraciones controladas antes del despliegue productivo.

8. **Pruebas:** la existencia de pruebas unitarias y de concurrencia no implica que todas hayan sido ejecutadas exitosamente. Los resultados de ejecución deberán registrarse como evidencia de validación.

## 8. Proceso de trabajo para completar funcionalidades

Para evitar diferencias entre documentación, contratos e implementación, cada funcionalidad deberá seguir este proceso:

1. Revisar los criterios de aceptación de la historia de usuario.
2. Definir las entidades y los cambios necesarios en el modelo de datos.
3. Actualizar los DTO y los contratos REST/OpenAPI.
4. Implementar repositorios, servicios, controladores y clientes Feign necesarios.
5. Incorporar y ejecutar las pruebas correspondientes.
6. Actualizar la documentación y el estado de implementación en el README.
7. Validar los endpoints mediante Postman.

Una historia de usuario se considerará completa cuando su implementación, pruebas y documentación estén verificadas.