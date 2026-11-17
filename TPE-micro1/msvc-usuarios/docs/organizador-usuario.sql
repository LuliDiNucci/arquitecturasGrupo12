-- Para instalaciones de Eventos que utilizan ddl-auto=validate.
-- Con el perfil mysql actual (ddl-auto=update), Hibernate agrega esta columna.
-- Ejecutar UNA vez, después de respaldar la base y revisar su esquema.
ALTER TABLE organizadores ADD COLUMN usuario_id BIGINT NULL;
ALTER TABLE organizadores ADD CONSTRAINT uk_organizador_usuario UNIQUE (usuario_id);
-- La vinculación se realiza por PATCH /organizadores/{id}/usuario como ADMINISTRADOR.
