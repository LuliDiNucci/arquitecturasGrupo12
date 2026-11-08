# 06 — Decisiones y limitaciones de msvc-reservas

## 1. Objetivo

Este documento registra las principales decisiones de diseño e implementación del microservicio de Reservas, junto con sus limitaciones conocidas.

El objetivo es dejar claro qué responsabilidades resuelve el servicio, cómo administra la disponibilidad y qué aspectos requieren coordinación o validación con los demás microservicios de TicketYa.

## 2. Decisiones de diseño

### 2.1. Responsabilidad del microservicio

`msvc-reservas` administra las reservas de entradas y la disponibilidad de sectores y asientos.

Sus responsabilidades principales son:

- Consultar la disponibilidad de sectores y asientos.
- Crear reservas para eventos.
- Confirmar reservas.
- Liberar reservas.
- Administrar vencimientos.
- Gestionar cancelaciones de eventos.
- Registrar la liberación de entradas cuando corresponde.

La información propia de eventos, sectores, asientos y precios se consulta mediante el cliente REST hacia `msvc-eventos-lugares`.

### 2.2. Comunicación con msvc-eventos-lugares

Se utiliza un cliente Feign (`EventoClient`) para consultar información del microservicio de Eventos y Lugares.

El cliente implementa consultas para obtener:

- Información de un evento.
- Información de un sector.
- Información de un asiento.
- Asientos pertenecientes a un sector.
- Precio de un sector para un evento.

De esta manera, Reservas evita mantener una copia completa de la información administrada por Eventos y Lugares.

**Limitación:** aunque el cliente Feign está implementado, la compatibilidad y el funcionamiento conjunto deben verificarse mediante pruebas de integración contra el microservicio real.

### 2.3. Vencimiento de las reservas

Las reservas tienen un tiempo de vigencia de 10 minutos desde su creación.

Se decidió utilizar un vencimiento lógico:

- Se registra la fecha y hora de vencimiento.
- Una reserva se considera vigente únicamente mientras `fechaVencimiento` sea posterior al momento actual.
- Las consultas de disponibilidad excluyen las reservas vencidas.
- Cuando una operación accede a una reserva vencida, se actualiza su estado a `VENCIDA`.

Esta decisión permite liberar la disponibilidad sin depender de una tarea programada que actualice periódicamente todas las reservas.

**Aclaración:** una reserva vencida puede permanecer temporalmente registrada con estado `ACTIVA` en la base de datos hasta que una operación la actualice. Sin embargo, deja de contabilizarse como ocupación una vez superada su fecha de vencimiento.

### 2.4. Control de concurrencia

Se utilizan mecanismos de persistencia y control transaccional para evitar que dos solicitudes simultáneas obtengan el mismo asiento.

La prueba de concurrencia con MySQL verifica el comportamiento ante múltiples solicitudes concurrentes para un mismo asiento.

En la prueba se realizan diez intentos simultáneos y se espera que solamente uno consiga reservarlo.

Esta decisión busca evitar la sobreventa y mantener la consistencia de la disponibilidad.

**Limitación:** la prueba realizada no reemplaza una validación de concurrencia bajo múltiples instancias del microservicio desplegadas en un entorno distribuido.

### 2.5. Persistencia

Se utiliza MySQL como base de datos propia del microservicio.

La información de reservas y sus detalles se persiste localmente. Para relacionar datos pertenecientes a otros microservicios se almacenan sus identificadores, evitando establecer relaciones JPA directas entre bases de datos diferentes.

La configuración actual utiliza `spring.jpa.hibernate.ddl-auto=update`.

Para un entorno productivo sería conveniente incorporar un mecanismo de migraciones de esquema y una estrategia de administración de cambios de base de datos.

### 2.6. Separación de responsabilidades

Se mantiene una arquitectura por capas:

- **Controllers:** reciben las solicitudes HTTP y exponen los endpoints.
- **Services:** implementan las reglas de negocio.
- **Repositories:** realizan las operaciones de persistencia.
- **Clients:** permiten la comunicación con otros microservicios.
- **Entities:** representan los datos persistidos.
- **DTOs:** definen los datos intercambiados mediante la API.

Esto permite separar las reglas de negocio de los detalles de comunicación y persistencia.

## 3. Decisiones sobre integraciones

### 3.1. Integración con msvc-compras

Reservas expone operaciones para confirmar y liberar reservas.

El microservicio de Compras cuenta con un cliente REST para comunicarse con Reservas y utilizar estas operaciones durante el proceso de compra.

La integración está implementada desde el lado de los clientes y endpoints involucrados.

**Pendiente de validar:** las pruebas de extremo a extremo deben comprobar el flujo completo entre ambos microservicios, incluyendo errores y reintentos.

### 3.2. Integración con msvc-eventos-lugares

Reservas expone operaciones para habilitar y cancelar reservas asociadas a eventos.

También consulta información de eventos, sectores y asientos mediante Feign.

**Pendiente de validar:** debe comprobarse el flujo completo iniciado desde Eventos y Lugares, incluyendo la notificación de cancelaciones y la habilitación de reservas.

### 3.3. Integración con msvc-entradas

Reservas contempla una operación para devolver una entrada y liberar la disponibilidad correspondiente.

La validación de las condiciones de devolución, como el estado de la entrada y los plazos permitidos, debe realizarse en coordinación con el microservicio responsable de esa funcionalidad.

**Pendiente de validar:** el flujo completo entre Entradas, Reservas y Compras debe probarse con los servicios reales.

### 3.4. Integración con API Gateway

La configuración del Gateway debe verificarse junto con el resto de los microservicios.

Las anotaciones de documentación que identifican ciertos endpoints como internos no constituyen por sí mismas un mecanismo de seguridad ni impiden que puedan invocarse directamente.

Por lo tanto, la protección y exposición de las operaciones internas deben validarse en la configuración efectiva del Gateway y de los servicios.

## 4. Limitaciones conocidas

Se identifican las siguientes limitaciones:

1. **Pruebas de integración:** existen pruebas unitarias y una prueba de concurrencia con MySQL, pero todavía debe comprobarse el funcionamiento de extremo a extremo con todos los microservicios reales.

2. **Concurrencia distribuida:** la prueba de concurrencia cubre solicitudes simultáneas sobre la persistencia utilizada, pero no demuestra por sí sola el comportamiento bajo múltiples instancias desplegadas.

3. **Actualización del estado de vencimiento:** no existe una tarea programada que actualice automáticamente todas las reservas vencidas. La disponibilidad se libera mediante la validación de la fecha de vencimiento, y el estado se actualiza cuando una operación accede a la reserva.

4. **Dependencia de Eventos y Lugares:** las operaciones que necesitan información de eventos, sectores, asientos o precios dependen de la disponibilidad del servicio externo.

5. **Seguridad de endpoints internos:** la identificación de endpoints como internos en Swagger no reemplaza la implementación de controles de acceso.

6. **Configuración de base de datos:** se utiliza actualización automática del esquema mediante Hibernate. Para producción se recomienda incorporar migraciones controladas.

7. **Manejo de fallas entre servicios:** los flujos distribuidos requieren pruebas adicionales ante caídas, demoras o respuestas inesperadas de otros microservicios.

## 5. Mejoras posibles

Como mejoras futuras se consideran:

- Incorporar pruebas de integración automatizadas entre los microservicios.
- Probar escenarios de concurrencia con múltiples instancias.
- Implementar mecanismos de seguridad para operaciones internas.
- Incorporar migraciones de base de datos.
- Mejorar la observabilidad mediante logs y métricas de las operaciones.
- Definir estrategias de reintentos y recuperación ante fallas de comunicación entre servicios.

Estas mejoras no deben confundirse con funcionalidades ya implementadas.

## 6. Criterio de alcance

Las decisiones y limitaciones descriptas reflejan el estado actual del microservicio y el alcance de la entrega.

Se distingue entre:

- **Implementado:** funcionalidad presente en el código.
- **Probado:** funcionalidad cubierta por pruebas unitarias, de integración o manuales según corresponda.
- **Pendiente de validar:** funcionamiento que requiere pruebas adicionales o coordinación con otros microservicios.
- **Mejora futura:** evolución posible que no forma parte de la implementación actual.