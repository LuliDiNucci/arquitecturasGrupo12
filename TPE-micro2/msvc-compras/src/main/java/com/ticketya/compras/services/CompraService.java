package com.ticketya.compras.services;

import com.ticketya.compras.dtos.CompraDTO;
import com.ticketya.compras.dtos.ConfirmacionCompraDTO;
import com.ticketya.compras.dtos.IniciarCompraDTO;

public interface CompraService {
    CompraDTO iniciarCompra(IniciarCompraDTO solicitud);
    CompraDTO obtenerCompra(Long id);
    CompraDTO confirmarCompra(Long compraId);
    ConfirmacionCompraDTO consultarConfirmacion(Long compraId);
}
