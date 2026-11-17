
package com.ticketya.reservas.dtos;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Solicitud interna para liberar una entrada " +
        "cuya devolución fue aprobada")
public class DevolverEntradaDTO {

    @Schema(description = "ID único de la entrada que se devuelve", example = "1001", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long entradaId;

    @Schema(description = "ID del sector al que pertenece la entrada", example = "5", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long sectorId;

    @Schema(description = "ID del asiento que se libera. " +
            "Se utiliza cuando el sector es numerado.", example = "25")
    private Long asientoId;

    public DevolverEntradaDTO() {
    }

    public Long getEntradaId() {
        return entradaId;
    }

    public void setEntradaId(Long entradaId) {
        this.entradaId = entradaId;
    }

    public Long getSectorId() {
        return sectorId;
    }

    public void setSectorId(Long sectorId) {
        this.sectorId = sectorId;
    }

    public Long getAsientoId() {
        return asientoId;
    }

    public void setAsientoId(Long asientoId) {
        this.asientoId = asientoId;
    }
}
