# Plan de testing — msvc-eventos, entrega 1

## Cómo correr las pruebas

Desde la raíz del proyecto, con **JDK 21** y Maven instalado:

```bash
mvn clean test
```

Para compilar sin correr tests:

```bash
mvn -DskipTests package
```

Para ejecutarlo localmente y probar Postman:

```bash
mvn spring-boot:run
```

La suite de API utiliza `application-test.yml` y H2 **en memoria**, separado del archivo `ticketya-eventos.mv.db` del servicio local.

## Casos implementados en src/test

| Caso | Archivo | Qué verifica |
|---|---|---|
| Publicar sin sectores | EventoServiceTest | 409, mantiene BORRADOR |
| Publicar sin precios | EventoServiceTest | 409 |
| Publicar con asientos faltantes | EventoServiceTest | 409 |
| Publicar configuración completa | EventoServiceTest | PUBLICADO |
| Cancelar dos veces | EventoServiceTest | Solo un aviso persistido |
| Modificar fecha de publicado | EventoServiceTest | 409 |
| Evento AGOTADO | EventoServiceTest | No habilitado para reservar |
| Búsqueda por precio mínimo | EventoServiceTest | Orden correcto |
| Fechas invertidas | EventoServiceTest | 400 |
| Precio de sector ajeno | EventoServiceTest | 400 |
| Sector no numerado | AsientoServiceTest | Rechaza creación de asientos |
| Superar capacidad | AsientoServiceTest | Rechazo |
| Duplicado en request | AsientoServiceTest | Rechazo |
| Duplicado ya persistido | AsientoServiceTest | Rechazo |
| Fila normalizada | AsientoServiceTest | Convierte a mayúsculas |
| Búsqueda y cancelación por HTTP | EventoApiIntegrationTest | 200/estado/filtros |
| Publicación completa con sector numerado | EventoApiIntegrationTest | 409 → 200 |
| Avisos para tres servicios | EventoApiIntegrationTest | Se crea outbox |
| Duplicación de asientos por HTTP | EventoApiIntegrationTest | 409 |
| Filtros por participante y fecha | EventoApiIntegrationTest | Coincidencia y exclusión |
| Crear sector y evitar duplicados | SectorServiceTest | 201 y conflicto |
| Avisos idempotentes a 3 servicios | NotificadorCancelacionesTest | Simula respuestas exitosas, una excepcion de Entradas y las integraciones deshabilitadas |

**Alcance real:** pruebas unitarias y pruebas de API dentro de un único servicio. No son pruebas de integración **entre microservicios**. Las clases están preparadas pero su ejecución debe verificarse en una instalación con Maven y dependencias.

## Pruebas manuales con Postman

1. Importar `postman/TicketYa-Eventos.postman_collection.json`.
2. Ejecutar la carpeta **Carga y publicación**, en orden, para que se guarden `organizadorId`, `participanteId`, `lugarId`, `sectorNumeradoId`, `sectorCampoId` y `eventoId`.
3. Revisar que cada creación devuelva 201, y las lecturas/acciones correctas 200.
4. Confirmar que `GET /eventos/buscar?ciudad=Tandil` devuelve el evento después de publicar.
5. Ejecutar las carpetas de consultas y cancelación; verificar que al cancelar cambia el estado y que los avisos quedan pendientes si `INTEGRACIONES_HABILITADAS=false`.

## Casos negativos adicionales sugeridos

- Precio negativo y coordenadas fuera de rango: **400**.
- ID de lugar, organizador, participante o evento inexistente: **404**.
- Publicar sin sectores, participantes, precios o asientos completos: **409**.
- Cambiar fecha o lugar de un evento PUBLICADO: **409**.
- Volver a publicar un evento CANCELADO: **409**.
- Buscar `desde > hasta`: **400**.
- Confirmar que el mismo sector de un mismo lugar puede tener **precios diferentes** en dos eventos.

## Prueba de concurrencia del TPE: responsabilidad de Reservas

La consigna exige lanzar **muchos pedidos simultáneos por el mismo asiento o último lugar** y verificar **una única compra exitosa**. Eventos **no puede garantizarlo** porque no registra las reservas ni las ventas. El equipo de Reservas debe probar con transacciones/bloqueos o actualización atómica en su base, incluso con varias instancias.

**Escenario esperado del equipo**: crear evento con un asiento; arrancar N clientes en paralelo; comprobar que exactamente 1 reserva/compra resulta confirmada y que N-1 fallan, sin duplicar pagos ni entradas. Guardar en la entrega del grupo el código de la prueba, N, cantidad de exitosas, evidencias y logs.

No se adjuntan resultados inventados de esa prueba porque la implementación de Reservas/Compras no se recibió en este ZIP.

## Checklist de evidencias antes de entregar

- Salida real de `mvn clean test` sin errores.
- Capturas o export de Postman mostrando POST, PATCH, GET, fallos 400/404/409.
- Swagger funcionando con todas las rutas y modelos de DTO.
- Prueba de consumo desde Reservas de `habilitado` y `sectores` cuando el grupo integre.
- Prueba grupal de concurrencia de compras (documentada por Reservas/Compras).
