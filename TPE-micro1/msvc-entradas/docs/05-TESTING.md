# 05 - Pruebas y Testing

## 1. Descripción general

El microservicio `msvc-entradas` cuenta con pruebas unitarias orientadas a verificar el comportamiento de sus principales servicios, la emisión e invalidación de entradas, la gestión de solicitudes de devolución y la generación de códigos QR.

Las pruebas utilizan JUnit y Mockito para comprobar los resultados de las operaciones y verificar las interacciones con repositorios y otros microservicios, sin depender de servicios externos reales.

## 2. Pruebas de emisión y consulta de entradas

### 2.1. EntradaServiceTest

Se prueban distintos escenarios relacionados con la emisión y consulta de entradas:

| Caso de prueba | Resultado esperado |
|---|---|
| Intentar emitir con un identificador de compra inválido. | Se rechaza la operación con error HTTP 400. |
| Solicitar nuevamente la emisión de una compra que ya tiene entradas. | Se devuelven las entradas existentes sin generar duplicados. |
| Consultar una entrada perteneciente a otro cliente. | Se rechaza la operación con error HTTP 403 y no se genera el QR. |

También se verifica que, al recuperar entradas existentes, no se vuelvan a consultar innecesariamente los microservicios de compras y reservas.

**Aclaración:** aunque el servicio contempla el caso de entrada inexistente mediante un error HTTP 404, no se encontró una prueba específica que verifique ese escenario.

### 2.2. EntradaServiceImplTest

Se prueban los principales escenarios de emisión e invalidación de entradas:

| Caso de prueba | Resultado esperado |
|---|---|
| Emitir entradas para una reserva con varios lugares. | Se genera una entrada por cada lugar, con códigos QR únicos. |
| Repetir la emisión de una compra. | No se generan entradas duplicadas. |
| Intentar emitir entradas para una compra no confirmada. | Se rechaza la operación y no se consultan las reservas ni se guardan entradas. |
| Intentar emitir entradas para una reserva no confirmada. | Se rechaza la operación y no se guardan entradas. |
| Invalidar entradas de un evento cancelado. | Las entradas emitidas pasan al estado `INVALIDADA`. |
| Repetir la invalidación de un evento. | No se generan nuevas acciones ni se vuelven a guardar las entradas. |
| Intentar invalidar entradas de un evento que no está cancelado. | Se rechaza la operación y no se modifican las entradas. |

Además, se verifica que el proceso de invalidación por cancelación del evento no invoque actualmente a los microservicios de reservas ni de compras. Es decir, esta operación solamente invalida las entradas.

## 3. Pruebas de solicitudes de devolución

En `DevolucionServiceImplTest` se prueban los escenarios principales del proceso de devolución:

| Caso de prueba | Resultado esperado |

| Solicitar la devolución de una entrada con más de 72 horas de anticipación al evento. | La solicitud se registra con estado `PENDIENTE`. |
| Solicitar la devolución con menos de 72 horas de anticipación. | Se rechaza la solicitud y no se guarda. |
| Solicitar la devolución de una entrada inexistente. | Se rechaza la operación y no se realizan consultas innecesarias a otros microservicios. |
| Intentar generar otra solicitud cuando ya existe una pendiente para la misma entrada. | Se rechaza la solicitud duplicada y no se guarda. |
| Aprobar una solicitud de devolución. | La solicitud pasa a `APROBADA`, se solicita la devolución del lugar a Reservas y el reintegro a Compras. |
| Rechazar una solicitud de devolución. | La solicitud pasa a `RECHAZADA`, la entrada permanece en estado `EMITIDA` y no se solicita la devolución del lugar ni el reintegro. |

También se verifica que, al aprobar una devolución, se invoque el endpoint correspondiente de Reservas utilizando los datos de la entrada y que se solicite el reintegro al microservicio de Compras.

**Aclaración:** no se encontró una prueba específica que verifique el reintento de las operaciones externas cuando una solicitud ya se encuentra aprobada. Tampoco se prueba específicamente el límite exacto de las 72 horas.

## 4. Pruebas de generación de códigos QR

En `QrCodeServiceImplTest` se verifica el funcionamiento del servicio encargado de generar los códigos QR.

| Caso de prueba | Resultado esperado |
|---|---|
| Generar una imagen QR en formato PNG codificada en Base64. | Se obtiene una imagen válida que puede decodificarse correctamente. |
| Generar códigos QR para contenidos diferentes. | Se obtienen imágenes codificadas diferentes. |

Estas pruebas permiten comprobar que el servicio genera imágenes QR válidas y que el contenido del código influye en el resultado.

## 5. Prueba de inicialización de la aplicación

Además de las pruebas unitarias, se incluye una prueba de contexto mediante `@SpringBootTest`, que verifica que la aplicación Spring pueda inicializarse correctamente.

## 6. Alcance y limitaciones de las pruebas

Las pruebas se concentran principalmente en la lógica de negocio de los servicios y en la interacción con repositorios y clientes de otros microservicios.

Se utilizan mocks para simular las dependencias externas, lo que permite probar los distintos escenarios sin necesidad de que los demás microservicios estén ejecutándose.

Actualmente, no se cuenta con pruebas de integración específicas de los endpoints REST ni con pruebas de autenticación o autorización para todos los controladores.

En particular, la consulta de entradas por cliente no cuenta actualmente con una verificación de identidad del cliente solicitante, y tampoco se encontraron pruebas que cubran ese aspecto.

Por lo tanto, las pruebas existentes permiten validar buena parte de la lógica principal del microservicio, aunque quedan pendientes pruebas de integración y de seguridad que amplíen la cobertura.