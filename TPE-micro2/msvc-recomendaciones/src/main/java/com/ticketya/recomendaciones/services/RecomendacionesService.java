package com.ticketya.recomendaciones.services;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;

import feign.FeignException;
import feign.RetryableException;

import com.ticketya.recomendaciones.clients.CompraClient;
import com.ticketya.recomendaciones.clients.EventoClient;
import com.ticketya.recomendaciones.dtos.EventoDetalleDTO;
import com.ticketya.recomendaciones.dtos.EventoRecomendadoDTO;
import com.ticketya.recomendaciones.dtos.EventoResumenDTO;
import com.ticketya.recomendaciones.dtos.ParticipanteDTO;
import com.ticketya.recomendaciones.exceptions.RecursoNoEncontradoException;
import com.ticketya.recomendaciones.models.Preferencias;
import com.ticketya.recomendaciones.repositories.PreferenciasRepository;

@Service
public class RecomendacionesService {

    private final PreferenciasRepository repository;
    private final EventoClient eventoClient;
    private final CompraClient compraClient;

    public RecomendacionesService(
            PreferenciasRepository repository,
            EventoClient eventoClient,
            CompraClient compraClient) {

        this.repository = repository;
        this.eventoClient = eventoClient;
        this.compraClient = compraClient;
    }

    public List<EventoRecomendadoDTO> obtenerRecomendaciones(
            Long clienteId) {

        Preferencias preferencias = repository.findByClienteId(clienteId)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No existen preferencias para este cliente"));

        // Obtener eventos publicados
        List<EventoResumenDTO> eventos = eventoClient.listarEventosPublicados();

        // Obtener sugerencias basadas en compras confirmadas
        List<Long> historial = obtenerHistorialSiDisponible(clienteId);

        Set<Long> eventosPorHistorial = new HashSet<>();

        if (historial != null) {
            eventosPorHistorial.addAll(historial);
        }

        List<EventoRecomendadoDTO> recomendaciones = new ArrayList<>();

        if (eventos == null || eventos.isEmpty()) {
            return recomendaciones;
        }

        for (EventoResumenDTO evento : eventos) {

            if (!esEventoRecomendable(evento)) {
                continue;
            }

            // Coincidencia por tipo de evento
            boolean coincideTipo = coincideEnLista(
                    preferencias.getTiposEventos(),
                    evento.getTipo());

            // Coincidencia por ciudad
            boolean coincideCiudad = coincideTexto(
                    preferencias.getCiudad(),
                    evento.getCiudad());

            // Coincidencia por historial de compras
            boolean coincideHistorial = eventosPorHistorial.contains(evento.getId());

            // Obtener participantes del evento
            EventoDetalleDTO detalle = eventoClient.obtenerEvento(evento.getId());

            boolean coincideArtista = false;
            String artistaPrincipal = null;

            if (detalle != null && detalle.participantes() != null) {

                for (ParticipanteDTO participante : detalle.participantes()) {

                    if (participante == null
                            || participante.nombre() == null) {
                        continue;
                    }

                    if (artistaPrincipal == null) {
                        artistaPrincipal = participante.nombre();
                    }

                    if (coincideEnLista(
                            preferencias.getArtistasFavoritos(),
                            participante.nombre())) {

                        coincideArtista = true;
                    }
                }
            }

            // Recomendar cuando coincide al menos un criterio
            if (coincideTipo
                    || coincideCiudad
                    || coincideArtista
                    || coincideHistorial) {

                EventoRecomendadoDTO recomendado = new EventoRecomendadoDTO(
                        evento.getId(),
                        evento.getNombre(),
                        evento.getTipo(),
                        artistaPrincipal,
                        evento.getCiudad(),
                        evento.getFechaHora(),
                        evento.getPrecioDesde() == null
                                ? null
                                : evento.getPrecioDesde().doubleValue());

                recomendaciones.add(recomendado);
            }
        }

        return recomendaciones;
    }

    private boolean esEventoRecomendable(
            EventoResumenDTO evento) {

        if (evento == null || evento.getId() == null) {
            return false;
        }

        if (!"PUBLICADO".equalsIgnoreCase(evento.getEstado())) {
            return false;
        }

        return evento.getFechaHora() != null
                && evento.getFechaHora().isAfter(LocalDateTime.now());
    }

    private List<Long> obtenerHistorialSiDisponible(Long clienteId) {
        try {
            return compraClient.obtenerEventosRecomendados(clienteId);
        } catch (RetryableException ex) {
            return List.of();
        } catch (FeignException ex) {
            if (ex.status() >= 500 && ex.status() <= 599) {
                return List.of();
            }
            throw ex;
        }
    }

    private boolean coincideEnLista(
            List<String> preferencias,
            String valor) {

        if (preferencias == null || valor == null) {
            return false;
        }

        for (String preferencia : preferencias) {

            if (coincideTexto(preferencia, valor)) {
                return true;
            }
        }

        return false;
    }

    private boolean coincideTexto(
            String primero,
            String segundo) {

        return primero != null
                && segundo != null
                && primero.trim().equalsIgnoreCase(segundo.trim());
    }
}
