

# Ejecución y configuración — Usuarios

## Requisitos

- Java 21.
- Maven.
- Docker Desktop.
- MySQL 8 para ejecución con persistencia relacional.

El microservicio utiliza el puerto `8085`. El acceso externo de TicketYa se realiza mediante el API Gateway, en `8084`.

## Configuración inicial

Desde la raíz de `TPE-micro2`, ejecutar una única vez:

```powershell
java .\scripts\GenerateDevSecrets.java
```

Este comando genera:

- `.env`, con las credenciales locales.
- `.secrets/jwt-private.pem`.
- `.secrets/jwt-public.pem`.

El generador no sobrescribe una configuración existente.

Los archivos generados contienen información sensible y no deben incorporarse al repositorio.

## Ejecución con Docker

Para iniciar el microservicio Usuarios y sus dependencias:

```powershell
docker compose up -d --build usuarios
```

Para iniciar el sistema TicketYa completo:

```powershell
docker compose up -d --build
```

Comprobar el estado:

```powershell
docker compose ps
```

## Administrador inicial

En una base de usuarios vacía, el mecanismo de bootstrap permite crear el primer administrador.

La configuración se obtiene de:

- `BOOTSTRAP_ADMIN_ENABLED`
- `BOOTSTRAP_ADMIN_EMAIL`
- `BOOTSTRAP_ADMIN_PASSWORD`

Después de la creación inicial, se puede deshabilitar el bootstrap mediante su variable de configuración.

Las credenciales administrativas no se incluyen en la documentación ni en las colecciones Postman exportadas.

## Base de datos

Usuarios mantiene una base MySQL independiente.

Durante el desarrollo se utiliza actualización del esquema mediante Hibernate. La configuración base permite validar un esquema existente, cuya definición SQL se encuentra en `src/main/resources/db/schema-mysql.sql`.

Los tests automatizados utilizan H2 y claves RSA generadas para las pruebas.

## API y verificación

- Swagger: http://localhost:8085/swagger-ui/index.html
- OpenAPI: http://localhost:8085/v3/api-docs
- Gateway: http://localhost:8084

La colección `TicketYa-Usuarios-JWT.postman_collection.json` permite probar registro, autenticación, roles y respuestas de seguridad.