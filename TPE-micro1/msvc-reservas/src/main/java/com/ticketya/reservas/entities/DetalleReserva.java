package com.ticketya.reservas.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "detalle_reserva")
public class DetalleReserva {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reserva_id", nullable = false)
    private Reserva reserva;

    @Column(name = "sector_id")
    private Long sectorId;

    private Integer cantidad;

    private String estado;

    @OneToOne(mappedBy = "detalleReserva", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private AsignacionAsiento asignacionAsiento;

    public DetalleReserva() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Reserva getReserva() {
        return reserva;
    }

    public void setReserva(Reserva reserva) {
        this.reserva = reserva;
    }

    public Long getSectorId() {
        return sectorId;
    }

    public void setSectorId(Long sectorId) {
        this.sectorId = sectorId;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public AsignacionAsiento getAsignacionAsiento() {
        return asignacionAsiento;
    }

    public void setAsignacionAsiento(AsignacionAsiento asignacionAsiento) {
        if (this.asignacionAsiento != null) {
            this.asignacionAsiento.setDetalleReserva(null);
        }
        this.asignacionAsiento = asignacionAsiento;
        if (asignacionAsiento != null) {
            asignacionAsiento.setDetalleReserva(this);
        }
    }

}
