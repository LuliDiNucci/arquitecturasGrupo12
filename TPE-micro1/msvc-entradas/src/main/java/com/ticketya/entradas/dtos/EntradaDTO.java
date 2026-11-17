
package com.ticketya.entradas.dtos;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Información de una entrada emitida")
public class EntradaDTO {

    @Schema(description = "Identificador de la entrada", example = "10")
    private Long id;

    @Schema(description = "Identificador de la compra", example = "125")
    private Long compraId;

    @Schema(description = "Identificador de la reserva", example = "50")
    private Long reservaId;

    @Schema(description = "Identificador del detalle de reserva", example = "8")
    private Long detalleReservaId;

    @Schema(description = "Índice del lugar dentro del detalle", example = "1")
    private Integer indiceLugar;

    @Schema(description = "Identificador del cliente titular", example = "20")
    private Long clienteId;

    @Schema(description = "Identificador del evento", example = "15")
    private Long eventoId;

    @Schema(description = "Identificador del sector", example = "3")
    private Long sectorId;

    @Schema(description = "Identificador del asiento. Puede ser nulo si no es numerado", example = "45")
    private Long asientoId;

    @Schema(description = "Código QR único de la entrada", example = "a8f3c2d1")
    private String codigoQr;

    @Schema(description = "Estado actual de la entrada", example = "EMITIDA")
    private String estado;

    @Schema(description = "Fecha y hora de emisión", example = "2026-11-08T15:30:00")
    private LocalDateTime fechaEmision;

    @Schema(description = "Imagen del código QR codificada en Base64")
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
