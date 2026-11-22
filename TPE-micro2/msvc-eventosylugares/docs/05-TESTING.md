# Pruebas — Eventos y Lugares

## 1. Objetivo

Se realizaron pruebas unitarias, de integración, seguridad y funcionamiento HTTP para comprobar las reglas de negocio y la comunicación con los demás microservicios.

Se utilizaron JUnit 5, Mockito, Spring Boot Test, MockMvc y Postman.

Las pruebas automatizadas de integración utilizan H2 en memoria, mientras que la ejecución integrada del proyecto utiliza MySQL.

## 2. Pruebas automatizadas

En la última ejecución completa de Maven, el microservicio Eventos y Lugares registró **177 pruebas satisfactorias**, sin fallos ni errores.

Las principales clases verificadas fueron:

| Pruebas | Cobertura |
|---|---|
| `EventoServiceTest` | Publicación, estados, precios y búsquedas. |
| `EventoApiIntegrationTest` | Endpoints y persistencia de eventos. |
| `OrganizadorUsuarioIntegrationTest` | Vinculación con Usuarios, roles y respuestas de error. |
| `ServiceSecurityIntegrationTest` | Autenticación, autorización y propiedad de eventos. |
| `AsientoServiceTest` | Capacidad, duplicados y sectores numerados. |
| `SectorServiceTest` | Creación y restricciones de sectores. |
| `NotificadorCancelacionesTest` | Comunicaciones, errores e idempotencia. |
| `AvisoArtistasFavoritosServiceImplTest` | Avisos a clientes interesados y prevención de duplicados. |
| `CatalogoCacheServiceTest` | Lecturas, vencimiento e invalidación de caché. |
| `InternalCredentialsTest` | Credenciales y autenticación entre servicios. |

Ejecución desde la raíz del proyecto:

```powershell
mvn -B test
```

## 3. Pruebas de integración funcional

Se verificaron los siguientes escenarios:

| Funcionalidad | Resultado |
|---|---|
| Crear lugares, sectores, asientos y participantes | Satisfactorio |
| Crear y publicar eventos | Satisfactorio |
| Buscar eventos con filtros | Satisfactorio |
| Suspender, agotar, reabrir y cancelar eventos | Satisfactorio |
| Consultar precios y habilitación para reservar | Satisfactorio |
| Vincular organizadores con usuarios autorizados | Satisfactorio |
| Propagar cancelaciones a Reservas, Entradas y Compras | Satisfactorio |
| Generar avisos por artistas favoritos | Satisfactorio |
| Notificar modificaciones de eventos a compradores | Satisfactorio |
| Consultar reportes de ventas, recaudación y ocupación | Satisfactorio |
| Registrar eventos estructurados en Grafana | Satisfactorio |

Se comprobó, entre otros casos, la cancelación del evento 3 y la modificación del evento 4. Las operaciones generaron los registros y notificaciones correspondientes.

## 4. Seguridad y casos negativos

Se verificaron:

- Rechazo de solicitudes sin JWT válido.
- Restricción de operaciones administrativas.
- Imposibilidad de que un organizador modifique eventos ajenos.
- Protección de los eventos no publicados.
- Rechazo de publicaciones con configuración incompleta.
- Validación de coordenadas y rangos de fechas.
- Rechazo de recursos inexistentes y operaciones incompatibles con el estado del evento.

Los códigos `400`, `401`, `403`, `404` y `409` se utilizaron para identificar las situaciones correspondientes.

## 5. Resultado

Las pruebas automatizadas y funcionales realizadas confirmaron el funcionamiento de la gestión de eventos, sus reglas de negocio, la seguridad y las principales integraciones con TicketYa.