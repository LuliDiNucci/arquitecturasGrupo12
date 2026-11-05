package ar.edu.unicen.ticketya.eventos.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

public class LugarResponseDTO {
    @Schema(example = "1")
    private Long id;
    @Schema(example = "Estadio Municipal")
    private String nombre;
    @Schema(example = "Av. Central 150")
    private String direccion;
    @Schema(example = "Tandil")
    private String ciudad;
    @Schema(example = "-37.321000")
    private BigDecimal latitud;
    @Schema(example = "-59.137000")
    private BigDecimal longitud;

    public LugarResponseDTO() {
    }

    public LugarResponseDTO(Long id, String nombre, String direccion, String ciudad, BigDecimal latitud, BigDecimal longitud) {
        this.id = id;
        this.nombre = nombre;
        this.direccion = direccion;
        this.ciudad = ciudad;
        this.latitud = latitud;
        this.longitud = longitud;
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

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getCiudad() {
        return ciudad;
    }

    public void setCiudad(String ciudad) {
        this.ciudad = ciudad;
    }

    public BigDecimal getLatitud() {
        return latitud;
    }

    public void setLatitud(BigDecimal latitud) {
        this.latitud = latitud;
    }

    public BigDecimal getLongitud() {
        return longitud;
    }

    public void setLongitud(BigDecimal longitud) {
        this.longitud = longitud;
    }
}
