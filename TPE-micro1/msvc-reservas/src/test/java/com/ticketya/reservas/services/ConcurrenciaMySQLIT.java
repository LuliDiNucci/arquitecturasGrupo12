package com.ticketya.reservas.services;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.ticketya.reservas.clients.EventoClient;
import com.ticketya.reservas.dtos.AsientoDTO;
import com.ticketya.reservas.dtos.CrearReservaDTO;
import com.ticketya.reservas.dtos.DetalleReservaDTO;
import com.ticketya.reservas.dtos.EventoDTO;
import com.ticketya.reservas.dtos.PrecioSectorDTO;
import com.ticketya.reservas.dtos.SectorDTO;

/** Ejecutar SOLO con una base MySQL de test separada; no utiliza H2. */
@SpringBootTest(properties = {
        "spring.datasource.url=${TEST_DB_URL:jdbc:mysql://localhost:3306/ticketya_reservas_test}",
        "spring.datasource.username=${TEST_DB_USER:root}",
        "spring.datasource.password=${TEST_DB_PASSWORD:}",
        "spring.jpa.hibernate.ddl-auto=update"
})
@EnabledIfEnvironmentVariable(named = "TICKETYA_MYSQL_TEST", matches = "true")
class ConcurrenciaMySQLIT {
    private static final long EVENTO = 9923371L;
    private static final long SECTOR = 9923372L;
    private static final long ASIENTO = 9923373L;

    @Autowired
    ReservaService reservaService;
    @Autowired
    JdbcTemplate jdbc;
    @MockitoBean
    EventoClient eventoClient;

    private CrearReservaDTO solicitud(int cliente) {
        CrearReservaDTO pedido = new CrearReservaDTO();
        pedido.setClienteId((long) cliente);
        pedido.setEventoId(EVENTO);
        pedido.setClaveIdempotencia(UUID.randomUUID().toString());
        DetalleReservaDTO detalle = new DetalleReservaDTO();
        detalle.setSectorId(SECTOR);
        detalle.setCantidad(1);
        detalle.setAsientoIds(List.of(ASIENTO));
        pedido.setDetalles(List.of(detalle));
        return pedido;
    }

    @Test
    void muchasSolicitudesMismoAsientoSoloUnaGana() throws Exception {
        // Se ejecuta sobre una BD DEDICADA de test. Limpiar datos residuales del evento
        // de prueba.
        jdbc.update("DELETE FROM entrada_liberada WHERE reserva_id IN (SELECT id FROM reserva WHERE evento_id=?)",
                EVENTO);
        jdbc.update(
                "DELETE FROM asignacion_asiento WHERE detalle_reserva_id IN (SELECT id FROM detalle_reserva WHERE reserva_id IN (SELECT id FROM reserva WHERE evento_id=?))",
                EVENTO);
        jdbc.update("DELETE FROM detalle_reserva WHERE reserva_id IN (SELECT id FROM reserva WHERE evento_id=?)",
                EVENTO);
        jdbc.update("DELETE FROM reserva WHERE evento_id=?", EVENTO);
        jdbc.update("DELETE FROM bloqueo_evento WHERE evento_id=?", EVENTO);

        EventoDTO evento = new EventoDTO();
        evento.setId(EVENTO);
        evento.setEstado("PUBLICADO");
        evento.setLugarId(200L);
        evento.setFechaHora(LocalDateTime.now().plusDays(10));
        SectorDTO sector = new SectorDTO();
        sector.setId(SECTOR);
        sector.setLugarId(200L);
        sector.setNumerado(true);
        AsientoDTO asiento = new AsientoDTO();
        asiento.setId(ASIENTO);
        asiento.setSectorId(SECTOR);
        PrecioSectorDTO precio = new PrecioSectorDTO();
        precio.setEventoId(EVENTO);
        precio.setSectorId(SECTOR);
        precio.setPrecio(new BigDecimal("100.00"));
        when(eventoClient.obtenerEvento(EVENTO)).thenReturn(evento);
        when(eventoClient.obtenerSector(SECTOR)).thenReturn(sector);
        when(eventoClient.obtenerAsiento(ASIENTO)).thenReturn(asiento);
        when(eventoClient.obtenerPrecioSector(EVENTO, SECTOR)).thenReturn(precio);

        reservaService.habilitarEvento(EVENTO);

        int cantidad = 10;
        ExecutorService pool = Executors.newFixedThreadPool(cantidad);
        CountDownLatch salida = new CountDownLatch(1);
        AtomicInteger exitos = new AtomicInteger();
        List<Future<?>> resultados = new ArrayList<>();
        try {
            for (int i = 1; i <= cantidad; i++) {
                final int cliente = i;
                resultados.add(pool.submit(() -> {
                    try {
                        salida.await();
                        reservaService.crearReserva(solicitud(cliente));
                        exitos.incrementAndGet();
                    } catch (IllegalStateException ex) {
                        // Asiento ocupado: rechazo esperado.
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException(ex);
                    }
                    return null;
                }));
            }
            salida.countDown();
            for (Future<?> resultado : resultados)
                resultado.get(30, TimeUnit.SECONDS);
            assertEquals(1, exitos.get(), "Solo una reserva debe ocupar el asiento");
        } finally {
            pool.shutdownNow();
        }
    }
}
