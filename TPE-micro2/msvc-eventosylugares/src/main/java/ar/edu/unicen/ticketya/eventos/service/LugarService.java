package ar.edu.unicen.ticketya.eventos.service;

import ar.edu.unicen.ticketya.eventos.dto.*;
import ar.edu.unicen.ticketya.eventos.model.*;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Define las operaciones de lugar.
 * La logica se encuentra en la clase de implementacion.
 */
public interface LugarService {

    LugarResponseDTO crearLugar(LugarRequestDTO request);

    List<LugarResponseDTO> listarLugares();

    LugarResponseDTO obtenerLugar(Long id);

    Lugar buscarLugar(Long id);

    Lugar bloquearLugar(Long id);

    void validarLugarEditable(Long lugarId);

    LugarResponseDTO aLugarResponseDTO(Lugar lugar);
}
