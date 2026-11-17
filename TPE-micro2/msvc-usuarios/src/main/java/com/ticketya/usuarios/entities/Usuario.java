package com.ticketya.usuarios.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

@Entity
@Table(name = "usuarios", uniqueConstraints = @UniqueConstraint(name = "uk_usuario_email", columnNames = "email"))
public class Usuario {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 100)
    private String nombre;
    @Column(nullable = false, length = 100)
    private String apellido;
    @Column(nullable = false, length = 254)
    private String email;
    @JsonIgnore
    @Column(nullable = false, length = 100)
    private String passwordHash;
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "usuario_roles", joinColumns = @JoinColumn(name = "usuario_id"),
        uniqueConstraints = @UniqueConstraint(columnNames = {"usuario_id", "rol"}))
    @Column(name = "rol", nullable = false, length = 30)
    @Enumerated(EnumType.STRING)
    private Set<Rol> roles = new HashSet<>();
    @Column(nullable = false)
    private boolean activo = true;
    @Column(nullable = false, updatable = false)
    private Instant fechaCreacion = Instant.now();
    @Version
    private Long version;

    protected Usuario() { }
    public Usuario(String nombre, String apellido, String email, String passwordHash, Set<Rol> roles) {
        this.nombre = nombre.trim();
        this.apellido = apellido.trim();
        this.email = normalizarEmail(email);
        this.passwordHash = passwordHash;
        this.roles = new HashSet<>(roles);
    }
    public static String normalizarEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }
    @PrePersist @PreUpdate
    private void normalizar() { email = normalizarEmail(email); }
    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public String getApellido() { return apellido; }
    public String getEmail() { return email; }
    @JsonIgnore
    public String getPasswordHash() { return passwordHash; }
    public Set<Rol> getRoles() { return Set.copyOf(roles); }
    public void setRoles(Set<Rol> roles) { this.roles = new HashSet<>(roles); }
    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
    public Instant getFechaCreacion() { return fechaCreacion; }
}
