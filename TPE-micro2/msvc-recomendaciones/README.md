# TicketYa — Microservicio de Recomendaciones

Microservicio encargado de administrar las preferencias de los clientes y recomendar eventos según sus intereses y el historial de compras.

## Tecnologías

Java 21, Spring Boot, Spring Data MongoDB, MongoDB, Spring Cloud OpenFeign, Spring Security y OpenAPI.

## Configuración

- **Puerto:** `8086`.
- **Base de datos:** MongoDB (`ticketya_recomendaciones`).
- **Dependencias:** Eventos y Lugares, Compras y Pagos.
- **Seguridad:** JWT con control de roles e identidad del cliente.

Las conexiones se configuran mediante `MONGODB_URI`, `EVENTOS_URL` y `COMPRAS_URL`.

## Funcionalidades

- Registrar, consultar y modificar preferencias.
- Recomendar eventos publicados y futuros.
- Combinar preferencias con sugerencias del historial de compras.
- Identificar clientes interesados en un artista y una ciudad mediante una consulta interna.

## Ejecución

Con MongoDB disponible y las variables de configuración establecidas:

```bash
mvn spring-boot:run
```

## Pruebas

```bash
mvn clean test
```

## Documentación

- `docs/01-FUNCIONALIDADES-Y-USER-STORIES.md`
- `docs/02-ARQUITECTURA-Y-DIAGRAMAS.md`
- `docs/03-MODELO-DE-DATOS.md`
- `docs/04-CONTRATOS-REST.md`
- `docs/05-TESTING.md`
- `docs/openapi.json` — especificación de la API

Swagger UI: `http://localhost:8086/swagger-ui.html`.