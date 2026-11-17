# 05 — Pruebas y testing de Reservas

## 1. Descripción general

El microservicio `msvc-reservas` cuenta con pruebas orientadas a verificar sus principales reglas de negocio, la persistencia, la idempotencia, la concurrencia y el comportamiento de los endpoints REST.

La estrategia de testing comprende tres niveles:

- **Pruebas unitarias:** verifican las reglas de negocio de los servicios utilizando dependencias simuladas.
- **Pruebas de integración con MySQL:** comprueban la persistencia y el control de concurrencia sobre una base de datos de pruebas.
- **Pruebas manuales:** permiten comprobar los flujos HTTP, las transiciones de estado y el comportamiento observable de las reservas.

Estas pruebas permiten evaluar la creación, confirmación, liberación, vencimiento y cancelación de reservas, así como la prevención de operaciones duplicadas.

## 2. Herramientas utilizadas

| Herramienta | Uso |
|---|---|
| JUnit 5 | Ejecución de pruebas automatizadas. |
| Mockito | Simulación de dependencias y verificación de interacciones. |
| Spring Boot Test | Configuración del contexto de pruebas. |
| Spring Data JPA | Persistencia y acceso a repositorios. |
| MySQL | Persistencia y pruebas de concurrencia. |
| Maven | Compilación y ejecución de pruebas. |
| PowerShell | Envío de solicitudes HTTP y comprobaciones manuales. |
| Postman | Ejecución de pruebas sobre los endpoints REST. |
| Simulador de Eventos y Lugares | Simulación de respuestas HTTP durante las pruebas locales de Reservas. |

## 3. Pruebas unitarias

### 3.1. ReservaServiceImplTest

Esta clase verifica las principales reglas de negocio del servicio de reservas.

| Caso de prueba | Comportamiento verificado |
|---|---|
| `rechazaSolicitudIncompletaSinGuardar` | Una solicitud incompleta se rechaza sin persistir una reserva. |
| `rechazaReservaSiEventoCanceladoLocalmente` | No se permite crear reservas para un evento cancelado. |
| `noConfirmaReservaVencida` | Una reserva vencida no puede confirmarse. |
| `cancelacionMarcaElEventoYCancelaActivas` | La cancelación registra el evento como cancelado y actualiza las reservas temporales activas. |

Las pruebas utilizan dependencias simuladas para verificar las reglas de negocio y las interacciones con los repositorios y servicios correspondientes.

### 3.2. Ejecución de pruebas unitarias

Desde el directorio raíz de `msvc-reservas`, con Java 21 y Maven instalados:

```powershell
mvn test
```

Este comando ejecuta las pruebas reconocidas por la configuración de Maven del proyecto.

## 4. Pruebas de concurrencia con MySQL

### 4.1. ConcurrenciaMySQLIT

La clase `ConcurrenciaMySQLIT` verifica el control de concurrencia cuando varios clientes intentan reservar simultáneamente un mismo asiento.

**Escenario de prueba:**

1. Se configura una base de datos MySQL exclusiva para pruebas.
2. Se prepara un evento y un asiento disponible.
3. Se ejecutan diez solicitudes concurrentes para reservar el mismo asiento.
4. Se comprueba que solamente una solicitud consiga reservar el lugar.
5. Las demás solicitudes deben rechazarse debido a la falta de disponibilidad o a un conflicto de concurrencia.

La prueba verifica el funcionamiento del bloqueo pesimista `PESSIMISTIC_WRITE` sobre `bloqueo_evento`, junto con las transacciones utilizadas para controlar la ocupación.

### 4.2. Configuración de la base de pruebas

Para ejecutar la prueba se utiliza una base independiente de la base de desarrollo.

**Crear la base de datos:**

```sql
CREATE DATABASE IF NOT EXISTS ticketya_reservas_test;
```

**Configurar las variables de entorno en PowerShell:**

```powershell
$env:TICKETYA_MYSQL_TEST="true"
$env:TEST_DB_URL="jdbc:mysql://localhost:3306/ticketya_reservas_test"
$env:TEST_DB_USER="root"
$env:TEST_DB_PASSWORD="TU_CONTRASENA"
```

La contraseña debe reemplazarse por la correspondiente a la instalación local de MySQL.

**Ejecutar la prueba de concurrencia:**

```powershell
mvn "-Dtest=ConcurrenciaMySQLIT" test
```

La prueba prepara y limpia sus propios datos, por lo que debe ejecutarse exclusivamente sobre una base destinada a testing.

### 4.3. Resultado de la prueba

Durante las verificaciones realizadas con MySQL, el escenario de solicitudes simultáneas sobre el mismo asiento obtuvo el resultado esperado.

Solamente una solicitud consiguió reservar el asiento, mientras que las restantes no pudieron completar una reserva incompatible.

Esto permite comprobar el control transaccional de ocupación y el mecanismo de bloqueo sobre la base MySQL utilizada durante la prueba.

## 5. Pruebas manuales de endpoints REST

Las pruebas manuales permiten verificar el comportamiento HTTP y las operaciones principales del microservicio.

Se utilizan PowerShell y Postman para enviar solicitudes y consultar las respuestas de la API.

### 5.1. Escenarios funcionales

| Escenario | Comportamiento verificado |
|---|---|
| Crear una reserva temporal. | Se registra una reserva en estado `ACTIVA` con su fecha de vencimiento. |
| Consultar una reserva. | Se recupera la información de la reserva y su estado. |
| Consultar disponibilidad. | Se informa la cantidad de lugares disponibles y, para sectores numerados, los asientos disponibles y ocupados. |
| Confirmar una reserva. | Una reserva activa y vigente pasa al estado `CONFIRMADA`. |
| Liberar una reserva. | La reserva cambia a `LIBERADA` y sus lugares dejan de considerarse ocupados. |
| Verificar vencimiento. | Después de 10 minutos, los lugares de una reserva activa vencida dejan de bloquear disponibilidad. |
| Cancelar un evento. | Se registra la cancelación local, se bloquean nuevas reservas y se cancelan las reservas temporales activas. |
| Devolver una entrada. | Se libera individualmente el lugar correspondiente y se registra la operación para evitar duplicaciones. |
| Repetir una solicitud idempotente. | No se generan reservas ni liberaciones duplicadas. |
| Enviar solicitudes inválidas. | Se devuelven respuestas HTTP acordes con las validaciones y reglas de negocio. |

### 5.2. Creación y consulta de reservas

Para crear una reserva temporal se utiliza:

```http
POST /reservas
```

La respuesta contiene el identificador de la reserva, el cliente, el evento, los detalles de los lugares y las fechas de creación y vencimiento.

La consulta de una reserva se realiza mediante:

```http
GET /reservas/{id}
```

Permite comprobar su estado y los lugares asociados.

### 5.3. Consulta de disponibilidad

La disponibilidad se consulta mediante:

```http
GET /disponibilidad?eventoId=20&sectorId=5
```

La respuesta incluye:

- `eventoId`
- `sectorId`
- `lugaresDisponibles`
- `asientosDisponibles`
- `asientosOcupados`

Para sectores numerados se informan los asientos libres y ocupados. En sectores no numerados, la ocupación se representa mediante la cantidad de lugares disponibles.

Las consultas consideran las reservas confirmadas y las reservas activas que todavía no vencieron.

### 5.4. Confirmación y liberación

La confirmación se realiza mediante:

```http
POST /reservas/{id}/confirmar
```

La operación valida que la reserva se encuentre activa y vigente antes de cambiar su estado a `CONFIRMADA`.

La liberación se realiza mediante:

```http
POST /reservas/{id}/liberar
```

Permite liberar una reserva temporal y devolver los lugares correspondientes a la disponibilidad, siempre que el estado del evento lo permita.

### 5.5. Vencimiento de reservas

Las reservas temporales tienen una duración de 10 minutos.

El vencimiento se controla mediante `fechaVencimiento`.

Cuando una reserva activa supera esa fecha, deja de bloquear lugares aunque su estado persistido todavía sea `ACTIVA`.

Las pruebas manuales de vencimiento permiten comprobar que los lugares vuelven a considerarse disponibles una vez transcurrido el plazo.

Este mecanismo utiliza vencimiento lógico, por lo que no requiere un scheduler para liberar capacidad.

Una operación posterior puede actualizar el estado persistido a `VENCIDA`.

Las reservas `CONFIRMADA` no se liberan mediante este mecanismo.

### 5.6. Cancelación de eventos

La cancelación local se procesa mediante:

```http
POST /reservas/evento/{eventoId}/cancelar
```

Esta operación registra la cancelación del evento, impide nuevas reservas y actualiza las reservas temporales activas.

Las entradas emitidas y los reintegros correspondientes son administrados por los microservicios Entradas y Compras.

### 5.7. Devolución individual de entradas

La devolución de un lugar se procesa mediante:

```http
POST /reservas/{id}/devolver-entrada
```

La solicitud contiene la identificación de la entrada, el sector y el asiento cuando corresponde.

El microservicio registra el identificador de la entrada liberada para impedir que una misma devolución modifique dos veces la disponibilidad.

## 6. Pruebas de integración entre componentes

El microservicio Reservas participa en los siguientes circuitos de integración:

| Integración | Operaciones involucradas |
|---|---|
| Reservas → Eventos y Lugares | Consulta de eventos, sectores, asientos y precios mediante OpenFeign. |
| Compras → Reservas | Confirmación de reservas después de pagos aprobados y liberación ante pagos rechazados. |
| Eventos y Lugares → Reservas | Habilitación y cancelación de eventos. |
| Entradas → Reservas | Liberación individual de lugares correspondientes a entradas devueltas. |
| API Gateway → Reservas | Enrutamiento de solicitudes a los endpoints correspondientes. |

Las pruebas unitarias utilizan mocks y las verificaciones locales pueden utilizar un simulador de Eventos y Lugares.

Las pruebas de integración de API y las pruebas manuales permiten complementar estas verificaciones, diferenciando la comunicación simulada de la ejecución conjunta de los microservicios.

## 7. Validaciones y respuestas de error

Las pruebas de las reglas de negocio contemplan el rechazo de operaciones que no cumplen las condiciones requeridas.

| Escenario | Respuesta esperada |
|---|---|
| Crear una reserva con datos incompletos. | `400 Bad Request` |
| Consultar una reserva inexistente. | `404 Not Found` |
| Reservar un asiento que ya está ocupado. | `409 Conflict` |
| Superar la capacidad disponible de un sector. | `409 Conflict` |
| Intentar reservar un evento cancelado. | `409 Conflict` |
| Confirmar una reserva vencida. | `409 Conflict` |
| Confirmar una reserva en un estado incompatible. | `409 Conflict` |
| Intentar liberar una entrada de forma incompatible con el estado de la reserva. | `409 Conflict` |
| Fallo al consultar Eventos y Lugares. | `503 Service Unavailable` |

Estas validaciones permiten comprobar que el sistema respeta las restricciones de disponibilidad, estado y consistencia.

## 8. Conclusión

El microservicio `msvc-reservas` cuenta con pruebas unitarias, pruebas de concurrencia sobre MySQL y procedimientos de verificación manual de sus principales operaciones.

La prueba de concurrencia permite comprobar la prevención de reservas simultáneas incompatibles, mientras que las pruebas de negocio verifican el vencimiento, la confirmación, la liberación y la cancelación de reservas.

Las verificaciones HTTP permiten evaluar el comportamiento observable de los endpoints, las respuestas de error y la actualización de la disponibilidad.

El conjunto de pruebas documenta los mecanismos utilizados por Reservas para mantener la consistencia de la ocupación y evitar duplicaciones durante el proceso de compra de entradas.