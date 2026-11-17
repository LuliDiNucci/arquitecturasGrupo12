package com.ticketya.msvc_fila_virtual;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import com.ticketya.msvc_fila_virtual.clients.EventosFeignClient;
import com.ticketya.msvc_fila_virtual.dtos.ClienteEnFilaDTO;
import com.ticketya.msvc_fila_virtual.dtos.EventoDTO;
import com.ticketya.msvc_fila_virtual.dtos.ValidacionTurnoDTO;
import com.ticketya.msvc_fila_virtual.entities.ClienteEnFila;
import com.ticketya.msvc_fila_virtual.entities.EstadoFila;
import com.ticketya.msvc_fila_virtual.repositories.ClienteEnFilaRepository;
import com.ticketya.msvc_fila_virtual.services.impl.FilaVirtualServiceImpl;

@ExtendWith(MockitoExtension.class)
class FilaVirtualServiceImplTest {

    @Mock
    private ClienteEnFilaRepository repository;

    @Mock
    private EventosFeignClient eventosFeignClient;

    private FilaVirtualServiceImpl service;

    @BeforeEach
    void preparar() {
        service = new FilaVirtualServiceImpl(
                repository,
                eventosFeignClient);
    }

    private ClienteEnFila crearCliente(
            Long id,
            Long eventoId,
            Long clienteId) {

        ClienteEnFila cliente = new ClienteEnFila(
                eventoId,
                clienteId);

        ReflectionTestUtils.setField(cliente, "id", id);

        return cliente;
    }

    private void simularEventoPublicado(Long eventoId) {

        when(eventosFeignClient.obtenerEvento(eventoId))
                .thenReturn(new EventoDTO(
                        eventoId,
                        "Evento de prueba",
                        "PUBLICADO",
                        true));
    }

    @Test
    void ingresarNuevamenteConservaRegistroYPosicion() {

        Long eventoId = 1L;
        Long clienteId = 10L;

        ClienteEnFila cliente = crearCliente(
                5L, eventoId, clienteId);

        simularEventoPublicado(eventoId);

        when(repository.findByEventoIdAndClienteId(
                eventoId, clienteId))
                .thenReturn(Optional.of(cliente));

        when(repository.countByEventoIdAndEstadoAndIdLessThan(
                eventoId,
                EstadoFila.EN_ESPERA,
                5L))
                .thenReturn(2L);

        ClienteEnFilaDTO primero = service.ingresar(
                eventoId, clienteId);

        ClienteEnFilaDTO segundo = service.ingresar(
                eventoId, clienteId);

        assertEquals(primero.id(), segundo.id());
        assertEquals(3L, primero.posicion());
        assertEquals(3L, segundo.posicion());

        verify(repository, times(2))
                .ingresarSiNoExiste(
                        eq(eventoId),
                        eq(clienteId),
                        any(LocalDateTime.class));
    }

    @Test
    void rechazaEventoSinAltaDemanda() {

        Long eventoId = 2L;

        when(eventosFeignClient.obtenerEvento(eventoId))
                .thenReturn(new EventoDTO(
                        eventoId,
                        "Evento normal",
                        "PUBLICADO",
                        false));

        ResponseStatusException error = assertThrows(
                ResponseStatusException.class,
                () -> service.ingresar(eventoId, 10L));

        assertEquals(
                HttpStatus.CONFLICT,
                error.getStatusCode());

        verify(repository, never())
                .ingresarSiNoExiste(
                        anyLong(),
                        anyLong(),
                        any(LocalDateTime.class));
    }

    @Test
    void habilitaClientesRespetandoOrden() {

        Long eventoId = 3L;

        ClienteEnFila primero = crearCliente(1L, eventoId, 10L);
        ClienteEnFila segundo = crearCliente(2L, eventoId, 20L);
        ClienteEnFila tercero = crearCliente(3L, eventoId, 30L);

        when(repository.findFirstByEventoIdOrderByIdAsc(eventoId))
                .thenReturn(Optional.of(primero));

        when(repository.findByEventoIdAndEstadoOrderByIdAsc(
                eventoId, EstadoFila.HABILITADO))
                .thenReturn(List.of());

        when(repository.findByEventoIdAndEstadoOrderByIdAsc(
                eventoId, EstadoFila.EN_ESPERA))
                .thenReturn(List.of(primero, segundo, tercero));

        List<ClienteEnFilaDTO> resultado = service.habilitarTanda(eventoId, 2);

        assertEquals(2, resultado.size());

        assertEquals(10L, resultado.get(0).clienteId());
        assertEquals(20L, resultado.get(1).clienteId());

        assertEquals(EstadoFila.HABILITADO,
                primero.getEstado());

        assertEquals(EstadoFila.HABILITADO,
                segundo.getEstado());

        assertEquals(EstadoFila.EN_ESPERA,
                tercero.getEstado());

        verify(repository).flush();
    }

    @Test
    void rechazaSegundaTandaSiExisteUnaActiva() {

        Long eventoId = 4L;

        ClienteEnFila cliente = crearCliente(1L, eventoId, 10L);

        cliente.habilitar(LocalDateTime.now(), 10);

        when(repository.findFirstByEventoIdOrderByIdAsc(eventoId))
                .thenReturn(Optional.of(cliente));

        when(repository.findByEventoIdAndEstadoOrderByIdAsc(
                eventoId, EstadoFila.HABILITADO))
                .thenReturn(List.of(cliente));

        ResponseStatusException error = assertThrows(
                ResponseStatusException.class,
                () -> service.habilitarTanda(eventoId, 3));

        assertEquals(
                HttpStatus.CONFLICT,
                error.getStatusCode());

        verify(repository, never()).flush();
    }

    @Test
    void turnoVigenteAutorizaAlCliente() {

        Long eventoId = 5L;
        Long clienteId = 10L;

        ClienteEnFila cliente = crearCliente(
                1L, eventoId, clienteId);

        cliente.habilitar(LocalDateTime.now(), 10);

        when(repository.bloquearPorEventoYCliente(
                eventoId, clienteId))
                .thenReturn(Optional.of(cliente));

        ValidacionTurnoDTO resultado = service.validarTurno(eventoId, clienteId);

        assertTrue(resultado.autorizado());

        assertEquals(
                EstadoFila.HABILITADO,
                resultado.estado());

        assertNotNull(resultado.fechaVencimiento());
    }

    @Test
    void clienteEnEsperaNoEstaAutorizado() {

        Long eventoId = 6L;
        Long clienteId = 10L;

        ClienteEnFila cliente = crearCliente(
                1L, eventoId, clienteId);

        when(repository.bloquearPorEventoYCliente(
                eventoId, clienteId))
                .thenReturn(Optional.of(cliente));

        ValidacionTurnoDTO resultado = service.validarTurno(eventoId, clienteId);

        assertFalse(resultado.autorizado());

        assertEquals(
                EstadoFila.EN_ESPERA,
                resultado.estado());
    }

    @Test
    void turnoVencidoNoAutorizaAlCliente() {

        Long eventoId = 7L;
        Long clienteId = 10L;

        ClienteEnFila cliente = crearCliente(
                1L, eventoId, clienteId);

        cliente.habilitar(
                LocalDateTime.now().minusMinutes(11),
                10);

        when(repository.bloquearPorEventoYCliente(
                eventoId, clienteId))
                .thenReturn(Optional.of(cliente));

        ValidacionTurnoDTO resultado = service.validarTurno(eventoId, clienteId);

        assertFalse(resultado.autorizado());

        assertEquals(
                EstadoFila.VENCIDO,
                resultado.estado());
    }

    @Test
    void noPermiteFinalizarTurnoVencido() {

        Long eventoId = 8L;
        Long clienteId = 10L;

        ClienteEnFila cliente = crearCliente(
                1L, eventoId, clienteId);

        cliente.habilitar(
                LocalDateTime.now().minusMinutes(11),
                10);

        when(repository.bloquearPorEventoYCliente(
                eventoId, clienteId))
                .thenReturn(Optional.of(cliente));

        ResponseStatusException error = assertThrows(
                ResponseStatusException.class,
                () -> service.finalizarTurno(
                        eventoId, clienteId));

        assertEquals(
                HttpStatus.CONFLICT,
                error.getStatusCode());

        assertEquals(
                EstadoFila.HABILITADO,
                cliente.getEstado());
    }

    @Test
    void finalizarTurnoEsIdempotente() {

        Long eventoId = 9L;
        Long clienteId = 10L;

        ClienteEnFila cliente = crearCliente(
                1L, eventoId, clienteId);

        cliente.habilitar(LocalDateTime.now(), 10);

        when(repository.bloquearPorEventoYCliente(
                eventoId, clienteId))
                .thenReturn(Optional.of(cliente));

        ClienteEnFilaDTO primero = service.finalizarTurno(eventoId, clienteId);

        ClienteEnFilaDTO segundo = service.finalizarTurno(eventoId, clienteId);

        assertEquals(
                EstadoFila.FINALIZADO,
                primero.estado());

        assertEquals(
                EstadoFila.FINALIZADO,
                segundo.estado());

        assertEquals(primero.id(), segundo.id());
    }

    @Test
    void turnoFinalizadoNoAutorizaNuevamente() {

        Long eventoId = 10L;
        Long clienteId = 10L;

        ClienteEnFila cliente = crearCliente(
                1L, eventoId, clienteId);

        cliente.habilitar(LocalDateTime.now(), 10);
        cliente.finalizar();

        when(repository.bloquearPorEventoYCliente(
                eventoId, clienteId))
                .thenReturn(Optional.of(cliente));

        ValidacionTurnoDTO resultado = service.validarTurno(eventoId, clienteId);

        assertFalse(resultado.autorizado());

        assertEquals(
                EstadoFila.FINALIZADO,
                resultado.estado());
    }
}