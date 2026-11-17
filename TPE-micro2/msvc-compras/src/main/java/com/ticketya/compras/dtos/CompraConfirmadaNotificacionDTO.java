package com.ticketya.compras.dtos;

/** Contrato POST /notificaciones/compras-confirmadas. */
public record CompraConfirmadaNotificacionDTO(DestinatarioNotificacionDTO destinatario,
                                               Long compraId, String nombreEvento) {
}
