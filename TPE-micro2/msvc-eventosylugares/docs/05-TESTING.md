# 05 — Plan de testing de Eventos y Lugares

## 1. Objetivo

Las pruebas del microservicio `msvc-eventosylugares` verifican las principales reglas de negocio relacionadas con la gestión de eventos, lugares, sectores, asientos, precios, publicación, búsquedas y cancelaciones.

La estrategia de testing incluye pruebas unitarias, pruebas de integración de API REST y pruebas manuales mediante Postman.

Las pruebas automatizadas permiten verificar las validaciones y transiciones de estado, mientras que las pruebas de API comprueban el comportamiento de los endpoints y las respuestas HTTP.

## 2. Tecnologías y configuración

Las pruebas utilizan Java 21, JUnit, Spring Boot Test, Maven y una base de datos H2 en memoria para las pruebas de integración de API.

La configuración de pruebas se encuentra en `application-test.yml`.

Esta base de datos es independiente del archivo `ticketya-eventos.mv.db` utilizado durante la ejecución local del servicio.

### 2.1. Ejecución de pruebas automatizadas

Desde el directorio raíz del microservicio `msvc-eventosylugares`, con JDK 21 y Maven instalados:

```bash
mvn clean test
```

Para compilar y empaquetar el proyecto sin ejecutar las pruebas:

```bash
mvn -DskipTests package
```

Para iniciar el microservicio localmente:

```bash
mvn spring-boot:run
```

## 3. Pruebas automatizadas

### 3.1. EventoServiceTest

Esta clase verifica las reglas de negocio relacionadas con la publicación, modificación, cancelación y búsqueda de eventos.

| Caso de prueba | Comportamiento verificado |
|---|---|
| Publicar un evento sin sectores. | Se rechaza la publicación con `409 Conflict` y el evento conserva el estado `BORRADOR`. |
| Publicar un evento sin precios configurados. | Se rechaza la publicación con `409 Conflict`. |
| Publicar un evento con asientos faltantes. | Se rechaza la publicación con `409 Conflict`. |
| Publicar un evento con configuración completa. | El evento cambia al estado `PUBLICADO`. |
| Cancelar un evento dos veces. | Se registra una única notificación de cancelación. |
| Modificar la fecha de un evento publicado. | Se rechaza la modificación con `409 Conflict`. |
| Consultar la habilitación de un evento agotado. | El evento `AGOTADO` no se encuentra habilitado para nuevas reservas. |
| Ordenar eventos por precio mínimo. | Los resultados se ordenan según el menor precio configurado por evento. |
| Buscar eventos con fechas invertidas. | Se rechaza la búsqueda con `400 Bad Request`. |
| Configurar un precio para un sector ajeno al evento. | Se rechaza la operación con `400 Bad Request`. |

### 3.2. AsientoServiceTest

Esta clase verifica las validaciones relacionadas con la creación y administración de asientos.

| Caso de prueba | Comportamiento verificado |
|---|---|
| Registrar asientos en un sector no numerado. | Se rechaza la creación. |
| Registrar asientos superando la capacidad del sector. | Se rechaza la operación. |
| Enviar asientos duplicados en una misma solicitud. | Se rechaza la solicitud. |
| Registrar un asiento que ya existe en el sector. | Se rechaza la duplicación. |
| Registrar una fila con letras minúsculas. | La fila se normaliza a mayúsculas. |

### 3.3. SectorServiceTest

Esta clase verifica la creación de sectores y las restricciones asociadas.

| Caso de prueba | Comportamiento verificado |
|---|---|
| Crear un sector válido. | Se registra correctamente y se obtiene una respuesta de creación. |
| Crear un sector con un nombre duplicado dentro del mismo lugar. | Se rechaza la operación por conflicto. |

### 3.4. EventoApiIntegrationTest

Las pruebas de integración de API utilizan Spring Boot Test y H2 en memoria para verificar el comportamiento de las operaciones REST.

| Caso de prueba | Comportamiento verificado |
|---|---|
| Búsqueda y cancelación de eventos mediante HTTP. | Se verifican las respuestas, estados y filtros correspondientes. |
| Publicación de un evento con sector numerado. | Se rechaza la configuración incompleta con `409` y se permite la publicación con `200` cuando se cumplen las condiciones. |
| Cancelación con avisos a tres microservicios. | Se registra la notificación de cancelación correspondiente. |
| Duplicación de asientos mediante HTTP. | Se rechaza la operación con `409 Conflict`. |
| Búsqueda por participante y rango de fechas. | Se comprueba la inclusión y exclusión de eventos según los criterios utilizados. |

### 3.5. NotificadorCancelacionesTest

Esta clase verifica el componente encargado de coordinar las notificaciones derivadas de la cancelación de eventos.

| Caso de prueba | Comportamiento verificado |
|---|---|
| Notificación a Reservas, Entradas y Compras. | Se verifica el procesamiento de las comunicaciones configuradas. |
| Error durante la notificación a Entradas. | Se simula una excepción para comprobar el tratamiento del error. |
| Integraciones deshabilitadas. | Se verifica el comportamiento del notificador cuando las integraciones están desactivadas. |
| Repetición de notificaciones. | Se comprueba el tratamiento idempotente de las operaciones de notificación. |

## 4. Pruebas manuales con Postman

El microservicio cuenta con una colección Postman que permite ejecutar los principales flujos de administración de lugares, sectores, asientos y eventos.

### 4.1. Configuración

1. Importar la colección `postman/TicketYa-Eventos.postman_collection.json`.
2. Iniciar el microservicio Eventos y Lugares.
3. Configurar la URL correspondiente al entorno local.
4. Ejecutar las solicitudes siguiendo el orden de creación y publicación.

### 4.2. Circuito de creación y publicación

La carpeta **Carga y publicación** permite ejecutar las operaciones necesarias para configurar un evento.

Durante la ejecución se registran los siguientes identificadores:

- `organizadorId`
- `participanteId`
- `lugarId`
- `sectorNumeradoId`
- `sectorCampoId`
- `eventoId`

El circuito contempla:

- Creación de organizadores y participantes.
- Creación de lugares.
- Configuración de sectores numerados y no numerados.
- Registro de asientos.
- Creación de eventos en estado `BORRADOR`.
- Configuración de precios por sector.
- Publicación del evento.
- Consulta de la información publicada.

Las operaciones de creación devuelven `201 Created` y las consultas o actualizaciones correctas devuelven `200 OK`, según el contrato correspondiente.

### 4.3. Pruebas de búsqueda

La colección permite consultar eventos utilizando distintos criterios.

Por ejemplo:

```http
GET /eventos/buscar?ciudad=Tandil
```

La búsqueda permite verificar que los eventos publicados aparezcan en los resultados y que los filtros configurados se apliquen correctamente.

También se contemplan búsquedas por tipo, participante y rango de fechas, además de los distintos criterios de ordenamiento.

### 4.4. Pruebas de cancelación

Las pruebas de cancelación permiten verificar:

- El cambio del estado del evento a `CANCELADO`.
- El registro de la notificación de cancelación.
- La consulta del estado de las notificaciones.
- La coordinación de los avisos a los microservicios configurados.
- El comportamiento de las comunicaciones cuando las integraciones están deshabilitadas.

El estado de las notificaciones puede consultarse mediante:

```http
GET /eventos/{id}/notificaciones-cancelacion
```

## 5. Validaciones y casos negativos

Las reglas de negocio se verifican mediante escenarios que comprueban el rechazo de operaciones inválidas.

| Escenario | Respuesta esperada |
|---|---|
| Crear un lugar con coordenadas fuera de rango. | `400 Bad Request` |
| Configurar un precio negativo. | `400 Bad Request` |
| Consultar un lugar, organizador, participante o evento inexistente. | `404 Not Found` |
| Publicar un evento sin sectores, participantes, precios o asientos completos. | `409 Conflict` |
| Modificar fecha o lugar de un evento `PUBLICADO`. | `409 Conflict` |
| Publicar nuevamente un evento `CANCELADO`. | `409 Conflict` |
| Buscar utilizando `desde` posterior a `hasta`. | `400 Bad Request` |

También se contempla la configuración de precios diferentes para un mismo sector utilizado en distintos eventos.

## 6. Conclusión

El microservicio `msvc-eventosylugares` cuenta con pruebas para las principales operaciones de negocio y validaciones relacionadas con eventos, lugares, sectores y asientos.

Las pruebas automatizadas cubren las reglas de publicación, modificación, búsqueda, control de capacidad, prevención de duplicados y generación de notificaciones de cancelación.

Las pruebas de API REST y la colección Postman complementan la cobertura, permitiendo comprobar las respuestas HTTP y los principales circuitos funcionales de Eventos y Lugares.