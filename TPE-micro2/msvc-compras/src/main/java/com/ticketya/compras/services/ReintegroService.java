
package com.ticketya.compras.services;

import java.util.List;

import com.ticketya.compras.dtos.ReintegroDTO;
import com.ticketya.compras.dtos.SolicitudReintegroDTO;

public interface ReintegroService {

    ReintegroDTO reintegrarEntrada(
            SolicitudReintegroDTO solicitud);

    List<ReintegroDTO> obtenerReintegros(Long compraId);

    void reintegrarEventoCancelado(
            Long eventoId,
            String operacionId);

    void compensarCompraSinEntradas(Long compraId);
}
