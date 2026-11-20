# Caché y resiliencia

## Catálogo de Eventos

Usamos Caffeine en `msvc-eventosylugares` para guardar temporalmente el catálogo en memoria. Elegimos esta tecnología porque las consultas al catálogo se repiten y pueden reutilizarse durante períodos cortos sin desplegar un servicio adicional.

| Consulta | Qué se guarda | Límite por instancia |
| --- | --- | --- |
| `GET /eventos/publicados` | Listado público de eventos | 1 listado |
| `GET /eventos/{id}` | Detalle por ID de eventos publicados o agotados | 1024 detalles |
| `GET /eventos/buscar` | Resultados por combinación de filtros y orden | 256 búsquedas |

El TTL es de **30 segundos** desde que se guarda cada entrada y se puede ajustar con `CATALOGO_CACHE_TTL_SEGUNDOS`. Cuando vence, la siguiente consulta vuelve a cargar el dato desde la fuente.

El catálogo se invalida después de confirmar la transacción al modificar, publicar, suspender, cancelar, agotar, reabrir o finalizar un evento, y al configurar sus precios. Se invalidan todas las búsquedas porque un mismo evento puede aparecer en resultados con distintos filtros. Una lectura que empezó antes de la invalidación solo puede escribir en la caché anterior.

La disponibilidad de asientos, el cupo libre, las reservas temporales y el estado de los pagos se consultan directamente en su fuente. La búsqueda administrativa queda fuera de la caché. Compras, Reservas, Entradas y Fila Virtual usan `X-Consulta-Fresca: true` para consultar el detalle actualizado de un evento cuando lo necesitan para una operación.

Cada réplica guarda su propia caché. La réplica que confirma el cambio invalida su copia de inmediato. Las demás pueden conservar el dato anterior hasta que venza el TTL de su entrada.

## Resiliencia entre microservicios

- **Timeouts generales.** Los clientes Feign usan habitualmente 3 segundos para la conexión y 5 segundos para la lectura. Los módulos que leen `FEIGN_CONNECT_TIMEOUT_MS` y `FEIGN_READ_TIMEOUT_MS` permiten ajustar esos valores. Algunos clientes tienen límites propios. Los dos clientes `RestClient` de Entradas también tienen configurados 3 segundos de conexión y 5 de lectura.
- **Catálogo de Eventos.** El cliente de Recomendaciones usa 1 segundo de conexión y 1,5 segundos de lectura. Se configuran con `RECOMENDACIONES_EVENTOS_CONNECT_TIMEOUT_MS` y `RECOMENDACIONES_EVENTOS_READ_TIMEOUT_MS`. Sus consultas `GET` pueden reintentarse una vez, con una pausa de 100 ms, ante un fallo de red, un timeout o un `502`, `503` o `504`. Si el fallo persiste, Recomendaciones devuelve `503`. Si Eventos responde `500`, Recomendaciones devuelve `503` en un solo intento.
- **Historial de Compras.** Ante un fallo de red o un error `5xx`, Recomendaciones sigue usando el catálogo y las preferencias del cliente para generar sugerencias.
- **Escrituras y avisos.** La política nueva de reintentos se limita a las lecturas del catálogo. Las operaciones que crean compras, procesan pagos o cambian reservas conservan sus mecanismos existentes. Si Notificaciones está indisponible, el aviso de compra queda pendiente y se vuelve a intentar su envío sin repetir el pago.

## Pruebas realizadas

Se ejecutaron **456 pruebas Maven** en Eventos y Lugares, Recomendaciones, Entradas, Compras, Reservas, Usuarios y Notificaciones. Todas pasaron.

- Se verificó que la primera consulta carga el dato desde la fuente, la segunda reutiliza la caché y una consulta posterior al TTL vuelve a cargarlo.
- Se comprobó que el catálogo refleja una modificación, suspensión o cancelación después de confirmar la transacción.
- Con fallos HTTP simulados, Recomendaciones se recuperó de un `503` transitorio. Si Eventos siguió indisponible o demoró más que el timeout, devolvió `503` tras dos intentos. Si falló el historial de Compras, siguió recomendando por preferencias.
- Se comprobó que el `RestClient` de Entradas corta la espera cuando se supera el timeout de lectura configurado.
- Se simuló una caída de Notificaciones. La compra quedó confirmada, repetir la misma clave de idempotencia devolvió el pago original y, al recuperarse el servicio, se recibió un solo correo en el servidor SMTP de prueba.

Las verificaciones HTTP se hicieron con servicios locales y fallos simulados. El arranque del stack completo con Docker Compose quedó sin verificar.