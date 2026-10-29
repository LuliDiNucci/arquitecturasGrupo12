package com.ticketya.reservas.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "asignacion_asiento")
public class AsignacionAsiento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "detalle_reserva_id", nullable = false, unique = true)
    private DetalleReserva detalleReserva;

    @Column(name = "asiento_id")
    private Long asientoId;

    public AsignacionAsiento() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public DetalleReserva getDetalleReserva() {
        return detalleReserva;
    }

    public void setDetalleReserva(DetalleReserva detalleReserva) {
        this.detalleReserva = detalleReserva;
    }

    public Long getAsientoId() {
        return asientoId;
    }

    public void setAsientoId(Long asientoId) {
        this.asientoId = asientoId;
    }

}
