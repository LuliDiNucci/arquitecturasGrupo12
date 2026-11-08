# 01 — Funcionalidades y user stories de Compras y Pagos

**Fuente:** historias de usuario provistas por el grupo para TicketYa, 2026.
**Estado del documento:** especificación funcional contrastada con la implementación actual de `msvc-compras`.
Este documento registra las funcionalidades solicitadas, su estado de implementación y la cobertura de pruebas identificada en el código. Los estados indicados corresponden a la revisión estática del proyecto; no implican que todas las pruebas hayan sido ejecutadas exitosamente.

## US01 — Iniciar una compra

**Como cliente**, quiero iniciar la compra de las entradas que reservé, para poder realizar el pago y adquirirlas.

**Criterios de aceptación:**
- El cliente debe tener una reserva temporal vigente.
- El sistema debe consultar al microservicio Reservas para verificar que la reserva exista y esté activa.
- Se debe registrar la compra asociada al cliente y a la reserva.
- Se debe calcular el importe total según las entradas reservadas y los precios correspondientes.
- La compra debe crearse en estado `PENDIENTE`.
- No se debe permitir iniciar una compra sobre una reserva vencida o cancelada.
- Una misma reserva no debe generar múltiples compras activas.

**Estado de implementación:** IMPLEMENTADA.

**Implementación:**
- `CompraServiceImpl.iniciarCompra()` obtiene la reserva mediante `ReservaClient`.
- Verifica que la reserva exista, pertenezca al cliente, esté activa y no haya vencido.
- Rechaza reservas que ya tienen una compra asociada.
- Consulta los precios de los sectores mediante `EventoClient`.
- Calcula los subtotales y el importe total.
- Registra la compra en estado `PENDIENTE`.

**Cobertura de pruebas:**
- `CompraServiceImplTest.iniciarCompraCalculaTotalYRegistraItems()`: verifica el cálculo del importe y el registro de   los items.
- `CompraServiceImplTest.noPermitirCompraDuplicadaPorReserva()`: verifica que una reserva no genere más de una compra.

## US02 — Registrar el detalle de una compra

**Como cliente**, quiero que se registre el detalle de las entradas que estoy comprando, para conocer el importe y los lugares seleccionados.

**Criterios de aceptación:**
- Cada compra debe tener uno o varios detalles.
- Cada detalle debe identificar el evento, sector y cantidad de entradas.
- Si corresponde, deben registrarse los asientos seleccionados.
- Se debe registrar el precio unitario correspondiente al momento de iniciar la compra.
- El importe total debe coincidir con la suma de los subtotales.
- El detalle debe conservarse para futuras consultas y devoluciones.

**Estado de implementación:** IMPLEMENTADA.

**Implementación:**
- Las entidades `Compra`, `DetalleCompra` e `ItemCompra` representan la compra, sus detalles y cada entrada individual.
- `DetalleCompra` registra evento, sector, cantidad, precio unitario y subtotal.
- `ItemCompra` permite registrar individualmente cada lugar adquirido y su asiento cuando corresponde.
- El precio unitario se conserva en el detalle de compra, independientemente de futuras modificaciones de precios.
- El importe total de la compra se calcula a partir de los subtotales.

**Cobertura de pruebas:**
- `CompraServiceImplTest.iniciarCompraCalculaTotalYRegistraItems()`: verifica el cálculo del total y la generación de los items individuales.
- La consulta de compra se implementa mediante `CompraServiceImpl.obtenerCompra()`, que devuelve la información de la compra y sus detalles.

## US03 — Procesar el pago de una compra

**Como cliente**, quiero pagar las entradas que reservé mediante un medio de pago disponible, para confirmar mi compra.

**Criterios de aceptación:**
- El cliente debe seleccionar un medio de pago.
- El sistema debe registrar el medio de pago utilizado, sin almacenar datos sensibles como el número completo de tarjeta.
- El pago debe procesarse mediante una pasarela de pagos simulada (mock).
- El sistema debe registrar el importe, la fecha y el resultado del pago.
- Si el pago es aprobado, debe registrarse como `APROBADO`.
- Si es rechazado, debe registrarse como `RECHAZADO`.
- No se debe procesar el pago si la reserva ya está vencida o cancelada.

**Estado de implementación:** IMPLEMENTADA.

**Implementación:**
- `PagoServiceImpl.procesarPago()` valida la solicitud y el medio de pago.
- Verifica que la compra esté pendiente y que la reserva continúe activa y vigente.
- Utiliza `PasarelaPagoMock` para simular el procesamiento del pago.
- Registra el importe, medio de pago, fechas, estado y referencia externa.
- Conserva la clave de idempotencia asociada al pago.
- Si el pago es aprobado, la compra pasa a `CONFIRMACION_PENDIENTE`.
- Si el pago es rechazado, pasa a `LIBERACION_PENDIENTE` hasta completar la liberación de la reserva.

**Cobertura de pruebas:**
- `PagoServiceImplTest`: contiene escenarios de idempotencia, reutilización de claves con datos diferentes, uso de una misma clave en compras distintas y rechazo de un segundo pago.
- La validación de los distintos medios de pago y la respuesta de la pasarela también se encuentran implementadas.

## US04 — Confirmar una compra aprobada

**Como cliente**, quiero que mi compra se confirme cuando el pago sea aprobado, para obtener mis entradas.

**Criterios de aceptación:**
- El pago debe encontrarse aprobado.
- La compra debe pasar al estado `CONFIRMADA`.
- El microservicio Reservas debe confirmar los lugares reservados.
- El microservicio Entradas debe emitir una entrada por cada lugar adquirido, con su QR único.
- La compra debe quedar asociada a las entradas emitidas.
- No se deben generar entradas duplicadas ante solicitudes repetidas.
- Si no se logra completar la confirmación de la reserva o la emisión de entradas, la compra no debe quedar registrada como completada exitosamente sin resolver esa situación.

**Estado de implementación:** IMPLEMENTADA, con manejo de confirmación pendiente y compensación ante fallas.

**Implementación:**
- `CompraServiceImpl.confirmarCompra()` verifica que exista un pago aprobado.
- Comprueba que el evento siga habilitado y que la reserva pueda confirmarse.
- Solicita la confirmación de los lugares a `ReservaClient`.
- Una vez confirmada la reserva, solicita la emisión de entradas a `EntradaClient`.
- Verifica que se hayan recibido todas las entradas esperadas y que sus identificadores y códigos QR sean válidos y únicos.
- Asocia cada entrada emitida con su correspondiente `ItemCompra`.
- La compra pasa a `CONFIRMADA` únicamente cuando se completa la emisión.
- Si la reserva ya no puede confirmarse, la compra pasa a `COMPENSACION_PENDIENTE`.
- Los reintentos no vuelven a emitir entradas de una compra ya completada.

**Cobertura de pruebas:**
- `CompraServiceImplTest.confirmarCompraSoloCuandoEstanTodasLasEntradas()`: verifica que la compra solamente se confirme cuando se reciben todas las entradas.
- La lógica de reintentos e idempotencia también se encuentra implementada en la comunicación con Entradas.

## US05 — Gestionar un pago rechazado

**Como cliente**, quiero recibir información cuando mi pago sea rechazado, para saber que la compra no pudo completarse.

**Criterios de aceptación:**
- Cuando la pasarela simulada rechace el pago, debe registrarse el resultado.
- La compra debe pasar al estado `RECHAZADA`.
- El microservicio Reservas debe recibir la solicitud para liberar los lugares reservados.
- Los lugares deben volver a estar disponibles si no fueron vendidos y el evento continúa habilitado.
- No se deben emitir entradas.
- El cliente debe recibir una respuesta indicando que la operación fue rechazada.

**Estado de implementación:** IMPLEMENTADA.

**Implementación:**
- `PagoServiceImpl` registra el pago rechazado y cambia la compra a `LIBERACION_PENDIENTE`.
- Intenta liberar la reserva mediante `ReservaClient`.
- Si la liberación falla, conserva el estado pendiente para permitir un nuevo intento.
- `PagoServiceImpl.reintentarLiberacion()` permite reintentar la operación.
- Una vez completada la liberación, la compra pasa al estado `RECHAZADA`.
- No se solicita la emisión de entradas para un pago rechazado.

**Cobertura de pruebas:**
- No se identificó una prueba específica que cubra de manera integral el rechazo del pago y la liberación de la reserva.
- La operación de reintento está implementada, pero su cobertura debe verificarse mediante pruebas adicionales.

## US06 — Evitar cobros duplicados

**Como cliente**, quiero que los reintentos de pago no generen cobros duplicados, para no pagar dos veces por las mismas entradas.

**Criterios de aceptación:**
- Cada operación de pago debe contar con una clave de idempotencia.
- Si el cliente repite una solicitud con la misma clave y los mismos datos, no debe generarse un nuevo cobro.
- El sistema debe recuperar el resultado de la operación original.
- Si se reutiliza la misma clave con datos diferentes, debe rechazarse la solicitud.
- Una compra confirmada no debe volver a cobrarse.
- Las solicitudes simultáneas del mismo pago no deben producir múltiples cobros.

**Estado de implementación:** IMPLEMENTADA.

**Implementación:**
- Cada pago registra una clave de idempotencia única.
- `PagoServiceImpl` recupera el resultado anterior cuando se repite una solicitud con la misma clave y los mismos datos.
- Rechaza la reutilización de una clave con datos diferentes o asociada a otra compra.
- Utiliza bloqueo de la compra mediante `CompraRepository.bloquearCompra()` para controlar solicitudes concurrentes.
- La persistencia de la clave de idempotencia permite evitar el procesamiento duplicado.

**Cobertura de pruebas:**
- `PagoServiceImplTest.testPagoIdempotente()`: verifica que repetir una solicitud no genere otro cobro.
- `PagoServiceImplTest.testClaveReutilizadaConDatosDiferentes()`: verifica el rechazo de una clave utilizada con otros datos.
- `PagoServiceImplTest.testClaveUtilizadaEnOtraCompra()`: verifica el rechazo de una clave asociada a otra compra.
- `PagoServiceImplTest.testNoPermitirSegundoPago()`: verifica que una compra no pueda volver a pagarse.
- `PagoConcurrenciaMySQLIT.dosSolicitudesMismaCompraSoloUnCobro()`: verifica solicitudes concurrentes sobre una misma compra.
- `PagoConcurrenciaMySQLIT.mismaClaveEnDosComprasDistintasSoloUnCobro()`: verifica la reutilización concurrente de una clave en compras diferentes.

## US07 — Procesar el reintegro de una entrada devuelta

**Como cliente**, quiero recibir el reintegro de mi dinero cuando se aprueba la devolución de una entrada, para recuperar el importe correspondiente.

**Criterios de aceptación:**
- El microservicio Entradas debe informar que una devolución fue aprobada.
- Debe verificarse que la entrada corresponda a una compra pagada.
- El sistema debe calcular el importe a reintegrar según el precio efectivamente abonado por esa entrada.
- Se debe solicitar el reintegro a la pasarela de pagos simulada.
- El reintegro debe registrar importe, fecha y estado.
- Una misma devolución no debe generar reintegros duplicados.
- Si la compra contiene varias entradas, debe poder reintegrarse solamente la entrada devuelta.

**Estado de implementación:** IMPLEMENTADA.

**Implementación:**
- `ReintegroServiceImpl.reintegrarEntrada()` busca el item de compra asociado a la entrada.
- Verifica que la entrada tenga una devolución aprobada mediante `EntradaClient`.
- Comprueba que la compra tenga un pago aprobado.
- Obtiene el importe a reintegrar a partir del precio histórico del item de compra.
- Procesa el reintegro mediante `PasarelaPagoMock`.
- Registra el importe, fecha, estado y referencia externa.
- Evita reintegros duplicados mediante la asociación con el item y la entrada.
- Actualiza el estado de la compra según los reintegros realizados, permitiendo reintegros parciales.

**Cobertura de pruebas:**
- `ReintegroServiceImplTest.devolucionIndividualUsaPrecioCongeladoYEvitaduplicados()`: verifica el uso del precio histórico y la prevención de reintegros duplicados.
- `ReintegroServiceImplTest.devolverUnaDeDosEntradasDejaReintegroParcial()`: verifica que pueda reintegrarse una entrada sin reintegrar las restantes.

## US08 — Gestionar reintegros por cancelación de un evento

**Como administrador**, quiero que se gestionen los reintegros de las entradas de un evento cancelado, para devolver el dinero a los compradores afectados.

**Criterios de aceptación:**
- El microservicio debe recibir la información de que un evento fue cancelado.
- Se deben identificar las compras pagadas correspondientes al evento.
- Se debe determinar qué importes corresponde reintegrar, evitando reintegrar nuevamente entradas ya devueltas.
- Se debe solicitar el reintegro de cada importe a la pasarela simulada.
- Se debe registrar el estado de cada reintegro.
- No deben procesarse reintegros duplicados si la cancelación se informa más de una vez.
- La compra debe reflejar si sus entradas fueron reintegradas total o parcialmente.

**Estado de implementación:** IMPLEMENTADA.

**Implementación:**
- `ReintegroServiceImpl.reintegrarEventoCancelado()` verifica que el evento se encuentre cancelado mediante `EventoClient`.
- Solicita primero la invalidación de las entradas mediante `EntradaClient`.
- Identifica las compras asociadas al evento.
- Procesa los reintegros de cada compra en transacciones independientes.
- Utiliza los items originales para calcular los importes y evitar reintegros duplicados.
- Permite reintentar la operación si quedan compras pendientes de reintegro.
- También contempla compras pagadas cuya emisión de entradas quedó pendiente.
- Actualiza el estado de las compras según los reintegros realizados.

**Cobertura de pruebas:**
- `ReintegroServiceImplTest.cancelarEventoDosVecesNoDuplicaReintegros()`: verifica que repetir la cancelación no genere reintegros duplicados.
- `ReintegroServiceImplTest.cancelarEventoReintegraCompraSinEntradasEmitidas()`: verifica el tratamiento de compras pagadas que todavía no tienen entradas emitidas.

## Correspondencia con la consigna

- **Funcionalidad 4:** se implementa el flujo de reserva temporal, inicio de compra, registro del detalle, consulta de precios, procesamiento del pago y emisión de entradas con QR mediante la integración con los microservicios correspondientes.
- **Funcionalidad 5:** se implementan mecanismos de idempotencia y bloqueo para evitar compras y cobros duplicados.
- **Funcionalidad 7:** se implementan reintegros individuales por devolución aprobada y reintegros asociados a la cancelación de eventos.
- **Entrega 1:** se encuentran implementados los servicios REST, la pasarela de pagos simulada, las integraciones entre microservicios y pruebas unitarias y de concurrencia. La documentación de contratos, Swagger y resultados de ejecución de pruebas se registra en los documentos correspondientes.
- **Entrega 2:** contempla funcionalidades y aspectos adicionales como JWT, Docker, logging centralizado y resiliencia avanzada, según el alcance definido para esa entrega.

## Resumen de cobertura

| Historia | Estado de implementación | Cobertura identificada |

| US01     | Implementada             | Pruebas de inicio de compra y prevención de duplicados |
| US02     | Implementada             | Prueba de cálculo del total y registro de items |
| US03     | Implementada             | Pruebas de idempotencia y validación de pagos |
| US04     | Implementada             | Prueba de confirmación con todas las entradas |
| US05     | Implementada             | Sin prueba integral específica identificada |
| US06     | Implementada             | Pruebas unitarias y de concurrencia |
| US07     | Implementada             | Pruebas de reintegro individual y parcial |
| US08     | Implementada             | Pruebas de cancelación repetida y compras sin entradas emitidas |

**Nota sobre la ejecución:** la cobertura indicada corresponde a métodos de prueba encontrados en el código fuente. Los resultados de ejecución deben registrarse una vez ejecutada la suite de pruebas en el entorno correspondiente.