package com.ticketya.usuarios.dto;

import com.ticketya.usuarios.entities.Rol;
import jakarta.validation.constraints.*;
import java.util.Set;

public class RolesRequest {
    @NotEmpty
    private Set<@NotNull Rol> roles;
    public Set<Rol> getRoles() { return roles; }
    public void setRoles(Set<Rol> roles) { this.roles = roles; }
}
