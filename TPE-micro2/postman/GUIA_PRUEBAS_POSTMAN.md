
# Pruebas de integración con Postman — TicketYa

## 1. Objetivo

Esta guía describe cómo ejecutar las pruebas de integración de TicketYa utilizando Postman y los microservicios desplegados mediante Docker Compose.

Las pruebas permiten verificar:

- Autenticación y autorización mediante JWT.
- Gestión de usuarios y roles.
- Creación y publicación de eventos.
- Gestión de lugares, sectores y asientos.
- Búsqueda de eventos y consulta de disponibilidad.
- Registro de preferencias y recomendaciones.
- Reservas temporales.
- Compras y pagos simulados.
- Emisión y consulta de entradas digitales.
- Validación de códigos QR.
- Solicitudes de devolución y reintegros.
- Reportes de eventos.
- Fila virtual para eventos de alta demanda.
- Cancelaciones, modificaciones y manejo de errores.

Las solicitudes se realizan a través del API Gateway.

## 2. Archivos de Postman

La carpeta `postman/` contiene las siguientes colecciones:

| Archivo                                         | Descripción                                                             |
| ----------------------------------------------- | ----------------------------------------------------------------------- |
| `TicketYa-Integracion.postman_collection.json`  | Colección principal de integración de la Entrega 2, con 89 solicitudes. |
| `TicketYa-Usuarios-JWT.postman_collection.json` | Pruebas adicionales de autenticación, JWT, usuarios y seguridad.        |

La colección principal organiza las solicitudes según los procesos de negocio y los roles necesarios para ejecutarlas.

La colección de Usuarios y JWT se conserva como complemento para verificar escenarios específicos de seguridad.

## 3. Requisitos previos

Antes de comenzar:

1. Tener Docker Desktop iniciado.
2. Tener configurado el archivo `.env` del proyecto.
3. Tener generadas las claves JWT necesarias.
4. Levantar los microservicios y sus bases de datos mediante Docker Compose.
5. Verificar que el API Gateway esté disponible en `http://localhost:8084`.
6. Tener instalado Postman.
7. Contar con un usuario administrador válido.

Desde la raíz del proyecto, ejecutar:

```powershell
docker compose up -d --build
```

Para consultar el estado de los contenedores:

```powershell
docker compose ps
```

Los servicios principales deben encontrarse en ejecución.

No es necesario eliminar las bases de datos antes de realizar las pruebas. La colección genera nuevos usuarios y recursos identificados mediante valores únicos.

## 4. Configuración de Postman

### 4.1. Importar la colección

1. Abrir Postman.
2. Seleccionar **Import**.
3. Importar `TicketYa-Integracion.postman_collection.json`.
4. Abrir la colección importada.
5. Ingresar en la pestaña **Variables**.

### 4.2. Configurar variables

La colección utiliza como dirección principal:

```text
baseUrl = http://localhost:8084
```

Antes de comenzar, configurar las siguientes variables con las credenciales del administrador existente:

| Variable        | Valor                         |
| --------------- | ----------------------------- |
| `adminEmail`    | Correo del administrador.     |
| `adminPassword` | Contraseña del administrador. |

Las credenciales deben configurarse localmente en Postman. No deben incluirse en archivos compartidos ni en el repositorio.

Las demás variables se completan automáticamente durante las pruebas.

Entre ellas:

- `qaId`
- `clienteId`
- `clienteToken`
- `organizadorUsuarioId`
- `organizadorToken`
- `personalId`
- `personalToken`
- `eventoId`
- `sectorCampoId`
- `sectorNumeradoId`
- `reservaId`
- `compraId`
- `entradaQrId`
- `entradaDevolucionId`
- `codigoQr`
- `devolucionId`
- `eventoAltaDemandaId`

Los tokens JWT se obtienen mediante los endpoints de autenticación.

**Importante:** las solicitudes deben ejecutarse en el orden indicado. No se recomienda utilizar `Run Collection` sobre las 89 solicitudes, ya que algunas pruebas opcionales modifican o cancelan recursos utilizados por otros escenarios.

## 5. Orden de ejecución principal

La colección de integración contiene siete carpetas:

| Carpeta                                                  | Cantidad de solicitudes | Ejecución |
| -------------------------------------------------------- | ----------------------: | --------- |
| `00 - Preparar identidades y JWT`                        |                      12 | Principal |
| `01 - Catálogo y preparación del evento`                 |                      25 | Principal |
| `02 - COMPRA COMPLETA`                                   |                      10 | Principal |
| `03 - Acceso QR, devolución y reportes`                  |                      13 | Principal |
| `04 - Fila virtual alta demanda`                         |                       9 | Principal |
| `90 - Opcionales: actualización, cancelación y rechazos` |                      11 | Opcional  |
| `91 - Seguridad y errores`                               |                       9 | Opcional  |
| **Total**                                                |                  **89** |           |

Las carpetas principales deben ejecutarse de la 00 a la 04.

Las carpetas 90 y 91 contienen pruebas adicionales que deben ejecutarse individualmente cuando corresponda.

---

## 6. Carpeta 00 — Preparar identidades y JWT

**Objetivo:** preparar usuarios de prueba y obtener los tokens necesarios para los diferentes roles.

Ejecutar las solicitudes de la carpeta `00 - Preparar identidades y JWT` en orden:

1. Inicializar los datos de prueba.
2. Iniciar sesión como administrador.
3. Registrar un cliente.
4. Iniciar sesión como cliente.
5. Consultar el perfil del cliente.
6. Registrar un usuario organizador.
7. Asignarle el rol `ORGANIZADOR`.
8. Iniciar sesión como organizador.
9. Registrar un usuario de personal de acceso.
10. Asignarle el rol `PERSONAL_ACCESO`.
11. Iniciar sesión como personal de acceso.
12. Consultar el perfil del administrador.

La primera solicitud genera un identificador único de ejecución (`qaId`), que se utiliza para evitar conflictos con datos anteriores.

También genera correos y contraseñas de prueba para los usuarios nuevos.

Los identificadores y tokens obtenidos quedan almacenados en las variables de la colección.

### Resultados esperados

- Los registros de usuarios devuelven HTTP `201 Created`.
- Los inicios de sesión devuelven HTTP `200 OK`.
- Los perfiles se consultan correctamente.
- Los roles administrativos se asignan mediante un usuario autorizado.
- Los tokens corresponden a los roles configurados.

**Importante:** las modificaciones de roles requieren un nuevo inicio de sesión para obtener un JWT con los permisos actualizados.

No debe continuarse con la siguiente carpeta si falló alguno de los pasos necesarios para crear o autenticar los usuarios.

---

## 7. Carpeta 01 — Catálogo y preparación del evento

**Objetivo:** crear los recursos necesarios para ejecutar el circuito completo de compra.

Ejecutar las 25 solicitudes de `01 - Catálogo y preparación del evento` en orden.

### 7.1. Configuración inicial

Las solicitudes crean:

1. Un organizador.
2. Su vinculación con el usuario que tiene rol `ORGANIZADOR`.
3. Un participante de tipo artista.
4. Un lugar.
5. Un sector numerado.
6. Un sector no numerado.
7. Los asientos del sector numerado.

También permiten consultar los lugares, organizadores, participantes y asientos registrados.

### 7.2. Preferencias

Se registran preferencias para el cliente de prueba:

- Tipo de evento: `RECITAL`.
- Artista favorito: el participante creado durante la ejecución.
- Ciudad: `Tandil`.

Esto prepara los datos necesarios para probar las recomendaciones y los avisos por artistas favoritos.

### 7.3. Creación y publicación

Se crea un evento asociado al organizador y al lugar de prueba.

Luego:

1. Se configuran los precios de los sectores.
2. Se publica el evento.
3. Se consulta el catálogo público.
4. Se ejecutan búsquedas con filtros.
5. Se consulta el detalle.
6. Se consultan sectores y precios.
7. Se comprueba la disponibilidad.

El evento se crea con una fecha futura generada automáticamente.

### 7.4. Recomendaciones

Al final de la carpeta se consultan:

- Las preferencias del cliente.
- Las recomendaciones generadas para ese cliente.

### Resultados esperados

- Los recursos se crean correctamente.
- El evento queda en estado `PUBLICADO`.
- El evento aparece en las consultas públicas correspondientes.
- Se obtienen los sectores y sus precios.
- La disponibilidad se consulta correctamente.
- El servicio de Recomendaciones responde utilizando las preferencias registradas.

La publicación también puede generar una notificación de artista favorito, que se procesa automáticamente mediante el servicio de Notificaciones.

---

## 8. Carpeta 02 — Compra completa

**Objetivo:** verificar el circuito principal de reserva, pago, confirmación y emisión de entradas.

Esta carpeta contiene 10 solicitudes y debe ejecutarse de manera consecutiva.

**Las reservas temporales vencen después de 10 minutos.** Por eso, no debe interrumpirse el proceso entre la creación de la reserva y la confirmación de la compra.

### Orden

1. Reservar dos lugares del sector Campo.
2. Consultar la reserva.
3. Iniciar la compra.
4. Consultar los datos de la compra.
5. Procesar el pago simulado con tarjeta.
6. Consultar la confirmación del pago.
7. Confirmar la compra y emitir las entradas.
8. Consultar las entradas del cliente.
9. Consultar el QR de una entrada.
10. Consultar el estado final de la compra.

Se reservan **dos entradas** para poder realizar posteriormente dos pruebas diferentes:

- Validar una entrada mediante QR.
- Solicitar la devolución de la otra.

### Resultados esperados

| Operación      | Resultado              |
| -------------- | ---------------------- |
| Reserva        | `ACTIVA`               |
| Compra inicial | `PENDIENTE`            |
| Pago simulado  | `APROBADO`             |
| Compra final   | `CONFIRMADA`           |
| Entradas       | `EMITIDA`              |
| Código QR      | Generado correctamente |

Durante el proceso se almacenan automáticamente:

- `reservaId`
- `compraId`
- `entradaQrId`
- `entradaDevolucionId`
- `codigoQr`

### Consideraciones

No es necesario llamar manualmente al endpoint interno de emisión de entradas.

La confirmación de compra coordina las operaciones correspondientes entre Compras, Reservas y Entradas.

Si la reserva vence antes de finalizar el pago, se debe comenzar una nueva ejecución del circuito de compra con una reserva vigente.

---

## 9. Carpeta 03 — Acceso QR, devolución y reportes

**Objetivo:** comprobar las operaciones posteriores a la compra.

Ejecutar las 13 solicitudes de `03 - Acceso QR, devolución y reportes` en orden.

### 9.1. Validación de QR

Primero:

1. El administrador asigna al personal de acceso al evento.
2. El personal valida el QR de una entrada.
3. Se intenta validar nuevamente el mismo QR.

### Resultados esperados

| Operación            | Resultado             |
| -------------------- | --------------------- |
| Asignar personal     | HTTP `204 No Content` |
| Primera validación   | HTTP `200 OK`         |
| Estado de la entrada | `UTILIZADA`           |
| Segunda validación   | HTTP `409 Conflict`   |

El código `409` de la segunda validación es un resultado correcto: demuestra que una entrada no puede utilizarse dos veces.

### 9.2. Devolución

Después, el cliente solicita la devolución de la segunda entrada.

La operación se realiza sobre una entrada diferente de la utilizada para validar el QR.

Ejecutar:

1. Solicitar devolución.
2. Consultar solicitudes pendientes como administrador.
3. Aprobar la devolución.
4. Consultar los reintegros asociados a la compra.
5. Consultar la entrada devuelta.
6. Consultar nuevamente la entrada utilizada.

### Resultados esperados

- La solicitud de devolución se registra.
- El administrador puede consultar la solicitud pendiente.
- La devolución queda en estado `APROBADA`.
- Se procesa el reintegro correspondiente.
- La entrada devuelta queda `INVALIDADA`.
- La primera entrada permanece `UTILIZADA`.

La liberación del lugar y el reintegro son coordinados por los microservicios correspondientes.

No deben ejecutarse manualmente llamadas internas que ya forman parte del proceso de aprobación.

### 9.3. Reportes

Finalmente, el organizador consulta:

- Reporte general del evento.
- Reporte por sectores.
- Reporte de recaudación.
- Reporte de ocupación.

### Resultado esperado

Los reportes deben responder correctamente y reflejar las operaciones registradas durante la prueba.

Las cantidades y los importes pueden incluir operaciones realizadas anteriormente sobre el mismo evento, si existen.

---

## 10. Carpeta 04 — Fila virtual de alta demanda

**Objetivo:** verificar el ingreso ordenado a una fila virtual y la habilitación de clientes para comprar.

La carpeta contiene nueve solicitudes.

Se utiliza un evento de alta demanda distinto del empleado en el circuito de compra anterior.

### Orden

1. Crear un evento marcado como de alta demanda.
2. Configurar sus precios.
3. Publicarlo.
4. Ingresar al cliente en la fila virtual.
5. Consultar la posición del cliente.
6. Habilitar una tanda desde el rol administrador.
7. Consultar el turno habilitado.
8. Crear una reserva con el turno autorizado.
9. Consultar nuevamente el estado de la fila.

### Resultados esperados

- El cliente puede registrarse en la fila.
- La posición y el estado se consultan correctamente.
- La habilitación de tandas requiere autorización administrativa.
- El cliente habilitado puede avanzar hacia la reserva.
- Reservas comprueba el turno mediante la integración interna con Fila Virtual.

Los turnos habilitados tienen una vigencia limitada.

La reserva debe ejecutarse mientras el turno continúe autorizado.

No es necesario invocar manualmente los endpoints internos de validación o finalización de turnos.

---

## 11. Carpeta 90 — Pruebas opcionales

**Nombre:** `90 - Opcionales: actualización, cancelación y rechazos`

Esta carpeta contiene 11 solicitudes.

No forma parte del recorrido principal y no debe ejecutarse indiscriminadamente.

Permite comprobar escenarios adicionales.

### 11.1. Pago rechazado

Las primeras tres solicitudes:

1. Crean una reserva nueva.
2. Inician una compra asociada.
3. Procesan un pago con el medio `TARJETA_RECHAZADA`.

### Resultado esperado

El pago queda en estado `RECHAZADO`.

La pasarela es simulada, por lo que no se realizan cobros reales.

La liberación de la reserva se gestiona mediante el comportamiento implementado en Compras y Reservas.

### 11.2. Modificación de evento

Se modifica la descripción de un evento previamente publicado.

La modificación puede generar notificaciones para los clientes que hayan comprado entradas.

El envío se procesa mediante el servicio de Notificaciones.

### 11.3. Suspensión y nueva publicación

Se comprueba que:

1. Un administrador puede suspender un evento.
2. Un organizador autorizado puede volver a publicarlo, cuando su estado lo permita.

### 11.4. Cancelación

La carpeta incluye la cancelación del evento y una consulta de las notificaciones asociadas.

**La cancelación debe ejecutarse únicamente cuando hayan terminado las demás pruebas sobre ese evento.**

Es una operación de negocio que puede provocar:

- Cancelación de reservas.
- Invalidación de entradas.
- Reintegros.
- Notificaciones a compradores.

No debe utilizarse un evento cancelado para continuar los escenarios de compra.

### 11.5. Otras operaciones

También se incluyen:

- Actualización de preferencias.
- Revocación de una asignación de personal.
- Consulta administrativa de un usuario por ID.

Estas operaciones permiten comprobar funcionalidades adicionales sin mezclarlas con el circuito principal.

---

## 12. Carpeta 91 — Seguridad y manejo de errores

**Nombre:** `91 - Seguridad y errores (opcionales)`

La carpeta contiene nueve pruebas negativas.

Su objetivo es verificar que el backend rechace las operaciones cuando faltan credenciales, permisos o datos válidos.

Entre los escenarios:

| Prueba                                                   | HTTP esperado      |
| -------------------------------------------------------- | ------------------ |
| Consultar una compra sin JWT                             | `401 Unauthorized` |
| Cliente intenta aprobar una devolución                   | `403 Forbidden`    |
| Cliente intenta administrar roles                        | `403 Forbidden`    |
| Cliente intenta habilitar una fila                       | `403 Forbidden`    |
| Cliente intenta emitir entradas mediante una API interna | `403 Forbidden`    |
| Cliente intenta registrar una notificación interna       | `403 Forbidden`    |
| Personal intenta validar para un evento no asignado      | `403 Forbidden`    |
| Consultar un lugar inexistente                           | `404 Not Found`    |
| Buscar con fechas invertidas                             | `400 Bad Request`  |

**Importante:** los códigos `400`, `401`, `403`, `404` y `409` no representan necesariamente fallos de la implementación.

En estas pruebas son resultados esperados que demuestran la aplicación de reglas de negocio y permisos.

---

## 13. Colección adicional de Usuarios y JWT

El archivo `TicketYa-Usuarios-JWT.postman_collection.json` se conserva como colección complementaria.

Contiene pruebas específicas de:

- Registro de usuarios.
- Validación de datos.
- Inicio de sesión.
- Consulta del perfil.
- JWT inválido o alterado.
- Restricciones de acceso.
- Administración de roles.
- Activación y desactivación de usuarios.
- Operaciones administrativas adicionales.

Esta colección puede ejecutarse de manera independiente.

Antes de hacerlo, verificar sus variables de conexión y autenticación.

Para realizar las pruebas a través del Gateway, utilizar:

```text
baseUrl = http://localhost:8084
```

Las operaciones que requieren credenciales internas de servicio deben ejecutarse únicamente con las credenciales correspondientes y en un entorno de pruebas autorizado.

No se deben colocar secretos internos en archivos que se compartan o publiquen.

---

## 14. Notificaciones por correo

TicketYa utiliza un servicio de Notificaciones que procesa los avisos mediante mecanismos persistentes y reintentos.

Las notificaciones se generan automáticamente durante las operaciones de negocio.

Se contemplan cuatro tipos:

1. Confirmación de compra.
2. Cancelación de evento.
3. Modificación de evento.
4. Publicación de un evento de un artista favorito.

La colección principal no envía arbitrariamente estos avisos mediante endpoints internos.

Los avisos se generan como consecuencia de las operaciones de Compras y Eventos, respetando la autenticación entre servicios.

### Verificación con Mailpit

En el entorno Docker de desarrollo, los correos pueden consultarse en:

http://localhost:8025

Allí se puede comprobar la recepción de los mensajes generados por los procesos de negocio.

Los correos son de prueba y no requieren un proveedor externo real.

---

## 15. Consideraciones sobre las pruebas internas

Algunos endpoints se encuentran reservados para la comunicación entre microservicios.

Entre ellos se encuentran operaciones relacionadas con:

- Confirmación y liberación interna de reservas.
- Emisión interna de entradas.
- Invalidación de entradas por cancelación.
- Liberación de lugares devueltos.
- Registro interno de reintegros.
- Validación y finalización de turnos de fila virtual.
- Registro de avisos por parte de servicios autorizados.

Estas operaciones no deben ejecutarse utilizando el JWT de un cliente o administrador, porque requieren identidades de servicio específicas.

La colección comprueba sus efectos a través de los flujos completos de negocio.

Por ejemplo:

- Confirmar una compra permite comprobar la confirmación de reserva y emisión de entradas.
- Aprobar una devolución permite comprobar la invalidación y el reintegro.
- Reservar en un evento de alta demanda permite comprobar la autorización de la fila virtual.
- Publicar o modificar eventos permite comprobar la generación de avisos.

Esto evita duplicar manualmente operaciones que ya se encuentran coordinadas por el backend.

---

## 16. Recomendaciones de ejecución

Para obtener resultados consistentes:

- Ejecutar las carpetas principales en orden.
- Completar primero la autenticación.
- Utilizar los identificadores almacenados automáticamente.
- No sustituir los IDs por valores fijos.
- Ejecutar el circuito de compra antes de que venza la reserva.
- No cancelar un evento antes de terminar sus pruebas.
- No utilizar una entrada ya invalidada para validar un QR.
- No aprobar y rechazar la misma solicitud de devolución.
- No repetir manualmente los reintegros generados automáticamente.
- No utilizar credenciales de servicio para simular operaciones de clientes.
- No exportar colecciones con contraseñas o tokens reales.
- No ejecutar las carpetas opcionales sin comprobar previamente los estados de los recursos.

Si se repite la inicialización de la carpeta 00, se genera un nuevo conjunto de datos de prueba.

No es necesario reiniciar ni vaciar las bases de datos para cada ejecución.

## 17. Resultados esperados

Al finalizar las carpetas principales, debe haberse comprobado:

| Proceso         | Evidencia esperada                                         |
| --------------- | ---------------------------------------------------------- |
| Autenticación   | Tokens JWT válidos y roles correctos                       |
| Eventos         | Evento creado y publicado                                  |
| Catálogo        | Búsquedas y consultas exitosas                             |
| Preferencias    | Preferencias registradas y consultadas                     |
| Recomendaciones | Consulta de eventos sugeridos                              |
| Reservas        | Reserva temporal creada                                    |
| Compras         | Compra confirmada                                          |
| Pagos           | Pago simulado aprobado                                     |
| Entradas        | Dos entradas emitidas                                      |
| Acceso QR       | Una entrada utilizada y rechazo del segundo uso            |
| Devoluciones    | Solicitud aprobada                                         |
| Reintegros      | Reintegro procesado                                        |
| Reportes        | Consultas de ventas, devoluciones, ocupación y recaudación |
| Fila virtual    | Cliente registrado, habilitado y autorizado para reservar  |

Las carpetas opcionales amplían la cobertura con pagos rechazados, modificaciones, cancelaciones y pruebas negativas de seguridad.

## 18. Conclusión

La colección de integración de la Entrega 2 permite recorrer los principales procesos de negocio de TicketYa desde Postman, utilizando el API Gateway y los mecanismos de autenticación y autorización implementados.

La utilización de variables automáticas evita depender de identificadores fijos y facilita repetir las pruebas sin reiniciar las bases de datos.

Las pruebas de operaciones internas se realizan mediante los flujos de negocio correspondientes, respetando la separación de responsabilidades entre microservicios.

Esta colección complementa las pruebas automatizadas del proyecto, las pruebas de seguridad y la prueba de carga realizada con JMeter.
