# TicketYa — Usuarios y seguridad (Entrega 2)

Java 21, Spring Boot 3.5.6, Maven, MySQL propio. Puerto 8085; Gateway 8084.
Registro de clientes, BCrypt (costo 12), login con mitigación de intentos,
JWT RS256, cuatro roles y administración protegida de usuarios.

Desde la raíz `TPE-micro1`, con Java/Maven y Docker Desktop iniciado:

~~~powershell
# Sólo la primera vez: no sobrescribe configuración existente.
java .\scripts\GenerateDevSecrets.java
docker compose up -d db-usuarios
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\Start-TicketYaService.ps1 -Service usuarios
~~~

El generador crea `.env` y `.secrets/` ignorados por Git. El launcher configura
el MySQL de Compose en localhost:3307 y ejecuta `mvn -B spring-boot:run`.
Para usar otro MySQL y para configurar las claves manualmente, ver
[ejecución y variables](docs/05-EJECUCION.md).
El bootstrap crea el primer administrador sólo en una base vacía, con email y
contraseña locales de `.env`. Desactivar `BOOTSTRAP_ADMIN_ENABLED` tras crearlo.

Swagger: http://localhost:8085/swagger-ui/index.html
OpenAPI: http://localhost:8085/v3/api-docs
Swagger agregado: http://localhost:8084/swagger-ui.html

| Método | Ruta | Acceso |
|---|---|---|
| POST | /auth/register | Público; sólo crea CLIENTE |
| POST | /auth/login | Público, credenciales de usuario |
| POST | /auth/service-token | HTTP Basic de servicio configurado |
| GET | /usuarios/me | Usuario autenticado |
| GET | /usuarios/{id} | Titular o ADMINISTRADOR |
| PATCH | /usuarios/{id}/roles | ADMINISTRADOR |
| PATCH | /usuarios/{id}/estado | ADMINISTRADOR |

La seguridad también se aplica en Gateway, Eventos, Reservas, Compras y Entradas.
`clienteId` y `X-Cliente-Id` deben coincidir con el JWT y con el recurso.
`Organizador.usuarioId` se vincula explícitamente por administración.

Documentación:

- [Especificación previa y aceptación](docs/01-ESPECIFICACION.md).
- [Arquitectura, datos, JWT y decisiones](docs/02-ARQUITECTURA.md).
- [Matriz completa de permisos](docs/03-PERMISOS.md).
- [API, ejemplos y errores](docs/04-API.md).
- [Ejecución, Docker, claves y variables](docs/05-EJECUCION.md).
- [Pruebas y verificación](docs/06-PRUEBAS.md).
- [Entrega final e inventario completo de archivos](docs/07-ENTREGA.md).
- [Coordinación con el equipo y límites](docs/08-COORDINACION.md).
- [Colección Postman](../postman/TicketYa-Usuarios-JWT.postman_collection.json).

Pruebas independientes, sin MySQL ni claves guardadas:

~~~powershell
cd .\msvc-usuarios
mvn clean compile
mvn clean test
mvn clean package
~~~

Las claves de prueba se generan en tiempo de ejecución. H2 sólo tiene alcance
de test. No se incluyen claves privadas, tokens ni contraseñas reales en Git.
No se necesita instalar otros módulos Maven para compilar este servicio.
