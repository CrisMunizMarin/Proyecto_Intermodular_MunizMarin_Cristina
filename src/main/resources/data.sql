-- =====================================================
-- MENTORCORE - Datos iniciales de prueba
-- Proyecto Intermodular 2º DAW - Cristina Muñiz Marín
-- =====================================================
-- IMPORTANTE: Las contraseñas están cifradas con BCrypt
-- Contraseña real de todos los usuarios: Admin1234
-- =====================================================

-- -----------------------------------------------------
-- USUARIOS DE PRUEBA (uno por cada rol)
-- -----------------------------------------------------
INSERT IGNORE INTO usuario 
    (nombre_usuario, email, password_hash, nombre, apellidos, telefono, rol, estado)
VALUES
-- Administrador del sistema
(
    'admin',
    'admin@mentorcore.com',
    '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewdBPj/RK.s5uMqC',
    'Administrador',
    'Sistema MentorCore',
    '600000001',
    'ADMIN',
    'ACTIVO'
),
-- Tutor Centro
(
    'tutorcentro1',
    'maria.garcia@laLaboral.edu',
    '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewdBPj/RK.s5uMqC',
    'María',
    'García López',
    '600000002',
    'TUTOR_CENTRO',
    'ACTIVO'
),
-- Tutor Empresa
(
    'tutorempresa1',
    'carlos.perez@empresa.com',
    '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewdBPj/RK.s5uMqC',
    'Carlos',
    'Pérez Martínez',
    '600000003',
    'TUTOR_EMPRESA',
    'ACTIVO'
),
-- Alumno
(
    'alumno1',
    'lucia.fernandez@alumno.com',
    '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewdBPj/RK.s5uMqC',
    'Lucía',
    'Fernández Ruiz',
    '600000004',
    'ALUMNO',
    'ACTIVO'
);

-- -----------------------------------------------------
-- EMPRESA DE PRUEBA
-- -----------------------------------------------------
INSERT IGNORE INTO empresa
    (nombre, cif, sector, direccion, municipio, provincia, telefono, email_contacto, estado)
VALUES
(
    'TechSolutions Asturias S.L.',
    'B33123456',
    'Tecnología y Desarrollo de Software',
    'Calle Covadonga 15, 3ºA',
    'Gijón',
    'Asturias',
    '985000001',
    'rrhh@techsolutions.com',
    'ACTIVA'
);

-- -----------------------------------------------------
-- CURSO ACADÉMICO DE PRUEBA
-- -----------------------------------------------------
INSERT IGNORE INTO curso_academico
    (codigo_curso, nombre, ciclo_formativo, nivel, anio_academico, activo)
VALUES
(
    '2VIFC303',
    '2º DAW Vespertino',
    'DAW vespertino',
    'SEGUNDO',
    '2025-2026',
    TRUE
);

-- -----------------------------------------------------
-- PERFIL TUTOR CENTRO
-- -----------------------------------------------------
INSERT IGNORE INTO tutor_centro
    (id_usuario, departamento, especialidad)
VALUES
(
    (SELECT id FROM usuario WHERE nombre_usuario = 'tutorcentro1'),
    'Informática y Comunicaciones',
    'Desarrollo de Aplicaciones Web'
);

-- -----------------------------------------------------
-- PERFIL TUTOR EMPRESA
-- -----------------------------------------------------
INSERT IGNORE INTO tutor_empresa
    (id_usuario, id_empresa, cargo, departamento_empresa)
VALUES
(
    (SELECT id FROM usuario WHERE nombre_usuario = 'tutorempresa1'),
    (SELECT id FROM empresa WHERE cif = 'B33123456'),
    'Jefe de Desarrollo',
    'Departamento de Tecnología'
);

-- -----------------------------------------------------
-- PERFIL ALUMNO
-- -----------------------------------------------------
INSERT IGNORE INTO alumno
    (id_usuario, id_curso, id_tutor_centro, grupo, horas_totales_fe, horas_completadas)
VALUES
(
    (SELECT id FROM usuario WHERE nombre_usuario = 'alumno1'),
    (SELECT id FROM curso_academico WHERE codigo_curso = '2VIFC303'),
    (SELECT id FROM usuario WHERE nombre_usuario = 'tutorcentro1'),
    '2VIFC303-A',
    400,
    0.00
);

-- -----------------------------------------------------
-- PERIODO DE FORMACIÓN DE PRUEBA
-- -----------------------------------------------------
INSERT IGNORE INTO periodo_formacion
    (id_curso, tipo, anio_academico, fecha_inicio, fecha_fin, horas_totales, estado, id_creado_por)
VALUES
(
    (SELECT id FROM curso_academico WHERE codigo_curso = '2VIFC303'),
    'ORDINARIO',
    '2025-2026',
    '2026-01-12',
    '2026-03-27',
    400,
    'ACTIVO',
    (SELECT id FROM usuario WHERE nombre_usuario = 'admin')
);

-- -----------------------------------------------------
-- PARÁMETROS GLOBALES DEL SISTEMA
-- -----------------------------------------------------
INSERT IGNORE INTO parametro_sistema
    (clave, valor, descripcion, tipo_dato)
VALUES
(
    'horas_totales_fe',
    '400',
    'Número total de horas de Formación en Empresa requeridas',
    'INTEGER'
),
(
    'max_faltas_injustificadas',
    '3',
    'Número máximo de faltas injustificadas permitidas',
    'INTEGER'
),
(
    'email_noreply',
    'mentorcore.noreply@gmail.com',
    'Email remitente para notificaciones automáticas',
    'STRING'
),
(
    'nombre_centro',
    'CIFP La Laboral',
    'Nombre del centro educativo',
    'STRING'
),
(
    'curso_activo',
    '2025-2026',
    'Año académico activo en el sistema',
    'STRING'
);

-- -----------------------------------------------------
-- TIPOS DE DOCUMENTO
-- -----------------------------------------------------
INSERT IGNORE INTO tipo_documento
    (nombre, descripcion, es_obligatorio, rol_responsable, extensiones_permitidas, activo)
VALUES
(
    'Convenio de Formación en Empresa',
    'Documento legal que formaliza la relación entre el centro y la empresa',
    TRUE,
    'TUTOR_CENTRO',
    'pdf',
    TRUE
),
(
    'DNI / NIE del Alumno',
    'Documento nacional de identidad del alumno',
    TRUE,
    'ALUMNO',
    'pdf,jpg,png',
    TRUE
),
(
    'Seguro Escolar',
    'Comprobante del seguro escolar vigente',
    TRUE,
    'ALUMNO',
    'pdf',
    TRUE
),
(
    'Anexo I - Plan de Formación',
    'Documento con las actividades y resultados de aprendizaje previstos',
    TRUE,
    'TUTOR_CENTRO',
    'pdf,docx',
    TRUE
),
(
    'Justificante de Falta',
    'Documento justificativo de ausencia del alumno',
    FALSE,
    'ALUMNO',
    'pdf,jpg,png',
    TRUE
);