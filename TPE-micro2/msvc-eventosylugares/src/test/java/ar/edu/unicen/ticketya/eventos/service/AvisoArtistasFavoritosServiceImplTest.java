package ar.edu.unicen.ticketya.eventos.service;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import ar.edu.unicen.ticketya.eventos.dto.EventoResponseDTO;
import ar.edu.unicen.ticketya.eventos.dto.LugarResponseDTO;
import ar.edu.unicen.ticketya.eventos.dto.ParticipanteResponseDTO;
import ar.edu.unicen.ticketya.eventos.dto.UsuarioContactoDTO;
import ar.edu.unicen.ticketya.eventos.feign.NotificacionesEventoFeignClient;
import ar.edu.unicen.ticketya.eventos.feign.RecomendacionesFeignClient;
import ar.edu.unicen.ticketya.eventos.feign.UsuariosFeignClient;
import ar.edu.unicen.ticketya.eventos.service.impl.AvisoArtistasFavoritosServiceImpl;
import feign.RetryableException;

class AvisoArtistasFavoritosServiceImplTest {

    private RecomendacionesFeignClient recomendaciones;
    private UsuariosFeignClient usuarios;
    private NotificacionesEventoFeignClient notificaciones;

    private AvisoArtistasFavoritosServiceImpl service;

    @BeforeEach
    void preparar() {
        recomendaciones = mock(RecomendacionesFeignClient.class);
        usuarios = mock(UsuariosFeignClient.class);
        notificaciones = mock(NotificacionesEventoFeignClient.class);

        service = new AvisoArtistasFavoritosServiceImpl(
                recomendaciones,
                usuarios,
                notificaciones);
    }

    private EventoResponseDTO crearEvento(String... artistas) {

        EventoResponseDTO evento = new EventoResponseDTO();

        evento.setId(10L);
        evento.setNombre("Recital de prueba");
        evento.setFechaHora(
                java.time.LocalDateTime.of(2027, 8, 20, 21, 0));

        LugarResponseDTO lugar = new LugarResponseDTO();
        lugar.setCiudad("Tandil");

        evento.setLugar(lugar);

        List<ParticipanteResponseDTO> participantes = java.util.Arrays.stream(artistas)
                .map(nombre -> new ParticipanteResponseDTO(
                        1L,
                        nombre,
                        null))
                .toList();

        evento.setParticipantes(participantes);

        return evento;
    }

    @Test
    void noNotificaCuandoNoHayClientesInteresados() {

        EventoResponseDTO evento = crearEvento("Artista A");

        when(recomendaciones.buscarClientesInteresados(
                "Artista A", "Tandil"))
                .thenReturn(List.of());

        service.notificarPublicacion(evento);

        verifyNoInteractions(usuarios, notificaciones);
    }

    @Test
    void notificaAlClienteInteresado() {

        EventoResponseDTO evento = crearEvento("Artista A");

        when(recomendaciones.buscarClientesInteresados(
                "Artista A", "Tandil"))
                .thenReturn(List.of(9L));

        when(usuarios.obtenerContacto(9L))
                .thenReturn(new UsuarioContactoDTO(
                        9L,
                        "cliente@ticketya.test"));

        service.notificarPublicacion(evento);

        verify(notificaciones, times(1))
                .avisarArtistaFavorito(any());
    }

    @Test
    void noDuplicaNotificacionesParaElMismoCliente() {

        EventoResponseDTO evento = crearEvento("Artista A", "Artista B");

        when(recomendaciones.buscarClientesInteresados(
                anyString(), eq("Tandil")))
                .thenReturn(List.of(9L));

        when(usuarios.obtenerContacto(9L))
                .thenReturn(new UsuarioContactoDTO(
                        9L,
                        "cliente@ticketya.test"));

        service.notificarPublicacion(evento);

        verify(notificaciones, times(1))
                .avisarArtistaFavorito(any());

        verify(usuarios, times(1))
                .obtenerContacto(9L);
    }

    @Test
    void reintentaCuandoFallaLaComunicacionConRecomendaciones() {

        EventoResponseDTO evento = crearEvento("Artista A");

        RetryableException error = mock(RetryableException.class);

        when(recomendaciones.buscarClientesInteresados(
                "Artista A", "Tandil"))
                .thenThrow(error)
                .thenThrow(error)
                .thenReturn(List.of(9L));

        when(usuarios.obtenerContacto(9L))
                .thenReturn(new UsuarioContactoDTO(
                        9L,
                        "cliente@ticketya.test"));

        service.notificarPublicacion(evento);

        verify(recomendaciones, times(3))
                .buscarClientesInteresados("Artista A", "Tandil");

        verify(notificaciones, times(1))
                .avisarArtistaFavorito(any());
    }
}