
package com.ticketya.compras.services;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import com.ticketya.compras.clients.EntradaClient;
import com.ticketya.compras.clients.EventoClient;
import com.ticketya.compras.dtos.EntradaDTO;
import com.ticketya.compras.dtos.EventoDTO;
import com.ticketya.compras.dtos.ReintegroDTO;
import com.ticketya.compras.dtos.SolicitudReintegroDTO;
import com.ticketya.compras.entities.Compra;
import com.ticketya.compras.entities.ItemCompra;
import com.ticketya.compras.entities.Pago;
import com.ticketya.compras.entities.Reintegro;
import com.ticketya.compras.enums.EstadoCompra;
import com.ticketya.compras.enums.EstadoPago;
import com.ticketya.compras.enums.EstadoReintegro;
import com.ticketya.compras.exceptions.CompraNoEncontradaException;
import com.ticketya.compras.repositories.CompraRepository;
import com.ticketya.compras.repositories.ItemCompraRepository;
import com.ticketya.compras.repositories.PagoRepository;
import com.ticketya.compras.repositories.ReintegroRepository;

@Service
public class ReintegroServiceImpl implements ReintegroService {

    private final CompraRepository compraRepo;
    private final ItemCompraRepository itemRepo;
    private final PagoRepository pagoRepo;
    private final ReintegroRepository reintegroRepo;
    private final EntradaClient entradaClient;
    private final PasarelaPagoMock pasarela;
    private final TransactionTemplate transacciones;
    private final EventoClient eventoClient;

    public ReintegroServiceImpl(
            CompraRepository compraRepo,
            ItemCompraRepository itemRepo,
            PagoRepository pagoRepo,
            ReintegroRepository reintegroRepo,
            EntradaClient entradaClient,
            EventoClient eventoClient,
            PasarelaPagoMock pasarela,
            PlatformTransactionManager transactionManager) {

        this.compraRepo = compraRepo;
        this.itemRepo = itemRepo;
        this.pagoRepo = pagoRepo;
        this.reintegroRepo = reintegroRepo;
        this.entradaClient = entradaClient;
        this.eventoClient = eventoClient;
        this.pasarela = pasarela;
        this.transacciones = new TransactionTemplate(transactionManager);
    }

    @Override
    @Transactional
    public ReintegroDTO reintegrarEntrada(
            SolicitudReintegroDTO solicitud) {

        if (solicitud == null
                || solicitud.getEntradaId() == null
                || solicitud.getEntradaId() <= 0) {
            throw new IllegalArgumentException(
                    "Debe informar una entrada válida");
        }

        Long entradaId = solicitud.getEntradaId();

        ItemCompra item = itemRepo.findByEntradaId(entradaId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "La entrada no pertenece a una compra"));

        Long compraId = item.getDetalleCompra().getCompra().getId();

        Compra compra = compraRepo.bloquearCompra(compraId)
                .orElseThrow(() -> new CompraNoEncontradaException(
                        "La compra no existe"));

        // Idempotencia: si ya se reintegró, devolver el mismo resultado.
        Reintegro anterior = buscarReintegro(item);

        if (anterior != null
                && EstadoReintegro.APROBADO.name()
                        .equals(anterior.getEstado())) {
            return convertirADTO(anterior);
        }

        // Entradas debe haber aprobado previamente la devolución.
        EntradaDTO entrada = entradaClient.obtenerEntrada(entradaId, compra.getClienteId());

        if (entrada == null
                || !entradaId.equals(entrada.getId())
                || (entrada.getCompraId() != null
                        && !compraId.equals(entrada.getCompraId()))
                || (entrada.getItemCompraId() != null
                        && !item.getId().equals(entrada.getItemCompraId()))
                || !("DEVOLUCION_APROBADA".equals(entrada.getEstado())
                        || "INVALIDADA".equals(entrada.getEstado()))) {

            throw new IllegalStateException(
                    "La entrada no tiene una devolución aprobada");
        }

        validarCompraReintegrable(compra);
        Pago pago = buscarPagoAprobado(compra);

        String motivo = solicitud.getMotivo() == null
                || solicitud.getMotivo().isBlank()
                        ? "DEVOLUCION_CLIENTE"
                        : solicitud.getMotivo().trim();

        ReintegroDTO resultado = procesarReintegro(pago, item, motivo);

        actualizarEstadoDeReintegros(compra);

        return resultado;
    }


    @Override
    public void reintegrarEventoCancelado(
            Long eventoId,
            String operacionId) {

        if (eventoId == null || eventoId <= 0
                || operacionId == null
                || operacionId.isBlank()) {

            throw new IllegalArgumentException(
                    "Evento y X-Operacion-Id son obligatorios");
        }

        EventoDTO evento = eventoClient.obtenerEvento(eventoId);

        if (evento == null
                || !eventoId.equals(evento.getId())) {

            throw new IllegalStateException(
                    "No se pudo validar el evento");
        }

        if (!"CANCELADO".equals(evento.getEstado())) {

            throw new IllegalStateException(
                    "No se pueden reintegrar compras "
                            + "de un evento que no está CANCELADO");
        }

        // Primero invalidar entradas y QRs en el servicio dueño de esos datos.
        // Si falla la invalidación, NO se ejecuta el reintegro.
        entradaClient.invalidarPorEvento(eventoId);

        // Obtener las compras asociadas al evento.
        List<Compra> compras = compraRepo.findByEventoId(eventoId);

        // Cada compra se procesa en una transacción independiente.
        // Si una falla, procesar las demás y reportar las pendientes.
        List<Long> pendientes = new ArrayList<>();
        for (Compra compra : compras) {
            Long compraId = compra.getId();
            try {
                transacciones.executeWithoutResult(
                        estado -> reintegrarCompraCancelada(compraId));
            } catch (RuntimeException ex) {
                pendientes.add(compraId);
            }
        }
        if (!pendientes.isEmpty()) {
            throw new IllegalStateException(
                    "Reintegros pendientes de las compras " + pendientes
                            + ". Reintentar la cancelación para recuperarlas");
        }
    }

    private void reintegrarCompraCancelada(Long compraId) {

        Compra compra = compraRepo.bloquearCompra(compraId)
                .orElseThrow(() -> new CompraNoEncontradaException(
                        "La compra no existe"));

        Pago pago = pagoRepo
                .findFirstByCompra_IdAndEstadoOrderByFechaSolicitudDesc(
                        compraId,
                        EstadoPago.APROBADO.name())
                .orElse(null);

        // Si no hubo pago aprobado, no hay dinero que reintegrar.
        if (pago == null) {
            return;
        }

        // Permitimos también compras pagadas cuya emisión quedó pendiente.
        boolean emisionPendiente = EstadoCompra.CONFIRMACION_PENDIENTE.name()
                .equals(compra.getEstado())
                || EstadoCompra.COMPENSACION_PENDIENTE.name()
                        .equals(compra.getEstado());
        if (!emisionPendiente) {
            validarCompraReintegrable(compra);
        }

        List<ItemCompra> items = itemRepo.findByDetalleCompra_Compra_Id(compraId);

        if (items.isEmpty()) {
            throw new IllegalStateException(
                    "La compra pagada no tiene items");
        }

        // Las entradas emitidas deben dejar de ser utilizables ANTES de reintegrar.
        // Entradas debe invalidarlas por cancelación (la coordinación se
        // verifica durante integración, no se asume).
        List<EntradaDTO> entradasRemotas = entradaClient.obtenerPorCliente(compra.getClienteId());
        if (entradasRemotas == null) {
            throw new IllegalStateException("No se pudo consultar el estado de las entradas");
        }
        for (EntradaDTO entrada : entradasRemotas) {
            if (entrada != null && compraId.equals(entrada.getCompraId())
                    && !"INVALIDADA".equals(entrada.getEstado())
                    && !"DEVUELTA".equals(entrada.getEstado())) {
                throw new IllegalStateException("Primero deben invalidarse las entradas del evento");
            }
        }
        for (ItemCompra item : items) {
            procesarReintegro(pago, item, "CANCELACION_EVENTO");
        }

        actualizarEstadoDeReintegros(compra);
    }

    // HISTORIAL DE REINTEGROS
    @Override
    @Transactional(readOnly = true)
    public List<ReintegroDTO> obtenerReintegros(Long compraId) {

        if (compraId == null || compraId <= 0) {
            throw new IllegalArgumentException(
                    "ID de compra inválido");
        }

        if (!compraRepo.existsById(compraId)) {
            throw new CompraNoEncontradaException(
                    "La compra no existe");
        }

        List<ReintegroDTO> resultado = new ArrayList<>();

        for (Reintegro reintegro : reintegroRepo.findByPago_Compra_Id(compraId)) {

            resultado.add(convertirADTO(reintegro));
        }

        return resultado;
    }

    private Pago buscarPagoAprobado(Compra compra) {

        return pagoRepo
                .findFirstByCompra_IdAndEstadoOrderByFechaSolicitudDesc(
                        compra.getId(),
                        EstadoPago.APROBADO.name())
                .orElseThrow(() -> new IllegalStateException(
                        "La compra no tiene un pago aprobado"));
    }

    private void validarCompraReintegrable(Compra compra) {

        String estado = compra.getEstado();

        if (!EstadoCompra.CONFIRMADA.name().equals(estado)
                && !EstadoCompra.REINTEGRO_PARCIAL.name().equals(estado)
                && !EstadoCompra.REINTEGRADA.name().equals(estado)) {

            throw new IllegalStateException(
                    "La compra no está en un estado reintegrable");
        }
    }

    // Buscar por item, conservando compatibilidad con reintegros anteriores.
    private Reintegro buscarReintegro(ItemCompra item) {

        if (item.getId() == null) {
            throw new IllegalStateException(
                    "El item de compra no tiene identificador");
        }

        Reintegro reintegro = reintegroRepo
                .findByItemCompra_Id(item.getId())
                .orElse(null);

        if (reintegro != null) {
            return reintegro;
        }

        // Compatibilidad con registros guardados antes del cambio.
        if (item.getEntradaId() != null) {

            reintegro = reintegroRepo
                    .findByEntradaId(item.getEntradaId())
                    .orElse(null);

            if (reintegro != null) {

                if (reintegro.getItemCompra() != null
                        && !item.getId().equals(
                                reintegro.getItemCompra().getId())) {

                    throw new IllegalStateException(
                            "El reintegro pertenece a otro item");
                }

                reintegro.setItemCompra(item);
            }
        }

        return reintegro;
    }

    // PROCESAR EL REINTEGRO CON LA PASARELA MOCK
    private ReintegroDTO procesarReintegro(
            Pago pago,
            ItemCompra item,
            String motivo) {

        Reintegro reintegro = buscarReintegro(item);

        if (reintegro != null
                && EstadoReintegro.APROBADO.name()
                        .equals(reintegro.getEstado())) {
            return convertirADTO(reintegro);
        }

        BigDecimal precioHistorico = item.getDetalleCompra().getPrecioUnitario();

        if (precioHistorico == null
                || precioHistorico.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalStateException(
                    "El precio histórico del item es inválido");
        }

        if (reintegro == null) {

            reintegro = new Reintegro();

            reintegro.setPago(pago);
            reintegro.setItemCompra(item);
            reintegro.setEntradaId(item.getEntradaId());
            reintegro.setImporte(precioHistorico);
            reintegro.setMotivo(motivo);
            reintegro.setFechaSolicitud(LocalDateTime.now());
        }

        reintegro.setEstado(EstadoReintegro.PROCESANDO.name());
        reintegro = reintegroRepo.saveAndFlush(reintegro);

        PasarelaPagoMock.ResultadoReintegro respuesta;

        // Los items gratuitos no necesitan una devolución bancaria.
        if (reintegro.getImporte().compareTo(BigDecimal.ZERO) == 0) {

            respuesta = new PasarelaPagoMock.ResultadoReintegro(
                    EstadoReintegro.APROBADO,
                    "SIN-COBRO-ITEM-" + item.getId());

        } else {

            respuesta = pasarela.reintegrar(
                    reintegro.getImporte(),
                    pago.getId(),
                    item.getId());
        }

        reintegro.setEstado(respuesta.estado().name());
        reintegro.setReferenciaExterna(
                respuesta.referenciaExterna());

        if (respuesta.estado() == EstadoReintegro.APROBADO) {
            reintegro.setFechaReintegro(LocalDateTime.now());
        }

        reintegroRepo.save(reintegro);

        return convertirADTO(reintegro);
    }

    // ACTUALIZAR ESTADO DE LA COMPRA
    private void actualizarEstadoDeReintegros(Compra compra) {

        List<ItemCompra> items = itemRepo.findByDetalleCompra_Compra_Id(compra.getId());

        int reintegrados = 0;

        for (ItemCompra item : items) {

            Reintegro reintegro = buscarReintegro(item);

            if (reintegro != null
                    && EstadoReintegro.APROBADO.name()
                            .equals(reintegro.getEstado())) {
                reintegrados++;
            }
        }

        if (!items.isEmpty() && reintegrados == items.size()) {

            compra.setEstado(EstadoCompra.REINTEGRADA.name());

        } else if (reintegrados > 0) {

            compra.setEstado(EstadoCompra.REINTEGRO_PARCIAL.name());
        }
    }

    // CONVERSIÓN MANUAL A DTO
    private ReintegroDTO convertirADTO(Reintegro reintegro) {

        return new ReintegroDTO(
                reintegro.getId(),
                reintegro.getPago().getId(),
                reintegro.getPago().getCompra().getId(),
                reintegro.getEntradaId(),
                reintegro.getImporte(),
                reintegro.getEstado(),
                reintegro.getMotivo(),
                reintegro.getFechaSolicitud(),
                reintegro.getFechaReintegro(),
                reintegro.getReferenciaExterna());
    }

    @Override
    @Transactional
    public void compensarCompraSinEntradas(Long compraId) {

        if (compraId == null || compraId <= 0) {
            throw new IllegalArgumentException(
                    "ID de compra inválido");
        }

        Compra compra = compraRepo.bloquearCompra(compraId)
                .orElseThrow(() -> new CompraNoEncontradaException(
                        "La compra no existe"));

        if (EstadoCompra.REINTEGRADA.name()
                .equals(compra.getEstado())) {
            return;
        }

        if (!EstadoCompra.COMPENSACION_PENDIENTE.name()
                .equals(compra.getEstado())) {
            throw new IllegalStateException(
                    "La compra no tiene una compensación pendiente");
        }

        Pago pago = buscarPagoAprobado(compra);

        List<ItemCompra> items = itemRepo.findByDetalleCompra_Compra_Id(compraId);

        if (items.isEmpty()) {
            throw new IllegalStateException(
                    "La compra no tiene items");
        }

        for (ItemCompra item : items) {

            if (item.getEntradaId() != null) {
                throw new IllegalStateException(
                        "La compra tiene entradas emitidas. "
                                + "Requiere conciliación antes del reintegro");
            }
        }

        // Un envío de entradas podría haberse completado aunque Compras no
        // haya recibido su respuesta. Consultar Entradas antes de compensar.
        List<EntradaDTO> emitidasRemotas = entradaClient.obtenerPorCliente(compra.getClienteId());
        if (emitidasRemotas == null) {
            throw new IllegalStateException("No fue posible conciliar la emisión remota");
        }
        for (EntradaDTO remota : emitidasRemotas) {
            if (remota != null && compraId.equals(remota.getCompraId())) {
                throw new IllegalStateException(
                        "Existen entradas emitidas: invalidarlas antes de reintegrar");
            }
        }

        for (ItemCompra item : items) {

            procesarReintegro(
                    pago,
                    item,
                    "RESERVA_NO_CONFIRMADA");
        }

        actualizarEstadoDeReintegros(compra);

        if (!EstadoCompra.REINTEGRADA.name()
                .equals(compra.getEstado())) {

            compra.setEstado(
                    EstadoCompra.COMPENSACION_PENDIENTE.name());
        }
    }
}
