package com.ticketya.entradas.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity 
@Table(name = "solicitud_devolucion")
public class SolicitudDevolucion {
    
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "entrada_id", nullable = false)
    private Entrada entrada;

    @Column(name = "fecha_solicitud", nullable = false)
    private LocalDateTime fechaSolicitud;

    private String motivo;

    @Column(nullable = false)
    private String estado;

    @Column(name = "fecha_resolucion")
    private LocalDateTime fechaResolucion;

    @Column(name = "administrador_id")
    private Long administradorId;

    private SolicitudDevolucion() {

    }

    public Long getId() {
        return id;
    }

    public Entrada getEntrada() {
        return entrada;
    }

    public void setEntrada(Entrada entrada) {
        this.entrada = entrada;
    }

    public LocalDateTime getFechaSolicitud() {
        return fechaSolicitud;
    }

    public void setFechaSolicitud(LocalDateTime fechaSolicitud) {
        this.fechaSolicitud = fechaSolicitud;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public LocalDateTime getFechaResolucion() {
        return fechaResolucion;
    }

    public void setFechaResolucion(LocalDateTime fechaResolucion) {
        this.fechaResolucion = fechaResolucion;
    }

    public Long getAdministradorId() {
        return administradorId;
    }

    public void setAdministradorId(Long administradorId) {
        this.administradorId = administradorId;
    }
}
