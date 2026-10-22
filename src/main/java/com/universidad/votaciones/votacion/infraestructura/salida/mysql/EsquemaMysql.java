package com.universidad.votaciones.votacion.infraestructura.salida.mysql;

import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Tablas del adaptador MySQL. La auditoría sigue en JPA, sobre este mismo datasource.
 */
@Component
@Profile("mysql")
@DependsOn("entityManagerFactory")
public class EsquemaMysql {

    EsquemaMysql(JdbcTemplate jdbc) {
        jdbc.execute("""
                CREATE TABLE IF NOT EXISTS jornada_electoral (
                    id VARCHAR(255) NOT NULL,
                    estado VARCHAR(255) NULL,
                    dia DATE NULL,
                    PRIMARY KEY (id)
                )
                """);
        jdbc.execute("""
                CREATE TABLE IF NOT EXISTS padron_electoral (
                    codigo_estudiante VARCHAR(255) NOT NULL,
                    PRIMARY KEY (codigo_estudiante)
                )
                """);
        jdbc.execute("""
                CREATE TABLE IF NOT EXISTS transacciones_voto_idempotente (
                    llave_idempotencia VARCHAR(255) NOT NULL,
                    id_estudiante VARCHAR(255) NOT NULL,
                    token_anonimo VARCHAR(255) NULL,
                    fecha_hora_peticion DATETIME(6) NOT NULL,
                    estado VARCHAR(255) NOT NULL,
                    PRIMARY KEY (llave_idempotencia)
                )
                """);
        jdbc.execute("""
                CREATE TABLE IF NOT EXISTS papeletas_voto (
                    id_papeleta VARCHAR(255) NOT NULL,
                    id_candidato VARCHAR(255) NOT NULL,
                    token_anonimo VARCHAR(255) NOT NULL,
                    fecha_hora_registro DATETIME(6) NOT NULL,
                    PRIMARY KEY (id_papeleta),
                    UNIQUE KEY uk_papeleta_sello (token_anonimo)
                )
                """);
        jdbc.execute("""
                CREATE TABLE IF NOT EXISTS candidatos (
                    id_candidato VARCHAR(255) NOT NULL,
                    numero_tarjeton VARCHAR(255) NOT NULL,
                    nombre_candidato VARCHAR(255) NOT NULL,
                    propuesta_resumen VARCHAR(1000) NULL,
                    PRIMARY KEY (id_candidato),
                    UNIQUE KEY uk_candidato_tarjeton (numero_tarjeton)
                )
                """);
        jdbc.execute("""
                CREATE TABLE IF NOT EXISTS usuarios (
                    id BIGINT NOT NULL AUTO_INCREMENT,
                    nombre VARCHAR(100) NOT NULL,
                    apellido VARCHAR(100) NOT NULL,
                    email VARCHAR(150) NOT NULL,
                    password VARCHAR(255) NOT NULL,
                    fecha_creacion DATETIME(6) NOT NULL,
                    fecha_actualizacion DATETIME(6) NOT NULL,
                    PRIMARY KEY (id),
                    UNIQUE KEY uk_usuario_email (email)
                )
                """);
        jdbc.execute("""
                CREATE TABLE IF NOT EXISTS votantes_estudiantes (
                    usuario_id BIGINT NOT NULL,
                    codigo_estudiantil VARCHAR(50) NOT NULL,
                    programa_academico VARCHAR(150) NOT NULL,
                    estado_votante VARCHAR(30) NOT NULL,
                    PRIMARY KEY (usuario_id),
                    UNIQUE KEY uk_votante_codigo (codigo_estudiantil),
                    CONSTRAINT fk_votante_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios (id)
                )
                """);
        jdbc.execute("""
                CREATE TABLE IF NOT EXISTS usuarios_administradores (
                    usuario_id BIGINT NOT NULL,
                    cargo VARCHAR(100) NOT NULL,
                    activo BOOLEAN NOT NULL,
                    PRIMARY KEY (usuario_id),
                    CONSTRAINT fk_admin_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios (id)
                )
                """);
    }
}
