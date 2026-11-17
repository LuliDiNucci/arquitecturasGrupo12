
package com.ticketya.recomendaciones.services;

import com.ticketya.recomendaciones.dtos.EventoResumenDTO;
import com.ticketya.recomendaciones.dtos.EventoRecomendadoDTO;
import com.ticketya.recomendaciones.models.Preferencias;
import com.ticketya.recomendaciones.repositories.PreferenciasRepository;
import com.ticketya.recomendaciones.clients.EventoClient;
import com.ticketya.recomendaciones.exceptions.RecursoNoEncontradoException;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.ArrayList;

@Service
public class RecomendacionesService {

    private final PreferenciasRepository repository;
    private final EventoClient eventoClient;

    public RecomendacionesService(
            PreferenciasRepository repository,
            EventoClient eventoClient) {

        this.repository = repository;
        this.eventoClient = eventoClient;
    }

    // OBTENER RECOMENDACIONES
    public List<EventoRecomendadoDTO> obtenerRecomendaciones(
            Long clienteId) {

        // Buscar las preferencias del cliente
        Preferencias preferencias = repository.findByClienteId(clienteId)
            .orElseThrow(() -> new RecursoNoEncontradoException(
                "No existen preferencias para este cliente"
            ));

        // Obtener eventos publicados mediante Feign
        List<EventoResumenDTO> eventos =
            eventoClient.listarEventosPublicados();

        List<EventoRecomendadoDTO> recomendaciones =
            new ArrayList<>();

        // Recorrer los eventos
        for (EventoResumenDTO evento : eventos) {

            boolean coincideTipo =
                preferencias.getTiposEventos() != null &&
                preferencias.getTiposEventos().stream()
                    .anyMatch(tipo -> tipo.equalsIgnoreCase(
                        evento.getTipo() == null ? "" : evento.getTipo()
                    ));

            boolean coincideCiudad =
                preferencias.getCiudad() != null &&
                preferencias.getCiudad().equalsIgnoreCase(
                    evento.getCiudad() == null ? "" : evento.getCiudad()
                );

            // Recomendar si coincide el tipo o la ciudad
            if (coincideTipo || coincideCiudad) {

                EventoRecomendadoDTO dto =
                    new EventoRecomendadoDTO(
                        evento.getId(),
                        evento.getNombre(),
                        evento.getTipo(),
                        null,
                        evento.getCiudad(),
                        evento.getFechaHora(),
                        evento.getPrecioDesde() != null
                            ? evento.getPrecioDesde().doubleValue()
                            : null
                    );

                recomendaciones.add(dto);
            }
        }

        return recomendaciones;
    }
}
