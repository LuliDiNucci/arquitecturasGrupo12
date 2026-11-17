# 04 — Plan y evidencia de testing de Compras y Pagos

## 1. Objetivo

Las pruebas del microservicio `msvc-compras` están orientadas a verificar las principales reglas de negocio relacionadas con la creación de compras, el procesamiento de pagos, la emisión de entradas, los reintegros y la prevención de operaciones duplicadas.

El proyecto cuenta con 13 métodos de prueba:

- 11 pruebas unitarias desarrolladas con JUnit 5 y Mockito.
- 2 pruebas de concurrencia e integración con Spring Boot y MySQL.

## 2. Estrategia de testing

### 2.1. Pruebas unitarias

**Tecnologías:** JUnit 5 y Mockito.

Las pruebas unitarias verifican la lógica de los servicios de Compras de manera aislada, simulando las dependencias externas mediante mocks.

Los principales aspectos evaluados son:

- Cálculo del importe total de una compra.
- Creación de compras, detalles e ítems.
- Prevención de compras duplicadas.
- Confirmación de compras y emisión de entradas.
- Idempotencia de pagos.
- Prevención de cobros duplicados.
- Registro de reintegros individuales.
- Idempotencia de reintegros y cancelaciones.
- Actualización de los estados de las compras.

### 2.2. Pruebas de concurrencia e integración

**Tecnologías:** Spring Boot Test, JUnit 5 y MySQL.

Las pruebas de concurrencia utilizan `@SpringBootTest` y una base de datos MySQL para verificar el comportamiento del servicio frente a solicitudes simultáneas de pago.

Las dependencias externas, como Reservas, la pasarela de pagos y el servicio de confirmación de Compras, se simulan mediante Mockito. La persistencia de compras y pagos se realiza sobre MySQL.

La base de datos utilizada para las pruebas es:

`ticketya_compras_test`

La configuración de Hibernate utiliza:

`spring.jpa.hibernate.ddl-auto=create-drop`

Esta configuración está destinada exclusivamente al entorno de pruebas, ya que permite crear y eliminar las tablas durante la ejecución.

## 3. Pruebas implementadas

### 3.1. CompraServiceImplTest

**Archivo:** `CompraServiceImplTest.java`

| Método de prueba | Comportamiento verificado |
|---|---|
| `iniciarCompraCalculaTotalYRegistraItems` | Crea una compra a partir de una reserva con dos sectores y tres entradas. Verifica el cálculo del total, la creación de detalles e ítems y la consulta de precios a Eventos. |
| `noPermitirCompraDuplicadaPorReserva` | Impide crear una segunda compra para una reserva que ya tiene una compra asociada. Verifica que no se realicen nuevas consultas externas ni persistencia. |
| `confirmarCompraSoloCuandoEstanTodasLasEntradas` | Verifica la confirmación de la reserva, la emisión de entradas, la asociación de sus identificadores a los ítems y la repetición idempotente de la confirmación. |

**Historias de usuario relacionadas:** US01, US02 y US04.

### 3.2. PagoServiceImplTest

**Archivo:** `PagoServiceImplTest.java`

| Método de prueba | Comportamiento verificado |
|---|---|
| `testPagoIdempotente` | Repite un pago con la misma clave y verifica que se obtenga el mismo resultado sin generar otro cobro. |
| `testClaveReutilizadaConDatosDiferentes` | Impide reutilizar una clave de idempotencia con otro medio de pago. |
| `testClaveUtilizadaEnOtraCompra` | Impide utilizar una misma clave de idempotencia para otra compra. |
| `testNoPermitirSegundoPago` | Impide procesar otro pago cuando la compra ya no se encuentra pendiente de pago. |

**Historias de usuario relacionadas:** US03 y US06.

### 3.3. ReintegroServiceImplTest

**Archivo:** `ReintegroServiceImplTest.java`

| Método de prueba | Comportamiento verificado |
|---|---|
| `devolucionIndividualUsaPrecioCongeladoYEvitaduplicados` | Registra el reintegro de una entrada utilizando su precio histórico y evita generar otro reintegro ante una solicitud repetida. |
| `cancelarEventoDosVecesNoDuplicaReintegros` | Ejecuta dos veces la cancelación de un evento y verifica que no se dupliquen los reintegros. |
| `devolverUnaDeDosEntradasDejaReintegroParcial` | Reintegra una de dos entradas y verifica que la compra quede en estado `REINTEGRO_PARCIAL`. |
| `cancelarEventoReintegraCompraSinEntradasEmitidas` | Verifica que una compra con pago aprobado, pero sin entradas emitidas, también pueda ser reintegrada ante la cancelación del evento. |

**Historias de usuario relacionadas:** US07 y US08.

### 3.4. PagoConcurrenciaMySQLIT

**Archivo:** `PagoConcurrenciaMySQLIT.java`

| Método de prueba | Comportamiento verificado |
|---|---|
| `dosSolicitudesMismaCompraSoloUnCobro` | Ejecuta dos solicitudes simultáneas para la misma compra y con la misma clave. Verifica que ambas obtengan el mismo pago, exista un solo registro y la pasarela se invoque una sola vez. |
| `mismaClaveEnDosComprasDistintasSoloUnCobro` | Ejecuta pagos simultáneos para dos compras diferentes utilizando la misma clave. Verifica que solo una operación sea aceptada y que la pasarela procese un único cobro. |

**Historia de usuario relacionada:** US06.

Estas pruebas utilizan MySQL para verificar la persistencia y el control de concurrencia, mientras que las comunicaciones con los servicios externos permanecen simuladas.

## 4. Relación entre historias de usuario y pruebas

| Historia de usuario | Escenarios verificados |
|---|---|
| US01 — Iniciar compra | Creación de compras, cálculo del importe y prevención de compras duplicadas. |
| US02 — Registrar detalle de compra | Creación de detalles e ítems y cálculo de importes por sector. |
| US03 — Procesar pago | Procesamiento idempotente y prevención de un segundo cobro. |
| US04 — Confirmar compra | Confirmación de reserva, emisión de entradas y repetición idempotente. |
| US06 — Evitar cobros duplicados | Reutilización de claves, validación de solicitudes y concurrencia de pagos. |
| US07 — Procesar reintegro individual | Precio histórico, reintegro parcial y prevención de duplicados. |
| US08 — Reintegros por cancelación | Cancelación repetida y reintegro de compras pagadas sin entradas emitidas. |

La tabla relaciona las historias con las pruebas automatizadas identificadas. Los escenarios de rechazo de pago, liberación de reservas y otros flujos operativos también forman parte de las solicitudes de prueba de la API documentadas en la colección Postman del proyecto.

## 5. Ejecución de las pruebas

### 5.1. Pruebas unitarias

Desde el directorio raíz del microservicio `msvc-compras`, ejecutar:

```bash
mvn test
```

Este comando ejecuta las pruebas unitarias detectadas por la configuración de Maven.

### 5.2. Pruebas de concurrencia con MySQL

La clase `PagoConcurrenciaMySQLIT` utiliza la base de datos:

`ticketya_compras_test`

La instancia MySQL de pruebas debe encontrarse disponible en `localhost:3306`, de acuerdo con la configuración del entorno.

Para ejecutar explícitamente las pruebas de concurrencia:

```bash
mvn -Dtest=PagoConcurrenciaMySQLIT test
```

La clase utiliza `@SpringBootTest` y el sufijo `IT`, por lo que se ejecuta explícitamente mediante el comando indicado.

### 5.3. Pruebas de API REST

La colección Postman del proyecto permite ejecutar solicitudes sobre los endpoints publicados a través del API Gateway.

Los escenarios contemplan las principales operaciones del circuito de compra:

- Inicio y consulta de compras.
- Procesamiento de pagos.
- Confirmación y emisión de entradas.
- Validación de solicitudes repetidas mediante claves de idempotencia.
- Consulta del estado de confirmación.
- Reintegros individuales y consultas de reintegros.
- Cancelación de eventos y procesamiento de reintegros.
- Compensación de compras.
- Reintentos de liberación de reservas.

Estas pruebas complementan las pruebas unitarias, permitiendo verificar las respuestas HTTP y el comportamiento de los servicios durante la ejecución local del sistema.

## 6. Conclusión

El microservicio `msvc-compras` dispone de pruebas unitarias para sus principales operaciones de negocio y de pruebas específicas de concurrencia sobre MySQL.

Las pruebas automatizadas abarcan la creación y confirmación de compras, el procesamiento idempotente de pagos, la prevención de cobros duplicados y la gestión de reintegros individuales y por cancelación de eventos.

La colección Postman complementa esta cobertura mediante la ejecución de los principales flujos de la API REST y sus interacciones con los demás microservicios.