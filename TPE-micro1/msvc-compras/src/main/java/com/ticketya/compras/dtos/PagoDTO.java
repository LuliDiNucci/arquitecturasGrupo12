
package com.ticketya.compras.dtos;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Información y resultado de un pago")
public class PagoDTO {

    @Schema(description = "ID del pago", example = "1")
    private Long id;

    @Schema(description = "ID de la compra asociada", example = "1")
    private Long compraId;

    @Schema(description = "Importe procesado", example = "30000.00")
    private BigDecimal importe;

    @Schema(description = "Medio de pago utilizado", example = "TARJETA")
    private String medioPago;

    @Schema(description = "Estado del pago", example = "APROBADO", allowableValues = {
            "PENDIENTE", "PROCESANDO", "APROBADO",
            "RECHAZADO", "ERROR"
    })
    private String estado;

    @Schema(description = "Fecha y hora de solicitud", example = "2026-11-05T21:15:00")
    private LocalDateTime fechaSolicitud;

    @Schema(description = "Fecha y hora del resultado del pago", example = "2026-11-05T21:15:02")
    private LocalDateTime fechaPago;

    @Schema(description = "Identificador de la operación en la pasarela simulada", example = "MOCK-PAGO-001")
    private String referenciaExterna;

    public PagoDTO() {
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

    public LocalDateTime getFechaSolicitud() {
        return fechaSolicitud;
    }

    public void setFechaSolicitud(LocalDateTime fechaSolicitud) {
        this.fechaSolicitud = fechaSolicitud;
    }

    public LocalDateTime getFechaPago() {
        return fechaPago;
    }

    public void setFechaPago(LocalDateTime fechaPago) {
        this.fechaPago = fechaPago;
    }

    public String getReferenciaExterna() {
        return referenciaExterna;
    }

    public void setReferenciaExterna(String referenciaExterna) {
        this.referenciaExterna = referenciaExterna;
    }
}
