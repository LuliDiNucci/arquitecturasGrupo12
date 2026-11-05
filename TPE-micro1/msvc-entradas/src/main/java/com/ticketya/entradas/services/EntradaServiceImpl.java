package com.ticketya.entradas.services;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ticketya.entradas.clients.CompraFeignClient;
import com.ticketya.entradas.clients.EventoFeignClient;
import com.ticketya.entradas.clients.ReservaFeignClient;
import com.ticketya.entradas.dtos.EntradaRequestDTO;
import com.ticketya.entradas.dtos.EntradaResponseDTO;
import com.ticketya.entradas.dtos.EventoDTO;
import com.ticketya.entradas.entities.Entrada;
import com.ticketya.entradas.repositories.EntradaRepository;

@Service
public class EntradaServiceImpl implements EntradaService {

    private final EntradaRepository entradaRepository;
    private final EventoFeignClient eventoFeignClient;
    private final ReservaFeignClient reservaFeignClient;
    private final CompraFeignClient compraFeignClient;

    public EntradaServiceImpl(
            EntradaRepository entradaRepository,
            EventoFeignClient eventoFeignClient,
            ReservaFeignClient reservaFeignClient,
            CompraFeignClient compraFeignClient) {

        this.entradaRepository = entradaRepository;
        this.eventoFeignClient = eventoFeignClient;
        this.reservaFeignClient = reservaFeignClient;
        this.compraFeignClient = compraFeignClient;
    }

    @Override
    @Transactional
    public EntradaResponseDTO emitir(EntradaRequestDTO dto) {

        // Verificar que el evento exista.
        EventoDTO evento = eventoFeignClient.obtenerEvento(dto.eventoId());

        if (evento == null) {
            throw new RuntimeException("El evento no existe");
        }

        if ("CANCELADO".equals(evento.estado())) {
            throw new RuntimeException("No se pueden emitir entradas para un evento cancelado");
        }

        Entrada entrada = new Entrada();

        entrada.setCompraId(dto.compraId());
        entrada.setReservaId(dto.reservaId());
        entrada.setClienteId(dto.clienteId());
        entrada.setEventoId(dto.eventoId());
        entrada.setSectorId(dto.sectorId());
        entrada.setAsientoId(dto.asientoId());

        // Generar un código QR único.
        entrada.setCodigoQr(UUID.randomUUID().toString());

        entrada.setEstado("EMITIDA");
        entrada.setFechaEmision(LocalDateTime.now());

        Entrada guardada = entradaRepository.save(entrada);

        return convertirDTO(guardada);
    }

    @Override
    public EntradaResponseDTO obtenerPorId(Long id) {

        Entrada entrada = entradaRepository.findById(id)
                .orElseThrow(() ->
                    new RuntimeException("Entrada no encontrada"));

        return convertirDTO(entrada);
    }

    @Override
    public List<EntradaResponseDTO> obtenerPorCliente(Long clienteId) {

        return entradaRepository.findByClienteId(clienteId)
                .stream()
                .map(this::convertirDTO)
                .toList();
    }

    @Override
    @Transactional
    public void invalidarPorEvento(Long eventoId) {

        EventoDTO evento = eventoFeignClient.obtenerEvento(eventoId);

        if (!"CANCELADO".equals(evento.estado())) {
            throw new RuntimeException("El evento no está cancelado");
        }

        List<Entrada> entradas =
                entradaRepository.findByEventoIdAndEstado(eventoId, "EMITIDA");

        for (Entrada entrada : entradas) {

            entrada.setEstado("INVALIDADA");

            // Liberar el lugar asociado a la reserva.
            reservaFeignClient.liberarReserva(entrada.getReservaId());

            // Coordinar el reintegro.
            compraFeignClient.reintegrarCompra(entrada.getCompraId());
        }

        entradaRepository.saveAll(entradas);
    }

    private EntradaResponseDTO convertirDTO(Entrada entrada) {

        return new EntradaResponseDTO(
                entrada.getId(),
                entrada.getCompraId(),
                entrada.getReservaId(),
                entrada.getClienteId(),
                entrada.getEventoId(),
                entrada.getSectorId(),
                entrada.getAsientoId(),
                entrada.getCodigoQr(),
                entrada.getEstado(),
                entrada.getFechaEmision(),
                entrada.getFechaUso()
        );
    }
}