# Ejecución, claves y configuración

## Requisitos

Java 21 y Maven 3.9. MySQL 8 para persistencia normal. Para Compose, Docker Desktop
con motor Linux iniciado. Los tests Maven no requieren MySQL, Docker ni claves
manuales; usan H2 y RSA generado en runtime.

Puertos: Reservas 8080, Eventos 8081, Compras 8082, Entradas 8083,
Gateway 8084 y Usuarios 8085. PORT configura cada proceso por separado.

## Preparación en Windows PowerShell

Desde la raíz TPE-micro1:

~~~powershell
java .\scripts\GenerateDevSecrets.java
docker compose up -d db-usuarios
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\Start-TicketYaService.ps1 -Service usuarios
~~~

Omitir el primer comando si ya existen .env y .secrets. El generador se niega a
sobrescribirlos. El launcher sólo inyecta a cada proceso sus variables necesarias
y restaura el entorno de PowerShell al salir. Bypass se limita a ese proceso
PowerShell; no modifica la política de ejecución global.

El generador deja el administrador inicial habilitado únicamente para la primera
base vacía. Su email y password se consultan localmente en .env; no hay una
contraseña universal en este documento. Cambiar BOOTSTRAP_ADMIN_ENABLED=false
después del primer arranque y retirar BOOTSTRAP_ADMIN_PASSWORD del entorno.

Para levantar los otros servicios desde Maven, abrir una terminal por proceso:

~~~powershell
docker compose up -d db-eventos db-reservas db-compras db-entradas
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\Start-TicketYaService.ps1 -Service eventos
# Otras terminales: -Service reservas, compras, entradas y gateway
~~~

Los MySQL de Compose se publican sólo en loopback: usuarios=3307, eventos=3308,
reservas=3309, compras=3310, entradas=3311. El launcher usa esos puertos.
La alternativa completa en contenedores es:

~~~powershell
docker compose up --build -d
docker compose ps
docker compose logs usuarios gateway
~~~

Se crea una instancia/base y volumen separados por servicio, con usuarios SQL
de aplicación y claves independientes. Sólo Usuarios monta la clave privada.
Los otros contenedores montan únicamente el archivo público, readonly.
Los Dockerfile usan builds Maven separados y runtime Java 21 sin usuario root.
El build de imagen omite ejecutar tests; ejecutar las suites antes de entregar.
No borrar los volúmenes si se desea conservar los usuarios existentes.

## MySQL propio y ejecución directa de Usuarios

Crear ticketya_usuarios y un usuario SQL con permisos sobre esa base. Ejemplo
de configuración (reemplazar las variables ficticias por los valores locales):

~~~powershell
cd .\msvc-usuarios
$env:DB_URL = 'jdbc:mysql://localhost:3306/ticketya_usuarios'
$env:DB_USER = 'ticketya'
$env:DB_PASSWORD = '<password-local-de-MySQL>'
$env:SPRING_PROFILES_ACTIVE = 'dev'
$env:JWT_PUBLIC_KEY = 'file:../.secrets/jwt-public.pem'
$env:JWT_PRIVATE_KEY = 'file:../.secrets/jwt-private.pem'
$env:BOOTSTRAP_ADMIN_ENABLED = 'false'
mvn spring-boot:run
~~~

Dev usa ddl-auto=update. Para un despliegue con perfiles diferentes, la configuración
base usa validate: inicializar el esquema con src/main/resources/db/schema-mysql.sql
y verificarlo sobre MySQL. No activar dev automáticamente en producción.
Eventos ya usaba update en su perfil mysql; para bases con validate, aplicar una
sola vez docs/sql/02-organizadores-usuario.sql antes de actualizar ese servicio.

## Generación manual de claves

Si se usa OpenSSL, desde la raíz:

~~~powershell
New-Item -ItemType Directory -Path .secrets -Force
openssl genpkey -algorithm RSA -pkeyopt rsa_keygen_bits:3072 -out .secrets/jwt-private.pem
openssl pkey -in .secrets/jwt-private.pem -pubout -out .secrets/jwt-public.pem
~~~

La privada debe ser PKCS#8 (BEGIN PRIVATE KEY) y la pública X.509
(BEGIN PUBLIC KEY). El generador Java es la alternativa sin OpenSSL.
Permitir acceso al material privado sólo al usuario del sistema operativo que
ejecuta Usuarios; usar archivos montados o un gestor de secretos en despliegue.
Nunca copiar la privada a Gateway ni a servicios de negocio. La rotación actual
requiere reiniciarlos y renovar logins; no hay JWKS ni superposición de claves.

## Variables

| Variable | Uso / valor inicial |
|---|---|
| PORT | Puerto del proceso; Usuarios 8085, Gateway 8084 |
| DB_URL, DB_USER, DB_PASSWORD | SQL propio por servicio; Usuarios no tiene password predeterminada |
| SPRING_PROFILES_ACTIVE | dev para Usuarios local, test en pruebas, mysql para Eventos |
| JWT_PUBLIC_KEY | Recurso Spring file:/ruta/absoluta/jwt-public.pem; default relativo ../.secrets/jwt-public.pem |
| JWT_PRIVATE_KEY | Sólo Usuarios, recurso file: con clave PKCS#8 |
| JWT_ISSUER | ticketya-usuarios (debe coincidir en todos) |
| JWT_AUDIENCE | ticketya-api (debe coincidir en todos) |
| JWT_TTL | PT60M; sólo emisión de usuarios |
| LOGIN_MAX_ATTEMPTS | 5; sólo Usuarios |
| LOGIN_WINDOW | PT15M; sólo Usuarios |
| BOOTSTRAP_ADMIN_ENABLED | false por defecto; activación explícita inicial |
| BOOTSTRAP_ADMIN_EMAIL, BOOTSTRAP_ADMIN_PASSWORD | Sólo primer administrador, base vacía |
| SERVICE_EVENTOS_SECRET | Emisor Usuarios; mínimo 32 caracteres |
| SERVICE_RESERVAS_SECRET | Emisor Usuarios; mínimo 32 caracteres |
| SERVICE_COMPRAS_SECRET | Emisor Usuarios; mínimo 32 caracteres |
| SERVICE_ENTRADAS_SECRET | Emisor Usuarios; mínimo 32 caracteres |
| SERVICE_CLIENT_SECRET | Sólo el secreto de ese servicio emisor de llamadas HTTP |
| SERVICE_TOKEN_URL | http://localhost:8085/auth/service-token |
| USUARIOS_URL | Destino de Gateway y del cliente Feign de Eventos hacia Usuarios; default http://localhost:8085 |
| EVENTOS_URL, RESERVAS_URL, COMPRAS_URL, ENTRADAS_URL | Destinos de Gateway/Feign/RestClient según el proceso |
| INTEGRACIONES_HABILITADAS | Eventos: mantiene los avisos y habilitación de reservas existentes |

.env.example es sólo una plantilla. GenerateDevSecrets genera secretos diferentes.
No cargar todos los secretos en todos los microservicios; el launcher/Compose
distribuyen a los servicios sólo su credencial. En producción distribuir únicamente
variables requeridas para ese contenedor y usar TLS para credenciales y Bearer.

## Verificación manual a través de Gateway

1. Iniciar Usuarios y Gateway con la misma clave pública, issuer y audience.
2. Registrar e iniciar sesión con baseUrl=http://localhost:8084 en Postman.
3. Copiar el Bearer a /usuarios/me y verificar 200; quitarlo y verificar 401.
4. Como CLIENTE, PATCH /usuarios/{id}/roles y PATCH /devoluciones/{id}/aprobar deben dar 403.
5. Iniciar los otros cuatro servicios, con secretos de servicio y URLs correctos.
6. Como administrador, asignar ORGANIZADOR y vincular el usuario mediante
   PATCH /organizadores/{id}/usuario. Volver a iniciar sesión tras cambiar roles.
7. Publicar y comprar con el cliente; clienteId y X-Cliente-Id deben ser su ID.
8. Intentar consultar tickets ajenos, alterar clienteId o reutilizar el ID de
   otra persona: 403. Repetir contra puertos directos para comprobar los filtros.
9. /swagger-ui.html del Gateway permite elegir cualquiera de las cinco APIs.

La prueba automatizada con seis JVM reales y H2 es reproducible:

~~~powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\Smoke-Security.ps1
~~~

Usa 18080–18085, claves y bases efímeras independientes, crea sus fixtures,
comprueba HTTP y detiene sólo los procesos que inició. Deja logs y result.json
en .local-security-smoke/ (ignorado por Git). Puede pasarse -MavenRepository
con la ruta del caché local. No demuestra concurrencia específica de MySQL.
