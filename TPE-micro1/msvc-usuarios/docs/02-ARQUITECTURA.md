# Arquitectura y decisiones

## Funcionalidades e historias de usuario

- US-U1: como visitante, registrar mi identidad sin elegir privilegios.
- US-U2: como usuario activo, autenticarme y recibir una credencial temporal.
- US-U3: como usuario, consultar mi perfil sin acceder a datos de otras personas.
- US-U4: como administrador, asignar varios roles y deshabilitar cuentas.
- US-U5: como organizador, operar únicamente eventos del organizador vinculado.
- US-U6: como servicio, ejecutar operaciones internas con una identidad limitada.

Los criterios de aceptación verificables están en la especificación y en las
pruebas de JWT, API y matriz de permisos. No se implementaron preferencias ni
fila virtual: sus controllers no existen en el repositorio relevado.

## Capas

`controllers` recibe DTO con Jakarta Validation; `services` define interfaces y
`services.impl` implementa los casos de uso; `repositories` usa Spring Data JPA;
`entities` contiene Usuario y Rol; `security` configura filtros, principal,
validadores y mitigación de intentos; `config` configura RSA, bootstrap y OpenAPI;
`exceptions` produce errores uniformes. Inyección por constructor; DTO de clases.

El gateway usa exclusivamente `SecurityWebFilterChain` y
`NimbusReactiveJwtDecoder`. Los servicios MVC usan `SecurityFilterChain`,
`NimbusJwtDecoder` y métodos `@PreAuthorize`. Todos verifican firmas localmente,
incluso si se accede directamente por su puerto.

## Modelo de datos

~~~mermaid
erDiagram
    USUARIOS ||--|{ USUARIO_ROLES : tiene
    USUARIOS {
        bigint id PK
        varchar nombre
        varchar apellido
        varchar email UK
        varchar password_hash
        boolean activo
        datetime fecha_creacion
        bigint version
    }
    USUARIO_ROLES {
        bigint usuario_id FK
        enum rol
    }
~~~

`usuario_roles` es una colección JPA de enums almacenados como strings, con
unicidad (usuario_id, rol). Email canónico: trim + minúsculas con Locale.ROOT,
más restricción UNIQUE en SQL y comprobación previa. El control SQL cubre también
registros concurrentes. Fecha de creación UTC y versión optimista.

BCrypt usa costo 12. Contraseñas de registro: entre 12 y 72 caracteres y máximo
72 bytes UTF-8. No se recortan contraseñas; nombre/apellido sí se recortan.
`passwordHash` no pertenece a ningún DTO y está marcado JsonIgnore en la entidad.
Las contraseñas de requests son write-only; no se implementan toString con secretos.

Usuarios tiene su propia base y sus entidades no se comparten con otros servicios.
La relación lógica externa es `clienteId = Long.parseLong(jwt.sub)`. Las bases
históricas necesitan una migración acordada: no se reinterpretan sus IDs sin validar.
En Eventos, `organizadores.usuario_id` es nullable, único y distinto de
`organizadores.id`; no hay FK entre bases. Sin vínculo, sólo administración
puede operar esos eventos. La asignación del vínculo exige ADMINISTRADOR.

## JWT de usuario

NimbusJwtEncoder firma RS256 con una clave privada PKCS#8. Sólo Usuarios la monta.
Los otros procesos reciben únicamente la clave pública X.509 (mínimo RSA 2048).
El generador local usa RSA 3072. No se genera una clave efímera al iniciar producción:
una configuración faltante o un par que no coincide hace fallar el arranque.

Claims: sub numérico positivo dentro de Long, iss, aud (lista), iat, exp, jti,
roles (lista de valores conocidos, no vacía) y token_use=user.
Emisor y audiencia deben coincidir con la configuración. Expiración inicial 60 min,
configurable entre más de cero y 24 h. Se comprueba exp sin tolerancia, nbf si existe,
iat (máximo 30 s futuro), exp posterior a iat y presencia/formato de las claims.
Sólo se acepta RS256; se rechazan none, HMAC, otra clave, firmas alteradas y roles
desconocidos. Las authorities se construyen después de validar el token:
USER y ROLE_CLIENTE / ROLE_ORGANIZADOR / ROLE_ADMINISTRADOR / ROLE_PERSONAL_ACCESO.

El JWT no contiene email, contraseña ni hash. No hay refresh token ni endpoint
de revocación. Desactivar una cuenta impide nuevos logins; cambiar roles afecta
tokens nuevos. Los JWT emitidos conservan su vigencia hasta exp. Una clave pública
montada implica reiniciar los validadores al rotarla y volver a iniciar sesión.
No se expone JWKS ni la clave privada en ningún endpoint.

## Login y bootstrap

Usuario inexistente, password incorrecta y cuenta inactiva devuelven el mismo 401.
Se ejecuta PasswordEncoder.matches incluso para cuentas inexistentes, con un hash
ficticio generado al iniciar. Cinco intentos fallidos por email normalizado dentro
de 15 minutos bloquean temporalmente nuevos intentos con 429; éxito reinicia esa
cuenta. La ventana y cantidad se configuran. El mapa tiene hasta 10.000 identidades,
purga ventanas vencidas y rechaza nuevas si está lleno. Los intentos de servicios
usan otro prefijo de identidad. No se confía en X-Forwarded-For.

La mitigación vive en memoria por instancia; reiniciar la limpia. Para desplegar
réplicas debe externalizarse ese contador. No constituye una protección distribuida.

El bootstrap está desactivado por defecto. Si se habilita, exige email válido,
contraseña válida y base vacía; nunca promueve un email registrado públicamente.
En posteriores arranques sólo tolera el administrador ya creado con ese email.
Los roles se administran por un endpoint con ADMINISTRADOR; no existe promoción
pública ni contraseña predeterminada en el código.

## Credenciales internas y llamadas HTTP

POST /auth/service-token recibe HTTP Basic con un ID fijo (eventos, reservas,
compras, entradas) y su secreto independiente configurado, de mínimo 32 caracteres.
La comparación del secreto usa MessageDigest.isEqual. No se aceptan roles ni ID
de usuario en esa solicitud. El token dura cinco minutos y tiene token_use=service,
sub=service:<id>, roles=[] y el resto de las claims estándar. Sólo genera
SERVICE_<id>, nunca ROLE_* ni USER.

Cada servicio obtiene y conserva su token hasta 30 s antes del vencimiento.
Feign propaga el JWT de usuario en GET con contexto humano; para escrituras
internas, tareas programadas o solicitudes recibidas desde otro servicio, obtiene
su propia credencial. Los dos RestClient de Entradas aplican la misma política.
Así los reintentos de cancelación no dependen de una sesión de administrador ni
comparten la identidad del servicio llamador. Los tiempos de conexión y lectura
del cliente de credenciales están acotados a 3 y 5 s.

Los permisos internos están limitados por ruta e identidad receptora; ver matriz.
Los tokens de servicio pueden leer algunos recursos necesarios para emisión y
reintegro, pero no administrar usuarios ni crear reservas/compras como personas.
Los secretos deben distribuirse sólo al emisor y a su servicio correspondiente.
Usar TLS al salir del entorno local; Basic y Bearer requieren transporte protegido.

## Dependencias

Se mantienen los parents y Spring Cloud existentes. Usuarios usa Boot 3.5.6,
Spring Security y Nimbus administrados por su BOM y Springdoc 2.8.16, igual que
Eventos. Boot 4 mantiene sus starters correspondientes. Las implementaciones de
validación están locales a cada proyecto para conservar compilación independiente
sin instalar una biblioteca interna ni acoplar Boot 3 con Boot 4; mantener el
contrato y sus pruebas al evolucionarlo.

Referencias verificadas: [Springdoc y Boot 3](https://springdoc.org/v2/),
[JWT MVC](https://docs.spring.io/spring-security/reference/servlet/oauth2/resource-server/jwt.html)
y [JWT reactivo](https://docs.spring.io/spring-security/reference/reactive/oauth2/resource-server/jwt.html).
