# 03 - Modelo de Datos

## 1. Descripción general

El microservicio `msvc-recomendaciones` utiliza MongoDB como sistema de persistencia para almacenar las preferencias de los clientes.

La base de datos utilizada se denomina `ticketya_recomendaciones`.

A diferencia de una base de datos relacional, MongoDB almacena la información mediante documentos, organizados en colecciones.

## 2. Persistencia de preferencias

El microservicio almacena las preferencias asociadas a los clientes de TicketYa.

Esta información se utiliza como entrada para el proceso de generación de recomendaciones.

Cada registro de preferencias se encuentra asociado a un cliente mediante su identificador.

## 3. Estructura lógica

El modelo contempla la siguiente información:

| Dato | Descripción |
|---|---|
| Identificador del documento | Identifica el registro almacenado en MongoDB |
| Identificador del cliente | Permite asociar las preferencias con un cliente |
| Preferencias | Contiene los criterios de interés registrados por el cliente |

Los nombres y tipos concretos de los atributos corresponden a la definición de la clase de persistencia implementada en el proyecto.

## 4. Base de datos y colección

- Motor de persistencia: MongoDB.
- Base de datos: `ticketya_recomendaciones`.
- Acceso a datos: Spring Data MongoDB.

La aplicación utiliza la siguiente configuración:

`spring.data.mongodb.uri=mongodb://localhost:27017/ticketya_recomendaciones`

## 5. Utilización de los datos

El proceso de recomendación utiliza las preferencias almacenadas para identificar los eventos que pueden resultar relevantes para cada cliente.

Para ello, combina esta información con los eventos publicados obtenidos desde `msvc-eventosylugares`.

## 6. Consideraciones

Las preferencias se almacenan de manera independiente de la información de eventos.

Los datos de los eventos no se persisten en esta base de datos como parte del modelo de preferencias: se obtienen mediante la comunicación con el microservicio de Eventos y Lugares.

Esta separación permite que cada microservicio administre sus propios datos.