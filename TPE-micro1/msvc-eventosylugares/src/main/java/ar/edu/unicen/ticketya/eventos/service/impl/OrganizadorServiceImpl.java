package ar.edu.unicen.ticketya.eventos.service.impl;

import ar.edu.unicen.ticketya.eventos.service.*;

import ar.edu.unicen.ticketya.eventos.dto.OrganizadorRequestDTO;
import ar.edu.unicen.ticketya.eventos.dto.OrganizadorResponseDTO;
import ar.edu.unicen.ticketya.eventos.exception.ReglaNegocioException;
import ar.edu.unicen.ticketya.eventos.model.Organizador;
import ar.edu.unicen.ticketya.eventos.repository.OrganizadorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class OrganizadorServiceImpl implements OrganizadorService {

    private final OrganizadorRepository organizadorRepository;

    public OrganizadorServiceImpl(OrganizadorRepository organizadorRepository) {
        this.organizadorRepository = organizadorRepository;
    }

    @Transactional
    @Override
    public OrganizadorResponseDTO crearOrganizador(OrganizadorRequestDTO request) {
        Organizador organizador = new Organizador(
                request.getNombre().trim(),
                request.getEmail().trim()
        );

        return aOrganizadorResponseDTO(organizadorRepository.save(organizador));
    }

    @Transactional(readOnly = true)
    @Override
    public List<OrganizadorResponseDTO> listarOrganizadores() {
        return organizadorRepository.findAll().stream()
                .map(this::aOrganizadorResponseDTO)
                .toList();
    }

    @Override
    public Organizador buscarOrganizador(Long id) {
        return organizadorRepository.findById(id)
                .orElseThrow(() -> ReglaNegocioException.noEncontrado("Organizador inexistente: " + id));
    }

    @Override
    public OrganizadorResponseDTO aOrganizadorResponseDTO(Organizador organizador) {
        return new OrganizadorResponseDTO(
                organizador.getId(),
                organizador.getNombre(),
                organizador.getEmail()
        );
    }
}
