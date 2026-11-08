# 05 — Testing del microservicio de Reservas

**Proyecto:** TicketYa · `msvc-reservas` · Entrega 1

## 1. Objetivo

Este documento describe la estrategia de pruebas utilizada para verificar las reglas de negocio, la persistencia, la concurrencia y el comportamiento de los endpoints del microservicio de Reservas.

Se distinguen tres niveles:

- **Pruebas unitarias:** verifican reglas y comportamientos de clases individuales.
- **Pruebas de integración con MySQL:** verifican persistencia y concurrencia utilizando una base de datos real de pruebas.
- **Pruebas manuales:** verifican los flujos HTTP y el comportamiento observable mediante herramientas como PowerShell, MySQL y un simulador de Eventos y Lugares.

La existencia de una prueba escrita no implica que se haya ejecutado en todos los entornos. Los resultados indicados se limitan a las verificaciones realizadas durante el desarrollo y a las pruebas manuales informadas por el equipo.

## 2. Herramientas utilizadas

| Herramienta | Uso |
| JUnit 5 | Ejecución de pruebas automatizadas |
| Mockito | Simulación de dependencias |
| Spring Boot Test | Configuración del contexto de pruebas |
| Spring Data JPA | Acceso a repositorios |
| MySQL | Persistencia y pruebas de concurrencia |
| PowerShell | Envío de solicitudes HTTP y comprobaciones manuales |
| Postman | Pruebas manuales de endpoints |
| Simulador de Eventos | Respuesta local a las consultas Feign durante las pruebas |

## 3. Pruebas unitarias

La clase principal de pruebas de servicio es `ReservaServiceImplTest`.

Se verifican, entre otros, los siguientes escenarios:

| Prueba | Comportamiento esperado |
| `rechazaSolicitudIncompletaSinGuardar` | Una solicitud incompleta se rechaza y no se persiste |
| `rechazaReservaSiEventoCanceladoLocalmente` | No se permite reservar un evento cancelado |
| `noConfirmaReservaVencida` | Una reserva vencida no puede confirmarse |
| `cancelacionMarcaElEventoYCancelaActivas` | La cancelación marca el evento y cancela las reservas temporales activas |

Estas pruebas utilizan dependencias simuladas cuando corresponde, por lo que permiten verificar reglas de negocio sin depender de todos los microservicios reales.

### Ejecución

Desde la carpeta `msvc-reservas`:

```powershell
mvn test
```

Este comando ejecuta las pruebas unitarias configuradas para el proyecto.

## 4. Prueba de concurrencia con MySQL

Se incluye la clase `ConcurrenciaMySQLIT`, que verifica el comportamiento ante solicitudes simultáneas sobre un mismo asiento.

### Escenario

- Se utiliza una base MySQL exclusiva para pruebas.
- Se prepara un evento y un asiento de prueba.
- Se envían diez solicitudes concurrentes para reservar el mismo asiento.
- Se comprueba que solo una solicitud consiga crear la reserva.

**Resultado esperado:** una sola reserva exitosa; las restantes deben rechazarse por falta de disponibilidad o por conflicto de concurrencia.

La prueba permite verificar que el bloqueo pesimista sobre `bloqueo_evento` y las transacciones de MySQL protegen la ocupación del asiento.

### Configuración segura

Primero se debe crear una base de datos separada:

```sql
CREATE DATABASE IF NOT EXISTS ticketya_reservas_test;
```

En PowerShell, configurar las variables:

```powershell
$env:TICKETYA_MYSQL_TEST="true"
$env:TEST_DB_URL="jdbc:mysql://localhost:3306/ticketya_reservas_test"
$env:TEST_DB_USER="root"
$env:TEST_DB_PASSWORD="TU_CONTRASENA"
```

Ejecutar únicamente esta prueba:

```powershell
mvn "-Dtest=ConcurrenciaMySQLIT" test
```

**Importante:** no ejecutar esta prueba contra una base con datos importantes. El escenario prepara y limpia datos de prueba.

La prueba se ejecutó durante el desarrollo con MySQL real y obtuvo el resultado esperado. La verificación con varias instancias reales del microservicio conectadas simultáneamente a la misma base queda como prueba adicional.

## 5. Pruebas manuales de endpoints

Durante el desarrollo se realizaron verificaciones mediante PowerShell, MySQL y un simulador de Eventos y Lugares.

Los escenarios informados por el equipo incluyen:

| Escenario | Verificación buscada |
| Crear reserva | Se crea una reserva temporal y se registra su vencimiento |
| Consultar reserva | Se obtiene la información y el estado de la reserva |
| Consultar disponibilidad | Se informa la cantidad o los asientos disponibles |
| Confirmar reserva | Una reserva activa y vigente pasa a confirmada |
| Liberar reserva | Una reserva temporal se libera y sus lugares vuelven a estar disponibles |
| Vencimiento | Al cumplirse los 10 minutos, los lugares dejan de considerarse ocupados |
| Cancelar evento | Se bloquean nuevas reservas y se cancelan las reservas temporales activas |
| Devolución individual | Se registra la liberación de una entrada y se evitan duplicaciones |
| Idempotencia | Los reintentos no generan reservas o liberaciones duplicadas |
| Errores HTTP | Se verifican respuestas ante solicitudes inválidas o conflictos de negocio |

### Vencimiento: aclaración importante

El equipo informó que verificó manualmente que, después de cumplirse el plazo de 10 minutos, la capacidad vuelve a estar disponible para nuevas reservas.

Esto se consigue mediante el vencimiento lógico: las consultas de ocupación excluyen las reservas activas cuya `fechaVencimiento` ya pasó.

Por eso, el registro puede permanecer temporalmente con estado `ACTIVA` en MySQL aunque el lugar ya esté disponible. Una operación posterior puede actualizarlo a `VENCIDA`.

**No se requiere un Scheduler para liberar capacidad en Entrega 1.** La actualización periódica del estado persistido es independiente de la disponibilidad efectiva.

## 6. Integraciones entre microservicios

Las pruebas locales con simuladores no reemplazan las pruebas integradas contra los servicios reales.

| Integración | Estado de implementación | Verificación pendiente |
| Reservas → Eventos y Lugares | Cliente Feign implementado | Compatibilidad de rutas y DTO con el servicio real |
| Compras → Reservas | Cliente de Compras y endpoints de confirmación/liberación desarrollados | Flujo completo de pago aprobado y rechazado |
| Eventos y Lugares → Reservas | Endpoints de habilitación y cancelación desarrollados | Notificación real y coordinación de cancelación |
| Entradas → Reservas | Endpoint de liberación individual disponible | Devolución aprobada y coordinación con los servicios responsables |
| Gateway → Reservas | No verificado en este documento | Protección de endpoints internos y acceso autorizado |

No se afirma que estas integraciones estén completamente probadas de extremo a extremo mientras no exista evidencia de ejecución conjunta.

## 7. Limitaciones conocidas

- US01 está parcialmente cubierta: para sectores numerados se informan los asientos disponibles, pero no se devuelve una lista explícita de asientos ocupados.
- La prueba de concurrencia con MySQL no equivale a una prueba distribuida con varias instancias reales.
- Las pruebas con simulador no demuestran compatibilidad completa con Eventos y Lugares.
- La coordinación completa de pago, confirmación, emisión de entradas, devolución y cancelación requiere pruebas integradas.
- La seguridad de los endpoints internos debe comprobarse en Gateway y/o en la configuración de red.
- El vencimiento lógico libera capacidad, pero el estado persistido puede actualizarse a `VENCIDA` posteriormente.

## 8. Criterios de aceptación y evidencia

| Requisito | Evidencia disponible | Estado |
| Consultar disponibilidad | Pruebas manuales de disponibilidad | Parcial: faltan ocupados explícitos |
| Crear reserva temporal | Pruebas manuales y unitarias | Implementado localmente |
| Evitar doble reserva | `ConcurrenciaMySQLIT` | Verificado con MySQL; falta prueba con varias instancias |
| Liberar por vencimiento | Verificación manual informada por el equipo | Verificado funcionalmente con vencimiento lógico |
| Confirmar por pago aprobado | Endpoint y cliente de Compras desarrollados | Falta prueba integrada |
| Liberar por pago rechazado | Endpoint y cliente de Compras desarrollados | Falta prueba integrada |
| Devolución y cancelación | Endpoints desarrollados y pruebas manuales locales | Falta prueba integrada con servicios reales |

## 9. Conclusión

Las pruebas desarrolladas permiten verificar las reglas principales del microservicio de Reservas, la persistencia local, la idempotencia y la protección frente a solicitudes simultáneas sobre un mismo asiento.

La prueba de concurrencia con MySQL aporta evidencia sobre el comportamiento transaccional. Las pruebas manuales permiten comprobar los flujos principales, incluido el vencimiento lógico y la liberación de capacidad.

Para cerrar la validación de Entrega 1 es necesario completar las pruebas integradas entre microservicios y documentar sus resultados, además de resolver el criterio pendiente de US01.

**Documentación relacionada:** [01 — Funcionalidades e historias de usuario](01-FUNCIONALIDADES-Y-USER-STORIES.md) · [02 — Arquitectura y diagramas](02-ARQUITECTURA-Y-DIAGRAMAS-RESERVAS.md) · [03 — Modelo de datos](03-MODELO-DE-DATOS-RESERVAS.md) · [06 — Decisiones y limitaciones](06-DECISIONES-Y-LIMITACIONES-RESERVAS.md).