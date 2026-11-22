

# Permisos y seguridad — Usuarios

## Roles

TicketYa utiliza cuatro roles de usuario:

| Rol | Responsabilidad |
|---|---|
| `CLIENTE` | Consultar eventos, reservar, comprar y gestionar sus entradas |
| `ORGANIZADOR` | Crear y administrar los eventos asociados a su identidad |
| `ADMINISTRADOR` | Administrar usuarios y realizar operaciones administrativas |
| `PERSONAL_ACCESO` | Validar entradas de eventos para los que tenga autorización |

Un usuario puede tener varios roles.

Las comunicaciones internas utilizan identidades `SERVICE_*`, independientes de los roles humanos.

## Permisos de los endpoints de Usuarios

| Método | Endpoint | Acceso |
|---|---|---|
| POST | `/auth/register` | Público |
| POST | `/auth/login` | Público |
| POST | `/auth/service-token` | Credenciales internas HTTP Basic |
| GET | `/usuarios/me` | Usuario autenticado |
| GET | `/usuarios/{id}` | Titular o administrador |
| GET | `/usuarios/{id}/contacto` | Servicios Eventos y Compras |
| PATCH | `/usuarios/{id}/roles` | Administrador |
| PATCH | `/usuarios/{id}/estado` | Administrador |

El endpoint interno de contacto devuelve únicamente el identificador y correo electrónico del usuario.

## Reglas de autorización

- Los usuarios externos se autentican mediante `Authorization: Bearer <JWT>`.
- El acceso a información de terceros requiere autorización.
- La administración de roles y estados está reservada a `ADMINISTRADOR`.
- Los tokens de servicio no otorgan permisos administrativos de usuario.
- Los JWT se validan tanto en el Gateway como en los microservicios.
- Las rutas que no poseen una autorización explícita son rechazadas.

## Respuestas de seguridad

| HTTP | Significado |
|---|---|
| 401 | Autenticación ausente, inválida o vencida |
| 403 | Usuario autenticado sin permisos suficientes |
| 429 | Límite temporal de intentos de autenticación |

Los controles de titularidad de reservas, compras, entradas y otros recursos se implementan en los microservicios responsables de esos datos.