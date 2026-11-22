# TicketYa — API Gateway

## Descripción

`msvc-gateway` es el punto de entrada HTTP de TicketYa. Recibe las solicitudes del frontend y las dirige al microservicio correspondiente.

Centraliza el enrutamiento y aplica controles de autenticación y autorización. Cada microservicio conserva la responsabilidad sobre su lógica de negocio, persistencia y validaciones específicas.

## Tecnologías

- Java 21
- Spring Boot 3.5.6
- Spring Cloud Gateway WebFlux
- Spring Security y JWT
- Springdoc OpenAPI
- Spring Boot Actuator

El Gateway utiliza una arquitectura reactiva y no posee base de datos propia.

## Configuración

- **Puerto:** `8084`.
- **Autenticación:** JWT firmado con RS256.
- **Comunicación:** HTTP REST con los microservicios.

## Microservicios conectados

| Microservicio | Puerto |
|---|---:|
| Usuarios | 8085 |
| Eventos y Lugares | 8081 |
| Reservas | 8080 |
| Compras y Pagos | 8082 |
| Entradas | 8083 |
| Recomendaciones | 8086 |
| Notificaciones | 8087 |
| Fila Virtual | 8088 |

Las direcciones se configuran mediante variables de entorno, como `USUARIOS_URL`, `EVENTOS_URL`, `RESERVAS_URL`, `COMPRAS_URL` y las correspondientes a los demás servicios.

## Ejecución

Desde la carpeta `msvc-gateway`:

```powershell
mvn spring-boot:run
```

El Gateway queda disponible en:

`http://localhost:8084`

## Verificación

Estado del Gateway:

`http://localhost:8084/actuator/health`

Swagger UI:

`http://localhost:8084/swagger-ui.html`

Swagger permite seleccionar la documentación de los distintos microservicios.

El endpoint de salud verifica el estado del Gateway. Para utilizar las funcionalidades del sistema, también deben encontrarse disponibles los servicios de destino.

## Pruebas

```powershell
mvn clean test
```

La documentación técnica se encuentra en:

- `docs/01-ARQUITECTURA-Y-SEGURIDAD.md`
- `docs/02-PRUEBAS.md`