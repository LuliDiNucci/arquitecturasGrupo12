package com.ticketya.entradas.services.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.ticketya.entradas.clients.CompraClient;
import com.ticketya.entradas.clients.CompraFeignClient;
import com.ticketya.entradas.clients.EventoFeignClient;
import com.ticketya.entradas.clients.ReservaClient;
import com.ticketya.entradas.clients.ReservaFeignClient;
import com.ticketya.entradas.dtos.CompraDTO;
import com.ticketya.entradas.dtos.DetalleReservaDTO;
import com.ticketya.entradas.dtos.EventoDTO;
import com.ticketya.entradas.dtos.ReservaDTO;
import com.ticketya.entradas.entities.Entrada;
import com.ticketya.entradas.repositories.EntradaRepository;
import com.ticketya.entradas.services.QrCodeService;

@ExtendWith(MockitoExtension.class)
class EntradaServiceImplTest {

    @Mock
    private EntradaRepository entradaRepository;

    @Mock
    private CompraClient compraClient;

    @Mock
    private ReservaClient reservaClient;

    @Mock
    private QrCodeService qrCodeService;

    @Mock
    private EventoFeignClient eventoFeignClient;

    @Mock
    private ReservaFeignClient reservaFeignClient;

    @Mock
    private CompraFeignClient compraFeignClient;

    private EntradaServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new EntradaServiceImpl(
                entradaRepository,
                compraClient,
                reservaClient,
                qrCodeService,
                eventoFeignClient,
                reservaFeignClient,
                compraFeignClient);
    }

    @Test
    void emiteUnaEntradaPorCadaLugarConCodigoQrUnico() {
        when(entradaRepository.findByCompraIdOrderByIdAsc(1L))
                .thenReturn(List.of());
        when(compraClient.obtenerConfirmacion(1L))
                .thenReturn(new CompraDTO(1L, 10L, 20L, "CONFIRMADA", true));
        when(reservaClient.obtener(20L))
                .thenReturn(reservaConfirmada(List.of(
                        new DetalleReservaDTO(30L, 40L, 2,
                                List.of(50L, 51L), 0))));
        when(entradaRepository.saveAllAndFlush(anyList()))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(qrCodeService.generarImagenBase64(anyString()))
                .thenReturn("imagen-base64");

        var result = service.emitir(1L);

        assertTrue(result.isCreada());
        assertEquals(2, result.getEntradas().size());
        assertNotEquals(result.getEntradas().get(0).getCodigoQr(),
                result.getEntradas().get(1).getCodigoQr());
        verify(qrCodeService).generarImagenBase64(
                eq(result.getEntradas().get(0).getCodigoQr()));
        verify(qrCodeService).generarImagenBase64(
                eq(result.getEntradas().get(1).getCodigoQr()));
    }

    @Test
    void emisionRepetidaNoCreaEntradasDuplicadas() {
        Entrada existing = entrada(70L, "qr-existente", "EMITIDA");
        when(entradaRepository.findByCompraIdOrderByIdAsc(1L))
                .thenReturn(List.of(existing));
        when(qrCodeService.generarImagenBase64("qr-existente"))
                .thenReturn("imagen-base64");

        var result = service.emitir(1L);

        assertTrue(!result.isCreada());
        assertEquals(1, result.getEntradas().size());
        verifyNoEmission();
    }

    @Test
    void noEmiteSiLaCompraNoEstaConfirmada() {
        when(entradaRepository.findByCompraIdOrderByIdAsc(1L))
                .thenReturn(List.of());
        when(compraClient.obtenerConfirmacion(1L))
                .thenReturn(new CompraDTO(1L, 10L, 20L, "PENDIENTE", false));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class, () -> service.emitir(1L));

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        verify(reservaClient, never()).obtener(any());
        verifyNoEmission();
    }

    @Test
    void noEmiteSiLaReservaNoEstaConfirmada() {
        when(entradaRepository.findByCompraIdOrderByIdAsc(1L))
                .thenReturn(List.of());
        when(compraClient.obtenerConfirmacion(1L))
                .thenReturn(new CompraDTO(1L, 10L, 20L, "CONFIRMADA", true));
        when(reservaClient.obtener(20L))
                .thenReturn(new ReservaDTO(20L, 10L, 30L, "ACTIVA", List.of()));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class, () -> service.emitir(1L));

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        verifyNoEmission();
    }

    @Test
    void invalidaEntradasEmitidasSinLiberarReservasNiReintegrar() {
        Entrada emitted = entrada(70L, "qr-1", "EMITIDA");
        Entrada alreadyInvalid = entrada(71L, "qr-2", "INVALIDADA");
        when(eventoFeignClient.obtenerEvento(30L))
                .thenReturn(new EventoDTO(30L, null, "CANCELADO"));
        when(entradaRepository.findByEventoIdAndEstado(30L, "EMITIDA"))
                .thenReturn(List.of(emitted));

        service.invalidarPorEvento(30L);

        assertEquals("INVALIDADA", emitted.getEstado());
        assertEquals("INVALIDADA", alreadyInvalid.getEstado());
        verify(entradaRepository).saveAll(List.of(emitted));
        verifyNoInteractions(reservaFeignClient, compraFeignClient);
    }

    @Test
    void invalidacionRepetidaNoGeneraNuevasAcciones() {
        Entrada emitted = entrada(70L, "qr-1", "EMITIDA");
        when(eventoFeignClient.obtenerEvento(30L))
                .thenReturn(new EventoDTO(30L, null, "CANCELADO"));
        when(entradaRepository.findByEventoIdAndEstado(30L, "EMITIDA"))
                .thenReturn(List.of(emitted), List.of());

        service.invalidarPorEvento(30L);
        service.invalidarPorEvento(30L);

        verify(entradaRepository).saveAll(List.of(emitted));
        verifyNoInteractions(reservaFeignClient, compraFeignClient);
    }

    @Test
    void noInvalidaSiElEventoNoEstaCancelado() {
        when(eventoFeignClient.obtenerEvento(30L))
                .thenReturn(new EventoDTO(30L, null, "PUBLICADO"));

        assertThrows(RuntimeException.class,
                () -> service.invalidarPorEvento(30L));

        verify(entradaRepository, never()).findByEventoIdAndEstado(any(), any());
        verify(entradaRepository, never()).saveAll(any());
    }

    private ReservaDTO reservaConfirmada(List<DetalleReservaDTO> detalles) {
        return new ReservaDTO(20L, 10L, 30L, "CONFIRMADA", detalles);
    }

    private Entrada entrada(Long id, String qr, String estado) {
        Entrada entrada = new Entrada();
        setField(entrada, "id", id);
        entrada.setCodigoQr(qr);
        entrada.setEstado(estado);
        entrada.setReservaId(20L);
        entrada.setCompraId(1L);
        return entrada;
    }

    private void verifyNoEmission() {
        verify(entradaRepository, never()).saveAllAndFlush(anyList());
    }

    private void setField(Object target, String name, Object value) {
        try {
            Field field = target.getClass().getDeclaredField(name);
            field.setAccessible(true);
            field.set(target, value);
        } catch (ReflectiveOperationException exception) {
            throw new AssertionError("No se pudo preparar el fixture", exception);
        }
    }
}
