# TicketYa — Microservicio de Reservas

**Entrega 1 · Arquitectura de microservicios**  
**Tecnologías:** Java 21 · Spring Boot 4.1.2 · Spring Data JPA / Hibernate · MySQL · OpenFeign · Springdoc / Swagger UI · JUnit 5 y Mockito

## 1. Objetivo y alcance

`msvc-reservas` gestiona la **disponibilidad, reserva temporal y ocupación confirmada** de los lugares de un evento. Trabaja con sectores **numerados** (asientos concretos) y **no numerados** (capacidad), evita la doble reserva mediante bloqueos transaccionales e implementa idempotencia frente a reintentos.

Su responsabilidad comprende:

- Consultar lugares libres para un evento y un sector.
- Crear reservas **ACTIVA** con vencimiento a los **10 minutos**.
- Respetar el máximo configurable de entradas por cliente y evento (valor local: **4**).
- Evitar reservas simultáneas incompatibles de asientos y sobreventa de sectores no numerados.
- Consultar, confirmar, liberar y vencer reservas.
- Bloquear ventas y cancelar reservas temporales cuando un evento se cancela.
- Liberar individualmente una entrada cuando un servicio autorizado informa una devolución aprobada.

**No** administra eventos, sectores, asientos ni precios; tampoco procesa pagos, genera QR, aprueba devoluciones o efectúa reintegros. Esas operaciones corresponden a otros microservicios.

Las historias de usuario oficiales **US01–US07**, sus criterios de aceptación y el estado real de implementación están en [`01-FUNCIONALIDADES-Y-USER-STORIES.md`](01-FUNCIONALIDADES-Y-USER-STORIES.md).

## 2. Requisitos y configuración

- JDK **21**, Maven y MySQL en funcionamiento.
- Base propia de Reservas (`ticketya_reservas`), separada de la de Eventos.
- Microservicio Eventos y Lugares disponible en el puerto configurado para las operaciones que lo requieren (o simulador local durante desarrollo).

Crear la base de datos en MySQL:

```sql
CREATE DATABASE IF NOT EXISTS ticketya_reservas;
```

Configurar credenciales en variables de entorno (no guardar contraseñas reales en Git):

```powershell
$env:DB_USERNAME="root"
$env:DB_PASSWORD="TU_CONTRASENA"
```

Configuración principal en `src/main/resources/application.properties`:

```properties
server.port=8080
spring.datasource.url=jdbc:mysql://localhost:3306/ticketya_reservas
spring.datasource.username=${DB_USERNAME:root}
spring.datasource.password=${DB_PASSWORD:}
spring.jpa.hibernate.ddl-auto=update
reservas.max-entradas-por-cliente=4
msvc.eventos.url=http://localhost:8081
springdoc.swagger-ui.path=/swagger-ui.html
```

`ddl-auto=update` es una configuración **de desarrollo**; no equivale a una estrategia de migraciones para producción.

## 3. Cómo ejecutar el proyecto

Desde la carpeta `msvc-reservas`:

```powershell
mvn clean compile
mvn spring-boot:run
```

Servicio: **http://localhost:8080**.

Para probar creación de reservas o disponibilidad, el servicio de Eventos y Lugares debe responder al contrato Feign. **No se debe levantar el simulador y el servicio real de Eventos en el puerto 8081 al mismo tiempo.**

### Swagger / OpenAPI

La documentación interactiva se genera con Springdoc a partir de los controllers y DTO anotados:

- **Swagger UI:** http://localhost:8080/swagger-ui.html
- **Especificación OpenAPI JSON:** http://localhost:8080/v3/api-docs
- **Contrato OpenAPI manual:** [`openapi.yaml`](openapi.yaml)
- **Colección Postman:** [`TicketYa-Reservas.postman_collection.json`](TicketYa-Reservas.postman_collection.json)

> Swagger sirve para visualizar y probar endpoints, **no** aplica seguridad por sí solo. El archivo `openapi.yaml` es una versión manual que debe mantenerse sincronizada con el contrato generado por Springdoc.

## 4. API REST

| Método | Ruta | Descripción | Acceso previsto |
| `GET` | `/disponibilidad?eventoId={id}&sectorId={id}` | Consulta lugares y asientos **disponibles** | Consulta del cliente |
| `POST` | `/reservas` | Crea una reserva temporal de 10 minutos | Flujo de compra |
| `GET` | `/reservas/{id}` | Consulta una reserva y su estado | Acceso autorizado |
| `POST` | `/reservas/{id}/confirmar` | Confirma una reserva tras un pago aprobado | **Interno** (Compras) |
| `POST` | `/reservas/{id}/liberar` | Libera una reserva temporal tras pago rechazado | **Interno** (Compras) |
| `POST` | `/reservas/{id}/devolver-entrada` | Libera **una** entrada cuya devolución ya fue aprobada | **Interno** (servicio autorizado) |
| `POST` | `/reservas/evento/{eventoId}/habilitar` | Prepara la fila de bloqueo de un evento publicado; devuelve `204` | **Interno** (Eventos) |
| `POST` | `/reservas/evento/{eventoId}/cancelar` | Bloquea nuevas ventas y cancela reservas temporales; devuelve `204` | **Interno** (Eventos) |

### Ejemplo: crear reserva

```json
{
  "clienteId": 1,
  "eventoId": 21,
  "claveIdempotencia": "RESERVA-001",
  "detalles": [
    {
      "sectorId": 5,
      "cantidad": 2,
      "asientoIds": [25, 26]
    }
  ]
}
```

En sectores no numerados se envía `asientoIds: []` y la cantidad solicitada. Los identificadores del ejemplo son ilustrativos; deben existir en Eventos y Lugares.

### Ejemplo: consultar disponibilidad

```http
GET /disponibilidad?eventoId=21&sectorId=5
```

```json
{
  "eventoId": 21,
  "sectorId": 5,
  "lugaresDisponibles": 3,
  "asientosDisponibles": [25, 26, 27]
}
```

La respuesta refleja el estado consultado, **no retiene asientos** ni garantiza que permanezcan libres hasta crear la reserva.

Los errores de negocio devuelven JSON con `estado` y `mensaje`, por ejemplo un `409 Conflict` cuando el evento fue cancelado.

## 5. Reglas y decisiones técnicas

- **Evento habilitado:** antes de permitir ventas se debe invocar `POST /reservas/evento/{eventoId}/habilitar`. Reservas verifica que el evento exista, esté `PUBLICADO` y no se encuentre cancelado localmente. Para crear una reserva, también se valida que tenga fecha futura.
- **Validaciones remotas:** se comprueba la correspondencia evento–lugar–sector–asiento y que exista un precio para el par evento/sector mediante `EventoClient` (Feign).
- **Vencimiento:** las reservas activas dejan de ocupar lugares cuando vence su `fechaVencimiento`, aunque el registro puede conservar `ACTIVA` hasta ser procesado en una operación posterior (véase el apartado 8).
- **Concurrencia:** se usa `PESSIMISTIC_WRITE` sobre una fila de `bloqueo_evento` y transacciones MySQL `READ_COMMITTED`. Los bloqueos se aplican dentro de **la base de Reservas**, incluso si hay varias instancias que la comparten.
- **Idempotencia:** `claveIdempotencia` identifica la solicitud de creación; un reintento idéntico devuelve la reserva existente. Una misma clave con otros datos se rechaza. Confirmación, liberación, cancelación y devoluciones incluyen tratamiento de reintentos.
- **Estados:** `ACTIVA`, `CONFIRMADA`, `VENCIDA`, `LIBERADA` y `CANCELADA`.
- **Cancelación de evento:** `cancelado=true` permanece registrado localmente e impide nuevas ventas. Las reservas confirmadas requieren coordinación externa para la gestión de reintegros.
- **Devolución individual:** se registra `entradaId` en `entrada_liberada` para evitar doble liberación. La verificación de titularidad, aprobación y plazo de 72 horas pertenece al servicio autorizado que invoca la operación.

Para más detalle, ver [`06-DECISIONES-Y-LIMITACIONES-RESERVAS.md`](06-DECISIONES-Y-LIMITACIONES-RESERVAS.md).

## 6. Comunicación con otros microservicios

**Eventos y Lugares → Reservas**

- Llama a `/reservas/evento/{eventoId}/habilitar` después de confirmar la publicación del evento.
- Llama a `/reservas/evento/{eventoId}/cancelar` al cancelar un evento, coordinando las consecuencias sobre ventas y entradas.

**Reservas → Eventos y Lugares (Feign)**

- `GET /eventos/{id}`
- `GET /sectores/{id}`
- `GET /asientos/{id}`
- `GET /sectores/{id}/asientos`
- `GET /eventos/{eventoId}/sectores/{sectorId}/precio`

**Compras y Pagos → Reservas:** confirma o libera reservas según el resultado del pago; si un pago se aprueba después del vencimiento, corresponde gestionar una compensación.

**Entradas / proceso autorizado → Reservas:** solicita liberar una entrada una vez validada y aprobada la devolución. El reintegro del dinero se gestiona fuera de Reservas.

**Estado de integración:** `EventoClient` y sus cinco operaciones Feign están implementados; los endpoints de confirmación/liberación que utiliza Compras y los endpoints de habilitación/cancelación invocados por Eventos también están implementados en Reservas. El endpoint de liberación individual de entradas está disponible para el servicio autorizado. La existencia de estos contratos no acredita por sí sola una prueba de extremo a extremo contra todos los servicios reales. El equipo debe registrar pruebas integradas de los flujos completos. La configuración y seguridad del Gateway se verifica en el microservicio Gateway, que no forma parte de este ZIP. Véase [`contrato-eventos.md`](contrato-eventos.md).

Los endpoints internos deben quedar restringidos por la configuración del Gateway y/o la red, no por el hecho de que Swagger los etiquete como «internos».

## 7. Pruebas

### Unitarias

```powershell
mvn test
```

Incluyen tests JUnit 5 y Mockito en `ReservaServiceImplTest.java`. La prueba MySQL de concurrencia está deshabilitada salvo activación explícita.

### Concurrencia con MySQL real

Usa una **base exclusiva para pruebas**, nunca una base con datos importantes. Crear antes `ticketya_reservas_test` y ejecutar en PowerShell:

```powershell
$env:TICKETYA_MYSQL_TEST="true"
$env:TEST_DB_URL="jdbc:mysql://localhost:3306/ticketya_reservas_test"
$env:TEST_DB_USER="root"
$env:TEST_DB_PASSWORD="TU_CONTRASENA"
mvn "-Dtest=ConcurrenciaMySQLIT" test
```

El test `muchasSolicitudesMismoAsientoSoloUnaGana` realiza diez intentos concurrentes por un mismo asiento y verifica que **solo uno** tenga éxito. Esta prueba pasó durante el desarrollo con MySQL real.

También se verificaron manualmente creación, confirmación, liberación, vencimiento lógico, cancelación, devolución individual, errores HTTP, idempotencia y disponibilidad mediante PowerShell, MySQL y un simulador de Eventos.

Detalles y resultados: [`05-TESTING-RESERVAS.md`](05-TESTING-RESERVAS.md).

## 8. Estado de las historias de usuario y pendientes conocidos

| Historia | Estado actual | Pendiente para cumplir todos sus criterios |
| **US01** Disponibilidad | **Parcial** | La API muestra asientos libres, **no lista explícitamente los ocupados**. |
| **US02** Reserva temporal | **Implementada localmente** | Validar contrato contra Eventos real y flujo de compra integrado. |
| **US03** Concurrencia e idempotencia | **Implementada y probada en MySQL** | Pruebas distribuidas de varias instancias y de fallas de red. |
| **US04** Vencimiento | **Implementada con vencimiento lógico** | El equipo informó que verificó manualmente que la capacidad vuelve a estar disponible al cumplirse los 10 minutos. No se requiere scheduler en Entrega 1; el estado puede persistir temporalmente como `ACTIVA` hasta una operación posterior. |
| **US05** Confirmación | **Endpoint implementado** | El cliente de Compras está desarrollado; falta evidencia de prueba integrada del flujo completo con pago y emisión de entradas. |
| **US06** Liberación por rechazo | **Endpoint implementado** | El cliente de Compras está desarrollado; falta evidencia de prueba integrada del flujo completo de rechazo y liberación. |
| **US07** Devoluciones y cancelación | **Endpoints implementados** | Falta prueba integrada de la coordinación con Entradas, Compras y Eventos para aprobación, titularidad, reintegros y cancelación. |

**Prioridades funcionales pendientes identificadas al contrastar las historias proporcionadas:**

1. **US01:** extender la respuesta de disponibilidad numerada para informar también los IDs ocupados, sin confundirlos con los disponibles; actualizar DTO, Service, Swagger, OpenAPI y tests si se adopta ese contrato.
2. **US04:** no se incorpora scheduler en Entrega 1. El vencimiento lógico libera capacidad porque las consultas excluyen las reservas cuyo plazo ya pasó. El estado persistido se actualiza a `VENCIDA` cuando una operación posterior procesa esa reserva.
3. **US03/US05–US07:** ejecutar y registrar pruebas integradas entre los microservicios reales, además de verificar la protección de los endpoints internos mediante Gateway/red.

No se debe presentar estos criterios pendientes como terminados solo porque el servicio compile o Swagger muestre sus endpoints.

## 9. Documentación del proyecto

| Archivo | Contenido |
| [`01-FUNCIONALIDADES-Y-USER-STORIES.md`](01-FUNCIONALIDADES-Y-USER-STORIES.md) | US01–US07, criterios de aceptación y trazabilidad |
| [`02-ARQUITECTURA-Y-DIAGRAMAS-RESERVAS.md`](02-ARQUITECTURA-Y-DIAGRAMAS-RESERVAS.md) | Arquitectura, Feign y secuencias Mermaid |
| [`03-MODELO-DE-DATOS-RESERVAS.md`](03-MODELO-DE-DATOS-RESERVAS.md) | Entidades, relaciones JPA y DER Mermaid |
| [`05-TESTING-RESERVAS.md`](05-TESTING-RESERVAS.md) | Estrategia, ejecución y resultados de pruebas |
| [`06-DECISIONES-Y-LIMITACIONES-RESERVAS.md`](06-DECISIONES-Y-LIMITACIONES-RESERVAS.md) | Concurrencia, idempotencia, límites y pendientes |
| [`contrato-eventos.md`](contrato-eventos.md) | Contrato Feign con Eventos y Lugares |
| [`openapi.yaml`](openapi.yaml) | Especificación OpenAPI mantenida manualmente |
| [`TicketYa-Reservas.postman_collection.json`](TicketYa-Reservas.postman_collection.json) | Colección Postman |

> Los documentos `02`, `03`, `05` y `06` deben conservarse en `docs/` con los nombres indicados. Los cambios posteriores en el código, el contrato de Eventos o las historias de usuario deben reflejarse también en Swagger, OpenAPI y documentación técnica.