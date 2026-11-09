# Pruebas de integración con Postman – TicketYa

## Requisitos previos

Antes de ejecutar las pruebas:

1. Tener MySQL iniciado y las bases de datos disponibles.
2. Levantar los cuatro microservicios: Eventos y Lugares, Reservas, Compras y Pagos, y Entradas.
3. Levantar el API Gateway en `http://localhost:8084`.
4. Importar `TicketYa-Integracion.postman_collection.json` en Postman.
5. Ejecutar las pruebas preferentemente con las bases de datos vacías.

La colección utiliza variables para almacenar automáticamente los identificadores de los recursos creados (`eventoId`, `reservaId`, `compraId`, `entradaId`, etc.).

**Importante:** las peticiones deben ejecutarse individualmente en el orden indicado. Las reservas temporales vencen después de 10 minutos, por lo que las operaciones de compra y pago deben realizarse mientras estén vigentes.

## Orden de ejecución

### 1. Eventos y Lugares – Carga inicial

**Carpeta 01 - Carga y publicación**

Ejecutar todas las peticiones, de la **01 a la 15**, en el orden en que aparecen.

Se crean el organizador, participante, lugar, sectores, asientos y evento. También se configuran los precios y se publica el evento.

Resultado esperado: evento en estado `PUBLICADO`.

### 2. Consultas y actualizaciones

**Carpeta 02 - Consultas y actualizaciones**

Ejecutar todas las peticiones, de la **16 a la 24**, en orden.

Se comprueban las búsquedas, los filtros, las consultas de sectores y la modificación de un evento publicado.

### 3. Ciclo de estados del evento

**Carpeta 03 - Ciclo de estados sin cancelar**

Ejecutar todas las peticiones, de la **25 a la 29**, en orden:

1. Suspender evento.
2. Comprobar que no está habilitado.
3. Republicar evento.
4. Marcar evento agotado.
5. Reabrir evento.

Resultado esperado: el evento vuelve a estar `PUBLICADO`.

**No ejecutar todavía la carpeta 04 - Cancelación.**

### 4. Reservas – Crear una reserva para comprar

**Carpeta 06 - Reservas**

Ejecutar:

1. `Habilitar evento (interno)`.
2. `Disponibilidad - Platea numerada`.
3. `Crear reserva`.
4. `Consultar reserva`.

Resultado esperado: reserva en estado `ACTIVA`.

No ejecutar todavía `Confirmar (interno)` ni `Liberar temporal (interno)`, porque esta reserva será utilizada por Compras y Pagos.

### 5. Compras y Pagos – Pago aprobado

**Carpeta 07 - Compras y Pagos**

Ejecutar:

1. `01 - Iniciar compra`.
2. `02 - Consultar compra`.
3. `03 - Procesar pago aprobado`.
4. `04 - Consultar confirmacion`.
5. `05 - Confirmar compra y emitir entradas`.

**Omitir por ahora `01b - Iniciar compra para pago rechazado`.**

Resultados esperados: pago `APROBADO`, compra `CONFIRMADA` y entrada emitida.

La confirmación de compra permite guardar automáticamente el identificador de la entrada generada.

### 6. Entradas – Consultar entradas emitidas

**Carpeta 09 - Entradas**

Ejecutar:

1. `02 - Consultar entrada`.
2. `03 - Consultar entradas de un cliente`.

Resultado esperado: entrada en estado `EMITIDA`, con su código QR.

No ejecutar `01 - Emitir entradas`, porque el proceso de confirmación de compra ya realizó la emisión.

No ejecutar todavía `04 - Invalidar entradas de un evento`.

### 7. Devoluciones – Aprobar una devolución

**Carpeta 10 - Devoluciones**

Ejecutar:

1. `01 - Solicitar devolucion`.
2. `02 - Consultar devoluciones pendientes`.
3. `03 - Aprobar devolucion`.

Resultado esperado: solicitud en estado `APROBADA`.

No ejecutar `04 - Rechazar devolucion` sobre la misma solicitud, porque ya fue aprobada.

### 8. Reintegros y liberación de disponibilidad

**Carpeta 08 - Reintegros**

Ejecutar:

1. `09 - Consultar reintegros de compra`.

Resultado esperado: reintegro asociado a la entrada devuelta, con estado `APROBADO`.

Después verificar:

- **Carpeta 09:** `02 - Consultar entrada`. Su estado debe ser `INVALIDADA`.
- **Carpeta 06:** `Disponibilidad - Campo (comprobar liberacion tras devolucion)`.

Resultado esperado: la disponibilidad de Campo vuelve a 500 lugares, siempre que no existan otras reservas ocupando capacidad.

No ejecutar `08 - Registrar reintegro de entrada` sobre una entrada ya reintegrada.

### 9. Reservas – Liberación temporal

**Carpeta 06 - Reservas**

Ejecutar:

1. `Crear reserva` nuevamente para obtener una reserva nueva en estado `ACTIVA`.
2. `Liberar temporal (interno)`.
3. `Consultar reserva`.
4. `Disponibilidad - Campo (comprobar liberacion tras devolucion)`.

Resultado esperado: reserva `LIBERADA` y disponibilidad restaurada.

### 10. Compras y Pagos – Pago rechazado

Primero ejecutar:

- **Carpeta 06:** `Crear reserva`, para obtener otra reserva `ACTIVA`.

Luego, en **Carpeta 07 - Compras y Pagos**, ejecutar:

1. `01b - Iniciar compra para pago rechazado (con reserva ACTIVA nueva)`.
2. `06 - Procesar pago rechazado`.
3. `07 - Reintentar liberacion de reserva`.

La petición `01b` guarda el identificador en `compraIdRechazada`, sin sobrescribir `compraId` de la compra aprobada.

Resultados esperados: pago `RECHAZADO`, reserva liberada automáticamente y reintento de liberación con respuesta `204 No Content`.

Para verificar el estado de la reserva rechazada, consultar su identificador correspondiente.

### 11. Cancelación del evento

**Carpeta 04 - Cancelación (ejecutar al final)**

Ejecutar todas las peticiones, de la **30 a la 33**, en orden:

1. Cancelar evento.
2. Verificar notificaciones a Reservas, Entradas y Compras.
3. Cancelar nuevamente para comprobar idempotencia.
4. Comprobar que el evento cancelado no aparece en las búsquedas.

Resultado esperado: evento `CANCELADO`, notificaciones procesadas y ausencia del evento en búsquedas públicas.

### 12. Validación de errores

**Carpeta 05 - Errores**

Ejecutar todas las peticiones, de la **34 a la 37**, en orden.

| Prueba | Resultado esperado |
|---|---|
| ID inexistente | 404 Not Found |
| Coordenadas inválidas | 400 Bad Request |
| Fechas invertidas | 400 Bad Request |
| Publicar evento cancelado | 409 Conflict |

Estos códigos HTTP representan resultados correctos de las pruebas negativas.

## Pruebas internas y alternativas

La colección también incluye operaciones de confirmación manual de reservas, devolución interna de entradas, invalidación por evento, compensaciones y rechazo de solicitudes de devolución.

Estas operaciones **no deben ejecutarse indiscriminadamente**, porque dependen de estados específicos o ya son realizadas automáticamente durante los flujos principales.

Para probar el rechazo de una devolución se necesita generar otra entrada válida, crear una nueva solicitud `PENDIENTE` y ejecutar `04 - Rechazar devolucion (alternativa)` antes de aprobarla.

## Consideraciones finales

- Ejecutar las pruebas en el orden indicado.
- No cancelar el evento antes de terminar las operaciones de reservas, compras y devoluciones.
- No confirmar y liberar manualmente la misma reserva.
- No aprobar y rechazar la misma solicitud de devolución.
- No registrar dos reintegros para la misma entrada.
- No repetir las operaciones internas que ya hayan sido ejecutadas automáticamente.
- Utilizar las variables de Postman para evitar depender de identificadores fijos.
- Si una reserva vence, crear una nueva antes de intentar comprar o pagar.

