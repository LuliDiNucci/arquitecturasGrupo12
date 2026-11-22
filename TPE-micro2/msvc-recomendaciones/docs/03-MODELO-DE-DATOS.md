# Modelo de datos — Recomendaciones

## Persistencia

El microservicio utiliza MongoDB con la base de datos `ticketya_recomendaciones` y la colección `preferencias`.

Se eligió MongoDB para el microservicio de Recomendaciones porque las preferencias de cada cliente se representan naturalmente mediante un documento que contiene sus tipos de eventos preferidos, artistas favoritos y ciudad.

El modelo documental permite almacenar listas de intereses dentro del mismo registro, sin necesidad de crear tablas intermedias ni relaciones adicionales. Además, ofrece flexibilidad para incorporar nuevos criterios de personalización.

MongoDB se utiliza exclusivamente para las preferencias. Las compras, reservas y pagos permanecen en bases de datos relacionales, donde se requieren transacciones y controles estrictos de consistencia.

## Documento Preferencias

| Campo | Tipo | Descripción |
|---|---|---|
| `_id` | Identificador de MongoDB | Identificador del documento |
| `clienteId` | `Long` | Identificador del cliente |
| `tiposEventos` | Lista de textos | Tipos de eventos preferidos |
| `artistasFavoritos` | Lista de textos | Nombres de artistas favoritos |
| `ciudad` | `String` | Ciudad de interés |

Ejemplo de documento:

```json
{
  "clienteId": 15,
  "tiposEventos": ["RECITAL", "FESTIVAL"],
  "artistasFavoritos": ["Banda Ejemplo"],
  "ciudad": "Tandil"
}
```

## Reglas de persistencia

- La aplicación consulta si ya existen preferencias para el cliente antes de registrar un documento nuevo.
- Las modificaciones actualizan el documento existente.
- La búsqueda se realiza mediante `clienteId`.
- Los eventos recomendados y el historial de compras no se almacenan en esta base de datos.

`clienteId` es una referencia lógica a un usuario de TicketYa, no una clave foránea de MongoDB. Los datos de eventos y compras permanecen en sus microservicios correspondientes.