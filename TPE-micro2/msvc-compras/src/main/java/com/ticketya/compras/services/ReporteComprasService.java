
package com.ticketya.compras.services;

import com.ticketya.compras.dtos.ReporteComprasEventoDTO;

public interface ReporteComprasService {

    ReporteComprasEventoDTO obtenerReporte(Long eventoId);
}