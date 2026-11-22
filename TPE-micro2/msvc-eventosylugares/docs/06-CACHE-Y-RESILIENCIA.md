# Caché y resiliencia — Eventos y Lugares

## 1. Caché del catálogo

El microservicio utiliza **Caffeine** para mejorar el rendimiento de las consultas frecuentes al catálogo.

| Consulta | Caché |
|---|---|
| `GET /eventos/publicados` | Listado público. |
| `GET /eventos/{id}` | Detalles de eventos publicados o agotados. |
| `GET /eventos/buscar` | Resultados de búsquedas públicas. |

Las entradas tienen un tiempo de vida predeterminado de **30 segundos**, configurable mediante `CATALOGO_CACHE_TTL_SEGUNDOS`.

La caché se invalida después de confirmar operaciones que modifican eventos, estados o precios.

Las consultas administrativas y los datos de disponibilidad real no se almacenan en esta caché. El encabezado `X-Consulta-Fresca: true` permite obtener el detalle actualizado de un evento sin utilizar el valor almacenado.

Cada instancia del microservicio mantiene su propia caché en memoria.

## 2. Resiliencia en comunicaciones

Las integraciones HTTP utilizan clientes con tiempos de espera configurados, para evitar que una dependencia indisponible bloquee indefinidamente una solicitud.

Las cancelaciones se registran de forma persistente antes de comunicar sus efectos a Reservas, Entradas y Compras.

Un proceso programado revisa las comunicaciones incompletas y las reintenta. Se utiliza un identificador de operación estable para prevenir efectos duplicados.

Los avisos por modificaciones y cancelaciones destinados a compradores también se almacenan para su procesamiento posterior.

## 3. Integración con otros componentes

- **Recomendaciones:** aporta clientes interesados en artistas favoritos y utiliza mecanismos de timeout y reintento para consultar el catálogo.
- **Compras y Reservas:** proporcionan datos de ventas y disponibilidad para los reportes.
- **Notificaciones:** procesa los avisos generados por operaciones relacionadas con los eventos.
- **Observabilidad:** registra las comunicaciones y errores mediante logs estructurados y `correlationId`.

## 4. Verificaciones

Las pruebas comprobaron:

- Reutilización de resultados dentro del tiempo de vida de la caché.
- Renovación de datos después del vencimiento.
- Invalidación posterior a cambios de eventos.
- Persistencia y reintento de notificaciones.
- Funcionamiento del microservicio dentro del entorno Docker Compose.
- Integración con los componentes de observabilidad.

Los resultados fueron satisfactorios.