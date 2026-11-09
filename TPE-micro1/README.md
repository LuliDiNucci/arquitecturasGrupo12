# TicketYa — Sistema de venta y gestión de entradas

## 1. Descripción general

TicketYa es una plataforma de venta y gestión de entradas para distintos tipos de eventos, como recitales, festivales, obras de teatro y conferencias.

El sistema permite consultar eventos publicados, conocer la disponibilidad de sectores y asientos, realizar reservas temporales, gestionar compras y pagos, emitir entradas digitales con códigos QR y administrar devoluciones y cancelaciones.

El proyecto está desarrollado utilizando una arquitectura de microservicios, donde cada servicio tiene responsabilidades específicas y se comunica con los demás mediante contratos REST.

**Trabajo Práctico Especial (TPE) — Entrega 1.**

## 2. Funcionalidades principales

- Gestión de eventos, lugares, sectores, asientos, organizadores y participantes.
- Publicación, búsqueda, suspensión y cancelación de eventos.
- Consulta de disponibilidad de lugares y asientos.
- Creación de reservas temporales con vencimiento de 10 minutos.
- Confirmación y liberación de reservas.
- Prevención de reservas simultáneas incompatibles.
- Inicio y gestión de compras.
- Procesamiento de pagos mediante pasarela simulada.
- Emisión de entradas digitales con códigos QR.
- Consulta de entradas adquiridas por los clientes.
- Solicitud, aprobación y rechazo de devoluciones.
- Invalidación de entradas y gestión de reintegros ante cancelaciones.

## 3. Arquitectura del sistema

TicketYa se organiza en cuatro microservicios funcionales y un API Gateway.

| Componente | Responsabilidad |
|---|---|
| `msvc-eventosylugares` | Gestiona eventos, lugares, sectores, asientos, organizadores, participantes y precios. |
| `msvc-reservas` | Administra disponibilidad, reservas temporales, confirmaciones, liberaciones y concurrencia. |
| `msvc-compras` | Gestiona compras, pagos, confirmaciones y reintegros. |
| `msvc-entradas` | Administra la emisión, consulta, devolución e invalidación de entradas digitales. |
| `msvc-gateway` | Centraliza el acceso y el enrutamiento de solicitudes hacia los microservicios. |

Cada microservicio administra sus propios datos y se comunica con los demás mediante APIs REST y clientes de integración.

No se comparten entidades JPA ni se accede directamente a las bases de datos de otros microservicios.

### 3.1. Flujo general de compra

1. El cliente consulta los eventos publicados y sus sectores.
2. Consulta la disponibilidad de lugares o asientos.
3. Solicita una reserva temporal con una vigencia de 10 minutos.
4. Inicia una compra asociada a la reserva.
5. Se procesa el pago mediante una pasarela simulada.
6. Si el pago es aprobado y la reserva continúa vigente, se confirma la reserva.
7. Compras solicita la emisión de las entradas correspondientes.
8. Las entradas quedan disponibles para su consulta con sus códigos QR.

Si el pago es rechazado, se solicita la liberación de la reserva.

Las reservas temporales vencidas dejan de bloquear lugares y no pueden confirmarse posteriormente.

### 3.2. Devoluciones y cancelaciones

El sistema permite solicitar devoluciones de entradas, sujetas a las reglas de negocio correspondientes.

Las solicitudes pueden ser aprobadas o rechazadas. Cuando se aprueba una devolución, se coordina la invalidación de la entrada, la liberación del lugar y el reintegro del importe correspondiente.

Ante la cancelación de un evento, Eventos y Lugares registra la cancelación y coordina las notificaciones hacia Reservas, Entradas y Compras.

Las notificaciones utilizan registros persistentes y mecanismos de reintento para gestionar las comunicaciones entre servicios.

## 4. Tecnologías utilizadas

| Tecnología | Uso |
|---|---|
| Java 21 | Lenguaje y plataforma de ejecución. |
| Spring Boot | Desarrollo de los microservicios. |
| Spring Data JPA / Hibernate | Persistencia y acceso a datos. |
| MySQL / MariaDB | Almacenamiento relacional utilizado por los servicios configurados con estas bases. |
| H2 | Base de datos utilizada en configuraciones locales o de pruebas de determinados servicios. |
| Spring Cloud OpenFeign | Comunicación HTTP entre microservicios. |
| Spring RestClient | Consumo de APIs REST. |
| Maven | Gestión de dependencias, compilación y ejecución. |
| JUnit 5 y Mockito | Pruebas automatizadas. |
| Spring Boot Test | Pruebas del contexto y de integración de componentes. |
| Postman | Ejecución de solicitudes y pruebas de API. |
| Swagger / OpenAPI | Documentación de los contratos REST. |
| Git y GitHub | Control de versiones y trabajo colaborativo. |

Las configuraciones de persistencia y conexión se encuentran definidas en los archivos de propiedades de cada microservicio.

## 5. Estructura del repositorio

```text
TPE-micro1/
│
├── README.md
│
├── docs/
│   ├── 01-FEATURES-Y-USER-STORIES.md
│   ├── 02-ARQUITECTURA-GENERAL.md
│   ├── 03-DIAGRAMA-SECUENCIA-COMPRA.md
│   ├── 04-RELACIONES-ENTRE-MICROSERVICIOS.md
│   ├── 05-CONTRATOS-DE-INTEGRACION.md
│   └── 06-PLAN-DE-TESTING.md
│
├── msvc-eventosylugares/
│   ├── src/
│   ├── docs/
│   └── pom.xml
│
├── msvc-reservas/
│   ├── src/
│   ├── docs/
│   └── pom.xml
│
├── msvc-compras/
│   ├── src/
│   ├── docs/
│   └── pom.xml
│
├── msvc-entradas/
│   ├── src/
│   ├── docs/
│   └── pom.xml
│
└── msvc-gateway/
    ├── src/
    └── pom.xml
```

Cada microservicio contiene su propio código fuente, configuración y documentación técnica.

## 6. Documentación del proyecto

### 6.1. Documentación general

La carpeta `docs/` contiene los documentos que describen el funcionamiento integral de TicketYa.

| Documento | Contenido |
|---|---|
| [01 — Features y User Stories](docs/01-FEATURES-Y-USER-STORIES.md) | Funcionalidades e historias de usuario del sistema. |
| [02 — Arquitectura general](docs/02-ARQUITECTURA-GENERAL.md) | Arquitectura, responsabilidades e interacciones entre servicios. |
| [03 — Diagrama de secuencia de compra](docs/03-DIAGRAMA-SECUENCIA-COMPRA.md) | Circuito de compra, incluyendo pagos rechazados y reservas vencidas. |
| [04 — Relaciones entre microservicios](docs/04-RELACIONES-ENTRE-MICROSERVICIOS.md) | Dependencias y comunicaciones entre servicios. |
| [05 — Contratos de integración](docs/05-CONTRATOS-DE-INTEGRACION.md) | Endpoints utilizados en la comunicación entre microservicios. |
| [06 — Plan de testing](docs/06-PLAN-DE-TESTING.md) | Estrategia general de pruebas del sistema. |

### 6.2. Documentación por microservicio

Cada microservicio funcional cuenta con una carpeta `docs/`
que contiene su documentación técnica.

Los documentos describen:

- Funcionalidades e historias de usuario.
- Arquitectura y diagramas.
- Modelo de datos y relaciones entre entidades.
- Contratos REST y ejemplos de solicitudes.
- Pruebas y testing.

La organización y numeración de los archivos puede variar
según el microservicio.

Los diagramas entidad-relación se incluyen como archivos
de imagen dentro de la documentación correspondiente.

Los contratos de API también se encuentran documentados
mediante OpenAPI y Swagger.

## 7. Requisitos para la ejecución

Para ejecutar el sistema localmente se requiere:

- JDK 21.
- Maven.
- MySQL o MariaDB para los microservicios que utilizan estos motores.
- Acceso a las dependencias Maven del proyecto.
- Postman, opcionalmente, para realizar pruebas manuales.
- Un navegador para consultar la documentación Swagger.

Cada microservicio es un proyecto Maven independiente y utiliza su propia configuración de persistencia y conexiones.

## 8. Configuración y ejecución local

### 8.1. Configuración de los servicios

Antes de iniciar los microservicios se deben revisar los archivos `application.properties` o `application.yml` correspondientes.

Se deben configurar las conexiones a las bases de datos, los puertos y las direcciones de los servicios utilizados mediante HTTP.

Los puertos predeterminados de los microservicios funcionales son:

| Microservicio | Puerto |
|---|---|
| Eventos y Lugares | `8081` |
| Reservas | `8080` |
| Compras y Pagos | `8082` |
| Entradas | `8083` |
| Api Gateway | `8084` |


### 8.2. Iniciar los microservicios

Desde la raíz del repositorio, abrir una terminal para cada componente.

**1. Eventos y Lugares**

```powershell
cd msvc-eventosylugares
mvn spring-boot:run
```

**2. Reservas**

```powershell
cd msvc-reservas
mvn spring-boot:run
```

**3. Compras y Pagos**

```powershell
cd msvc-compras
mvn spring-boot:run
```

**4. Entradas**

```powershell
cd msvc-entradas
mvn spring-boot:run
```

**5. API Gateway**

```powershell
cd msvc-gateway
mvn spring-boot:run
```

Cada comando se ejecuta desde la carpeta correspondiente, manteniendo el proceso en ejecución.

Para probar el circuito completo de compra, los microservicios involucrados deben encontrarse disponibles.

### 8.3. Compilar el proyecto

Desde el directorio de cada microservicio:

```powershell
mvn clean compile
```

Para generar el archivo ejecutable:

```powershell
mvn clean package
```

### 8.4. Ejecutar pruebas automatizadas

Desde el directorio del microservicio correspondiente:

```powershell
mvn clean test
```

Este comando ejecuta las pruebas reconocidas por la configuración de Maven.

Algunas pruebas de integración utilizan configuraciones y bases de datos específicas, documentadas en los archivos de testing de cada microservicio.

## 9. Documentación Swagger / OpenAPI

Los microservicios exponen documentación de sus endpoints mediante Swagger y OpenAPI.

Con cada servicio iniciado, se pueden consultar las siguientes direcciones:

| Microservicio | Swagger UI |
|---|---|
| Eventos y Lugares | `http://localhost:8081/swagger-ui.html` |
| Reservas | `http://localhost:8080/swagger-ui.html` |
| Compras y Pagos | `http://localhost:8082/swagger-ui.html` |
| Entradas | `http://localhost:8083/swagger-ui.html` |

La especificación OpenAPI generada puede consultarse en la ruta `/v3/api-docs` de cada servicio que tenga habilitada la generación mediante Springdoc.

La documentación permite consultar los métodos HTTP, parámetros, esquemas de datos y respuestas de las operaciones.

## 10. Pruebas del sistema

### 10.1. Pruebas unitarias

Los microservicios incluyen pruebas automatizadas para sus principales reglas de negocio.

Entre los escenarios contemplados se encuentran:

- Validaciones de publicación y configuración de eventos.
- Prevención de sectores y asientos duplicados.
- Creación y vencimiento de reservas.
- Control de concurrencia sobre lugares.
- Idempotencia de operaciones.
- Confirmación y liberación de reservas.
- Validación de compras y pagos.
- Emisión de entradas.
- Solicitudes y resolución de devoluciones.
- Procesamiento de cancelaciones y reintegros.

Las clases y escenarios correspondientes se describen en el documento de testing de cada microservicio.

### 10.2. Pruebas de concurrencia

Reservas incluye la prueba `ConcurrenciaMySQLIT`, que utiliza MySQL para comprobar el comportamiento ante solicitudes simultáneas sobre un mismo asiento.

El escenario ejecuta diez solicitudes concurrentes y verifica que solamente una pueda reservar el lugar.

La configuración y los comandos de ejecución se encuentran documentados en `msvc-reservas/docs/05-TESTING-RESERVAS.md`.

### 10.3. Pruebas manuales con Postman

El proyecto utiliza colecciones Postman para ejecutar y validar solicitudes HTTP.

Para realizar las pruebas:

1. Iniciar los microservicios necesarios para el circuito.
2. Importar las colecciones Postman disponibles en el repositorio.
3. Configurar las URL y variables del entorno.
4. Ejecutar las operaciones de creación y consulta de datos.
5. Probar los circuitos de reserva, compra, pago y emisión.
6. Verificar los escenarios de devolución y cancelación.
7. Consultar los estados y respuestas HTTP obtenidos.

Las solicitudes permiten validar tanto operaciones exitosas como errores de negocio y conflictos de disponibilidad.

## 11. Alcance de la entrega

La primera entrega de TicketYa comprende:

- Definición de funcionalidades e historias de usuario con criterios de aceptación.
- Identificación de los microservicios y sus responsabilidades.
- Diseño de la arquitectura y sus principales flujos de comunicación.
- Diagramas de secuencia del proceso de compra.
- Modelos de datos y diagramas entidad-relación.
- Definición y documentación de contratos REST.
- Implementación de APIs utilizando Spring Boot.
- Centralización del acceso mediante API Gateway.
- Documentación de APIs con Swagger y OpenAPI.
- Pruebas automatizadas y manuales de los principales escenarios de negocio.
- Configuración y documentación para la ejecución local.

La documentación específica de cada microservicio complementa este README con las reglas de negocio, endpoints, modelos de datos y casos de prueba correspondientes.