# TicketYa — Sistema de venta y gestión de entradas

## 1. Descripción general

TicketYa es una plataforma de venta y gestión de entradas para distintos tipos de eventos, como recitales, festivales, obras de teatro y conferencias.

El sistema permite consultar eventos publicados, conocer la disponibilidad de sectores y asientos, realizar reservas temporales, gestionar compras y pagos, emitir entradas digitales con códigos QR y administrar devoluciones y cancelaciones.

El proyecto está desarrollado utilizando una arquitectura de microservicios, donde cada servicio tiene responsabilidades específicas y se comunica con los demás mediante contratos REST.

**Alcance:** Trabajo Práctico Especial (TPE) — Entrega 1.

## 2. Funcionalidades principales

- Gestión de eventos, lugares, sectores, asientos, organizadores y participantes.
- Publicación, búsqueda y cancelación de eventos.
- Consulta de disponibilidad de entradas.
- Creación, confirmación y liberación de reservas temporales.
- Inicio y gestión de compras.
- Procesamiento de pagos.
- Emisión de entradas digitales con códigos QR.
- Solicitud, aprobación y rechazo de devoluciones.
- Invalidación de entradas y gestión de reintegros ante cancelaciones.

## 3. Arquitectura del sistema

TicketYa está organizado en los siguientes microservicios:

| Microservicio          | Responsabilidad |
| `msvc-eventosylugares` | Gestiona eventos, lugares, sectores, asientos, organizadores y participantes. |
| `msvc-reservas`        | Gestiona la disponibilidad y las reservas temporales y confirmadas. |
| `msvc-compras`         | Gestiona las compras, los pagos, las confirmaciones y los reintegros. |
| `msvc-entradas`        | Gestiona la emisión, consulta, invalidación y devolución de entradas digitales. |
| `msvc-gateway`         | Centraliza el acceso y el enrutamiento hacia los microservicios. |

Los servicios mantienen la responsabilidad sobre sus propios datos y se comunican mediante interfaces REST, utilizando clientes de integración cuando necesitan información u operaciones de otros servicios.

### Flujo general de compra

1. El cliente consulta los eventos y sus sectores disponibles.
2. Solicita una reserva temporal.
3. Inicia una compra asociada a esa reserva.
4. Se procesa el pago.
5. Si el pago es aprobado, se confirma la reserva y se emiten las entradas.
6. Las entradas quedan disponibles para su consulta mediante sus códigos QR.

En caso de rechazo del pago, cancelación del evento o devolución aprobada, se ejecutan las operaciones correspondientes para liberar lugares, invalidar entradas y gestionar los reintegros.

## 4. Tecnologías utilizadas

El proyecto utiliza principalmente:

- **Java:** lenguaje de programación.
- **Spring Boot:** desarrollo de los microservicios.
- **Spring Data JPA / Hibernate:** persistencia y acceso a datos.
- **MySQL / MariaDB:** almacenamiento de información.
- **Spring Cloud OpenFeign y clientes REST:** comunicación entre microservicios.
- **Maven:** gestión de dependencias y construcción.
- **JUnit 5 y Mockito:** pruebas unitarias.
- **H2:** base de datos en memoria para pruebas.
- **Postman:** pruebas manuales de endpoints.
- **Swagger / OpenAPI:** documentación de API.
- **Git y GitHub:** control de versiones y trabajo colaborativo.

Las versiones y configuraciones específicas se encuentran documentadas en cada microservicio.

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
├── msvc-reservas/
├── msvc-compras/
├── msvc-entradas/
└── msvc-gateway/
```

Cada microservicio contiene su propio código fuente, configuración y documentación específica.

## 6. Documentación general

La documentación compartida del proyecto se encuentra en la carpeta `docs/`:

| Documento | Contenido |
| [01 — Features y User Stories](docs/01-FEATURES-Y-USER-STORIES.md) | Funcionalidades e historias de usuario del sistema. |
| [02 — Arquitectura general](docs/02-ARQUITECTURA-GENERAL.md) | Arquitectura, responsabilidades e interacciones entre servicios. |
| [03 — Diagrama de secuencia de compra](docs/03-DIAGRAMA-SECUENCIA-COMPRA.md) | Representación del circuito principal de compra. |
| [04 — Relaciones entre microservicios](docs/04-RELACIONES-ENTRE-MICROSERVICIOS.md) | Dependencias y comunicaciones entre servicios. |
| [05 — Contratos de integración](docs/05-CONTRATOS-DE-INTEGRACION.md) | Endpoints y contratos utilizados para la comunicación. |
| [06 — Plan de testing](docs/06-PLAN-DE-TESTING.md) | Estrategia de pruebas, cobertura y validaciones pendientes. |

Para conocer el funcionamiento y las decisiones particulares de cada servicio, consultar su respectivo `README.md` y la carpeta `docs/` interna.

## 7. Ejecución y configuración

Cada microservicio es un proyecto Maven independiente y cuenta con su propia configuración.

Para ejecutar un servicio localmente, ingresar a su directorio y utilizar:

```bash
mvn spring-boot:run
```

Para compilar y ejecutar las pruebas configuradas:

```bash
mvn clean test
```

La ejecución del sistema completo requiere configurar las bases de datos, los puertos y las direcciones de los servicios según las propiedades de cada microservicio.

Las configuraciones particulares y los requisitos para ejecutar cada servicio se encuentran detallados en sus respectivos README.

## 8. Alcance de la entrega

Esta entrega contempla la organización del sistema en microservicios, la definición de sus responsabilidades, funcionalidades, contratos de integración, flujos principales y estrategia de testing.

Las funcionalidades y validaciones deben interpretarse de acuerdo con el estado de implementación documentado en cada servicio y con las pruebas efectivamente realizadas.

---

**TicketYa — Trabajo Práctico Especial de Arquitectura Web.**