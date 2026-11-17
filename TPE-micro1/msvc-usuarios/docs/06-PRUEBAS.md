# Pruebas automatizadas y límites de verificación

## Cómo repetirlas

En cada Maven independiente:

~~~powershell
mvn clean compile
mvn clean test
mvn clean package
~~~

Desde la raíz también puede ejecutarse el mismo orden para los seis servicios:

~~~powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\Verify-Security.ps1
~~~

El script verifica primero Usuarios y después los componentes modificados. Conserva
build-compile.log, build-test.log y build-package.log por servicio (ignorados por
Git). Si un comando falla, detiene la verificación con código de salida no exitoso.
Puede configurarse -MavenRepository con una ruta de caché.

## Cobertura

| Suite | Evidencia |
|---|---|
| AuthSecurityIntegrationTest | H2 real, BCrypt verificable, email canónico y UNIQUE, duplicado, datos inválidos, intento de ADMINISTRADOR ignorado, DTO sin hash, login correcto/incorrecto/inexistente/inactivo, JWT emitido, /me, roles, estado, ownership, OpenAPI y Swagger |
| JwtValidationTest | Firma RSA real; exp, issuer, audience, iat, jti, subject, token_use y roles; clave ajena, firma alterada, none y HS256; authorities humanas y de servicio separadas |
| LoginAttemptLimiterTest | Límite, reinicio por éxito, independencia entre cuentas y vencimiento con reloj controlado |
| ServiceSecurityIntegrationTest en Eventos | Matriz de cada operación administrativa frente a cuatro roles y cuatro identidades internas, evento ajeno, transferencia de organizador, borradores, búsqueda pública y JWT inválido |
| ServiceSecurityIntegrationTest en Reservas | Matriz de todos sus endpoints privados, clienteId falso, reserva ajena y confirmación sólo por Compras |
| ServiceSecurityIntegrationTest en Compras | Matriz de todos sus endpoints, compra/pago/reintegros ajenos y lectura interna limitada para Entradas |
| ServiceSecurityIntegrationTest en Entradas | Matriz de todos sus endpoints, header de identidad falso, cliente ajeno, devolución ajena y administradorId derivado del JWT |
| InternalCredentialsTest (cuatro servicios) | Obtención HTTP Basic de token propio, caché, propagación humana en GET, escrituras internas, llamadas sin contexto y cambio a identidad propia en llamadas anidadas |
| MsvcEntradasApplicationTests | Entrada persistida en H2, lectura por su titular con JWT firmado y rechazo del atacante que falsifica X-Cliente-Id |
| MsvcGatewayApplicationTests | Destinos HTTP reales controlados, rutas y body, errores de destino, tiempos acotados, rutas Usuarios/OpenAPI, Bearer preservado, JWT inválido/vencido, autorización y Swagger agregado |

Las suites previas de negocio se mantienen. EventoApiIntegrationTest ahora
autentica sus fixtures administrativos; los tests nuevos verifican separadamente
los rechazos. No se desactivó la seguridad por perfil ni se sustituyeron los
decodificadores por tokens sin firma en las pruebas HTTP.

## Prueba con seis servicios reales

Smoke-Security.ps1 usa seis JVM separadas, HTTP real, un H2 por microservicio
persistente y claves generadas para esa ejecución. No sustituye Feign, RestClient,
controllers ni servicios por mocks. Comprueba registro/login, perfil, acceso
directo, roles, vinculación del organizador, publicación (habilitación interna),
reserva, pago, confirmación, emisión, lectura propia/ajena, devolución aprobada
con reintegro y cancelación notificada por scheduler con reintegro restante.
Detiene los procesos creados incluso ante fallos.

Se completó una ejecución final de **42 verificaciones HTTP**, registrada en el informe
de entrega. El motor Docker no estaba iniciado; esta ejecución usa H2 de test.

## Infraestructura pendiente

docker compose --env-file .env.example config --quiet fue validado.
No se ejecutaron imágenes, contenedores ni validación de persistencia MySQL: Docker
respondió que no existe la tubería dockerDesktopLinuxEngine. No había servidor
MySQL local disponible. Los tests *MySQLIT existentes no forman parte de Surefire
por su nombre y requieren esa infraestructura; no se los contabiliza como ejecutados.

Para cerrar esa verificación con el equipo: iniciar Docker Desktop, generar los
secretos locales, ejecutar docker compose up --build y repetir la colección/flujo
contra 8084. Verificar las pruebas de concurrencia MySQL de Reservas/Compras según
sus documentos, sin sustituirlas por el resultado secuencial de H2.

Los comandos finales y totales por servicio se consignan en 07-ENTREGA.md.
