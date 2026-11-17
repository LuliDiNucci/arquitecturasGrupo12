
package com.ticketya.msvc_fila_virtual.services;

import java.util.List;

import com.ticketya.msvc_fila_virtual.dtos.ClienteEnFilaDTO;
import com.ticketya.msvc_fila_virtual.dtos.ValidacionTurnoDTO;

public interface FilaVirtualService {

    ClienteEnFilaDTO ingresar(Long eventoId, Long clienteId);

    ClienteEnFilaDTO consultarEstado(Long eventoId, Long clienteId);

    List<ClienteEnFilaDTO> habilitarTanda(
            Long eventoId,
            Integer cantidad);

    ValidacionTurnoDTO validarTurno(
            Long eventoId,
            Long clienteId);

    ClienteEnFilaDTO finalizarTurno(
            Long eventoId,
            Long clienteId);
}
