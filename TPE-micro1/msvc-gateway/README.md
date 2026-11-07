# msvc-gateway

API Gateway de TicketYa para la Entrega 1. Es el único punto de entrada
externo y reenvía las solicitudes HTTP a los microservicios responsables,
sin conocer su lógica de negocio ni acceder a sus bases de datos.

## Tecnología

- Java 21
- Spring Boot 3.5.6
- Spring Cloud 2025.0.3
- Spring Cloud Gateway Server WebFlux
- Spring Boot Actuator

El Gateway es reactivo y no mezcla WebMVC. No contiene controladores,
entidades, repositorios ni base de datos.

## Microservicios y rutas públicas

| Servicio | Puerto local | Prefijos públicos |
|---|---:|---|
| Eventos y Lugares | 8081 | `/eventos/**`, `/lugares/**`, `/sectores/**`, `/asientos/**`, `/organizadores/**`, `/participantes/**` |
| Reservas | 8080 | `/reservas/**`, `/disponibilidad/**` |
| Compras y Pagos | 8082 | `/compras/**` |
| Entradas y Devoluciones | 8083 | `/entradas/**`, `/devoluciones/**` |

Las rutas originales se conservan. Por ejemplo,
`GET http://localhost:8084/eventos/publicados` se reenvía como
`GET http://localhost:8081/eventos/publicados`.

Los servicios con Springdoc exponen su documentación mediante:

- `http://localhost:8084/eventos/v3/api-docs`
- `http://localhost:8084/reservas/v3/api-docs`
- `http://localhost:8084/compras/v3/api-docs`

`msvc-entradas` no incluye actualmente una dependencia de Springdoc, por lo
que no se anuncia una especificación OpenAPI para ese servicio.

## Ejecución desde VS Code

Desde esta carpeta:

```powershell
.\mvnw.cmd spring-boot:run
```

También puede ejecutarse con Maven instalado:

```powershell
mvn spring-boot:run
```

El Gateway escucha en `http://localhost:8084`.

## Variables de entorno

Las variables permiten cambiar las direcciones sin modificar el código:

```text
PORT=8084
EVENTOS_URL=http://localhost:8081
RESERVAS_URL=http://localhost:8080
COMPRAS_URL=http://localhost:8082
ENTRADAS_URL=http://localhost:8083
```

Todos esos valores tienen defaults para ejecución local.

## Ejemplos para Postman

```text
GET  http://localhost:8084/eventos/publicados
GET  http://localhost:8084/disponibilidad?eventoId=1&sectorId=2
POST http://localhost:8084/reservas
POST http://localhost:8084/compras
POST http://localhost:8084/entradas/emitir
GET  http://localhost:8084/devoluciones/pendientes
```

Para requests `POST`, `PUT` o `PATCH` se envía el mismo JSON que espera el
microservicio original. El Gateway conserva método, parámetros, encabezados,
cuerpo y código de respuesta. Los errores de conexión tienen timeout para no
bloquear indefinidamente.

## Verificación

Health check:

```text
GET http://localhost:8084/actuator/health
```

Debe responder `200` mientras el Gateway esté iniciado. Para probar el
enrutamiento completo, deben estar levantados los microservicios destino.
