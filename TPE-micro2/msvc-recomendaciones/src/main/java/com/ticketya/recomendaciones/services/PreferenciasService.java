package com.ticketya.recomendaciones.services;

import com.ticketya.recomendaciones.dtos.PreferenciasEntradaDTO;
import com.ticketya.recomendaciones.dtos.PreferenciasSalidaDTO;

import com.ticketya.recomendaciones.models.Preferencias;
import com.ticketya.recomendaciones.repositories.PreferenciasRepository;

import com.ticketya.recomendaciones.exceptions.RecursoNoEncontradoException;
import com.ticketya.recomendaciones.exceptions.PreferenciasExistentesException;

import org.springframework.stereotype.Service;

import com.ticketya.recomendaciones.clients.EventoClient;
import com.ticketya.recomendaciones.dtos.EventoResumenDTO;
import java.util.List;

@Service
public class PreferenciasService {

    private final PreferenciasRepository repository;
    private final EventoClient eventoClient;

    public PreferenciasService(
            PreferenciasRepository repository,
            EventoClient eventoClient) {

        this.repository = repository;
        this.eventoClient = eventoClient;
    }

    // REGISTRAR PREFERENCIAS
    public PreferenciasSalidaDTO registrar(
            Long clienteId, PreferenciasEntradaDTO dto) {

        // Verificar si el cliente ya tiene preferencias
        if (repository.existsByClienteId(clienteId)) {
            throw new PreferenciasExistentesException(
                "El cliente ya tiene preferencias registradas"
            );
        }

        // Crear las preferencias
        Preferencias preferencias = new Preferencias(
            clienteId,
            dto.getTiposEventos(),
            dto.getArtistasFavoritos(),
            dto.getCiudad()
        );

        // Guardar en MongoDB
        Preferencias guardadas = repository.save(preferencias);

        return convertirDTO(guardadas);
    }

    // CONSULTAR PREFERENCIAS
    public PreferenciasSalidaDTO consultar(Long clienteId) {

        Preferencias preferencias = repository.findByClienteId(clienteId)
            .orElseThrow(() -> new RecursoNoEncontradoException(
                "No existen preferencias para este cliente"
            ));

        return convertirDTO(preferencias);
    }

    // MODIFICAR PREFERENCIAS
    public PreferenciasSalidaDTO modificar(
            Long clienteId, PreferenciasEntradaDTO dto) {

        Preferencias preferencias = repository.findByClienteId(clienteId)
            .orElseThrow(() -> new RecursoNoEncontradoException(
                "No existen preferencias para este cliente"
            ));

        preferencias.setTiposEventos(dto.getTiposEventos());
        preferencias.setArtistasFavoritos(dto.getArtistasFavoritos());
        preferencias.setCiudad(dto.getCiudad());

        Preferencias actualizadas = repository.save(preferencias);

        return convertirDTO(actualizadas);
    }

    // CONVERTIR MODELO A DTO DE SALIDA
    private PreferenciasSalidaDTO convertirDTO(
            Preferencias preferencias) {

        return new PreferenciasSalidaDTO(
            preferencias.getClienteId(),
            preferencias.getTiposEventos(),
            preferencias.getArtistasFavoritos(),
            preferencias.getCiudad()
        );
    }

// CONSULTAR EVENTOS PUBLICADOS
public List<EventoResumenDTO> consultarEventosPublicados() {
    return eventoClient.listarEventosPublicados();
}
}
