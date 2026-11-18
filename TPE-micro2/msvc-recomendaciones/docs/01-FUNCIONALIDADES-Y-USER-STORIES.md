# 01 - Funcionalidades y User Stories

## 1. Descripción general

El microservicio de Recomendaciones tiene como objetivo ofrecer a los clientes de TicketYa sugerencias de eventos que puedan resultar de su interés.

Para ello, permite registrar las preferencias de los clientes y consultar los eventos publicados en el sistema, utilizando esa información para generar recomendaciones.

## 2. Funcionalidades principales

### 2.1. Registro de preferencias

Permite registrar las preferencias de un cliente para utilizarlas como criterio al momento de generar recomendaciones.

Las preferencias se almacenan en MongoDB.

### 2.2. Consulta de recomendaciones

Permite consultar las recomendaciones asociadas a un cliente mediante su identificador.

Para generar las recomendaciones, el microservicio utiliza las preferencias almacenadas y consulta los eventos publicados a través del microservicio de Eventos y Lugares.

### 2.3. Integración con Eventos y Lugares

El microservicio se comunica con `msvc-eventosylugares` mediante Spring Cloud OpenFeign.

Esta integración permite obtener los eventos publicados que pueden utilizarse en el proceso de recomendación.

## 3. User Stories

### US-01: Registrar preferencias

**Como** cliente de TicketYa,

**quiero** registrar mis preferencias,

**para** recibir recomendaciones de eventos relacionadas con mis intereses.

**Criterio de aceptación:**
- El sistema debe permitir registrar las preferencias asociadas a un cliente.
- La información debe persistirse en MongoDB.

### US-02: Consultar recomendaciones

**Como** cliente de TicketYa,

**quiero** consultar recomendaciones de eventos,

**para** encontrar eventos que coincidan con mis intereses.

**Criterios de aceptación:**
- El sistema debe permitir consultar recomendaciones utilizando el identificador del cliente.
- Debe utilizar las preferencias registradas para generar las recomendaciones.
- Debe consultar los eventos publicados disponibles en el microservicio de Eventos y Lugares.

### US-03: Obtener eventos publicados

**Como** sistema de recomendaciones,

**quiero** consultar los eventos publicados en TicketYa,

**para** utilizarlos como información de entrada al proceso de recomendación.

**Criterios de aceptación:**
- El microservicio debe comunicarse con Eventos y Lugares mediante Feign.
- Debe utilizar los eventos publicados recibidos para el proceso de recomendación.

## 4. Alcance y estado de verificación

Las funcionalidades de registro de preferencias y consulta de recomendaciones están implementadas.

La integración con Eventos y Lugares está desarrollada mediante Feign. Sin embargo, quedó pendiente verificar el resultado completo de las recomendaciones utilizando eventos reales publicados, debido a que no se logró disponer de un evento publicado durante las pruebas de integración.

