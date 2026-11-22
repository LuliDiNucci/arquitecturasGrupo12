

# Contratos REST — Usuarios

## Información general

- URL directa: `http://localhost:8085`
- API Gateway: `http://localhost:8084`
- Formato: JSON
- Autenticación: JWT Bearer para operaciones de usuario

Los contratos completos se encuentran disponibles en Swagger/OpenAPI.

## 1. Registrar cliente

`POST /auth/register` — Público.

**Solicitud:**

```json
{
  "nombre": "Ana",
  "apellido": "Perez",
  "email": "ana@example.com",
  "password": "ContrasenaEjemplo2026!"
}
```

**Respuesta:** HTTP 201.

```json
{
  "id": 12,
  "nombre": "Ana",
  "apellido": "Perez",
  "email": "ana@example.com",
  "roles": ["CLIENTE"],
  "activo": true,
  "fechaCreacion": "2026-11-22T12:00:00Z"
}
```

El registro público siempre asigna `CLIENTE`, independientemente de los roles enviados.

Errores: `400` por datos inválidos y `409` por correo duplicado.

## 2. Iniciar sesión

`POST /auth/login` — Público.

**Solicitud:**

```json
{
  "email": "ana@example.com",
  "password": "ContrasenaEjemplo2026!"
}
```

**Respuesta:** HTTP 200.

La respuesta contiene:

- `accessToken`: JWT firmado.
- `tokenType`: `Bearer`.
- `expiresIn`: vigencia en segundos.
- `expiresAt`: fecha de vencimiento.
- `usuario`: datos públicos del usuario autenticado.

Errores: `400` por solicitud inválida, `401` por credenciales incorrectas o cuenta inactiva y `429` por exceso de intentos.

## 3. Consultar perfil propio

`GET /usuarios/me`

Requiere JWT de usuario. Devuelve HTTP 200 con los datos del perfil autenticado.

## 4. Consultar usuario

`GET /usuarios/{id}`

Disponible para el titular y los administradores.

Respuestas: `200`, `401`, `403` y `404`.

## 5. Administrar roles

`PATCH /usuarios/{id}/roles`

Requiere `ADMINISTRADOR`.

**Solicitud:**

```json
{
  "roles": ["CLIENTE", "ORGANIZADOR"]
}
```

Reemplaza el conjunto de roles del usuario y devuelve HTTP 200 con el perfil actualizado.

## 6. Administrar estado

`PATCH /usuarios/{id}/estado`

Requiere `ADMINISTRADOR`.

**Solicitud:**

```json
{
  "activo": false
}
```

Permite habilitar o deshabilitar una cuenta. Devuelve HTTP 200.

## 7. Consultar contacto interno

`GET /usuarios/{id}/contacto`

Disponible únicamente para los servicios autenticados `eventos` y `compras`.

**Respuesta:** HTTP 200.

```json
{
  "id": 12,
  "email": "ana@example.com"
}
```

Este endpoint permite obtener información mínima de contacto sin compartir la base de datos de Usuarios.

## 8. Obtener token de servicio

`POST /auth/service-token`

Utiliza autenticación HTTP Basic con el identificador y secreto configurados para el microservicio.

Devuelve HTTP 200 con un JWT interno de cinco minutos de vigencia.

Está destinado a comunicaciones automáticas entre servicios y no al inicio de sesión de personas.

## Manejo de errores

La API utiliza respuestas HTTP y un formato JSON uniforme con los campos:

- `timestamp`
- `status`
- `error`
- `path`
- `detalles`

Los errores no incluyen contraseñas, hashes ni tokens.