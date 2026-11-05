# TicketYa — msvc-eventos-lugares (Entrega 1)

**Microservicio Eventos y Lugares**, en Java 21 + Spring Boot 3.5.6, Maven y JPA. Mantiene una **base relacional propia** y puede ejecutarse **sin los demás microservicios**.

Se revisó con el TPE original (páginas 1–5): funcionalidad 1, 2, 3, 9 y notificación de cancelaciones de la 7. La lógica de reservas, doble venta, pagos, reintegros y QR pertenece al resto del grupo.

## Requisitos locales

- JDK 21.
- Maven 3.9+ (comprobar con `mvn -version`).
- MariaDB con el esquema `ticketya_eventos` existente. Hibernate usa `ddl-auto: validate`; no crea ni modifica tablas. H2 se utiliza exclusivamente en tests.

## Ejecutar y verificar

En la raíz de este proyecto:

```bash
mvn clean test
mvn spring-boot:run
```

Abrir:
- Swagger UI: http://localhost:8081/swagger-ui.html
- OpenAPI JSON: http://localhost:8081/v3/api-docs
- Estado: http://localhost:8081/actuator/health

**Importante:** estos comandos son las instrucciones de ejecución; los tests deben correrse en una máquina con Maven. No se incluye un reporte de ejecución de tests no realizada.

### MariaDB (configuracion por defecto)

Configurar `DB_URL` (por defecto `jdbc:mariadb://localhost:3306/ticketya_eventos`), `DB_USER` y `DB_PASSWORD`. El perfil `mariadb` tambien conserva `validate`. El perfil MySQL opcional conserva `validate`.

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
  integration/       Avisos de cancelacion a otros servicios
  exception/         Manejo de errores HTTP
  config/            Clock, REST y Swagger
src/test/            Pruebas unitarias y pruebas de API con H2
postman/             Coleccion de requests
scripts/             Generador de datos locales

docs/
  01-FEATURES-Y-USER-STORIES.md
  02-ARQUITECTURA-Y-DIAGRAMAS.md
  03-MODELO-DE-DATOS.md
  04-CONTRATOS-API-REST.md
  05-TESTING.md
  06-DECISIONES-Y-LIMITACIONES.md
  07-AUDITORIA-CONTRA-LA-CONSIGNA.md
```

## Flujo rápido de uso

La forma más cómoda es importar en Postman **`postman/TicketYa-Eventos.postman_collection.json`** y ejecutar la carpeta **01 — Carga y publicación** en orden. La colección genera automáticamente una fecha futura y guarda las variables de IDs. Después ejecutar Consultas, Ciclo de estados, Cancelación y Errores.

Alternativa automática: con el servidor corriendo, desde otra terminal:

```bash
python scripts/cargar_datos_prueba.py
```

Ese script crea organizador, participante, lugar, sector numerado y no numerado, asientos, evento, precios, y lo publica. Crea nuevos registros cada vez: úsalo con una BD de prueba y no para producción.

## Responsabilidades del microservicio

- Un mismo `Lugar` contiene `Sector` (numerado o no) y los numerados tienen `Asiento` (fila + número).
- `PrecioSector` es el vínculo `Evento + Sector + precio`: permite un precio distinto para cada evento.
- `EventoParticipante` vincula múltiples participantes con múltiples eventos.
- Evento nace `BORRADOR`; se publica tras validar fecha futura, participantes, sectores, precios y asientos completos.
- Búsqueda por tipo, organizador, participante, ciudad, fechas y texto; ordenamientos por fecha, precio desde y relevancia.
- Estados adicionales de operación: SUSPENDIDO, AGOTADO, CANCELADO, FINALIZADO.
- Al cancelar se guarda una **notificación pendiente** para Reservas, Entradas y Compras, sin que deban estar encendidos para arrancar Eventos.

## Contratos con el grupo

Reservas y Compras pueden consumir:
- `GET /eventos/{id}/habilitado`
- `GET /eventos/{id}/sectores`
- `GET /eventos/{id}`

**La lista de asientos no equivale a disponibilidad.** Reservas guarda y controla ocupación. Pagos, reintegros y emisión de QR tampoco están en esta base.

En esta version, `service/` contiene las interfaces y `service/impl/` contiene sus implementaciones. Los controllers siguen dependiendo de las interfaces.

Integracion verificada contra el repositorio grupal:

- Reservas expone `POST /reservas/evento/{id}/cancelar`, en el puerto 8080. El Feign usa esa ruta real. La cabecera `X-Operacion-Id` es informativa: Reservas no la procesa.
- Entradas y Compras tienen clientes Feign completos para los contratos propuestos: `POST /entradas/evento/{id}/invalidar` y `POST /compras/evento/{id}/cancelar`. Los receptores aun no implementan estas rutas. Al habilitar integraciones se enviaran las solicitudes HTTP; si fallan, los avisos permanecen pendientes para reintento.
- Habilitar el envio con `INTEGRACIONES_HABILITADAS=true`; por defecto permanece apagado.
- `RESERVAS_URL`, `ENTRADAS_URL` y `COMPRAS_URL` permiten configurar las direcciones de los tres receptores.
- Nombre de aplicacion y artefacto: `msvc-eventos-lugares`, coincidente con el cliente de Reservas.
- Eventos y Compras declaran ambos 8081 en el repositorio. Para ejecutarlos juntos hay que asignar puertos distintos mediante `PORT` en Eventos y actualizar la URL del consumidor. Entradas no declara puerto propio.
- Reservas tambien exige habilitacion previa mediante su endpoint `/reservas/evento/{id}/habilitar` y espera un endpoint de precio individual y campos planos de IDs que este servicio aun no ofrece. El flujo completo de venta sigue pendiente de adaptar; la cancelacion tiene contrato real.

Listados: `GET /eventos` conserva todos los estados para administracion. `GET /eventos/publicados` y `/eventos/buscar` muestran PUBLICADO y AGOTADO. La busqueda con `incluirNoPublicados=true` conserva la consulta administrativa. Estas rutas aun no tienen control de roles.

## Alcance y límites

La documentación de esta carpeta corresponde a Eventos y Lugares. El gateway, las user stories **de todo el sistema** y la prueba de concurrencia de **no doble venta** son entregables compartidos del grupo. La Entrega 2 agrega JWT, NoSQL, resiliencia avanzada, Docker Compose, monitoreo, etc. Ver `docs/07-AUDITORIA-CONTRA-LA-CONSIGNA.md`.

**Verificacion:** ejecutar `mvn test "-DskipTests=false" "-Dmaven.test.skip=false" "-Dtest=*Test"`. Las pruebas de API usan H2; no certifican conexion ni esquema MariaDB.
