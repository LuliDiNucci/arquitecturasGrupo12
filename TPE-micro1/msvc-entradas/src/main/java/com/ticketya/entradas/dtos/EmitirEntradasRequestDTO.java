
package com.ticketya.entradas.dtos;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Datos necesarios para solicitar la emisión de entradas")
public class EmitirEntradasRequestDTO {

    @Schema(description = "Identificador de la compra confirmada", example = "125")
    private Long compraId;

    public EmitirEntradasRequestDTO() {
    }

    public EmitirEntradasRequestDTO(Long compraId) {
        this.compraId = compraId;
    }

    public Long getCompraId() {
        return compraId;
    }

    public void setCompraId(Long compraId) {
        this.compraId = compraId;
    }
}
