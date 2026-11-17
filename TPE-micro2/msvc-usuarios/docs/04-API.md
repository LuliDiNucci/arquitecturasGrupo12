# Contratos REST de Usuarios

Base directa: http://localhost:8085. Gateway: http://localhost:8084.
Content-Type: application/json. Los IDs de usuario son Long positivos.
Swagger UI usa el esquema bearerJwt (botón Authorize, pegar sólo el token).
Las respuestas nunca contienen passwordHash.

## Registro: POST /auth/register

Público; 201 y Location: /usuarios/{id}.

~~~json
{
  "nombre": "Ana",
  "apellido": "Pérez",
  "email": "Ana@Example.com",
  "password": "Ejemplo-seguro-2026!",
  "roles": ["ADMINISTRADOR"]
}
~~~

Aunque se envíe roles o rol, el resultado siempre es CLIENTE:

~~~json
{
  "id": 12,
  "nombre": "Ana",
  "apellido": "Pérez",
  "email": "ana@example.com",
  "roles": ["CLIENTE"],
  "activo": true,
  "fechaCreacion": "2026-11-16T20:00:00Z"
}
~~~

400: campos vacíos, email inválido, longitud inválida o password de más de 72
bytes UTF-8. 409: email duplicado después de normalizarlo. Otros campos
desconocidos se rechazan con 400; rol/roles son ignorados deliberadamente.

## Login: POST /auth/login

Público; las credenciales se validan en Usuarios.

~~~json
{"email":"ana@example.com","password":"Ejemplo-seguro-2026!"}
~~~

200, Cache-Control: no-store:

~~~json
{
  "accessToken": "<JWT_RS256>",
  "tokenType": "Bearer",
  "expiresIn": 3600,
  "expiresAt": "2026-11-16T21:00:00Z",
  "usuario": {
    "id": 12,
    "nombre": "Ana",
    "apellido": "Pérez",
    "email": "ana@example.com",
    "roles": ["CLIENTE"],
    "activo": true,
    "fechaCreacion": "2026-11-16T20:00:00Z"
  }
}
~~~

400: solicitud inválida. 401: Credenciales inválidas (mismo mensaje para email
inexistente, password incorrecta y cuenta inactiva). 429: límite temporal; esperar
la ventana configurada y volver a intentar. No hay sesión/cookie ni refresh token.

## Perfil

GET /usuarios/me requiere Authorization: Bearer <JWT>.
GET /usuarios/{id} requiere ser el titular o ADMINISTRADOR.
200 devuelve el UsuarioResponse del registro. 401 indica JWT ausente, inválido
o vencido; 403 otro usuario o token de servicio; 404 usuario inexistente
si el solicitante está autorizado a consultarlo.

## Administración

PATCH /usuarios/{id}/roles reemplaza el conjunto completo de roles:

~~~json
{"roles":["CLIENTE","ORGANIZADOR"]}
~~~

PATCH /usuarios/{id}/estado administra la habilitación para próximos logins:

~~~json
{"activo":false}
~~~

Ambos requieren ADMINISTRADOR y devuelven 200 con UsuarioResponse.
400: rol desconocido, conjunto nulo/vacío, roles nulos o activo no booleano/nulo.
401: JWT inválido/ausente/vencido. 403: sin rol administrativo.
404: usuario inexistente. Los JWT ya emitidos mantienen su vigencia hasta exp.
El estado se expone para gestionar cuentas inactivas sin editar SQL manualmente.

## Token interno: POST /auth/service-token

Header: Authorization: Basic base64(clientId:secret).
No recibe roles ni claims en un body. Los IDs configurables de credenciales son
eventos, reservas, compras y entradas. Un servicio no es una cuenta Usuario.

200 devuelve TokenResponse con expiresIn=300, usuario=null y tokenType=Bearer,
Cache-Control: no-store. 401 para credencial faltante, inválida o no configurada;
429 al superar el límite. No usar un Bearer de persona para obtener otro rol.
Swagger ofrece serviceBasic separadamente. Los servicios lo usan de forma
automática; una persona no necesita este endpoint para reservar o comprar.

## Vinculación agregada a Eventos

PATCH /organizadores/{id}/usuario en Eventos (8081) o Gateway. Requiere
ADMINISTRADOR; mantiene el ID de negocio separado de la identidad autenticada.

~~~json
{"usuarioId":12}
~~~

200: OrganizadorResponseDTO existente. 400: ID de usuario nulo/no positivo.
404: organizador o usuario inexistente. 409: usuario inactivo, sin rol ORGANIZADOR
o ya vinculado a otro organizador. 503: Usuarios no disponible, error de comunicación,
respuesta inválida o imposibilidad de autorizar la consulta remota.

Antes de guardar, Eventos consulta GET /usuarios/{usuarioId} por Feign y comprueba
id, activo y roles del DTO. Se propaga el JWT del ADMINISTRADOR con el interceptor
existente para lecturas; no se amplían permisos de SERVICE_eventos ni se comparten
entidades o bases JPA. La URL se configura con USUARIOS_URL (localhost:8085 por
defecto). Si no puede validar, conserva el vínculo anterior y rechaza la operación.
El administrador debe asignar ORGANIZADOR mediante Usuarios antes de vincular.

## Error uniforme del nuevo servicio

~~~json
{
  "timestamp":"2026-11-16T20:00:00Z",
  "status":400,
  "error":"Datos inválidos",
  "path":"/auth/register",
  "detalles":["email: debe ser una dirección de correo electrónico bien formada"]
}
~~~

Las excepciones no se serializan ni incluyen SQL, contraseñas, hashes o tokens.
Los servicios existentes conservan sus DTO de errores de negocio; Spring
Security devuelve 401/403 para la capa de autenticación/autorización.

## Postman

Importar postman/TicketYa-Usuarios-JWT.postman_collection.json (formato v2.1).
El folder de cliente incluye registro, login, /me, duplicado, token inválido y
escalada de roles denegada. El login guarda accessToken en token y el registro
guarda clientId. Cambiar baseUrl entre 8085 y 8084. El folder administrativo
requiere completar adminEmail/adminPassword locales; guarda adminToken.
No exportar variables pobladas con credenciales o tokens reales.
