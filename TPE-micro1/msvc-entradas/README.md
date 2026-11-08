# Microservicio de Entradas – TicketYa

## 1. Descripción

El microservicio `msvc-entradas` forma parte del sistema TicketYa y se encarga de gestionar las entradas asociadas a las compras realizadas por los clientes.

Es responsable de emitir las entradas una vez confirmada la compra y la reserva, generar sus códigos QR, permitir su consulta y administrar las solicitudes de devolución.

También permite invalidar las entradas cuando un evento es cancelado.

## 2. Funcionalidades principales

- **Emisión de entradas:** genera una entrada por cada lugar incluido en una reserva, verificando previamente que la compra esté aprobada y la reserva confirmada.
- **Generación de códigos QR:** cada entrada cuenta con un código QR único.
- **Consulta de entradas:** permite consultar una entrada individual o todas las entradas de un cliente.
- **Invalidación de entradas:** cambia el estado de las entradas emitidas a `INVALIDADA` cuando el evento correspondiente está cancelado.
- **Solicitud de devolución:** permite solicitar la devolución de una entrada, siempre que se cumplan las condiciones establecidas, entre ellas el plazo mínimo de 72 horas antes del evento.
- **Gestión de devoluciones:** permite consultar solicitudes pendientes, aprobarlas o rechazarlas. Al aprobar una solicitud, se coordina la devolución del lugar con Reservas y el reintegro con Compras.

## 3. Tecnologías utilizadas

- Java 17.
- Spring Boot.
- Spring Data JPA.
- Spring Web MVC.
- Spring Cloud OpenFeign.
- RestClient.
- MySQL.
- Maven.
- JUnit y Mockito para pruebas.
- ZXing para la generación de códigos QR.

## 4. Base de datos

El microservicio utiliza una base de datos MySQL propia, denominada `ticketya_entradas`.

La conexión se configura en `src/main/resources/application.properties`.

Por defecto, se utiliza:

| Parámetro              | Valor               |
| ---------------------- | ------------------- |
| Motor                  | MySQL               |
| Base de datos          | `ticketya_entradas` |
| Host                   | `localhost`         |
| Puerto                 | `3306`              |
| Usuario predeterminado | `root`              |

Las credenciales pueden configurarse mediante las variables de entorno:

- `DB_USERNAME`: usuario de MySQL.
- `DB_PASSWORD`: contraseña de MySQL.

La propiedad `spring.datasource.url` incluye `createDatabaseIfNotExist=true`, por lo que la base de datos se crea automáticamente si no existe, siempre que el usuario configurado tenga los permisos necesarios.

El modelo de datos está compuesto por las entidades `Entrada` y `SolicitudDevolucion`.

## 5. Configuración y ejecución

### Requisitos previos

Antes de ejecutar el microservicio se necesita:

- Tener instalado Java 17.
- Tener MySQL en funcionamiento.
- Contar con Maven Wrapper, incluido en el proyecto.

### Configuración

Verificar los datos de conexión a MySQL en:

`src/main/resources/application.properties`

Si se utilizan credenciales distintas de las predeterminadas, configurar las variables de entorno `DB_USERNAME` y `DB_PASSWORD`.

### Ejecutar el microservicio

Desde la carpeta `msvc-entradas`, ejecutar:

```powershell
.\mvnw spring-boot:run
```

El microservicio se inicia en:

`http://localhost:8083`

Para detenerlo, utilizar `Ctrl + C` en la terminal.

## 6. Comunicación con otros microservicios

`msvc-entradas` se comunica con otros servicios del sistema para validar información y coordinar operaciones.

| Microservicio          | Puerto | Responsabilidad                                              |
| ---------------------- | -----: | ------------------------------------------------------------ |
| `msvc-reservas`        |   8080 | Consultar reservas y solicitar la devolución de lugares.     |
| `msvc-eventosylugares` |   8081 | Consultar información y estado de los eventos.               |
| `msvc-compras`         |   8082 | Consultar la confirmación de compras y solicitar reintegros. |

Las direcciones de estos servicios se configuran mediante las propiedades:

- `msvc.reservas.url`
- `msvc.eventos.url`
- `msvc.compras.url`

Para realizar operaciones que dependen de otros microservicios, estos deben encontrarse disponibles.

## 7. Endpoints principales

### Entradas

| Método | Endpoint                          | Descripción                                                             |
| ------ | --------------------------------- | ----------------------------------------------------------------------- |
| POST   | `/entradas/emitir`                | Emite las entradas correspondientes a una compra.                       |
| GET    | `/entradas/{id}`                  | Consulta una entrada individual. Requiere el encabezado `X-Cliente-Id`. |
| GET    | `/entradas/cliente/{clienteId}`   | Obtiene las entradas de un cliente.                                     |
| POST   | `/entradas/evento/{id}/invalidar` | Invalida las entradas emitidas de un evento cancelado.                  |

### Devoluciones

| Método | Endpoint                                                        | Descripción                           |
| ------ | --------------------------------------------------------------- | ------------------------------------- |
| POST   | `/devoluciones`                                                 | Registra una solicitud de devolución. |
| GET    | `/devoluciones/pendientes`                                      | Consulta las solicitudes pendientes.  |
| PATCH  | `/devoluciones/{id}/aprobar?administradorId={administradorId}`  | Aprueba una solicitud de devolución.  |
| PATCH  | `/devoluciones/{id}/rechazar?administradorId={administradorId}` | Rechaza una solicitud de devolución.  |

## 8. Pruebas

El microservicio cuenta con pruebas unitarias desarrolladas con JUnit y Mockito.

Se cubren principalmente:

- Emisión e idempotencia de entradas.
- Validación de compras y reservas.
- Consulta y autorización de acceso a entradas.
- Invalidación de entradas por cancelación de eventos.
- Solicitudes, aprobación y rechazo de devoluciones.
- Generación y validación de imágenes QR.
- Inicialización del contexto de Spring.

Para ejecutar las pruebas desde la carpeta `msvc-entradas`:

```powershell
.\mvnw test
```

## 9. Consideraciones y limitaciones

- La emisión de entradas verifica que la compra esté aprobada y que la reserva esté confirmada.
- La emisión es idempotente: si las entradas ya existen para una compra, se devuelven las existentes sin generar duplicados.
- Cada entrada posee un código QR único.
- Las devoluciones deben solicitarse con al menos 72 horas de anticipación al evento.
- La aprobación de una devolución coordina operaciones con los microservicios de Reservas y Compras.
- Actualmente, la invalidación de entradas por cancelación de un evento solamente modifica el estado de las entradas. La liberación de los lugares y la coordinación de los reintegros no están implementadas en ese flujo.

