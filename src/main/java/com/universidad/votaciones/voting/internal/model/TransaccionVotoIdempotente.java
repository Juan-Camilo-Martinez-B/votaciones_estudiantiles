package com.universidad.votaciones.voting.internal.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

/**
 * Registra la transacción de emisión de voto con llave de idempotencia (ADR-002).
 * Garantiza que peticiones duplicadas (por reintentos de red, clicks repetidos)
 * no inserten múltiples votos ni alteren el padrón electoral.
 */
@Entity
@Table(name = "transacciones_voto_idempotente")
public class TransaccionVotoIdempotente {

    @Id
    @Column(nullable = false, unique = true)
    private String llaveIdempotencia;

    @Column(nullable = false)
    private String idEstudiante;

    @Column(nullable = true)
    private String tokenAnonimo;

    @Column(nullable = false)
    private LocalDateTime fechaHoraPeticion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoProcesamiento estado;

    public TransaccionVotoIdempotente() {
    }

    public TransaccionVotoIdempotente(String llaveIdempotencia, String idEstudiante, LocalDateTime fechaHoraPeticion, EstadoProcesamiento estado) {
        this.llaveIdempotencia = llaveIdempotencia;
        this.idEstudiante = idEstudiante;
        this.fechaHoraPeticion = fechaHoraPeticion;
        this.estado = estado;
    }

    public TransaccionVotoIdempotente(String llaveIdempotencia, String idEstudiante, String tokenAnonimo, LocalDateTime fechaHoraPeticion, EstadoProcesamiento estado) {
        this.llaveIdempotencia = llaveIdempotencia;
        this.idEstudiante = idEstudiante;
        this.tokenAnonimo = tokenAnonimo;
        this.fechaHoraPeticion = fechaHoraPeticion;
        this.estado = estado;
    }

    public String getLlaveIdempotencia() {
        return llaveIdempotencia;
    }

    public void setLlaveIdempotencia(String llaveIdempotencia) {
        this.llaveIdempotencia = llaveIdempotencia;
    }

    public String getIdEstudiante() {
        return idEstudiante;
    }

    public void setIdEstudiante(String idEstudiante) {
        this.idEstudiante = idEstudiante;
    }

    public String getTokenAnonimo() {
        return tokenAnonimo;
    }

    public void setTokenAnonimo(String tokenAnonimo) {
        this.tokenAnonimo = tokenAnonimo;
    }

    public LocalDateTime getFechaHoraPeticion() {
        return fechaHoraPeticion;
    }

    public void setFechaHoraPeticion(LocalDateTime fechaHoraPeticion) {
        this.fechaHoraPeticion = fechaHoraPeticion;
    }

    public EstadoProcesamiento getEstado() {
        return estado;
    }

    public void setEstado(EstadoProcesamiento estado) {
        this.estado = estado;
    }

    public boolean esValida() {
        return this.estado == EstadoProcesamiento.PROCESADO;
    }
}
