-- Crear tabla para las Comunidades Autónomas de España
CREATE TABLE IF NOT EXISTS regions (
   id INT AUTO_INCREMENT PRIMARY KEY,
   code VARCHAR(10) NOT NULL UNIQUE,
   name VARCHAR(100) NOT NULL
);

-- Crear tabla para los Usuarios
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(40) NOT NULL UNIQUE,
    passwordHash VARCHAR(500) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    accountNonLocked BOOLEAN NOT NULL DEFAULT TRUE,
    lastPasswordChange DATETIME NULL,
    passwordExpiresAt DATETIME NULL,
    failedLoginAttempts INT DEFAULT 0,
    emailVerified BOOLEAN NOT NULL DEFAULT FALSE,
    mustChangePassword BOOLEAN NOT NULL DEFAULT FALSE
);