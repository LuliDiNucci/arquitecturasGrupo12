# Contrato de comunicación con msvc-eventos-lugares

## 1. Objetivo

Este documento describe la comunicación entre `msvc-reservas` y `msvc-eventos-lugares`.

El microservicio de Reservas utiliza un cliente Feign (`EventoClient`) para consultar información de eventos, sectores, asientos y precios.

Además, expone operaciones que permiten habilitar o cancelar las reservas de un evento.

## 2. Operaciones de consulta implementadas

El cliente `EventoClient` implementa las siguientes operaciones:

| Operación Feign | Método / ruta | Información requerida |
| `obtenerEvento` | `GET /eventos/{id}` | Identificador, nombre, fecha y hora, estado y lugar. |
| `obtenerSector` | `GET /sectores/{id}` | Identificador, nombre, tipo de sector, capacidad y lugar. |
| `obtenerAsiento` | `GET /asientos/{id}` | Identificador, fila, número y sector. |
| `obtenerAsientosPorSector` | `GET /sectores/{id}/asientos` | Lista de asientos pertenecientes al sector. |
| `obtenerPrecioSector` | `GET /eventos/{eventoId}/sectores/{sectorId}/precio` | Precio del sector para el evento indicado. |

Los datos concretos intercambiados deben respetar los DTOs y formatos definidos por ambos servicios.

Para la consulta de precios, la relación entre el evento y el sector debe existir. Si no existe, se espera una respuesta HTTP 404.

## 3. Configuración de comunicación

La URL del microservicio de Eventos y Lugares se configura mediante la propiedad:

`msvc.eventos.url`

En el entorno local se utiliza:

`http://localhost:8081`

En otros entornos debe configurarse la dirección correspondiente al servicio desplegado.

## 4. Operaciones expuestas por Reservas para Eventos

El microservicio de Reservas expone las siguientes operaciones:

| Operación | Método / ruta | Propósito |
| Cancelar reservas de un evento | `POST /reservas/evento/{eventoId}/cancelar` | Cancelar las reservas activas asociadas al evento. |
| Habilitar reservas de un evento | `POST /reservas/evento/{eventoId}/habilitar` | Habilitar la posibilidad de realizar reservas para el evento. |

Estas operaciones forman parte de la comunicación interna entre microservicios.

La existencia de los endpoints en Reservas no demuestra por sí sola que el servicio de Eventos ya los invoque correctamente. Es necesario validar esa comunicación mediante pruebas de integración.

## 5. Condiciones para la integración

### 5.1. Publicación y habilitación de reservas

Antes de permitir la venta de entradas de un evento, el servicio responsable de Eventos y Lugares debe coordinar la habilitación de las reservas mediante:

`POST /reservas/evento/{eventoId}/habilitar`

La operación de habilitación debe comprobar que el evento exista, esté publicado y no haya sido cancelado.

La repetición de una solicitud de habilitación debe mantener un resultado consistente, sin generar registros duplicados.

### 5.2. Cancelación de eventos

Cuando se cancela un evento, el servicio de Eventos y Lugares debe coordinar la cancelación de las reservas mediante:

`POST /reservas/evento/{eventoId}/cancelar`

La cancelación debe impedir que se sigan generando reservas para ese evento.

También debe coordinarse con Compras y Entradas para resolver las compras y entradas afectadas.

**Importante:** como los microservicios utilizan bases de datos independientes, no existe una transacción global que garantice automáticamente la atomicidad de todas las operaciones. Se requiere coordinación y manejo de posibles fallas parciales.

### 5.3. Confirmación y liberación de reservas

El microservicio de Compras utiliza las operaciones de confirmación y liberación de Reservas según el resultado del pago.

Si el pago se aprueba, se confirma la reserva. Si se rechaza, se libera.

Si un pago se aprueba cuando la reserva ya venció, Compras debe contemplar un mecanismo de reversión o compensación para evitar confirmar una operación que ya no dispone de los recursos reservados.

### 5.4. Devolución de entradas

En el flujo de devolución, Entradas debe validar las condiciones correspondientes, incluyendo:

- Titularidad de la entrada.
- Identificador de la entrada.
- Estado de la entrada.
- Plazo permitido de 72 horas.
- Aprobación de la solicitud.

Cuando corresponda, se solicita la liberación de una entrada individual en Reservas y se coordina el reintegro con Compras.

Las reglas de aprobación y validación de la devolución pertenecen al servicio responsable de esa funcionalidad, no al cliente Feign de Eventos.

### 5.5. Concurrencia y disponibilidad

Reservas utiliza mecanismos de bloqueo sobre el evento para coordinar las operaciones que modifican su disponibilidad.

La implementación utiliza la entidad de bloqueo del evento (`bloqueo_evento`) y cuenta con una prueba de concurrencia sobre MySQL para verificar que múltiples solicitudes sobre un mismo asiento no produzcan reservas duplicadas.

Esta prueba no reemplaza la validación del comportamiento con múltiples instancias desplegadas.

### 5.6. Seguridad de operaciones internas

Las operaciones internas de Reservas no deben quedar expuestas a clientes arbitrarios a través del API Gateway.

La documentación de Swagger que identifica un endpoint como interno no constituye un mecanismo de seguridad.

La protección efectiva debe verificarse en la configuración del Gateway y en los controles de acceso de los servicios.

## 6. Estado de implementación e integración

| Elemento | Estado |
| Cliente Feign `EventoClient` | Implementado en Reservas. |
| Consultas de eventos, sectores y asientos | Implementadas en el cliente Feign. |
| Consulta de precios por evento y sector | Implementada en el cliente Feign. |
| Endpoint para cancelar reservas de un evento | Implementado en Reservas. |
| Endpoint para habilitar reservas de un evento | Implementado en Reservas. |
| Comunicación efectiva con Eventos y Lugares | Requiere validación integrada. |
| Flujo completo de cancelación entre servicios | Requiere validación integrada. |
| Flujo completo de habilitación y publicación | Requiere validación integrada. |
| Protección de endpoints internos mediante Gateway | Requiere verificación de la configuración conjunta. |

## 7. Consideraciones finales

El cliente Feign y los endpoints necesarios para la comunicación están implementados en `msvc-reservas`.

Sin embargo, la integración entre microservicios debe evaluarse considerando el comportamiento conjunto, especialmente ante errores de comunicación, cancelaciones, vencimientos y operaciones concurrentes.

Las funcionalidades implementadas localmente no deben considerarse automáticamente verificadas de extremo a extremo hasta contar con pruebas que involucren los servicios reales.