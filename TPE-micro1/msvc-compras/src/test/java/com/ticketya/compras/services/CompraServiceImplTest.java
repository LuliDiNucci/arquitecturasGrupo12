package com.ticketya.compras.services;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.ticketya.compras.clients.EntradaClient;
import com.ticketya.compras.clients.EventoClient;
import com.ticketya.compras.clients.ReservaClient;
import com.ticketya.compras.dtos.DetalleReservaDTO;
import com.ticketya.compras.dtos.EntradaDTO;
import com.ticketya.compras.dtos.EventoDTO;
import com.ticketya.compras.dtos.IniciarCompraDTO;
import com.ticketya.compras.dtos.PrecioSectorDTO;
import com.ticketya.compras.dtos.ReservaDTO;
import com.ticketya.compras.dtos.SolicitudEmisionDTO;
import com.ticketya.compras.entities.Compra;
import com.ticketya.compras.entities.DetalleCompra;
import com.ticketya.compras.entities.ItemCompra;
import com.ticketya.compras.entities.Pago;
import com.ticketya.compras.enums.EstadoCompra;
import com.ticketya.compras.enums.EstadoPago;
import com.ticketya.compras.repositories.CompraRepository;
import com.ticketya.compras.repositories.DetalleCompraRepository;
import com.ticketya.compras.repositories.ItemCompraRepository;
import com.ticketya.compras.repositories.PagoRepository;

class CompraServiceImplTest {

        @Test
        void confirmarCompraSoloCuandoEstanTodasLasEntradas() {
                CompraRepository compraRepo = mock(CompraRepository.class);
                DetalleCompraRepository detalleRepo = mock(DetalleCompraRepository.class);
                ItemCompraRepository itemRepo = mock(ItemCompraRepository.class);
                ReservaClient reservas = mock(ReservaClient.class);
                EventoClient eventos = mock(EventoClient.class);
                EntradaClient entradas = mock(EntradaClient.class);
                PagoRepository pagos = mock(PagoRepository.class);

                Compra compra = new Compra();
                compra.setId(5L);
                compra.setReservaId(8L);
                compra.setClienteId(12L);
                compra.setEventoId(21L);
                compra.setEstado(EstadoCompra.CONFIRMACION_PENDIENTE.name());

                DetalleCompra detalle = new DetalleCompra();
                detalle.setId(22L);
                detalle.setSectorId(3L);
                detalle.setCompra(compra);
                ItemCompra item = new ItemCompra();
                item.setId(99L);
                detalle.agregarItem(item);
                compra.getDetalles().add(detalle);

                Pago pago = new Pago();
                pago.setEstado(EstadoPago.APROBADO.name());

                ReservaDTO reserva = new ReservaDTO();
                reserva.setId(8L);
                reserva.setClienteId(12L);
                reserva.setEventoId(21L);
                reserva.setEstado("ACTIVA");
                reserva.setFechaVencimiento(LocalDateTime.now().plusMinutes(5));

                ReservaDTO confirmada = new ReservaDTO();
                confirmada.setId(8L);
                confirmada.setClienteId(12L);
                confirmada.setEventoId(21L);
                confirmada.setEstado("CONFIRMADA");

                EntradaDTO emitida = new EntradaDTO();
                emitida.setId(100L);
                emitida.setItemCompraId(99L);
                emitida.setCompraId(5L);
                emitida.setCodigoQR("QR-100-UNICO");
                emitida.setEstado("EMITIDA");

                when(compraRepo.bloquearCompra(5L)).thenReturn(Optional.of(compra));
                when(pagos.findFirstByCompra_IdAndEstadoOrderByFechaSolicitudDesc(
                                5L, EstadoPago.APROBADO.name())).thenReturn(Optional.of(pago));
                when(reservas.obtenerReserva(8L)).thenReturn(reserva);
                when(reservas.confirmarReserva(8L)).thenReturn(confirmada);
                when(itemRepo.findByDetalleCompra_Compra_Id(5L)).thenReturn(List.of(item));
                when(entradas.emitirEntradas(any(SolicitudEmisionDTO.class)))
                                .thenReturn(List.of(emitida));

                EventoDTO evento = new EventoDTO();
                evento.setId(21L);
                evento.setEstado("PUBLICADO");

                when(eventos.obtenerEvento(21L))
                                .thenReturn(evento);

                CompraServiceImpl service = new CompraServiceImpl(
                                compraRepo, detalleRepo, itemRepo, reservas, eventos, entradas, pagos);

                var resultado = service.confirmarCompra(5L);

                assertEquals("CONFIRMADA", resultado.getEstado());
                assertEquals(Long.valueOf(100L), item.getEntradaId());

                ArgumentCaptor<SolicitudEmisionDTO> captor = ArgumentCaptor.forClass(SolicitudEmisionDTO.class);
                verify(entradas).emitirEntradas(captor.capture());
                assertEquals("EMISION-COMPRA-5", captor.getValue().getClaveIdempotencia());

                // Repetición idempotente: no volver a confirmar ni emitir.
                service.confirmarCompra(5L);
                verify(entradas, times(1)).emitirEntradas(any(SolicitudEmisionDTO.class));
        }

        @Test
        void iniciarCompraCalculaTotalYRegistraItems() {

                CompraRepository compraRepo = mock(CompraRepository.class);
                DetalleCompraRepository detalleRepo = mock(DetalleCompraRepository.class);
                ItemCompraRepository itemRepo = mock(ItemCompraRepository.class);
                ReservaClient reservas = mock(ReservaClient.class);
                EventoClient eventos = mock(EventoClient.class);
                EntradaClient entradas = mock(EntradaClient.class);
                PagoRepository pagos = mock(PagoRepository.class);

                // Reserva de 3 entradas en dos sectores.
                ReservaDTO reserva = new ReservaDTO();
                reserva.setId(10L);
                reserva.setClienteId(200L);
                reserva.setEventoId(21L);
                reserva.setEstado("ACTIVA");
                reserva.setFechaVencimiento(
                                LocalDateTime.now().plusMinutes(10));

                DetalleReservaDTO sector5 = new DetalleReservaDTO();
                sector5.setSectorId(5L);
                sector5.setCantidad(2);
                sector5.setAsientoIds(List.of(25L, 26L));

                DetalleReservaDTO sector6 = new DetalleReservaDTO();
                sector6.setSectorId(6L);
                sector6.setCantidad(1);
                sector6.setAsientoIds(List.of());

                reserva.setDetalles(List.of(sector5, sector6));

                when(compraRepo.findByReservaId(10L))
                                .thenReturn(Optional.empty());

                when(reservas.obtenerReserva(10L))
                                .thenReturn(reserva);

                // Precio sector 5: $15.000
                PrecioSectorDTO precio5 = new PrecioSectorDTO();
                precio5.setEventoId(21L);
                precio5.setSectorId(5L);
                precio5.setPrecio(new BigDecimal("15000.00"));

                // Precio sector 6: $10.000
                PrecioSectorDTO precio6 = new PrecioSectorDTO();
                precio6.setEventoId(21L);
                precio6.setSectorId(6L);
                precio6.setPrecio(new BigDecimal("10000.00"));

                when(eventos.obtenerPrecioSector(21L, 5L))
                                .thenReturn(precio5);

                when(eventos.obtenerPrecioSector(21L, 6L))
                                .thenReturn(precio6);

                // Simular el guardado en la base de datos.
                when(compraRepo.saveAndFlush(any(Compra.class)))
                                .thenAnswer(inv -> {
                                        Compra compra = inv.getArgument(0);
                                        compra.setId(1L);
                                        return compra;
                                });

                when(detalleRepo.saveAndFlush(any(DetalleCompra.class)))
                                .thenAnswer(inv -> {
                                        DetalleCompra detalle = inv.getArgument(0);
                                        return detalle;
                                });

                CompraServiceImpl service = new CompraServiceImpl(
                                compraRepo, detalleRepo, itemRepo,
                                reservas, eventos, entradas, pagos);

                IniciarCompraDTO solicitud = new IniciarCompraDTO();
                solicitud.setClienteId(200L);
                solicitud.setReservaId(10L);

                var resultado = service.iniciarCompra(solicitud);

                // 2 x 15.000 + 1 x 10.000 = 40.000
                assertEquals(
                                0,
                                new BigDecimal("40000.00")
                                                .compareTo(resultado.getTotal()));

                assertEquals("PENDIENTE", resultado.getEstado());
                assertEquals(2, resultado.getDetalles().size());

                // Verificar cantidad de items individuales.
                int cantidadItems = 0;

                for (var detalle : resultado.getDetalles()) {
                        cantidadItems += detalle.getItems().size();
                }

                assertEquals(3, cantidadItems);

                // Los precios deben consultarse al servicio Eventos.
                verify(eventos).obtenerPrecioSector(21L, 5L);
                verify(eventos).obtenerPrecioSector(21L, 6L);

                verify(compraRepo, times(1))
                                .saveAndFlush(any(Compra.class));

                verify(detalleRepo, times(2))
                                .saveAndFlush(any(DetalleCompra.class));
        }

        @Test
        void noPermitirCompraDuplicadaPorReserva() {

                CompraRepository compraRepo = mock(CompraRepository.class);
                DetalleCompraRepository detalleRepo = mock(DetalleCompraRepository.class);
                ItemCompraRepository itemRepo = mock(ItemCompraRepository.class);
                ReservaClient reservas = mock(ReservaClient.class);
                EventoClient eventos = mock(EventoClient.class);
                EntradaClient entradas = mock(EntradaClient.class);
                PagoRepository pagos = mock(PagoRepository.class);

                Compra compraExistente = new Compra();
                compraExistente.setId(1L);
                compraExistente.setReservaId(10L);
                compraExistente.setEstado("PENDIENTE");

                when(compraRepo.findByReservaId(10L))
                                .thenReturn(Optional.of(compraExistente));

                CompraServiceImpl service = new CompraServiceImpl(
                                compraRepo, detalleRepo, itemRepo,
                                reservas, eventos, entradas, pagos);

                IniciarCompraDTO solicitud = new IniciarCompraDTO();
                solicitud.setClienteId(200L);
                solicitud.setReservaId(10L);

                try {

                        service.iniciarCompra(solicitud);

                        fail("No debería crear otra compra para la misma reserva");

                } catch (IllegalStateException e) {

                        assertTrue(
                                        e.getMessage().contains("ya tiene una compra"));
                }

                // No debemos consultar servicios externos
                // ni registrar una segunda compra.
                verifyNoInteractions(reservas, eventos);

                verify(compraRepo, never())
                                .saveAndFlush(any(Compra.class));
        }

}
