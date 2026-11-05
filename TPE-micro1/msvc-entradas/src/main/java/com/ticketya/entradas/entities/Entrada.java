
package com.ticketya.entradas.entities;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
    name = "entrada",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_entrada_lugar_reservado",
        columnNames = {
            "reserva_id",
            "detalle_reserva_id",
            "indice_lugar"
        }
    ),
    indexes = {
        @Index(name = "idx_entrada_compra", columnList = "compra_id"),
        @Index(name = "idx_entrada_cliente", columnList = "cliente_id")
    }
)
public class Entrada {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "compra_id", nullable = false)
    private Long compraId;

    @Column(name = "reserva_id", nullable = false)
    private Long reservaId;

    @Column(name = "detalle_reserva_id", nullable = false)
    private Long detalleReservaId;

    @Column(name = "indice_lugar", nullable = false)
    private Integer indiceLugar;

    @Column(name = "cliente_id", nullable = false)
    private Long clienteId;

    @Column(name = "evento_id", nullable = false)
    private Long eventoId;

    @Column(name = "sector_id", nullable = false)
    private Long sectorId;

    // Null cuando el sector no es numerado.
    @Column(name = "asiento_id")
    private Long asientoId;

    @Column(name = "codigo_qr", nullable = false, unique = true, length = 64)
    private String codigoQr;

    @Column(nullable = false, length = 32)
    private String estado;

    @Column(name = "fecha_emision", nullable = false)
    private LocalDateTime fechaEmision;

    @Column(name = "fecha_uso")
    private LocalDateTime fechaUso;

    public Entrada() {}

    public Long getId() {
        return id;
    }

    public Long getCompraId() {
        return compraId;
    }

    public void setCompraId(Long compraId) {
        this.compraId = compraId;
    }

    public Long getReservaId() {
        return reservaId;
    }

    public void setReservaId(Long reservaId) {
        this.reservaId = reservaId;
    }

    public Long getDetalleReservaId() {
        return detalleReservaId;
    }

    public void setDetalleReservaId(Long detalleReservaId) {
        this.detalleReservaId = detalleReservaId;
    }

    public Integer getIndiceLugar() {
        return indiceLugar;
    }

    public void setIndiceLugar(Integer indiceLugar) {
        this.indiceLugar = indiceLugar;
    }

    public Long getClienteId() {
        return clienteId;
    }

    public void setClienteId(Long clienteId) {
        this.clienteId = clienteId;
    }

    public Long getEventoId() {
        return eventoId;
    }

    public void setEventoId(Long eventoId) {
        this.eventoId = eventoId;
    }

    public Long getSectorId() {
        return sectorId;
    }

    public void setSectorId(Long sectorId) {
        this.sectorId = sectorId;
    }

    public Long getAsientoId() {
        return asientoId;
    }

    public void setAsientoId(Long asientoId) {
        this.asientoId = asientoId;
    }

    public String getCodigoQr() {
        return codigoQr;
    }

    public void setCodigoQr(String codigoQr) {
        this.codigoQr = codigoQr;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public LocalDateTime getFechaEmision() {
        return fechaEmision;
    }

    public void setFechaEmision(LocalDateTime fechaEmision) {
        this.fechaEmision = fechaEmision;
    }

    public LocalDateTime getFechaUso() {
        return fechaUso;
    }

    public void setFechaUso(LocalDateTime fechaUso) {
        this.fechaUso = fechaUso;
    }

    public Long getEventoId() {
        return eventoId;
    }

    public void setEventoId(Long eventoId) {
        this.eventoId = eventoId;
    }
}
