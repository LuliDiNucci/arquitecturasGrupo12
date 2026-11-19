package com.ticketya.recomendaciones.services;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import static org.mockito.Mockito.when;

import com.ticketya.recomendaciones.clients.CompraClient;
import com.ticketya.recomendaciones.clients.EventoClient;
import com.ticketya.recomendaciones.dtos.EventoDetalleDTO;
import com.ticketya.recomendaciones.dtos.EventoRecomendadoDTO;
import com.ticketya.recomendaciones.dtos.EventoResumenDTO;
import com.ticketya.recomendaciones.models.Preferencias;
import com.ticketya.recomendaciones.repositories.PreferenciasRepository;

public class RecomendacionesServiceTest {

    @Test
    void recomiendaEventoPorHistorialAunqueNoCoincidaConPreferencias() {

        PreferenciasRepository repository = Mockito.mock(PreferenciasRepository.class);

        EventoClient eventoClient = Mockito.mock(EventoClient.class);

        CompraClient compraClient = Mockito.mock(CompraClient.class);

        RecomendacionesService service = new RecomendacionesService(
                repository,
                eventoClient,
                compraClient);

        Preferencias preferencias = new Preferencias();
        preferencias.setClienteId(3L);
        preferencias.setTiposEventos(List.of("TEATRO"));
        preferencias.setArtistasFavoritos(List.of());
        preferencias.setCiudad("Cordoba");

        EventoResumenDTO evento = new EventoResumenDTO();
        evento.setId(2L);
        evento.setNombre("Recital recomendado por historial");
        evento.setTipo("RECITAL");
        evento.setEstado("PUBLICADO");
        evento.setCiudad("Tandil");
        evento.setFechaHora(LocalDateTime.now().plusDays(30));
        evento.setPrecioDesde(new BigDecimal("20000"));

        when(repository.findByClienteId(3L))
                .thenReturn(Optional.of(preferencias));

        when(eventoClient.listarEventosPublicados())
                .thenReturn(List.of(evento));

        when(compraClient.obtenerEventosRecomendados(3L))
                .thenReturn(List.of(2L));

        when(eventoClient.obtenerEvento(2L))
                .thenReturn(new EventoDetalleDTO(2L, List.of()));

        List<EventoRecomendadoDTO> resultado = service.obtenerRecomendaciones(3L);

        assertEquals(1, resultado.size());
        assertEquals(2L, resultado.get(0).getId());
    }

    @Test
    void noRecomiendaEventosVencidosNiNoPublicados() {

        PreferenciasRepository repository = Mockito.mock(PreferenciasRepository.class);

        EventoClient eventoClient = Mockito.mock(EventoClient.class);

        CompraClient compraClient = Mockito.mock(CompraClient.class);

        RecomendacionesService service = new RecomendacionesService(
                repository, eventoClient, compraClient);

        Preferencias preferencias = new Preferencias();
        preferencias.setClienteId(6L);
        preferencias.setTiposEventos(List.of("RECITAL"));
        preferencias.setArtistasFavoritos(List.of());
        preferencias.setCiudad("Tandil");

        // Evento publicado, pero con fecha pasada
        EventoResumenDTO vencido = new EventoResumenDTO();
        vencido.setId(10L);
        vencido.setNombre("Recital vencido");
        vencido.setTipo("RECITAL");
        vencido.setEstado("PUBLICADO");
        vencido.setCiudad("Tandil");
        vencido.setFechaHora(LocalDateTime.now().minusDays(5));

        // Evento futuro, pero suspendido
        EventoResumenDTO suspendido = new EventoResumenDTO();
        suspendido.setId(11L);
        suspendido.setNombre("Recital suspendido");
        suspendido.setTipo("RECITAL");
        suspendido.setEstado("SUSPENDIDO");
        suspendido.setCiudad("Tandil");
        suspendido.setFechaHora(LocalDateTime.now().plusDays(30));

        when(repository.findByClienteId(6L))
                .thenReturn(Optional.of(preferencias));

        when(eventoClient.listarEventosPublicados())
                .thenReturn(List.of(vencido, suspendido));

        when(compraClient.obtenerEventosRecomendados(6L))
                .thenReturn(List.of(10L, 11L));

        List<EventoRecomendadoDTO> resultado = service.obtenerRecomendaciones(6L);

        assertEquals(0, resultado.size());
    }
}