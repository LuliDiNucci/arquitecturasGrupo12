# 05 — Testing

## 1. Entorno

Las comprobaciones de `msvc-recomendaciones` se realizaron con Spring Boot 4.1.2, MongoDB en Docker Compose, Eventos y Lugares, Compras y Pagos y API Gateway. Se utilizaron solicitudes REST desde PowerShell, consultas con `mongosh` y Swagger UI.

## 2. Pruebas funcionales e integración verificadas

| Caso | Acción | Resultado observado |
|---|---|---|
| Registro | POST `/api/preferencias/99` | Preferencias registradas |
| Consulta | GET `/api/preferencias/99` | Devuelve tipos, artistas y ciudad |
| Modificación | PUT `/api/preferencias/99` y GET posterior | Refleja los valores actualizados |
| MongoDB | Consultar colección `preferencias` en `ticketya_recomendaciones` | Documento del cliente 99 persistido |
| Integración con Eventos | GET `/eventos/publicados` | Evento 1 PUBLICADO recuperado |
| Tipo y ciudad | Preferencias `RECITAL` / `Tandil` | Recomienda el recital publicado |
| Artista | Solo coincide `Banda de Prueba TicketYa` | Recomienda el recital por participante |
| Sin coincidencias | `TEATRO` / `Cordoba` sin artistas | HTTP `200`, cuerpo `[]` |
| Sin preferencias | GET de recomendaciones para cliente inexistente | HTTP `404` |
| JWT ausente | GET de recomendaciones sin Bearer | HTTP `401` |
| Cliente ajeno | Cliente 3 consulta recomendaciones del 99 | HTTP `403` |
| Cliente propietario | Cliente 3 consulta sus preferencias | HTTP `200` |
| Historial en Compras | GET `/compras/cliente/3/eventos-recomendados` | HTTP `200`, cuerpo `[]` |
| Recomendaciones + Compras | GET mediante Gateway para cliente 3 | HTTP `200` con recital recomendado por preferencias |
| Compras confirmadas para historial | Cliente 6 compra evento 2; cliente 7 compra eventos 2 y 3 | Tres compras confirmadas correctamente |
| Historial colaborativo real | GET `/compras/cliente/6/eventos-recomendados` | Incluye el evento 3 |
| Recomendación por historial real | GET `/api/recomendaciones/6` | Incluye el evento 3 aunque no coincide con las preferencias de tipo, artista o ciudad |
| Unicidad de recomendaciones | Agrupar los resultados por ID de evento | Ningún evento duplicado |
| Despliegue | Recomendaciones en Docker Compose y `/actuator/health` | Contenedor activo, `UP` |

## 3. Testing automatizado

El proyecto incluye `RecomendacionesServiceTest`, implementado con JUnit y Mockito. Se ejecutó `mvn clean test` con resultado `BUILD SUCCESS`.

- `recomiendaEventoPorHistorialAunqueNoCoincidaConPreferencias`: Compras devuelve un evento sugerido que no coincide con las preferencias explícitas; el servicio lo incluye en la respuesta.
- `noRecomiendaEventosVencidosNiNoPublicados`: un evento publicado con fecha pasada y otro evento futuro suspendido se excluyen de las recomendaciones, incluso cuando coinciden con preferencias e historial.

Estas pruebas aíslan la lógica de selección mediante dobles de prueba de repositorio y clientes Feign.

## 4. Regla de recomendación colaborativa

La consulta de historial de Compras devuelve identificadores de eventos a partir de compras confirmadas. Por ejemplo, si un cliente compró entradas para el evento A y otro compró entradas para A y B, el evento B es candidato para el primero, siempre que no lo haya comprado y que B continúe publicado y sea futuro.

La selección por historial se combina con las coincidencias por preferencias; el resultado elimina duplicados y conserva las restricciones de publicación y fecha.
