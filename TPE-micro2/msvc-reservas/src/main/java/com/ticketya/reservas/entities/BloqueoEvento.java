
package com.ticketya.reservas.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "bloqueo_evento")
public class BloqueoEvento {

    @Id
    @Column(name = "evento_id")
    private Long eventoId;

    @Column(name = "cancelado", nullable = false)
    private boolean cancelado = false;

    public boolean isCancelado() { return cancelado; }
    public void setCancelado(boolean cancelado) { this.cancelado = cancelado; }

    public BloqueoEvento() {
    }

    public Long getEventoId() {
        return eventoId;
    }

    public void setEventoId(Long eventoId) {
        this.eventoId = eventoId;
    }
}
