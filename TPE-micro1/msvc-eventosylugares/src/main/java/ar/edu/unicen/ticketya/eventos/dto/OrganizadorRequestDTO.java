package ar.edu.unicen.ticketya.eventos.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.*;

public class OrganizadorRequestDTO {
    @NotBlank @Size(max=150)
    @Schema(example = "Producciones Sierra")
    private String nombre;
    @NotBlank @Email @Size(max=200)
    @Schema(example = "contacto@ejemplo.com")
    private String email;

    public OrganizadorRequestDTO() {
    }

    public OrganizadorRequestDTO(String nombre, String email) {
        this.nombre = nombre;
        this.email = email;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
