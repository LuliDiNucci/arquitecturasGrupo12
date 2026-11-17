package com.ticketya.compras.dtos;

import java.util.List;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Contrato propuesto para msvc-entradas. Acordar ruta y campos con el equipo.
 * La clave hace idempotente la emisión de todas las entradas de una compra.
 */
@Schema(description = "Solicitud idempotente de emisión de entradas para una compra")
public class SolicitudEmisionDTO {
    @Schema(description = "ID de compra", example = "1")
    private Long compraId;
    @Schema(description = "ID del cliente", example = "200")
    private Long clienteId;
    @Schema(description = "ID del evento", example = "21")
    private Long eventoId;
    @Schema(description = "Clave estable de emisión", example = "EMISION-COMPRA-1")
    private String claveIdempotencia;
    @Schema(description = "Una solicitud por cada item comprado")
    private List<ItemEmisionDTO> items;

    public SolicitudEmisionDTO() { }
    public SolicitudEmisionDTO(Long compraId, Long clienteId, Long eventoId,
                               String claveIdempotencia, List<ItemEmisionDTO> items) {
        this.compraId = compraId;
        this.clienteId = clienteId;
        this.eventoId = eventoId;
        this.claveIdempotencia = claveIdempotencia;
        this.items = items;
    }
    public Long getCompraId() { return compraId; }
    public void setCompraId(Long compraId) { this.compraId = compraId; }
    public Long getClienteId() { return clienteId; }
    public void setClienteId(Long clienteId) { this.clienteId = clienteId; }
    public Long getEventoId() { return eventoId; }
    public void setEventoId(Long eventoId) { this.eventoId = eventoId; }
    public String getClaveIdempotencia() { return claveIdempotencia; }
    public void setClaveIdempotencia(String claveIdempotencia) { this.claveIdempotencia = claveIdempotencia; }
    public List<ItemEmisionDTO> getItems() { return items; }
    public void setItems(List<ItemEmisionDTO> items) { this.items = items; }
}
