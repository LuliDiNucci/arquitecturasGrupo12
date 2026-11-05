# Resultado de verificación de la revisión

**Fecha:** 4 de noviembre de 2026. **Insumos:** `mandarCHAT.zip` y `TPE-ArqWeb-TicketYa-2026 (1).pdf`.

## Verificaciones realizadas

- El proyecto contiene los **6 servicios Java** separados: Lugar, Sector, Asiento, Evento, Organizador y Participante.
- No aparecen referencias a `CatalogoService` ni `PersonasController`.
- Los DTO principales son **clases Java tradicionales**, con constructores, getters y setters; no quedan accesores de compatibilidad con `record`.
- Se contabilizaron **26 endpoints** REST; los 26 poseen documentación de resumen y respuesta de éxito en annotations Swagger, y se declaran respuestas 400/404/409 donde corresponde.
- Se revisaron las reglas de publicación, asientos numerados, precio por evento, filtros y transiciones de estado.
- Se añadieron avisos de cancelación pendientes también para **Compras**, además de Reservas y Entradas.
- Hay **25 métodos de prueba `@Test`** escritos, repartidos en 5 clases de test (unitarias y pruebas de API interna).
- Se generó una colección Postman con **37 solicitudes** agrupadas; los JSON con variables sustituidas se analizaron correctamente.
- Un analizador del compilador JDK 21 procesó los **66 archivos Java** (main + test) sin reportar errores de sintaxis.
- `scripts/cargar_datos_prueba.py` pasa la comprobación de sintaxis de Python.

## Qué no se pudo verificar aquí

No había **Maven** instalado en el contenedor, ni acceso al repositorio de dependencias para descargarlo. Por eso:

- **No** se ejecutó `mvn clean test`.
- **No** se probó el contexto de Spring, la ejecución de Swagger o los controladores contra la base de datos.
- **No** se probó el flujo entre Eventos, Reservas, Compras y Entradas (el ZIP incluía solo Eventos).
- **No** se simuló la concurrencia real de compra que exige el TPE; es una prueba grupal, especialmente de Reservas.

El resultado es **revisión estática y preparación de pruebas**, no una certificación de compilación ni funcionamiento de un sistema integrado. Antes de entregar es obligatorio ejecutar `mvn clean test` en un entorno con Maven/JDK 21 y revisar los resultados. Si hay errores de compilación o test, corregirlos antes de la entrega.
