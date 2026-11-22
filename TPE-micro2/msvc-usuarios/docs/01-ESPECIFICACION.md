

# Funcionalidades e historias de usuario — Usuarios

## Objetivo

El microservicio administra la identidad y autenticación de los usuarios de TicketYa. Centraliza el registro, la emisión de credenciales y la gestión de roles utilizados para autorizar operaciones en los demás microservicios.

## Funcionalidades

| Código | Funcionalidad |
|---|---|
| F-US-01 | Registro de clientes |
| F-US-02 | Autenticación y emisión de JWT |
| F-US-03 | Consulta de perfiles |
| F-US-04 | Administración de roles y cuentas |
| F-US-05 | Autenticación entre microservicios |
| F-US-06 | Protección de credenciales y accesos |

## Historias de usuario y criterios de aceptación

### US-01 — Registro de clientes

Como visitante, quiero crear una cuenta para acceder a las funcionalidades de TicketYa.

**Criterios de aceptación:**

- El registro válido crea un usuario con rol `CLIENTE`.
- El correo electrónico se normaliza y debe ser único.
- No se permite asignar privilegios mediante el registro público.
- Los datos inválidos producen HTTP 400 y los correos duplicados HTTP 409.

### US-02 — Inicio de sesión

Como usuario registrado, quiero iniciar sesión para acceder a las operaciones permitidas por mis roles.

**Criterios de aceptación:**

- Las credenciales válidas generan un JWT firmado.
- Las credenciales incorrectas o las cuentas inactivas producen HTTP 401.
- Cinco intentos fallidos dentro de la ventana configurada generan un bloqueo temporal con HTTP 429.
- La respuesta no expone contraseñas ni hashes.

### US-03 — Consulta de perfiles

Como usuario autenticado, quiero consultar mis datos personales.

**Criterios de aceptación:**

- Cada usuario puede consultar su propio perfil.
- Un administrador puede consultar otros usuarios.
- El acceso a datos de otro usuario sin autorización produce HTTP 403.

### US-04 — Administración de usuarios

Como administrador, quiero gestionar roles y estados de cuentas.

**Criterios de aceptación:**

- Solamente `ADMINISTRADOR` puede modificar roles o habilitar y deshabilitar usuarios.
- Se admiten los cuatro roles definidos por TicketYa.
- Los cambios de roles se reflejan al emitir un nuevo JWT.
- Las cuentas inactivas no pueden iniciar nuevas sesiones.

### US-05 — Autenticación interna

Como microservicio, quiero obtener una credencial propia para realizar operaciones internas autorizadas.

**Criterios de aceptación:**

- La autenticación interna utiliza credenciales de servicio configuradas.
- Se emiten JWT específicos de servicio, sin roles humanos.
- Los tokens internos tienen una vigencia de cinco minutos.
- Las credenciales inválidas son rechazadas.

## Reglas generales

La autenticación utiliza JWT RS256 y las contraseñas se almacenan mediante BCrypt. La autorización considera tanto el rol como la titularidad de los recursos.

El API Gateway controla el acceso externo y cada microservicio aplica nuevamente sus restricciones de seguridad.