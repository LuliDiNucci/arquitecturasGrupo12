# 04 - Contratos REST

## 1. Descripción general

El microservicio `msvc-recomendaciones` expone operaciones REST para gestionar las preferencias de los clientes y consultar recomendaciones de eventos.

La API utiliza JSON como formato de intercambio de información.

## 2. URL base

En el entorno local, el microservicio se ejecuta en:

`http://localhost:8086`

## 3. Consulta de recomendaciones

### GET /api/recomendaciones/{clienteId}

Permite consultar las recomendaciones asociadas a un cliente.

**Parámetros de ruta:**

| Parámetro | Tipo | Descripción |
|---|---|---|
| clienteId | Long | Identificador del cliente para el cual se solicitan recomendaciones |

**Ejemplo de solicitud:**

```http
GET http://localhost:8086/api/recomendaciones/1
```

**Respuesta esperada:**

La operación devuelve la información de los eventos recomendados para el cliente solicitado, según sus preferencias y los eventos publicados disponibles.

La estructura exacta de la respuesta depende del DTO o modelo utilizado por el controlador.

## 4. Gestión de preferencias

El microservicio también contempla operaciones para registrar y consultar las preferencias de los clientes.

Estas operaciones permiten almacenar los intereses que luego se utilizan durante el proceso de recomendación.

Las rutas, métodos HTTP, parámetros y estructuras de respuesta deben completarse según las operaciones efectivamente expuestas por `PreferenciasController`.

## 5. Códigos de respuesta

Los siguientes códigos describen situaciones habituales que deben verificarse en las pruebas de la API:

| Código | Descripción |
|---|---|
| 200 | Solicitud procesada correctamente |
| 400 | Solicitud con datos inválidos |
| 404 | Recurso o información solicitada no encontrada |
| 500 | Error interno durante el procesamiento |

Los códigos concretos que devuelve cada operación dependen de su implementación.

## 6. Documentación Swagger

La API puede consultarse mediante Swagger UI en:

`http://localhost:8086/swagger-ui.html`

Swagger permite visualizar los endpoints, sus parámetros, los esquemas de datos y ejecutar solicitudes de prueba.

