package ar.edu.unicen.ticketya.eventos.service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import ar.edu.unicen.ticketya.eventos.feign.ComprasFeignClient;
import ar.edu.unicen.ticketya.eventos.feign.EntradasFeignClient;
import ar.edu.unicen.ticketya.eventos.feign.ReservasFeignClient;
import ar.edu.unicen.ticketya.eventos.integration.NotificadorCancelaciones;
import ar.edu.unicen.ticketya.eventos.model.NotificacionCancelacion;
import ar.edu.unicen.ticketya.eventos.repository.NotificacionCancelacionRepository;

@ExtendWith(MockitoExtension.class)
class NotificadorCancelacionesTest {

    @Mock private NotificacionCancelacionRepository repository;
    @Mock private ReservasFeignClient reservasClient;
    @Mock private EntradasFeignClient entradasClient;
    @Mock private ComprasFeignClient comprasClient;

    private final Clock clock = Clock.systemUTC();

    private NotificacionCancelacion aviso() {
        return new NotificacionCancelacion(10L, false, false, 0,
                LocalDateTime.now(clock), null, null);
    }


    private NotificadorCancelaciones notificador(boolean habilitadas) {
        return new NotificadorCancelaciones(
                repository,
                reservasClient,
                entradasClient,
                comprasClient,
                clock,
                habilitadas,
                true,  // entradasHabilitadas
                true   // comprasHabilitadas
        );
    }


    @Test
    void siFallanEntradasYComprasConservaAmbosAvisosPendientes() {
        NotificacionCancelacion aviso = aviso();
        when(repository.pendientes()).thenReturn(List.of(aviso));
        doThrow(new RuntimeException("HTTP 404")).when(entradasClient)
                .cancelarEvento(10L, "cancelacion-evento-10");
        doThrow(new RuntimeException("HTTP 404")).when(comprasClient)
                .cancelarEvento(10L, "cancelacion-evento-10");
        notificador(true).enviarPendientes();
        verify(reservasClient).cancelarEvento(10L, "cancelacion-evento-10");
        assertTrue(aviso.isReservasNotificadas());
        assertFalse(aviso.isEntradasNotificadas());
        assertFalse(aviso.isComprasNotificadas());
        assertTrue(aviso.getUltimoError().contains("Entradas"));
        assertTrue(aviso.getUltimoError().contains("Compras"));
    }

    @Test
    void conIntegracionesDesactivadasDejaLosAvisosPendientes() {
        notificador(false).enviarPendientes();

        verifyNoInteractions(repository, reservasClient, entradasClient, comprasClient);
    }

    @Test
    void avisaATresServiciosUsandoElMismoIdDeOperacion() {
        NotificacionCancelacion aviso = aviso();
        when(repository.pendientes()).thenReturn(List.of(aviso));

        notificador(true).enviarPendientes();

        verify(reservasClient).cancelarEvento(10L, "cancelacion-evento-10");
        verify(entradasClient).cancelarEvento(10L, "cancelacion-evento-10");
        verify(comprasClient).cancelarEvento(10L, "cancelacion-evento-10");
        assertTrue(aviso.isReservasNotificadas());
        assertTrue(aviso.isEntradasNotificadas());
        assertTrue(aviso.isComprasNotificadas());
        verify(repository).save(aviso);
    }

    @Test
    void siFallaEntradasSigueAvisandoACompras() {
        NotificacionCancelacion aviso = aviso();
        when(repository.pendientes()).thenReturn(List.of(aviso));
        doThrow(new RuntimeException("No disponible"))
                .when(entradasClient).cancelarEvento(10L, "cancelacion-evento-10");

        notificador(true).enviarPendientes();

        verify(reservasClient).cancelarEvento(10L, "cancelacion-evento-10");
        verify(comprasClient).cancelarEvento(10L, "cancelacion-evento-10");
        assertTrue(aviso.isReservasNotificadas());
        assertFalse(aviso.isEntradasNotificadas());
        assertTrue(aviso.isComprasNotificadas());
        assertTrue(aviso.getUltimoError().contains("Entradas"));
    }
}
