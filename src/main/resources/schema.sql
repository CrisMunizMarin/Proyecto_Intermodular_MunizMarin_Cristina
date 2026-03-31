-- =====================================================
-- MENTORCORE - Script de creación de base de datos
-- Proyecto Intermodular 2º DAW - Cristina Muñiz Marín
-- CIFP La Laboral - Gijón - Curso 2025/2026
-- =====================================================
-- Ejecutar con: spring.sql.init.mode=always
-- =====================================================

SET FOREIGN_KEY_CHECKS = 0;

-- -----------------------------------------------------
-- TABLA: usuario
-- Entidad base de autenticación para todos los roles
-- RF1, RF10, RF11 - RNF3, RNF4
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS usuario (
    id                  BIGINT          NOT NULL AUTO_INCREMENT,
    nombre_usuario      VARCHAR(50)     NOT NULL UNIQUE,
    email               VARCHAR(150)    NOT NULL UNIQUE,
    password_hash       VARCHAR(255)    NOT NULL,
    nombre              VARCHAR(100)    NOT NULL,
    apellidos           VARCHAR(150)    NOT NULL,
    telefono            VARCHAR(20),
    rol                 ENUM('ALUMNO','TUTOR_CENTRO','TUTOR_EMPRESA','ADMIN') NOT NULL,
    estado              ENUM('ACTIVO','INACTIVO','SUSPENDIDO') NOT NULL DEFAULT 'ACTIVO',
    foto_perfil_url     VARCHAR(500),
    ultimo_login        DATETIME,
    fecha_creacion      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    token_recuperacion  VARCHAR(255),
    token_expira        DATETIME,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------
-- TABLA: empresa
-- Empresa colaboradora que acoge alumnos en prácticas
-- RF18
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS empresa (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    nombre          VARCHAR(200)    NOT NULL,
    cif             VARCHAR(15)     NOT NULL UNIQUE,
    sector          VARCHAR(100),
    direccion       VARCHAR(255),
    municipio       VARCHAR(100),
    provincia       VARCHAR(100),
    codigo_postal   VARCHAR(10),
    telefono        VARCHAR(20),
    email_contacto  VARCHAR(150),
    web             VARCHAR(255),
    estado          ENUM('ACTIVA','INACTIVA') NOT NULL DEFAULT 'ACTIVA',
    fecha_alta      DATE            NOT NULL DEFAULT (CURRENT_DATE),
    notas           TEXT,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------
-- TABLA: curso_academico
-- Año académico con sus ciclos y grupos
-- RF12, RF20
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS curso_academico (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    codigo_curso    VARCHAR(15)     NOT NULL UNIQUE,
    nombre          VARCHAR(150)    NOT NULL,
    ciclo_formativo VARCHAR(100)    NOT NULL,
    nivel           ENUM('PRIMERO','SEGUNDO') NOT NULL,
    anio_academico  VARCHAR(20)     NOT NULL,
    activo          BOOLEAN         NOT NULL DEFAULT TRUE,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------
-- TABLA: alumno
-- Perfil extendido del usuario con rol ALUMNO
-- RF2, RF3, RF13
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS alumno (
    id                  BIGINT          NOT NULL AUTO_INCREMENT,
    id_usuario          BIGINT          NOT NULL UNIQUE,
    id_curso            BIGINT          NOT NULL,
    id_tutor_centro     BIGINT          NOT NULL,
    grupo               VARCHAR(20),
    dni                 VARCHAR(15)     UNIQUE,
    fecha_nacimiento    DATE,
    num_seguridad_social VARCHAR(20),
    horas_totales_fe    INT             NOT NULL DEFAULT 400,
    horas_completadas   DECIMAL(6,2)    NOT NULL DEFAULT 0.00,
    PRIMARY KEY (id),
    CONSTRAINT fk_alumno_usuario        FOREIGN KEY (id_usuario)      REFERENCES usuario(id),
    CONSTRAINT fk_alumno_curso          FOREIGN KEY (id_curso)        REFERENCES curso_academico(id),
    CONSTRAINT fk_alumno_tutor_centro   FOREIGN KEY (id_tutor_centro) REFERENCES usuario(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------
-- TABLA: tutor_centro
-- Perfil extendido del usuario con rol TUTOR_CENTRO
-- RF4, RF5, RF6, RF8
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS tutor_centro (
    id                      BIGINT          NOT NULL AUTO_INCREMENT,
    id_usuario              BIGINT          NOT NULL UNIQUE,
    departamento            VARCHAR(100),
    especialidad            VARCHAR(100),
    num_expediente_docente  VARCHAR(30),
    PRIMARY KEY (id),
    CONSTRAINT fk_tutor_centro_usuario FOREIGN KEY (id_usuario) REFERENCES usuario(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------
-- TABLA: tutor_empresa
-- Perfil extendido del usuario con rol TUTOR_EMPRESA
-- RF7, RF9, RF19
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS tutor_empresa (
    id                      BIGINT          NOT NULL AUTO_INCREMENT,
    id_usuario              BIGINT          NOT NULL UNIQUE,
    id_empresa              BIGINT          NOT NULL,
    cargo                   VARCHAR(100),
    departamento_empresa    VARCHAR(100),
    PRIMARY KEY (id),
    CONSTRAINT fk_tutor_empresa_usuario  FOREIGN KEY (id_usuario)  REFERENCES usuario(id),
    CONSTRAINT fk_tutor_empresa_empresa  FOREIGN KEY (id_empresa)  REFERENCES empresa(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------
-- TABLA: periodo_formacion
-- Periodos ordinario y extraordinario de FE por curso
-- RF20
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS periodo_formacion (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    id_curso        BIGINT          NOT NULL,
    tipo            ENUM('ORDINARIO','EXTRAORDINARIO') NOT NULL,
    anio_academico  VARCHAR(20)     NOT NULL,
    fecha_inicio    DATE            NOT NULL,
    fecha_fin       DATE            NOT NULL,
    horas_totales   INT             NOT NULL DEFAULT 400,
    estado          ENUM('PLANIFICADO','ACTIVO','CERRADO') NOT NULL DEFAULT 'PLANIFICADO',
    descripcion     TEXT,
    id_creado_por   BIGINT          NOT NULL,
    fecha_creacion  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_periodo_curso      FOREIGN KEY (id_curso)      REFERENCES curso_academico(id),
    CONSTRAINT fk_periodo_creado_por FOREIGN KEY (id_creado_por) REFERENCES usuario(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------
-- TABLA: asignacion
-- Historial alumno-empresa-tutorEmpresa por periodo
-- RF13, RF21
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS asignacion (
    id                  BIGINT          NOT NULL AUTO_INCREMENT,
    id_alumno           BIGINT          NOT NULL,
    id_empresa          BIGINT          NOT NULL,
    id_tutor_empresa    BIGINT          NOT NULL,
    id_periodo          BIGINT          NOT NULL,
    fecha_inicio        DATE            NOT NULL,
    fecha_fin           DATE,
    estado              ENUM('ACTIVA','FINALIZADA','SUSPENDIDA') NOT NULL DEFAULT 'ACTIVA',
    motivo_cambio       TEXT,
    id_reasignado_por   BIGINT,
    fecha_creacion      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_asignacion_alumno         FOREIGN KEY (id_alumno)         REFERENCES alumno(id),
    CONSTRAINT fk_asignacion_empresa        FOREIGN KEY (id_empresa)        REFERENCES empresa(id),
    CONSTRAINT fk_asignacion_tutor_empresa  FOREIGN KEY (id_tutor_empresa)  REFERENCES tutor_empresa(id),
    CONSTRAINT fk_asignacion_periodo        FOREIGN KEY (id_periodo)        REFERENCES periodo_formacion(id),
    CONSTRAINT fk_asignacion_reasignado     FOREIGN KEY (id_reasignado_por) REFERENCES usuario(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------
-- TABLA: tarea
-- Registro diario de actividades del alumno
-- RF2, RF5, RF9
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS tarea (
    id                  BIGINT          NOT NULL AUTO_INCREMENT,
    id_alumno           BIGINT          NOT NULL,
    fecha_registro      DATE            NOT NULL,
    descripcion         TEXT            NOT NULL,
    horas_dedicadas     DECIMAL(4,2)    NOT NULL,
    area_actividad      VARCHAR(100),
    estado_validacion   ENUM('PENDIENTE','VALIDADO','RECHAZADO','REQUIERE_REVISION') NOT NULL DEFAULT 'PENDIENTE',
    comentario_tutor    TEXT,
    id_validador        BIGINT,
    fecha_validacion    DATETIME,
    fecha_creacion      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_tarea_alumno    FOREIGN KEY (id_alumno)   REFERENCES alumno(id),
    CONSTRAINT fk_tarea_validador FOREIGN KEY (id_validador) REFERENCES usuario(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------
-- TABLA: tipo_documento
-- Catálogo de tipos de documentos configurado por Admin
-- RF12
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS tipo_documento (
    id                      BIGINT          NOT NULL AUTO_INCREMENT,
    nombre                  VARCHAR(100)    NOT NULL,
    descripcion             TEXT,
    es_obligatorio          BOOLEAN         NOT NULL DEFAULT FALSE,
    rol_responsable         ENUM('ALUMNO','TUTOR_CENTRO','TUTOR_EMPRESA','TODOS'),
    extensiones_permitidas  VARCHAR(100),
    activo                  BOOLEAN         NOT NULL DEFAULT TRUE,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------
-- TABLA: documento
-- Expediente digital: archivos subidos por los usuarios
-- RF3, RF6, RF22
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS documento (
    id                      BIGINT          NOT NULL AUTO_INCREMENT,
    id_alumno               BIGINT          NOT NULL,
    id_tipo_documento       BIGINT          NOT NULL,
    id_subido_por           BIGINT          NOT NULL,
    nombre_archivo          VARCHAR(255)    NOT NULL,
    ruta_almacenamiento     VARCHAR(500)    NOT NULL,
    tamano_bytes            BIGINT,
    mime_type               VARCHAR(100),
    estado                  ENUM('PENDIENTE','VALIDADO','RECHAZADO') NOT NULL DEFAULT 'PENDIENTE',
    comentario_revision     TEXT,
    contexto                ENUM('EXPEDIENTE','JUSTIFICANTE_FALTA') NOT NULL DEFAULT 'EXPEDIENTE',
    es_obligatorio          BOOLEAN         NOT NULL DEFAULT FALSE,
    fecha_subida            DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_revision          DATETIME,
    PRIMARY KEY (id),
    CONSTRAINT fk_documento_alumno          FOREIGN KEY (id_alumno)         REFERENCES alumno(id),
    CONSTRAINT fk_documento_tipo            FOREIGN KEY (id_tipo_documento) REFERENCES tipo_documento(id),
    CONSTRAINT fk_documento_subido_por      FOREIGN KEY (id_subido_por)     REFERENCES usuario(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------
-- TABLA: falta_asistencia
-- Faltas registradas por el tutor empresa
-- RF19, RF22
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS falta_asistencia (
    id                  BIGINT          NOT NULL AUTO_INCREMENT,
    id_alumno           BIGINT          NOT NULL,
    id_asignacion       BIGINT          NOT NULL,
    id_registrado_por   BIGINT          NOT NULL,
    fecha_falta         DATE            NOT NULL,
    tipo                ENUM('JUSTIFICADA','INJUSTIFICADA') NOT NULL,
    estado              ENUM('INJUSTIFICADA','PENDIENTE_REVISION','JUSTIFICADA') NOT NULL DEFAULT 'INJUSTIFICADA',
    observacion         TEXT,
    id_justificante     BIGINT,
    id_validado_por     BIGINT,
    fecha_validacion    DATETIME,
    fecha_creacion      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uq_falta_alumno_fecha (id_alumno, fecha_falta),
    PRIMARY KEY (id),
    CONSTRAINT fk_falta_alumno          FOREIGN KEY (id_alumno)         REFERENCES alumno(id),
    CONSTRAINT fk_falta_asignacion      FOREIGN KEY (id_asignacion)     REFERENCES asignacion(id),
    CONSTRAINT fk_falta_registrado_por  FOREIGN KEY (id_registrado_por) REFERENCES usuario(id),
    CONSTRAINT fk_falta_justificante    FOREIGN KEY (id_justificante)   REFERENCES documento(id),
    CONSTRAINT fk_falta_validado_por    FOREIGN KEY (id_validado_por)   REFERENCES usuario(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------
-- TABLA: valoracion
-- Evaluación final APTO/NO APTO por cada tutor
-- RF7
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS valoracion (
    id                      BIGINT          NOT NULL AUTO_INCREMENT,
    id_alumno               BIGINT          NOT NULL,
    id_evaluador            BIGINT          NOT NULL,
    tipo_evaluador          ENUM('TUTOR_CENTRO','TUTOR_EMPRESA') NOT NULL,
    nota_global             DECIMAL(4,2),
    resultado               ENUM('APTO','NO_APTO','PENDIENTE') NOT NULL DEFAULT 'PENDIENTE',
    puntacion_actitud       TINYINT         CHECK (puntacion_actitud BETWEEN 0 AND 10),
    puntacion_competencias  TINYINT         CHECK (puntacion_competencias BETWEEN 0 AND 10),
    puntacion_integracion   TINYINT         CHECK (puntacion_integracion BETWEEN 0 AND 10),
    puntacion_iniciativa    TINYINT         CHECK (puntacion_iniciativa BETWEEN 0 AND 10),
    observaciones           TEXT,
    fecha_emision           DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    bloqueada               BOOLEAN         NOT NULL DEFAULT FALSE,
    UNIQUE KEY uq_valoracion_alumno_evaluador (id_alumno, tipo_evaluador),
    PRIMARY KEY (id),
    CONSTRAINT fk_valoracion_alumno    FOREIGN KEY (id_alumno)   REFERENCES alumno(id),
    CONSTRAINT fk_valoracion_evaluador FOREIGN KEY (id_evaluador) REFERENCES usuario(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------
-- TABLA: notificacion
-- Sistema de mensajería interna entre roles
-- RF13, RF16
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS notificacion (
    id                      BIGINT          NOT NULL AUTO_INCREMENT,
    id_emisor               BIGINT          NOT NULL,
    id_receptor             BIGINT          NOT NULL,
    tipo                    ENUM('AVISO','ALERTA','RECORDATORIO','MENSAJE','VALIDACION') NOT NULL,
    titulo                  VARCHAR(200)    NOT NULL,
    mensaje                 TEXT            NOT NULL,
    leida                   BOOLEAN         NOT NULL DEFAULT FALSE,
    fecha_envio             DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_lectura           DATETIME,
    entidad_relacionada     VARCHAR(50),
    id_entidad_relacionada  BIGINT,
    PRIMARY KEY (id),
    CONSTRAINT fk_notificacion_emisor   FOREIGN KEY (id_emisor)   REFERENCES usuario(id),
    CONSTRAINT fk_notificacion_receptor FOREIGN KEY (id_receptor) REFERENCES usuario(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------
-- TABLA: convenio
-- Documento legal alumno-empresa-centro educativo
-- RF3
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS convenio (
    id                      BIGINT          NOT NULL AUTO_INCREMENT,
    id_alumno               BIGINT          NOT NULL,
    id_empresa              BIGINT          NOT NULL,
    id_tutor_centro         BIGINT          NOT NULL,
    numero_convenio         VARCHAR(50)     NOT NULL UNIQUE,
    fecha_firma             DATE,
    fecha_inicio            DATE            NOT NULL,
    fecha_fin               DATE            NOT NULL,
    horas_semanales         INT,
    horario_descripcion     TEXT,
    actividades_previstas   TEXT,
    estado                  ENUM('BORRADOR','FIRMADO','VIGENTE','FINALIZADO','ANULADO') NOT NULL DEFAULT 'BORRADOR',
    archivo_pdf_url         VARCHAR(500),
    PRIMARY KEY (id),
    CONSTRAINT fk_convenio_alumno       FOREIGN KEY (id_alumno)       REFERENCES alumno(id),
    CONSTRAINT fk_convenio_empresa      FOREIGN KEY (id_empresa)      REFERENCES empresa(id),
    CONSTRAINT fk_convenio_tutor_centro FOREIGN KEY (id_tutor_centro) REFERENCES usuario(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------
-- TABLA: parametro_sistema
-- Configuración global gestionada por el Administrador
-- RF12
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS parametro_sistema (
    id                  BIGINT          NOT NULL AUTO_INCREMENT,
    clave               VARCHAR(100)    NOT NULL UNIQUE,
    valor               TEXT            NOT NULL,
    descripcion         TEXT,
    tipo_dato           ENUM('STRING','INTEGER','BOOLEAN','DATE','JSON'),
    id_modificado_por   BIGINT,
    fecha_modificacion  DATETIME,
    PRIMARY KEY (id),
    CONSTRAINT fk_parametro_modificado_por FOREIGN KEY (id_modificado_por) REFERENCES usuario(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------
-- TABLA: log_auditoria
-- Registro inmutable para cumplimiento RGPD/LOPD
-- Objetivo 7
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS log_auditoria (
    id                  BIGINT          NOT NULL AUTO_INCREMENT,
    id_usuario          BIGINT,
    accion              VARCHAR(100)    NOT NULL,
    entidad_afectada    VARCHAR(50),
    id_entidad          BIGINT,
    ip_origen           VARCHAR(45),
    datos_anteriores    JSON,
    datos_nuevos        JSON,
    fecha_hora          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    resultado           ENUM('EXITO','ERROR','DENEGADO'),
    PRIMARY KEY (id),
    CONSTRAINT fk_log_usuario FOREIGN KEY (id_usuario) REFERENCES usuario(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

SET FOREIGN_KEY_CHECKS = 1;