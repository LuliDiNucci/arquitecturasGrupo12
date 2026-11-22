# TicketYa — Microservicio de Notificaciones

Microservicio responsable de registrar y enviar correos relacionados con compras confirmadas, cancelaciones, modificaciones y eventos de artistas favoritos.

## Tecnologías

Java 21, Spring Boot, Spring Data JPA, MySQL, Spring Mail, Spring Security y OpenAPI.

## Configuración

- **Puerto:** `8087`.
- **Base de datos:** MySQL (`ticketya_notificaciones`).
- **Envío de correos:** SMTP.
- **Seguridad:** JWT para la comunicación entre microservicios.

Las conexiones se configuran mediante las variables `DB_URL`, `DB_USER`, `DB_PASSWORD`, `SMTP_HOST`, `SMTP_PORT` y las credenciales correspondientes.

## Ejecución

Con la base de datos y el servidor SMTP disponibles:

```powershell
.\mvnw.cmd spring-boot:run
```

Para utilizar el perfil local con H2:

```powershell
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=local"
```

El perfil local utiliza una base H2 almacenada en `./data/`.

## Pruebas

```powershell
.\mvnw.cmd test
```

## Documentación

- `docs/01-FUNCIONALIDADES-Y-USER-STORIES.md`
- `docs/02-ARQUITECTURA-Y-DIAGRAMAS.md`
- `docs/03-MODELO-DE-DATOS.md`
- `docs/04-CONTRATOS-REST.md`
- `docs/05-TESTING.md`
- `docs/openapi.json`

Swagger UI: `http://localhost:8087/swagger-ui.html`.

En desarrollo puede utilizarse Mailpit como servidor SMTP de prueba. Para entregar correos a direcciones reales se requiere configurar un proveedor SMTP que permita ese envío.