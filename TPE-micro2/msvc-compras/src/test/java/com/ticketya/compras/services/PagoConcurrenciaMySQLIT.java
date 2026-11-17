
package com.ticketya.compras.services;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.ticketya.compras.clients.ReservaClient;
import com.ticketya.compras.dtos.CompraDTO;
import com.ticketya.compras.dtos.PagoDTO;
import com.ticketya.compras.dtos.ProcesarPagoDTO;
import com.ticketya.compras.dtos.ReservaDTO;
import com.ticketya.compras.entities.Compra;
import com.ticketya.compras.enums.EstadoPago;
import com.ticketya.compras.repositories.CompraRepository;
import com.ticketya.compras.repositories.PagoRepository;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:mysql://localhost:3306/ticketya_compras_test",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
public class PagoConcurrenciaMySQLIT {

    @Autowired
    private PagoService pagoService;

    @Autowired
    private CompraRepository compraRepo;

    @Autowired
    private PagoRepository pagoRepo;

    @MockitoBean
    private ReservaClient reservaClient;

    @MockitoBean
    private CompraService compraService;

    @MockitoBean
    private PasarelaPagoMock pasarela;

    @Test
    public void dosSolicitudesMismaCompraSoloUnCobro()
            throws Exception {

        // 1. Crear una compra real en la base de test.
        Compra compra = new Compra();
        compra.setClienteId(200L);
        compra.setReservaId(90001L);
        compra.setEventoId(21L);
        compra.setFechaCompra(LocalDate.now());
        compra.setTotal(new BigDecimal("10000.00"));
        compra.setEstado("PENDIENTE");

        compra = compraRepo.saveAndFlush(compra);
        Long compraId = compra.getId();

        // 2. Simular una reserva vigente.
        ReservaDTO reserva = new ReservaDTO();
        reserva.setId(90001L);
        reserva.setClienteId(200L);
        reserva.setEventoId(21L);
        reserva.setEstado("ACTIVA");
        reserva.setFechaVencimiento(
                LocalDateTime.now().plusMinutes(10));

        when(reservaClient.obtenerReserva(90001L))
                .thenReturn(reserva);

        // 3. Simular la confirmación posterior al pago.
        when(compraService.confirmarCompra(compraId))
                .thenReturn(new CompraDTO(
                        compraId,
                        200L,
                        90001L,
                        21L,
                        LocalDate.now(),
                        new BigDecimal("10000.00"),
                        "CONFIRMADA",
                        List.of()));

        // 4. Simular la pasarela.
        when(pasarela.procesarPago(
                any(BigDecimal.class),
                eq("TARJETA"),
                anyString()))
                .thenReturn(
                        new PasarelaPagoMock.ResultadoPasarela(
                                EstadoPago.APROBADO,
                                "MOCK-TEST-CONCURRENCIA"));

        // Ambas solicitudes utilizan la misma clave.
        ProcesarPagoDTO solicitud = new ProcesarPagoDTO();
        solicitud.setMedioPago("TARJETA");
        solicitud.setClaveIdempotencia(
                UUID.randomUUID().toString());

        ExecutorService executor = Executors.newFixedThreadPool(2);

        CountDownLatch preparados = new CountDownLatch(2);
        CountDownLatch inicio = new CountDownLatch(1);

        try {
            // 5. Lanzar dos solicitudes simultáneas.
            Future<PagoDTO> primero = executor.submit(() -> {
                preparados.countDown();
                inicio.await();
                return pagoService.procesarPago(
                        compraId, solicitud);
            });

            Future<PagoDTO> segundo = executor.submit(() -> {
                preparados.countDown();
                inicio.await();
                return pagoService.procesarPago(
                        compraId, solicitud);
            });

            assertTrue(preparados.await(10, TimeUnit.SECONDS));
            inicio.countDown();

            PagoDTO pago1 = primero.get(30, TimeUnit.SECONDS);
            PagoDTO pago2 = segundo.get(30, TimeUnit.SECONDS);

            // 6. Verificar idempotencia.
            assertEquals(pago1.getId(), pago2.getId());
            assertEquals("APROBADO", pago1.getEstado());
            assertEquals("APROBADO", pago2.getEstado());

            // Solo debe existir un registro de pago.
            assertEquals(
                    1,
                    pagoRepo
                            .findByCompra_IdOrderByFechaSolicitudDesc(
                                    compraId)
                            .size());

            // La pasarela debe haberse invocado una sola vez.
            verify(pasarela, times(1))
                    .procesarPago(
                            any(BigDecimal.class),
                            eq("TARJETA"),
                            anyString());

        } finally {
            inicio.countDown();
            executor.shutdownNow();
        }
    }

    @Test
    public void mismaClaveEnDosComprasDistintasSoloUnCobro()
            throws Exception {

        // Crear dos compras distintas en MySQL.
        Compra compra1 = new Compra();
        compra1.setClienteId(200L);
        compra1.setReservaId(91001L);
        compra1.setEventoId(21L);
        compra1.setFechaCompra(LocalDate.now());
        compra1.setTotal(new BigDecimal("10000.00"));
        compra1.setEstado("PENDIENTE");

        compra1 = compraRepo.saveAndFlush(compra1);

        Compra compra2 = new Compra();
        compra2.setClienteId(200L);
        compra2.setReservaId(91002L);
        compra2.setEventoId(21L);
        compra2.setFechaCompra(LocalDate.now());
        compra2.setTotal(new BigDecimal("10000.00"));
        compra2.setEstado("PENDIENTE");

        compra2 = compraRepo.saveAndFlush(compra2);

        Long id1 = compra1.getId();
        Long id2 = compra2.getId();

        // Simular las dos reservas.
        ReservaDTO reserva1 = new ReservaDTO();
        reserva1.setId(91001L);
        reserva1.setClienteId(200L);
        reserva1.setEventoId(21L);
        reserva1.setEstado("ACTIVA");
        reserva1.setFechaVencimiento(
                LocalDateTime.now().plusMinutes(10));

        ReservaDTO reserva2 = new ReservaDTO();
        reserva2.setId(91002L);
        reserva2.setClienteId(200L);
        reserva2.setEventoId(21L);
        reserva2.setEstado("ACTIVA");
        reserva2.setFechaVencimiento(
                LocalDateTime.now().plusMinutes(10));

        when(reservaClient.obtenerReserva(91001L))
                .thenReturn(reserva1);

        when(reservaClient.obtenerReserva(91002L))
                .thenReturn(reserva2);

        when(compraService.confirmarCompra(anyLong()))
                .thenAnswer(inv -> {

                    Long id = inv.getArgument(0);

                    return new CompraDTO(
                            id,
                            200L,
                            null,
                            21L,
                            LocalDate.now(),
                            new BigDecimal("10000.00"),
                            "CONFIRMADA",
                            List.of());
                });

        when(pasarela.procesarPago(
                any(BigDecimal.class),
                eq("TARJETA"),
                anyString()))
                .thenReturn(
                        new PasarelaPagoMock.ResultadoPasarela(
                                EstadoPago.APROBADO,
                                "MOCK-PRUEBA-DUPLICADO"));

        // Misma clave para compras DIFERENTES.
        String clave = UUID.randomUUID().toString();

        ProcesarPagoDTO solicitud = new ProcesarPagoDTO();
        solicitud.setMedioPago("TARJETA");
        solicitud.setClaveIdempotencia(clave);

        ExecutorService executor = Executors.newFixedThreadPool(2);

        CountDownLatch preparados = new CountDownLatch(2);
        CountDownLatch inicio = new CountDownLatch(1);

        try {

            Future<PagoDTO> futuro1 = executor.submit(() -> {
                preparados.countDown();
                inicio.await();
                return pagoService.procesarPago(id1, solicitud);
            });

            Future<PagoDTO> futuro2 = executor.submit(() -> {
                preparados.countDown();
                inicio.await();
                return pagoService.procesarPago(id2, solicitud);
            });

            assertTrue(preparados.await(
                    10, TimeUnit.SECONDS));

            inicio.countDown();

            int exitos = 0;
            int rechazados = 0;

            for (Future<PagoDTO> futuro : List.of(futuro1, futuro2)) {

                try {

                    PagoDTO pago = futuro.get(
                            30, TimeUnit.SECONDS);

                    assertEquals("APROBADO", pago.getEstado());
                    exitos++;

                } catch (ExecutionException e) {

                    assertTrue(
                            e.getCause() instanceof RuntimeException,
                            "Se esperaba un conflicto de idempotencia");

                    rechazados++;
                }
            }

            // Solo una compra puede utilizar esta clave.
            assertEquals(1, exitos);
            assertEquals(1, rechazados);

            assertTrue(
                    pagoRepo.findByClaveIdempotencia(clave)
                            .isPresent());

            // La pasarela solamente procesa un cobro.
            verify(pasarela, times(1))
                    .procesarPago(
                            any(BigDecimal.class),
                            eq("TARJETA"),
                            eq(clave));

        } finally {

            inicio.countDown();
            executor.shutdownNow();
        }
    }

}
