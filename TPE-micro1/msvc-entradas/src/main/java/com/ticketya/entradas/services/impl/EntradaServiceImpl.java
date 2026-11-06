
package com.ticketya.entradas.services.impl;

import com.ticketya.entradas.services.EntradaService;
import com.ticketya.entradas.clients.EventoFeignClient;
import com.ticketya.entradas.clients.ReservaFeignClient;
import com.ticketya.entradas.clients.CompraFeignClient;
import com.ticketya.entradas.dtos.EventoDTO;
import com.ticketya.entradas.services.QrCodeService;
import com.ticketya.entradas.dtos.ResultadoEmisionDTO;

import com.ticketya.entradas.clients.CompraClient;
import com.ticketya.entradas.clients.ReservaClient;
import com.ticketya.entradas.dtos.CompraDTO;
import com.ticketya.entradas.dtos.DetalleReservaDTO;
import com.ticketya.entradas.dtos.EntradaDTO;
import com.ticketya.entradas.dtos.ReservaDTO;
import com.ticketya.entradas.entities.Entrada;
import com.ticketya.entradas.repositories.EntradaRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class EntradaServiceImpl implements EntradaService {

    private final EventoFeignClient eventoFeignClient;
    private final ReservaFeignClient reservaFeignClient;
    private final CompraFeignClient compraFeignClient;
    private final EntradaRepository entradaRepository;
    private final CompraClient compraClient;
    private final ReservaClient reservaClient;
    private final QrCodeService qrCodeService;

    public EntradaServiceImpl(
            EntradaRepository entradaRepository,
            CompraClient compraClient,
            ReservaClient reservaClient,
            QrCodeService qrCodeService,
            EventoFeignClient eventoFeignClient,
            ReservaFeignClient reservaFeignClient,
            CompraFeignClient compraFeignClient) {

        this.eventoFeignClient = eventoFeignClient;
        this.reservaFeignClient = reservaFeignClient;
        this.compraFeignClient = compraFeignClient;
        this.entradaRepository = entradaRepository;
        this.compraClient = compraClient;
        this.reservaClient = reservaClient;
        this.qrCodeService = qrCodeService;
    }

    /*
     * Emite una entrada por cada lugar adquirido.
     *
     * Primero verifica que la compra y la reserva
     * esten confirmadas.
     *
     * Si ya existen entradas para esa compra,
     * devuelve las existentes para evitar duplicados.
     */
    @Transactional
    @Override
    public ResultadoEmisionDTO emitir(Long compraId) {

        if (compraId == null || compraId <= 0) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "compraId invalido"
            );
        }

        // Verificar si la compra ya tiene entradas.
        List<Entrada> existentes =
                entradaRepository.findByCompraIdOrderByIdAsc(compraId);

        if (!existentes.isEmpty()) {
            return new ResultadoEmisionDTO(
                false,
                convertirTodas(existentes)
            );
        }

        // Consultar la compra en el microservicio Compras.
        CompraDTO compra = compraClient.obtenerConfirmacion(compraId);

        // Verificar que la compra este confirmada y pagada.
        if (compra == null
                || !compraId.equals(compra.getId())
                || compra.getClienteId() == null
                || compra.getReservaId() == null
                || !"CONFIRMADA".equals(compra.getEstado())
                || !Boolean.TRUE.equals(compra.getPagoAprobado())) {

            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "La compra no esta confirmada, el pago no esta aprobado o faltan datos"
            );
        }

        // Consultar la reserva asociada a esa compra.
        ReservaDTO reserva =
                reservaClient.obtener(compra.getReservaId());

        // Verificar que la reserva corresponda a la compra.
        if (reserva == null
                || !compra.getReservaId().equals(reserva.getId())
                || !compra.getClienteId().equals(reserva.getClienteId())
                || reserva.getEventoId() == null
                || !"CONFIRMADA".equals(reserva.getEstado())) {

            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "La reserva no esta confirmada o no coincide con la compra"
            );
        }

        // Verificar que la reserva tenga lugares adquiridos.
        if (reserva.getDetalles() == null
                || reserva.getDetalles().isEmpty()) {

            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "La reserva confirmada no tiene lugares"
            );
        }

        Set<Long> idsDetalles = new HashSet<>();
        List<Entrada> nuevas = new ArrayList<>();

        LocalDateTime ahora = LocalDateTime.now();

        // Recorrer los sectores incluidos en la reserva.
        for (DetalleReservaDTO detalle : reserva.getDetalles()) {

            validarDetalle(detalle, idsDetalles);

            int cantidad = detalle.getCantidad();

            List<Long> asientos = detalle.getAsientoIds();

            boolean numerado =
                    asientos != null && !asientos.isEmpty();

            // Crear una entrada por cada lugar adquirido.
            for (int i = 0; i < cantidad; i++) {

                Entrada entrada = new Entrada();

                // Guardar las relaciones de la entrada.
                entrada.setCompraId(compra.getId());
                entrada.setReservaId(reserva.getId());
                entrada.setDetalleReservaId(detalle.getId());
                entrada.setIndiceLugar(i + 1);

                entrada.setClienteId(compra.getClienteId());
                entrada.setEventoId(reserva.getEventoId());
                entrada.setSectorId(detalle.getSectorId());

                // Si el sector es numerado, guardar el asiento.
                // Si no es numerado, el asiento queda en null.
                entrada.setAsientoId(
                    numerado ? asientos.get(i) : null
                );

                // Generar un identificador unico para el QR.
                entrada.setCodigoQr(
                    UUID.randomUUID().toString()
                );

                entrada.setEstado("EMITIDA");
                entrada.setFechaEmision(ahora);

                nuevas.add(entrada);
            }
        }

        // Guardar todas las entradas en la base de datos.
        List<Entrada> guardadas =
                entradaRepository.saveAllAndFlush(nuevas);

        return new ResultadoEmisionDTO(
            true,
            convertirTodas(guardadas)
        );
    }

    /*
     * Valida los datos de un detalle de reserva.
     *
     * Controla que tenga una cantidad valida de lugares,
     * que los asientos coincidan con esa cantidad
     * y que no existan lugares liberados.
     */
    private void validarDetalle(
            DetalleReservaDTO detalle,
            Set<Long> idsDetalles) {

        if (detalle == null
                || detalle.getId() == null
                || detalle.getSectorId() == null
                || detalle.getCantidad() == null
                || detalle.getCantidad() < 1
                || !idsDetalles.add(detalle.getId())
                || (detalle.getCantidadLiberada() != null
                    && detalle.getCantidadLiberada() != 0)) {

            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Detalle de reserva invalido o con lugares liberados"
            );
        }

        List<Long> asientos = detalle.getAsientoIds();

        // Si hay asientos, se trata de un sector numerado.
        if (asientos != null && !asientos.isEmpty()) {

            // La cantidad debe coincidir con los asientos.
            if (asientos.size() != detalle.getCantidad()
                    || asientos.stream().anyMatch(
                        id -> id == null || id <= 0
                    )
                    || asientos.stream().distinct().count()
                        != asientos.size()) {

                throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "La cantidad de asientos no coincide con el detalle numerado"
                );
            }
        }
    }

    /*
     * Busca una entrada por su ID.
     *
     * Comprueba que pertenezca al cliente indicado
     * antes de devolver sus datos y codigo QR.
     */
    @Transactional(readOnly = true)
    @Override
    public EntradaDTO obtener(Long id, Long clienteId) {

        Entrada entrada = entradaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Entrada inexistente"
                ));

        if (clienteId == null
                || !entrada.getClienteId().equals(clienteId)) {

            throw new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "La entrada no pertenece al cliente indicado"
            );
        }

        return convertir(entrada);
    }

    /*
     * Busca todas las entradas de un cliente.
     *
     * Devuelve una lista con los datos de las entradas
     * y las imagenes de sus respectivos codigos QR.
     */
    @Transactional(readOnly = true)
    @Override
    public List<EntradaDTO> obtenerPorCliente(Long clienteId) {

        return convertirTodas(
            entradaRepository
                .findByClienteIdOrderByFechaEmisionDescIdDesc(clienteId)
        );
    }

    /*
     * Convierte una lista de entidades Entrada
     * en una lista de objetos EntradaDTO.
     */
    private List<EntradaDTO> convertirTodas(List<Entrada> entradas) {

        return entradas.stream()
                .map(this::convertir)
                .toList();
    }

    /*
     * Convierte una entidad Entrada en un EntradaDTO.
     *
     * Utiliza QrCodeService para generar la imagen
     * del QR a partir del codigo unico almacenado.
     */
    private EntradaDTO convertir(Entrada e) {

        return new EntradaDTO(
            e.getId(),
            e.getCompraId(),
            e.getReservaId(),
            e.getDetalleReservaId(),
            e.getIndiceLugar(),
            e.getClienteId(),
            e.getEventoId(),
            e.getSectorId(),
            e.getAsientoId(),
            e.getCodigoQr(),
            e.getEstado(),
            e.getFechaEmision(),
            qrCodeService.generarImagenBase64(e.getCodigoQr())
        );
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
        }

        if (!entradas.isEmpty()) {
            entradaRepository.saveAll(entradas);
        }

        /*
         * La liberación de reservas confirmadas y la coordinación del
         * reintegro quedan pendientes de la implementación de Compras.
         * No se invocan endpoints de liberación temporal ni se simulan
         * reintegros para mantener esta operación idempotente.
         */
    }

}
