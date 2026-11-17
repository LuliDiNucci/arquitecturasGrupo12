package ar.edu.unicen.ticketya.eventos.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Set;

/** Datos de identidad necesarios para vincular; no comparte entidades de Usuarios. */
@JsonIgnoreProperties(ignoreUnknown = true)
public class UsuarioValidacionResponseDTO {
    private Long id;
    private Boolean activo;
    private Set<String> roles;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }
    public Set<String> getRoles() { return roles; }
    public void setRoles(Set<String> roles) { this.roles = roles; }
}
