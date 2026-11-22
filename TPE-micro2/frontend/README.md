# TicketYa - Guía de uso

TicketYa es una plataforma web que permite buscar eventos, reservar lugares, comprar entradas digitales y gestionar espectáculos.

## 1. Ejecutar la aplicación

1. Abrir Docker Desktop.
2. Abrir el proyecto en VS Code.
3. Desde la terminal, en la carpeta principal del proyecto, ejecutar:

```powershell
docker compose -f docker-compose.yml -f frontend/compose.frontend.yml up -d --build
```

4. Esperar a que los servicios se inicien.
5. Ingresar desde el navegador a **http://localhost:5174**.

## 2. Funcionalidades del cliente

1. **Registrarse o iniciar sesión** desde la página principal.
2. **Buscar eventos** utilizando los filtros disponibles.
3. **Seleccionar un evento**, elegir sector y asientos o cantidad de entradas.
4. **Realizar una reserva temporal** y completar el pago simulado antes de que venza.
5. **Consultar las entradas compradas** desde la sección «Mis entradas», donde se muestra su código QR.
6. **Solicitar devoluciones** cuando se cumplan las condiciones establecidas.
7. **Configurar preferencias** y consultar recomendaciones personalizadas.

Para eventos de alta demanda, el cliente debe ingresar a la fila virtual y esperar a que se habilite su turno.

## 3. Funcionalidades según el rol

- **Organizador:** crear, modificar y publicar eventos propios, configurar precios y consultar reportes.
- **Administrador:** gestionar eventos, lugares, sectores, organizadores, participantes, usuarios, devoluciones y fila virtual.
- **Personal de acceso:** escanear códigos QR o ingresarlos manualmente para validar entradas.

Los roles deben estar previamente asignados por un administrador.

## 4. Notificaciones

Los correos de prueba generados por el sistema pueden consultarse en:

**http://localhost:8025**

Mailpit permite visualizar las notificaciones sin enviarlas a correos reales.

## 5. Consideraciones

- Todos los datos se obtienen de los microservicios mediante el API Gateway.
- Para visualizar eventos, deben existir eventos publicados en la base de datos.
- Las compras utilizan una pasarela de pagos simulada.
- Cada operación respeta los permisos y las validaciones del backend.