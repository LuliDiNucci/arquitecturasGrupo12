
package com.ticketya.compras.services;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.SimpleTransactionStatus;

import com.ticketya.compras.clients.EntradaClient;
import com.ticketya.compras.clients.EventoClient;
import com.ticketya.compras.dtos.EntradaDTO;
import com.ticketya.compras.dtos.EventoDTO;
import com.ticketya.compras.dtos.ReintegroDTO;
import com.ticketya.compras.dtos.SolicitudReintegroDTO;
import com.ticketya.compras.entities.Compra;
import com.ticketya.compras.entities.DetalleCompra;
import com.ticketya.compras.entities.ItemCompra;
import com.ticketya.compras.entities.Pago;
import com.ticketya.compras.entities.Reintegro;
import com.ticketya.compras.enums.EstadoCompra;
import com.ticketya.compras.enums.EstadoPago;
import com.ticketya.compras.enums.EstadoReintegro;
import com.ticketya.compras.repositories.CompraRepository;
import com.ticketya.compras.repositories.ItemCompraRepository;
import com.ticketya.compras.repositories.PagoRepository;
import com.ticketya.compras.repositories.ReintegroRepository;

class ReintegroServiceImplTest {

        @Test
        void devolucionIndividualUsaPrecioCongeladoYEvitaduplicados() {

                CompraRepository compras = mock(CompraRepository.class);
                ItemCompraRepository items = mock(ItemCompraRepository.class);
                PagoRepository pagos = mock(PagoRepository.class);
                ReintegroRepository reintegros = mock(ReintegroRepository.class);
                EntradaClient entradas = mock(EntradaClient.class);
                PasarelaPagoMock pasarela = new PasarelaPagoMock();

                Compra compra = new Compra();
                compra.setId(1L);
        compra.setClienteId(1L);
                compra.setEstado(EstadoCompra.CONFIRMADA.name());

                DetalleCompra detalle = new DetalleCompra();
                detalle.setCompra(compra);
                detalle.setPrecioUnitario(new BigDecimal("15000.00"));

                ItemCompra item = new ItemCompra();
                item.setId(13L);
                item.setEntradaId(100L);
                detalle.agregarItem(item);

                Pago pago = new Pago();
                pago.setId(9L);
                pago.setEstado(EstadoPago.APROBADO.name());
                pago.setCompra(compra);

                EntradaDTO entrada = new EntradaDTO();
                entrada.setId(100L);
                entrada.setItemCompraId(13L);
                entrada.setCompraId(1L);
                entrada.setEstado("DEVOLUCION_APROBADA");

                when(items.findByEntradaId(100L))
                                .thenReturn(Optional.of(item));

                when(compras.bloquearCompra(1L))
                                .thenReturn(Optional.of(compra));

                when(entradas.obtenerEntrada(100L, 1L))
                                .thenReturn(entrada);

                when(pagos.findFirstByCompra_IdAndEstadoOrderByFechaSolicitudDesc(
                                1L, "APROBADO"))
                                .thenReturn(Optional.of(pago));

                when(items.findByDetalleCompra_Compra_Id(1L))
                                .thenReturn(List.of(item));

                final Reintegro[] creado = new Reintegro[1];

                when(reintegros.findByEntradaId(100L))
                                .thenAnswer(inv -> Optional.ofNullable(creado[0]));

                when(reintegros.saveAndFlush(any(Reintegro.class)))
                                .thenAnswer(inv -> {
                                        Reintegro nuevo = inv.getArgument(0);
                                        nuevo.setId(77L);
                                        creado[0] = nuevo;
                                        return nuevo;
                                });

                ReintegroServiceImpl service = new ReintegroServiceImpl(
                                compras,
                                items,
                                pagos,
                                reintegros,
                                entradas,
                                crearEventoCanceladoMock(),
                                pasarela,
                                crearGestorTransacciones());

                SolicitudReintegroDTO solicitud = new SolicitudReintegroDTO();
                solicitud.setEntradaId(100L);
                solicitud.setMotivo("DEVOLUCION_CLIENTE");

                ReintegroDTO primerResultado = service.reintegrarEntrada(solicitud);

                assertEquals(
                                new BigDecimal("15000.00"),
                                primerResultado.getImporte());

                assertEquals(
                                EstadoReintegro.APROBADO.name(),
                                primerResultado.getEstado());

                assertEquals(
                                EstadoCompra.REINTEGRADA.name(),
                                compra.getEstado());

                ReintegroDTO segundoResultado = service.reintegrarEntrada(solicitud);

                assertEquals(
                                primerResultado.getId(),
                                segundoResultado.getId());

                verify(reintegros, times(1))
                                .saveAndFlush(any(Reintegro.class));

                verify(entradas, times(1))
                                .obtenerEntrada(100L, 1L);
        }

        @Test
        void cancelarEventoDosVecesNoDuplicaReintegros() {

                CompraRepository compras = mock(CompraRepository.class);
                ItemCompraRepository items = mock(ItemCompraRepository.class);
                PagoRepository pagos = mock(PagoRepository.class);
                ReintegroRepository reintegros = mock(ReintegroRepository.class);
                EntradaClient entradas = mock(EntradaClient.class);

                Compra compra = new Compra();
                compra.setId(2L);
        compra.setClienteId(1L);
                compra.setEventoId(21L);
                compra.setEstado(EstadoCompra.CONFIRMADA.name());

                DetalleCompra detalle = new DetalleCompra();
                detalle.setCompra(compra);
                detalle.setPrecioUnitario(new BigDecimal("7500.00"));

                ItemCompra item = new ItemCompra();
                item.setId(50L);
                item.setEntradaId(200L);
                detalle.agregarItem(item);

                Pago pago = new Pago();
                pago.setId(11L);
                pago.setCompra(compra);
                pago.setEstado(EstadoPago.APROBADO.name());

                when(compras.findByEventoId(21L))
                                .thenReturn(List.of(compra));

                when(compras.bloquearCompra(2L))
                                .thenReturn(Optional.of(compra));

                when(pagos.findFirstByCompra_IdAndEstadoOrderByFechaSolicitudDesc(
                                2L, "APROBADO"))
                                .thenReturn(Optional.of(pago));

                when(items.findByDetalleCompra_Compra_Id(2L))
                                .thenReturn(List.of(item));

                final Reintegro[] creado = new Reintegro[1];

                when(reintegros.findByEntradaId(200L))
                                .thenAnswer(inv -> Optional.ofNullable(creado[0]));

                when(reintegros.saveAndFlush(any(Reintegro.class)))
                                .thenAnswer(inv -> {
                                        Reintegro nuevo = inv.getArgument(0);
                                        nuevo.setId(80L);
                                        creado[0] = nuevo;
                                        return nuevo;
                                });

                ReintegroServiceImpl service = new ReintegroServiceImpl(
                                compras,
                                items,
                                pagos,
                                reintegros,
                                entradas,
                                crearEventoCanceladoMock(),
                                new PasarelaPagoMock(),
                                crearGestorTransacciones());

                service.reintegrarEventoCancelado(
                                21L, "CANCELACION-UNO");

                service.reintegrarEventoCancelado(
                                21L, "CANCELACION-UNO");

                assertEquals(
                                EstadoCompra.REINTEGRADA.name(),
                                compra.getEstado());

                verify(reintegros, times(1))
                                .saveAndFlush(any(Reintegro.class));

                verify(entradas, times(2)).obtenerPorCliente(1L);
        }

        @Test
        void devolverUnaDeDosEntradasDejaReintegroParcial() {

                CompraRepository compras = mock(CompraRepository.class);
                ItemCompraRepository items = mock(ItemCompraRepository.class);
                PagoRepository pagos = mock(PagoRepository.class);
                ReintegroRepository reintegros = mock(ReintegroRepository.class);
                EntradaClient entradas = mock(EntradaClient.class);

                Compra compra = new Compra();
                compra.setId(1L);
        compra.setClienteId(1L);
                compra.setEstado(EstadoCompra.CONFIRMADA.name());

                DetalleCompra detalle = new DetalleCompra();
                detalle.setCompra(compra);
                detalle.setPrecioUnitario(new BigDecimal("15000.00"));

                ItemCompra item1 = new ItemCompra();
                item1.setId(101L);
                item1.setEntradaId(201L);
                detalle.agregarItem(item1);

                ItemCompra item2 = new ItemCompra();
                item2.setId(102L);
                item2.setEntradaId(202L);
                detalle.agregarItem(item2);

                Pago pago = new Pago();
                pago.setId(10L);
                pago.setCompra(compra);
                pago.setEstado(EstadoPago.APROBADO.name());

                EntradaDTO entrada = new EntradaDTO();
                entrada.setId(201L);
                entrada.setCompraId(1L);
                entrada.setItemCompraId(101L);
                entrada.setEstado("DEVOLUCION_APROBADA");

                when(items.findByEntradaId(201L))
                                .thenReturn(Optional.of(item1));

                when(compras.bloquearCompra(1L))
                                .thenReturn(Optional.of(compra));

                when(entradas.obtenerEntrada(201L, 1L))
                                .thenReturn(entrada);

                when(pagos.findFirstByCompra_IdAndEstadoOrderByFechaSolicitudDesc(
                                1L, "APROBADO"))
                                .thenReturn(Optional.of(pago));

                when(items.findByDetalleCompra_Compra_Id(1L))
                                .thenReturn(List.of(item1, item2));

                final Reintegro[] creado = { null };

                when(reintegros.findByItemCompra_Id(101L))
                                .thenAnswer(inv -> Optional.ofNullable(creado[0]));

                when(reintegros.saveAndFlush(any(Reintegro.class)))
                                .thenAnswer(inv -> {
                                        Reintegro r = inv.getArgument(0);
                                        r.setId(1L);
                                        creado[0] = r;
                                        return r;
                                });

                ReintegroServiceImpl service = new ReintegroServiceImpl(
                                compras,
                                items,
                                pagos,
                                reintegros,
                                entradas,
                                crearEventoCanceladoMock(),
                                new PasarelaPagoMock(),
                                crearGestorTransacciones());

                SolicitudReintegroDTO solicitud = new SolicitudReintegroDTO();
                solicitud.setEntradaId(201L);
                solicitud.setMotivo("DEVOLUCION_CLIENTE");

                ReintegroDTO resultado = service.reintegrarEntrada(solicitud);

                assertEquals(
                                0,
                                new BigDecimal("15000.00")
                                                .compareTo(resultado.getImporte()));

                assertEquals(
                                EstadoReintegro.APROBADO.name(),
                                resultado.getEstado());

                assertEquals(
                                EstadoCompra.REINTEGRO_PARCIAL.name(),
                                compra.getEstado());

                verify(reintegros, times(1))
                                .saveAndFlush(any(Reintegro.class));

                verify(entradas, times(1))
                                .obtenerEntrada(201L, 1L);
        }

        @Test
        void cancelarEventoReintegraCompraSinEntradasEmitidas() {

                CompraRepository compras = mock(CompraRepository.class);
                ItemCompraRepository items = mock(ItemCompraRepository.class);
                PagoRepository pagos = mock(PagoRepository.class);
                ReintegroRepository reintegros = mock(ReintegroRepository.class);
                EntradaClient entradas = mock(EntradaClient.class);

                Compra compra = new Compra();
                compra.setId(2L);
        compra.setClienteId(1L);
                compra.setEventoId(21L);
                compra.setEstado(
                                EstadoCompra.CONFIRMACION_PENDIENTE.name());

                DetalleCompra detalle = new DetalleCompra();
                detalle.setCompra(compra);
                detalle.setPrecioUnitario(new BigDecimal("20000.00"));

                ItemCompra item = new ItemCompra();
                item.setId(60L);
                item.setEntradaId(null);
                detalle.agregarItem(item);

                Pago pago = new Pago();
                pago.setId(11L);
                pago.setCompra(compra);
                pago.setEstado(EstadoPago.APROBADO.name());

                when(compras.findByEventoId(21L))
                                .thenReturn(List.of(compra));

                when(compras.bloquearCompra(2L))
                                .thenReturn(Optional.of(compra));

                when(pagos.findFirstByCompra_IdAndEstadoOrderByFechaSolicitudDesc(
                                2L, "APROBADO"))
                                .thenReturn(Optional.of(pago));

                when(items.findByDetalleCompra_Compra_Id(2L))
                                .thenReturn(List.of(item));

                final Reintegro[] creado = { null };

                when(reintegros.findByItemCompra_Id(60L))
                                .thenAnswer(inv -> Optional.ofNullable(creado[0]));

                when(reintegros.saveAndFlush(any(Reintegro.class)))
                                .thenAnswer(inv -> {
                                        Reintegro r = inv.getArgument(0);
                                        r.setId(90L);
                                        creado[0] = r;
                                        return r;
                                });

                ReintegroServiceImpl service = new ReintegroServiceImpl(
                                compras,
                                items,
                                pagos,
                                reintegros,
                                entradas,
                                crearEventoCanceladoMock(),
                                new PasarelaPagoMock(),
                                crearGestorTransacciones());

                service.reintegrarEventoCancelado(
                                21L, "CANCELACION-21");

                assertEquals(
                                EstadoCompra.REINTEGRADA.name(),
                                compra.getEstado());

                assertEquals(
                                EstadoReintegro.APROBADO.name(),
                                creado[0].getEstado());

                assertEquals(
                                new BigDecimal("20000.00"),
                                creado[0].getImporte());

                assertEquals(
                                60L,
                                creado[0].getItemCompra().getId());

                // Repetir la cancelación no debe crear otro reintegro.
                service.reintegrarEventoCancelado(
                                21L, "CANCELACION-21");

                verify(reintegros, times(1))
                                .saveAndFlush(any(Reintegro.class));

                verify(entradas, times(2)).obtenerPorCliente(1L);
        }

        // Mock de un evento cancelado para las pruebas de US08.
        private EventoClient crearEventoCanceladoMock() {

                EventoClient eventos = mock(EventoClient.class);

                EventoDTO evento = new EventoDTO();
                evento.setId(21L);
                evento.setEstado("CANCELADO");

                when(eventos.obtenerEvento(21L))
                                .thenReturn(evento);

                return eventos;
        }

        // Administrador de transacciones simulado para los tests.
        private PlatformTransactionManager crearGestorTransacciones() {

                PlatformTransactionManager gestor = mock(PlatformTransactionManager.class);

                when(gestor.getTransaction(any()))
                                .thenReturn(new SimpleTransactionStatus());

                return gestor;
        }
}
