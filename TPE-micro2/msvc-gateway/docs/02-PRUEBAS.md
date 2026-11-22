# Pruebas — API Gateway

## Estrategia

El Gateway cuenta con pruebas de integración implementadas mediante JUnit, Spring Boot Test y WebTestClient.

Se utilizan servidores HTTP simulados para comprobar el enrutamiento y las respuestas sin depender de microservicios externos reales.

## Resultados

Los reportes de Maven incluidos en el proyecto registran:

| Resultado | Cantidad |
|---|---:|
| Tests ejecutados | 14 |
| Tests aprobados | 14 |
| Fallas | 0 |
| Errores | 0 |
| Omitidos | 0 |

## Casos verificados

### Enrutamiento

- Redirección de solicitudes a Eventos y Lugares.
- Envío de parámetros y cuerpos JSON a Reservas.
- Enrutamiento de operaciones de Compras y Entradas.
- Conservación de encabezados y tokens hacia los servicios de destino.
- Propagación de códigos de error devueltos por los microservicios.

### Seguridad

- Rechazo de solicitudes sin autenticación.
- Rechazo de tokens inválidos o vencidos.
- Verificación de permisos según roles.
- Protección de operaciones administrativas.
- Acceso de Personal de Acceso a la validación de entradas.
- Rechazo de escrituras de notificaciones con tokens de usuarios comunes.

### Disponibilidad y documentación

- Respuesta del endpoint `/actuator/health`.
- Comportamiento cuando un servicio de destino no está disponible.
- Disponibilidad de Swagger UI y su configuración.

### Trazabilidad

- Propagación de `X-Correlation-ID` hacia los microservicios.
- Generación de identificadores cuando faltan o son inválidos.
- Inclusión del identificador en respuestas exitosas y de error.

## Ejecución

Desde `msvc-gateway`:

```powershell
mvn clean test
```

La clase principal de pruebas es `MsvcGatewayApplicationTests`.