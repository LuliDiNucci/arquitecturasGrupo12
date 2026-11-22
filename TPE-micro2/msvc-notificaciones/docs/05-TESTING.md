# Pruebas — Notificaciones

## Estrategia

El microservicio cuenta con pruebas unitarias y de integración implementadas con JUnit, Mockito, Spring Boot Test, H2 y GreenMail.

H2 permite comprobar la persistencia sin depender de MySQL durante los tests. GreenMail proporciona un servidor SMTP de prueba.

## Resultados

Los reportes de Maven incluidos en el proyecto registran **32 pruebas ejecutadas, sin fallas ni errores**.

| Clase | Pruebas | Contenido |
|---|---:|---|
| `NotificacionServiceTest` | 8 | Registro de avisos, idempotencia, modificaciones y artistas favoritos |
| `NotificacionApiIntegrationTest` | 6 | Contratos HTTP, validaciones, envío de correos, concurrencia y OpenAPI |
| `ProcesadorNotificacionesIntegrationTest` | 3 | Reintentos, agotamiento de intentos y procesamiento concurrente |
| `NotificationSecurityIntegrationTest` | 15 | Autenticación JWT, permisos, aislamiento de datos y trazabilidad |
| **Total** | **32** | **0 fallas y 0 errores** |

## Escenarios destacados

- Registro de una compra confirmada y posterior entrega del correo.
- Prevención de notificaciones duplicadas ante solicitudes simultáneas.
- Diferenciación de modificaciones mediante la versión del evento.
- Registro de avisos de artistas favoritos únicamente cuando coinciden el artista y la ciudad.
- Recuperación del envío después de un fallo temporal de SMTP.
- Cambio a estado `FALLIDA` cuando se agotan los intentos.
- Prevención de dos envíos simultáneos de la misma notificación.
- Rechazo de accesos sin autenticación o con permisos insuficientes.

## Ejecución

Desde la carpeta `msvc-notificaciones`:

```bash
mvn clean test
```

Las pruebas se encuentran en `src/test/java/com/ticketya/notificaciones`.