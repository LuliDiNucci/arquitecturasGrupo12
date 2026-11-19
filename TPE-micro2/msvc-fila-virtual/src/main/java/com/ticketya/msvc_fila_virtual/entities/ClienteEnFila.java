
package com.ticketya.msvc_fila_virtual.entities;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "clientes_en_fila", uniqueConstraints = @UniqueConstraint(columnNames = { "evento_id", "cliente_id" }))
public class ClienteEnFila {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "evento_id", nullable = false)
    private Long eventoId;

    @Column(name = "cliente_id", nullable = false)
    private Long clienteId;

    @Column(nullable = false)
    private LocalDateTime fechaIngreso;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoFila estado;

    private LocalDateTime fechaHabilitacion;

    private LocalDateTime fechaVencimiento;

    protected ClienteEnFila() {
    }

    public ClienteEnFila(Long eventoId, Long clienteId) {
        this.eventoId = eventoId;
        this.clienteId = clienteId;
        this.fechaIngreso = LocalDateTime.now();
        this.estado = EstadoFila.EN_ESPERA;
    }

    public void habilitar(LocalDateTime momento, int duracionMinutos) {
        if (estado != EstadoFila.EN_ESPERA) {
            throw new IllegalStateException(
                    "Solo se puede habilitar un cliente en espera");
        }

        this.estado = EstadoFila.HABILITADO;
        this.fechaHabilitacion = momento;
        this.fechaVencimiento = momento.plusMinutes(duracionMinutos);
    }

    public boolean tieneTurnoVencido(LocalDateTime momento) {
        return estado == EstadoFila.HABILITADO
                && fechaVencimiento != null
                && !fechaVencimiento.isAfter(momento);
    }

    public void vencer() {
        if (estado != EstadoFila.HABILITADO) {
            throw new IllegalStateException(
                    "Solo puede vencer un turno habilitado");
        }

        this.estado = EstadoFila.VENCIDO;
    }

    public void finalizar() {

        if (estado != EstadoFila.HABILITADO
                && estado != EstadoFila.VENCIDO
                && estado != EstadoFila.FINALIZADO) {

            throw new IllegalStateException(
                    "El cliente nunca tuvo un turno habilitado");
        }

        this.estado = EstadoFila.FINALIZADO;
    }

    public Long getId() {
        return id;
    }

    public Long getEventoId() {
        return eventoId;
    }

    public Long getClienteId() {
        return clienteId;
    }

    public LocalDateTime getFechaIngreso() {
        return fechaIngreso;
    }

    public EstadoFila getEstado() {
        return estado;
    }

    public LocalDateTime getFechaHabilitacion() {
        return fechaHabilitacion;
    }

    public LocalDateTime getFechaVencimiento() {
        return fechaVencimiento;
    }
}
