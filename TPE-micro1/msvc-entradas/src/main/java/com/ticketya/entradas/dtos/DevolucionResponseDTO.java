package com.ticketya.entradas.dtos;

import java.time.LocalDateTime;

public class DevolucionResponseDTO {
    private Long id;
    private Long entradaId;
    private LocalDateTime fechaSolicitud;
    private String motivo;
    private String estado;
    private LocalDateTime fechaResolucion;
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
