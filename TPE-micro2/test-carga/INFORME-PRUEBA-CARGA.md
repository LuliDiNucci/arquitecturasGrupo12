# Informe de prueba de carga — TicketYa

## 1. Objetivo

Evaluar el comportamiento del sistema TicketYa ante la ejecución concurrente del flujo completo de compra de entradas, verificando los tiempos de respuesta, los errores HTTP y la consistencia de la disponibilidad.

## 2. Entorno de prueba

- Herramienta: Apache JMeter 5.6.3.
- Sistema operativo: Windows 11.
- Java: JDK 21.
- Arquitectura: microservicios Spring Boot ejecutados mediante Docker Compose.
- Microservicios involucrados: Usuarios, Eventos y Lugares, Reservas, Compras y Pagos, y Entradas.
- Evento utilizado: ID 4.
- Sector: Campo General, ID 2.
- Capacidad del sector: 20 lugares.
- Tipo de sector: no numerado.

## 3. Configuración

Se configuró un grupo de 10 usuarios virtuales, con un período de incorporación de 1 segundo y una ejecución por usuario.

Cada usuario realizó el siguiente circuito:

1. Registro de cliente.
2. Inicio de sesión y obtención de JWT.
3. Creación de reserva temporal.
4. Creación de compra.
5. Procesamiento de pago mediante pasarela simulada.
6. Confirmación de compra y emisión de entrada.

Cada usuario utilizó una identidad propia y claves de idempotencia diferentes.

La prueba se ejecutó mediante JMeter en modo no gráfico.

## 4. Resultados

| Operación | Solicitudes | Errores | Tiempo promedio |
|---|---:|---:|---:|
| Registro de cliente | 10 | 0 | 353,50 ms |
| Inicio de sesión | 10 | 0 | 297,00 ms |
| Creación de reserva | 10 | 0 | 503,20 ms |
| Creación de compra | 10 | 0 | 457,30 ms |
| Procesamiento de pago | 10 | 0 | 5.265,70 ms |
| Confirmación de compra | 10 | 0 | 190,00 ms |
| **Total** | **60** | **0** | **1.177,78 ms** |

### Métricas generales

- Usuarios virtuales: 10.
- Solicitudes ejecutadas: 60.
- Duración aproximada: 8 segundos.
- Tiempo mínimo de respuesta: 137 ms.
- Tiempo máximo de respuesta: 5.288 ms.
- Tiempo promedio general: 1.177,78 ms.
- Porcentaje de errores HTTP: 0 %.

## 5. Verificación de consistencia

Después de la ejecución, se consultó el reporte del evento y su disponibilidad.

Se obtuvieron los siguientes valores acumulados, que incluyen operaciones anteriores a la prueba:

- Entradas vendidas: 12.
- Entradas devueltas: 1.
- Entradas vigentes: 11.
- Lugares ocupados: 11.
- Lugares disponibles: 9.
- Porcentaje de ocupación: 55 %.
- Importe pagado: $240.000.
- Importe reintegrado: $20.000.
- Recaudación neta: $220.000.

Los resultados son consistentes con la capacidad del sector y con las devoluciones registradas.

## 6. Análisis

La prueba finalizó sin errores HTTP en las 60 solicitudes ejecutadas.

El procesamiento del pago fue la operación de mayor duración, con un tiempo promedio de 5.265,70 ms. Las demás operaciones registraron promedios inferiores a 504 ms.

La prueba demuestra que el sistema pudo ejecutar el circuito de compra bajo la carga ensayada. No permite establecer por sí sola el límite máximo de usuarios concurrentes soportados por la aplicación.

## 7. Conclusión

TicketYa completó satisfactoriamente la prueba de carga de 10 usuarios virtuales concurrentes, con un total de 60 solicitudes HTTP y una tasa de errores del 0 %.

La verificación posterior de disponibilidad, ocupación y recaudación mostró valores consistentes.

El principal punto identificado para un posible análisis de rendimiento posterior es el tiempo de procesamiento de la pasarela de pagos simulada.

## 8. Evidencia

- Plan de prueba JMeter: `TicketYa-Carga.jmx`.
- Resultados: `resultados-20261120-150717.jtl`.
- Informe HTML generado automáticamente por Apache JMeter.