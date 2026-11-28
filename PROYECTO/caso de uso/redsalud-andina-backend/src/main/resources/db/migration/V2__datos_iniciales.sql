-- Datos semilla para demostración local en PostgreSQL (usuarios ABAC)
INSERT INTO identidad.usuarios_clinicos (usuario_id, rol, centro_id, especialidad, activo, version) VALUES
    ('MED-RURAL-1', 'MEDICO_RURAL', 'CENTRO-01', 'GENERAL',     TRUE, 0),
    ('ESP-CARD-1',  'ESPECIALISTA', 'CENTRO-02', 'CARDIOLOGIA', TRUE, 0),
    ('ADMIN-1',     'ADMIN',        'CENTRO-01', 'NINGUNA',     TRUE, 0)
ON CONFLICT (usuario_id) DO NOTHING;
