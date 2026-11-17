-- Sólo para bases existentes cuya creación de esquema no use Hibernate update.
-- Ejecutar una vez en ticketya_eventos; no hay FK entre bases de servicios.
ALTER TABLE organizadores ADD COLUMN usuario_id BIGINT NULL;
CREATE UNIQUE INDEX uk_organizador_usuario ON organizadores(usuario_id);
-- Asociar por PATCH /organizadores/{id}/usuario como ADMINISTRADOR.
-- Nunca poblar usuario_id copiando automáticamente organizadores.id.
