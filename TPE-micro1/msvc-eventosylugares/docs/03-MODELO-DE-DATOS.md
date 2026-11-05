# Modelo de datos — msvc-eventos

## Diagrama ER (Mermaid)

```mermaid
erDiagram
  LUGAR ||--o{ SECTOR : contiene
  SECTOR ||--o{ ASIENTO : identifica
  ORGANIZADOR ||--o{ EVENTO : organiza
  LUGAR ||--o{ EVENTO : aloja
  EVENTO ||--o{ PRECIO_SECTOR : define
  SECTOR ||--o{ PRECIO_SECTOR : recibe
  EVENTO ||--o{ EVENTO_PARTICIPANTE : incluye
  PARTICIPANTE ||--o{ EVENTO_PARTICIPANTE : participa
  EVENTO ||--o| NOTIFICACION_CANCELACION : origina
  LUGAR {
    bigint id PK
    string nombre
    string direccion
    string ciudad
    decimal latitud
    decimal longitud
  }
  SECTOR {
    bigint id PK
    bigint lugar_id FK
    string nombre
    boolean numerado
    int capacidad
  }
  ASIENTO {
    bigint id PK
    bigint sector_id FK
    string fila
    int numero
  }
  ORGANIZADOR {
    bigint id PK
    string nombre
    string email
  }
  PARTICIPANTE {
    bigint id PK
    string nombre
    enum tipo
  }
  EVENTO {
    bigint id PK
    string nombre
    string descripcion
    enum tipo
    bigint organizador_id FK
    bigint lugar_id FK
    datetime fecha_hora
    enum estado
    boolean alta_demanda
    enum estado_previo_suspension
    bigint version
  }
  PRECIO_SECTOR {
    bigint id PK
    bigint evento_id FK
    bigint sector_id FK
    decimal precio
  }
  EVENTO_PARTICIPANTE {
    bigint id PK
    bigint evento_id FK
    bigint participante_id FK
  }
  NOTIFICACION_CANCELACION {
    bigint id PK
    bigint evento_id UK
    boolean reservas_notificadas
    boolean entradas_notificadas
    boolean compras_notificadas
    int intentos
    datetime creada_en
  }
```

`NOTIFICACION_CANCELACION.evento_id` es una referencia lógica a `EVENTO` en la misma base, **no** está mapeada como clave foránea JPA. Permite registrar la intención de aviso en la misma transacción.

## Relaciones y restricciones

| Relación | Cardinalidad | Implementación |
|---|---|---|
| Lugar → Sector | 1:N | `Sector.lugar` (`@ManyToOne`) |
| Sector → Asiento | 1:N | `Asiento.sector` (`@ManyToOne`) |
| Lugar → Evento | 1:N | `Evento.lugar` |
| Organizador → Evento | 1:N | `Evento.organizador` |
| Evento ↔ Participante | N:M | Entidad intermedia `EventoParticipante` |
| Evento ↔ Sector | N:M con atributo `precio` | Entidad intermedia `PrecioSector` |
| Evento → aviso | 1:0..1 | `NotificacionCancelacion.eventoId` único |

**Unicidades:** `(lugar_id, nombre)` en `Sector`; `(sector_id, fila, numero)` en `Asiento`; `(evento_id, sector_id)` en `PrecioSector`; `(evento_id, participante_id)` en `EventoParticipante`.

**Por qué existe PrecioSector:** un mismo estadio puede tener un sector Platea con $30.000 para un recital y $45.000 para otro. Poner el precio en `Sector` impediría diferenciarlo por evento.

**Por qué existe EventoParticipante:** un mismo artista/equipo puede estar en muchos eventos, y un evento puede tener varios artistas/equipos. La tabla intermedia permite registrar el vínculo sin duplicar la entidad participante.

## Entidades solapadas y relaciones entre microservicios

| Servicio | Identificadores que consume de Eventos | Dueño de la información |
|---|---|---|
| Reservas | eventoId, sectorId, asientoId | Eventos define geometría; Reservas guarda ocupación, vencimientos y asignaciones |
| Compras | eventoId, sectorId, asientoId y precio consultado | Eventos define precio vigente; Compras guarda **snapshot** del importe al comprar |
| Entradas | eventoId, sectorId, asientoId | Eventos define el evento; Entradas guarda ticket, QR y estado |

No se comparten entidades JPA ni bases de datos, aunque algunos nombres y IDs se repitan en DTOs. El servicio de disponibilidad es **Reservas**, no Eventos.
