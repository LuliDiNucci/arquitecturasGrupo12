# Contratos a coordinar con el equipo

La seguridad de los endpoints actuales y de los clientes HTTP está implementada.
Estas decisiones de datos y funcionalidades futuras requieren acuerdo:

1. **IDs históricos:** para datos existentes, mapear cada clienteId de Reservas,
   Compras y Entradas a Usuario.id. No registrar un usuario esperando que su
   autoincremental coincida con un cliente antiguo. Bases nuevas no necesitan esa
   migración; los IDs se obtienen del registro y de sub.
2. **Organizadores:** asignar rol ORGANIZADOR en Usuarios, vincular por
   PATCH /organizadores/{id}/usuario como ADMINISTRADOR y renovar el JWT. Eventos
   no consulta la base de Usuarios ni asume igualdad entre ambos IDs. Antes de guardar,
   consulta la API REST con el JWT administrativo propagado por Feign y exige que
   el usuario exista, esté activo y tenga ORGANIZADOR. Una validación fallida o
   Usuarios no disponible impide modificar el vínculo.
3. **Infraestructura de lugares:** la edición física compartida es administrativa.
   El organizador elige sectores/precios de su propio evento. Si el equipo agrega
   propiedad de lugares, debe diseñar también esa autorización antes de abrirla.
4. **PERSONAL_ACCESO y QR:** el enum y sus authorities existen. No hay controller
   de validación/uso de entradas en el servicio actual. Al implementarlo, crear un
   DTO mínimo para el resultado, restricción PERSONAL_ACCESO/ADMINISTRADOR,
   asignación por evento y una transición de uso atómica. No reutilizar GET de
   tickets completos como endpoint de personal de acceso.
5. **Reportes, preferencias y fila virtual:** aún no existen sus controllers.
   Deben incorporarse explícitamente a la matriz y filtros de Gateway y servicio,
   con dueño de recurso. El comportamiento por defecto es denegar.
6. **Despliegue:** distribuir la clave privada sólo a Usuarios; clave pública,
   issuer y audience iguales en todos. Cada servicio recibe únicamente su secreto.
   Las URLs internas se configuran por entorno; no agregar permitAll para evitar
   errores de integración. Los Dockerfile y Compose están preparados; falta
   ejecutar MySQL/contenedores en un equipo con motor Docker iniciado.
7. **Operación:** no hay revocación inmediata ni contador de login distribuido.
   Para réplicas y suspensión instantánea, acordar un almacenamiento compartido
   para intentos/revocaciones y pruebas correspondientes. No se presentan como
   funcionalidades de esta entrega.
