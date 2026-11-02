package com.ticketya.compras.entities;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "pago")
public class Pago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne //cada pago pertenece a una compra
    @JoinColumn(name = "compra_id", nullable = false)
    private Compra compra;

    @Column(name = "importe", precision = 10, scale = 2)
    private BigDecimal importe;

    @Column(name = "medio_pago")
    private String medioPago;

    @Column(name = "estado")
    private String estado;

    @Column(name = "fecha_pago")
    private LocalDate fechaPago;

    @Column(name = "clave_idempotencia")
    private String claveIdempotencia;

    @Column(name = "referencia_externa")
    private String referenciaExterna;
    
    @OneToMany(mappedBy = "pago") //un pago puede tener varios reintegros 
    private List<Reintegro> reintegros = new ArrayList<>();

     public Pago() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Compra getCompra() {
    return compra;
    }

    public void setCompra(Compra compra) {
        this.compra = compra;
    }

    public BigDecimal getImporte() {
        return importe;
    }

    public void setImporte(BigDecimal importe) {
        this.importe = importe;
    }

    public String getMedioPago() {
        return medioPago;
    }

    public void setMedioPago(String medioPago) {
        this.medioPago = medioPago;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public LocalDate getFechaPago() {
        return fechaPago;
    }

    public void setFechaPago(LocalDate fechaPago) {
        this.fechaPago = fechaPago;
    }

    public String getClaveIdempotencia() {
        return claveIdempotencia;
    }

    public void setClaveIdempotencia(String claveIdempotencia) {
        this.claveIdempotencia = claveIdempotencia;
    }

    public String getReferenciaExterna() {
        return referenciaExterna;
    }

    public void setReferenciaExterna(String referenciaExterna) {
        this.referenciaExterna = referenciaExterna;
    }

    public List<Reintegro> getReintegros() {
        return reintegros;
    }

    public void setReintegros(List<Reintegro> reintegros) {
        this.reintegros = reintegros;
    }
}
