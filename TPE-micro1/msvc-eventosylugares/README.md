# TicketYa — msvc-eventos-lugares (Entrega 1)

**Microservicio Eventos y Lugares**, desarrollado en Java 21 + Spring Boot 3.5.6, Maven y JPA. Mantiene una **base relacional propia** y puede iniciarse sin que los demás microservicios estén disponibles.

Se revisó con el TPE original (páginas 1–5): funcionalidades 1, 2, 3, 9 y notificación de cancelaciones de la 7. La lógica de reservas, doble venta, pagos, reintegros y QR pertenece al resto del grupo.

## Requisitos locales

- JDK 21.
- Maven 3.9+ (comprobar con `mvn -version`).
- MariaDB con el esquema `ticketya_eventos` existente. Hibernate usa `ddl-auto: validate`; no crea ni modifica tablas. H2 se utiliza exclusivamente en tests.

## Ejecutar y verificar

Desde la raíz de este proyecto:

```bash
mvn clean test
mvn spring-boot:run
```

Puerto predeterminado de Eventos: `8081`.

Abrir:

- Swagger UI: [http://localhost:8081/swagger-ui.html](http://localhost:8081/swagger-ui.html)
- OpenAPI JSON: [http://localhost:8081/v3/api-docs](http://localhost:8081/v3/api-docs)
- Estado: [http://localhost:8081/actuator/health](http://localhost:8081/actuator/health)

**Importante:** estos comandos son instrucciones de ejecución; los tests deben correrse en una máquina con Maven. No se incluye un reporte de ejecución de tests no realizada.

### MariaDB (configuración por defecto)

Configurar `DB_URL` (por defecto `jdbc:mariadb://localhost:3306/ticketya_eventos`), `DB_USER` y `DB_PASSWORD`. El perfil `mariadb` también conserva `validate`. El perfil MySQL opcional conserva `validate`.

```powershell
$env:DB_USER="root"
$env:DB_PASSWORD="clave"
mvn spring-boot:run
```

## Carpetas del proyecto

```text
src/main/java/ar/edu/unicen/ticketya/eventos/
  controller/        Evento, Lugar, Sector, Asiento, Organizador, Participante
  service/           Un service por cada responsabilidad
  dto/               RequestDTO y ResponseDTO por entidad (clases tradicionales)
  model/             Entidades JPA + enums
  repository/        JpaRepository / JpaSpecificationExecutor
  integration/       Clientes Feign y avisos de cancelación reintentables
  exception/         Manejo de errores HTTP
  config/            Clock, REST y Swagger

src/test/             Pruebas unitarias y pruebas de API con H2
postman/              Colección de requests
scripts/              Generador de datos locales

docs/
  01-FEATURES-Y-USER-STORIES.md
  02-ARQUITECTURA-Y-DIAGRAMAS.md
  03-MODELO-DE-DATOS.md
  04-CONTRATOS-API-REST.md
  05-TESTING.md
  06-DECISIONES-Y-LIMITACIONES.md
  07-AUDITORIA-CONTRA-LA-CONSIGNA.md
  08-RESULTADO-VERIFICACION.md
```

## Flujo rápido de uso

La forma más cómoda es importar en Postman **`postman/TicketYa-Eventos.postman_collection.json`** y ejecutar la carpeta **01 — Carga y publicación** en orden. La colección genera automáticamente una fecha futura y guarda las variables de IDs. Después ejecutar Consultas, Ciclo de estados, Cancelación y Errores.

Alternativa automática: con el servidor corriendo, desde otra terminal:

```bash
python scripts/cargar_datos_prueba.py
```

Ese script crea organizador, participante, lugar, sector numerado y no numerado, asientos, evento, precios y lo publica. Crea nuevos registros cada vez: úsalo con una BD de prueba y no para producción.

## Responsabilidades del microservicio

- Un mismo `Lugar` contiene `Sector` (numerado o no) y los numerados tienen `Asiento` (fila + número).
- `PrecioSector` es el vínculo `Evento + Sector + precio`: permite un precio distinto para cada evento.
- `EventoParticipante` vincula múltiples participantes con múltiples eventos.
- Evento nace `BORRADOR`; se publica tras validar fecha futura, participantes, sectores, precios y asientos completos.
- Búsqueda por tipo, organizador, participante, ciudad, fechas y texto; ordenamientos por fecha, precio desde y relevancia.
- Estados adicionales de operación: `SUSPENDIDO`, `AGOTADO`, `CANCELADO`, `FINALIZADO`.
- Al cancelar un evento se guarda una **notificación pendiente** para los demás microservicios. La cancelación local no depende de que estén disponibles en ese momento.

## Contratos con el grupo

Reservas y Compras pueden consumir:

- `GET /eventos/{id}/habilitado`
- `GET /eventos/{id}/sectores`
- `GET /eventos/{id}`

**La lista de asientos no equivale a disponibilidad.** Reservas guarda y controla la ocupación. Pagos, reintegros y emisión de QR tampoco están en esta base.

En esta versión, `service/` contiene las interfaces y `service/impl/` contiene sus implementaciones. Los controllers siguen dependiendo de las interfaces.

### Integración verificada contra el repositorio grupal

**Puertos predeterminados actuales:**

| Microservicio | Puerto |
| Eventos y Lugares | `8081` |
| Reservas | `8080` |
| Compras | `8082` |
| Entradas | `8083` |

Los puertos pueden modificarse mediante variables de entorno. Eventos utiliza `PORT` para configurar su puerto.

**Cancelación de eventos:**

- **Reservas:** expone `POST /reservas/evento/{id}/cancelar`. El cliente Feign utiliza esa ruta implementada. También se envía `X-Operacion-Id`, que Reservas recibe como dato informativo y no procesa.
- **Entradas:** expone `POST /entradas/evento/{id}/invalidar`. El endpoint invalida las entradas emitidas del evento cancelado. El cliente Feign envía `X-Operacion-Id`, pero el endpoint no lo procesa.
- **Compras:** expone `POST /compras/evento/{id}/cancelar`. Exige `X-Operacion-Id` y gestiona los reintegros correspondientes a las compras con pagos aprobados. La protección frente a reintegros duplicados se apoya en el registro de reintegros por ítem y en la reutilización de los ya aprobados.

**Coordinación y reintentos:**

- La configuración predeterminada establece `INTEGRACIONES_HABILITADAS=true`, `ENTRADAS_HABILITADAS=true` y `COMPRAS_HABILITADAS=true`.
- Las direcciones de los receptores se configuran mediante `RESERVAS_URL`, `ENTRADAS_URL` y `COMPRAS_URL`. Por defecto apuntan a `http://localhost:8080`, `http://localhost:8083` y `http://localhost:8082`, respectivamente.
- La cancelación del evento y la creación de su notificación pendiente se guardan en la base de Eventos.
- `NotificadorCancelaciones` ejecuta reintentos programados cada 30 segundos. Registra los avisos completados, los intentos y los errores para continuar con las operaciones pendientes.
- Reservas se notifica primero. Entradas se notifica de forma independiente. Compras se notifica cuando Reservas y Entradas ya fueron notificadas correctamente, siempre que esas integraciones estén habilitadas.
- Si una notificación falla, queda pendiente para un nuevo intento. Esto permite que Eventos registre la cancelación aunque otro microservicio no esté disponible al momento de realizarla.

**Identificación de la operación:** Eventos genera un `X-Operacion-Id` estable para la cancelación. Su tratamiento depende del receptor: Reservas y Entradas no lo procesan; Compras lo exige y valida.

**Limitaciones de la integración de venta:**

- Reservas también dispone de `POST /reservas/evento/{id}/habilitar`, utilizado para habilitar las reservas de un evento.
- El flujo de venta todavía requiere compatibilizar los contratos de habilitación, consulta de precios y representación de los identificadores entre los microservicios.
- La consulta de sectores y asientos no representa disponibilidad en tiempo real. La disponibilidad y la prevención de doble venta son responsabilidad de Reservas.

Los listados `GET /eventos` conservan todos los estados para administración. `GET /eventos/publicados` y `/eventos/buscar` muestran `PUBLICADO` y `AGOTADO`. La búsqueda con `incluirNoPublicados=true` conserva la consulta administrativa. Estas rutas aún no tienen control de roles.

## Alcance y límites

La documentación de esta carpeta corresponde a Eventos y Lugares. El gateway, las user stories **de todo el sistema** y la prueba de concurrencia de **no doble venta** son entregables compartidos del grupo. La Entrega 2 agrega JWT, NoSQL, resiliencia avanzada, Docker Compose, monitoreo, etc.

Ver `docs/07-AUDITORIA-CONTRA-LA-CONSIGNA.md` y `docs/08-RESULTADO-VERIFICACION.md`.

**Verificación:** ejecutar:

```bash
mvn test "-DskipTests=false" "-Dmaven.test.skip=false" "-Dtest=*Test"
```

Las pruebas de API usan H2; no certifican conexión ni esquema MariaDB.