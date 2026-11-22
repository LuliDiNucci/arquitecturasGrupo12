# Modelo de datos — Eventos y Lugares

## 1. Persistencia

El microservicio utiliza una base de datos MySQL propia y Spring Data JPA.

Su modelo administra lugares, sectores, asientos, eventos, organizadores, participantes, precios y registros persistentes de notificaciones.

## 2. Diagrama entidad-relación

```mermaid
erDiagram
    ORGANIZADOR ||--o{ EVENTO : organiza
    LUGAR ||--o{ EVENTO : alberga
    LUGAR ||--o{ SECTOR : contiene
    SECTOR ||--o{ ASIENTO : tiene
    EVENTO ||--o{ PRECIO_SECTOR : define
    SECTOR ||--o{ PRECIO_SECTOR : recibe
    EVENTO ||--o{ EVENTO_PARTICIPANTE : incluye
    PARTICIPANTE ||--o{ EVENTO_PARTICIPANTE : participa
    EVENTO ||--o| NOTIFICACION_CANCELACION : registra
    EVENTO ||--o{ AVISO_EVENTO_PENDIENTE : genera

    ORGANIZADOR {
        bigint id PK
        string nombre
        string email
        bigint usuario_id UK
    }

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

    EVENTO {
        bigint id PK
        bigint organizador_id FK
        bigint lugar_id FK
        string nombre
        string descripcion
        string tipo
        datetime fecha_hora
        string estado
        boolean alta_demanda
        string estado_previo_suspension
        bigint version
    }

    PARTICIPANTE {
        bigint id PK
        string nombre
        string tipo
    }

    EVENTO_PARTICIPANTE {
        bigint id PK
        bigint evento_id FK
        bigint participante_id FK
    }

    PRECIO_SECTOR {
        bigint id PK
        bigint evento_id FK
        bigint sector_id FK
        decimal precio
    }

    NOTIFICACION_CANCELACION {
        bigint id PK
        bigint evento_id UK
        boolean reservas_notificadas
        boolean entradas_notificadas
        boolean compras_notificadas
        int intentos
        string correlation_id
        string ultimo_error
    }

    AVISO_EVENTO_PENDIENTE {
        bigint id PK
        bigint evento_id
        bigint version_evento
        string nombre_evento
        string detalle_cambios
        boolean cancelacion
        boolean entregado
        datetime intentar_en
        string correlation_id
    }
```

Las relaciones de `NOTIFICACION_CANCELACION` y `AVISO_EVENTO_PENDIENTE` con `EVENTO` representan referencias lógicas por identificador, no asociaciones JPA con claves foráneas.

## 3. Relaciones principales

| Relación | Cardinalidad | Descripción |
|---|---|---|
| Lugar–Sector | 1:N | Un lugar contiene varios sectores. |
| Sector–Asiento | 1:N | Los sectores numerados contienen asientos. |
| Organizador–Evento | 1:N | Un organizador administra varios eventos. |
| Lugar–Evento | 1:N | Un lugar puede albergar diferentes eventos. |
| Evento–Participante | N:M | Se resuelve con `EventoParticipante`. |
| Evento–Sector | N:M | Se resuelve con `PrecioSector`, que almacena el precio. |
| Evento–NotificaciónCancelación | 1:0..1 | Se registra una cancelación por evento. |
| Evento–AvisoEventoPendiente | 1:N | Se registran avisos por cambios y cancelaciones. |

## 4. Restricciones importantes

- El nombre de un sector debe ser único dentro de su lugar.
- La combinación de fila y número de un asiento debe ser única dentro de su sector.
- Cada evento tiene un único precio por sector.
- No se repite un participante dentro del mismo evento.
- La vinculación `Organizador.usuarioId` es única y referencia lógicamente a Usuarios.
- Los avisos a compradores se identifican por evento, tipo de aviso y versión para evitar registros duplicados.

## 5. Separación entre microservicios

Eventos y Lugares mantiene la configuración de los espacios y sus precios.

Las reservas temporales, los asientos ocupados y la disponibilidad real pertenecen a Reservas. Compras administra pagos y reintegros; Entradas administra los tickets y sus QR.

Las integraciones utilizan identificadores y DTO, sin compartir tablas ni entidades JPA entre microservicios.