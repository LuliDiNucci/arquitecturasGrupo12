# 05 — Pruebas y testing

## Objetivo

Las pruebas verifican las reglas de emisión, consulta, validación, devolución e invalidación de entradas, además de los controles de autenticación y autorización.

Se utilizan JUnit 5, Mockito, MockMvc y Spring Boot Test.

## Pruebas implementadas

| Clase de prueba | Funcionalidades verificadas |
|---|---|
| `EntradaServiceTest` | Solicitudes inválidas, emisión repetida y titularidad de entradas |
| `EntradaServiceImplTest` | Emisión, idempotencia, compra y reserva confirmadas, invalidación y validación QR |
| `DevolucionServiceImplTest` | Plazo de 72 horas, duplicados, aprobación, liberación y rechazo |
| `QrCodeServiceImplTest` | Generación de QR en formato PNG/Base64 y diferenciación de códigos |
| `InternalCredentialsTest` | Credenciales utilizadas en las comunicaciones internas |
| `ServiceSecurityIntegrationTest` | Validación de JWT, roles, titularidad y permisos de endpoints |
| `MsvcEntradasApplicationTests` | Integración HTTP, asignación de personal, validación de QR y restricciones de acceso |

## Escenarios destacados

**Emisión:**
- Una entrada por lugar reservado.
- Códigos QR únicos.
- Repetición de emisión sin duplicaciones.
- Rechazo cuando la compra o reserva no cumple las condiciones requeridas.

**Validación de acceso:**
- Validación correcta y cambio a `UTILIZADA`.
- Rechazo de códigos inexistentes, utilizados o invalidados.
- Rechazo de entradas correspondientes a otro evento.
- Personal no asignado sin permisos para validar.
- Asignación y revocación de personal por un administrador.

**Devoluciones:**
- Solicitud válida con más de 72 horas de anticipación.
- Rechazo de solicitudes fuera del plazo.
- Prevención de solicitudes pendientes duplicadas.
- Aprobación con liberación del lugar y solicitud de reintegro.
- Rechazo sin modificar la disponibilidad del lugar.

**Seguridad:**
- Rechazo de JWT ausentes o inválidos.
- Autorización por roles.
- Prevención de consultas sobre entradas de otros clientes.
- Restricción de operaciones internas a los servicios autorizados.

## Estrategia de integración

Las pruebas unitarias utilizan dependencias simuladas. Las pruebas de integración HTTP utilizan MockMvc y una base H2 de pruebas para verificar los controles de seguridad y el comportamiento de la API.

La colección Postman general de TicketYa permite comprobar los flujos completos con los microservicios ejecutándose mediante Docker.

## Ejecución

Desde la carpeta `msvc-entradas`:

```bash
mvn test
```

Los resultados se generan en los reportes de Maven.

## Conclusión

El microservicio cuenta con pruebas automatizadas de emisión, devolución, generación y validación QR, integración HTTP y seguridad.

La colección Postman complementa estas pruebas para verificar la interacción real entre Entradas, Compras, Reservas y Eventos y Lugares.