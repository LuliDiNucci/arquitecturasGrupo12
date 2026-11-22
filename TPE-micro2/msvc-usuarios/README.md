

# TicketYa — Microservicio Usuarios

## Descripción

`msvc-usuarios` gestiona la identidad, autenticación y administración de usuarios de TicketYa.

Está desarrollado con Java 21, Spring Boot 3.5.6, Spring Security y Spring Data JPA. Utiliza una base de datos MySQL propia y se ejecuta en el puerto `8085`.

## Funcionalidades

- Registro público de clientes.
- Autenticación mediante JWT firmado con RS256.
- Almacenamiento seguro de contraseñas mediante BCrypt.
- Administración de roles y habilitación de cuentas.
- Consulta de perfiles con control de titularidad.
- Emisión de tokens para comunicaciones internas entre microservicios.
- Protección contra intentos reiterados de inicio de sesión.

Los roles disponibles son `CLIENTE`, `ORGANIZADOR`, `ADMINISTRADOR` y `PERSONAL_ACCESO`.

La autenticación se integra con el API Gateway y los demás microservicios. Las solicitudes externas ingresan por el Gateway, mientras que cada servicio valida los JWT y aplica sus propias reglas de autorización.

## Ejecución

Desde la raíz de `TPE-micro2`, generar la configuración local si todavía no existe:

```powershell
java .\scripts\GenerateDevSecrets.java
```

Iniciar TicketYa:

```powershell
docker compose up -d --build
```

El generador crea `.env` y las claves JWT necesarias. Estos archivos contienen información sensible y están excluidos de Git.

## API

- Swagger: http://localhost:8085/swagger-ui/index.html
- OpenAPI: http://localhost:8085/v3/api-docs
- Gateway: http://localhost:8084

## Documentación

- [01 — Funcionalidades e historias de usuario](docs/01-ESPECIFICACION.md)
- [02 — Arquitectura y modelo de datos](docs/02-ARQUITECTURA.md)
- [03 — Permisos y seguridad](docs/03-PERMISOS.md)
- [04 — Contratos REST](docs/04-API.md)
- [05 — Ejecución y configuración](docs/05-EJECUCION.md)
- [06 — Pruebas](docs/06-PRUEBAS.md)

Las pruebas de API también están disponibles en las colecciones Postman del proyecto.