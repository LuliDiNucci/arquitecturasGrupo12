package com.ticketya.compras.dtos;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Reintegro de una entrada individual")
public class ReintegroDTO {
    @Schema(description = "ID del reintegro", example = "1")
    private Long id;
    @Schema(description = "ID del pago original", example = "1")
    private Long pagoId;
    @Schema(description = "ID de compra", example = "1")
    private Long compraId;
    @Schema(description = "ID de entrada reintegrada", example = "100")
    private Long entradaId;
    @Schema(description = "Importe histórico por entrada", example = "10000.00")
    private BigDecimal importe;
    @Schema(description = "Estado del reintegro", example = "APROBADO")
    private String estado;
    @Schema(description = "Motivo", example = "DEVOLUCION_CLIENTE")
    private String motivo;
    @Schema(description = "Fecha de solicitud", example = "2026-11-05T22:30:00")
    private LocalDateTime fechaSolicitud;
    @Schema(description = "Fecha de aprobación", example = "2026-11-05T22:30:02")
    private LocalDateTime fechaReintegro;
    @Schema(description = "Referencia de la pasarela simulada", example = "MOCK-REINTEGRO-...")
    private String referenciaExterna;

    public ReintegroDTO() { }
    public ReintegroDTO(Long id, Long pagoId, Long compraId, Long entradaId,
                        BigDecimal importe, String estado, String motivo,
                        LocalDateTime fechaSolicitud, LocalDateTime fechaReintegro,
                        String referenciaExterna) {
        this.id = id;
        this.pagoId = pagoId;
        this.compraId = compraId;
        this.entradaId = entradaId;
        this.importe = importe;
        this.estado = estado;
        this.motivo = motivo;
        this.fechaSolicitud = fechaSolicitud;
        this.fechaReintegro = fechaReintegro;
        this.referenciaExterna = referenciaExterna;
    }
    public Long getId() { return id; }
    public Long getPagoId() { return pagoId; }
    public Long getCompraId() { return compraId; }
    public Long getEntradaId() { return entradaId; }
    public BigDecimal getImporte() { return importe; }
    public String getEstado() { return estado; }
    public String getMotivo() { return motivo; }
    public LocalDateTime getFechaSolicitud() { return fechaSolicitud; }
    public LocalDateTime getFechaReintegro() { return fechaReintegro; }
    public String getReferenciaExterna() { return referenciaExterna; }
}
