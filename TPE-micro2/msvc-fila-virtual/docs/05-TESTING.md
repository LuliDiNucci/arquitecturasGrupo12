# 05 — Pruebas y testing de Fila Virtual

## 1. Descripción general

El microservicio `msvc-fila-virtual` cuenta con pruebas automatizadas para verificar las reglas de negocio, la gestión de turnos, la concurrencia, la seguridad y la inicialización de la aplicación.

Las pruebas de integración verifican la comunicación entre Fila Virtual, Reservas y los demás componentes involucrados en el control de acceso a eventos de alta demanda.

Las pruebas permiten verificar:

- El ingreso de clientes a filas virtuales.
- La prevención de registros duplicados.
- El respeto del orden de llegada.
- La habilitación de tandas.
- La vigencia de autorizaciones temporales.
- El vencimiento de turnos.
- La finalización idempotente.
- El control de concurrencia.
- La autenticación y autorización mediante JWT.
- La integración con Reservas.
- La prevención de reutilización de turnos finalizados.

## 2. Herramientas utilizadas

| Herramienta | Utilización |
|---|---|
| JUnit 5 | Definición y ejecución de pruebas automatizadas. |
| Mockito | Simulación de repositorios y dependencias externas. |
| Spring Boot Test | Pruebas con componentes y contexto de Spring. |
| Spring Security Test | Verificación de restricciones de autenticación y autorización. |
| MockMvc | Ejecución de pruebas HTTP de controladores y seguridad. |
| Spring Data JPA | Persistencia de registros de fila virtual. |
| MySQL | Almacenamiento y control transaccional. |
| Maven | Compilación y ejecución de pruebas. |
| Docker Compose | Ejecución conjunta de microservicios y bases de datos. |
| PowerShell | Ejecución de solicitudes HTTP y validación de respuestas. |
| Postman | Ejecución de pruebas de la API REST. |
| Swagger/OpenAPI | Documentación y consulta de los contratos REST. |

## 3. Pruebas unitarias

Las pruebas de lógica de negocio se implementan en:

`FilaVirtualServiceImplTest.java`

Se utiliza JUnit 5 y Mockito para verificar las operaciones de `FilaVirtualServiceImpl`.

Las dependencias externas se simulan mediante mocks, permitiendo comprobar las reglas del servicio de forma aislada.

### 3.1. Casos de prueba

Entre los escenarios implementados se encuentran:

| Método de prueba | Comportamiento verificado |
|---|---|
| `ingresarNuevamenteConservaRegistroYPosicion` | Un ingreso repetido devuelve el mismo registro y conserva su posición. |
| `rechazaEventoSinAltaDemanda` | No se permite ingresar a la fila de un evento que no requiere fila virtual. |
| `habilitaClientesRespetandoOrden` | Se seleccionan los primeros clientes en espera. |
| `rechazaSegundaTandaSiExisteUnaActiva` | No se permite una nueva tanda mientras exista otra vigente. |
| `turnoVigenteAutorizaAlCliente` | Un turno habilitado y vigente autoriza al cliente. |
| `clienteEnEsperaNoEstaAutorizado` | Un cliente en espera no está autorizado para reservar. |
| `turnoVencidoNoAutorizaAlCliente` | Un turno vencido no autoriza nuevas reservas. |
| `finalizarTurnoEsIdempotente` | Una finalización repetida conserva el estado `FINALIZADO`. |
| `turnoFinalizadoNoAutorizaNuevamente` | Un turno finalizado no puede reutilizarse. |
| `permiteFinalizarTurnoVencidoPorConfirmacionDeReserva` | Permite cerrar un turno vencido mediante una finalización asíncrona tardía. |

### 3.2. Reglas de negocio verificadas

Las pruebas unitarias comprueban:

- Idempotencia del ingreso de clientes.
- Conservación de la posición original.
- Validación de eventos de alta demanda.
- Respeto del orden de llegada.
- Prevención de tandas activas superpuestas.
- Autorización de turnos vigentes.
- Rechazo de clientes que continúan esperando.
- Detección de turnos vencidos.
- Prevención de reutilización de turnos finalizados.
- Idempotencia de la finalización.
- Finalización tardía de turnos vencidos cuando corresponde a una reserva previamente creada.

La finalización de un turno vencido no implica que pueda utilizarse para crear una nueva reserva.

## 4. Pruebas de concurrencia

Las pruebas de concurrencia se implementan en:

`FilaVirtualConcurrenciaTest.java`

Se ejecutan utilizando Spring Boot Test y la persistencia configurada para el entorno de pruebas.

La clase utiliza mecanismos de ejecución concurrente para iniciar operaciones simultáneas y verificar sus resultados.

### 4.1. Ingreso simultáneo del mismo cliente

**Método:**

`dosIngresosSimultaneosNoDuplicanCliente`

Se ejecutan dos solicitudes simultáneas para registrar al mismo cliente en la fila de un evento.

La prueba verifica:

- Que las solicitudes no generen registros duplicados.
- Que ambas operaciones hagan referencia al mismo registro.
- Que se conserve la posición del cliente.
- Que exista un único registro para la combinación de evento y cliente.

Esta prueba verifica el funcionamiento de la restricción de unicidad y la operación de ingreso idempotente.

### 4.2. Habilitación simultánea de tandas

**Método:**

`dosHabilitacionesSimultaneasNoCreanDosTandas`

Se prepara una fila con clientes esperando y se ejecutan solicitudes simultáneas de habilitación.

La prueba verifica:

- Que no se generen dos tandas activas incompatibles.
- Que se respete el orden de ingreso.
- Que el bloqueo pesimista coordine las operaciones concurrentes.
- Que los clientes no seleccionados permanezcan esperando.

### 4.3. Mecanismos de protección

**Ingreso de clientes:**

- Restricción única sobre `evento_id` y `cliente_id`.
- Inserción idempotente mediante MySQL.
- Recuperación del registro existente ante solicitudes repetidas.

**Habilitación de tandas:**

- Operaciones transaccionales.
- Bloqueo pesimista mediante JPA.
- Verificación de turnos habilitados y vigentes.
- Selección ordenada de clientes en espera.

Estos mecanismos permiten mantener la consistencia cuando se reciben solicitudes simultáneas.

## 5. Pruebas de seguridad

Las pruebas de seguridad verifican las restricciones de acceso configuradas mediante Spring Security y JWT.

Se incluyen escenarios de autenticación y autorización para los distintos tipos de usuarios y operaciones.

### 5.1. Autenticación

Se verifica que los endpoints protegidos requieran una identidad autenticada.

Las solicitudes sin un JWT válido deben rechazarse según las reglas de seguridad del servicio.

### 5.2. Autorización por roles

Se verifica que los permisos dependan de la identidad autenticada.

Entre los escenarios implementados se encuentran:

| Método de prueba | Comportamiento verificado |
|---|---|
| `clienteNoPuedeHabilitarTandas` | Un cliente no puede habilitar tandas. |
| `administradorPuedeHabilitarTandas` | Un administrador tiene permisos para habilitar tandas. |

También se comprueba la protección de los endpoints correspondientes a clientes y operaciones internas.

### 5.3. Protección de las operaciones internas

La validación y finalización de turnos están destinadas a la comunicación interna con Reservas.

Estas operaciones utilizan autenticación mediante credenciales de servicio.

La separación de permisos evita que un cliente pueda finalizar directamente su turno para modificar el proceso de reserva.

## 6. Prueba de inicialización

La clase:

`MsvcFilaVirtualApplicationTests.java`

incluye una prueba de inicialización mediante `@SpringBootTest`.

Esta prueba verifica que el contexto de Spring Boot pueda cargarse correctamente con la configuración correspondiente.

## 7. Pruebas HTTP mediante Postman

El microservicio dispone de una colección Postman para verificar el comportamiento de sus endpoints REST.

**Archivo:**

`postman/TicketYa-FilaVirtual.postman_collection.json`

La colección utiliza variables para configurar la dirección del servicio y los identificadores necesarios durante las pruebas.

### 7.1. Escenarios funcionales

Los escenarios contemplados en la colección incluyen:

| Caso | Escenario | Resultado esperado |
|---|---|---|
| 01 | Ingresar un cliente a la fila. | HTTP 200 y estado `EN_ESPERA`. |
| 02 | Repetir el ingreso del mismo cliente. | Se devuelve el registro existente. |
| 03 | Ingresar un segundo cliente. | Se crea un registro independiente. |
| 04 | Consultar la posición del primer cliente. | Se informa su posición actual. |
| 05 | Validar un cliente en espera. | `autorizado = false`. |
| 06 | Habilitar una tanda. | El cliente pasa a `HABILITADO`. |
| 07 | Validar un turno habilitado. | `autorizado = true`. |
| 08 | Intentar habilitar otra tanda activa. | HTTP 409. |
| 09 | Finalizar un turno. | Estado `FINALIZADO`. |
| 10 | Repetir la finalización. | Se conserva `FINALIZADO`. |
| 11 | Validar un turno finalizado. | `autorizado = false`. |
| 12 | Consultar un cliente inexistente. | HTTP 404. |
| 13 | Intentar ingresar con un identificador inválido. | HTTP 400. |

Los escenarios protegidos requieren configurar las credenciales JWT correspondientes.

Las operaciones internas de validación y finalización requieren una identidad de servicio autorizada.

## 8. Pruebas de integración con Reservas

Las pruebas de integración verifican la coordinación entre Fila Virtual y Reservas para controlar el acceso a eventos de alta demanda.

El entorno de ejecución utiliza Docker Compose y los siguientes componentes:

- API Gateway.
- Usuarios.
- Eventos y Lugares.
- Fila Virtual.
- Reservas.
- Bases de datos MySQL independientes.

La comunicación se realiza mediante HTTP y los mecanismos de autenticación y autorización JWT definidos para el sistema.

### 8.1. Escenarios de integración

| Caso | Escenario | Resultado esperado |
|---|---|---|
| INT-01 | Un cliente ingresa a la fila de un evento publicado de alta demanda. | Se registra en estado `EN_ESPERA` y recibe una posición. |
| INT-02 | Un cliente intenta reservar sin tener un turno habilitado. | Reservas rechaza la solicitud con HTTP 409. |
| INT-03 | Un administrador habilita una tanda. | Los clientes seleccionados pasan a `HABILITADO`. |
| INT-04 | Un cliente con turno vigente solicita una reserva temporal. | Reservas valida la autorización y permite crear la reserva. |
| INT-05 | Se crea correctamente una reserva temporal. | Reservas registra la finalización pendiente del turno. |
| INT-06 | Se procesa la finalización pendiente. | Fila Virtual actualiza el turno a `FINALIZADO`. |
| INT-07 | Un cliente intenta crear otra reserva con un turno finalizado. | Reservas rechaza la solicitud con HTTP 409. |
| INT-08 | Se recibe nuevamente la finalización de un turno finalizado. | Se conserva el estado `FINALIZADO` sin efectos duplicados. |

### 8.2. Validación de autorizaciones

Para eventos de alta demanda, Reservas consulta el endpoint:

`GET /filas/{eventoId}/clientes/{clienteId}/validar`

La respuesta indica si el cliente está autorizado para continuar con la creación de una reserva.

La autorización solamente es válida si el turno está en estado `HABILITADO` y no ha vencido.

Los turnos en estado `EN_ESPERA`, `VENCIDO` o `FINALIZADO` no permiten crear nuevas reservas.

### 8.3. Finalización asíncrona

Después de crear correctamente una reserva temporal, Reservas registra una operación pendiente de finalización del turno.

El procesamiento se realiza de forma asíncrona mediante:

`POST /filas/{eventoId}/clientes/{clienteId}/finalizar`

Ante fallos temporales de comunicación, Reservas conserva la operación pendiente y puede reintentar su ejecución.

Fila Virtual admite finalizaciones repetidas de manera idempotente.

También permite finalizar un turno vencido cuando recibe tardíamente la finalización correspondiente a una reserva creada con autorización vigente.

### 8.4. Consistencia entre microservicios

La integración mantiene las siguientes reglas:

- Fila Virtual administra las autorizaciones temporales.
- Reservas administra la disponibilidad y creación de reservas.
- La validación del turno no consume la autorización.
- La finalización se solicita después de crear correctamente la reserva.
- Un turno finalizado no permite crear nuevas reservas.
- Los reintentos de finalización no deben provocar efectos duplicados.
- Cada microservicio mantiene su propia base de datos.
  
## 9. Ejecución de pruebas automatizadas

Las pruebas se ejecutan desde el directorio:

`TPE-micro2/msvc-fila-virtual`

### 9.1. Ejecutar todas las pruebas

```bash
mvn clean test
```

Este comando compila el código y ejecuta las pruebas detectadas por Maven.

### 9.2. Ejecutar las pruebas unitarias

```bash
mvn -Dtest=FilaVirtualServiceImplTest test
```

### 9.3. Ejecutar las pruebas de concurrencia

```bash
mvn -Dtest=FilaVirtualConcurrenciaTest test
```

### 9.4. Consultar los resultados

Maven genera reportes de ejecución en:

`target/surefire-reports/`

Estos reportes permiten consultar la cantidad de pruebas ejecutadas, sus resultados y los detalles de los errores detectados.

## 10. Configuración del entorno de pruebas

Las pruebas automatizadas utilizan la configuración correspondiente a los perfiles de pruebas definidos en el proyecto.

Las pruebas unitarias emplean Mockito para simular dependencias.

Las pruebas de concurrencia utilizan persistencia y mecanismos transaccionales para verificar el comportamiento ante operaciones simultáneas.

Las pruebas de integración HTTP se realizaron con los microservicios ejecutados mediante Docker Compose.

La comunicación entre servicios se realizó utilizando sus direcciones internas de Docker.

## 11. Resultados de las pruebas

La ejecución de las pruebas automatizadas mediante Maven finaliza correctamente.

```text
BUILD SUCCESS
```

Las pruebas comprueban las principales reglas de negocio relacionadas con el ingreso de clientes, la asignación de posiciones, la habilitación de tandas, el vencimiento de autorizaciones, la concurrencia y la seguridad.

Las pruebas de integración verifican el comportamiento conjunto de Fila Virtual y Reservas.

| Funcionalidad | Resultado |
|---|---|
| Ingreso idempotente a la fila | Aprobado |
| Asignación y consulta de posiciones | Aprobado |
| Habilitación ordenada de clientes | Aprobado |
| Control de tandas concurrentes | Aprobado |
| Validación de autorizaciones vigentes | Aprobado |
| Rechazo de reservas sin autorización | Aprobado |
| Creación de reservas con turno vigente | Aprobado |
| Finalización asíncrona de turnos | Aprobado |
| Prevención de reutilización de turnos finalizados | Aprobado |
| Autenticación y autorización | Aprobado |

Los resultados de las pruebas automatizadas pueden consultarse en los reportes generados por Maven:

`target/surefire-reports/`

## 12. Conclusión

Las pruebas automatizadas verifican las reglas principales de Fila Virtual, incluyendo el ingreso idempotente de clientes, la conservación de posiciones, el orden de habilitación, la vigencia de turnos, la seguridad y la prevención de operaciones concurrentes incompatibles.

Las pruebas de integración verificaron que Reservas utiliza correctamente las autorizaciones temporales de Fila Virtual.

Se comprobó que un cliente sin turno vigente no puede reservar, que una autorización válida permite crear una reserva y que la finalización posterior impide reutilizar el turno.

La separación de responsabilidades se mantiene: Fila Virtual administra las autorizaciones temporales y Reservas administra la creación y persistencia de reservas.