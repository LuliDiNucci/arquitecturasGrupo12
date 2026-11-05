package ar.edu.unicen.ticketya.eventos.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.List;

public class PreciosEventoRequestDTO {
    @NotEmpty
    @Schema(example = "[{\"sectorId\":1,\"precio\":35000.00}]")
    private List<@NotNull @Valid PrecioSectorRequestDTO> precios;

    public PreciosEventoRequestDTO() {
    }

    public PreciosEventoRequestDTO(List<@NotNull @Valid PrecioSectorRequestDTO> precios) {
        this.precios = precios;
    }

    public List<@NotNull @Valid PrecioSectorRequestDTO> getPrecios() {
        return precios;
    }

    public void setPrecios(List<@NotNull @Valid PrecioSectorRequestDTO> precios) {
        this.precios = precios;
    }
}
