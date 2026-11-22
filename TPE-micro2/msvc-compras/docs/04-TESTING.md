# 04 — Pruebas y testing

## Objetivo

Las pruebas de `msvc-compras` verifican la creación y confirmación de compras, el procesamiento de pagos, la idempotencia, los reintegros, la concurrencia y los controles de seguridad.

Se utilizan JUnit 5, Mockito, Spring Boot Test, MockMvc y MySQL.

## Pruebas automatizadas

| Clase | Cobertura |
|---|---|
| `CompraServiceImplTest` | Cálculo de importes, creación de ítems, compras duplicadas y confirmación con emisión de entradas |
| `PagoServiceImplTest` | Idempotencia, reutilización incorrecta de claves y prevención de cobros duplicados |
| `ReintegroServiceImplTest` | Reintegros individuales, parciales, cancelaciones repetidas y compensaciones sin entradas |
| `InternalCredentialsTest` | Uso correcto de credenciales internas y rechazo de llamadas sin credenciales configuradas |
| `ServiceSecurityIntegrationTest` | Validación de JWT, permisos por rol, titularidad y restricciones de acceso a las operaciones |

Las pruebas de negocio utilizan mocks para simular las dependencias externas.

Las pruebas de seguridad utilizan MockMvc y tokens de prueba para verificar las respuestas de autorización.

## Concurrencia

La clase `PagoConcurrenciaMySQLIT` contiene dos escenarios de integración:

1. Dos solicitudes simultáneas para una misma compra y clave de idempotencia.
2. Dos compras diferentes que intentan utilizar simultáneamente la misma clave.

Estas pruebas verifican que solamente se efectúe el cobro permitido y que la persistencia mantenga la integridad de los datos.

Utilizan MySQL con una base de pruebas denominada `ticketya_compras_test`. Las tablas se crean y eliminan durante la ejecución de esta clase.

## Ejecución

Desde la carpeta del microservicio:

**Pruebas habituales:**

```bash
mvn test
```

**Pruebas de concurrencia con MySQL:**

```bash
mvn -Dtest=PagoConcurrenciaMySQLIT test
```

Para las pruebas de concurrencia debe estar disponible una instancia MySQL con la configuración definida en la clase de prueba.

## Pruebas de integración REST

La colección Postman del proyecto permite comprobar el circuito completo a través del API Gateway:

- Creación de compra desde una reserva vigente.
- Pago aprobado y rechazado.
- Confirmación de reserva y emisión de entradas.
- Reintentos de pago y prevención de duplicados.
- Consulta de compras y estados.
- Reintegros individuales y por cancelación.
- Reintentos de compensación y liberación.
- Autorización mediante JWT y roles.
- Consultas internas para reportes y recomendaciones.

La colección complementa las pruebas automatizadas al verificar las interacciones entre microservicios durante la ejecución del sistema.

## Resultado

El microservicio dispone de pruebas automatizadas de negocio y seguridad, además de pruebas específicas de concurrencia con MySQL.

Las operaciones principales también se comprueban mediante pruebas de integración HTTP con la colección Postman general de TicketYa.