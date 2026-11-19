# 03 — Modelo de Datos

## 1. Persistencia

Recomendaciones utiliza MongoDB, con base de datos `ticketya_recomendaciones` y colección `preferencias`. La entidad `Preferencias` se mapea mediante Spring Data MongoDB.

Se seleccionó MongoDB por la naturaleza documental de las preferencias. Cada cliente posee un conjunto de atributos simples y listas de tipos de eventos y artistas favoritos que se almacenan juntos en un único documento. Esta representación evita tablas de asociación para las listas y permite ampliar los criterios de personalización manteniendo una estructura flexible. La elección se limita a las preferencias; los procesos transaccionales de compras y pagos continúan en sus respectivos microservicios y bases relacionales.

## 2. Documento de preferencias

| Atributo | Tipo Java | Descripción |
|---|---|---|
| `id` | `String` | Identificador del documento (`_id` en MongoDB) |
| `clienteId` | `Long` | Identificador del cliente propietario |
| `tiposEventos` | `List<String>` | Tipos de eventos preferidos |
| `artistasFavoritos` | `List<String>` | Nombres de artistas favoritos |
| `ciudad` | `String` | Ciudad de interés |

Ejemplo de estructura lógica del documento:

```json
{
  "clienteId": 99,
  "tiposEventos": ["RECITAL"],
  "artistasFavoritos": ["Banda de Prueba TicketYa"],
  "ciudad": "Tandil"
}
```

MongoDB también asigna el campo `_id`; el documento persistido incluye metadatos de mapeo de Spring Data.

## 3. Relaciones con otros servicios

- `clienteId` es una referencia lógica al usuario gestionado por `msvc-usuarios`.
- Los eventos recomendados no se guardan en esta colección: se consultan en `msvc-eventosylugares`.
- Las compras confirmadas tampoco se duplican: `msvc-compras` conserva sus tablas relacionales y expone una consulta REST de identificadores de eventos recomendados.

No existen claves foráneas entre la base MongoDB de Recomendaciones y las bases de los demás microservicios.

## 4. Configuración

La conexión se configura mediante la propiedad de Spring Boot 4:

```properties
spring.mongodb.uri=${MONGODB_URI:mongodb://localhost:27017/ticketya_recomendaciones}
```

En Docker Compose, `MONGODB_URI` apunta al nombre de servicio `db-recomendaciones`, no a `localhost`.
