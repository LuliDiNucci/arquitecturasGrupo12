# 06 — Decisiones y limitaciones de `msvc-reservas`

**Proyecto:** TicketYa · Entrega 1  
**Propósito:** explicar por qué se implementó Reservas de esta manera, qué garantiza su código y qué responsabilidades permanecen fuera del servicio.

## 1. Responsabilidad y límites del microservicio

`msvc-reservas` es dueño de la **ocupación temporal y confirmada** de los lugares de un evento. Gestiona las reservas y sus estados, sus detalles, las asignaciones de asientos, la liberación individual y las tablas locales utilizadas para concurrencia e idempotencia.

El catálogo de eventos, lugares, sectores, asientos y precios pertenece a Eventos y Lugares; los pagos/reintegros pertenecen a Compras y Pagos; la emisión y validación de entradas pertenece a Entradas. Reservas guarda sus **identificadores** (`eventoId`, `sectorId`, `asientoId`, `clienteId`), no entidades JPA ni claves foráneas entre bases de microservicios.

**Decisión:** cada microservicio conserva su propia base de datos. **Consecuencia:** hay independencia de persistencia, pero las operaciones de varios servicios no forman una transacción distribuida automática.

## 2. Reservas temporales de 10 minutos

Una reserva nueva comienza en estado `ACTIVA` y se crea con fecha de vencimiento diez minutos posterior a la creación. La ocupación toma en cuenta las reservas `CONFIRMADA` y las `ACTIVA` cuyo plazo todavía no venció.

**Decisión:** usar **vencimiento lógico** al consultar ocupación y actualizar estados cuando una operación accede a una reserva vencida, **sin scheduler**.

- **Ventaja:** una reserva vencida deja de ocupar lugares aun si su estado persistido todavía no cambió.
- **Limitación:** el registro en MySQL puede seguir temporalmente en estado `ACTIVA` hasta una lectura u operación que procese su vencimiento. No se ofrece liberación periódica automática del estado.

## 3. Sectores numerados y no numerados

- **Numerados:** el cliente indica `asientoIds`; Reservas comprueba cantidad, ausencia de duplicados, existencia y pertenencia al sector. Se registran asignaciones individuales y se excluyen de disponibilidad los asientos aún ocupados.
- **No numerados:** no se reciben asientos individuales. Se consulta la capacidad y se descuenta la cantidad ocupada por reservas vigentes, considerando liberaciones individuales.

**Decisión:** usar un mismo `DetalleReserva` con `sectorId`, `cantidad` y `cantidadLiberada`, y `AsignacionAsiento` solo cuando corresponde. Reservas no inventa asientos ni altera la capacidad del sector en la base de Eventos.

## 4. Concurrencia: bloqueo pesimista local

**Problema:** dos solicitudes simultáneas podrían leer la misma disponibilidad y ocupar el último lugar.

**Decisión:** transacciones locales de Spring/JPA (`READ_COMMITTED`) y un bloqueo `PESSIMISTIC_WRITE` sobre la fila del evento en `bloqueo_evento` antes de comprobar disponibilidad y guardar una reserva. Las operaciones de confirmación, liberación, devolución y cancelación también utilizan este bloqueo.

**Condición operativa:** la fila de `bloqueo_evento` debe crearse **antes de iniciar ventas**, por medio de `POST /reservas/evento/{eventoId}/habilitar`. No se crea dentro de cada reserva concurrente. Habilitar valida que el evento exista y esté `PUBLICADO`; un evento cancelado no puede volver a habilitarse en esta implementación.

**Validación registrada:** prueba de diez solicitudes paralelas del mismo asiento con MySQL: exactamente una creación exitosa.

**Límite:** el bloqueo serializa operaciones **dentro de la base de Reservas**. No bloquea directamente las tablas de Eventos, Compras ni Entradas; tampoco garantiza por sí solo ausencia de interbloqueos o fallos en cualquier patrón de carga distribuida.

## 5. Idempotencia en creación de reservas

**Problema:** un reintento por timeout podría crear dos reservas o descontar disponibilidad dos veces.

**Decisión:** `claveIdempotencia` obligatoria, unicidad de la clave guardada en `reserva` y una fila por clave en `clave_idempotencia_bloqueo`, que se bloquea **antes** de la fila del evento.

- Misma clave **y mismo contenido**: se recupera la reserva existente.
- Misma clave con **datos diferentes**: se rechaza la solicitud.
- La clave se utiliza globalmente en Reservas: el cliente debe generar una clave nueva para una operación nueva.

**Validación registrada:** reintentos secuenciales y simultáneos devolvieron la misma reserva y no duplicaron filas en MySQL.

**Límite:** toda prueba de idempotencia simultánea tiene un alcance acotado a los escenarios ejecutados; ante otras condiciones de bloqueo o reintentos de red deben mantenerse pruebas de integración y observabilidad.

## 6. Límite de entradas por cliente

El servicio suma las entradas ya ocupadas por el cliente para un mismo evento y las que solicita en la operación actual, y rechaza la creación si excede el máximo.

**Decisión:** límite configurable mediante `reservas.max-entradas-por-cliente`, con valor local `4`.

**Validación registrada:** se rechazaron cinco entradas; cuatro fueron admitidas y una petición adicional del mismo cliente/evento fue rechazada.

**Límite:** `clienteId` es un dato recibido en la solicitud; su autenticidad debe validarla el sistema de identidad/Gateway o un servicio autorizado cuando se integre la seguridad.

## 7. Integración HTTP con Eventos y Lugares

**Decisión:** usar Spring Cloud OpenFeign (`EventoClient`) para consultar:

- Evento: `GET /eventos/{id}`.
- Sector: `GET /sectores/{id}`.
- Asiento: `GET /asientos/{id}`.
- Asientos por sector: `GET /sectores/{id}/asientos`.
- Precio del sector en el evento: `GET /eventos/{eventoId}/sectores/{sectorId}/precio`.

Reservas exige un evento futuro `PUBLICADO` y que el sector corresponda al lugar del evento y tenga precio asignado. Un 404 remoto se traduce a respuesta 404 local; otros errores Feign se traducen a 503 en el manejador global.

**Límite:** las consultas remotas pueden fallar o cambiar entre operaciones; no hay transacción conjunta con la base de Eventos. La compatibilidad de rutas, DTO y datos debe probarse con el servicio real. La URL local de desarrollo es `msvc.eventos.url=http://localhost:8081`.

## 8. Publicación y cancelación de eventos

**Publicación:** Eventos debe notificar a Reservas con `POST /reservas/evento/{eventoId}/habilitar` una vez persistido el estado `PUBLICADO`. Reservas prepara la fila local necesaria para admitir solicitudes concurrentes.

**Cancelación:** Eventos notifica a `POST /reservas/evento/{eventoId}/cancelar`. Reservas marca `cancelado=true` y cancela las reservas temporales activas, impidiendo nuevas ventas y rechazando la consulta de disponibilidad de un evento cancelado (409).

**Decisión:** mantener la marca de cancelación persistente y no permitir reactivación en esta versión.

**Límites:**

- Las reservas ya `CONFIRMADA` no se convierten automáticamente en reintegros: Compras/Entradas deben gestionar la compensación.
- Entre la transacción de cancelación en Eventos y su notificación a Reservas puede existir una ventana de inconsistencia. No hay atomicidad global: hacen falta coordinación, reintentos y pruebas ante fallos.
- El endpoint de habilitación no debe invocarse antes del commit de publicación en Eventos, porque Reservas vuelve a consultar su estado.

## 9. Confirmación, liberación y pagos

- `POST /reservas/{id}/confirmar`: reserva `ACTIVA` no vencida → `CONFIRMADA`; confirmar una reserva ya confirmada es idempotente.
- `POST /reservas/{id}/liberar`: libera reserva temporal; no se usa para liberar una reserva confirmada.
- Un pago que se aprueba después del vencimiento no debe permitir ocupar lugares sin disponibilidad: **Compras debe compensar/revertir** esa operación.

**Límite:** Reservas no cobra, no registra transacciones de pago ni emite entradas/QR. Estas interacciones todavía deben probarse con los servicios respectivos.

## 10. Devoluciones individuales

`POST /reservas/{id}/devolver-entrada` descuenta **una entrada** de una reserva confirmada; libera el asiento cuando el sector es numerado y registra `entradaId` en `entrada_liberada` para evitar una segunda liberación del mismo ID.

**Decisión:** reservar el control de disponibilidad y registrar una devolución aplicada; **no** validar dentro de Reservas condiciones que pertenecen a otro servicio.

**Límites de seguridad/negocio:** el solicitante autorizado debe demostrar la titularidad, identidad de la entrada, aprobación y el cumplimiento del plazo de **72 horas** antes de llamar a Reservas. `entradaId` en la petición, por sí solo, no prueba esas condiciones. Compras gestiona el reintegro correspondiente.

## 11. Persistencia y manejo de errores

- **Base de desarrollo:** MySQL `ticketya_reservas`.
- **JPA:** `spring.jpa.hibernate.ddl-auto=update` se utiliza en desarrollo; no reemplaza migraciones controladas para entornos compartidos o productivos.
- **Errores REST:** `ReservaExceptionHandler` transforma datos inválidos en 400, conflictos de estado/disponibilidad en 409, recursos remotos inexistentes en 404 y fallos remotos en 503. Se devuelven objetos con `estado` y `mensaje`.
- **Pruebas de concurrencia:** se ejecutan sobre `ticketya_reservas_test`, una base separada.

## 12. Swagger, contrato REST y seguridad

La documentación interactiva mediante Springdoc/Swagger UI permite ver endpoints, parámetros y esquemas; el contrato manual también se conserva en `docs/openapi.yaml` y la colección `docs/TicketYa-Reservas.postman_collection.json`.

**Decisión:** señalar como **internos** los endpoints de confirmar, liberar, cancelar, habilitar y devolver entrada. **Limitación:** documentar un endpoint como interno en Swagger **no lo protege**. En el despliegue deben restringirse mediante Gateway/red y controles de autorización pertinentes. La entrega inicial no valida todavía un mecanismo integral de identidad entre servicios.

**Mantenimiento:** el OpenAPI generado en tiempo de ejecución y el YAML manual deben permanecer sincronizados, incluyendo el endpoint de habilitación, el 409 de disponibilidad y los esquemas de error.

## 13. Trabajo pendiente y riesgos conocidos

| Área | Pendiente / riesgo |
|---|---|
| Eventos real | Verificar contratos Feign, fechas, publicación y precios contra respuestas reales |
| Habilitación | Reintentos y confirmación de notificación una vez confirmado el commit de publicación |
| Cancelación | Coordinar entradas confirmadas, reintegros y orden de propagación entre servicios |
| Compras | Probar pago aprobado/rechazado, expiración y pago aprobado tarde con compensación |
| Entradas | Probar emisión/QR, titularidad, aprobación y regla de 72 horas |
| Seguridad | Proteger endpoints internos y evitar su exposición arbitraria mediante Gateway |
| Fiabilidad | Ejecutar pruebas de concurrencia y fallos de red adicionales, y observar errores entre servicios |
| Documentación | Mantener sincronizados Swagger, OpenAPI YAML, README y Postman después de cada cambio |

## 14. Referencias del proyecto

- `docs/02-ARQUITECTURA-Y-DIAGRAMAS.md` — arquitectura y secuencias.
- `docs/03-MODELO-DE-DATOS.md` — modelo de datos y entidades.
- `docs/05-TESTING.md` — casos de prueba y ejecución.
- `docs/contrato-eventos.md` — contrato con Eventos y Lugares.
- `docs/openapi.yaml` — contrato HTTP propuesto/documentado.
- `src/main/java/com/ticketya/reservas/services/` — reglas de negocio.
