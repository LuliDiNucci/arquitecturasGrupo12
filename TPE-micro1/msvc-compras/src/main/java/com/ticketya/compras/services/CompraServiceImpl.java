
package com.ticketya.compras.services;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ticketya.compras.clients.EntradaClient;
import com.ticketya.compras.clients.EventoClient;
import com.ticketya.compras.clients.ReservaClient;
import com.ticketya.compras.dtos.CompraDTO;
import com.ticketya.compras.dtos.ConfirmacionCompraDTO;
import com.ticketya.compras.dtos.DetalleCompraRespuestaDTO;
import com.ticketya.compras.dtos.DetalleReservaDTO;
import com.ticketya.compras.dtos.EntradaDTO;
import com.ticketya.compras.dtos.EventoDTO;
import com.ticketya.compras.dtos.IniciarCompraDTO;
import com.ticketya.compras.dtos.ItemCompraDTO;
import com.ticketya.compras.dtos.ItemEmisionDTO;
import com.ticketya.compras.dtos.PrecioSectorDTO;
import com.ticketya.compras.dtos.ReservaDTO;
import com.ticketya.compras.dtos.SolicitudEmisionDTO;
import com.ticketya.compras.entities.Compra;
import com.ticketya.compras.entities.DetalleCompra;
import com.ticketya.compras.entities.ItemCompra;
import com.ticketya.compras.enums.EstadoCompra;
import com.ticketya.compras.enums.EstadoPago;
import com.ticketya.compras.exceptions.CompraNoEncontradaException;
import com.ticketya.compras.repositories.CompraRepository;
import com.ticketya.compras.repositories.DetalleCompraRepository;
import com.ticketya.compras.repositories.ItemCompraRepository;
import com.ticketya.compras.repositories.PagoRepository;

@Service
public class CompraServiceImpl implements CompraService {

    private final CompraRepository compraRepo;
    private final DetalleCompraRepository detalleRepo;
    private final ItemCompraRepository itemRepo;
    private final ReservaClient reservaClient;
    private final EventoClient eventoClient;
    private final EntradaClient entradaClient;
    private final PagoRepository pagoRepo;

    public CompraServiceImpl(
            CompraRepository compraRepo,
            DetalleCompraRepository detalleRepo,
            ItemCompraRepository itemRepo,
            ReservaClient reservaClient,
            EventoClient eventoClient,
            EntradaClient entradaClient,
            PagoRepository pagoRepo) {

        this.compraRepo = compraRepo;
        this.detalleRepo = detalleRepo;
        this.itemRepo = itemRepo;
        this.reservaClient = reservaClient;
        this.eventoClient = eventoClient;
        this.entradaClient = entradaClient;
        this.pagoRepo = pagoRepo;
    }

    @Override
    @Transactional
    public CompraDTO iniciarCompra(IniciarCompraDTO solicitud) {

        if (solicitud == null
                || solicitud.getClienteId() == null
                || solicitud.getClienteId() <= 0
                || solicitud.getReservaId() == null
                || solicitud.getReservaId() <= 0) {

            throw new IllegalArgumentException(
                    "Solicitud de compra inválida");
        }

        Long clienteId = solicitud.getClienteId();
        Long reservaId = solicitud.getReservaId();

        if (compraRepo.findByReservaId(reservaId).isPresent()) {
            throw new IllegalStateException(
                    "La reserva ya tiene una compra asociada");
        }

        ReservaDTO reserva = reservaClient.obtenerReserva(reservaId);

        if (reserva == null
                || reserva.getId() == null
                || !reservaId.equals(reserva.getId())) {

            throw new IllegalArgumentException(
                    "La reserva no existe");
        }

        if (!clienteId.equals(reserva.getClienteId())) {
            throw new IllegalStateException(
                    "La reserva no pertenece al cliente");
        }

        if (!"ACTIVA".equals(reserva.getEstado())) {
            throw new IllegalStateException(
                    "La reserva no está activa");
        }

        if (reserva.getFechaVencimiento() == null
                || !reserva.getFechaVencimiento()
                        .isAfter(LocalDateTime.now())) {

            throw new IllegalStateException(
                    "La reserva está vencida");
        }

        if (reserva.getEventoId() == null
                || reserva.getDetalles() == null
                || reserva.getDetalles().isEmpty()) {

            throw new IllegalArgumentException(
                    "La reserva no tiene detalles válidos");
        }

        Compra compra = new Compra();

        compra.setClienteId(clienteId);
        compra.setReservaId(reservaId);
        compra.setEventoId(reserva.getEventoId());
        compra.setFechaCompra(LocalDate.now());
        compra.setEstado(EstadoCompra.PENDIENTE.name());

        BigDecimal total = BigDecimal.ZERO;

        List<DetalleCompra> detalles = new ArrayList<>();

        for (DetalleReservaDTO detalleReserva : reserva.getDetalles()) {

            if (detalleReserva == null
                    || detalleReserva.getSectorId() == null
                    || detalleReserva.getCantidad() == null
                    || detalleReserva.getCantidad() <= 0) {

                throw new IllegalArgumentException(
                        "Detalle de reserva inválido");
            }

            if (detalleReserva.getCantidadLiberada() != null
                    && detalleReserva.getCantidadLiberada() > 0) {

                throw new IllegalStateException(
                        "La reserva tiene entradas liberadas");
            }

            List<Long> asientos = detalleReserva.getAsientoIds();

            if (asientos != null && !asientos.isEmpty()) {

                if (asientos.size() != detalleReserva.getCantidad()
                        || asientos.stream().anyMatch(a -> a == null)
                        || new HashSet<>(asientos).size() != asientos.size()) {

                    throw new IllegalArgumentException(
                            "Los asientos de la reserva son inválidos");
                }
            }

            PrecioSectorDTO precio = eventoClient.obtenerPrecioSector(
                    reserva.getEventoId(),
                    detalleReserva.getSectorId());

            if (precio == null
                    || !reserva.getEventoId().equals(precio.getEventoId())
                    || !detalleReserva.getSectorId().equals(precio.getSectorId())
                    || precio.getPrecio() == null
                    || precio.getPrecio().compareTo(BigDecimal.ZERO) < 0) {

                throw new IllegalStateException(
                        "No se pudo obtener un precio válido");
            }

            BigDecimal precioUnitario = precio.getPrecio();

            BigDecimal subtotal = precioUnitario.multiply(
                    BigDecimal.valueOf(detalleReserva.getCantidad()));

            DetalleCompra detalleCompra = new DetalleCompra();

            detalleCompra.setCompra(compra);
            detalleCompra.setEventoId(reserva.getEventoId());
            detalleCompra.setSectorId(detalleReserva.getSectorId());
            detalleCompra.setCantidad(detalleReserva.getCantidad());
            detalleCompra.setPrecioUnitario(precioUnitario);
            detalleCompra.setSubtotal(subtotal);

            for (int i = 0; i < detalleReserva.getCantidad(); i++) {

                ItemCompra item = new ItemCompra();

                if (asientos != null && !asientos.isEmpty()) {
                    item.setAsientoId(asientos.get(i));
                }

                detalleCompra.agregarItem(item);
            }

            detalles.add(detalleCompra);

            total = total.add(subtotal);
        }

        compra.setTotal(total);

        compra = compraRepo.saveAndFlush(compra);

        for (DetalleCompra detalle : detalles) {

            detalle.setCompra(compra);

            detalle = detalleRepo.saveAndFlush(detalle);

            compra.getDetalles().add(detalle);

        }

        return convertirADTO(compra);
    }

    @Override
    @Transactional(readOnly = true)
    public CompraDTO obtenerCompra(Long id) {

        Compra compra = compraRepo.findById(id)
                .orElseThrow(() -> new CompraNoEncontradaException(
                        "La compra no existe"));

        return convertirADTO(compra);
    }

    @Override
    @Transactional
    public CompraDTO confirmarCompra(Long compraId) {

        if (compraId == null || compraId <= 0) {
            throw new IllegalArgumentException("ID de compra inválido");
        }

        Compra compra = compraRepo.bloquearCompra(compraId)
                .orElseThrow(() -> new CompraNoEncontradaException("La compra no existe"));

        // Un reintento nunca debe volver a emitir entradas de una compra completada.
        if (EstadoCompra.CONFIRMADA.name().equals(compra.getEstado())
                || EstadoCompra.REINTEGRO_PARCIAL.name().equals(compra.getEstado())
                || EstadoCompra.REINTEGRADA.name().equals(compra.getEstado())
                || EstadoCompra.COMPENSACION_PENDIENTE.name()
                        .equals(compra.getEstado())) {

            return convertirADTO(compra);
        }

        if (!EstadoCompra.CONFIRMACION_PENDIENTE.name().equals(compra.getEstado())) {
            throw new IllegalStateException("La compra no tiene una confirmación pendiente");
        }

        if (pagoRepo.findFirstByCompra_IdAndEstadoOrderByFechaSolicitudDesc(
                compraId, EstadoPago.APROBADO.name()).isEmpty()) {
            throw new IllegalStateException("La compra no tiene un pago aprobado");
        }

        // Verificar que el evento siga habilitado para vender entradas.
        EventoDTO evento = eventoClient.obtenerEvento(
                compra.getEventoId());

        if (evento == null
                || !compra.getEventoId().equals(evento.getId())
                || evento.getEstado() == null) {

            throw new IllegalStateException(
                    "No se pudo verificar el estado del evento");
        }

        if ("CANCELADO".equals(evento.getEstado())) {
            // No se emiten nuevas entradas para un evento cancelado.
            // La compensación verifica además la ausencia de emisiones remotas.
            compra.setEstado(EstadoCompra.COMPENSACION_PENDIENTE.name());
            return convertirADTO(compra);
        }

        if (!"PUBLICADO".equals(evento.getEstado())
                && !"AGOTADO".equals(evento.getEstado())) {

            throw new IllegalStateException(
                    "El evento no se encuentra habilitado para confirmar entradas");
        }

        ReservaDTO reserva = reservaClient.obtenerReserva(compra.getReservaId());
        if (reserva == null || !compra.getReservaId().equals(reserva.getId())
                || !compra.getClienteId().equals(reserva.getClienteId())
                || !compra.getEventoId().equals(reserva.getEventoId())) {
            throw new IllegalStateException("Los datos de la reserva no coinciden");
        }

        // Si la reserva ya no puede confirmarse, debemos compensar el pago.
        if ("VENCIDA".equals(reserva.getEstado())
                || "LIBERADA".equals(reserva.getEstado())
                || "CANCELADA".equals(reserva.getEstado())) {

            compra.setEstado(
                    EstadoCompra.COMPENSACION_PENDIENTE.name());

            return convertirADTO(compra);
        }

        if ("ACTIVA".equals(reserva.getEstado())) {

            if (reserva.getFechaVencimiento() == null
                    || !reserva.getFechaVencimiento()
                            .isAfter(LocalDateTime.now())) {

                compra.setEstado(
                        EstadoCompra.COMPENSACION_PENDIENTE.name());

                return convertirADTO(compra);
            }

            reserva = reservaClient.confirmarReserva(
                    compra.getReservaId());
        }

        if (reserva == null || !"CONFIRMADA".equals(reserva.getEstado())) {
            throw new IllegalStateException("Reservas no confirmó los lugares");
        }

        List<ItemCompra> items = itemRepo.findByDetalleCompra_Compra_Id(compraId);
        if (items.isEmpty()) {
            throw new IllegalStateException("La compra no tiene items para emitir");
        }

        Map<Long, ItemCompra> itemsPorId = new HashMap<>();
        List<ItemEmisionDTO> pedidos = new ArrayList<>();
        for (ItemCompra item : items) {
            if (item.getId() == null) {
                throw new IllegalStateException("Item sin identificador");
            }
            itemsPorId.put(item.getId(), item);
            pedidos.add(new ItemEmisionDTO(
                    item.getId(), item.getDetalleCompra().getSectorId(), item.getAsientoId()));
        }

        SolicitudEmisionDTO solicitud = new SolicitudEmisionDTO(
                compraId,
                compra.getClienteId(),
                compra.getEventoId(),
                "EMISION-COMPRA-" + compraId,
                pedidos);

        List<EntradaDTO> emitidas = entradaClient.emitirEntradas(solicitud);
        if (emitidas == null || emitidas.size() != items.size()) {
            throw new IllegalStateException("Entradas no devolvió todas las entradas esperadas");
        }

        Set<Long> idsEntrada = new HashSet<>();
        Set<Long> idsItem = new HashSet<>();
        Set<String> codigosQR = new HashSet<>();
        for (EntradaDTO entrada : emitidas) {
            if (entrada == null || entrada.getId() == null
                    || entrada.getId() <= 0
                    || !idsEntrada.add(entrada.getId())
                    || (entrada.getCompraId() != null
                            && !compraId.equals(entrada.getCompraId()))
                    || entrada.getCodigoQR() == null
                    || entrada.getCodigoQR().isBlank()
                    || !codigosQR.add(entrada.getCodigoQR())
                    || !"EMITIDA".equals(entrada.getEstado())) {
                throw new IllegalStateException(
                        "Entradas devolvió datos incompletos, duplicados o ajenos a la compra");
            }

            // Entradas actual devuelve sector/asiento en lugar de itemCompraId.
            // Soportamos ambos formatos sin cambiar el modelo de Compra.
            ItemCompra item = entrada.getItemCompraId() != null
                    ? itemsPorId.get(entrada.getItemCompraId())
                    : localizarItemEmitido(items, idsItem, entrada);
            if (item == null || !idsItem.add(item.getId())) {
                throw new IllegalStateException("No se pudo asociar la entrada a un item único");
            }
            if (item.getEntradaId() != null
                    && !item.getEntradaId().equals(entrada.getId())) {
                throw new IllegalStateException("Una entrada emitida cambió de identificador");
            }
            item.setEntradaId(entrada.getId());
        }

        compra.setEstado(EstadoCompra.CONFIRMADA.name());
        return convertirADTO(compra);
    }

    @Override
    @Transactional(readOnly = true)
    public ConfirmacionCompraDTO consultarConfirmacion(Long compraId) {
        Compra compra = compraRepo.findById(compraId)
                .orElseThrow(() -> new CompraNoEncontradaException("La compra no existe"));
        boolean pagoAprobado = pagoRepo.findFirstByCompra_IdAndEstadoOrderByFechaSolicitudDesc(
                compraId, EstadoPago.APROBADO.name()).isPresent();
        return new ConfirmacionCompraDTO(compra.getId(), compra.getClienteId(),
                compra.getReservaId(), compra.getEstado(), pagoAprobado);
    }

    private ItemCompra localizarItemEmitido(
            List<ItemCompra> items, Set<Long> asignados, EntradaDTO entrada) {
        for (ItemCompra item : items) {
            if (asignados.contains(item.getId())) {
                continue;
            }
            if (entrada.getSectorId() != null
                    && entrada.getSectorId().equals(item.getDetalleCompra().getSectorId())
                    && java.util.Objects.equals(entrada.getAsientoId(), item.getAsientoId())) {
                return item;
            }
        }
        return null;
    }

    private CompraDTO convertirADTO(Compra compra) {

        List<DetalleCompraRespuestaDTO> detallesDTO = new ArrayList<>();

        for (DetalleCompra detalle : compra.getDetalles()) {

            List<ItemCompraDTO> itemsDTO = new ArrayList<>();

            for (ItemCompra item : detalle.getItems()) {

                itemsDTO.add(new ItemCompraDTO(
                        item.getId(),
                        item.getAsientoId(),
                        item.getEntradaId()));
            }

            detallesDTO.add(new DetalleCompraRespuestaDTO(
                    detalle.getId(),
                    detalle.getEventoId(),
                    detalle.getSectorId(),
                    detalle.getCantidad(),
                    detalle.getPrecioUnitario(),
                    detalle.getSubtotal(),
                    itemsDTO));
        }

        return new CompraDTO(
                compra.getId(),
                compra.getClienteId(),
                compra.getReservaId(),
                compra.getEventoId(),
                compra.getFechaCompra(),
                compra.getTotal(),
                compra.getEstado(),
                detallesDTO);
    }
}
