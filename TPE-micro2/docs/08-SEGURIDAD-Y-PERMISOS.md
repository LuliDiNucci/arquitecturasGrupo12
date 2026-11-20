# Revisión de seguridad y permisos efectivos

## Qué se reutilizó

Usuarios ya firmaba JWT RS256 con claves RSA externas, BCrypt, emisor, audiencia,
expiración, `iat`, `jti`, `sub`, `roles` y `token_use`. Los validadores de Usuarios,
Gateway, Eventos, Reservas, Compras, Entradas y Fila Virtual ya verificaban el contrato
estricto. Se conservaron sus filtros de Resource Server, DTOs y controles existentes.

Los roles reales del enum `Rol` son `CLIENTE`, `ORGANIZADOR`, `PERSONAL_ACCESO` y
`ADMINISTRADOR`. El registro público sigue creando exclusivamente CLIENTE. Cambiar
roles o estado sigue siendo administrativo. No se confía en roles ni IDs enviados
por el cliente para construir el principal.

Existía autenticación entre servicios: cada emisor habilitado tiene un secreto
distinto, externo al repositorio, con al menos 32 caracteres. Se intercambia mediante
HTTP Basic en `/auth/service-token` por un JWT RS256 de cinco minutos, sin roles humanos,
con `token_use=service` y sujeto `service:<nombre>`. Los receptores validan firma y
claims antes de derivar autoridades `SERVICE_compras`, etc. El header de correlación
no interviene en esa decisión. Se reutilizó este mecanismo; no se añadió una API key
compartida ni un JWT estático.

## Correcciones aplicadas

- **Notificaciones:** antes no tenía Spring Security. Sus cuatro POST podían disparar
  correos anónimamente y su GET permitía consultar avisos privados. Ahora sólo Compras
  registra compras confirmadas y sólo Eventos registra cancelaciones, modificaciones
  o artistas favoritos. Ni siquiera ADMINISTRADOR puede enviar avisos arbitrarios con
  un JWT humano. La consulta requiere ser titular, administrador o el servicio emisor
  adecuado para ese tipo de notificación. Se mantienen paths, payloads y respuestas
  de negocio, y los tests SMTP existentes usan credenciales válidas.
- **Gateway:** se añadió el permiso de PERSONAL_ACCESO sobre la ruta existente
  `POST /entradas/validar`, antes denegada por defecto. Se habilitan las nuevas rutas
  administrativas de asignaciones. Sólo se enrutan consultas GET de Notificaciones
  y su OpenAPI; los POST internos siguen sin exponerse por esa ruta. El servicio
  también exige autenticación al acceder directamente por su puerto.
- **Entradas:** el rol global PERSONAL_ACCESO no representaba asignación a un evento.
  Se añadió la relación local `asignaciones_personal_acceso(evento_id, usuario_id)` y
  dos operaciones administrativas idempotentes de alta y revocación. Validar QR exige
  rol y asignación vigente al evento, además de las comprobaciones de entrada válida,
  evento coincidente y uso único existentes. La revocación de la asignación es efectiva
  inmediatamente aunque el JWT siga vigente. Asignar un ID sin rol PERSONAL_ACCESO no
  permite validar: ambas condiciones son obligatorias.
- **Recomendaciones:** ya tenía roles y controles de titularidad, pero su validador
  era menos estricto. Se reutilizó el contrato `JwtSupport` existente para exigir
  firma RS256, expiración, emisor, audiencia, sujeto y claims completos. El cliente de
  Compras obtiene el token del principal autenticado, en lugar del header bruto.
- **Todos:** identidad del log basada en JWT validado; respuestas 401/403 conservan
  correlación. Se sanearon logs de reintentos que imprimían excepciones remotas.

No se cambió la política previa de acceso a compras, reservas, eventos o devoluciones.
Los tests existentes de esos controles se conservaron y se ejecutaron. Un usuario
con JWT válido sigue sin tener permisos administrativos por ese solo hecho.

## Matriz de roles

"Propio" significa comparar `sub` autenticado con la relación persistida, no aceptar
el ID declarado por el cliente. Un usuario puede acumular roles; esta tabla muestra
lo que habilita cada rol por separado. "Público" también permite acceso sin JWT.
Los servicios tienen autoridades separadas y se describen después de la tabla.

| Recurso / operación real | Cliente | Organizador | Personal de acceso | Administrador |
|---|---|---|---|---|
| POST `/auth/register`, `/auth/login` | Público | Público | Público | Público |
| GET `/usuarios/me` | Propio | Propio | Propio | Propio |
| GET `/usuarios/{id}` | Propio | Propio | Propio | Permitido |
| PATCH `/usuarios/{id}/roles`, `/{id}/estado` | Denegado | Denegado | Denegado | Permitido |
| GET `/usuarios/{id}/contacto` | Denegado | Denegado | Denegado | Denegado |
| GET `/eventos/publicados` y búsquedas públicas | Público | Público | Público | Público |
| GET `/eventos/{id}`, sectores/precio/habilitación | Publicado o agotado | Publicado/agotado o propio | Publicado o agotado | Permitido |
| GET `/eventos` (listado administrativo) | Denegado | Denegado | Denegado | Permitido |
| POST `/eventos` | Denegado | Organizador propio | Denegado | Permitido |
| PUT `/eventos/{id}`, `/{id}/precios` | Denegado | Evento y organizador propios | Denegado | Permitido |
| PATCH `/eventos/{id}/publicar`, `/agotar`, `/reabrir`, `/finalizar` | Denegado | Evento propio | Denegado | Permitido |
| PATCH `/eventos/{id}/suspender`, `/cancelar` | Denegado | Denegado | Denegado | Permitido |
| GET `/eventos/{id}/notificaciones-cancelacion` | Denegado | Denegado | Denegado | Permitido |
| GET lugares, sectores, asientos y participantes | Público | Público | Público | Público |
| POST `/lugares`, `/lugares/{id}/sectores`, `/sectores/{id}/asientos`, `/participantes` | Denegado | Denegado | Denegado | Permitido |
| GET/POST `/organizadores`; PATCH `/{id}/usuario` | Denegado | Denegado | Denegado | Permitido |
| GET `/disponibilidad` | Público | Público | Público | Público |
| POST `/reservas` | `clienteId` debe coincidir con JWT | Denegado | Denegado | Denegado |
| GET `/reservas/{id}` | Reserva propia | Denegado | Denegado | Permitido |
| POST `/reservas/{id}/confirmar`, `/liberar`, `/devolver-entrada` | Denegado | Denegado | Denegado | Denegado |
| POST `/reservas/evento/{id}/habilitar`, `/cancelar` | Denegado | Denegado | Denegado | Denegado |
| POST `/compras` | Cliente y reserva propios | Denegado | Denegado | Denegado |
| GET `/compras/{id}`, `/{id}/confirmacion`, `/{id}/reintegros` | Compra propia | Denegado | Denegado | Permitido |
| POST `/compras/{id}/pagar`, `/confirmar`, `/reintentar-liberacion` | Compra propia | Denegado | Denegado | Denegado |
| POST `/compras/{id}/compensar` | Denegado | Denegado | Denegado | Permitido |
| GET `/compras/cliente/{id}/eventos-recomendados` (llamada directa del cliente de Recomendaciones) | Cliente propio | Denegado | Denegado | Denegado |
| POST `/compras/reintegros`, `/compras/evento/{id}/cancelar`; GET compradores | Denegado | Denegado | Denegado | Denegado |
| POST `/entradas/emitir`, `/entradas/evento/{id}/invalidar` | Denegado | Denegado | Denegado | Denegado |
| GET `/entradas/{id}`, `/entradas/cliente/{id}` | Entrada/cliente propios | Denegado | Denegado | Permitido, con titular real en el header exigido por el contrato de consulta individual |
| POST `/entradas/validar` | Denegado | Denegado | Sólo evento asignado y entrada válida, no utilizada | Denegado |
| PUT/DELETE `/entradas/accesos/eventos/{eventoId}/personal/{usuarioId}` | Denegado | Denegado | Denegado | Permitido |
| POST `/devoluciones` | Entrada propia | Denegado | Denegado | Denegado |
| GET `/devoluciones/pendientes`; PATCH `/{id}/aprobar`, `/rechazar` | Denegado | Denegado | Denegado | Permitido; administradorId real del JWT |
| POST `/filas/{eventoId}/ingresar` | Cliente propio | Denegado | Denegado | Denegado |
| GET `/filas/{eventoId}/clientes/{clienteId}` | Cliente propio | Denegado | Denegado | Permitido |
| POST `/filas/{eventoId}/habilitar` | Denegado | Denegado | Denegado | Permitido |
| GET `/filas/{eventoId}/clientes/{clienteId}/validar`; POST `/finalizar` | Denegado | Denegado | Denegado | Denegado |
| GET/POST/PUT `/api/preferencias/{clienteId}` | Propio | Denegado | Denegado | Permitido |
| GET `/api/recomendaciones/{clienteId}` | Propio | Denegado | Denegado | Permitido |
| GET `/notificaciones/{id}` | Propia | Propia | Propia | Permitido |
| POST `/notificaciones/compras-confirmadas` | Denegado | Denegado | Denegado | Denegado |
| POST `/notificaciones/eventos-cancelados`, `/eventos-modificados`, `/artistas-favoritos` | Denegado | Denegado | Denegado | Denegado |

Swagger/OpenAPI es público como antes. La salud publicada por Gateway no muestra
detalles. Un token inválido que se envía incluso a una ruta pública es rechazado por
el Resource Server; "público" no significa que se acepten tokens falsificados.

## Permisos internos

| Identidad de servicio | Operaciones habilitadas |
|---|---|
| `service:compras` | Confirmar/liberar reservas, emitir entradas, consultar entradas para reintegros, obtener contacto y registrar/consultar avisos de compra. |
| `service:entradas` | Consultar confirmación de compra/reserva, devolver lugares y solicitar reintegros. |
| `service:eventos` | Habilitar/cancelar reservas, invalidar entradas, cancelar compras, consultar compradores/contactos y registrar/consultar avisos de eventos. |
| `service:reservas` | Consultar información de Eventos y validar/finalizar turnos de Fila Virtual. |

Las lecturas Feign del flujo HTTP conservan el JWT humano cuando esa era la política
existente; las escrituras internas usan el JWT de la identidad del servicio que llama.
Notificaciones no necesita credencial emisora propia porque no llama a otros servicios.
Los endpoints no adquieren seguridad por llamarse "internal": todos pasan por el
Resource Server y la autorización del servicio responsable.

## Límites y decisiones conservadas

- La baja de un usuario o el cambio de roles impide nuevos accesos según el servicio
  de Usuarios, pero los JWT ya emitidos conservan sus claims hasta su expiración.
  Es el comportamiento previo documentado; no se introdujo revocación distribuida.
- Las asignaciones de acceso son referencias por ID, como las demás relaciones entre
  microservicios. Para que sean efectivas se exige además el rol del JWT; no se comparte
  la entidad Usuario ni se hacen joins entre bases.
- Los puertos directos de aplicación siguen protegidos; no se depende sólo del Gateway.
- El stack Loki está preparado para desarrollo local y su API se enlaza a loopback.
  No se debe publicar como un servicio multiusuario en Internet sin autenticación externa.
- Los secretos generados viven en `.env`/`.secrets`, ignorados por Git. No se hicieron
  commits ni push, ni se almacenó la clave privada en imágenes o documentación.
