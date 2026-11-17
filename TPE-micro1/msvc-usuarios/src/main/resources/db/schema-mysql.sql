-- Inicialización manual para el perfil prod (ddl-auto=validate). Dev usa update.
CREATE TABLE usuarios (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    apellido VARCHAR(100) NOT NULL,
    email VARCHAR(254) NOT NULL,
    password_hash VARCHAR(100) NOT NULL,
    activo BIT NOT NULL,
    fecha_creacion DATETIME(6) NOT NULL,
    version BIGINT,
    CONSTRAINT uk_usuario_email UNIQUE (email)
);
CREATE TABLE usuario_roles (
    usuario_id BIGINT NOT NULL,
    rol ENUM('CLIENTE','ORGANIZADOR','ADMINISTRADOR','PERSONAL_ACCESO') NOT NULL,
    CONSTRAINT uk_usuario_rol UNIQUE (usuario_id, rol),
    CONSTRAINT fk_roles_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
);
