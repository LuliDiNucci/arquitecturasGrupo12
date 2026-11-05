# Arquitectura de microservicios — Entrega 1

## 1. Diagrama de módulos (Mermaid)

```mermaid
flowchart LR
  Cliente[Cliente / Organizador / Administrador] --> Gateway[API Gateway]
  Gateway --> Eventos[msvc-eventos :8081]
  Gateway --> Reservas[msvc-reservas]
  Gateway --> Compras[msvc-compras]
  Gateway --> Entradas[msvc-entradas]
  Eventos --> DBE[(BD relacional Eventos)]
  Reservas --> DBR[(BD relacional Reservas)]
  Compras --> DBC[(BD relacional Compras)]
  Entradas --> DBT[(BD relacional Entradas)]
  Reservas -- GET habilitado / sectores --> Eventos
  Compras -- GET sectores y precios --> Eventos
  Eventos -. avisos cancelacion .-> Reservas
  Eventos -. avisos cancelacion .-> Compras
  Eventos -. avisos cancelacion .-> Entradas
  Compras -- confirmar / liberar --> Reservas
  Compras -- emitir tickets --> Entradas
```

**Puertos ajenos:** los de Reservas, Compras y Entradas se acuerdan con el grupo. `8081` solo corresponde a Eventos. **No hay acceso cruzado a bases**.

## 2. Diagrama de secuencia — compra numerada (contrato del grupo, no implementación de Eventos)

```mermaid
sequenceDiagram
  actor Cliente
  participant GW as Gateway
  participant E as Eventos
  participant R as Reservas
  participant C as Compras/Pagos
  participant T as Entradas
  Cliente->>GW: Buscar evento / sectores
  GW->>E: GET /eventos/buscar y /eventos/{id}/sectores
  E-->>GW: Datos del evento, precios, IDs de asientos
  Cliente->>GW: Reservar asiento A-1
  GW->>R: POST /reservas
  R->>E: GET /eventos/{id}/habilitado y /sectores
  E-->>R: Estado, sector y asientos válidos
  Note over R: Transaccion y bloqueo atomico (10 min)
  alt Asiento ocupado
    R-->>Cliente: 409 No disponible
  else Reserva creada
    R-->>Cliente: Reserva con vencimiento
    Cliente->>GW: Pagar reserva
    GW->>C: POST /compras/{id}/pagar
    alt Pago rechazado
      C->>R: POST /reservas/{id}/liberar
      C-->>Cliente: Pago rechazado
    else Pago aprobado y reserva vigente
      C->>R: POST /reservas/{id}/confirmar
      R-->>C: Confirmacion
      C->>T: POST /entradas/emitir
      T-->>C: Entradas y QR
      C-->>Cliente: Compra confirmada
    else Reserva vencio o no pudo confirmarse
      C->>R: Liberar (si corresponde)
      C-->>Cliente: Iniciar reintegro / compra no confirmada
    end
  end
```

La prueba que demuestra la **no doble venta** requiere el servicio Reservas funcionando con bloqueo/actualización atómica en BD. Eventos solo consulta estado y geometría del lugar.

## 3. Secuencia — cancelación de evento

```mermaid
sequenceDiagram
  actor Admin
  participant E as Eventos
  participant DB as BD Eventos
  participant R as Reservas
  participant T as Entradas
  participant C as Compras
  Admin->>E: PATCH /eventos/{id}/cancelar
  E->>DB: Transaccion: CANCELADO + aviso pendiente
  E-->>Admin: Estado CANCELADO
  Note over E: Scheduler reintenta entrega del aviso
  E->>R: POST /reservas/evento/{id}/cancelar
  R-->>E: 2xx (bloquea nuevas reservas)
  E->>T: POST /entradas/evento/{id}/invalidar
  T-->>E: 2xx (invalida QR)
  E->>C: POST /compras/evento/{id}/cancelar
  C-->>E: 2xx (gestiona reintegros)
  Note over E,C: Retries con X-Operacion-Id estable
```

**Importante:** las rutas salientes son un **contrato propuesto**, no prueban que otros equipos ya las implementaron. Un 2xx confirma recepción, no que todos los reintegros terminaron. No se implementa notificación por correo ni saga completa en Entrega 1.

## 4. Estructura de código

- `controller`: endpoints HTTP y DTO Request/Response.
- `service`: seis servicios de negocio (Lugar, Sector, Asiento, Evento, Organizador, Participante).
- `repository`: una interfaz `JpaRepository` por entidad persistida; Evento incluye `JpaSpecificationExecutor`.
- `model`: entidades locales, enums, `PrecioSector` y `EventoParticipante` para relaciones N:M.
- `integration`: avisos reintentables de cancelación (outbox básico).
- `exception` y `config`: reglas de error, reloj, REST y Swagger.

**Datos compartidos:** `eventoId`, `sectorId`, `asientoId`, `reservaId`, `compraId`, `clienteId` viajan en DTOs de integración como IDs; no se definen FKs de una base a otra.
