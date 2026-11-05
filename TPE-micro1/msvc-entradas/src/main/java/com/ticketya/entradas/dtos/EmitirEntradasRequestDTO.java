
package com.ticketya.entradas.dtos;

public class EmitirEntradasRequestDTO {

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
