package ar.edu.unicen.ticketya.eventos.service;

import ar.edu.unicen.ticketya.eventos.dto.EventoResponseDTO;

public interface AvisoArtistasFavoritosService {

    void notificarPublicacion(EventoResponseDTO evento);
}