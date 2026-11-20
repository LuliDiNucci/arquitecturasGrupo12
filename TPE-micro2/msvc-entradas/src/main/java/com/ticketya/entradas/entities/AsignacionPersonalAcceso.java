package com.ticketya.entradas.entities;

import jakarta.persistence.*;

/** Relación mínima para que PERSONAL_ACCESO no pueda validar cualquier evento. */
@Entity
@Table(name = "asignaciones_personal_acceso", uniqueConstraints =
    @UniqueConstraint(name = "uk_acceso_evento_usuario", columnNames = {"evento_id", "usuario_id"}))
public class AsignacionPersonalAcceso {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "evento_id", nullable = false) private Long eventoId;
    @Column(name = "usuario_id", nullable = false) private Long usuarioId;
    protected AsignacionPersonalAcceso() {}
    public AsignacionPersonalAcceso(Long eventoId, Long usuarioId) {
        this.eventoId = eventoId; this.usuarioId = usuarioId;
    }
    public Long getId() { return id; }
    public Long getEventoId() { return eventoId; }
    public Long getUsuarioId() { return usuarioId; }
}
