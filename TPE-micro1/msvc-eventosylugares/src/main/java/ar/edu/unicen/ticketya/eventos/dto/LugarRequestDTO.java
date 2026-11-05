package ar.edu.unicen.ticketya.eventos.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public class LugarRequestDTO {
    @NotBlank @Size(max=150)
    @Schema(example = "Estadio Municipal")
    private String nombre;
    @NotBlank @Size(max=220)
    @Schema(example = "Av. Central 150")
    private String direccion;
    @NotBlank @Size(max=100)
    @Schema(example = "Tandil")
    private String ciudad;
    @NotNull @DecimalMin("-90.0") @DecimalMax("90.0")
    @Schema(example = "-37.321000")
    private BigDecimal latitud;
    @NotNull @DecimalMin("-180.0") @DecimalMax("180.0")
    @Schema(example = "-59.137000")
    private BigDecimal longitud;

    public LugarRequestDTO() {
    }

    public LugarRequestDTO(String nombre, String direccion, String ciudad, BigDecimal latitud, BigDecimal longitud) {
        this.nombre = nombre;
        this.direccion = direccion;
        this.ciudad = ciudad;
        this.latitud = latitud;
        this.longitud = longitud;
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
