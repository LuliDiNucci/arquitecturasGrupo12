package com.ticketya.notificaciones.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record DestinatarioDTO(
        @NotNull @Positive @Schema(example = "12") Long clienteId,
        @NotBlank @Email @Size(max = 254) @Pattern(regexp = "^[^\\s,;]+$")
        @Schema(example = "cliente@example.test") String email
) {}
