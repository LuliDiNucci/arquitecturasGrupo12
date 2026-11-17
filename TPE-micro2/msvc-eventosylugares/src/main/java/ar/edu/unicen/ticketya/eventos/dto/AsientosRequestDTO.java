package ar.edu.unicen.ticketya.eventos.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.List;

public class AsientosRequestDTO {
    @NotEmpty
    @Schema(example = "[{\"fila\":\"A\",\"numero\":1},{\"fila\":\"A\",\"numero\":2}]")
    private List<@NotNull @Valid AsientoRequestDTO> asientos;

    public AsientosRequestDTO() {
    }

    public AsientosRequestDTO(List<@NotNull @Valid AsientoRequestDTO> asientos) {
        this.asientos = asientos;
    }

    public List<@NotNull @Valid AsientoRequestDTO> getAsientos() {
        return asientos;
    }

    public void setAsientos(List<@NotNull @Valid AsientoRequestDTO> asientos) {
        this.asientos = asientos;
    }
}
