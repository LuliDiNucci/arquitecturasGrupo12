# Arquitectura de microservicios — Entrega 1

## 1. Diagrama de módulos (Mermaid)

```mermaid
flowchart LR
  Cliente[Cliente / Organizador / Administrador] --> Gateway[API Gateway]
  Gateway --> Eventos[msvc-eventos :8081]
  Gateway --> Reservas[msvc-reservas :8080]
  Gateway --> Compras[msvc-compras :8082]
  Gateway --> Entradas[msvc-entradas :8083]

  Eventos --> DBE[(BD relacional Eventos)]
  Reservas --> DBR[(BD relacional Reservas)]
  Compras --> DBC[(BD relacional Compras)]
  Entradas --> DBT[(BD relacional Entradas)]

  Reservas -- GET habilitado / sectores --> Eventos
  Compras -- GET sectores y precios --> Eventos

  Eventos -. avisos de cancelación reintentables .-> Reservas
  Eventos -. avisos de cancelación reintentables .-> Entradas
  Eventos -. avisos de cancelación reintentables .-> Compras

  Compras -- confirmar / liberar --> Reservas
  Compras -- emitir entradas --> Entradas
  Compras -- invalidar entradas antes de reintegrar --> Entradas
```

**Puertos:** según la configuración predeterminada actual, Eventos utiliza `8081`, Reservas `8080`, Compras `8082` y Entradas `8083`. Pueden modificarse mediante variables de entorno.

**Datos compartidos:** los microservicios intercambian identificadores y DTOs mediante HTTP/Feign. No existe acceso cruzado a las bases de datos.

## 2. Diagrama de secuencia — compra numerada

Este diagrama representa el flujo de compra del grupo. No implica que todas las operaciones estén implementadas dentro del microservicio Eventos.

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
  E-->>GW: Datos del evento, precios e IDs de asientos

  Cliente->>GW: Reservar asiento A-1
  GW->>R: POST /reservas
  R->>E: GET datos del evento / sector / asiento
  E-->>R: Datos para validar la reserva

  Note over R: Transacción y bloqueo atómico (10 min)

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
      R-->>C: Confirmación
      C->>T: POST /entradas/emitir
      T-->>C: Entradas y QR
      C-->>Cliente: Compra confirmada
    else Reserva vencida o no pudo confirmarse
      C->>R: Liberar reserva si corresponde
      C-->>Cliente: Compra no confirmada / compensación o reintegro pendiente
    end
  end
```

La prueba que demuestra la **no doble venta** requiere el servicio Reservas funcionando con bloqueo y actualización atómica en la base de datos. Eventos consulta el estado y los datos del evento, sector y asiento.

## 3. Secuencia — cancelación de evento

La cancelación se registra primero en la base de datos de Eventos. Las notificaciones a los demás microservicios se realizan posteriormente mediante un proceso programado que reintenta las operaciones pendientes.

```mermaid
sequenceDiagram
  actor Admin
  participant E as Eventos
  participant DB as BD Eventos
  participant S as Scheduler
  participant R as Reservas
  participant T as Entradas
  participant C as Compras
  participant PC as Pasarela mock

  Admin->>E: PATCH /eventos/{id}/cancelar
  E->>DB: Guardar estado CANCELADO
  E->>DB: Crear notificación pendiente
  E-->>Admin: Cancelación registrada

  Note over S,DB: Scheduler ejecuta reintentos cada 30 segundos

  loop Mientras existan notificaciones pendientes
    S->>DB: Obtener notificaciones pendientes

    S->>R: POST /reservas/evento/{id}/cancelar
    alt Reservas responde correctamente
      R-->>S: 2xx
      S->>DB: Marcar reservasNotificadas
    else Error de comunicación
      R-->>S: Error
      S->>DB: Registrar error para reintentar
    end

    S->>T: POST /entradas/evento/{id}/invalidar
    alt Entradas responde correctamente
      T-->>S: 2xx
      S->>DB: Marcar entradasNotificadas
    else Error de comunicación
      T-->>S: Error
      S->>DB: Registrar error para reintentar
    end

    Note over S: Compras se notifica cuando Reservas y Entradas finalizaron correctamente

    opt Reservas y Entradas notificadas
      S->>C: POST /compras/evento/{id}/cancelar
      Note over C: Requiere X-Operacion-Id
      C->>T: POST /entradas/evento/{id}/invalidar
      T-->>C: Entradas invalidadas o ya invalidadas

      loop Por cada compra con pago aprobado
        C->>C: Bloquear compra y procesar reintegros por ítem
        C->>PC: Solicitar reintegro si el importe es mayor a cero
        PC-->>C: Resultado del reintegro
        C->>C: Actualizar estado de reintegros y compra
      end

      alt Todos los reintegros completados
        C-->>S: 204 No Content
        S->>DB: Marcar comprasNotificadas
      else Algún reintegro pendiente o error
        C-->>S: Error
        S->>DB: Registrar error para reintentar
      end
    end
  end
```

### Comportamiento real de la cancelación

- **Eventos:** cambia el estado del evento a `CANCELADO` y registra una notificación pendiente dentro de su propia transacción. La respuesta al administrador no espera a que terminen las notificaciones externas.
- **Scheduler:** se ejecuta con un intervalo de 30 segundos (`fixedDelay`). Conserva el estado de notificación de cada servicio, registra errores y vuelve a intentar las operaciones que siguen pendientes.
- **Reservas:** recibe `POST /reservas/evento/{id}/cancelar` para cancelar las reservas del evento e impedir nuevas operaciones sobre ellas.
- **Entradas:** recibe `POST /entradas/evento/{id}/invalidar`. Las entradas emitidas pasan a `INVALIDADA`, dejando de ser utilizables.
- **Compras:** recibe `POST /compras/evento/{id}/cancelar`. Antes de procesar los reintegros vuelve a solicitar la invalidación de las entradas y verifica que estén `INVALIDADA` o `DEVUELTA`.

### Estados de las compras y reintegros

Compras procesa individualmente cada compra que tenga un pago aprobado. Para cada ítem utiliza el precio histórico registrado en la compra.

- Si todos los ítems tienen reintegros aprobados, la compra pasa a `REINTEGRADA`.
- Si algunos ítems fueron reintegrados y otros no, pasa a `REINTEGRO_PARCIAL`.
- Si no se aprobó ningún reintegro, el método no asigna un nuevo estado a la compra.
- Los ítems gratuitos se marcan como reintegrados sin invocar la pasarela.
- Si un ítem ya tiene un reintegro aprobado, no se procesa nuevamente.

Las compras sin pago aprobado no requieren una devolución de dinero. Si alguna compra no puede completar sus reintegros, Compras informa el error y la notificación permanece pendiente para un nuevo intento.

### Identificador de operación

Eventos envía el encabezado `X-Operacion-Id` con un identificador estable para la cancelación.

- Reservas recibe el encabezado, pero su implementación lo utiliza como dato informativo.
- Entradas recibe el encabezado desde Feign, aunque su endpoint no lo procesa.
- Compras exige el encabezado y lo valida. La protección contra reintegros duplicados se apoya en el registro de reintegros por ítem y en la verificación de los ya aprobados.

**Importante:** un `2xx` de una notificación indica que el servicio receptor completó su operación HTTP. En el caso de Compras, el `204` se devuelve cuando el procesamiento del evento no deja compras con reintegros pendientes. La cancelación no constituye una transacción distribuida única entre las bases de datos.

No se implementa una saga distribuida completa ni notificación por correo en esta entrega.

## 4. Estructura de código

- `controller`: endpoints HTTP y DTO Request/Response.
- `service`: servicios de negocio (Lugar, Sector, Asiento, Evento, Organizador, Participante).
- `repository`: interfaces `JpaRepository` para las entidades persistidas; Evento incluye `JpaSpecificationExecutor`.
- `model`: entidades locales, enums, `PrecioSector` y `EventoParticipante` para relaciones N:M.
- `integration`: clientes Feign y gestión de notificaciones reintentables de cancelación (outbox básico).
- `exception` y `config`: reglas de error, reloj, REST y Swagger.

**Datos compartidos:** `eventoId`, `sectorId`, `asientoId`, `reservaId`, `compraId` y `clienteId` viajan en DTOs de integración como identificadores. No se definen claves foráneas entre bases de datos de distintos microservicios.