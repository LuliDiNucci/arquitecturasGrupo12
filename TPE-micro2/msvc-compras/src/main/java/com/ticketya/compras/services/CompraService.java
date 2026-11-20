
package com.ticketya.compras.services;

import java.util.List;

import com.ticketya.compras.dtos.CompraDTO;
import com.ticketya.compras.dtos.ConfirmacionCompraDTO;
import com.ticketya.compras.dtos.IniciarCompraDTO;

public interface CompraService {
    CompraDTO iniciarCompra(IniciarCompraDTO solicitud);
    CompraDTO obtenerCompra(Long id);
    CompraDTO confirmarCompra(Long compraId);
    List<Long> buscarCompradores(Long eventoId, boolean incluirReintegrados);
    ConfirmacionCompraDTO consultarConfirmacion(Long compraId);
}

