
package com.ticketya.compras.services;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import com.ticketya.compras.clients.ReservaClient;
import com.ticketya.compras.dtos.CompraDTO;
import com.ticketya.compras.dtos.PagoDTO;
import com.ticketya.compras.dtos.ProcesarPagoDTO;
import com.ticketya.compras.dtos.ReservaDTO;
import com.ticketya.compras.entities.Compra;
import com.ticketya.compras.entities.Pago;
import com.ticketya.compras.enums.EstadoCompra;
import com.ticketya.compras.enums.EstadoPago;
import com.ticketya.compras.exceptions.CompraNoEncontradaException;
import com.ticketya.compras.repositories.CompraRepository;
import com.ticketya.compras.repositories.PagoRepository;

@Service
public class PagoServiceImpl implements PagoService {

        private static final Logger log = LoggerFactory.getLogger(PagoServiceImpl.class);

        private final CompraRepository compraRepo;
        private final PagoRepository pagoRepo;
        private final ReservaClient reservaClient;
        private final PasarelaPagoMock pasarela;
        private final CompraService compraService;
        private final TransactionTemplate transacciones;
        private final ReintegroService reintegroService;

        public PagoServiceImpl(
                        CompraRepository compraRepo,
                        PagoRepository pagoRepo,
                        ReservaClient reservaClient,
                        PasarelaPagoMock pasarela,
                        CompraService compraService,
                        ReintegroService reintegroService,
                        PlatformTransactionManager transactionManager) {

                this.compraRepo = compraRepo;
                this.pagoRepo = pagoRepo;
                this.reservaClient = reservaClient;
                this.pasarela = pasarela;
                this.compraService = compraService;
                this.reintegroService = reintegroService;
                this.transacciones = new TransactionTemplate(transactionManager);
        }

        @Override
        public PagoDTO procesarPago(Long compraId, ProcesarPagoDTO solicitud) {

                PagoDTO resultado = transacciones.execute(
                                estado -> registrarPago(compraId, solicitud));

                if (EstadoPago.APROBADO.name().equals(resultado.getEstado())) {

                        try {

                                CompraDTO compraActual = compraService.confirmarCompra(compraId);

                                if (EstadoCompra.COMPENSACION_PENDIENTE.name()
                                                .equals(compraActual.getEstado())) {

                                        reintegroService.compensarCompraSinEntradas(compraId);
                                }

                        } catch (RuntimeException e) {

                                log.warn(
                                                "No se pudo completar la confirmación "
                                                                + "o compensación de la compra {}",
                                                compraId,
                                                e);
                        }

                } else if (EstadoPago.RECHAZADO.name().equals(resultado.getEstado())) {

                        try {

                                reintentarLiberacion(compraId);

                        } catch (RuntimeException e) {

                                log.warn(
                                                "Pago {} rechazado; liberación de compra {} "
                                                                + "pendiente de reintento",
                                                resultado.getId(),
                                                compraId,
                                                e);
                        }
                }

                return resultado;
        }

        private PagoDTO registrarPago(Long compraId, ProcesarPagoDTO solicitud) {

                if (compraId == null || compraId <= 0
                                || solicitud == null
                                || solicitud.getMedioPago() == null
                                || solicitud.getClaveIdempotencia() == null
                                || solicitud.getClaveIdempotencia().isBlank()) {

                        throw new IllegalArgumentException(
                                        "Solicitud de pago inválida");
                }

                String medioPago = solicitud.getMedioPago()
                                .trim()
                                .toUpperCase();

                String clave = solicitud.getClaveIdempotencia().trim();

                Set<String> mediosValidos = Set.of(
                                "TARJETA",
                                "TRANSFERENCIA",
                                "BILLETERA",
                                "TARJETA_RECHAZADA");

                if (!mediosValidos.contains(medioPago)) {
                        throw new IllegalArgumentException(
                                        "Medio de pago inválido");
                }

                if (clave.length() > 160) {
                        throw new IllegalArgumentException(
                                        "La clave de idempotencia es demasiado larga");
                }

                Compra compra = compraRepo.bloquearCompra(compraId)
                                .orElseThrow(() -> new CompraNoEncontradaException(
                                                "La compra no existe"));

                Pago pagoAnterior = pagoRepo
                                .findByClaveIdempotencia(clave)
                                .orElse(null);

                if (pagoAnterior != null) {

                        if (!pagoAnterior.getCompra().getId().equals(compraId)
                                        || !pagoAnterior.getMedioPago().equals(medioPago)
                                        || pagoAnterior.getImporte()
                                                        .compareTo(compra.getTotal()) != 0) {

                                throw new IllegalStateException(
                                                "La clave de idempotencia ya fue utilizada " +
                                                                "con datos diferentes");
                        }

                        if (EstadoPago.PROCESANDO.name()
                                        .equals(pagoAnterior.getEstado())) {

                                throw new IllegalStateException(
                                                "El pago todavía está en procesamiento");
                        }

                        return convertirADTO(pagoAnterior);
                }

                if (!EstadoCompra.PENDIENTE.name()
                                .equals(compra.getEstado())) {

                        throw new IllegalStateException(
                                        "La compra no está pendiente de pago");
                }

                if (compra.getTotal() == null
                                || compra.getTotal()
                                                .compareTo(BigDecimal.ZERO) <= 0) {

                        throw new IllegalStateException(
                                        "El importe de la compra es inválido");
                }

                ReservaDTO reserva = reservaClient.obtenerReserva(
                                compra.getReservaId());

                if (reserva == null
                                || !compra.getReservaId().equals(reserva.getId())
                                || !compra.getClienteId().equals(reserva.getClienteId())
                                || !compra.getEventoId().equals(reserva.getEventoId())) {

                        throw new IllegalStateException(
                                        "Los datos de la reserva no coinciden");
                }

                if (!"ACTIVA".equals(reserva.getEstado())
                                || reserva.getFechaVencimiento() == null
                                || !reserva.getFechaVencimiento()
                                                .isAfter(LocalDateTime.now())) {

                        throw new IllegalStateException(
                                        "La reserva no está activa o ya venció");
                }

                Pago pago = new Pago();

                pago.setCompra(compra);
                pago.setImporte(compra.getTotal());
                pago.setMedioPago(medioPago);
                pago.setClaveIdempotencia(clave);
                pago.setEstado(EstadoPago.PROCESANDO.name());
                pago.setFechaSolicitud(LocalDateTime.now());

                pago = pagoRepo.saveAndFlush(pago);

                PasarelaPagoMock.ResultadoPasarela resultado = pasarela.procesarPago(
                                pago.getImporte(),
                                medioPago,
                                clave);

                pago.setEstado(resultado.estado().name());
                pago.setReferenciaExterna(
                                resultado.referenciaExterna());
                pago.setFechaPago(LocalDateTime.now());

                if (resultado.estado() == EstadoPago.APROBADO) {

                        compra.setEstado(
                                        EstadoCompra.CONFIRMACION_PENDIENTE.name());

                } else if (resultado.estado() == EstadoPago.RECHAZADO) {

                        compra.setEstado(EstadoCompra.LIBERACION_PENDIENTE.name());
                }

                pagoRepo.save(pago);

                return convertirADTO(pago);
        }

        private PagoDTO convertirADTO(Pago pago) {

                PagoDTO dto = new PagoDTO();

                dto.setId(pago.getId());
                dto.setCompraId(pago.getCompra().getId());
                dto.setImporte(pago.getImporte());
                dto.setMedioPago(pago.getMedioPago());
                dto.setEstado(pago.getEstado());
                dto.setFechaSolicitud(pago.getFechaSolicitud());
                dto.setFechaPago(pago.getFechaPago());
                dto.setReferenciaExterna(
                                pago.getReferenciaExterna());

                return dto;
        }

        @Override
        public void reintentarLiberacion(Long compraId) {

                if (compraId == null || compraId <= 0) {
                        throw new IllegalArgumentException("El ID de compra es inválido");
                }
                transacciones.executeWithoutResult(estado -> {
                        Compra compra = compraRepo.bloquearCompra(compraId)
                                        .orElseThrow(() -> new CompraNoEncontradaException("La compra no existe"));

                        if (EstadoCompra.RECHAZADA.name().equals(compra.getEstado())) {
                                return;
                        }
                        if (!EstadoCompra.LIBERACION_PENDIENTE.name().equals(compra.getEstado())) {
                                throw new IllegalStateException(
                                                "La compra no tiene una liberación pendiente");
                        }
                        liberarReservaSiCorresponde(compra);
                });
        }

        private void liberarReservaSiCorresponde(Compra compra) {

                ReservaDTO reserva = reservaClient.obtenerReserva(
                                compra.getReservaId());

                if (reserva == null
                                || !compra.getReservaId().equals(reserva.getId())) {

                        throw new IllegalStateException(
                                        "No se pudo consultar la reserva");
                }

                String estadoReserva = reserva.getEstado();


                if ("LIBERADA".equals(estadoReserva)
                                || "VENCIDA".equals(estadoReserva)
                                || "CANCELADA".equals(estadoReserva)) {

                        compra.setEstado(
                                        EstadoCompra.RECHAZADA.name());

                        return;
                }


                if (!"ACTIVA".equals(estadoReserva)) {

                        throw new IllegalStateException(
                                        "La reserva no se encuentra en un estado liberable");
                }

                // Solicitar la liberación a msvc-reservas.
                ReservaDTO reservaLiberada = reservaClient.liberarReserva(compra.getReservaId());

                if (reservaLiberada == null
                                || !compra.getReservaId().equals(reservaLiberada.getId())
                                || !"LIBERADA".equals(reservaLiberada.getEstado())) {

                        throw new IllegalStateException(
                                        "Reservas no confirmó la liberación");
                }

                compra.setEstado(
                                EstadoCompra.RECHAZADA.name());
        }

}
