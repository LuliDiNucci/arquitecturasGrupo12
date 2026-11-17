package com.ticketya.notificaciones.services;

import com.ticketya.notificaciones.dtos.ArtistaFavoritoRequestDTO;
import com.ticketya.notificaciones.dtos.CompraConfirmadaRequestDTO;
import com.ticketya.notificaciones.dtos.EventoCanceladoRequestDTO;
import com.ticketya.notificaciones.dtos.EventoModificadoRequestDTO;
import com.ticketya.notificaciones.dtos.NotificacionResponseDTO;

import java.util.Optional;

public interface NotificacionService {

    NotificacionResponseDTO registrarCompraConfirmada(CompraConfirmadaRequestDTO solicitud);

    NotificacionResponseDTO registrarEventoCancelado(EventoCanceladoRequestDTO solicitud);

    NotificacionResponseDTO registrarEventoModificado(EventoModificadoRequestDTO solicitud);

    Optional<NotificacionResponseDTO> registrarArtistaFavorito(ArtistaFavoritoRequestDTO solicitud);

    Optional<NotificacionResponseDTO> buscarPorId(long id);
}
