package ar.edu.unicen.ticketya.eventos.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public class OrganizadorResponseDTO {
    @Schema(example = "1")
    private Long id;
    @Schema(example = "Producciones Sierra")
    private String nombre;
    @Schema(example = "contacto@ejemplo.com")
    private String email;

    public OrganizadorResponseDTO() {
    }

    public OrganizadorResponseDTO(Long id, String nombre, String email) {
        this.id = id;
        this.nombre = nombre;
        this.email = email;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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
