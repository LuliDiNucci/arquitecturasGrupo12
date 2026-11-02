package com.ticketya.compras.entities;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "reintegro")
public class Reintegro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "pago_id", nullable = false)
    private Pago pago;

    @Column(name = "entrada_id")
    private Long entradaId;

    @Column(name = "importe", precision = 10, scale = 2)
    private BigDecimal importe;

    @Column(name = "fecha_solicitud")
    private LocalDate fechaSolicitud;

    @Column(name = "fecha_reintegro")
    private LocalDate fechaReintegro;

    @Column(name = "estado")
    private String estado;

    @Column(name = "referencia_externa")
    private String referenciaExterna;

    public Reintegro() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Pago getPago() {
    return pago;
    }

    public void setPago(Pago pago) {
        this.pago = pago;
    }

    public Long getEntradaId() {
        return entradaId;
    }

    public void setEntradaId(Long entradaId) {
        this.entradaId = entradaId;
    }

    public BigDecimal getImporte() {
        return importe;
    }

    public void setImporte(BigDecimal importe) {
        this.importe = importe;
    }

    public LocalDate getFechaSolicitud() {
        return fechaSolicitud;
    }

    public void setFechaSolicitud(LocalDate fechaSolicitud) {
        this.fechaSolicitud = fechaSolicitud;
    }

    public LocalDate getFechaReintegro() {
        return fechaReintegro;
    }

    public void setFechaReintegro(LocalDate fechaReintegro) {
        this.fechaReintegro = fechaReintegro;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getReferenciaExterna() {
        return referenciaExterna;
    }

    public void setReferenciaExterna(String referenciaExterna) {
        this.referenciaExterna = referenciaExterna;
    }

}
