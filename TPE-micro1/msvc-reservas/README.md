# TicketYa – `msvc-reservas` (Entrega 1)

Microservicio Java 21 / Spring Boot 4.1.2 / MySQL. Administra reservas temporales (10 min), ocupación de sectores numerados o por capacidad, confirmación, liberación por pago rechazado y devolución individual de entradas. **No** procesa pagos ni emite QR.

## Requisitos

- JDK 21, Maven y MySQL levantado
- Crear base de datos: `CREATE DATABASE IF NOT EXISTS ticketya_reservas;`
- Configurar `DB_USERNAME` y `DB_PASSWORD` mediante variables de entorno (la configuración local usa root y contraseña vacía por defecto)
- `msvc.eventos.url` apunta por defecto a `http://localhost:8081`: ajustar cuando el compañero tenga el servicio funcionando

En PowerShell, desde esta carpeta:

```powershell
$env:DB_USERNAME="root"
$env:DB_PASSWORD="MI_CONTRASENA"
mvn clean test
mvn spring-boot:run
```

Los endpoints del controller se exponen en `http://localhost:8080`; no se “suben” manualmente. Para probar `POST /reservas` o `GET /disponibilidad`, hace falta Eventos real o simulado; los métodos en `EventoClient` son contratos **propuestos**, pendientes de acordar con el equipo.

## Endpoints

| HTTP | Ruta | Descripción |
|---|---|---|
| POST | `/reservas` | Crear temporal; reintentos llevan la misma `claveIdempotencia` |
| GET | `/reservas/{id}` | Consultar reserva |
| POST | `/reservas/{id}/confirmar` | **Interno**: pago aprobado |
| POST | `/reservas/{id}/liberar` | **Interno**: pago rechazado |
| POST | `/reservas/{id}/devolver-entrada` | **Interno**: liberar UNA entrada aprobada |
| POST | `/reservas/evento/{eventoId}/cancelar` | **Interno**: cancelar temporales, bloquear nuevas |
| GET | `/disponibilidad?eventoId=20&sectorId=5` | Disponibilidad informativa (no bloquea) |

Ver `docs/openapi.yaml` (OpenAPI 3.0) y `docs/TicketYa-Reservas.postman_collection.json`, importable en Postman. OpenAPI YAML puede abrirse en Swagger Editor; no se instala interfaz Swagger UI automáticamente.

## Comportamiento y límites

- **Reservas vencidas:** las consultas de disponibilidad excluyen `ACTIVA` cuya fecha ya pasó, aunque MySQL conserva el estado hasta la próxima lectura relevante. No se implementó Scheduler.
- **Bloqueos e idempotencia:** serialización por evento con `PESSIMISTIC_WRITE` sobre `bloqueo_evento` y por clave mediante `clave_idempotencia_bloqueo` (MySQL `INSERT IGNORE`). Orden de adquisición: clave, evento. El bloqueo no cubre transacciones remotas.
- **Cancelación:** la tabla de bloqueo guarda `cancelado=true`; es irreversible en esta versión (no reutilizar IDs de evento). El flujo de reintegros masivos corresponde a Compras/Entradas.
- **Devoluciones:** `entradaId` se usa como registro de idempotencia. **Reservas no puede demostrar titularidad/aprobación** sin que Entradas provea un contrato interno confiable. Proteger el endpoint en el gateway/red. El límite 72 h lo valida Entradas.
- **Configuración del sector:** se consulta que `PrecioSectorDTO` exista para el par evento-sector.
- **Persistencia:** `ddl-auto=update` es para desarrollo; revisar esquema antes de usar con datos existentes y migrar a una herramienta como Flyway si se necesita.

## Pruebas

`mvn test` ejecuta las pruebas unitarias con repositorios y Feign simulados. Se incluyó `ConcurrenciaMySQLIT.java`: simula diez solicitudes simultáneas del mismo asiento usando transacciones reales sobre **MySQL separado para tests**. Por seguridad se omite salvo que se habilite explícitamente:

```powershell
# Crear antes ticketya_reservas_test en MySQL y configurar credenciales
$env:TICKETYA_MYSQL_TEST="true"
$env:TEST_DB_URL="jdbc:mysql://localhost:3306/ticketya_reservas_test"
$env:TEST_DB_USER="root"
$env:TEST_DB_PASSWORD="MI_PASSWORD"
mvn -Dtest=ConcurrenciaMySQLIT test
```

La prueba está escrita, pero **no ha sido ejecutada aquí**: necesita tu MySQL local. No ejecutar contra una BD con datos importantes porque limpia un evento de prueba reservado.

## Pendientes de integración del grupo

Consultar `docs/contrato-eventos.md`. También falta comprobar el flujo completo con el Gateway, la pasarela simulada, Compras y Entradas; no se incluyen en este ZIP.
