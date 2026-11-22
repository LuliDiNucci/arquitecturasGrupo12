# Seguridad y permisos — TicketYa

## Autenticación

TicketYa utiliza autenticación mediante JWT firmado con RSA y el algoritmo RS256.

El microservicio de Usuarios administra las identidades, verifica contraseñas mediante BCrypt y emite tokens.

El API Gateway y los microservicios validan la firma y los datos del JWT, incluidos el emisor, la audiencia y la expiración.

## Roles

| Rol | Permisos principales |
|---|---|
| `CLIENTE` | Comprar entradas, gestionar reservas propias, consultar entradas, solicitar devoluciones y administrar preferencias |
| `ORGANIZADOR` | Crear y administrar eventos propios y consultar sus reportes |
| `ADMINISTRADOR` | Administrar usuarios, eventos, lugares, devoluciones, accesos y fila virtual |
| `PERSONAL_ACCESO` | Validar entradas QR en eventos para los cuales tiene asignación |

Las operaciones que afectan información de un usuario verifican su identidad. Un identificador enviado en el cuerpo de una solicitud no concede permisos por sí solo.

## Seguridad entre microservicios

Las operaciones internas protegidas utilizan JWT de servicio con autoridades independientes de los roles humanos.

Por ejemplo:

- `SERVICE_compras`: confirmación de reservas y emisión de entradas.
- `SERVICE_entradas`: devolución de lugares y operaciones de reintegro.
- `SERVICE_eventos`: comunicaciones de cancelación y avisos.
- `SERVICE_reservas`: validación de turnos en Fila Virtual.

Los permisos también se comprueban en los microservicios de destino, no únicamente en el Gateway.

## Personal de acceso

Para validar una entrada se requiere:

1. JWT válido con rol `PERSONAL_ACCESO`.
2. Asignación vigente al evento correspondiente.
3. Entrada válida, perteneciente al evento y no utilizada anteriormente.

## Respuestas de seguridad

- `401 Unauthorized`: autenticación ausente o inválida.
- `403 Forbidden`: identidad válida sin permisos suficientes.

## Protección de credenciales

Las claves RSA y credenciales de desarrollo se almacenan en archivos locales de configuración excluidos del control de versiones.

El repositorio incluye un generador para crear una configuración nueva sin distribuir contraseñas ni claves privadas.

Para consultar los permisos detallados de cada operación, ver la documentación de Usuarios y la configuración de seguridad de cada microservicio.