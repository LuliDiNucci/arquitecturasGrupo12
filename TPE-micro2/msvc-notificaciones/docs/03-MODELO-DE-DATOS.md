# Modelo de datos — Notificaciones

## Persistencia

El microservicio utiliza una base de datos relacional MySQL y Spring Data JPA.

La entidad principal es `Notificacion`, almacenada en la tabla `notificaciones`.

## Entidad Notificacion

| Atributo | Descripción |
|---|---|
| `id` | Identificador de la notificación |
| `tipo` | Tipo de aviso |
| `referenciaId` | ID de compra o evento |
| `versionEvento` | Versión del evento asociada al aviso |
| `clienteId` | Identificador del destinatario |
| `email` | Dirección de correo |
| `asunto` | Asunto del mensaje |
| `mensaje` | Contenido del correo |
| `estado` | Estado del envío |
| `intentos` | Cantidad de intentos realizados |
| `creadaEn` | Fecha de registro |
| `proximoIntentoEn` | Fecha del siguiente intento |
| `enviadaEn` | Fecha de envío exitoso |
| `ultimoError` | Último error de envío |
| `correlationId` | Identificador de trazabilidad |

## Tipos de notificación

- `COMPRA_CONFIRMADA`
- `EVENTO_CANCELADO`
- `EVENTO_MODIFICADO`
- `ARTISTA_FAVORITO`

## Estados

- `PENDIENTE`: espera procesamiento o un nuevo intento.
- `ENVIADA`: el servidor SMTP aceptó el mensaje.
- `FALLIDA`: se agotaron los intentos configurados.

## Restricciones

La combinación de `tipo`, `referencia_id`, `version_evento` y `cliente_id` es única.

Además, existe un índice sobre el estado y la fecha del próximo intento para facilitar la búsqueda de notificaciones pendientes.

Los identificadores de compras, eventos y clientes son referencias lógicas a otros microservicios. No se utilizan claves foráneas entre sus bases de datos.