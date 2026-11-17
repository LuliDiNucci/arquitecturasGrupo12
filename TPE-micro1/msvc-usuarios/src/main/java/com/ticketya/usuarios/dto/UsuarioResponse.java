package com.ticketya.usuarios.dto;

import com.ticketya.usuarios.entities.*;
import java.time.Instant;
import java.util.Set;

public class UsuarioResponse {
    private final Long id;
    private final String nombre;
    private final String apellido;
    private final String email;
    private final Set<Rol> roles;
    private final boolean activo;
    private final Instant fechaCreacion;
    public UsuarioResponse(Usuario usuario) {
        id = usuario.getId(); nombre = usuario.getNombre(); apellido = usuario.getApellido();
        email = usuario.getEmail(); roles = usuario.getRoles(); activo = usuario.isActivo();
        fechaCreacion = usuario.getFechaCreacion();
    }
    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public String getApellido() { return apellido; }
    public String getEmail() { return email; }
    public Set<Rol> getRoles() { return roles; }
    public boolean isActivo() { return activo; }
    public Instant getFechaCreacion() { return fechaCreacion; }
}
