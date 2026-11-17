package ar.edu.unicen.ticketya.eventos.service.impl;

import ar.edu.unicen.ticketya.eventos.service.*;

import ar.edu.unicen.ticketya.eventos.dto.OrganizadorRequestDTO;
import ar.edu.unicen.ticketya.eventos.dto.OrganizadorResponseDTO;
import ar.edu.unicen.ticketya.eventos.dto.UsuarioValidacionResponseDTO;
import ar.edu.unicen.ticketya.eventos.exception.ReglaNegocioException;
import ar.edu.unicen.ticketya.eventos.feign.UsuariosFeignClient;
import ar.edu.unicen.ticketya.eventos.model.Organizador;
import ar.edu.unicen.ticketya.eventos.repository.OrganizadorRepository;
import feign.FeignException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class OrganizadorServiceImpl implements OrganizadorService {

    private final OrganizadorRepository organizadorRepository;
    private final UsuariosFeignClient usuariosFeignClient;

    public OrganizadorServiceImpl(OrganizadorRepository organizadorRepository,
                                 UsuariosFeignClient usuariosFeignClient) {
        this.organizadorRepository = organizadorRepository;
        this.usuariosFeignClient = usuariosFeignClient;
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
    @Transactional
    public OrganizadorResponseDTO vincularUsuario(Long id, Long usuarioId) {
        Organizador organizador = buscarOrganizador(id);
        if (usuarioId == null || usuarioId <= 0) {
            throw ReglaNegocioException.invalido("usuarioId debe ser positivo");
        }
        validarUsuarioOrganizador(usuarioId);
        organizador.setUsuarioId(usuarioId);
        return aOrganizadorResponseDTO(organizadorRepository.saveAndFlush(organizador));
    }

    private void validarUsuarioOrganizador(Long usuarioId) {
        UsuarioValidacionResponseDTO usuario;
        try {
            usuario = usuariosFeignClient.obtenerUsuario(usuarioId);
        } catch (FeignException.NotFound ex) {
            throw ReglaNegocioException.noEncontrado("Usuario inexistente: " + usuarioId);
        } catch (FeignException ex) {
            throw new ReglaNegocioException(HttpStatus.SERVICE_UNAVAILABLE,
                    "No se pudo validar el usuario con msvc-usuarios");
        }
        if (usuario == null || !usuarioId.equals(usuario.getId())
                || usuario.getActivo() == null || usuario.getRoles() == null) {
            throw new ReglaNegocioException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Respuesta de validacion de msvc-usuarios invalida");
        }
        if (!usuario.getActivo()) {
            throw ReglaNegocioException.conflicto("El usuario no esta activo");
        }
        if (!usuario.getRoles().contains("ORGANIZADOR")) {
            throw ReglaNegocioException.conflicto("El usuario debe tener el rol ORGANIZADOR");
        }
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
