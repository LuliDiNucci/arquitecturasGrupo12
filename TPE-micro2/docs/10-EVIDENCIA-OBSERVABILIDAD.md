# Evidencia y límites de verificación

Fecha de validación: **19 de noviembre de 2026**, zona America/Buenos_Aires.
Código base: `30f40ea61117d6ff2e70d327fd36bb054a310510`. No se hicieron commits ni push.

## Compilación y pruebas automatizadas

Comando final ejecutado desde `TPE-micro2`:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\Verify-Security.ps1 -Service todos -MavenRepository C:\Users\usuario\.m2\repository -Offline
```

Cada módulo completó `mvn clean install`: compilar, ejecutar tests, empaquetar e instalar
el artefacto localmente. No se deshabilitaron tests ni filtros de seguridad.

| Módulo | Tests | Fallos | Errores | Omitidos | Build |
|---|---:|---:|---:|---:|---|
| `observability` | 22 | 0 | 0 | 0 | Pasado |
| `msvc-usuarios` | 31 | 0 | 0 | 0 | Pasado |
| `msvc-eventosylugares` | 171 | 0 | 0 | 0 | Pasado |
| `msvc-reservas` | 64 | 0 | 0 | 0 | Pasado |
| `msvc-compras` | 84 | 0 | 0 | 0 | Pasado |
| `msvc-entradas` | 85 | 0 | 0 | 0 | Pasado |
| `msvc-gateway` | 14 | 0 | 0 | 0 | Pasado |
| `msvc-notificaciones` | 31 | 0 | 0 | 0 | Pasado |
| `msvc-fila-virtual` | 21 | 0 | 0 | 0 | Pasado |
| `msvc-recomendaciones` | 4 | 0 | 0 | 0 | Pasado |
| **Total** | **527** | **0** | **0** | **0** | **10 módulos** |

Los XML originales están en `target/surefire-reports/TEST-*.xml` de cada módulo y las
salidas Maven en `build-install.log`. [La evidencia JSON](evidence/observabilidad-resultados.json)
conserva un resumen por suite, resultados de build y el resultado del smoke; no incluye
claves ni tokens. Los tests `*IT` optativos de MySQL no forman parte de esos 527 tests
y se indican expresamente como no ejecutados abajo.

## Las nueve pruebas solicitadas

| Prueba | Endpoint / rol | Esperado | Obtenido y evidencia verificable | Estado |
|---|---|---|---|---|
| 1. Sin JWT | GET `/usuarios/me`, `/compras/1`, `/reservas/1`, `/entradas/1`, `/notificaciones/1`; anónimo | 401 y correlación en respuesta | 401 en tests directos y Gateway. `ServiceSecurityIntegrationTest.directRequestsRequireCryptographicallyValidToken`, `MsvcGatewayApplicationTests.rechazaTokensInvalidosYExpirados`, `NotificationSecurityIntegrationTest.clientsReadOnlyOwnNotificationsAndCannotForgeIdentity` y smoke. | Pasada |
| 2. Cliente sobre recurso ajeno | GET entrada/compra/reserva/notificación ajena; CLIENTE | 403 sin ejecutar operación | 403. `rejectsForeignEntriesForgedHeaderAndForeignReturn`, `rejectsForgedClientAndForeignPurchaseIncludingPayments`, `clientsReadOnlyOwnNotificationsAndCannotForgeIdentity`, `RecommendationSecurityTest.protectsBothApisAgainstMissingTokenAndForeignOwner`; también contra entrada persistida y por HTTP real. | Pasada |
| 3. Organizador sobre evento ajeno | PUT `/eventos/{id}`, `/{id}/precios`; PATCH publicar; ORGANIZADOR ajeno | 403 | 403. `ar.edu.unicen.ticketya.eventos.security.ServiceSecurityIntegrationTest.organizerCannotModifyOrPublishForeignEvent` y `organizerCannotCreateWithSomeoneElsesOrganizerOrTransferOwnEvent`. Conservados y ejecutados. | Pasada |
| 4. QR autorizado | POST `/entradas/validar`; PERSONAL_ACCESO asignado por ADMINISTRADOR | 200, estado UTILIZADA; segundo uso 409 | 200/409. `MsvcEntradasApplicationTests.qrRequiresStaffAssignedToThisEventAndCannotBeUsedTwice` usa una entrada persistida y el servicio real. Se reprodujo además por HTTP entre procesos en el smoke. | Pasada |
| 5. Cliente valida QR | POST `/entradas/validar`; CLIENTE | 403 | 403 en Gateway, servicio directo, test con BD y smoke. Personal sin asignación o con asignación revocada también obtiene 403. | Pasada |
| 6. Trazabilidad de compra | POST compra/pago/confirmación; CLIENTE y credenciales internas | Mismo ID en llamadas y logs de varios servicios | Mismo ID en Gateway, Compras, Reservas, Eventos, Entradas, Usuarios y Notificaciones; interceptores Feign/RestClient y outbox incluidos. `HttpClientCorrelationTest`, tests de Gateway y `trace-evidence.json` del smoke. | Pasada |
| 7. Fallo de dependencia | POST `/reservas` con el proceso de Eventos detenido por el smoke; CLIENTE | Error y logs con mismo ID | Gateway y Reservas responden/registran 503; el cliente Feign registra 502/ConnectException con la misma correlación. No se crea un endpoint artificial de error. | Pasada |
| 8. Notificaciones | Cuatro POST internos y GET privado; anónimo, roles humanos y servicios | 401/403 según identidad; permitido sólo para emisor/titular | `NotificationSecurityIntegrationTest`: JWT con firma ajena, expirado, sin exp, con audiencia/emisor/roles incorrectos → 401; humanos en POST → 403; servicio equivocado → 403; titular → 200. Tests previos SMTP/concurrencia conservados. Smoke con JWT de servicio emitido realmente por Usuarios y correo enviado. | Pasada |
| 9. Consulta central de dos servicios | Loki `/loki/api/v1/query_range` y Grafana Explore | Registros de dos microservicios consultables por el mismo ID | No se pudo iniciar la verificación: el motor Docker Linux no estaba iniciado. `Verify-CentralizedLogs.ps1` y `Smoke-Security.ps1 -VerifyLoki` implementan la comprobación reproducible. Tener logs JSON o un Compose válido no se presenta como ingesta exitosa. | **No ejecutada** |

## Smoke con procesos reales

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\Smoke-Security.ps1 -MavenRepository C:\Users\usuario\.m2\repository
```

Resultado: **PASS, 69 verificaciones HTTP**. Levanta siete aplicaciones reales con
Tomcat/Netty, cada una con H2 aislada, y un SMTP GreenMail. Usa registro/login, JWT
firmados y el intercambio real de credenciales de servicio. No reemplaza los filtros
de seguridad ni los clientes HTTP por mocks. Las bases se destruyen con sus procesos.

La prueba mantiene el circuito previo de devolución y cancelación; el escenario QR
usa otro evento para no alterar sus reglas de reintegro. Incluye revisión de que los
tokens y secretos empleados no aparezcan en los registros recolectados.

La salida exacta e IDs de la última ejecución están en
[`evidence/observabilidad-resultados.json`](evidence/observabilidad-resultados.json).
Una selección de registros originales por servicio está en
[`evidence/traza-compra.jsonl`](evidence/traza-compra.jsonl), y el error real en
[`evidence/traza-error.jsonl`](evidence/traza-error.jsonl). Los tiempos son mediciones
del proceso y no ejemplos inventados.

## Otras verificaciones

- 16 solicitudes reactivas concurrentes con cambio de scheduler conservan sus propios
  usuarios e IDs: `ReactiveHttpLoggingTest`.
- Encabezados múltiples, vacíos, mayores a 64 caracteres y con controles se sustituyen
  por UUID: `HttpLoggingTest` y pruebas de Gateway sobre HTTP real.
- 200, 201, 400, 401, 403 y 500 generan un solo evento por solicitud y limpian/restauran
  MDC. También hay casos de excepción no manejada y finalización Servlet asíncrona.
- `NotificationSecurityIntegrationTest.emitsRealStructuredJsonIncludingHandledErrorsWithoutSensitiveFields`
  lee la salida JSON real del encoder: servicio, usuario, fecha y duración numérica.
- El envío programado conserva el ID capturado en la entidad:
  `persistsCorrelationAndRestoresItDuringActualBackgroundDelivery`; el smoke también
  comprueba propagación durante avisos y cancelaciones.
- `docker compose --env-file .local-observability/compose-validation/.env -f docker-compose.yml -f docker-compose.observability.yml config --quiet`:
  **exit 0** con valores efímeros generados para la validación.
- `git diff --check`: ejecutado al cierre; resultado conservado en la evidencia JSON.

Durante el desarrollo se detectó una escritura JSON fallida por claves duplicadas
MDC/SLF4J y se corrigió; por eso se agregó el test del encoder real. Fila Virtual
también requería MySQL y un PEM local para iniciar sus tests originales: se añadieron
fixtures de H2/clave efímera sin cambiar sus aserciones ni relajar JWT. Los resultados
de esta tabla son los posteriores a esas correcciones.

## Pendientes y alcance de las pruebas

1. **Loki/Alloy/Grafana en ejecución:** `docker version` devolvió que no existe el pipe
   `dockerDesktopLinuxEngine`. No se ejecutaron `loki -verify-config`, `alloy validate`,
   la ingesta ni la consulta central. La guía contiene los comandos exactos; no se
   afirma que la prueba 9 haya pasado.
2. **Imágenes y bases reales:** no se ejecutó `docker compose up --build`. El smoke
   usa H2 y SMTP real de pruebas, no prueba MySQL/MariaDB ni MongoDB en contenedores.
   Recomendaciones tiene pruebas unitarias y MVC de seguridad, sin una instancia Mongo.
3. **Concurrencia MySQL optativa:** `reservas.services.ConcurrenciaMySQLIT` y
   `compras.services.PagoConcurrenciaMySQLIT` se conservaron y no se ejecutaron en esta
   validación. Surefire no los selecciona en su patrón por defecto. El primero también
   exige `TICKETYA_MYSQL_TEST=true`, como ya exigía originalmente. No se deshabilitaron
   para conseguir los resultados de la tabla. Usar exclusivamente bases de prueba y
   seguir los documentos de testing propios de esos servicios.
4. **Fila Virtual con MySQL:** sus 21 tests pasan con H2 en modo MySQL, incluidas las
   aserciones concurrentes existentes. Puede repetirse con `FILA_TEST_DB_URL`,
   `FILA_TEST_DB_DRIVER=com.mysql.cj.jdbc.Driver`, `FILA_TEST_DB_USER` y
   `FILA_TEST_DB_PASSWORD`; esa variante no se ejecutó.

## Explicación para la defensa oral

"Agregué un filtro común que registra automáticamente cada petición, con su usuario
autenticado, resultado y duración. En el Gateway usé un filtro reactivo porque las
peticiones pueden cambiar de hilo. El identificador viaja en X-Correlation-ID y los
clientes HTTP lo conservan; los trabajos que se reintentan lo guardan en la base.
Así puedo buscar una operación completa sin confundir solicitudes de distintos usuarios.

Cada servicio produce JSON. Alloy está configurado para leer sus archivos, Loki para
almacenarlos y Grafana para buscarlos desde un mismo lugar. Probé los logs y su
correlación entre siete aplicaciones; la consulta real en Loki queda pendiente de
ejecutar con Docker iniciado.

Reutilicé los JWT y las credenciales de servicio existentes. Cerré el envío anónimo de
notificaciones y la lectura de avisos ajenos. También exigí que el personal esté asignado
al evento antes de validar QR, además de tener el rol correcto. Las verificaciones se
aplican en cada servicio, incluso si se accede sin pasar por el Gateway."
