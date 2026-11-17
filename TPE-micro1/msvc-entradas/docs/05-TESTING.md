# 05 — Pruebas y testing de Entradas

## 1. Descripción general

El microservicio `msvc-entradas` cuenta con pruebas unitarias destinadas a verificar las principales reglas de negocio relacionadas con la emisión, consulta e invalidación de entradas, la gestión de solicitudes de devolución y la generación de códigos QR.

Las pruebas se desarrollan con JUnit y Mockito, utilizando mocks para simular las dependencias externas y verificar las interacciones con repositorios y otros microservicios.

También se incluye una prueba de inicialización del contexto de Spring Boot.

## 2. Pruebas de emisión y consulta de entradas

### 2.1. EntradaServiceTest

Las pruebas verifican distintos escenarios relacionados con la emisión y consulta de entradas.

| Caso de prueba | Comportamiento verificado |
|---|---|
| Emisión con un identificador de compra inválido. | Se rechaza la operación con error HTTP 400. |
| Repetición de una emisión para una compra con entradas existentes. | Se devuelven las entradas registradas sin generar duplicados. |
| Consulta de una entrada perteneciente a otro cliente. | Se rechaza la operación con error HTTP 403 y no se genera el código QR. |

También se comprueba que, al recuperar entradas existentes, no se realicen consultas innecesarias a los microservicios Compras y Reservas.

### 2.2. EntradaServiceImplTest

Las pruebas verifican las reglas de negocio relacionadas con la emisión e invalidación de entradas.

| Caso de prueba | Comportamiento verificado |
|---|---|
| Emitir entradas para una reserva con varios lugares. | Se genera una entrada por cada lugar adquirido, con códigos QR únicos. |
| Repetir la emisión de una compra. | Se recuperan las entradas existentes sin generar duplicados. |
| Intentar emitir entradas para una compra no confirmada. | Se rechaza la operación sin consultar Reservas ni guardar entradas. |
| Intentar emitir entradas para una reserva no confirmada. | Se rechaza la operación sin persistir entradas. |
| Invalidar entradas de un evento cancelado. | Las entradas emitidas pasan al estado `INVALIDADA`. |
| Repetir la invalidación de un evento. | No se generan modificaciones adicionales sobre las entradas ya invalidadas. |
| Intentar invalidar entradas de un evento que no está cancelado. | Se rechaza la operación y no se modifican las entradas. |

La invalidación por cancelación se verifica como una operación propia de Entradas. La coordinación general del proceso de cancelación corresponde a Eventos y Lugares, que se comunica con Reservas, Entradas y Compras.

## 3. Pruebas de solicitudes de devolución

### 3.1. DevolucionServiceImplTest

Las pruebas verifican las reglas de negocio correspondientes a las solicitudes de devolución y su resolución.

| Caso de prueba | Comportamiento verificado |
|---|---|
| Solicitar una devolución con más de 72 horas de anticipación. | Se registra una solicitud con estado `PENDIENTE`. |
| Solicitar una devolución con menos de 72 horas de anticipación. | Se rechaza la solicitud y no se guarda. |
| Solicitar la devolución de una entrada inexistente. | Se rechaza la operación sin realizar consultas externas innecesarias. |
| Solicitar otra devolución cuando ya existe una pendiente. | Se rechaza la solicitud duplicada y no se persiste. |
| Aprobar una solicitud de devolución. | La solicitud cambia a `APROBADA`, se solicita la devolución del lugar a Reservas y el reintegro a Compras. |
| Rechazar una solicitud de devolución. | La solicitud cambia a `RECHAZADA`, la entrada conserva el estado `EMITIDA` y no se solicita la devolución del lugar ni el reintegro. |

Las pruebas también comprueban que, al aprobar una devolución, se utilicen los datos correspondientes a la entrada para solicitar la devolución del lugar a Reservas y que se invoque la operación de reintegro de Compras.

## 4. Pruebas de generación de códigos QR

### 4.1. QrCodeServiceImplTest

Las pruebas verifican el funcionamiento del servicio de generación de códigos QR.

| Caso de prueba | Comportamiento verificado |
|---|---|
| Generar una imagen QR en formato PNG codificada en Base64. | Se obtiene una imagen válida que puede decodificarse correctamente. |
| Generar códigos QR para contenidos diferentes. | Se obtienen imágenes codificadas diferentes. |

Estas pruebas comprueban que el servicio genera imágenes QR válidas y que su representación depende del contenido codificado.

## 5. Prueba de inicialización de la aplicación

El microservicio incluye una prueba de contexto mediante `@SpringBootTest`, destinada a verificar que la aplicación Spring Boot pueda inicializarse correctamente con su configuración.

Esta prueba complementa las pruebas unitarias de los servicios al comprobar la inicialización del contexto de la aplicación.

## 6. Estrategia de ejecución

### 6.1. Pruebas automatizadas

Las pruebas unitarias pueden ejecutarse desde el directorio raíz de `msvc-entradas` mediante Maven:

```bash
mvn test
```

Este comando ejecuta las pruebas detectadas por la configuración del proyecto y permite consultar sus resultados en los reportes generados por Maven.

### 6.2. Pruebas de API REST

La colección Postman de TicketYa permite ejecutar solicitudes sobre los endpoints publicados a través del API Gateway.

Los flujos de Entradas contemplan:

- Emisión de entradas a partir de compras con pago aprobado.
- Consulta individual de entradas.
- Consulta de entradas por cliente.
- Obtención de códigos QR.
- Solicitud de devoluciones.
- Consulta de solicitudes pendientes.
- Aprobación y rechazo de devoluciones.
- Invalidación de entradas por cancelación de eventos.

Estas pruebas permiten verificar el comportamiento HTTP y las interacciones del microservicio dentro del circuito general de TicketYa.

## 7. Conclusión

El microservicio `msvc-entradas` dispone de pruebas automatizadas para sus principales operaciones de negocio, incluyendo la emisión idempotente de entradas, las validaciones de compras y reservas, la generación de códigos QR, la gestión de devoluciones y la invalidación por cancelación de eventos.

Las pruebas unitarias verifican el comportamiento de los servicios mediante dependencias simuladas, mientras que la colección Postman permite comprobar los principales flujos REST durante la ejecución local del sistema.