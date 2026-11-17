package com.ticketya.entradas.dtos;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Información de una solicitud de devolución")
public class DevolucionResponseDTO {
    
    @Schema(description = "Identificador de la solicitud", example = "5")
    private Long id;

    @Schema(description = "Identificador de la entrada", example = "10")
    private Long entradaId;

    @Schema(description = "Fecha y hora de la solicitud", example = "2026-11-08T15:30:00")
    private LocalDateTime fechaSolicitud;

    @Schema(description = "Motivo de la devolución", example = "No puedo asistir")
    private String motivo;

    @Schema(description = "Estado de la solicitud", example = "PENDIENTE")
    private String estado;

    @Schema(description = "Fecha y hora de resolución", example = "2026-11-09T10:00:00")
    private LocalDateTime fechaResolucion;

    @Schema(description = "Identificador del administrador que resolvió la solicitud", example = "2")
    private Long administradorId;

    public DevolucionResponseDTO() {}

    public DevolucionResponseDTO(Long id, Long entradaId, LocalDateTime fechaSolicitud, String motivo, String estado, LocalDateTime fechaResolucion, Long administradorId) {
        this.id = id;
        this.entradaId = entradaId;
        this.fechaSolicitud = fechaSolicitud;
        this.motivo = motivo;
        this.estado = estado;
        this.fechaResolucion = fechaResolucion;
        this.administradorId = administradorId;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getEntradaId() { return entradaId; }
    public void setEntradaId(Long entradaId) { this.entradaId = entradaId; }

    public LocalDateTime getFechaSolicitud() { return fechaSolicitud; }
    public void setFechaSolicitud(LocalDateTime fechaSolicitud) { this.fechaSolicitud = fechaSolicitud; }

    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public LocalDateTime getFechaResolucion() { return fechaResolucion; }
    public void setFechaResolucion(LocalDateTime fechaResolucion) { this.fechaResolucion = fechaResolucion; }

    public Long getAdministradorId() { return administradorId; }
    public void setAdministradorId(Long administradorId) { this.administradorId = administradorId; }
}
