package ar.edu.unicen.ticketya.eventos.service;

import ar.edu.unicen.ticketya.eventos.service.impl.*;

import ar.edu.unicen.ticketya.eventos.dto.*;
import ar.edu.unicen.ticketya.eventos.exception.ReglaNegocioException;
import ar.edu.unicen.ticketya.eventos.model.*;
import ar.edu.unicen.ticketya.eventos.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventoServiceTest {

    @Mock private EventoRepository eventoRepository;
    @Mock private SectorRepository sectorRepository;
    @Mock private AsientoRepository asientoRepository;
    @Mock private PrecioSectorRepository precioSectorRepository;
    @Mock private EventoParticipanteRepository eventoParticipanteRepository;
    @Mock private NotificacionCancelacionRepository notificacionRepository;
    @Mock private LugarService lugarService;
    @Mock private OrganizadorService organizadorService;
    @Mock private ParticipanteService participanteService;
    @Mock private AsientoService asientoService;

    private EventoService eventoService;
    private final Clock clock = Clock.fixed(
            Instant.parse("2026-11-04T18:00:00Z"),
            ZoneId.of("America/Argentina/Buenos_Aires")
    );

    @BeforeEach
    void preparar() {
        eventoService = new EventoServiceImpl(
                eventoRepository, sectorRepository, asientoRepository,
                precioSectorRepository, eventoParticipanteRepository,
                notificacionRepository, lugarService, organizadorService,
                participanteService, asientoService, clock
        );
    }

    private Evento evento(EstadoEvento estado, long id) {
        Organizador organizador = new Organizador("Organizador", "contacto@ejemplo.com");
        ReflectionTestUtils.setField(organizador, "id", 3L);
        Lugar lugar = new Lugar("Estadio", "Calle 123", "Tandil",
                new BigDecimal("-37.320000"), new BigDecimal("-59.140000"));
        ReflectionTestUtils.setField(lugar, "id", 4L);

        Evento evento = new Evento(
                "Recital", "Descripcion", TipoEvento.RECITAL,
                organizador, lugar, LocalDateTime.of(2027, 12, 20, 21, 0), estado, false
        );
        ReflectionTestUtils.setField(evento, "id", id);
        return evento;
    }

    @Test
    void publicarRechazaUnEventoSinSectores() {
        Evento evento = evento(EstadoEvento.BORRADOR, 10);
        when(eventoRepository.findById(10L)).thenReturn(Optional.of(evento));
        when(eventoParticipanteRepository.findByEventoId(10L)).thenReturn(
                List.of(new EventoParticipante(evento, new Participante("Banda", TipoParticipante.ARTISTA)))
        );

        ReglaNegocioException error = assertThrows(
                ReglaNegocioException.class, () -> eventoService.publicar(10L)
        );

        assertEquals(HttpStatus.CONFLICT, error.getStatus());
        assertEquals(EstadoEvento.BORRADOR, evento.getEstado());
        verify(eventoRepository, never()).saveAndFlush(any());
    }

    @Test
    void publicarRechazaSiNoSeConfiguroElPrecioDeTodosLosSectores() {
        Evento evento = evento(EstadoEvento.BORRADOR, 10);
        Sector sector = new Sector(evento.getLugar(), "Platea", true, 2);
        ReflectionTestUtils.setField(sector, "id", 15L);
        when(eventoRepository.findById(10L)).thenReturn(Optional.of(evento));
        when(eventoParticipanteRepository.findByEventoId(10L)).thenReturn(
                List.of(new EventoParticipante(evento, new Participante("Banda", TipoParticipante.ARTISTA)))
        );
        when(sectorRepository.findByLugarIdOrderById(4L)).thenReturn(List.of(sector));

        assertThrows(ReglaNegocioException.class, () -> eventoService.publicar(10L));
        assertEquals(EstadoEvento.BORRADOR, evento.getEstado());
    }

    @Test
    void publicarRechazaSectorNumeradoIncompleto() {
        Evento evento = evento(EstadoEvento.BORRADOR, 10);
        Sector sector = new Sector(evento.getLugar(), "Platea", true, 2);
        ReflectionTestUtils.setField(sector, "id", 15L);
        when(eventoRepository.findById(10L)).thenReturn(Optional.of(evento));
        when(eventoParticipanteRepository.findByEventoId(10L)).thenReturn(
                List.of(new EventoParticipante(evento, new Participante("Banda", TipoParticipante.ARTISTA)))
        );
        when(sectorRepository.findByLugarIdOrderById(4L)).thenReturn(List.of(sector));
        when(precioSectorRepository.findByEventoId(10L)).thenReturn(
                List.of(new PrecioSector(evento, sector, new BigDecimal("30000.00")))
        );
        when(asientoRepository.countBySectorId(15L)).thenReturn(1L);

        ReglaNegocioException error = assertThrows(
                ReglaNegocioException.class, () -> eventoService.publicar(10L)
        );
        assertEquals(HttpStatus.CONFLICT, error.getStatus());
    }

    @Test
    void publicarCorrectamenteConDosAsientosYUnPrecio() {
        Evento evento = evento(EstadoEvento.BORRADOR, 10);
        Sector sector = new Sector(evento.getLugar(), "Platea", true, 2);
        ReflectionTestUtils.setField(sector, "id", 15L);
        when(eventoRepository.findById(10L)).thenReturn(Optional.of(evento));
        when(eventoParticipanteRepository.findByEventoId(10L)).thenReturn(
                List.of(new EventoParticipante(evento, new Participante("Banda", TipoParticipante.ARTISTA)))
        );
        when(sectorRepository.findByLugarIdOrderById(4L)).thenReturn(List.of(sector));
        when(precioSectorRepository.findByEventoId(10L)).thenReturn(
                List.of(new PrecioSector(evento, sector, new BigDecimal("30000.00")))
        );
        when(asientoRepository.countBySectorId(15L)).thenReturn(2L);
        when(asientoRepository.findBySectorIdIn(List.of(15L))).thenReturn(List.of());

        EventoResponseDTO respuesta = eventoService.publicar(10L);

        assertEquals(EstadoEvento.PUBLICADO, respuesta.getEstado());
        verify(eventoRepository).saveAndFlush(evento);
    }

    @Test
    void cancelarRegistraUnaSolaNotificacion() {
        Evento evento = evento(EstadoEvento.PUBLICADO, 10);
        when(eventoRepository.findById(10L)).thenReturn(Optional.of(evento));

        eventoService.cancelar(10L);
        eventoService.cancelar(10L);

        assertEquals(EstadoEvento.CANCELADO, evento.getEstado());
        ArgumentCaptor<NotificacionCancelacion> captor = ArgumentCaptor.forClass(NotificacionCancelacion.class);
        verify(notificacionRepository, times(1)).save(captor.capture());
        assertEquals(10L, captor.getValue().getEventoId());
        assertFalse(captor.getValue().isReservasNotificadas());
        assertFalse(captor.getValue().isEntradasNotificadas());
        assertFalse(captor.getValue().isComprasNotificadas());
    }

    @Test
    void noPermiteModificarFechaDeUnEventoPublicado() {
        Evento evento = evento(EstadoEvento.PUBLICADO, 10);
        when(eventoRepository.findById(10L)).thenReturn(Optional.of(evento));
        EventoRequestDTO request = new EventoRequestDTO(
                "Nuevo nombre", "Descripcion", TipoEvento.RECITAL,
                3L, 4L, LocalDateTime.of(2027, 12, 21, 21, 0), List.of(5L), false
        );

        ReglaNegocioException error = assertThrows(
                ReglaNegocioException.class, () -> eventoService.actualizar(10L, request)
        );

        assertEquals(HttpStatus.CONFLICT, error.getStatus());
    }

    @Test
    void eventoAgotadoNoEstaHabilitadoParaReservar() {
        Evento evento = evento(EstadoEvento.AGOTADO, 10);
        when(eventoRepository.findById(10L)).thenReturn(Optional.of(evento));

        assertFalse(eventoService.habilitacion(10L).isHabilitadoParaReservar());
        evento.setEstado(EstadoEvento.PUBLICADO);
        assertTrue(eventoService.habilitacion(10L).isHabilitadoParaReservar());
    }

    @Test
    void buscarOrdenaPorPrecioMinimo() {
        Evento primero = evento(EstadoEvento.PUBLICADO, 10);
        Evento segundo = evento(EstadoEvento.PUBLICADO, 11);
        ReflectionTestUtils.setField(segundo, "nombre", "Otro recital");
        when(eventoRepository.findAll(any(Specification.class))).thenReturn(List.of(primero, segundo));

        Sector sector = new Sector(primero.getLugar(), "Platea", true, 2);
        when(precioSectorRepository.findByEventoIdIn(List.of(10L, 11L))).thenReturn(List.of(
                new PrecioSector(primero, sector, new BigDecimal("40000.00")),
                new PrecioSector(segundo, sector, new BigDecimal("20000.00"))
        ));
        when(eventoParticipanteRepository.findByEventoIdIn(List.of(10L, 11L))).thenReturn(List.of());

        List<EventoResumenResponseDTO> resultado = eventoService.buscar(
                null, null, null, null, null, null,
                null, OrdenEvento.PRECIO_ASC, false
        );

        assertEquals(List.of(11L, 10L), resultado.stream().map(EventoResumenResponseDTO::getId).toList());
    }

    @Test
    void noAceptaUnRangoDeFechasInvertido() {
        LocalDateTime desde = LocalDateTime.of(2027, 10, 10, 0, 0);
        LocalDateTime hasta = LocalDateTime.of(2027, 10, 1, 0, 0);

        ReglaNegocioException error = assertThrows(ReglaNegocioException.class,
                () -> eventoService.buscar(null, null, null, null, desde, hasta,
                        null, OrdenEvento.FECHA_ASC, false));

        assertEquals(HttpStatus.BAD_REQUEST, error.getStatus());
    }

    @Test
    void configurarPreciosRechazaUnSectorDeOtroLugar() {
        Evento evento = evento(EstadoEvento.BORRADOR, 10);
        when(eventoRepository.findById(10L)).thenReturn(Optional.of(evento));
        when(sectorRepository.findByLugarIdOrderById(4L)).thenReturn(List.of());
        PreciosEventoRequestDTO request = new PreciosEventoRequestDTO(
                List.of(new PrecioSectorRequestDTO(999L, new BigDecimal("100.00")))
        );

        ReglaNegocioException error = assertThrows(ReglaNegocioException.class,
                () -> eventoService.configurarPrecios(10L, request));

        assertEquals(HttpStatus.BAD_REQUEST, error.getStatus());
        verify(precioSectorRepository, never()).save(any());
    }
}
