package com.ticketya.reservas.services;

import com.ticketya.reservas.clients.EventoClient;
import com.ticketya.reservas.dtos.*;
import com.ticketya.reservas.entities.*;
import com.ticketya.reservas.enums.EstadoReserva;
import com.ticketya.reservas.repositories.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservaServiceImplTest {
    @Mock ReservaRepository reservaRepo;
    @Mock DetalleReservaRepository detalleRepo;
    @Mock AsignacionAsientoRepository asientoRepo;
    @Mock BloqueoEventoRepository bloqueoRepo;
    @Mock EntradaLiberadaRepository entradaLiberadaRepo;
    @Mock ClaveIdempotenciaBloqueoRepository claveBloqueoRepo;
    @Mock EventoClient eventoClient;
    @InjectMocks ReservaServiceImpl service;

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
}
