# 05 - Testing

## 1. Objetivo

El objetivo de las pruebas del microservicio `msvc-recomendaciones` es verificar el funcionamiento de la gestión de preferencias de los clientes y la generación de recomendaciones a partir de los eventos publicados en TicketYa.

## 2. Entorno de pruebas

| Componente | Configuración |
|---|---|
| Microservicio | msvc-recomendaciones |
| Puerto | 8086 |
| Base de datos | MongoDB |
| Base de datos utilizada | ticketya_recomendaciones |
| Microservicio externo | msvc-eventosylugares |
| Puerto del servicio externo | 8081 |
| Herramienta de pruebas | Swagger UI |

## 3. Pruebas realizadas

### 3.1. Verificación de disponibilidad de MongoDB

Se verificó que el contenedor de MongoDB estuviera iniciado y que aceptara conexiones.

**Resultado:** se observó que MongoDB estaba disponible y aceptaba conexiones.

### 3.2. Consulta de eventos publicados

Se intentó consultar el endpoint:

`GET http://localhost:8081/eventos/publicados`

**Resultado:** el servicio respondió correctamente, pero devolvió una lista vacía (`[]`).

Esto impidió disponer de eventos publicados para comprobar la generación de recomendaciones.

### 3.3. Consulta de recomendaciones

Se identificó el endpoint:

`GET http://localhost:8086/api/recomendaciones/{clienteId}`

La prueba de integración completa quedó pendiente porque no se contaba con eventos publicados para utilizar como datos de entrada.

## 4. Pruebas pendientes

| Prueba | Objetivo | Estado |
|---|---|---|
| Registro de preferencias | Verificar que las preferencias se almacenen en MongoDB | Pendiente de documentar resultado |
| Consulta de preferencias | Verificar la recuperación de las preferencias de un cliente | Pendiente de documentar resultado |
| Consulta de eventos publicados | Obtener eventos desde Eventos y Lugares | Ejecutada: respuesta vacía |
| Generación de recomendaciones | Verificar que se devuelvan eventos relacionados con las preferencias | Pendiente |
| Integración completa | Comprobar el recorrido entre preferencias, Feign y recomendaciones | Pendiente |

## 5. Conclusión

El microservicio cuenta con la implementación de la gestión de preferencias y la integración con el microservicio de Eventos y Lugares.

Durante las pruebas se pudo consultar el endpoint de eventos publicados, que respondió con una lista vacía.

Por este motivo, no fue posible verificar el resultado de la generación de recomendaciones utilizando eventos reales.

Queda pendiente realizar una prueba integral con eventos publicados y preferencias registradas para comprobar que las recomendaciones devueltas correspondan a los intereses de cada cliente.
## Pruebas de preferencias

Se realizaron pruebas funcionales desde Swagger UI sobre el microservicio `msvc-recomendaciones`.

**1. Registrar preferencias**

- Endpoint: `POST /api/preferencias/{clienteId}`
- Cliente utilizado: `4`
- Resultado: `201 Created`.
- Se registraron correctamente los tipos de eventos, artistas favoritos y ciudad.

**2. Consultar preferencias**

- Endpoint: `GET /api/preferencias/{clienteId}`
- Cliente utilizado: `4`
- Resultado: `200 OK`.
- Se recuperaron correctamente las preferencias registradas.

**3. Modificar preferencias**

- Endpoint: `PUT /api/preferencias/{clienteId}`
- Cliente utilizado: `4`
- Resultado: `200 OK`.
- Se modificaron los datos de preferencias.

**4. Verificar modificación**

- Se realizó nuevamente una consulta mediante `GET /api/preferencias/4`.
- Resultado: `200 OK`.
- La respuesta reflejó los valores modificados.

**Conclusión:** Se verificó el funcionamiento de los endpoints de registro, consulta y modificación de preferencias desde Swagger UI. Los resultados confirman que los cambios persisten entre operaciones de la API.
