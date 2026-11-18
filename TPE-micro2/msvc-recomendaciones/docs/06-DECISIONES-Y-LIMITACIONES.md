# 06 - Decisiones y Limitaciones

## 1. Decisiones de diseño

### 1.1. Uso de MongoDB

Se eligió MongoDB para almacenar las preferencias de los clientes.

Esta tecnología permite trabajar con documentos y mantener la información de preferencias separada de los datos administrados por otros microservicios.

### 1.2. Separación de responsabilidades

El microservicio de Recomendaciones administra sus propias preferencias y su lógica de recomendación.

No almacena ni administra los eventos, ya que esa responsabilidad corresponde al microservicio de Eventos y Lugares.

### 1.3. Comunicación mediante OpenFeign

Se utiliza Spring Cloud OpenFeign para consultar los eventos publicados en `msvc-eventosylugares`.

De esta manera, Recomendaciones obtiene la información necesaria sin acceder directamente a la base de datos de Eventos.

### 1.4. Configuración externa

La URL del microservicio de Eventos se configura mediante la variable de entorno `EVENTOS_URL`, permitiendo adaptar la dirección del servicio según el entorno de ejecución.

## 2. Limitaciones identificadas

### 2.1. Dependencia de Eventos y Lugares

La generación de recomendaciones depende de la disponibilidad del microservicio de Eventos y Lugares y de que este proporcione eventos publicados.

Si no existen eventos publicados, no hay información disponible para generar recomendaciones basadas en eventos.

### 2.2. Verificación de la integración

Durante las pruebas se consultó el endpoint de eventos publicados, que devolvió una lista vacía.

Por este motivo, quedó pendiente verificar el funcionamiento integral de la generación de recomendaciones utilizando eventos reales.

### 2.3. Pruebas de integración pendientes

Es necesario realizar una prueba completa que incluya:

1. Registrar las preferencias de un cliente.
2. Crear y publicar un evento en el microservicio de Eventos y Lugares.
3. Consultar las recomendaciones del cliente.
4. Verificar que los eventos devueltos coincidan con sus preferencias.

## 3. Trabajo pendiente

- Completar las pruebas de integración con eventos publicados.
- Verificar los resultados de las recomendaciones para distintos clientes.
- Documentar los resultados obtenidos en Swagger.
- Comprobar el comportamiento cuando no existen eventos publicados.

## 4. Conclusión

El microservicio de Recomendaciones cuenta con una estructura propia de persistencia, gestión de preferencias y comunicación con el microservicio de Eventos y Lugares.

La principal limitación encontrada durante las pruebas fue la falta de eventos publicados disponibles para verificar el resultado de las recomendaciones.

La funcionalidad de integración quedó implementada, pero su comportamiento completo debe validarse mediante pruebas con datos reales.