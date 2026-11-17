
package com.ticketya.msvc_fila_virtual.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Solicitud para habilitar una tanda de clientes")
public record HabilitarTandaDTO(

        @Schema(description = "Cantidad máxima de clientes a habilitar", example = "3", requiredMode = Schema.RequiredMode.REQUIRED) @NotNull @Min(1) @Max(100) Integer cantidad

) {
}
