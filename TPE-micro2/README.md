# TicketYa — Plataforma de venta y gestión de entradas

## Descripción

TicketYa es una aplicación web para consultar eventos, reservar lugares, comprar entradas digitales y administrar espectáculos.

El sistema está desarrollado con una arquitectura de microservicios en Java y Spring Boot, y dispone de un frontend en React. La comunicación con los servicios se realiza mediante un API Gateway.

## Funcionalidades

- Registro e inicio de sesión con JWT y roles.
- Búsqueda y consulta de eventos.
- Gestión de lugares, sectores, asientos y precios.
- Reservas temporales con vencimiento de 10 minutos.
- Fila virtual para eventos de alta demanda.
- Compras y pagos mediante pasarela simulada.
- Emisión y validación de entradas digitales con QR.
- Devoluciones, reintegros y cancelaciones.
- Recomendaciones según preferencias e historial.
- Notificaciones por correo.
- Administración de eventos, usuarios y reportes.

## Arquitectura

| Componente | Puerto | Responsabilidad |
|---|---:|---|
| Usuarios | 8085 | Autenticación, usuarios y roles |
| Eventos y Lugares | 8081 | Catálogo, organización y precios |
| Reservas | 8080 | Disponibilidad y reservas |
| Compras y Pagos | 8082 | Compras, pagos y reintegros |
| Entradas | 8083 | Entradas, QR y devoluciones |
| Recomendaciones | 8086 | Preferencias y sugerencias |
| Notificaciones | 8087 | Correos y reintentos |
| Fila Virtual | 8088 | Control de acceso por turnos |
| API Gateway | 8084 | Enrutamiento y seguridad |

Cada microservicio administra sus datos y se comunica con los demás mediante APIs REST.

## Requisitos

- Docker Desktop con Docker Compose.
- Java 21 para generar la configuración inicial.
- Navegador web.

Maven y Node.js son necesarios si se desea ejecutar los servicios o el frontend fuera de Docker.

## Ejecución completa

Abrir una terminal PowerShell en la raíz `TPE-micro2`.

**1. Generar la configuración local**

Si todavía no existen `.env` y `.secrets/`, ejecutar:

```powershell
java scripts/GenerateDevSecrets.java
```

Este comando genera credenciales de desarrollo y las claves RSA utilizadas para JWT. Si la configuración ya existe, no debe volver a ejecutarse.

**2. Iniciar backend y frontend**

```powershell
docker compose -f docker-compose.yml -f frontend/compose.frontend.yml up -d --build
```

Esperar a que los servicios finalicen el arranque.

**3. Acceder**

| Aplicación | Dirección |
|---|---|
| TicketYa — Frontend | http://localhost:5174 |
| API Gateway | http://localhost:8084 |
| Swagger UI | http://localhost:8084/swagger-ui.html |
| Mailpit — Correos de prueba | http://localhost:8025 |

### Cuenta administradora

En una instalación nueva, el generador de configuración habilita la creación de un administrador inicial. El correo y la contraseña se encuentran en las variables `BOOTSTRAP_ADMIN_EMAIL` y `BOOTSTRAP_ADMIN_PASSWORD` del archivo local `.env`.

Estas credenciales son privadas.

Los usuarios pueden registrarse desde el frontend con rol `CLIENTE`. Los demás roles se asignan mediante las operaciones administrativas correspondientes.

## Detener el sistema

```powershell
docker compose -f docker-compose.yml -f frontend/compose.frontend.yml down
```

Este comando detiene los contenedores sin eliminar los volúmenes de datos.

## Pruebas

- **Backend:** pruebas automatizadas con JUnit, Mockito y Spring Boot Test.
- **Frontend:** pruebas con Vitest y Testing Library.
- **API:** colección `postman/TicketYa-Integracion.postman_collection.json`.
- **Carga:** plan, resultados e informe en `test-carga/`.
- **Observabilidad:** registros correlacionados y evidencias en `docs/evidence/`.

La pasarela de pagos es simulada. Los correos se reciben en Mailpit durante la ejecución local; no se envían a direcciones reales.

## Documentación

- [Funcionalidades e historias de usuario](docs/01-FEATURES-Y-USER-STORIES.md)
- [Arquitectura general](docs/02-ARQUITECTURA-GENERAL.md)
- [Secuencia de compra](docs/03-DIAGRAMA-SECUENCIA-COMPRA.md)
- [Contratos de integración](docs/05-CONTRATOS-DE-INTEGRACION.md)
- [Pruebas](docs/06-PLAN-DE-TESTING.md)
- [Observabilidad](docs/07-OBSERVABILIDAD.md)
- [Seguridad y permisos](docs/08-SEGURIDAD-Y-PERMISOS.md)

Cada microservicio incluye su documentación específica. Las APIs pueden explorarse desde el Swagger agrupado del Gateway.