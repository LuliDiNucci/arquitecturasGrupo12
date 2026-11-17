# TicketYa · Microservicio de notificaciones

El servicio recibe pedidos internos de correo, los guarda en su propia base y responde antes del envío. Compras, Eventos o el servicio responsable de las preferencias podrán llamar a los endpoints cuando integren sus flujos. Cada solicitud representa a un destinatario e incluye su ID y su correo. Para una cancelación o un cambio, el servicio de origen identifica a los compradores y hace una solicitud por cliente.

## Ejecutar desde VS Code

Abrí el repositorio del grupo, agregá esta carpeta en `TPE-micro1/msvc-notificaciones` y usá Java 21. En la terminal de PowerShell:

```powershell
cd TPE-micro1/msvc-notificaciones
.\mvnw.cmd test
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=local"
```

En Linux o macOS, usá `./mvnw` en lugar de `.\mvnw.cmd`. También podés usar `mvn` si ya instalaste Maven. El perfil `local` guarda la base H2 en `./data/`, de modo que conserva los avisos al reiniciar. Con el perfil predeterminado se usa MySQL y el esquema `ticketya_notificaciones`. Podés configurar `DB_URL`, `DB_USER` y `DB_PASSWORD`. El puerto HTTP predeterminado es 8085 y se puede cambiar con `PORT`.

Si tenés Docker, podés levantar Mailpit para recibir correos de prueba:

```powershell
docker run --rm --name ticketya-mailpit -p 127.0.0.1:1025:1025 -p 127.0.0.1:8025:8025 axllent/mailpit
```

- [Swagger UI](http://localhost:8085/swagger-ui.html)
- [Especificación OpenAPI](http://localhost:8085/v3/api-docs)
- [Bandeja de Mailpit](http://localhost:8025)

Los valores SMTP predeterminados son `localhost:1025` y `notificaciones@ticketya.test`. Para otro servidor se pueden definir `SMTP_HOST`, `SMTP_PORT`, `SMTP_USER`, `SMTP_PASSWORD`, `SMTP_AUTH`, `SMTP_STARTTLS` y `MAIL_FROM`.

## Contratos REST

Todos los POST reciben JSON. Un registro pendiente devuelve **202** con el estado y el encabezado `Location: /notificaciones/{id}`. Si se repite un pedido, la respuesta conserva el ID original. Devuelve **200** cuando ese aviso ya está enviado o fallido, o **202** cuando sigue pendiente. Una solicitud inválida devuelve **400** en formato `application/problem+json`.

| Método y ruta | Solicitud | Identidad de un aviso |
| --- | --- | --- |
| `POST /notificaciones/compras-confirmadas` | `destinatario`, `compraId`, `nombreEvento` | Compra y cliente |
| `POST /notificaciones/eventos-cancelados` | `destinatario`, `eventoId`, `nombreEvento` | Evento y cliente |
| `POST /notificaciones/eventos-modificados` | `destinatario`, `eventoId`, `versionEvento`, `nombreEvento`, `detalleCambios` | Evento, versión y cliente |
| `POST /notificaciones/artistas-favoritos` | `destinatario`, `eventoId`, `nombreEvento`, `fechaHora`, `ciudadEvento`, `artistaId`, `nombreArtista`, `ciudadCliente`, `artistasFavoritosIds` | Evento y cliente |
| `GET /notificaciones/{id}` | ID devuelto en `Location` | **200** con el estado o **404** si el ID no existe |

El origen debe aportar la versión del evento correspondiente a la modificación. El aviso de artista favorito devuelve **204** si la ciudad del evento difiere de la del cliente o si el artista no figura entre sus favoritos. Se ignoran mayúsculas y espacios a los extremos de los nombres de ciudad. Si coinciden dos artistas favoritos del mismo evento, el primer aviso registrado cubre la publicación.

### Ejemplos de solicitudes

**Compra confirmada**

```json
{
  "destinatario": { "clienteId": 12, "email": "cliente@example.test" },
  "compraId": 101,
  "nombreEvento": "Recital en Tandil"
}
```

**Evento cancelado**

```json
{
  "destinatario": { "clienteId": 12, "email": "cliente@example.test" },
  "eventoId": 45,
  "nombreEvento": "Recital en Tandil"
}
```

**Evento modificado**

```json
{
  "destinatario": { "clienteId": 12, "email": "cliente@example.test" },
  "eventoId": 45,
  "versionEvento": 3,
  "nombreEvento": "Recital en Tandil",
  "detalleCambios": "La apertura de puertas se pasa a las 20:30."
}
```

**Artista favorito**

```json
{
  "destinatario": { "clienteId": 12, "email": "cliente@example.test" },
  "eventoId": 45,
  "nombreEvento": "Recital en Tandil",
  "fechaHora": "2026-12-05T21:00:00",
  "ciudadEvento": "Tandil",
  "artistaId": 8,
  "nombreArtista": "Los Viajeros",
  "ciudadCliente": "Tandil",
  "artistasFavoritosIds": [8, 15]
}
```

### Ejemplo de respuesta

```json
{
  "id": 1,
  "tipo": "COMPRA_CONFIRMADA",
  "referenciaId": 101,
  "versionEvento": 0,
  "clienteId": 12,
  "estado": "PENDIENTE",
  "intentos": 0,
  "creadaEn": "2026-11-16T22:00:00Z",
  "proximoIntentoEn": "2026-11-16T22:00:00Z",
  "enviadaEn": null,
  "ultimoError": null
}
```

`referenciaId` corresponde a la compra para `COMPRA_CONFIRMADA` y al evento para los otros tipos. `versionEvento` vale cero en los tipos que prescinden de ella. Las fechas del estado se devuelven en UTC y `fechaHora` del evento corresponde a la hora local provista por el origen. Si falla el envío, `ultimoError` contiene el tipo de error de correo.

## Procesamiento y reintentos

1. El controller valida el DTO. El servicio arma el texto y registra la notificación como `PENDIENTE`.
2. El scheduler consulta en segundo plano las notificaciones que tienen un intento programado para ese momento. El procesador toma un bloqueo de escritura de la fila y verifica de nuevo su estado antes de enviar por SMTP.
3. Si SMTP acepta el correo, se guarda `ENVIADA`. Si falla, la fila sigue `PENDIENTE` hasta el próximo intento. Al agotar el máximo se guarda `FALLIDA`.

Por defecto se revisa la cola cada segundo y se hacen hasta tres intentos en total, con diez segundos entre intentos fallidos. Se puede ajustar con `NOTIFICACIONES_INTERVALO_MS`, `NOTIFICACIONES_MAX_INTENTOS` y `NOTIFICACIONES_REINTENTO_SEGUNDOS`. Como la cola vive en la base, los avisos pendientes se retoman al arrancar.

La restricción única `(tipo, referencia_id, version_evento, cliente_id)` evita filas duplicadas incluso ante solicitudes simultáneas. El bloqueo evita dos envíos simultáneos de una misma fila. Si SMTP acepta el mensaje y el proceso se interrumpe antes de confirmar la transacción, puede haber una segunda entrega en el siguiente intento.

La API es interna. El cableado de los llamados desde los servicios de origen y la autenticación en la arquitectura del grupo se coordinan al integrar los módulos.

## Pruebas

`mvn test` usa JUnit, H2 y un SMTP de pruebas GreenMail que arranca con la suite. Cubre los contratos HTTP, Swagger, recuperación y agotamiento de reintentos, solicitudes duplicadas concurrentes y procesamiento concurrente. Para probar manualmente, enviá un POST desde Swagger, consultá el `Location` y buscá el correo en Mailpit.
