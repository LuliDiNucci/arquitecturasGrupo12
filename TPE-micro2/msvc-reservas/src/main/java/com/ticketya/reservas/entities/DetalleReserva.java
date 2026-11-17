
package com.ticketya.reservas.entities;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "detalle_reserva")
public class DetalleReserva {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reserva_id", nullable = false)
    private Reserva reserva;

    @Column(name = "sector_id", nullable = false)
    private Long sectorId;

    @Column(nullable = false)
    private Integer cantidad;

    @Column(name = "cantidad_liberada", nullable = false)
    private Integer cantidadLiberada = 0;

    @OneToMany(mappedBy = "detalleReserva", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AsignacionAsiento> asignaciones = new ArrayList<>();

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

    public Integer getCantidadLiberada() {
        return cantidadLiberada;
    }

    public void setCantidadLiberada(Integer cantidadLiberada) {
        this.cantidadLiberada = cantidadLiberada;
    }

    public List<AsignacionAsiento> getAsignaciones() {
        return asignaciones;
    }

    public void agregarAsignacion(AsignacionAsiento asignacion) {
        asignaciones.add(asignacion);
        asignacion.setDetalleReserva(this);
    }

    public void quitarAsignacion(AsignacionAsiento asignacion) {
        if (asignaciones.remove(asignacion)) {
            asignacion.setDetalleReserva(null);
        }
    }

}
