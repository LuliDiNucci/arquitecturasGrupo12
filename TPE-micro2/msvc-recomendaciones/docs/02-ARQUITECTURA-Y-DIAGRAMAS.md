# 02 - Arquitectura y Diagramas

## 1. Arquitectura general

El microservicio `msvc-recomendaciones` forma parte de la arquitectura de microservicios de TicketYa.

Su responsabilidad principal es gestionar las preferencias de los clientes y generar recomendaciones de eventos a partir de ellas.

Para cumplir esta función utiliza dos fuentes de información:

- MongoDB: almacena las preferencias de los clientes.
- `msvc-eventosylugares`: proporciona los eventos publicados mediante una comunicación HTTP realizada con Spring Cloud OpenFeign.

## 2. Componentes principales

### 2.1. Capa de presentación

Está compuesta por los controladores REST, que reciben las solicitudes HTTP y exponen las operaciones del microservicio.

Entre sus operaciones se encuentra la consulta de recomendaciones por identificador de cliente.

### 2.2. Capa de servicios

Contiene la lógica de negocio relacionada con las preferencias y la generación de recomendaciones.

Coordina la recuperación de las preferencias y la consulta de eventos publicados.

### 2.3. Capa de persistencia

Utiliza Spring Data MongoDB para almacenar y recuperar las preferencias de los clientes.

La información se guarda en la base de datos `ticketya_recomendaciones`.

### 2.4. Comunicación con otros microservicios

Se utiliza Spring Cloud OpenFeign para comunicarse con `msvc-eventosylugares`.

El cliente Feign permite consultar el endpoint de eventos publicados sin implementar manualmente las solicitudes HTTP.

## 3. Diagrama de arquitectura

```text
                  CLIENTE
                     |
                     | HTTP
                     v
          MSVC-RECOMENDACIONES
                     |
          +----------+-----------+
          |                      |
          v                      v
    CONTROLADORES             SERVICIOS
          |                      |
          |                +-----+------+
          |                |            |
          v                v            v
       ENDPOINTS       MONGODB       EVENTO CLIENT
                           |            |
                           v            | HTTP / Feign
                    ticketya_           |
                    recomendaciones     v
                               MSVC-EVENTOSYLUGARES
                                          |
                                          v
                                    EVENTOS PUBLICADOS
```

## 4. Flujo de consulta de recomendaciones

El flujo general de consulta es el siguiente:

1. El cliente realiza una solicitud al endpoint `GET /api/recomendaciones/{clienteId}`.
2. El controlador recibe el identificador del cliente.
3. La capa de servicios recupera las preferencias almacenadas en MongoDB.
4. Se consultan los eventos publicados mediante el cliente Feign.
5. La lógica de recomendaciones utiliza las preferencias y los eventos disponibles para generar el resultado.
6. El controlador devuelve la respuesta al cliente.

## 5. Diagrama de secuencia

```text
Cliente       Controller       Service       MongoDB       EventoClient       Eventos
  |                |              |              |                |               |
  | GET recomendaciones           |              |                |               |
  |--------------->|              |              |                |               |
  |                | consultar    |              |                |               |
  |                |------------->|              |                |               |
  |                |              | buscar      |                |               |
  |                |              |------------->|                |               |
  |                |              | preferencias|                |               |
  |                |              |<-------------|                |               |
  |                |              | consultar eventos publicados  |               |
  |                |              |------------------------------>|               |
  |                |              |                               | GET publicados|
  |                |              |                               |-------------->|
  |                |              |                               | eventos       |
  |                |              |                               |<--------------|
  |                |              | eventos                       |               |
  |                |              |<------------------------------|               |
  |                | resultado    |              |                |               |
  |                |<-------------|              |                |               |
  | respuesta      |              |              |                |               |
  |<---------------|              |              |                |               |
```

## 6. Configuración de comunicación

La URL del microservicio de Eventos se configura mediante la propiedad:

`app.eventos.url=${EVENTOS_URL:http://localhost:8081}`

Esto permite establecer la dirección del servicio mediante una variable de entorno y utilizar `http://localhost:8081` como valor predeterminado.

## 7. Consideraciones

La generación de recomendaciones depende de que el microservicio de Eventos y Lugares esté disponible y devuelva eventos publicados.

Si la consulta de eventos no devuelve resultados, el microservicio no dispone de eventos para utilizar en el proceso de recomendación.

La integración se encuentra implementada, pero quedó pendiente verificar el flujo completo con eventos reales publicados.