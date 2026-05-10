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
    (nombre_usuario, email, password_hash, nombre, apellidos, telefono, rol, estado, fecha_creacion)
VALUES
(
    'admin',
    'admin@mentorcore.com',
    '$2a$12$rAM9dfgvHRrrPlZLsr3oBuBtCMs6vQXebumyxPccF0HvacTdKeLfG',
    'Administrador',
    'Sistema MentorCore',
    '600000001',
    'ADMIN',
    'ACTIVO',
    CURRENT_TIMESTAMP
),
(
    'tutorcentro1',
    'maria.garcia@laLaboral.edu',
    '$2a$12$rAM9dfgvHRrrPlZLsr3oBuBtCMs6vQXebumyxPccF0HvacTdKeLfG',
    'María',
    'García López',
    '600000002',
    'TUTOR_CENTRO',
    'ACTIVO',
    CURRENT_TIMESTAMP
),
(
    'tutorempresa1',
    'carlos.perez@empresa.com',
    '$2a$12$rAM9dfgvHRrrPlZLsr3oBuBtCMs6vQXebumyxPccF0HvacTdKeLfG',
    'Carlos',
    'Pérez Martínez',
    '600000003',
    'TUTOR_EMPRESA',
    'ACTIVO',
    CURRENT_TIMESTAMP
),
(
    'alumno1',
    'lucia.fernandez@alumno.com',
    '$2a$12$rAM9dfgvHRrrPlZLsr3oBuBtCMs6vQXebumyxPccF0HvacTdKeLfG',
    'Lucía',
    'Fernández Ruiz',
    '600000004',
    'ALUMNO',
    'ACTIVO',
    CURRENT_TIMESTAMP
);

-- Si el usuaio ya existe en la BD, esta sentencia los actualiza
UPDATE usuario
SET password_hash = '$2a$12$rAM9dfgvHRrrPlZLsr3oBuBtCMs6vQXebumyxPccF0HvacTdKeLfG'
WHERE nombre_usuario IN ('admin', 'tutorcentro1', 'tutorempresa1', 'alumno1');

-- Perfil extendido del administrador
INSERT INTO administrador
    (id_usuario)
SELECT
    (SELECT id FROM usuario WHERE nombre_usuario = 'admin')
WHERE NOT EXISTS (
    SELECT 1
    FROM administrador
    WHERE id_usuario = (SELECT id FROM usuario WHERE nombre_usuario = 'admin')
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
),
(
    '1IFC302',
    '1º DAM Diurno',
    'DAM diurno',
    'PRIMERO',
    '2025-2026',
    TRUE
),
(
    '2IFC302',
    '2º DAM Diurno',
    'DAM diurno',
    'SEGUNDO',
    '2025-2026',
    TRUE
),
(
    '1VIFC303',
    '1º DAW Vespertino',
    'DAW vespertino',
    'PRIMERO',
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
INSERT INTO periodo_formacion
    (id_curso, tipo, anio_academico, fecha_inicio, fecha_fin, horas_totales, estado, id_creado_por)
SELECT
    (SELECT id FROM curso_academico WHERE codigo_curso = '2VIFC303'),
    'ORDINARIO',
    '2025-2026',
    '2026-01-12',
    '2026-03-27',
    400,
    'ACTIVO',
    (SELECT id FROM usuario WHERE nombre_usuario = 'admin')
WHERE NOT EXISTS (
    SELECT 1
    FROM periodo_formacion
    WHERE id_curso = (SELECT id FROM curso_academico WHERE codigo_curso = '2VIFC303')
      AND tipo = 'ORDINARIO'
      AND anio_academico = '2025-2026'
      AND fecha_inicio = '2026-01-12'
      AND fecha_fin = '2026-03-27'
);

INSERT INTO periodo_formacion
    (id_curso, tipo, anio_academico, fecha_inicio, fecha_fin, horas_totales, estado, id_creado_por)
SELECT
    (SELECT id FROM curso_academico WHERE codigo_curso = '2IFC302'),
    'ORDINARIO',
    '2025-2026',
    '2026-01-19',
    '2026-04-10',
    400,
    'PLANIFICADO',
    (SELECT id FROM usuario WHERE nombre_usuario = 'admin')
WHERE NOT EXISTS (
    SELECT 1
    FROM periodo_formacion
    WHERE id_curso = (SELECT id FROM curso_academico WHERE codigo_curso = '2IFC302')
      AND tipo = 'ORDINARIO'
      AND anio_academico = '2025-2026'
      AND fecha_inicio = '2026-01-19'
      AND fecha_fin = '2026-04-10'
);

INSERT INTO periodo_formacion
    (id_curso, tipo, anio_academico, fecha_inicio, fecha_fin, horas_totales, estado, id_creado_por)
SELECT
    (SELECT id FROM curso_academico WHERE codigo_curso = '1VIFC303'),
    'EXTRAORDINARIO',
    '2025-2026',
    '2026-05-04',
    '2026-07-03',
    400,
    'PLANIFICADO',
    (SELECT id FROM usuario WHERE nombre_usuario = 'admin')
WHERE NOT EXISTS (
    SELECT 1
    FROM periodo_formacion
    WHERE id_curso = (SELECT id FROM curso_academico WHERE codigo_curso = '1VIFC303')
      AND tipo = 'EXTRAORDINARIO'
      AND anio_academico = '2025-2026'
      AND fecha_inicio = '2026-05-04'
      AND fecha_fin = '2026-07-03'
);

-- -----------------------------------------------------
-- ASIGNACIÓN DE PRUEBA
-- Vincula al alumno con la empresa y tutor de empresa
-- para que los flujos de tutor-empresa sean operativos.
-- -----------------------------------------------------
INSERT INTO asignacion
    (id_alumno, id_empresa, id_tutor_empresa, id_periodo, fecha_inicio, estado)
SELECT
    (SELECT id FROM usuario WHERE nombre_usuario = 'alumno1'),
    (SELECT id FROM empresa WHERE cif = 'B33123456'),
    (SELECT id FROM usuario WHERE nombre_usuario = 'tutorempresa1'),
    (SELECT id
     FROM periodo_formacion
     WHERE id_curso = (SELECT id FROM curso_academico WHERE codigo_curso = '2VIFC303')
       AND tipo = 'ORDINARIO'
       AND anio_academico = '2025-2026'
       AND fecha_inicio = '2026-01-12'
       AND fecha_fin = '2026-03-27'
     ORDER BY id DESC
     LIMIT 1),
    '2026-01-12',
    'EN_CURSO'
WHERE NOT EXISTS (
    SELECT 1
    FROM asignacion
    WHERE id_alumno = (SELECT id FROM usuario WHERE nombre_usuario = 'alumno1')
      AND estado = 'EN_CURSO'
);

-- -----------------------------------------------------
-- CONVENIO DE PRUEBA
-- Permite probar la visualización y actualización del PDF
-- sin depender de creación manual adicional.
-- -----------------------------------------------------
INSERT INTO convenio
    (id_alumno, id_empresa, id_tutor_centro, numero_convenio, fecha_firma, fecha_inicio, fecha_fin,
     horas_semanales, horario_descripcion, actividades_previstas, estado, archivo_pdf_url)
SELECT
    (SELECT id FROM usuario WHERE nombre_usuario = 'alumno1'),
    (SELECT id FROM empresa WHERE cif = 'B33123456'),
    (SELECT id FROM usuario WHERE nombre_usuario = 'tutorcentro1'),
    'MC-2025-0001',
    '2026-01-10',
    '2026-01-12',
    '2026-03-27',
    35,
    'Lunes a viernes de 08:00 a 15:00',
    'Apoyo al desarrollo web, seguimiento de incidencias, pruebas funcionales y documentación técnica.',
    'VIGENTE',
    NULL
WHERE NOT EXISTS (
    SELECT 1
    FROM convenio
    WHERE numero_convenio = 'MC-2025-0001'
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
    'TODOS',
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
    'TODOS',
    'pdf,doc,docx,odt',
    TRUE
),
(
    'Justificante de Falta',
    'Documento justificativo de ausencia del alumno',
    FALSE,
    'ALUMNO',
    'pdf,jpg,png',
    TRUE
),
(
    'Informe de Seguimiento Empresa',
    'Documento subido por el tutor de empresa con información de seguimiento o incidencias',
    FALSE,
    'TUTOR_EMPRESA',
    'pdf,doc,docx',
    TRUE
);

UPDATE tipo_documento
SET rol_responsable = 'TODOS',
    descripcion = 'Documento legal que formaliza la relación entre el centro y la empresa',
    extensiones_permitidas = 'pdf',
    activo = TRUE
WHERE nombre = 'Convenio de Formación en Empresa';

UPDATE tipo_documento
SET rol_responsable = 'ALUMNO',
    descripcion = 'Documento nacional de identidad del alumno',
    extensiones_permitidas = 'pdf,jpg,png',
    activo = TRUE
WHERE nombre = 'DNI / NIE del Alumno';

UPDATE tipo_documento
SET rol_responsable = 'ALUMNO',
    descripcion = 'Comprobante del seguro escolar vigente',
    extensiones_permitidas = 'pdf',
    activo = TRUE
WHERE nombre = 'Seguro Escolar';

UPDATE tipo_documento
SET rol_responsable = 'TODOS',
    descripcion = 'Documento con las actividades y resultados de aprendizaje previstos',
    extensiones_permitidas = 'pdf,doc,docx,odt',
    activo = TRUE
WHERE nombre = 'Anexo I - Plan de Formación';

UPDATE tipo_documento
SET rol_responsable = 'ALUMNO',
    descripcion = 'Documento justificativo de ausencia del alumno',
    extensiones_permitidas = 'pdf,jpg,png',
    activo = TRUE
WHERE nombre = 'Justificante de Falta';

UPDATE tipo_documento
SET rol_responsable = 'TUTOR_EMPRESA',
    descripcion = 'Documento subido por el tutor de empresa con información de seguimiento o incidencias',
    extensiones_permitidas = 'pdf,doc,docx',
    activo = TRUE
WHERE nombre = 'Informe de Seguimiento Empresa';
