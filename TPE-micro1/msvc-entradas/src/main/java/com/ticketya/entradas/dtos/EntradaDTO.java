
package com.ticketya.entradas.dtos;

import java.time.LocalDateTime;

public class EntradaDTO {

    private Long id;
    private Long compraId;
    private Long reservaId;
    private Long detalleReservaId;
    private Integer indiceLugar;
    private Long clienteId;
    private Long eventoId;
    private Long sectorId;
    private Long asientoId;
    private String codigoQr;
    private String estado;
    private LocalDateTime fechaEmision;
    private String qrImagenBase64;

    public EntradaDTO() {
    }

    public EntradaDTO(
            Long id,
            Long compraId,
            Long reservaId,
            Long detalleReservaId,
            Integer indiceLugar,
            Long clienteId,
            Long eventoId,
            Long sectorId,
            Long asientoId,
            String codigoQr,
            String estado,
            LocalDateTime fechaEmision,
            String qrImagenBase64) {

        this.id = id;
        this.compraId = compraId;
        this.reservaId = reservaId;
        this.detalleReservaId = detalleReservaId;
        this.indiceLugar = indiceLugar;
        this.clienteId = clienteId;
        this.eventoId = eventoId;
        this.sectorId = sectorId;
        this.asientoId = asientoId;
        this.codigoQr = codigoQr;
        this.estado = estado;
        this.fechaEmision = fechaEmision;
        this.qrImagenBase64 = qrImagenBase64;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getQrImagenBase64() {
        return qrImagenBase64;
    }

    public void setQrImagenBase64(String qrImagenBase64) {
        this.qrImagenBase64 = qrImagenBase64;
    }
}
