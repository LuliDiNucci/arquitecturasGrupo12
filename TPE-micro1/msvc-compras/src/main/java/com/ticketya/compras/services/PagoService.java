
package com.ticketya.compras.services;

import com.ticketya.compras.dtos.PagoDTO;
import com.ticketya.compras.dtos.ProcesarPagoDTO;

public interface PagoService {

    PagoDTO procesarPago(
            Long compraId,
            ProcesarPagoDTO solicitud);

    void reintentarLiberacion(Long compraId);
}
