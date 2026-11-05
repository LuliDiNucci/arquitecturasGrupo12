package ar.edu.unicen.ticketya.eventos.service;

import ar.edu.unicen.ticketya.eventos.dto.*;
import ar.edu.unicen.ticketya.eventos.model.*;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Define las operaciones de organizador.
 * La logica se encuentra en la clase de implementacion.
 */
public interface OrganizadorService {

    OrganizadorResponseDTO crearOrganizador(OrganizadorRequestDTO request);

    List<OrganizadorResponseDTO> listarOrganizadores();

    Organizador buscarOrganizador(Long id);

    OrganizadorResponseDTO aOrganizadorResponseDTO(Organizador organizador);
}
