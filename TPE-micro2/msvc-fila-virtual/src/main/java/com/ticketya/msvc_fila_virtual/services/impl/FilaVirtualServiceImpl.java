
package com.ticketya.msvc_fila_virtual.services.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.ticketya.msvc_fila_virtual.clients.EventosFeignClient;
import com.ticketya.msvc_fila_virtual.dtos.ClienteEnFilaDTO;
import com.ticketya.msvc_fila_virtual.dtos.EventoDTO;
import com.ticketya.msvc_fila_virtual.dtos.ValidacionTurnoDTO;
import com.ticketya.msvc_fila_virtual.entities.ClienteEnFila;
import com.ticketya.msvc_fila_virtual.entities.EstadoFila;
import com.ticketya.msvc_fila_virtual.repositories.ClienteEnFilaRepository;
import com.ticketya.msvc_fila_virtual.services.FilaVirtualService;

import feign.FeignException;

@Service
public class FilaVirtualServiceImpl implements FilaVirtualService {

    private static final int DURACION_TURNO_MINUTOS = 10;

    private final ClienteEnFilaRepository repository;
    private final EventosFeignClient eventosFeignClient;
    private static final Logger logger = LoggerFactory.getLogger(FilaVirtualServiceImpl.class);

    public FilaVirtualServiceImpl(
            ClienteEnFilaRepository repository,
            EventosFeignClient eventosFeignClient) {

        this.repository = repository;
        this.eventosFeignClient = eventosFeignClient;
    }

    @Override
    @Transactional
    public ClienteEnFilaDTO ingresar(Long eventoId, Long clienteId) {

        validarIds(eventoId, clienteId);

        validarEventoAltaDemanda(eventoId);

        repository.ingresarSiNoExiste(
                eventoId,
                clienteId,
                LocalDateTime.now());

        ClienteEnFila cliente = repository
                .findByEventoIdAndClienteId(eventoId, clienteId)
                .orElseThrow(() -> new IllegalStateException(
                        "No se pudo recuperar el cliente de la fila"));

        actualizarVencimiento(cliente);

        return convertirADTO(cliente);
    }

    @Override
    @Transactional
    public ClienteEnFilaDTO consultarEstado(
            Long eventoId,
            Long clienteId) {

        validarIds(eventoId, clienteId);

        ClienteEnFila cliente = repository
                .findByEventoIdAndClienteId(eventoId, clienteId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "El cliente no está registrado en la fila"));

        actualizarVencimiento(cliente);

        return convertirADTO(cliente);
    }

    @Override
    @Transactional
    public List<ClienteEnFilaDTO> habilitarTanda(
            Long eventoId,
            Integer cantidad) {

        if (eventoId == null || eventoId <= 0) {
            throw new IllegalArgumentException(
                    "El eventoId debe ser positivo");
        }

        if (cantidad == null || cantidad < 1 || cantidad > 100) {
            throw new IllegalArgumentException(
                    "La cantidad debe estar entre 1 y 100");
        }

        repository.findFirstByEventoIdOrderByIdAsc(eventoId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "El evento no tiene clientes en la fila"));

        LocalDateTime ahora = LocalDateTime.now();

        // Revisamos turnos habilitados previamente.
        List<ClienteEnFila> habilitados = repository
                .findByEventoIdAndEstadoOrderByIdAsc(
                        eventoId, EstadoFila.HABILITADO);

        boolean existeTandaActiva = false;

        for (ClienteEnFila cliente : habilitados) {
            if (cliente.tieneTurnoVencido(ahora)) {
                cliente.vencer();
            } else {
                existeTandaActiva = true;
            }
        }

        if (existeTandaActiva) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Ya existe una tanda activa para este evento");
        }

        // Obtenemos los clientes que siguen esperando.
        List<ClienteEnFila> esperando = repository
                .findByEventoIdAndEstadoOrderByIdAsc(
                        eventoId, EstadoFila.EN_ESPERA);

        if (esperando.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "No hay clientes esperando para habilitar");
        }

        List<ClienteEnFilaDTO> resultado = new ArrayList<>();

        int cantidadAHabilitar = Math.min(cantidad, esperando.size());

        for (int i = 0; i < cantidadAHabilitar; i++) {
            ClienteEnFila cliente = esperando.get(i);

            cliente.habilitar(ahora, DURACION_TURNO_MINUTOS);

            resultado.add(convertirADTO(cliente));
        }

        repository.flush();

        return resultado;
    }

    private void actualizarVencimiento(ClienteEnFila cliente) {
        if (cliente.tieneTurnoVencido(LocalDateTime.now())) {
            cliente.vencer();
        }
    }

    private ClienteEnFilaDTO convertirADTO(ClienteEnFila cliente) {

        Long posicion = null;

        if (cliente.getEstado() == EstadoFila.EN_ESPERA) {

            long anteriores = repository
                    .countByEventoIdAndEstadoAndIdLessThan(
                            cliente.getEventoId(),
                            EstadoFila.EN_ESPERA,
                            cliente.getId());

            posicion = anteriores + 1;
        }

        return ClienteEnFilaDTO.desdeEntidad(cliente, posicion);
    }

    private void validarIds(Long eventoId, Long clienteId) {

        if (eventoId == null || eventoId <= 0) {
            throw new IllegalArgumentException(
                    "El eventoId debe ser positivo");
        }

        if (clienteId == null || clienteId <= 0) {
            throw new IllegalArgumentException(
                    "El clienteId debe ser positivo");
        }
    }

    @Override
    @Transactional
    public ValidacionTurnoDTO validarTurno(
            Long eventoId,
            Long clienteId) {

        validarIds(eventoId, clienteId);

        ClienteEnFila cliente = repository
                .bloquearPorEventoYCliente(eventoId, clienteId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "El cliente no está registrado en la fila"));

        actualizarVencimiento(cliente);

        boolean autorizado = cliente.getEstado() == EstadoFila.HABILITADO
                && cliente.getFechaVencimiento() != null
                && cliente.getFechaVencimiento()
                        .isAfter(LocalDateTime.now());

        return new ValidacionTurnoDTO(
                eventoId,
                clienteId,
                autorizado,
                cliente.getEstado(),
                cliente.getFechaVencimiento());
    }

    @Override
    @Transactional
    public ClienteEnFilaDTO finalizarTurno(
            Long eventoId,
            Long clienteId) {

        validarIds(eventoId, clienteId);

        ClienteEnFila cliente = repository
                .bloquearPorEventoYCliente(eventoId, clienteId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "El cliente no está registrado en la fila"));

        if (cliente.getEstado() == EstadoFila.FINALIZADO) {
            return convertirADTO(cliente);
        }

        if (cliente.getEstado() != EstadoFila.HABILITADO
                && cliente.getEstado() != EstadoFila.VENCIDO) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "El cliente nunca tuvo un turno habilitado");
        }

        cliente.finalizar();

        return convertirADTO(cliente);
    }

    private void validarEventoAltaDemanda(Long eventoId) {

        EventoDTO evento;

        try {
            evento = eventosFeignClient.obtenerEvento(eventoId);

        } catch (FeignException.NotFound ex) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "El evento no existe");

        } catch (FeignException.Forbidden ex) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "El evento no está disponible para ingresar a la fila");

        } catch (FeignException ex) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "No se pudo consultar Eventos y Lugares", ex);
        }

        if (evento == null) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Eventos y Lugares devolvió una respuesta vacía");
        }

        if (!"PUBLICADO".equals(evento.estado())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "El evento no está publicado");
        }

        if (!evento.altaDemanda()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "El evento no requiere fila virtual");
        }
    }

    @Scheduled(fixedDelay = 60000)
    @Transactional
    public void vencerTurnosAutomaticamente() {

        int cantidad = repository.vencerTurnosExpirados(
                EstadoFila.HABILITADO,
                EstadoFila.VENCIDO,
                LocalDateTime.now());

        if (cantidad > 0) {
            logger.info(
                    "Se marcaron {} turnos como VENCIDO",
                    cantidad);
        }
    }

}
