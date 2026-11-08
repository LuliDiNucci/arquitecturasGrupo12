# 07 — Verificación del proyecto contra la consigna oficial

## 1. Cobertura del microservicio Eventos y Lugares

| Requisito | Código/entregable | Resultado de auditoría |
| Evento con ID, nombre, descripción, tipo, fecha, lugar, organizador y participantes | `model/Evento`, `EventoParticipante`, DTOs | Implementado |
| Lugar con coordenadas, ciudad, dirección y sectores | `Lugar`, `Sector` y controladores | Implementado |
| Sectores numerados y no numerados | `Sector`, `AsientoService` | Implementado |
| Filas y asientos identificados | `Asiento` y unicidad (sector, fila, número) | Implementado |
| Precio por sector y por evento | `PrecioSector` (único evento + sector) | Implementado |
| Crear eventos en BORRADOR y publicar con configuración completa | `EventoService` | Implementado |
| Modificar, suspender y cancelar eventos | `EventoController`, `EventoService` | Implementado |
| Avisar a Reservas, Entradas y Compras por cancelación | Clientes Feign, `NotificadorCancelaciones` y registro de notificaciones pendientes | Implementado; depende de la disponibilidad de los microservicios receptores |
| Búsqueda por tipo, organizador, participante, ciudad y fechas | `EventoService.buscar` | Implementado |
| Ordenar por fecha, precio o relevancia | `EventoService.resumir` | Implementado según la revisión del código |
| Endpoint de ofertas para Reservas/Compras | `GET /eventos/{id}/sectores` | Implementado |
| Consultar si se permiten reservas | `GET /eventos/{id}/habilitado` | Implementado |
| Base relacional propia | `application.yml`, configuración de base de datos | Configurada |
| Proyecto Spring Boot ejecutable independiente | `pom.xml`, `application.yml`, clases principales | Código y configuración disponibles; ejecución integral pendiente de verificación |
| Swagger/OpenAPI | `springdoc`, controladores anotados y DTOs con ejemplos | Configurado; verificar la interfaz al levantar la aplicación |
| Pruebas unitarias | `src/test/java/.../service` | Escritas; ejecución pendiente de verificar |
| Pruebas de API del servicio | `src/test/java/.../integration` | Escritas; ejecución pendiente de verificar |
| Colección Postman | `postman/*.json` | Disponible; ejecución manual pendiente |
| Documentación ER, arquitectura, historias de usuario, contratos y testing | `docs/*.md` | Documentada |
| README e instrucciones | `README.md` | Documentado |
| Datos de prueba | Script local y colección Postman | Disponibles |

## 2. Integraciones con otros microservicios

La cancelación de eventos utiliza los siguientes endpoints receptores:

| Microservicio | Método y ruta | Función |
| Reservas | `POST /reservas/evento/{id}/cancelar` | Cancelar las reservas del evento |
| Entradas | `POST /entradas/evento/{id}/invalidar` | Invalidar las entradas emitidas |
| Compras | `POST /compras/evento/{id}/cancelar` | Procesar los reintegros correspondientes |

Estos endpoints están implementados en sus respectivos microservicios.

Eventos registra las notificaciones pendientes y las procesa mediante `NotificadorCancelaciones`, con reintentos programados. La configuración predeterminada habilita las integraciones globales, de Entradas y de Compras:

- `INTEGRACIONES_HABILITADAS=true`
- `ENTRADAS_HABILITADAS=true`
- `COMPRAS_HABILITADAS=true`

Los valores pueden modificarse mediante variables de entorno.

La existencia de los contratos y de los mecanismos de reintento no implica que se haya certificado el funcionamiento integral de todos los servicios en ejecución simultánea.

## 3. Aspectos que dependen del resto del sistema

Los siguientes aspectos involucran responsabilidades compartidas entre microservicios y deben evaluarse sobre el sistema completo:

- **Concurrencia y prevención de doble venta:** intervienen principalmente Reservas y Compras, además de la coordinación con Entradas.
- **Reintegros por cancelación:** el procesamiento corresponde a Compras y utiliza la invalidación de entradas.
- **Gateway de pagos:** depende de la implementación y configuración correspondiente en Compras.
- **Historias de usuario del sistema completo:** deben contrastarse con el documento grupal y las responsabilidades de cada microservicio.

La existencia de implementaciones en los distintos repositorios no reemplaza las pruebas de integración entre ellos.

## 4. Cambios y ajustes identificados durante el desarrollo

Durante el desarrollo y las revisiones se realizaron, entre otros, los siguientes trabajos:

1. Se normalizaron los DTO para utilizar clases Java tradicionales, con constructores, getters y setters.
2. `ApiErrorResponseDTO` se implementó como clase tradicional.
3. Se incorporó el seguimiento de avisos de cancelación para Compras, además de Reservas y Entradas.
4. Se incorporó la configuración de las direcciones de los microservicios mediante variables de entorno.
5. Se desarrollaron pruebas automáticas, documentación Mermaid, contratos REST, colección Postman y datos de prueba.
6. Se actualizaron los diagramas y contratos para reflejar el flujo de cancelación implementado y los estados reales de las compras.

## 5. Estado de verificación y limitaciones

La revisión del código permite contrastar la implementación con los requisitos de la consigna y verificar la existencia de las principales funcionalidades y contratos.

Sin embargo, no deben considerarse superadas pruebas que no hayan sido ejecutadas y respaldadas con resultados.

Quedan sujetas a ejecución y verificación en el entorno correspondiente:

- Compilación y ejecución de las pruebas Maven.
- Arranque de cada microservicio y conexión con su base de datos.
- Visualización y prueba de Swagger/OpenAPI.
- Pruebas de integración entre Eventos, Reservas, Compras y Entradas.
- Pruebas de concurrencia y prevención de doble venta.
- Pruebas de recuperación ante fallos de comunicación entre servicios.
- Verificación del flujo completo de pagos y reintegros.

## 6. Conclusión

El microservicio Eventos y Lugares cuenta con una implementación y documentación que cubren los principales requisitos asignados en la consigna.

La revisión permitió corregir referencias desactualizadas sobre los contratos de cancelación, las integraciones y la configuración predeterminada.

**Estado de auditoría:** revisión estática del código y de la documentación. La certificación del funcionamiento completo requiere ejecutar las pruebas correspondientes y verificar el sistema integrado.