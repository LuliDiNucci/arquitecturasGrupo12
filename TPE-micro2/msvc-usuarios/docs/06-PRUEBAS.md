

# Pruebas y validación — Usuarios

## Objetivo

Verificar el registro de usuarios, autenticación JWT, autorización por roles y titularidad, protección de credenciales y funcionamiento de las operaciones administrativas.

## Pruebas automatizadas

El microservicio utiliza JUnit 5, Spring Boot Test y H2 para las pruebas de persistencia.

| Suite | Pruebas | Funcionalidades verificadas |
|---|---:|---|
| `AuthSecurityIntegrationTest` | 14 | Registro, login, administración de usuarios, permisos, BCrypt y endpoints |
| `JwtValidationTest` | 16 | Firma RSA, vencimiento, claims y rechazo de JWT inválidos |
| `LoginAttemptLimiterTest` | 1 | Control de intentos de autenticación |
| **Total** | **31** | **Sin fallos ni errores** |

Los resultados corresponden a la ejecución final de Maven realizada durante la verificación del proyecto.

## Casos principales

Se verificaron:

- Registro válido y rechazo de correos duplicados.
- Asignación exclusiva del rol `CLIENTE` durante el registro público.
- Autenticación con credenciales correctas e incorrectas.
- Rechazo de usuarios inactivos.
- Emisión y validación de JWT RS256.
- Rechazo de tokens inválidos, alterados o vencidos.
- Restricciones de acceso según rol y titularidad.
- Administración de roles y estados.
- Separación entre credenciales de usuario y de servicio.
- Limitación de intentos fallidos de autenticación.

## Ejecución

Desde la raíz del proyecto:

```powershell
mvn -B -pl msvc-usuarios -am test
```

## Pruebas de integración

También se verificó el funcionamiento de Usuarios junto con el Gateway y los demás microservicios mediante solicitudes HTTP autenticadas.

La colección principal de Postman permitió ejecutar el registro, inicio de sesión y asignación de roles utilizados posteriormente en los flujos de compra, devolución y validación de entradas.

Las pruebas comprobaron que las solicitudes autorizadas se procesan correctamente y que las operaciones protegidas rechazan identidades sin permisos suficientes.