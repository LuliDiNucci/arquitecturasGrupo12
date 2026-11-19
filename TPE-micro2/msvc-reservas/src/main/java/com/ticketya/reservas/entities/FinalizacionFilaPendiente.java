package com.ticketya.reservas.entities;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "finalizaciones_fila_pendientes", uniqueConstraints = @UniqueConstraint(name = "uk_finalizacion_reserva", columnNames = "reserva_id"))
public class FinalizacionFilaPendiente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "reserva_id", nullable = false)
    private Long reservaId;

    @Column(name = "evento_id", nullable = false)
    private Long eventoId;

    @Column(name = "cliente_id", nullable = false)
    private Long clienteId;

    @Column(nullable = false, length = 20)
    private String estado;

    @Column(nullable = false)
    private int intentos;

    @Column(name = "proximo_intento_en", nullable = false)
    private Instant proximoIntentoEn;

    protected FinalizacionFilaPendiente() {
    }

    public FinalizacionFilaPendiente(
            Long reservaId,
            Long eventoId,
            Long clienteId) {

        this.reservaId = reservaId;
        this.eventoId = eventoId;
        this.clienteId = clienteId;
        this.estado = "PENDIENTE";
        this.intentos = 0;
        this.proximoIntentoEn = Instant.now();
    }

    public void marcarFinalizada() {
        this.estado = "FINALIZADA";
    }

    public void programarReintento(Instant fecha) {
        this.intentos++;
        this.proximoIntentoEn = fecha;
    }

    public Long getId() {
        return id;
    }

    public Long getReservaId() {
        return reservaId;
    }

    public Long getEventoId() {
        return eventoId;
    }

    public Long getClienteId() {
        return clienteId;
    }

    public String getEstado() {
        return estado;
    }

    public int getIntentos() {
        return intentos;
    }

    public Instant getProximoIntentoEn() {
        return proximoIntentoEn;
    }
}