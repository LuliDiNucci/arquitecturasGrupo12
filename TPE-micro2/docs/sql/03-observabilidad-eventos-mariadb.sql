-- Sólo para una base existente de Eventos que usa ddl-auto=validate.
-- Aplicar UNA VEZ antes de actualizar el servicio. El perfil MySQL de desarrollo
-- usa ddl-auto=update y agrega estas columnas automáticamente: no repetir allí.
-- Seleccionar primero la base de Eventos, sin eliminar ni recrear sus datos.
ALTER TABLE avisos_clientes_evento ADD COLUMN correlation_id VARCHAR(64) NULL;
ALTER TABLE notificaciones_cancelacion ADD COLUMN correlation_id VARCHAR(64) NULL;
