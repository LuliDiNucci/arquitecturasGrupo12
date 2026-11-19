package com.ticketya.reservas.services;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import com.ticketya.reservas.clients.EventoClient;
import com.ticketya.reservas.clients.FilaVirtualClient;
import com.ticketya.reservas.dtos.CrearReservaDTO;
import com.ticketya.reservas.dtos.DetalleReservaDTO;
import com.ticketya.reservas.dtos.EventoDTO;
import com.ticketya.reservas.dtos.ValidacionTurnoDTO;
import com.ticketya.reservas.entities.BloqueoEvento;
import com.ticketya.reservas.entities.ClaveIdempotenciaBloqueo;
import com.ticketya.reservas.entities.Reserva;
import com.ticketya.reservas.enums.EstadoReserva;
import com.ticketya.reservas.repositories.AsignacionAsientoRepository;
import com.ticketya.reservas.repositories.BloqueoEventoRepository;
import com.ticketya.reservas.repositories.ClaveIdempotenciaBloqueoRepository;
import com.ticketya.reservas.repositories.DetalleReservaRepository;
import com.ticketya.reservas.repositories.EntradaLiberadaRepository;
import com.ticketya.reservas.repositories.FinalizacionFilaPendienteRepository;
import com.ticketya.reservas.repositories.ReservaRepository;

import feign.FeignException;

@ExtendWith(MockitoExtension.class)
class ReservaServiceImplTest {
    @Mock
    ReservaRepository reservaRepo;
    @Mock
    DetalleReservaRepository detalleRepo;
    @Mock
    AsignacionAsientoRepository asientoRepo;
    @Mock
    BloqueoEventoRepository bloqueoRepo;
    @Mock
    EntradaLiberadaRepository entradaLiberadaRepo;
    @Mock
    ClaveIdempotenciaBloqueoRepository claveBloqueoRepo;
    @Mock
    EventoClient eventoClient;
    @Mock
    FilaVirtualClient filaVirtualClient;
    @Mock
    FinalizacionFilaPendienteRepository finalizacionFilaRepo;

    @InjectMocks
    ReservaServiceImpl service;

    @BeforeEach
    void inicializar() {
        ReflectionTestUtils.setField(service, "maxEntradasPorCliente", 4);
    }

    private CrearReservaDTO pedido() {
        CrearReservaDTO d = new CrearReservaDTO();
        d.setClienteId(1L);
        d.setEventoId(20L);
        d.setClaveIdempotencia("clave-20");
        DetalleReservaDTO detalle = new DetalleReservaDTO();
        detalle.setSectorId(5L);
        detalle.setCantidad(1);
        detalle.setAsientoIds(List.of(25L));
        d.setDetalles(List.of(detalle));
        return d;
    }

    @Test
    void rechazaSolicitudIncompletaSinGuardar() {
        CrearReservaDTO d = pedido();
        d.setClienteId(null);
        assertThrows(IllegalArgumentException.class, () -> service.crearReserva(d));
        verify(reservaRepo, never()).saveAndFlush(any());
    }

    @Test
    void rechazaReservaSiEventoCanceladoLocalmente() {
        CrearReservaDTO d = pedido();
        BloqueoEvento bloqueo = new BloqueoEvento();
        bloqueo.setEventoId(20L);
        bloqueo.setCancelado(true);
        ClaveIdempotenciaBloqueo clave = new ClaveIdempotenciaBloqueo();
        clave.setClave("clave-20");
        when(claveBloqueoRepo.bloquear("clave-20")).thenReturn(Optional.of(clave));
        when(bloqueoRepo.bloquear(20L)).thenReturn(Optional.of(bloqueo));
        when(bloqueoRepo.findById(20L)).thenReturn(Optional.of(bloqueo));
        assertThrows(IllegalStateException.class, () -> service.crearReserva(d));
        verify(reservaRepo, never()).saveAndFlush(any());
    }

    @Test
    void noConfirmaReservaVencida() {
        Reserva r = new Reserva();
        r.setId(8L);
        r.setEventoId(20L);
        r.setEstado(EstadoReserva.ACTIVA);
        r.setFechaVencimiento(LocalDateTime.now().minusMinutes(1));
        when(reservaRepo.buscarEventoId(8L)).thenReturn(Optional.of(20L));
        when(bloqueoRepo.bloquear(20L)).thenReturn(Optional.of(new BloqueoEvento()));
        when(reservaRepo.findById(8L)).thenReturn(Optional.of(r));
        assertThrows(IllegalStateException.class, () -> service.confirmarReserva(8L));
        assertEquals(EstadoReserva.VENCIDA, r.getEstado());
        verify(reservaRepo, never()).save(any());
    }

    @Test
    void cancelacionMarcaElEventoYCancelaActivas() {
        Reserva r = new Reserva();
        r.setEstado(EstadoReserva.ACTIVA);
        r.setFechaVencimiento(LocalDateTime.now().plusMinutes(5));
        BloqueoEvento bloqueo = new BloqueoEvento();
        when(bloqueoRepo.bloquear(20L)).thenReturn(Optional.of(bloqueo));
        when(bloqueoRepo.findById(20L)).thenReturn(Optional.of(bloqueo));
        when(reservaRepo.findByEventoId(20L)).thenReturn(List.of(r));
        service.cancelarReservasEvento(20L);
        assertTrue(bloqueo.isCancelado());
        assertEquals(EstadoReserva.CANCELADA, r.getEstado());
        verify(reservaRepo).saveAll(any());
    }

    @Test
    void eventoNormalNoRequiereFilaVirtual() {

        EventoDTO evento = new EventoDTO();
        evento.setId(20L);
        evento.setAltaDemanda(false);

        assertDoesNotThrow(() -> ReflectionTestUtils.invokeMethod(
                service,
                "validarTurnoFilaVirtual",
                evento,
                1L));

        verifyNoInteractions(filaVirtualClient);
    }

    @Test
    void eventoAltaDemandaConTurnoValidoPermiteContinuar() {

        EventoDTO evento = new EventoDTO();
        evento.setId(20L);
        evento.setAltaDemanda(true);

        ValidacionTurnoDTO turno = new ValidacionTurnoDTO(
                20L,
                1L,
                true,
                "HABILITADO",
                LocalDateTime.now().plusMinutes(5));

        when(filaVirtualClient.validarTurno(20L, 1L))
                .thenReturn(turno);

        assertDoesNotThrow(() -> ReflectionTestUtils.invokeMethod(
                service,
                "validarTurnoFilaVirtual",
                evento,
                1L));

        verify(filaVirtualClient).validarTurno(20L, 1L);
    }

    @Test
    void eventoAltaDemandaSinAutorizacionRechazaReserva() {

        EventoDTO evento = new EventoDTO();
        evento.setId(20L);
        evento.setAltaDemanda(true);

        ValidacionTurnoDTO turno = new ValidacionTurnoDTO(
                20L,
                1L,
                false,
                "EN_ESPERA",
                null);

        when(filaVirtualClient.validarTurno(20L, 1L))
                .thenReturn(turno);

        assertThrows(
                ResponseStatusException.class,
                () -> ReflectionTestUtils.invokeMethod(
                        service,
                        "validarTurnoFilaVirtual",
                        evento,
                        1L));
    }

    @Test
    void eventoAltaDemandaConTurnoVencidoRechazaReserva() {

        EventoDTO evento = new EventoDTO();
        evento.setId(20L);
        evento.setAltaDemanda(true);

        ValidacionTurnoDTO turno = new ValidacionTurnoDTO(
                20L,
                1L,
                true,
                "HABILITADO",
                LocalDateTime.now().minusMinutes(1));

        when(filaVirtualClient.validarTurno(20L, 1L))
                .thenReturn(turno);

        assertThrows(
                ResponseStatusException.class,
                () -> ReflectionTestUtils.invokeMethod(
                        service,
                        "validarTurnoFilaVirtual",
                        evento,
                        1L));
    }

    @Test
    void clienteNoRegistradoEnFilaNoPuedeReservar() {

        EventoDTO evento = new EventoDTO();
        evento.setId(20L);
        evento.setAltaDemanda(true);

        when(filaVirtualClient.validarTurno(20L, 1L))
                .thenThrow(
                        mock(FeignException.NotFound.class));

        ResponseStatusException error = assertThrows(
                ResponseStatusException.class,
                () -> ReflectionTestUtils.invokeMethod(
                        service,
                        "validarTurnoFilaVirtual",
                        evento,
                        1L));

        assertEquals(HttpStatus.CONFLICT, error.getStatusCode());
    }

    @Test
    void filaVirtualNoDisponibleRechazaReserva() {

        EventoDTO evento = new EventoDTO();
        evento.setId(20L);
        evento.setAltaDemanda(true);

        when(filaVirtualClient.validarTurno(20L, 1L))
                .thenThrow(
                        mock(FeignException.ServiceUnavailable.class));

        ResponseStatusException error = assertThrows(
                ResponseStatusException.class,
                () -> ReflectionTestUtils.invokeMethod(
                        service,
                        "validarTurnoFilaVirtual",
                        evento,
                        1L));

        assertEquals(
                HttpStatus.SERVICE_UNAVAILABLE,
                error.getStatusCode());
    }
}
