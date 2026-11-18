# Microservicio de Recomendaciones - TicketYa

## 1. Descripción general

El microservicio `msvc-recomendaciones` forma parte del sistema TicketYa y tiene como objetivo generar recomendaciones de eventos para los clientes a partir de sus preferencias.

Permite almacenar las preferencias de cada cliente y consultar eventos publicados para identificar aquellos que pueden resultar de su interés.

## 2. Tecnologías utilizadas

- Java 21.
- Spring Boot.
- Spring Web.
- Spring Data MongoDB.
- MongoDB.
- Spring Cloud OpenFeign.
- Springdoc OpenAPI / Swagger.

## 3. Configuración

| Propiedad | Valor |
|---|---|
| Nombre del servicio | msvc-recomendaciones |
| Puerto | 8086 |
| Base de datos | MongoDB |
| Base de datos utilizada | ticketya_recomendaciones |
| Servicio externo | msvc-eventosylugares |
| Puerto de Eventos | 8081 |

## 4. Comunicación con otros microservicios

El microservicio se comunica con `msvc-eventosylugares` mediante un cliente Feign.

Esta comunicación permite consultar los eventos publicados para utilizarlos como información de entrada al proceso de recomendación.

La URL del servicio de Eventos se configura mediante la propiedad `EVENTOS_URL`, cuyo valor predeterminado es:

`http://localhost:8081`

## 5. Persistencia

Las preferencias de los clientes se almacenan en MongoDB, utilizando la base de datos `ticketya_recomendaciones`.

## 6. Endpoints principales

- `GET /api/recomendaciones/{clienteId}`: consulta las recomendaciones correspondientes a un cliente.

La documentación completa de los endpoints se encuentra en `docs/04-CONTRATOS-REST.md`.

## 7. Swagger

Con el microservicio en ejecución, la documentación interactiva se encuentra disponible en:

`http://localhost:8086/swagger-ui.html`

## 8. Ejecución

Para ejecutar el microservicio se requiere Java 21, Maven y MongoDB en funcionamiento.

Desde la carpeta del proyecto:

```bash
mvn spring-boot:run
```

## 9. Pruebas

Se realizaron pruebas de configuración y consultas del servicio. Sin embargo, quedó pendiente verificar integralmente la generación de recomendaciones con eventos reales publicados.

Los resultados y las limitaciones de las pruebas se detallan en `docs/05-TESTING.md`.

## 10. Documentación complementaria

- `01-FUNCIONALIDADES-Y-USER-STORIES.md`
- `02-ARQUITECTURA-Y-DIAGRAMAS.md`
- `03-MODELO-DE-DATOS.md`
- `04-CONTRATOS-REST.md`
- `05-TESTING.md`
- `06-DECISIONES-Y-LIMITACIONES.md`
