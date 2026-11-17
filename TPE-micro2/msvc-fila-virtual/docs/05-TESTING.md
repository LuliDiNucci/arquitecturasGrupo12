# 05 — Pruebas y testing de Fila Virtual

## 1. Descripción general

El microservicio `msvc-fila-virtual` cuenta con pruebas automatizadas para verificar las reglas de negocio, la gestión de turnos, la concurrencia y la inicialización de la aplicación.

Las pruebas permiten comprobar el ingreso de clientes a filas virtuales, la prevención de registros duplicados, el respeto del orden de llegada, la habilitación de tandas y el control de autorizaciones temporales.

La implementación utiliza JUnit 5, Mockito, Spring Boot Test y Spring Data JPA.

La suite contiene **13 métodos de prueba automatizados**, distribuidos en:

- 10 pruebas unitarias de lógica de negocio.
- 2 pruebas de concurrencia con persistencia.
- 1 prueba de inicialización del contexto de Spring Boot.

## 2. Herramientas utilizadas

| Herramienta      | Utilización                                                                 |
| ---------------- | --------------------------------------------------------------------------- |
| JUnit 5          | Definición y ejecución de pruebas automatizadas.                            |
| Mockito          | Simulación de repositorios y dependencias externas.                         |
| Spring Boot Test | Inicialización del contexto y pruebas con componentes Spring.               |
| Spring Data JPA  | Persistencia de los registros de fila virtual.                              |
| MySQL            | Almacenamiento y control transaccional durante las pruebas de concurrencia. |
| Maven            | Compilación y ejecución de la suite de pruebas.                             |
| Postman          | Preparación y ejecución de pruebas HTTP de la API REST.                     |
| Swagger/OpenAPI  | Consulta y documentación interactiva de los endpoints.                      |

## 3. Pruebas unitarias

Las pruebas unitarias se implementan en:

`FilaVirtualServiceImplTest.java`

Esta clase utiliza JUnit 5 y Mockito para verificar las reglas de negocio de `FilaVirtualServiceImpl`.

Las dependencias externas se simulan mediante mocks, permitiendo comprobar el comportamiento del servicio de manera aislada.

### 3.1. Casos de prueba

| Método de prueba                              | Comportamiento verificado                                                                                   |
| --------------------------------------------- | ----------------------------------------------------------------------------------------------------------- |
| `ingresarNuevamenteConservaRegistroYPosicion` | Comprueba que un ingreso repetido devuelve el mismo registro y conserva la posición del cliente.            |
| `rechazaEventoSinAltaDemanda`                 | Verifica que no se permita ingresar a la fila de un evento que no está configurado como de alta demanda.    |
| `habilitaClientesRespetandoOrden`             | Comprueba que la habilitación seleccione a los primeros clientes en espera, respetando el orden de llegada. |
| `rechazaSegundaTandaSiExisteUnaActiva`        | Verifica que no se habilite una nueva tanda mientras exista otra vigente.                                   |
| `turnoVigenteAutorizaAlCliente`               | Comprueba que un turno habilitado y vigente autorice al cliente.                                            |
| `clienteEnEsperaNoEstaAutorizado`             | Verifica que un cliente que continúa esperando no esté autorizado.                                          |
| `turnoVencidoNoAutorizaAlCliente`             | Comprueba que un turno vencido sea rechazado y actualice su estado a `VENCIDO`.                             |
| `noPermiteFinalizarTurnoVencido`              | Verifica que no se permita finalizar un turno cuyo período de validez terminó.                              |
| `finalizarTurnoEsIdempotente`                 | Comprueba que una finalización repetida conserve el estado `FINALIZADO`.                                    |
| `turnoFinalizadoNoAutorizaNuevamente`         | Verifica que un turno finalizado no pueda utilizarse nuevamente.                                            |

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
- Prevención de utilización de turnos finalizados.
- Idempotencia de la finalización.

## 4. Pruebas de concurrencia

Las pruebas de concurrencia se implementan en:

`FilaVirtualConcurrenciaTest.java`

Se ejecutan utilizando Spring Boot Test y la persistencia configurada para el perfil de pruebas.

La clase utiliza `ExecutorService`, `CountDownLatch` y `Future` para ejecutar solicitudes simultáneas.

Las operaciones de base de datos se realizan mediante Spring Data JPA.

### 4.1. Ingreso simultáneo del mismo cliente

**Método:**

`dosIngresosSimultaneosNoDuplicanCliente`

Se ejecutan dos solicitudes simultáneas para registrar al mismo cliente en la fila de un evento.

La prueba verifica que:

- Ambas solicitudes terminen sin errores.
- Ambas reciban una respuesta.
- Las dos respuestas contengan el mismo identificador de registro.
- La posición del cliente se conserve.
- Exista un único registro en la base de datos para la combinación de evento y cliente.

Esta prueba comprueba el funcionamiento de la restricción de unicidad y la operación de ingreso idempotente.

### 4.2. Habilitación simultánea de tandas

**Método:**

`dosHabilitacionesSimultaneasNoCreanDosTandas`

Se prepara una fila con seis clientes esperando.

Posteriormente, se ejecutan dos solicitudes simultáneas para habilitar tres clientes cada una.

La prueba verifica que:

- Solamente tres clientes sean habilitados.
- Los otros tres permanezcan en estado `EN_ESPERA`.
- Una de las solicitudes sea rechazada debido a la existencia de una tanda activa.
- Los clientes habilitados sean los primeros en ingresar a la fila.

Esta prueba comprueba el control de concurrencia durante la habilitación de turnos.

### 4.3. Mecanismos de protección

El microservicio utiliza diferentes mecanismos para proteger las operaciones concurrentes.

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

## 5. Prueba de inicialización

La clase:

`MsvcFilaVirtualApplicationTests.java`

incluye una prueba de inicialización mediante `@SpringBootTest`.

Esta prueba verifica que el contexto de Spring Boot pueda cargarse correctamente con la configuración correspondiente.

## 6. Pruebas HTTP mediante Postman

El microservicio dispone de una colección Postman para verificar el comportamiento de sus endpoints REST.

**Archivo:**

`postman/TicketYa-FilaVirtual.postman_collection.json`

La colección utiliza las siguientes variables:

| Variable            | Descripción                                                  |
| ------------------- | ------------------------------------------------------------ |
| `baseUrl`           | Dirección base del microservicio.                            |
| `eventoId`          | Identificador del evento utilizado para las pruebas.         |
| `clienteId`         | Identificador del primer cliente.                            |
| `segundoClienteId`  | Identificador del segundo cliente.                           |
| `registroId`        | Identificador del registro generado para el primer cliente.  |
| `segundoRegistroId` | Identificador del registro generado para el segundo cliente. |
| `posicionOriginal`  | Posición inicial del primer cliente.                         |

### 6.1. Escenarios incluidos

| Caso | Escenario                                        | Resultado esperado                  |
| ---- | ------------------------------------------------ | ----------------------------------- |
| 01   | Ingresar un cliente a la fila.                   | HTTP 200 y estado `EN_ESPERA`.      |
| 02   | Repetir el ingreso del mismo cliente.            | Se devuelve el registro existente.  |
| 03   | Ingresar un segundo cliente.                     | Se crea un registro independiente.  |
| 04   | Consultar la posición del primer cliente.        | Se informa su posición actual.      |
| 05   | Validar un cliente en espera.                    | `autorizado = false`.               |
| 06   | Habilitar una tanda de un cliente.               | El cliente pasa a `HABILITADO`.     |
| 07   | Validar un turno habilitado.                     | `autorizado = true`.                |
| 08   | Intentar habilitar otra tanda activa.            | HTTP 409.                           |
| 09   | Finalizar un turno.                              | Estado `FINALIZADO`.                |
| 10   | Repetir la finalización.                         | Se conserva el estado `FINALIZADO`. |
| 11   | Validar un turno finalizado.                     | `autorizado = false`.               |
| 12   | Consultar un cliente inexistente.                | HTTP 404.                           |
| 13   | Intentar ingresar con un identificador inválido. | HTTP 400.                           |

La colección incluye verificaciones automáticas de los códigos HTTP y los estados devueltos por la API.

Los identificadores de los clientes se generan mediante variables de colección para facilitar ejecuciones independientes.

## 7. Ejecución de pruebas automatizadas

Las pruebas se ejecutan desde el directorio raíz del microservicio:

`TPE-micro2/msvc-fila-virtual`

### 7.1. Ejecutar todas las pruebas

```bash
mvn test
```

Este comando compila el código necesario y ejecuta los tests detectados por Maven.

### 7.2. Ejecutar las pruebas unitarias

```bash
mvn -Dtest=FilaVirtualServiceImplTest test
```

### 7.3. Ejecutar las pruebas de concurrencia

```bash
mvn -Dtest=FilaVirtualConcurrenciaTest test
```

### 7.4. Consultar los resultados

Maven genera los reportes de ejecución en:

`target/surefire-reports/`

Los reportes permiten consultar la cantidad de pruebas ejecutadas, sus resultados y los detalles de los errores detectados.

## 8. Configuración del entorno de pruebas

Las pruebas de integración utilizan el perfil `test` de Spring Boot.

La configuración del entorno se define mediante las propiedades correspondientes al proyecto.

Para las pruebas de concurrencia se utiliza MySQL, con una base de datos de pruebas independiente.

Las dependencias con otros microservicios, como Eventos y Lugares, se simulan mediante Mockito durante las pruebas automatizadas.

Esto permite verificar las reglas de negocio de Fila Virtual sin necesidad de ejecutar simultáneamente todos los componentes de TicketYa.

## 9. Resultados de las pruebas automatizadas

La suite automatizada se ejecutó mediante Maven y finalizó correctamente.

| Categoría                      | Pruebas | Resultado        |
| ------------------------------ | ------: | ---------------- |
| Pruebas unitarias de servicios |      10 | Aprobadas        |
| Pruebas de concurrencia        |       2 | Aprobadas        |
| Inicialización de Spring Boot  |       1 | Aprobada         |
| **Total**                      |  **13** | **13 aprobadas** |

El resultado general de la ejecución fue:

```text
BUILD SUCCESS
```

No se registraron fallas ni errores en la ejecución final de la suite.

## 10. Conclusión

Las pruebas automatizadas verifican las reglas principales de Fila Virtual, incluyendo el ingreso idempotente de clientes, la conservación de posiciones, el orden de habilitación, la vigencia de turnos y la prevención de operaciones concurrentes incompatibles.

Las pruebas unitarias permiten validar los comportamientos individuales de los servicios, mientras que las pruebas de concurrencia comprueban la consistencia de las operaciones sobre la base de datos.

La colección Postman complementa estas verificaciones mediante escenarios HTTP y comprobaciones automáticas de las respuestas de la API.
