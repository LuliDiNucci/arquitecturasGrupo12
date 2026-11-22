# Estrategia de pruebas — TicketYa

## Objetivo

Verificar las reglas de negocio, la comunicación entre microservicios, la seguridad y el funcionamiento de los principales circuitos del sistema.

## Niveles de prueba

| Nivel | Herramientas | Alcance |
|---|---|---|
| Unitarias | JUnit y Mockito | Reglas de negocio aisladas |
| Integración | Spring Boot Test y WebTestClient | Persistencia, APIs y comunicación |
| API | Postman | Flujos HTTP completos |
| Frontend | Vitest y Testing Library | Renderizado y navegación |
| Carga | Apache JMeter | Solicitudes concurrentes |
| Observabilidad | Grafana y Loki | Registros, errores y correlación |

## Escenarios principales

- Publicación y modificación de eventos.
- Validación de disponibilidad y prevención de sobreventa.
- Creación y vencimiento de reservas.
- Compra y pago aprobado o rechazado.
- Confirmación de compras y emisión de entradas.
- Validación única de códigos QR.
- Devoluciones y reintegros.
- Cancelaciones de eventos.
- Autenticación, autorización y titularidad.
- Fila Virtual, recomendaciones y notificaciones.

## Pruebas de integración con Postman

La colección `postman/TicketYa-Integracion.postman_collection.json` contiene **89 solicitudes** para recorrer los principales procesos y verificar respuestas HTTP.

Se complementa con la guía ubicada en `postman/GUIA_PRUEBAS_POSTMAN.md`.

## Pruebas del frontend

El frontend incluye pruebas automatizadas para:

- Visualización del catálogo.
- Redirección de rutas privadas sin sesión.
- Manejo de rutas inexistentes.

Se ejecutan mediante:

```bash
npm test
```

## Prueba de carga

Se utilizó Apache JMeter 5.6.3 con diez usuarios virtuales para ejecutar el flujo de registro, autenticación, reserva, compra, pago y confirmación.

Resultados registrados:

| Métrica | Resultado |
|---|---:|
| Usuarios virtuales | 10 |
| Solicitudes HTTP | 60 |
| Errores HTTP | 0 |
| Tiempo promedio general | 1.177,78 ms |

El informe y los archivos de evidencia se encuentran en `test-carga/`.

La prueba verifica el comportamiento bajo la carga ensayada, sin establecer el máximo de usuarios que soporta el sistema.

## Ejecución automatizada

Desde la raíz del backend:

```bash
mvn clean test
```

Desde `frontend/`:

```bash
npm test
```

Los casos específicos y sus resultados se detallan en la documentación de testing de cada microservicio.