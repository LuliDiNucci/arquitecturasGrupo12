
package com.ticketya.msvc_fila_virtual;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.ticketya.msvc_fila_virtual.clients.EventosFeignClient;
import com.ticketya.msvc_fila_virtual.dtos.EventoDTO;
import com.ticketya.msvc_fila_virtual.entities.ClienteEnFila;
import com.ticketya.msvc_fila_virtual.entities.EstadoFila;
import com.ticketya.msvc_fila_virtual.repositories.ClienteEnFilaRepository;
import com.ticketya.msvc_fila_virtual.services.FilaVirtualService;

@SpringBootTest
@ActiveProfiles("test")
class FilaVirtualConcurrenciaTest {

    @Autowired
    private FilaVirtualService service;

    @Autowired
    private ClienteEnFilaRepository repository;

    @MockitoBean
    private EventosFeignClient eventosFeignClient;

    @BeforeEach
    void preparar() {
        repository.deleteAll();

        when(eventosFeignClient.obtenerEvento(anyLong()))
                .thenAnswer(invocacion -> new EventoDTO(
                        invocacion.getArgument(0),
                        "Evento de prueba",
                        "PUBLICADO",
                        true));
    }

    @Test
    void dosIngresosSimultaneosNoDuplicanCliente()
            throws Exception {

        Long eventoId = 100L;
        Long clienteId = 25L;

        List<Throwable> errores = ejecutarEnParalelo(
                () -> service.ingresar(eventoId, clienteId),
                () -> service.ingresar(eventoId, clienteId));

        long registros = repository.findAll().stream()
                .filter(c -> c.getEventoId().equals(eventoId))
                .filter(c -> c.getClienteId().equals(clienteId))
                .count();

        assertEquals(1, registros,
                "No debe existir más de un registro "
                        + "por evento y cliente");

        assertTrue(errores.size() <= 1,
                "Al menos una solicitud debe poder ingresar");
    }

    @Test
    void dosHabilitacionesSimultaneasNoCreanDosTandas()
            throws Exception {

        Long eventoId = 200L;

        for (long clienteId = 1; clienteId <= 6; clienteId++) {
            repository.save(
                    new ClienteEnFila(eventoId, clienteId));
        }

        repository.flush();

        List<Throwable> errores = ejecutarEnParalelo(
                () -> service.habilitarTanda(eventoId, 3),
                () -> service.habilitarTanda(eventoId, 3));

        List<ClienteEnFila> habilitados = repository.findByEventoIdAndEstadoOrderByIdAsc(
                eventoId, EstadoFila.HABILITADO);

        List<ClienteEnFila> esperando = repository.findByEventoIdAndEstadoOrderByIdAsc(
                eventoId, EstadoFila.EN_ESPERA);

        assertEquals(3, habilitados.size(),
                "Solo deben habilitarse tres clientes");

        assertEquals(3, esperando.size(),
                "Los otros tres deben seguir esperando");

        assertEquals(1, errores.size(),
                "Una solicitud debe ser rechazada "
                        + "porque ya existe una tanda activa");

        List<Long> clientesHabilitados = habilitados.stream()
                .map(ClienteEnFila::getClienteId)
                .toList();

        assertEquals(List.of(1L, 2L, 3L),
                clientesHabilitados,
                "Deben habilitarse los primeros tres clientes");
    }

    private List<Throwable> ejecutarEnParalelo(
            Runnable primera,
            Runnable segunda) throws Exception {

        ExecutorService executor = Executors.newFixedThreadPool(2);

        CountDownLatch inicio = new CountDownLatch(1);

        try {
            Future<Throwable> tarea1 = executor.submit(
                    () -> ejecutar(inicio, primera));

            Future<Throwable> tarea2 = executor.submit(
                    () -> ejecutar(inicio, segunda));

            inicio.countDown();

            List<Throwable> errores = new ArrayList<>();

            Throwable error1 = tarea1.get(
                    20, TimeUnit.SECONDS);

            Throwable error2 = tarea2.get(
                    20, TimeUnit.SECONDS);

            if (error1 != null) {
                errores.add(error1);
            }

            if (error2 != null) {
                errores.add(error2);
            }

            return errores;

        } finally {
            executor.shutdownNow();
        }
    }

    private Throwable ejecutar(
            CountDownLatch inicio,
            Runnable tarea) {

        try {
            inicio.await();
            tarea.run();
            return null;

        } catch (Throwable ex) {
            return ex;
        }
    }
}
