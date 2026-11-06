package com.ticketya.entradas.services;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.Mock;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.SimpleTransactionStatus;

import com.ticketya.entradas.clients.CompraFeignClient;
import com.ticketya.entradas.clients.EventoFeignClient;
import com.ticketya.entradas.clients.ReservaFeignClient;
import com.ticketya.entradas.dtos.DevolucionRequestDTO;
import com.ticketya.entradas.dtos.DevolverEntradaDTO;
import com.ticketya.entradas.dtos.EventoDTO;
import com.ticketya.entradas.entities.Entrada;
import com.ticketya.entradas.entities.SolicitudDevolucion;
import com.ticketya.entradas.repositories.EntradaRepository;
import com.ticketya.entradas.repositories.SolicitudDevolucionRepository;

@ExtendWith(MockitoExtension.class)
class DevolucionServiceImplTest {

        @Mock
        private SolicitudDevolucionRepository devolucionRepository;

        @Mock
        private EntradaRepository entradaRepository;

        @Mock
        private EventoFeignClient eventoFeignClient;

        @Mock
        private ReservaFeignClient reservaFeignClient;

        @Mock
        private CompraFeignClient compraFeignClient;

        private DevolucionServiceImpl service;

        @BeforeEach
        void setUp() {
                PlatformTransactionManager transactionManager = mock(PlatformTransactionManager.class);

                lenient().when(transactionManager.getTransaction(any()))
                                .thenAnswer(inv -> new SimpleTransactionStatus());
                service = new DevolucionServiceImpl(
                                devolucionRepository,
                                entradaRepository,
                                eventoFeignClient,
                                reservaFeignClient,
                                compraFeignClient,
                                transactionManager);
        }

        @Test
        void solicitaDevolucionValidaConMasDe72Horas() throws Exception {
                Entrada entrada = entrada(10L);
                when(entradaRepository.findById(10L)).thenReturn(Optional.of(entrada));
                when(eventoFeignClient.obtenerEvento(20L)).thenReturn(
                                new EventoDTO(20L, LocalDateTime.now().plusHours(96), "PUBLICADO"));
                when(devolucionRepository.findByEntrada_IdAndEstado(10L, "PENDIENTE"))
                                .thenReturn(Optional.empty());
                when(devolucionRepository.save(any(SolicitudDevolucion.class)))
                                .thenAnswer(invocation -> invocation.getArgument(0));

                var response = service.solicitar(
                                new DevolucionRequestDTO(10L, "Cambio de planes"));

                assertEquals(10L, response.getEntradaId());
                assertEquals("PENDIENTE", response.getEstado());
                verify(devolucionRepository).save(any(SolicitudDevolucion.class));
        }

        @Test
        void rechazaSolicitudConMenosDe72Horas() {
                Entrada entrada = entrada(10L);
                when(entradaRepository.findById(10L)).thenReturn(Optional.of(entrada));
                when(eventoFeignClient.obtenerEvento(20L)).thenReturn(
                                new EventoDTO(20L, LocalDateTime.now().plusHours(24), "PUBLICADO"));

                assertThrows(RuntimeException.class, () -> service.solicitar(
                                new DevolucionRequestDTO(10L, "Muy cerca del evento")));

                verify(devolucionRepository, never()).save(any());
        }

        @Test
        void rechazaEntradaInexistente() {
                when(entradaRepository.findById(10L)).thenReturn(Optional.empty());

                assertThrows(RuntimeException.class, () -> service.solicitar(
                                new DevolucionRequestDTO(10L, "Entrada inexistente")));

                verifyNoExternalCalls();
        }

        @Test
        void rechazaSolicitudDuplicadaPendiente() {
                Entrada entrada = entrada(10L);
                when(entradaRepository.findById(10L)).thenReturn(Optional.of(entrada));
                when(eventoFeignClient.obtenerEvento(20L)).thenReturn(
                                new EventoDTO(20L, LocalDateTime.now().plusHours(96), "PUBLICADO"));
                when(devolucionRepository.findByEntrada_IdAndEstado(10L, "PENDIENTE"))
                                .thenReturn(Optional.of(new SolicitudDevolucion()));

                assertThrows(RuntimeException.class, () -> service.solicitar(
                                new DevolucionRequestDTO(10L, "Solicitud repetida")));

                verify(devolucionRepository, never()).save(any());
        }

        @Test
        void aprobarLiberaUnaEntradaConElEndpointDeDevolucion() throws Exception {
                Entrada entrada = entrada(10L);
                SolicitudDevolucion solicitud = solicitud(entrada, "PENDIENTE");
                when(devolucionRepository.findById(50L)).thenReturn(Optional.of(solicitud));
                when(devolucionRepository.save(any(SolicitudDevolucion.class)))
                                .thenAnswer(invocation -> invocation.getArgument(0));

                service.aprobar(50L, 99L);

                ArgumentCaptor<DevolverEntradaDTO> captor = ArgumentCaptor.forClass(DevolverEntradaDTO.class);
                verify(reservaFeignClient).devolverEntrada(
                                org.mockito.ArgumentMatchers.eq(30L), captor.capture());
                DevolverEntradaDTO request = captor.getValue();
                assertEquals(10L, request.entradaId());
                assertEquals(20L, request.sectorId());
                assertEquals(40L, request.asientoId());
                assertEquals("APROBADA", solicitud.getEstado());
                ArgumentCaptor<DevolucionRequestDTO> captor1 = ArgumentCaptor.forClass(DevolucionRequestDTO.class);

                verify(compraFeignClient).reintegrarEntrada(captor1.capture());

                assertEquals(entrada.getId(), captor1.getValue().getEntradaId());
        }

        @Test
        void rechazarNoLiberaElAsientoNiSolicitaReintegro() throws Exception {
                Entrada entrada = entrada(10L);
                SolicitudDevolucion solicitud = solicitud(entrada, "PENDIENTE");
                when(devolucionRepository.findById(50L)).thenReturn(Optional.of(solicitud));
                when(devolucionRepository.save(any(SolicitudDevolucion.class)))
                                .thenAnswer(invocation -> invocation.getArgument(0));

                service.rechazar(50L, 99L);

                assertEquals("RECHAZADA", solicitud.getEstado());
                assertEquals("EMITIDA", entrada.getEstado());
                verify(reservaFeignClient, never()).devolverEntrada(any(), any());
                verify(compraFeignClient, never()).reintegrarEntrada(any());
                verify(entradaRepository, never()).save(any());
        }

        private Entrada entrada(Long id) {
                Entrada entrada = new Entrada();
                setField(entrada, "id", id);
                entrada.setEstado("EMITIDA");
                entrada.setEventoId(20L);
                entrada.setReservaId(30L);
                entrada.setSectorId(20L);
                entrada.setAsientoId(40L);
                entrada.setCompraId(60L);
                return entrada;
        }

        private SolicitudDevolucion solicitud(Entrada entrada, String estado) {
                SolicitudDevolucion solicitud = new SolicitudDevolucion();
                solicitud.setEntrada(entrada);
                solicitud.setEstado(estado);
                return solicitud;
        }

        private void verifyNoExternalCalls() {
                verify(eventoFeignClient, never()).obtenerEvento(any());
                verify(reservaFeignClient, never()).devolverEntrada(any(), any());
                verify(compraFeignClient, never()).reintegrarEntrada(any());
        }

        private void setField(Object target, String name, Object value) {
                try {
                        Field field = target.getClass().getDeclaredField(name);
                        field.setAccessible(true);
                        field.set(target, value);
                } catch (ReflectiveOperationException exception) {
                        throw new AssertionError("No se pudo preparar el fixture", exception);
                }
        }
}
