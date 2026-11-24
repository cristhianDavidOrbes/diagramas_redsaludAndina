-- Migración inicial RedSalud Andina: 9 esquemas aislados (cero claves foráneas entre contextos)
CREATE SCHEMA IF NOT EXISTS identidad;
CREATE SCHEMA IF NOT EXISTS agenda;
CREATE SCHEMA IF NOT EXISTS episodio;
CREATE SCHEMA IF NOT EXISTS receta;
CREATE SCHEMA IF NOT EXISTS interconsulta;
CREATE SCHEMA IF NOT EXISTS legado;
CREATE SCHEMA IF NOT EXISTS auditoria;
CREATE SCHEMA IF NOT EXISTS sincronizacion;
CREATE SCHEMA IF NOT EXISTS analitica;

CREATE TABLE identidad.usuarios_clinicos (
    usuario_id     VARCHAR(64) PRIMARY KEY,
    rol            VARCHAR(32) NOT NULL,
    centro_id      VARCHAR(64) NOT NULL,
    especialidad   VARCHAR(64),
    activo         BOOLEAN     NOT NULL DEFAULT TRUE,
    version        BIGINT      NOT NULL DEFAULT 0
);

CREATE TABLE agenda.cupos_especialista (
    cupo_id          VARCHAR(64) PRIMARY KEY,
    especialista_id  VARCHAR(64) NOT NULL,
    centro_id        VARCHAR(64) NOT NULL,
    fecha_hora       TIMESTAMPTZ NOT NULL,
    reservado        BOOLEAN     NOT NULL DEFAULT FALSE,
    paciente_id      VARCHAR(64),
    interconsulta_id VARCHAR(64),
    version          BIGINT      NOT NULL DEFAULT 0
);

CREATE TABLE interconsulta.interconsultas (
    interconsulta_id VARCHAR(64) PRIMARY KEY,
    paciente_id      VARCHAR(64) NOT NULL,
    centro_origen_id VARCHAR(64) NOT NULL,
    especialidad     VARCHAR(64) NOT NULL,
    estado           VARCHAR(32) NOT NULL,
    creada_en        TIMESTAMPTZ NOT NULL,
    version          BIGINT      NOT NULL DEFAULT 0
);

CREATE TABLE episodio.episodios_clinicos (
    episodio_id      VARCHAR(64) PRIMARY KEY,
    paciente_id      VARCHAR(64) NOT NULL,
    centro_id        VARCHAR(64) NOT NULL,
    nota_evolucion   TEXT        NOT NULL,
    signos_vitales   TEXT        NOT NULL,
    adjunto_s3_key   VARCHAR(255),
    actualizado_en   TIMESTAMPTZ NOT NULL,
    version          BIGINT      NOT NULL DEFAULT 0
);

CREATE TABLE receta.recetas_medicas (
    receta_id        VARCHAR(64) PRIMARY KEY,
    episodio_id      VARCHAR(64) NOT NULL,
    paciente_id      VARCHAR(64) NOT NULL,
    especialista_id  VARCHAR(64) NOT NULL,
    medicamentos     TEXT        NOT NULL,
    firma_kms_hash   VARCHAR(128) NOT NULL,
    kms_key_id       VARCHAR(128) NOT NULL,
    firmada_en       TIMESTAMPTZ NOT NULL,
    enviada_farmacia BOOLEAN     NOT NULL DEFAULT FALSE,
    version          BIGINT      NOT NULL DEFAULT 0
);

CREATE TABLE legado.historial_read_model (
    paciente_id          VARCHAR(64) PRIMARY KEY,
    resumen_antecedentes TEXT        NOT NULL,
    sincronizado_en      TIMESTAMPTZ NOT NULL,
    ttl_segundos         BIGINT      NOT NULL
);

CREATE TABLE sincronizacion.claves_idempotencia (
    idempotency_key  VARCHAR(128) PRIMARY KEY,
    agregado_id      VARCHAR(64)  NOT NULL,
    estado_resultado VARCHAR(32)  NOT NULL,
    codigo_http      INT          NOT NULL,
    mensaje          TEXT         NOT NULL,
    procesado_en     TIMESTAMPTZ  NOT NULL,
    CONSTRAINT uq_sincronizacion_idempotency_key UNIQUE (idempotency_key)
);

CREATE TABLE sincronizacion.outbox_eventos (
    evento_id        VARCHAR(64)  PRIMARY KEY,
    agregado_id      VARCHAR(64)  NOT NULL,
    tipo_evento      VARCHAR(64)  NOT NULL,
    payload          TEXT         NOT NULL,
    publicado        BOOLEAN      NOT NULL DEFAULT FALSE,
    creado_en        TIMESTAMPTZ  NOT NULL
);

CREATE TABLE sincronizacion.conflictos_lww (
    conflicto_id     VARCHAR(64)  PRIMARY KEY,
    agregado_id      VARCHAR(64)  NOT NULL,
    campo            VARCHAR(64)  NOT NULL,
    valor_ganador    TEXT         NOT NULL,
    valor_desplazado TEXT         NOT NULL,
    registrado_en    TIMESTAMPTZ  NOT NULL
);

CREATE TABLE auditoria.trazas_acceso (
    traza_id         VARCHAR(64)  PRIMARY KEY,
    usuario_id       VARCHAR(64)  NOT NULL,
    rol              VARCHAR(32)  NOT NULL,
    recurso_id       VARCHAR(128) NOT NULL,
    motivo           VARCHAR(255) NOT NULL,
    permitido        BOOLEAN      NOT NULL,
    centro_id        VARCHAR(64)  NOT NULL,
    ocurrido_en      TIMESTAMPTZ  NOT NULL,
    hash_anterior    VARCHAR(64)  NOT NULL,
    hash_actual      VARCHAR(64)  NOT NULL
);

CREATE TABLE analitica.metricas_centro (
    centro_id         VARCHAR(64) PRIMARY KEY,
    teleconsultas     BIGINT      NOT NULL DEFAULT 0,
    disponibilidad_pct DOUBLE PRECISION NOT NULL DEFAULT 99.5,
    p95_sync_minutos  DOUBLE PRECISION NOT NULL DEFAULT 4.0
);
