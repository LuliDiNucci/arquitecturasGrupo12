
package com.ticketya.compras.services;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.SimpleTransactionStatus;

import com.ticketya.compras.clients.ReservaClient;
import com.ticketya.compras.dtos.CompraDTO;
import com.ticketya.compras.dtos.PagoDTO;
import com.ticketya.compras.dtos.ProcesarPagoDTO;
import com.ticketya.compras.dtos.ReservaDTO;
import com.ticketya.compras.entities.Compra;
import com.ticketya.compras.entities.Pago;
import com.ticketya.compras.enums.EstadoPago;
import com.ticketya.compras.repositories.CompraRepository;
import com.ticketya.compras.repositories.PagoRepository;

public class PagoServiceImplTest {

    private CompraRepository compraRepo;
    private PagoRepository pagoRepo;
    private ReservaClient reservaClient;
    private PasarelaPagoMock pasarela;
    private CompraService compraService;
    private ReintegroService reintegroService;

    private PagoServiceImpl service;

    @BeforeEach
    public void preparar() {

        compraRepo = mock(CompraRepository.class);
        pagoRepo = mock(PagoRepository.class);
        reservaClient = mock(ReservaClient.class);
        pasarela = mock(PasarelaPagoMock.class);
        compraService = mock(CompraService.class);
        reintegroService = mock(ReintegroService.class);

        PlatformTransactionManager gestor = mock(PlatformTransactionManager.class);

        when(gestor.getTransaction(any()))
                .thenAnswer(inv -> new SimpleTransactionStatus());

        service = new PagoServiceImpl(
                compraRepo,
                pagoRepo,
                reservaClient,
                pasarela,
                compraService,
                reintegroService,
                gestor);
    }

    // TEST 1: repetir la misma clave no genera otro cobro.
    @Test
    public void testPagoIdempotente() {

        Compra compra = crearCompra("PENDIENTE");

        when(compraRepo.bloquearCompra(1L))
                .thenReturn(Optional.of(compra));

        ReservaDTO reserva = crearReservaActiva();

        when(reservaClient.obtenerReserva(10L))
                .thenReturn(reserva);

        final Pago[] pagoGuardado = { null };

        when(pagoRepo.findByClaveIdempotencia("CLAVE-1"))
                .thenAnswer(inv -> Optional.ofNullable(pagoGuardado[0]));

        when(pagoRepo.saveAndFlush(any(Pago.class)))
                .thenAnswer(inv -> {

                    Pago pago = inv.getArgument(0);
                    pago.setId(50L);
                    pagoGuardado[0] = pago;

                    return pago;
                });

        when(pasarela.procesarPago(
                any(BigDecimal.class),
                eq("TARJETA"),
                eq("CLAVE-1")))
                .thenReturn(
                        new PasarelaPagoMock.ResultadoPasarela(
                                EstadoPago.APROBADO,
                                "MOCK-123"));

        when(compraService.confirmarCompra(1L))
                .thenReturn(new CompraDTO(
                        1L, 20L, 10L, 21L,
                        null, new BigDecimal("10000.00"),
                        "CONFIRMADA", List.of()));

        ProcesarPagoDTO solicitud = crearSolicitud("TARJETA", "CLAVE-1");

        PagoDTO primero = service.procesarPago(1L, solicitud);
        PagoDTO segundo = service.procesarPago(1L, solicitud);

        assertEquals(primero.getId(), segundo.getId());
        assertEquals("APROBADO", segundo.getEstado());
        assertEquals(primero.getReferenciaExterna(),
                segundo.getReferenciaExterna());

        // La pasarela solamente debe procesar un cobro.
        verify(pasarela, times(1))
                .procesarPago(
                        any(BigDecimal.class),
                        eq("TARJETA"),
                        eq("CLAVE-1"));

        verify(pagoRepo, times(1))
                .saveAndFlush(any(Pago.class));
    }

    // TEST 2: misma clave con otro medio de pago.
    @Test
    public void testClaveReutilizadaConDatosDiferentes() {

        Compra compra = crearCompra("PENDIENTE");

        when(compraRepo.bloquearCompra(1L))
                .thenReturn(Optional.of(compra));

        Pago anterior = crearPagoAnterior(compra);

        when(pagoRepo.findByClaveIdempotencia("CLAVE-1"))
                .thenReturn(Optional.of(anterior));

        try {

            service.procesarPago(
                    1L,
                    crearSolicitud(
                            "TRANSFERENCIA",
                            "CLAVE-1"));

            fail("Debía rechazar la clave reutilizada");

        } catch (IllegalStateException e) {

            assertTrue(e.getMessage()
                    .contains("idempotencia"));
        }

        verifyNoInteractions(pasarela);
    }

    // TEST 3: una clave no puede usarse para otra compra.
    @Test
    public void testClaveUtilizadaEnOtraCompra() {

        Compra compraActual = crearCompra("PENDIENTE");

        when(compraRepo.bloquearCompra(1L))
                .thenReturn(Optional.of(compraActual));

        Compra otraCompra = new Compra();
        otraCompra.setId(99L);

        Pago anterior = crearPagoAnterior(otraCompra);

        when(pagoRepo.findByClaveIdempotencia("CLAVE-1"))
                .thenReturn(Optional.of(anterior));

        try {

            service.procesarPago(
                    1L,
                    crearSolicitud("TARJETA", "CLAVE-1"));

            fail("No debería permitir reutilizar la clave");

        } catch (IllegalStateException e) {

            assertTrue(e.getMessage()
                    .contains("idempotencia"));
        }

        verifyNoInteractions(pasarela);
    }

    // TEST 4: no cobrar nuevamente una compra ya pagada.
    @Test
    public void testNoPermitirSegundoPago() {

        Compra compra = crearCompra("CONFIRMACION_PENDIENTE");

        when(compraRepo.bloquearCompra(1L))
                .thenReturn(Optional.of(compra));

        when(pagoRepo.findByClaveIdempotencia("CLAVE-2"))
                .thenReturn(Optional.empty());

        try {

            service.procesarPago(
                    1L,
                    crearSolicitud("TARJETA", "CLAVE-2"));

            fail("No debería permitir un segundo pago");

        } catch (IllegalStateException e) {

            assertTrue(e.getMessage()
                    .contains("pendiente de pago"));
        }

        verifyNoInteractions(pasarela);
    }

    // MÉTODOS AUXILIARES DE LOS TESTS

    private Compra crearCompra(String estado) {

        Compra compra = new Compra();

        compra.setId(1L);
        compra.setClienteId(20L);
        compra.setReservaId(10L);
        compra.setEventoId(21L);
        compra.setTotal(new BigDecimal("10000.00"));
        compra.setEstado(estado);

        return compra;
    }

    private ReservaDTO crearReservaActiva() {

        ReservaDTO reserva = new ReservaDTO();

        reserva.setId(10L);
        reserva.setClienteId(20L);
        reserva.setEventoId(21L);
        reserva.setEstado("ACTIVA");

        reserva.setFechaVencimiento(
                LocalDateTime.now().plusMinutes(10));

        return reserva;
    }

    private ProcesarPagoDTO crearSolicitud(
            String medio, String clave) {

        ProcesarPagoDTO solicitud = new ProcesarPagoDTO();

        solicitud.setMedioPago(medio);
        solicitud.setClaveIdempotencia(clave);

        return solicitud;
    }

    private Pago crearPagoAnterior(Compra compra) {

        Pago pago = new Pago();

        pago.setId(50L);
        pago.setCompra(compra);
        pago.setImporte(new BigDecimal("10000.00"));
        pago.setMedioPago("TARJETA");
        pago.setClaveIdempotencia("CLAVE-1");
        pago.setEstado("APROBADO");

        return pago;
    }
}
