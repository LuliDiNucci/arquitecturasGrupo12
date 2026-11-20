package ar.edu.unicen.ticketya.eventos.service;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

import ar.edu.unicen.ticketya.eventos.dto.EventoResponseDTO;
import ar.edu.unicen.ticketya.eventos.model.EstadoEvento;

class CatalogoCacheServiceTest {

    @Test
    void primeraConsultaLuegoAciertoYVencimientoTtl() throws Exception {
        CatalogoCacheService cache = new CatalogoCacheService(1);
        EventoResponseDTO primero = publicado();
        EventoResponseDTO segundo = publicado();
        AtomicInteger consultas = new AtomicInteger();

        assertSame(primero, cache.detalle(7L, false,
                () -> consultas.getAndIncrement() == 0 ? primero : segundo));
        assertSame(primero, cache.detalle(7L, false, () -> segundo));
        Thread.sleep(1100);
        assertSame(segundo, cache.detalle(7L, false, () -> segundo));
    }

    @Test
    void invalidacionObligaAConsultarDeNuevo() {
        CatalogoCacheService cache = new CatalogoCacheService(30);
        EventoResponseDTO primero = publicado();
        EventoResponseDTO segundo = publicado();
        cache.detalle(7L, false, () -> primero);
        cache.invalidarDespuesDeConfirmar();

        assertSame(segundo, cache.detalle(7L, false, () -> segundo));
    }

    private EventoResponseDTO publicado() {
        EventoResponseDTO evento = mock(EventoResponseDTO.class);
        when(evento.getEstado()).thenReturn(EstadoEvento.PUBLICADO);
        return evento;
    }
}
