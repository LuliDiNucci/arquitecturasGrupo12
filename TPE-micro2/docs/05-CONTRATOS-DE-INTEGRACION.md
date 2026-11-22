# Contratos de integración — TicketYa

## Objetivo

Los microservicios de TicketYa se comunican mediante APIs REST. Cada servicio expone operaciones específicas y utiliza identificadores para relacionar información sin acceder a las bases de datos de otros servicios.

Los contratos completos de cada API se encuentran disponibles en su documentación OpenAPI.

## Integraciones principales

| Origen | Destino | Finalidad |
|---|---|---|
| Gateway | Microservicios | Enrutamiento de solicitudes |
| Reservas | Eventos y Lugares | Consultar eventos, sectores y asientos |
| Reservas | Fila Virtual | Validar y finalizar turnos cuando corresponde |
| Compras | Reservas | Consultar, confirmar y liberar reservas |
| Compras | Eventos y Lugares | Obtener información y precios |
| Compras | Entradas | Solicitar emisión de entradas |
| Entradas | Compras | Validar compras y gestionar reintegros |
| Entradas | Reservas | Consultar reservas y devolver lugares |
| Eventos y Lugares | Reservas, Compras y Entradas | Coordinar cancelaciones |
| Eventos y Lugares | Usuarios | Validar usuarios asociados a organizadores |
| Recomendaciones | Eventos y Compras | Obtener catálogo e historial |
| Eventos y Lugares | Recomendaciones | Consultar clientes interesados |
| Compras y Eventos | Notificaciones | Registrar avisos por correo |

## Seguridad de las comunicaciones

Las solicitudes privadas requieren un JWT válido.

Las llamadas realizadas en nombre de un usuario conservan la identidad necesaria para verificar su autorización. Las operaciones internas protegidas utilizan JWT de servicio con autoridades específicas.

El encabezado `X-Correlation-ID` permite seguir las solicitudes entre los diferentes componentes.

## Manejo de errores

Los servicios utilizan respuestas HTTP para comunicar resultados y errores, incluyendo:

- `400`: solicitud inválida.
- `401`: autenticación requerida o inválida.
- `403`: permisos insuficientes.
- `404`: recurso inexistente.
- `409`: conflicto de negocio.
- `503`: dependencia no disponible, cuando corresponde.

## Contratos OpenAPI

Los controladores REST se documentan mediante OpenAPI. Swagger UI permite consultar rutas, parámetros, cuerpos de solicitud y respuestas.

El API Gateway agrupa la documentación de los servicios en:

`http://localhost:8084/swagger-ui.html`

La especificación individual de cada microservicio se conserva en su carpeta `docs/` cuando corresponde.

Las reglas de negocio y los contratos completos se describen en la documentación específica de cada servicio.