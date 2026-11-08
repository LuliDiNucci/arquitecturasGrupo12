# 05 — Plan y evidencia de testing

## 1. Estado actual

El microservicio `msvc-compras` cuenta con pruebas unitarias y pruebas de integración orientadas a verificar las principales reglas de negocio.

Se identificaron **13 métodos de prueba implementados**:

- 11 pruebas unitarias con JUnit y Mockito.
- 2 pruebas de concurrencia con Spring Boot y MySQL.

## 2. Estrategia de testing

Se utilizan dos niveles principales:

### 2.1. Pruebas unitarias

Tecnologías: JUnit 5 y Mockito.

Permiten verificar la lógica de los servicios de Compras de manera aislada, simulando las dependencias externas mediante mocks.

Se comprueban:
- Cálculo del importe total.
- Creación de compras y sus detalles.
- Validación de compras duplicadas.
- Confirmación de compras y emisión de entradas.
- Idempotencia de pagos.
- Prevención de cobros duplicados.
- Registro de reintegros.
- Idempotencia ante devoluciones y cancelaciones.
- Actualización de estados de compra.

### 2.2. Pruebas de concurrencia e integración

Se utiliza `@SpringBootTest` con una base MySQL de pruebas.

Estas pruebas permiten verificar el comportamiento de las operaciones de pago cuando se reciben solicitudes simultáneas.

Se simulan mediante Mockito las dependencias externas, como Reservas, la pasarela de pago y el servicio de confirmación de Compras. La persistencia de compras y pagos se realiza sobre MySQL.

La configuración utiliza la base:

`ticketya_compras_test`

y `spring.jpa.hibernate.ddl-auto=create-drop`.

**Precaución:** esta configuración debe utilizarse exclusivamente con una base de datos de pruebas, nunca con la base de datos de producción.

## 3. Pruebas implementadas

### 3.1. CompraServiceImplTest

Archivo: `CompraServiceImplTest.java`

| Método de prueba | Comportamiento verificado |
| `iniciarCompraCalculaTotalYRegistraItems` | Crea una compra a partir de una reserva con dos sectores y tres entradas. Verifica el cálculo del total, la creación de detalles e ítems y la consulta de precios a Eventos. |
| `noPermitirCompraDuplicadaPorReserva` | Impide crear una segunda compra para una reserva que ya tiene una compra asociada. Verifica que no se realicen nuevas consultas externas ni persistencia. |
| `confirmarCompraSoloCuandoEstanTodasLasEntradas` | Verifica la confirmación de la reserva, la emisión de entradas, la asociación de sus identificadores a los ítems y la repetición idempotente de la confirmación. |

**Cobertura principal:** US01, US02 y US04.

### 3.2. PagoServiceImplTest

Archivo: `PagoServiceImplTest.java`

| Método de prueba | Comportamiento verificado |
| `testPagoIdempotente` | Repite un pago con la misma clave y verifica que se obtenga el mismo resultado sin generar otro cobro. |
| `testClaveReutilizadaConDatosDiferentes` | Impide reutilizar una clave de idempotencia con otro medio de pago. |
| `testClaveUtilizadaEnOtraCompra` | Impide utilizar una misma clave de idempotencia para otra compra. |
| `testNoPermitirSegundoPago` | Impide procesar otro pago cuando la compra ya no se encuentra pendiente de pago. |

**Cobertura principal:** US03 y US06.

### 3.3. ReintegroServiceImplTest

Archivo: `ReintegroServiceImplTest.java`

| Método de prueba | Comportamiento verificado |
| `devolucionIndividualUsaPrecioCongeladoYEvitaduplicados` | Registra el reintegro de una entrada utilizando su precio histórico y evita generar otro reintegro ante una solicitud repetida. |
| `cancelarEventoDosVecesNoDuplicaReintegros` | Ejecuta dos veces la cancelación de un evento y verifica que no se dupliquen los reintegros. |
| `devolverUnaDeDosEntradasDejaReintegroParcial` | Reintegra una de dos entradas y verifica que la compra quede en estado `REINTEGRO_PARCIAL`. |
| `cancelarEventoReintegraCompraSinEntradasEmitidas` | Verifica que una compra con pago aprobado, pero sin entradas emitidas, también pueda ser reintegrada ante la cancelación del evento. |

**Cobertura principal:** US07 y US08.

### 3.4. PagoConcurrenciaMySQLIT

Archivo: `PagoConcurrenciaMySQLIT.java`

| Método de prueba | Comportamiento verificado |
| `dosSolicitudesMismaCompraSoloUnCobro` | Ejecuta dos solicitudes simultáneas para la misma compra y con la misma clave. Verifica que ambas obtengan el mismo pago, exista un solo registro y la pasarela se invoque una sola vez. |
| `mismaClaveEnDosComprasDistintasSoloUnCobro` | Ejecuta pagos simultáneos para dos compras diferentes utilizando la misma clave. Verifica que solo una operación sea aceptada y que la pasarela procese un único cobro. |

**Cobertura principal:** US06.

Estas pruebas utilizan MySQL para la persistencia y concurrencia, mientras que las dependencias externas se encuentran simuladas.

## 4. Relación entre historias de usuario y pruebas

| Historia | Escenarios cubiertos por tests existentes | Estado de implementación de tests |
| US01 — Iniciar compra | Cálculo y creación de compra; prevención de compra duplicada. | Implementado |
| US02 — Detalle de compra | Cálculo de importes por sector, creación de detalles e ítems. | Implementado parcialmente |
| US03 — Procesar pago | Idempotencia y prevención de un segundo pago. | Implementado parcialmente |
| US04 — Confirmar compra | Confirmación y emisión completa; repetición idempotente. | Implementado |
| US05 — Pago rechazado | No se identificó un test específico del rechazo y liberación de reserva. | Pendiente |
| US06 — Idempotencia y concurrencia | Reutilización de claves, compras diferentes y solicitudes simultáneas. | Implementado |
| US07 — Reintegro individual | Precio histórico, reintegro parcial y prevención de duplicados. | Implementado |
| US08 — Cancelación de evento | Reintegros por cancelación repetida y compras sin entradas emitidas. | Implementado parcialmente |

**Aclaraciones sobre la cobertura:**

- US02 no cuenta con una prueba específica para diferencias de redondeo con `BigDecimal`.
- US03 no cuenta con una prueba específica para cada resultado posible de la pasarela ni para el rechazo del pago.
- US05 requiere incorporar una prueba que verifique el rechazo del pago y la liberación de la reserva.
- US08 no tiene una prueba específica que combine una entrada ya reintegrada individualmente con la posterior cancelación del evento.
- No se identificaron pruebas específicas de los controladores mediante `MockMvc` ni pruebas de contrato HTTP con los otros microservicios.

## 5. Pruebas pendientes recomendadas

Para ampliar la cobertura se proponen los siguientes escenarios:

| ID | Caso | Resultado esperado |
| P01 | Reserva vencida al iniciar la compra | No se crea la compra. |
| P02 | Reserva liberada o cancelada | Se rechaza el inicio de compra. |
| P03 | Diferencias de redondeo en importes | Los totales se calculan correctamente con `BigDecimal`. |
| P04 | Pago rechazado por la pasarela | Se registra el rechazo y se ejecuta la liberación de la reserva. |
| P05 | Reserva vencida al procesar el pago | No se realiza el cobro. |
| P06 | Fallo durante la confirmación o emisión | La compra no queda confirmada incorrectamente y conserva un estado recuperable. |
| P07 | Reintento de liberación de reserva | Se completa la liberación pendiente sin efectos duplicados. |
| P08 | Cancelación de evento con una entrada previamente reintegrada | No se genera un segundo reintegro para esa entrada. |
| P09 | Consulta de compra y reintegros | Se devuelven los datos y estados correspondientes. |
| P10 | Errores de comunicación con otros microservicios | Se manejan los errores sin dejar estados inconsistentes. |

Estos escenarios son propuestas de ampliación, no pruebas existentes.

## 6. Ejecución de pruebas y evidencia

Para ejecutar las pruebas unitarias desde el directorio de `msvc-compras`:

```bash
mvn test
```

La clase `PagoConcurrenciaMySQLIT` requiere una base MySQL de pruebas disponible en `localhost:3306`, con el nombre `ticketya_compras_test`.

Para ejecutarla explícitamente con Maven:

```bash
mvn -Dtest=PagoConcurrenciaMySQLIT test
```

La clase utiliza `@SpringBootTest` y tiene el sufijo `IT`; por lo tanto, no debe suponerse que se ejecuta automáticamente con el conjunto habitual de pruebas unitarias. Se debe invocar explícitamente o configurar un mecanismo de ejecución de pruebas de integración.

### Evidencia a registrar

Por cada ejecución se recomienda registrar:

- Fecha y hora.
- Comando utilizado.
- Resultado de Maven.
- Cantidad de pruebas ejecutadas, aprobadas y fallidas.
- Mensajes de error, si existen.
- Versión o commit del código.
- Estado de la base de datos de pruebas, cuando corresponda.

Las pruebas pendientes o fallidas deben mantenerse identificadas como tales, sin marcarlas como aprobadas.

## 7. Conclusión

`msvc-compras` cuenta con pruebas unitarias para las principales operaciones de compra, pago y reintegro, además de pruebas específicas de concurrencia sobre MySQL para prevenir cobros duplicados.

La cobertura actual permite verificar varias reglas importantes del negocio, especialmente la idempotencia y la consistencia de los reintegros.

Como próximos pasos se recomienda ampliar las pruebas de rechazo de pagos, recuperación ante fallos, validación de reservas vencidas e integración HTTP entre microservicios, y registrar los resultados de ejecución reales.