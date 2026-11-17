package com.ticketya.usuarios.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

public class LoginRequest {
    @NotBlank @Email @Size(max = 254) @Schema(example = "ana@example.com")
    private String email;
    @NotBlank @Size(max = 72)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Schema(example = "Ejemplo-seguro-2026!", accessMode = Schema.AccessMode.WRITE_ONLY)
    private String password;
    @AssertTrue(message = "La contraseña debe tener como máximo 72 bytes UTF-8")
    @Schema(hidden = true)
    public boolean isPasswordBytesValid() {
        return password == null || password.getBytes(java.nio.charset.StandardCharsets.UTF_8).length <= 72;
    }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email == null ? null : email.trim(); }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
