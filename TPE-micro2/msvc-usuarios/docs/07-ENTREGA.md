# Entrega final — Usuarios, JWT y seguridad

Verificación local: 16/11/2026. Sin commits ni pushes.

## Resultado implementado

Se creó msvc-usuarios independiente (Java 21 / Boot 3.5.6), identidad JPA propia,
registro CLIENTE, BCrypt costo 12, login uniforme y limitado, JWT RS256,
administración de roles/estado y bootstrap sin promoción pública.
Se integraron los cinco puntos de acceso restantes: Gateway reactivo y
cuatro servicios MVC validan firma, exp, issuer, audience, claims, roles y titularidad.
Feign/RestClient usan propagación humana en lecturas y credenciales propias de
servicio para operaciones internas y scheduler. Se preservaron versiones y
contratos de negocio, con los cambios mínimos de autenticación documentados.

Incluye OpenAPI/Swagger con Authorize, 22 requests Postman, Dockerfiles,
Compose con seis aplicaciones y cinco MySQL separados, scripts PowerShell,
generador Java de claves y documentación técnica.

## Verificación ejecutada

| Proyecto | clean compile | clean test | clean package | Tests por suite completa |
|---|---|---|---|---:|
| msvc-usuarios | PASS | PASS | PASS | 30 |
| msvc-eventosylugares | PASS | PASS | PASS | 154 |
| msvc-reservas | PASS | PASS | PASS | 58 |
| msvc-compras | PASS | PASS | PASS | 84 |
| msvc-entradas | PASS | PASS | PASS | 77 |
| msvc-gateway | PASS | PASS | PASS | 11 |
| **Total** | | | | **414** |

Cero fallos, errores o tests omitidos en esas ejecuciones de Surefire. Package
volvió a ejecutar las mismas suites. Los *MySQLIT no están incluidos en ese total.

Los comandos reales se ejecutaron desde cada directorio, mediante
scripts/Verify-Security.ps1, con Maven -B y el caché local configurado:
`-Dmaven.repo.local=C:\Users\usuario\.m2\repository`. Los logs están en
build-compile.log, build-test.log y build-package.log de cada proyecto; los XML en
target/surefire-reports. El script falla si Maven devuelve un código diferente de 0.

La prueba final scripts/Smoke-Security.ps1 pasó **42 verificaciones HTTP**:
seis JVM reales, H2 independiente por servicio, registro/login, Gateway, roles,
propietarios, publicación/habilitación, reserva, compra, pago, confirmación,
emisión, devolución aprobada, reintegro y cancelación notificada por scheduler.
Incluye compra de reserva ajena con 403 conservado desde Reservas hacia Compras.
Registro local: `.local-security-smoke/run-20261116-230656/result.json`.
Los procesos de esa ejecución fueron detenidos al terminar.

Swagger de Usuarios, OpenAPI y la página/configuración agregada del Gateway
están cubiertos por pruebas HTTP. La colección Postman se validó como JSON;
no se afirma una ejecución del runner de Postman.

Dependencias comprobadas dentro del JAR de Usuarios: Boot 3.5.6,
Security 6.5.5, Nimbus JOSE JWT 9.37.4, Springdoc 2.8.16 y MySQL Connector/J 9.4.0.
H2 no se incluye en el JAR productivo.

## Endpoints incorporados

| Método | Ruta | Acceso |
|---|---|---|
| POST | /auth/register | Público; crea sólo CLIENTE |
| POST | /auth/login | Público; valida credenciales |
| POST | /auth/service-token | Credencial Basic de servicio configurado |
| GET | /usuarios/me | Persona autenticada |
| GET | /usuarios/{id} | Titular o ADMINISTRADOR |
| PATCH | /usuarios/{id}/roles | ADMINISTRADOR |
| PATCH | /usuarios/{id}/estado | ADMINISTRADOR |
| PATCH | /organizadores/{id}/usuario | ADMINISTRADOR; en Eventos |

La [matriz exhaustiva de métodos, roles y propietarios](03-PERMISOS.md) cubre
los controllers reales. CLIENTE sólo opera sus recursos; ORGANIZADOR sólo sus
eventos; ADMINISTRADOR administra usuarios, infraestructura, cancelaciones y
devoluciones. PERSONAL_ACCESO no recibe permisos administrativos ni datos de
tickets completos. Los permisos internos usan SERVICE_<id>, no un quinto rol humano.

## Ejecución y Postman

Desde TPE-micro1, iniciar Docker Desktop y, si no hay configuración local,
ejecutar `java scripts/GenerateDevSecrets.java`. Luego:

~~~powershell
docker compose up -d db-usuarios
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\Start-TicketYaService.ps1 -Service usuarios
~~~

El generador ya fue ejecutado en este workspace: .env y .secrets están ignorados
por Git y no se sobrescriben. El launcher usa el MySQL de Compose en 3307.
Para otro MySQL, variables, bootstrap, claves, ejecución completa, Gateway y
contenedores, seguir [05-EJECUCION](05-EJECUCION.md).

Importar [la colección Postman](../../postman/TicketYa-Usuarios-JWT.postman_collection.json).
Usar baseUrl=http://localhost:8085 o http://localhost:8084. El folder Cliente genera
email, registra, hace login y guarda el Bearer automáticamente. Los folders Admin
e Integración requieren las credenciales/servicios locales indicados.
[Contratos y ejemplos JSON](04-API.md).

## Coordinación y límites pendientes

- Docker Compose pasó `docker compose --env-file .env.example config --quiet`.
  No se ejecutaron imágenes/contenedores ni MySQL: Docker informó que no existe
  la tubería dockerDesktopLinuxEngine y no había MySQL local disponible.
  Repetir el flujo con Compose cuando el motor esté iniciado y ejecutar los
  *MySQLIT de concurrencia existentes.
- Asociar los clienteId históricos a usuarios reales antes de reutilizar datos
  de Entrega 1; no se pueden inferir esos vínculos automáticamente.
- Administración debe crear/asignar ORGANIZADOR y vincular usuarioId a la entidad
  de negocio correcta. Ese contrato está implementado y pasó la prueba HTTP.
- QR de ingreso/uso, reportes nuevos, preferencias y fila virtual no tienen
  controllers en el código recibido. Su implementación de negocio queda para
  los servicios responsables; no se inventaron endpoints ni se afirmó su existencia.
  Al incorporar QR, requerir PERSONAL_ACCESO/A y definir asignación por evento.
- El contador de intentos es local a cada instancia. JWT ya emitidos conservan
  sus roles y habilitación hasta exp; no hay revocación inmediata ni refresh token.
  Estas decisiones y el uso de TLS en despliegue están documentados en
  [arquitectura](02-ARQUITECTURA.md).

No quedan integraciones de autenticación diferidas entre los endpoints existentes:
el flujo completo se verificó por HTTP. La limitación de infraestructura anterior
impide afirmar que se verificó su ejecución con MySQL o dentro de contenedores.

## Inventario completo

115 archivos creados y 33 modificados en TPE-micro1.
Se excluyen target, logs, claves locales, .env y reportes de ejecución ignorados.
Los cambios previos fuera del directorio (.vscode/settings.json y TPE-micro1.zip)
se conservaron.

### Archivos creados

- [.dockerignore](../../.dockerignore)
- [.env.example](../../.env.example)
- [.gitignore](../../.gitignore)
- [docker-compose.yml](../../docker-compose.yml)
- [docs/sql/02-organizadores-usuario.sql](../../docs/sql/02-organizadores-usuario.sql)
- [msvc-compras/.dockerignore](../../msvc-compras/.dockerignore)
- [msvc-compras/Dockerfile](../../msvc-compras/Dockerfile)
- [msvc-compras/src/main/java/com/ticketya/compras/security/CurrentIdentity.java](../../msvc-compras/src/main/java/com/ticketya/compras/security/CurrentIdentity.java)
- [msvc-compras/src/main/java/com/ticketya/compras/security/FeignSecurityConfig.java](../../msvc-compras/src/main/java/com/ticketya/compras/security/FeignSecurityConfig.java)
- [msvc-compras/src/main/java/com/ticketya/compras/security/InternalCredentials.java](../../msvc-compras/src/main/java/com/ticketya/compras/security/InternalCredentials.java)
- [msvc-compras/src/main/java/com/ticketya/compras/security/JwtSupport.java](../../msvc-compras/src/main/java/com/ticketya/compras/security/JwtSupport.java)
- [msvc-compras/src/main/java/com/ticketya/compras/security/OpenApiSecurityConfig.java](../../msvc-compras/src/main/java/com/ticketya/compras/security/OpenApiSecurityConfig.java)
- [msvc-compras/src/main/java/com/ticketya/compras/security/PurchaseAccess.java](../../msvc-compras/src/main/java/com/ticketya/compras/security/PurchaseAccess.java)
- [msvc-compras/src/main/java/com/ticketya/compras/security/SecurityConfig.java](../../msvc-compras/src/main/java/com/ticketya/compras/security/SecurityConfig.java)
- [msvc-compras/src/test/java/com/ticketya/compras/security/InternalCredentialsTest.java](../../msvc-compras/src/test/java/com/ticketya/compras/security/InternalCredentialsTest.java)
- [msvc-compras/src/test/java/com/ticketya/compras/security/ServiceSecurityIntegrationTest.java](../../msvc-compras/src/test/java/com/ticketya/compras/security/ServiceSecurityIntegrationTest.java)
- [msvc-compras/src/test/java/com/ticketya/compras/security/TestTokens.java](../../msvc-compras/src/test/java/com/ticketya/compras/security/TestTokens.java)
- [msvc-entradas/.dockerignore](../../msvc-entradas/.dockerignore)
- [msvc-entradas/Dockerfile](../../msvc-entradas/Dockerfile)
- [msvc-entradas/src/main/java/com/ticketya/entradas/security/CurrentIdentity.java](../../msvc-entradas/src/main/java/com/ticketya/entradas/security/CurrentIdentity.java)
- [msvc-entradas/src/main/java/com/ticketya/entradas/security/EntryAccess.java](../../msvc-entradas/src/main/java/com/ticketya/entradas/security/EntryAccess.java)
- [msvc-entradas/src/main/java/com/ticketya/entradas/security/FeignSecurityConfig.java](../../msvc-entradas/src/main/java/com/ticketya/entradas/security/FeignSecurityConfig.java)
- [msvc-entradas/src/main/java/com/ticketya/entradas/security/InternalCredentials.java](../../msvc-entradas/src/main/java/com/ticketya/entradas/security/InternalCredentials.java)
- [msvc-entradas/src/main/java/com/ticketya/entradas/security/InternalRestSecurity.java](../../msvc-entradas/src/main/java/com/ticketya/entradas/security/InternalRestSecurity.java)
- [msvc-entradas/src/main/java/com/ticketya/entradas/security/JwtSupport.java](../../msvc-entradas/src/main/java/com/ticketya/entradas/security/JwtSupport.java)
- [msvc-entradas/src/main/java/com/ticketya/entradas/security/OpenApiSecurityConfig.java](../../msvc-entradas/src/main/java/com/ticketya/entradas/security/OpenApiSecurityConfig.java)
- [msvc-entradas/src/main/java/com/ticketya/entradas/security/SecurityConfig.java](../../msvc-entradas/src/main/java/com/ticketya/entradas/security/SecurityConfig.java)
- [msvc-entradas/src/test/java/com/ticketya/entradas/security/InternalCredentialsTest.java](../../msvc-entradas/src/test/java/com/ticketya/entradas/security/InternalCredentialsTest.java)
- [msvc-entradas/src/test/java/com/ticketya/entradas/security/ServiceSecurityIntegrationTest.java](../../msvc-entradas/src/test/java/com/ticketya/entradas/security/ServiceSecurityIntegrationTest.java)
- [msvc-entradas/src/test/java/com/ticketya/entradas/security/TestTokens.java](../../msvc-entradas/src/test/java/com/ticketya/entradas/security/TestTokens.java)
- [msvc-eventosylugares/.dockerignore](../../msvc-eventosylugares/.dockerignore)
- [msvc-eventosylugares/Dockerfile](../../msvc-eventosylugares/Dockerfile)
- [msvc-eventosylugares/src/main/java/ar/edu/unicen/ticketya/eventos/dto/UsuarioOrganizadorRequestDTO.java](../../msvc-eventosylugares/src/main/java/ar/edu/unicen/ticketya/eventos/dto/UsuarioOrganizadorRequestDTO.java)
- [msvc-eventosylugares/src/main/java/ar/edu/unicen/ticketya/eventos/security/CurrentIdentity.java](../../msvc-eventosylugares/src/main/java/ar/edu/unicen/ticketya/eventos/security/CurrentIdentity.java)
- [msvc-eventosylugares/src/main/java/ar/edu/unicen/ticketya/eventos/security/EventAccess.java](../../msvc-eventosylugares/src/main/java/ar/edu/unicen/ticketya/eventos/security/EventAccess.java)
- [msvc-eventosylugares/src/main/java/ar/edu/unicen/ticketya/eventos/security/FeignSecurityConfig.java](../../msvc-eventosylugares/src/main/java/ar/edu/unicen/ticketya/eventos/security/FeignSecurityConfig.java)
- [msvc-eventosylugares/src/main/java/ar/edu/unicen/ticketya/eventos/security/InternalCredentials.java](../../msvc-eventosylugares/src/main/java/ar/edu/unicen/ticketya/eventos/security/InternalCredentials.java)
- [msvc-eventosylugares/src/main/java/ar/edu/unicen/ticketya/eventos/security/JwtSupport.java](../../msvc-eventosylugares/src/main/java/ar/edu/unicen/ticketya/eventos/security/JwtSupport.java)
- [msvc-eventosylugares/src/main/java/ar/edu/unicen/ticketya/eventos/security/OpenApiSecurityConfig.java](../../msvc-eventosylugares/src/main/java/ar/edu/unicen/ticketya/eventos/security/OpenApiSecurityConfig.java)
- [msvc-eventosylugares/src/main/java/ar/edu/unicen/ticketya/eventos/security/SecurityConfig.java](../../msvc-eventosylugares/src/main/java/ar/edu/unicen/ticketya/eventos/security/SecurityConfig.java)
- [msvc-eventosylugares/src/test/java/ar/edu/unicen/ticketya/eventos/security/InternalCredentialsTest.java](../../msvc-eventosylugares/src/test/java/ar/edu/unicen/ticketya/eventos/security/InternalCredentialsTest.java)
- [msvc-eventosylugares/src/test/java/ar/edu/unicen/ticketya/eventos/security/ServiceSecurityIntegrationTest.java](../../msvc-eventosylugares/src/test/java/ar/edu/unicen/ticketya/eventos/security/ServiceSecurityIntegrationTest.java)
- [msvc-eventosylugares/src/test/java/ar/edu/unicen/ticketya/eventos/security/TestTokens.java](../../msvc-eventosylugares/src/test/java/ar/edu/unicen/ticketya/eventos/security/TestTokens.java)
- [msvc-gateway/.dockerignore](../../msvc-gateway/.dockerignore)
- [msvc-gateway/Dockerfile](../../msvc-gateway/Dockerfile)
- [msvc-gateway/src/main/java/com/ticketya/gateway/security/JwtSupport.java](../../msvc-gateway/src/main/java/com/ticketya/gateway/security/JwtSupport.java)
- [msvc-gateway/src/main/java/com/ticketya/gateway/security/SecurityConfig.java](../../msvc-gateway/src/main/java/com/ticketya/gateway/security/SecurityConfig.java)
- [msvc-gateway/src/test/java/com/ticketya/gateway/security/TestTokens.java](../../msvc-gateway/src/test/java/com/ticketya/gateway/security/TestTokens.java)
- [msvc-reservas/.dockerignore](../../msvc-reservas/.dockerignore)
- [msvc-reservas/Dockerfile](../../msvc-reservas/Dockerfile)
- [msvc-reservas/src/main/java/com/ticketya/reservas/security/CurrentIdentity.java](../../msvc-reservas/src/main/java/com/ticketya/reservas/security/CurrentIdentity.java)
- [msvc-reservas/src/main/java/com/ticketya/reservas/security/FeignSecurityConfig.java](../../msvc-reservas/src/main/java/com/ticketya/reservas/security/FeignSecurityConfig.java)
- [msvc-reservas/src/main/java/com/ticketya/reservas/security/InternalCredentials.java](../../msvc-reservas/src/main/java/com/ticketya/reservas/security/InternalCredentials.java)
- [msvc-reservas/src/main/java/com/ticketya/reservas/security/JwtSupport.java](../../msvc-reservas/src/main/java/com/ticketya/reservas/security/JwtSupport.java)
- [msvc-reservas/src/main/java/com/ticketya/reservas/security/OpenApiSecurityConfig.java](../../msvc-reservas/src/main/java/com/ticketya/reservas/security/OpenApiSecurityConfig.java)
- [msvc-reservas/src/main/java/com/ticketya/reservas/security/ReservationAccess.java](../../msvc-reservas/src/main/java/com/ticketya/reservas/security/ReservationAccess.java)
- [msvc-reservas/src/main/java/com/ticketya/reservas/security/SecurityConfig.java](../../msvc-reservas/src/main/java/com/ticketya/reservas/security/SecurityConfig.java)
- [msvc-reservas/src/test/java/com/ticketya/reservas/security/InternalCredentialsTest.java](../../msvc-reservas/src/test/java/com/ticketya/reservas/security/InternalCredentialsTest.java)
- [msvc-reservas/src/test/java/com/ticketya/reservas/security/ServiceSecurityIntegrationTest.java](../../msvc-reservas/src/test/java/com/ticketya/reservas/security/ServiceSecurityIntegrationTest.java)
- [msvc-reservas/src/test/java/com/ticketya/reservas/security/TestTokens.java](../../msvc-reservas/src/test/java/com/ticketya/reservas/security/TestTokens.java)
- [msvc-usuarios/.dockerignore](../../msvc-usuarios/.dockerignore)
- [msvc-usuarios/.gitignore](../../msvc-usuarios/.gitignore)
- [msvc-usuarios/Dockerfile](../../msvc-usuarios/Dockerfile)
- [msvc-usuarios/docs/01-ESPECIFICACION.md](../../msvc-usuarios/docs/01-ESPECIFICACION.md)
- [msvc-usuarios/docs/02-ARQUITECTURA.md](../../msvc-usuarios/docs/02-ARQUITECTURA.md)
- [msvc-usuarios/docs/03-PERMISOS.md](../../msvc-usuarios/docs/03-PERMISOS.md)
- [msvc-usuarios/docs/04-API.md](../../msvc-usuarios/docs/04-API.md)
- [msvc-usuarios/docs/05-EJECUCION.md](../../msvc-usuarios/docs/05-EJECUCION.md)
- [msvc-usuarios/docs/06-PRUEBAS.md](../../msvc-usuarios/docs/06-PRUEBAS.md)
- [msvc-usuarios/docs/07-ENTREGA.md](../../msvc-usuarios/docs/07-ENTREGA.md)
- [msvc-usuarios/docs/08-COORDINACION.md](../../msvc-usuarios/docs/08-COORDINACION.md)
- [msvc-usuarios/docs/organizador-usuario.sql](../../msvc-usuarios/docs/organizador-usuario.sql)
- [msvc-usuarios/pom.xml](../../msvc-usuarios/pom.xml)
- [msvc-usuarios/README.md](../../msvc-usuarios/README.md)
- [msvc-usuarios/src/main/java/com/ticketya/usuarios/config/BootstrapAdmin.java](../../msvc-usuarios/src/main/java/com/ticketya/usuarios/config/BootstrapAdmin.java)
- [msvc-usuarios/src/main/java/com/ticketya/usuarios/config/JwtConfig.java](../../msvc-usuarios/src/main/java/com/ticketya/usuarios/config/JwtConfig.java)
- [msvc-usuarios/src/main/java/com/ticketya/usuarios/config/OpenApiConfig.java](../../msvc-usuarios/src/main/java/com/ticketya/usuarios/config/OpenApiConfig.java)
- [msvc-usuarios/src/main/java/com/ticketya/usuarios/controllers/AuthController.java](../../msvc-usuarios/src/main/java/com/ticketya/usuarios/controllers/AuthController.java)
- [msvc-usuarios/src/main/java/com/ticketya/usuarios/controllers/UsuarioController.java](../../msvc-usuarios/src/main/java/com/ticketya/usuarios/controllers/UsuarioController.java)
- [msvc-usuarios/src/main/java/com/ticketya/usuarios/dto/ErrorResponse.java](../../msvc-usuarios/src/main/java/com/ticketya/usuarios/dto/ErrorResponse.java)
- [msvc-usuarios/src/main/java/com/ticketya/usuarios/dto/EstadoRequest.java](../../msvc-usuarios/src/main/java/com/ticketya/usuarios/dto/EstadoRequest.java)
- [msvc-usuarios/src/main/java/com/ticketya/usuarios/dto/LoginRequest.java](../../msvc-usuarios/src/main/java/com/ticketya/usuarios/dto/LoginRequest.java)
- [msvc-usuarios/src/main/java/com/ticketya/usuarios/dto/RegistroRequest.java](../../msvc-usuarios/src/main/java/com/ticketya/usuarios/dto/RegistroRequest.java)
- [msvc-usuarios/src/main/java/com/ticketya/usuarios/dto/RolesRequest.java](../../msvc-usuarios/src/main/java/com/ticketya/usuarios/dto/RolesRequest.java)
- [msvc-usuarios/src/main/java/com/ticketya/usuarios/dto/TokenResponse.java](../../msvc-usuarios/src/main/java/com/ticketya/usuarios/dto/TokenResponse.java)
- [msvc-usuarios/src/main/java/com/ticketya/usuarios/dto/UsuarioResponse.java](../../msvc-usuarios/src/main/java/com/ticketya/usuarios/dto/UsuarioResponse.java)
- [msvc-usuarios/src/main/java/com/ticketya/usuarios/entities/Rol.java](../../msvc-usuarios/src/main/java/com/ticketya/usuarios/entities/Rol.java)
- [msvc-usuarios/src/main/java/com/ticketya/usuarios/entities/Usuario.java](../../msvc-usuarios/src/main/java/com/ticketya/usuarios/entities/Usuario.java)
- [msvc-usuarios/src/main/java/com/ticketya/usuarios/exceptions/ApiException.java](../../msvc-usuarios/src/main/java/com/ticketya/usuarios/exceptions/ApiException.java)
- [msvc-usuarios/src/main/java/com/ticketya/usuarios/exceptions/GlobalExceptionHandler.java](../../msvc-usuarios/src/main/java/com/ticketya/usuarios/exceptions/GlobalExceptionHandler.java)
- [msvc-usuarios/src/main/java/com/ticketya/usuarios/repositories/UsuarioRepository.java](../../msvc-usuarios/src/main/java/com/ticketya/usuarios/repositories/UsuarioRepository.java)
- [msvc-usuarios/src/main/java/com/ticketya/usuarios/security/ApiSecurityErrors.java](../../msvc-usuarios/src/main/java/com/ticketya/usuarios/security/ApiSecurityErrors.java)
- [msvc-usuarios/src/main/java/com/ticketya/usuarios/security/JwtSupport.java](../../msvc-usuarios/src/main/java/com/ticketya/usuarios/security/JwtSupport.java)
- [msvc-usuarios/src/main/java/com/ticketya/usuarios/security/LoginAttemptLimiter.java](../../msvc-usuarios/src/main/java/com/ticketya/usuarios/security/LoginAttemptLimiter.java)
- [msvc-usuarios/src/main/java/com/ticketya/usuarios/security/SecurityConfig.java](../../msvc-usuarios/src/main/java/com/ticketya/usuarios/security/SecurityConfig.java)
- [msvc-usuarios/src/main/java/com/ticketya/usuarios/services/impl/ServiceAuthenticationServiceImpl.java](../../msvc-usuarios/src/main/java/com/ticketya/usuarios/services/impl/ServiceAuthenticationServiceImpl.java)
- [msvc-usuarios/src/main/java/com/ticketya/usuarios/services/impl/TokenServiceImpl.java](../../msvc-usuarios/src/main/java/com/ticketya/usuarios/services/impl/TokenServiceImpl.java)
- [msvc-usuarios/src/main/java/com/ticketya/usuarios/services/impl/UsuarioServiceImpl.java](../../msvc-usuarios/src/main/java/com/ticketya/usuarios/services/impl/UsuarioServiceImpl.java)
- [msvc-usuarios/src/main/java/com/ticketya/usuarios/services/ServiceAuthenticationService.java](../../msvc-usuarios/src/main/java/com/ticketya/usuarios/services/ServiceAuthenticationService.java)
- [msvc-usuarios/src/main/java/com/ticketya/usuarios/services/TokenService.java](../../msvc-usuarios/src/main/java/com/ticketya/usuarios/services/TokenService.java)
- [msvc-usuarios/src/main/java/com/ticketya/usuarios/services/UsuarioService.java](../../msvc-usuarios/src/main/java/com/ticketya/usuarios/services/UsuarioService.java)
- [msvc-usuarios/src/main/java/com/ticketya/usuarios/UsuariosApplication.java](../../msvc-usuarios/src/main/java/com/ticketya/usuarios/UsuariosApplication.java)
- [msvc-usuarios/src/main/resources/application-dev.yml](../../msvc-usuarios/src/main/resources/application-dev.yml)
- [msvc-usuarios/src/main/resources/application.yml](../../msvc-usuarios/src/main/resources/application.yml)
- [msvc-usuarios/src/main/resources/db/schema-mysql.sql](../../msvc-usuarios/src/main/resources/db/schema-mysql.sql)
- [msvc-usuarios/src/test/java/com/ticketya/usuarios/AuthSecurityIntegrationTest.java](../../msvc-usuarios/src/test/java/com/ticketya/usuarios/AuthSecurityIntegrationTest.java)
- [msvc-usuarios/src/test/java/com/ticketya/usuarios/security/JwtValidationTest.java](../../msvc-usuarios/src/test/java/com/ticketya/usuarios/security/JwtValidationTest.java)
- [msvc-usuarios/src/test/java/com/ticketya/usuarios/security/LoginAttemptLimiterTest.java](../../msvc-usuarios/src/test/java/com/ticketya/usuarios/security/LoginAttemptLimiterTest.java)
- [msvc-usuarios/src/test/java/com/ticketya/usuarios/security/TestTokens.java](../../msvc-usuarios/src/test/java/com/ticketya/usuarios/security/TestTokens.java)
- [msvc-usuarios/src/test/resources/application-test.yml](../../msvc-usuarios/src/test/resources/application-test.yml)
- [postman/TicketYa-Usuarios-JWT.postman_collection.json](../../postman/TicketYa-Usuarios-JWT.postman_collection.json)
- [scripts/GenerateDevSecrets.java](../../scripts/GenerateDevSecrets.java)
- [scripts/Smoke-Security.ps1](../../scripts/Smoke-Security.ps1)
- [scripts/Start-TicketYaService.ps1](../../scripts/Start-TicketYaService.ps1)
- [scripts/Verify-Security.ps1](../../scripts/Verify-Security.ps1)

### Archivos modificados

- [docs/02-ARQUITECTURA-GENERAL.md](../../docs/02-ARQUITECTURA-GENERAL.md)
- [docs/05-CONTRATOS-DE-INTEGRACION.md](../../docs/05-CONTRATOS-DE-INTEGRACION.md)
- [docs/06-PLAN-DE-TESTING.md](../../docs/06-PLAN-DE-TESTING.md)
- [msvc-compras/pom.xml](../../msvc-compras/pom.xml)
- [msvc-compras/src/main/java/com/ticketya/compras/controllers/CompraController.java](../../msvc-compras/src/main/java/com/ticketya/compras/controllers/CompraController.java)
- [msvc-compras/src/main/java/com/ticketya/compras/controllers/PagoController.java](../../msvc-compras/src/main/java/com/ticketya/compras/controllers/PagoController.java)
- [msvc-compras/src/main/java/com/ticketya/compras/controllers/ReintegroController.java](../../msvc-compras/src/main/java/com/ticketya/compras/controllers/ReintegroController.java)
- [msvc-compras/src/main/java/com/ticketya/compras/exceptions/CompraExceptionHandler.java](../../msvc-compras/src/main/java/com/ticketya/compras/exceptions/CompraExceptionHandler.java)
- [msvc-compras/src/main/resources/application.properties](../../msvc-compras/src/main/resources/application.properties)
- [msvc-entradas/pom.xml](../../msvc-entradas/pom.xml)
- [msvc-entradas/src/main/java/com/ticketya/entradas/clients/CompraClient.java](../../msvc-entradas/src/main/java/com/ticketya/entradas/clients/CompraClient.java)
- [msvc-entradas/src/main/java/com/ticketya/entradas/clients/ReservaClient.java](../../msvc-entradas/src/main/java/com/ticketya/entradas/clients/ReservaClient.java)
- [msvc-entradas/src/main/java/com/ticketya/entradas/controllers/DevolucionController.java](../../msvc-entradas/src/main/java/com/ticketya/entradas/controllers/DevolucionController.java)
- [msvc-entradas/src/main/java/com/ticketya/entradas/controllers/EntradaController.java](../../msvc-entradas/src/main/java/com/ticketya/entradas/controllers/EntradaController.java)
- [msvc-entradas/src/main/resources/application.properties](../../msvc-entradas/src/main/resources/application.properties)
- [msvc-entradas/src/test/java/com/ticketya/entradas/MsvcEntradasApplicationTests.java](../../msvc-entradas/src/test/java/com/ticketya/entradas/MsvcEntradasApplicationTests.java)
- [msvc-eventosylugares/pom.xml](../../msvc-eventosylugares/pom.xml)
- [msvc-eventosylugares/src/main/java/ar/edu/unicen/ticketya/eventos/controller/EventoController.java](../../msvc-eventosylugares/src/main/java/ar/edu/unicen/ticketya/eventos/controller/EventoController.java)
- [msvc-eventosylugares/src/main/java/ar/edu/unicen/ticketya/eventos/controller/OrganizadorController.java](../../msvc-eventosylugares/src/main/java/ar/edu/unicen/ticketya/eventos/controller/OrganizadorController.java)
- [msvc-eventosylugares/src/main/java/ar/edu/unicen/ticketya/eventos/model/Organizador.java](../../msvc-eventosylugares/src/main/java/ar/edu/unicen/ticketya/eventos/model/Organizador.java)
- [msvc-eventosylugares/src/main/java/ar/edu/unicen/ticketya/eventos/service/impl/OrganizadorServiceImpl.java](../../msvc-eventosylugares/src/main/java/ar/edu/unicen/ticketya/eventos/service/impl/OrganizadorServiceImpl.java)
- [msvc-eventosylugares/src/main/java/ar/edu/unicen/ticketya/eventos/service/OrganizadorService.java](../../msvc-eventosylugares/src/main/java/ar/edu/unicen/ticketya/eventos/service/OrganizadorService.java)
- [msvc-eventosylugares/src/main/resources/application.yml](../../msvc-eventosylugares/src/main/resources/application.yml)
- [msvc-eventosylugares/src/test/java/ar/edu/unicen/ticketya/eventos/integration/EventoApiIntegrationTest.java](../../msvc-eventosylugares/src/test/java/ar/edu/unicen/ticketya/eventos/integration/EventoApiIntegrationTest.java)
- [msvc-gateway/pom.xml](../../msvc-gateway/pom.xml)
- [msvc-gateway/README.md](../../msvc-gateway/README.md)
- [msvc-gateway/src/main/resources/application.yml](../../msvc-gateway/src/main/resources/application.yml)
- [msvc-gateway/src/test/java/com/ticketya/gateway/MsvcGatewayApplicationTests.java](../../msvc-gateway/src/test/java/com/ticketya/gateway/MsvcGatewayApplicationTests.java)
- [msvc-reservas/pom.xml](../../msvc-reservas/pom.xml)
- [msvc-reservas/src/main/java/com/ticketya/reservas/controllers/ReservaController.java](../../msvc-reservas/src/main/java/com/ticketya/reservas/controllers/ReservaController.java)
- [msvc-reservas/src/main/java/com/ticketya/reservas/exceptions/ReservaExceptionHandler.java](../../msvc-reservas/src/main/java/com/ticketya/reservas/exceptions/ReservaExceptionHandler.java)
- [msvc-reservas/src/main/resources/application.properties](../../msvc-reservas/src/main/resources/application.properties)
- [README.md](../../README.md)
