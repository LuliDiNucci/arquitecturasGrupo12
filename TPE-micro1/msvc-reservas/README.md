# TicketYa – `msvc-reservas` (Entrega 1)

Microservicio Java 21 / Spring Boot 4.1.2 / MySQL. Administra reservas temporales (10 min), ocupación de sectores numerados o por capacidad, confirmación, liberación por pago rechazado y devolución individual de entradas. **No** procesa pagos ni emite QR.

## Requisitos

- JDK 21, Maven y MySQL levantado.
- Crear base de datos: `CREATE DATABASE IF NOT EXISTS ticketya_reservas;`
- Configurar `DB_USERNAME` y `DB_PASSWORD` mediante variables de entorno (la configuración local usa root y contraseña vacía por defecto).
- `msvc.eventos.url` apunta por defecto a `http://localhost:8081`: ajustar cuando el servicio esté disponible.

En PowerShell, desde esta carpeta:

```powershell
$env:DB_USERNAME="root"
$env:DB_PASSWORD="MI_CONTRASENA"
mvn clean test
mvn spring-boot:run
```

Los endpoints del controller se exponen en `http://localhost:8080`; no se “suben” manualmente. Para probar `POST /reservas` o `GET /disponibilidad`, hace falta Eventos y Lugares real o un simulador compatible. Los métodos de `EventoClient` están **implementados mediante OpenFeign**; la compatibilidad de sus rutas y DTO debe comprobarse contra el servicio real.

## Endpoints

| HTTP | Ruta | Descripción |
| POST | `/reservas` | Crear temporal; reintentos llevan la misma `claveIdempotencia` |
| GET | `/reservas/{id}` | Consultar reserva |
| POST | `/reservas/{id}/confirmar` | **Interno**: pago aprobado |
| POST | `/reservas/{id}/liberar` | **Interno**: pago rechazado |
| POST | `/reservas/{id}/devolver-entrada` | **Interno**: liberar UNA entrada aprobada |
| POST | `/reservas/evento/{eventoId}/cancelar` | **Interno**: cancelar temporales, bloquear nuevas |
| GET | `/disponibilidad?eventoId=20&sectorId=5` | Disponibilidad informativa (no bloquea) |

Ver `docs/openapi.yaml` (OpenAPI 3.0) y `docs/TicketYa-Reservas.postman_collection.json`, importable en Postman. OpenAPI YAML puede abrirse en Swagger Editor; no se instala interfaz Swagger UI automáticamente.

## Comportamiento y límites

- **Reservas vencidas:** el vencimiento es lógico a los 10 minutos. Las consultas de disponibilidad excluyen las reservas `ACTIVA` cuya fecha ya pasó, por lo que la capacidad queda disponible aunque MySQL conserve temporalmente ese estado. El equipo informó que verificó manualmente la liberación de capacidad. No se implementó Scheduler porque no es necesario para liberar la disponibilidad; una operación posterior persiste el estado `VENCIDA`.
- **Bloqueos e idempotencia:** serialización por evento con `PESSIMISTIC_WRITE` sobre `bloqueo_evento` y por clave mediante `clave_idempotencia_bloqueo` (MySQL `INSERT IGNORE`). Orden de adquisición: clave, evento. El bloqueo no cubre transacciones remotas.
- **Cancelación:** la tabla de bloqueo guarda `cancelado=true`; es irreversible en esta versión (no reutilizar IDs de evento). El flujo de reintegros masivos corresponde a Compras/Entradas.
- **Devoluciones:** `entradaId` se usa como registro de idempotencia. **Reservas no puede demostrar titularidad/aprobación** sin que Entradas provea un contrato interno confiable. Proteger el endpoint en el gateway/red. El límite de 72 h lo valida Entradas.
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

La prueba está escrita, pero **no se afirma que haya sido ejecutada en todos los entornos**: necesita MySQL local. No ejecutar contra una BD con datos importantes porque limpia un evento de prueba reservado.

## Estado de las integraciones

- **Eventos y Lugares:** `EventoClient` y sus operaciones Feign están implementados. Durante el desarrollo se utilizaron simuladores; la compatibilidad y el flujo con el servicio real deben verificarse en una prueba integrada.
- **Compras:** Reservas expone los endpoints de confirmar y liberar; el cliente de Compras para invocarlos está desarrollado. Falta registrar una prueba integrada del flujo de pago aprobado/rechazado y la confirmación/liberación resultante.
- **Entradas:** Reservas expone el endpoint interno de liberación individual de una entrada. La validación de titularidad, aprobación y plazo pertenece a los servicios responsables. La coordinación completa de devolución/cancelación debe probarse integrada.
- **Eventos → Reservas:** están implementados los endpoints internos para habilitar y cancelar un evento; debe verificarse el envío real de las notificaciones y sus reintentos.
- **Gateway:** la configuración y las reglas de seguridad del Gateway no forman parte de este microservicio. Debe comprobarse que los endpoints internos no queden expuestos a clientes no autorizados.

La existencia de endpoints y clientes implementados no equivale a una prueba de extremo a extremo. Véase `docs/contrato-eventos.md` y la documentación de testing.