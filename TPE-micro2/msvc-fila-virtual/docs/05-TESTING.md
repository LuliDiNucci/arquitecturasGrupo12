# 05 — Pruebas y testing

## Objetivo

Las pruebas de Fila Virtual verifican el ingreso de clientes, la asignación de posiciones, la habilitación de tandas, el vencimiento de turnos, la concurrencia y la autorización de operaciones.

Se utilizan JUnit 5, Mockito, Spring Boot Test, MockMvc y H2.

## Pruebas automatizadas

| Clase | Cobertura |
|---|---|
| `FilaVirtualServiceImplTest` | Reglas de ingreso, habilitación, validación, vencimiento y finalización |
| `FilaVirtualConcurrenciaTest` | Ingresos simultáneos y habilitaciones concurrentes |
| `FilaVirtualSecurityTest` | Autenticación, roles, titularidad y acceso interno |
| `MsvcFilaVirtualApplicationTests` | Inicialización del contexto Spring Boot |

## Casos principales

### Funcionalidad

- El ingreso repetido conserva el registro y su posición.
- Se rechazan eventos que no requieren fila virtual.
- Los clientes se habilitan respetando el orden de llegada.
- No se permite una nueva tanda mientras exista otra activa.
- Los turnos vencidos no autorizan reservas.
- La finalización es idempotente.
- Los turnos finalizados no pueden reutilizarse.

### Concurrencia

- Dos ingresos simultáneos del mismo cliente no generan duplicados.
- Dos habilitaciones simultáneas no producen tandas incompatibles.

### Seguridad

- Se rechazan solicitudes sin JWT válido.
- Un cliente no puede ingresar en nombre de otra persona.
- Un cliente no puede consultar registros ajenos ni habilitar tandas.
- Un administrador puede habilitar tandas.
- Solo el servicio Reservas puede acceder a la finalización interna de turnos.

## Entorno de pruebas

Las pruebas de negocio utilizan Mockito para simular dependencias externas.

Las pruebas de concurrencia utilizan Spring Boot y persistencia transaccional con H2 configurada en modo compatible con MySQL.

Los flujos completos entre Fila Virtual y Reservas se verifican mediante solicitudes HTTP sobre los servicios ejecutados con Docker Compose.

## Ejecución

Desde la carpeta `msvc-fila-virtual`:

```bash
mvn test
```

El comando ejecuta las pruebas automatizadas y genera los reportes de Maven.

## Integración con Reservas

La colección Postman general de TicketYa permite verificar:

1. Ingreso de un cliente a una fila de alta demanda.
2. Consulta de su posición.
3. Habilitación de un turno.
4. Rechazo de una reserva sin autorización.
5. Creación de una reserva con turno vigente.
6. Finalización de la autorización utilizada.
7. Rechazo de una nueva reserva con un turno finalizado.

## Conclusión

Las pruebas verifican las principales reglas de negocio, seguridad y concurrencia del microservicio.

La integración con Reservas permite controlar el acceso a la creación de reservas temporales para eventos de alta demanda.