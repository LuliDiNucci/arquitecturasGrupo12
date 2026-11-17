package com.ticketya.msvc_fila_virtual.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Schema(description = "Datos necesarios para ingresar a la fila virtual de un evento")
public record IngresarFilaDTO(

        @Schema(description = "Identificador del cliente que desea ingresar a la fila", example = "10", requiredMode = Schema.RequiredMode.REQUIRED) @NotNull @Positive Long clienteId

) {
}