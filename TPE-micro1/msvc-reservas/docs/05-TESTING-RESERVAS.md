# 05 — Testing de `msvc-reservas`

**Proyecto:** TicketYa · Entrega 1  
**Tecnologías:** JUnit 5, Mockito, Spring Boot Test, Maven, MySQL y pruebas HTTP con PowerShell/Postman.  
**Alcance de este documento:** pruebas de la lógica de Reservas y de sus endpoints. No acredita una integración completa con Compras, Entradas, API Gateway ni Eventos y Lugares real.

## 1. Estrategia de pruebas

Se emplean tres niveles complementarios:

1. **Pruebas unitarias:** `ReservaServiceImplTest.java` utiliza JUnit 5 y Mockito para aislar el servicio de repositorios y del cliente Feign.
2. **Prueba de integración/concurrencia:** `ConcurrenciaMySQLIT.java` utiliza una base MySQL **exclusiva de pruebas**, un contexto Spring Boot real y un `EventoClient` simulado. Prueba diez intentos simultáneos sobre el mismo asiento.
3. **Pruebas manuales HTTP:** se ejecutaron llamadas REST contra Reservas, con MySQL y un simulador HTTP de Eventos y Lugares. Se revisaron códigos de respuesta, contenido JSON y persistencia desde MySQL Workbench.

> Los resultados manuales corresponden a las verificaciones realizadas durante el desarrollo; no equivalen a pruebas automatizadas en CI. Las pruebas de integración entre los microservicios reales siguen pendientes.

## 2. Pruebas unitarias implementadas

Archivo: `src/test/java/com/ticketya/reservas/services/ReservaServiceImplTest.java`.

| Test | Escenario | Comprobación |
|---|---|---|
| `rechazaSolicitudIncompletaSinGuardar` | Falta el ID del cliente | Excepción por argumento inválido; no se guarda la reserva |
| `rechazaReservaSiEventoCanceladoLocalmente` | La fila `bloqueo_evento` indica cancelación | Rechazo de la creación; no se guarda la reserva |
| `noConfirmaReservaVencida` | Reserva `ACTIVA` con fecha de vencimiento anterior al momento actual | Se rechaza la confirmación; el objeto pasa a `VENCIDA` |
| `cancelacionMarcaElEventoYCancelaActivas` | Cancelación de un evento con reserva activa | El evento se marca cancelado y la reserva pasa a `CANCELADA` |

Los repositorios y `EventoClient` se simulan con Mockito. Estos tests comprueban reglas específicas de `ReservaServiceImpl`, no sustituyen las pruebas SQL ni las pruebas REST.

**Ejecución:**

```powershell
mvn test
```

Para ejecutar solo la clase unitaria:

```powershell
mvn "-Dtest=ReservaServiceImplTest" test
```

## 3. Prueba de concurrencia con MySQL

Archivo: `src/test/java/com/ticketya/reservas/services/ConcurrenciaMySQLIT.java`.

### Objetivo

Asegurar que, si diez clientes distintos intentan reservar **el mismo asiento** al mismo tiempo, **solo uno** consiga crear la reserva. Los demás deben ser rechazados por ocupación; no pueden existir dos reservas exitosas del mismo asiento.

### Implementación

- Ejecuta Spring Boot y repositorios JPA reales contra MySQL.
- Simula `EventoClient` mediante `@MockitoBean`, porque el objeto de la prueba es la concurrencia **dentro de Reservas**, no la disponibilidad de la API remota.
- Elimina los datos residuales del **evento de prueba** y configura el catálogo simulado.
- Habilita el evento antes de lanzar los hilos para que exista la fila de bloqueo.
- Usa `ExecutorService` con diez trabajadores y `CountDownLatch` para comenzar de forma simultánea.
- Cada trabajador emplea una `claveIdempotencia` distinta, por lo que se comprueba ocupación concurrente y no duplicación por reintento.
- Verifica `assertEquals(1, exitos.get())`.

### Configuración segura

**Crear previamente una base dedicada** y no ejecutar esta prueba contra `ticketya_reservas`, ya que el test borra registros del evento de prueba:

```sql
CREATE DATABASE IF NOT EXISTS ticketya_reservas_test;
```

En PowerShell, dentro de `msvc-reservas`:

```powershell
$env:TICKETYA_MYSQL_TEST="true"
$env:TEST_DB_URL="jdbc:mysql://localhost:3306/ticketya_reservas_test"
$env:TEST_DB_USER="root"
$env:TEST_DB_PASSWORD="TU_CONTRASENA_MYSQL"
mvn "-Dtest=ConcurrenciaMySQLIT" test
```

Si la contraseña es vacía, configurar `$env:TEST_DB_PASSWORD=""`. La prueba usa `@EnabledIfEnvironmentVariable`, por lo que **se omite** si `TICKETYA_MYSQL_TEST` no vale `true`.

**Resultado observado durante el desarrollo:** `BUILD SUCCESS` después de corregir el orden de preparación del `EventoClient` simulado y habilitar el evento de prueba. La afirmación de concurrencia comprobada se limita al escenario ensayado (10 intentos por un asiento sobre MySQL local); no es una garantía universal de ausencia de interbloqueos en toda carga o topología.

## 4. Registro de pruebas manuales de API

Se utilizó el simulador de Eventos y Lugares en `http://localhost:8081` y `msvc-reservas` en `http://localhost:8080`. Los IDs 20 y 21 son **datos de prueba**, no IDs obligatorios del sistema real.

| Caso comprobado | Acción | Resultado observado / esperado |
|---|---|---|
| Reserva inexistente | `GET /reservas/999999` | **404**, `Reserva inexistente` |
| Solicitud sin datos | `POST /reservas` con `{}` | **400**, solicitud incompleta |
| Reserva temporal correcta | `POST /reservas` con evento, sector y asiento válidos | **201**, `ACTIVA`, vencimiento a 10 minutos |
| Disponibilidad numerada | `GET /disponibilidad?eventoId=20&sectorId=5` | Lista de asientos libres disminuye al reservar |
| Asiento ocupado | Otro cliente intenta reservar un asiento ya retenido | **409**, asiento no disponible |
| Idempotencia secuencial | Repetir el mismo `POST /reservas` con igual clave y contenido | Misma reserva, sin duplicar fila |
| Idempotencia simultánea | Dos solicitudes simultáneas con igual clave y contenido | Misma reserva, una fila en MySQL |
| Confirmación | `POST /reservas/{id}/confirmar` | Reserva pasa a `CONFIRMADA` |
| Liberación | `POST /reservas/{id}/liberar` | Reserva pasa a `LIBERADA`; capacidad vuelve a estar disponible |
| Vencimiento | Consultar/confirmar una reserva vencida | Estado `VENCIDA` o **409** al intentar confirmar |
| Sector no numerado | Reservar capacidad en sector Campo | Disminuye el contador sin IDs de asientos |
| Sector sin capacidad suficiente | Solicitud mayor que el remanente | **409** |
| Límite por cliente | Pedir más de 4 entradas para el mismo evento | **400**; exactamente 4 fue admitido |
| Asientos duplicados | Enviar `[25,25]` | **400**, cantidad o asientos inválidos |
| Cantidad cero | Enviar un detalle con `cantidad: 0` | **400**, detalle inválido |
| Cancelación de evento | `POST /reservas/evento/20/cancelar` | Reservas activas canceladas; `cancelado=1` |
| Nueva reserva para evento cancelado | `POST /reservas` para evento 20 | **409**, evento cancelado |
| Habilitar evento inexistente | `POST /reservas/evento/9999/habilitar` | **400**, evento no existe; no se inserta bloqueo |
| Habilitar evento válido | `POST /reservas/evento/21/habilitar` | **204 No Content** |
| Sector inexistente | Reserva sobre `sectorId=9999` de evento 21 | **404**, recurso inexistente en Eventos y Lugares |
| Asiento inexistente | Reserva sobre `asientoId=9999` de evento 21 | **404**, recurso inexistente en Eventos y Lugares |
| Devolución individual | `POST /reservas/4/devolver-entrada` con entrada 1001 | Se registra una vez en `entrada_liberada` y se libera el asiento |
| Reintento de devolución | Repetir el mismo `entradaId` y datos | Sin segunda liberación |
| Devolver entrada no confirmada | Devolución para reserva liberada | **409**, solo entradas confirmadas |
| Disponibilidad de evento cancelado | `GET /disponibilidad?eventoId=20&sectorId=5` | **409**, evento cancelado |
| Disponibilidad de evento habilitado | `GET /disponibilidad?eventoId=21&sectorId=5` | **200**, asientos `[25,26,27]`, 3 lugares |

Las comprobaciones de MySQL realizadas durante las pruebas incluyeron las tablas `reserva`, `bloqueo_evento` y `entrada_liberada`.

### Ejemplos para repetir manualmente

Consulta informativa:

```powershell
Invoke-RestMethod -Uri "http://localhost:8080/disponibilidad?eventoId=21&sectorId=5" -Method GET
```

Creación de reserva — elegir IDs existentes del servicio de Eventos real o del simulador, y una clave nueva:

```powershell
$body = @{
    clienteId = 1
    eventoId = 21
    claveIdempotencia = "PRUEBA-RESERVA-NUEVA-001"
    detalles = @(@{
        sectorId = 5
        cantidad = 1
        asientoIds = @(25)
    })
} | ConvertTo-Json -Depth 10

Invoke-RestMethod -Uri "http://localhost:8080/reservas" -Method POST `
    -ContentType "application/json" -Body $body
```

**Atención:** para pruebas de creación se debe haber ejecutado previamente `POST /reservas/evento/{eventoId}/habilitar`; el evento tiene que existir, estar `PUBLICADO` y no figurar cancelado.

## 5. Pruebas pendientes

Estos flujos **no se consideran validados de punta a punta** en el estado documentado:

- Comunicación contra **Eventos y Lugares real** en lugar del simulador y compatibilidad de DTO/URLs con datos reales.
- Evento publicado → habilitación automática en Reservas, incluyendo caída y reintento de la notificación.
- Evento cancelado → bloqueo de nuevas ventas y coordinación de entradas confirmadas con Entradas/Compras.
- Pago aprobado → confirmación; pago rechazado → liberación; aprobación tardía → compensación/reintegro.
- Emisión de entradas, verificación de titularidad, regla de las 72 horas y aprobación de devoluciones, gestionadas por los servicios correspondientes.
- Autorización de endpoints internos mediante Gateway/red y pruebas de seguridad.
- Pruebas adicionales de alta contención con diferentes eventos, claves repetidas y fallos de red.

## 6. Evidencia y criterios de aceptación

Para una entrega grupal verificable, adjuntar o registrar resultados de `mvn test`, `mvn "-Dtest=ConcurrenciaMySQLIT" test` con MySQL aislado, respuestas HTTP relevantes y evidencias de integración real cuando esté disponible. **No afirmar cobertura total** solo por un `BUILD SUCCESS`: la compilación no demuestra reglas de negocio ni integración entre microservicios.

Documentación relacionada: `docs/openapi.yaml`, `docs/TicketYa-Reservas.postman_collection.json`, `docs/02-ARQUITECTURA-Y-DIAGRAMAS.md` y `docs/03-MODELO-DE-DATOS.md`.
