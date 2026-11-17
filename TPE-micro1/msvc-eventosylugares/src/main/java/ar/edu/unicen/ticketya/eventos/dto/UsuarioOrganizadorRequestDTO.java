package ar.edu.unicen.ticketya.eventos.dto;

import jakarta.validation.constraints.*;

public class UsuarioOrganizadorRequestDTO {
    @NotNull @Positive private Long usuarioId;
    public Long getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Long usuarioId) { this.usuarioId = usuarioId; }
}
