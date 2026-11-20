
package com.ticketya.entradas.services;

import java.util.List;
import com.ticketya.entradas.dtos.EntradaDTO;
import com.ticketya.entradas.dtos.ResultadoEmisionDTO;
import com.ticketya.entradas.dtos.EntradaReporteDTO;

public interface EntradaService {

    ResultadoEmisionDTO emitir(Long compraId);

    EntradaDTO obtener(Long id, Long clienteId);

    List<EntradaDTO> obtenerPorCliente(Long clienteId);

    List<EntradaDTO> obtenerPorEvento(Long eventoId);

    void invalidarPorEvento(Long eventoId);

    EntradaDTO validarQr(String codigoQr, Long eventoId);

    List<EntradaReporteDTO> obtenerEntradasReportePorEvento(Long eventoId);
}
