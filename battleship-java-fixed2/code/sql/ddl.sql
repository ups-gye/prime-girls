-- ═══════════════════════════════════════════════════════════
--  Batalla Naval - DDL PostgreSQL
--  Ejecutar en tu base de datos Neon (o PostgreSQL local)
-- ═══════════════════════════════════════════════════════════

-- Tabla de usuarios (JPA la crea automáticamente con hbm2ddl=update,
-- pero se incluye aquí como respaldo)
CREATE TABLE IF NOT EXISTS usuarios (
    id                SERIAL PRIMARY KEY,
    username          VARCHAR(50)  NOT NULL UNIQUE,
    password          VARCHAR(50)  NOT NULL,
    nombre            VARCHAR(100),
    apellido          VARCHAR(100),
    avatar            VARCHAR(100),
    partidas_ganadas  INTEGER DEFAULT 0,
    partidas_perdidas INTEGER DEFAULT 0,
    puntos_totales    INTEGER DEFAULT 0
);

-- Índice para búsquedas rápidas por username
CREATE INDEX IF NOT EXISTS idx_usuarios_username ON usuarios(username);

-- Usuario administrador de prueba (contraseña: admin123)
INSERT INTO usuarios (username, password, nombre, apellido, avatar, partidas_ganadas, partidas_perdidas, puntos_totales)
VALUES ('admin', 'admin123', 'Administrador', 'Sistema', 'captain-1', 0, 0, 0)
ON CONFLICT (username) DO NOTHING;

-- Usuarios de prueba
INSERT INTO usuarios (username, password, nombre, apellido, avatar)
VALUES
    ('luis.almeida',  'pass123', 'Luis',   'Almeida',  'captain-1'),
    ('maria.perez',   'pass123', 'María',  'Pérez',    'captain-2'),
    ('henry.paz',     'pass123', 'Henry',  'Paz',      'captain-3'),
    ('monica.pita',   'pass123', 'Mónica', 'Pita',     'captain-4'),
    ('gina.alcides',  'pass123', 'Gina',   'Alcides',  'captain-5'),
    ('angel.torres',  'pass123', 'Ángel',  'Torres',   'captain-6')
ON CONFLICT (username) DO NOTHING;
