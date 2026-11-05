# Verificación del proyecto contra la consigna oficial

**Base de comparación:** *TPE Arquitectura Web — TicketYa (2026)*, PDF 5 páginas, y las capturas con la propuesta de división de trabajo compartidas para el grupo.

## Cobertura del microservicio Eventos y Lugares

| Requisito | Código/entregable | Resultado de auditoría |
|---|---|---|
| Evento con ID, nombre, descripción, tipo, fecha, lugar, organizador y participantes | `model/Evento`, `EventoParticipante`, DTOs | Implementado |
| Lugar con coordenadas, ciudad, dirección, sectores | `Lugar`, `Sector` y controladores | Implementado |
| Sectores numerados/no numerados | `Sector`, `AsientoService` | Implementado |
| Filas y asientos identificados | `Asiento` y unicidad (sector,fila,numero) | Implementado |
| Precio por sector y por evento | `PrecioSector` (único evento+sector) | Implementado |
| Crear BORRADOR y publicar con configuración completa | `EventoService` | Implementado |
| Modificar, suspender, cancelar | `EventoController`, `EventoService` | Implementado |
| Avisar a Reservas/Entradas/Compras por cancelación | `feign/*FeignClient`, `NotificadorCancelaciones`, outbox | Implementado como contrato propuesto; depende de receptores |
| Búsqueda por tipo, organizador, participante, ciudad y fechas | `EventoService.buscar` | Implementado |
| Ordenar por fecha, precio o relevancia | `EventoService.resumir` | Implementado |
| Endpoint de ofertas para Reservas/Compras | `GET /eventos/{id}/sectores` | Implementado |
| Consulta si se permiten reservas | `GET /eventos/{id}/habilitado` | Implementado |
| Base relacional propia | `application.yml`, `application-mysql.yml` | Implementado |
| Proyecto Spring Boot ejecutable independiente | `pom.xml`, `application.yml`, clases | Código disponible; arranque real pendiente de ejecución Maven |
| Swagger/OpenAPI | `springdoc`, controllers anotados, DTOs con ejemplos | Configurado; verificar UI al levantar |
| Pruebas unitarias | `src/test/java/.../service` | Escritas; pendiente ejecutar Maven |
| Pruebas de API del servicio | `src/test/java/.../integration` | Escritas; pendiente ejecutar Maven |
| Colección Postman | `postman/*.json` | Generada; ejecutar en Postman |
| Documentación ER, arquitectura, US, contratos y testing | `docs/*.md` | Generada |
| README e instrucciones | `README.md` | Generado |
| Datos de prueba | script local + colección Postman | Generados |

## Dependencias que no pueden verificarse solamente con este ZIP

- **Concurrencia y no doble venta**: base y algoritmos de Reservas; compra confirmada en Compras y Entradas.
- **Reintegros reales de cancelaciones**: código de Compras (y flujo de devoluciones de Entradas).
- **Gateway** y diseño del sistema completo: código/configuración del grupo.
- **User stories de todos los microservicios**: documento grupal aparte, aunque esta entrega incluye las de Eventos.

## Cambios realizados durante la auditoría

1. Quitadas las funciones de acceso heredadas del estilo `record` dentro de DTOs (quedan constructores, getters y setters clásicos).
2. `ApiErrorResponseDTO` como clase tradicional, sin `record`.
3. Se agregó seguimiento de avisos también para Compras y la URL `COMPRAS_URL` (no requerida para arrancar).
4. Se configuraron timeouts de conexion/lectura para Feign y el envio de cancelaciones queda deshabilitado por defecto.
5. Se crearon pruebas automáticas, documentación Mermaid, contratos, colección Postman y datos de prueba.

**Estado de verificación:** se puede hacer análisis de código fuente y validación de sintaxis Java, pero la ejecución de Maven, el arranque completo y el flujo entre microservicios requieren el entorno real y los otros servicios. No se deben afirmar como pruebas superadas sin sus resultados.
