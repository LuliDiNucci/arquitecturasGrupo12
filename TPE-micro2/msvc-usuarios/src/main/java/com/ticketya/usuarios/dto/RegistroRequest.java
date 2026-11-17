package com.ticketya.usuarios.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

@JsonIgnoreProperties({"rol", "roles"})
public class RegistroRequest {
    @NotBlank @Size(max = 100) @Schema(example = "Ana")
    private String nombre;
    @NotBlank @Size(max = 100) @Schema(example = "Pérez")
    private String apellido;
    @NotBlank @Email @Size(max = 254) @Schema(example = "ana@example.com")
    private String email;
    @NotBlank @Size(min = 12, max = 72)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Schema(example = "Ejemplo-seguro-2026!", accessMode = Schema.AccessMode.WRITE_ONLY)
    private String password;
    // BCrypt admite como máximo 72 bytes, no 72 caracteres Unicode.
    @AssertTrue(message = "La contraseña debe tener como máximo 72 bytes UTF-8")
    @Schema(hidden = true)
    public boolean isPasswordBytesValid() {
        return password == null || password.getBytes(java.nio.charset.StandardCharsets.UTF_8).length <= 72;
    }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getApellido() { return apellido; }
    public void setApellido(String apellido) { this.apellido = apellido; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email == null ? null : email.trim(); }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
