# Pruebas — Recomendaciones

## Estrategia

Se utilizan pruebas automatizadas con JUnit, Mockito y Spring Security Test para verificar la selección de eventos, la seguridad y la comunicación con servicios externos.

El proyecto incluye **10 tests**, organizados en cuatro clases.

## Pruebas automatizadas

| Clase | Casos verificados |
|---|---|
| `RecomendacionesServiceTest` | Recomendación por historial; exclusión de eventos vencidos o no publicados; recomendaciones por preferencias ante fallos de Compras |
| `ClientesInteresadosServiceTest` | Coincidencia de artista y ciudad; ausencia de coincidencias |
| `RecommendationSecurityTest` | Rechazo de accesos sin autorización; validación de tokens y roles; acceso del propietario y del administrador |
| `EventosFeignConfigTest` | Reintento ante error `503` en consultas GET; ausencia de reintento para POST |

Las pruebas de servicios utilizan objetos simulados para aislar la lógica de negocio. Las pruebas de seguridad verifican respuestas HTTP mediante el controlador y la configuración de autenticación.

## Pruebas de integración documentadas

Se probaron los siguientes escenarios de funcionamiento mediante solicitudes REST y consultas a MongoDB:

- Registro, consulta y modificación de preferencias.
- Persistencia de los datos del cliente en MongoDB.
- Obtención de eventos publicados desde Eventos y Lugares.
- Recomendaciones por tipo, ciudad y artista favorito.
- Resultado vacío cuando no existen coincidencias.
- Recomendación de un evento sugerido por el historial de compras confirmadas.
- Ausencia de eventos duplicados en los resultados.
- Rechazo de consultas sin JWT o dirigidas a otro cliente.

## Ejecución

Desde la carpeta `msvc-recomendaciones`:

```bash
mvn clean test
```

Las pruebas automatizadas están ubicadas en `src/test/java/com/ticketya/recomendaciones`.