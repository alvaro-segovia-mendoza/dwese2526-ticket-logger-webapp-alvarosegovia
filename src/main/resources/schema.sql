-- Crear tabla para las Comunidades Autónomas de España
CREATE TABLE IF NOT EXISTS regions (
   id INT AUTO_INCREMENT PRIMARY KEY,
   code VARCHAR(10) NOT NULL UNIQUE,
   name VARCHAR(100) NOT NULL
);

-- Crear tabla users si no existe
CREATE TABLE IF NOT EXISTS users (
   id BIGINT AUTO_INCREMENT PRIMARY KEY,
   email VARCHAR(100) NOT NULL UNIQUE,
   password_hash VARCHAR(500) NOT NULL,
   active BOOLEAN NOT NULL DEFAULT TRUE,
   account_non_locked BOOLEAN NOT NULL DEFAULT TRUE,
   last_password_change DATETIME NULL,
   password_expires_at DATETIME NULL,
   failed_login_attempts INT DEFAULT 0,
   email_verified BOOLEAN NOT NULL DEFAULT FALSE,
   must_change_password BOOLEAN NOT NULL DEFAULT FALSE
);

-- Crear tabla para perfil de usuario relación 1:1 con users
CREATE TABLE IF NOT EXISTS user_profiles (
   -- Clave primaria = FK a users.id  (1:1 tipo "shared primary key")
   user_id BIGINT NOT NULL,
   first_name      VARCHAR(60)  NOT NULL,
   last_name       VARCHAR(80)  NOT NULL,
   -- Teléfono como texto (por prefijos, espacios, etc.)
   phone_number    VARCHAR(30)  NULL,
   -- Ruta/URL de la imagen de perfil (no el binario)
   profile_image   VARCHAR(255) NULL,
   -- Otros campos típicos de perfil
   bio             VARCHAR(500) NULL,              -- pequeña descripción / sobre mí
   locale          VARCHAR(10)  NULL,              -- es_ES, en_US...
   created_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
   updated_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
                                   ON UPDATE CURRENT_TIMESTAMP,
   -- PRIMARY KEY obligatorio antes de FK en shared primary key
   CONSTRAINT pk_user_profiles PRIMARY KEY (user_id),
   -- Foreign key hacia users.id
   CONSTRAINT fk_user_profiles_user
       FOREIGN KEY (user_id)
       REFERENCES users(id)
       ON DELETE CASCADE
       ON UPDATE CASCADE
);


-- Crear tabla para las provincias españolas
CREATE TABLE IF NOT EXISTS provinces (
   id INT AUTO_INCREMENT PRIMARY KEY,
   code VARCHAR(10) NOT NULL UNIQUE,
   name VARCHAR(100) NOT NULL,
   region_id INT NOT NULL,
   FOREIGN KEY (region_id) REFERENCES regions(id)
);

-- Tabla de roles
CREATE TABLE IF NOT EXISTS roles (
                                     id BIGINT AUTO_INCREMENT PRIMARY KEY,
    -- Nombre técnico que usaremos en Spring Security: ROLE_ADMIN, ROLE_USER...
                                     name VARCHAR(50) NOT NULL UNIQUE,
    -- Nombre legible para la interfaz
    display_name VARCHAR(100) NOT NULL,
    -- Descripción opcional del rol
    description VARCHAR(255) NULL
    );


-- Tabla intermedia N:M entre users y roles
CREATE TABLE IF NOT EXISTS user_roles (
                                          user_id BIGINT NOT NULL,
                                          role_id BIGINT NOT NULL,
    -- Clave primaria compuesta: un usuario no puede tener un rol repetido
                                          CONSTRAINT pk_user_roles PRIMARY KEY (user_id, role_id),
    -- FK a users
    CONSTRAINT fk_user_roles_user
    FOREIGN KEY (user_id)
    REFERENCES users(id)
    ON DELETE CASCADE
    ON UPDATE CASCADE,
    -- FK a roles
    CONSTRAINT fk_user_roles_role
    FOREIGN KEY (role_id)
    REFERENCES roles(id)
    ON DELETE CASCADE
    ON UPDATE CASCADE
    );
