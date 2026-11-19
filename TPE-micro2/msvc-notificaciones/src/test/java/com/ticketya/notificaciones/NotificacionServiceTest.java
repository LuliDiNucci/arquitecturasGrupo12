package com.ticketya.notificaciones;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import org.springframework.dao.DataIntegrityViolationException;

import com.ticketya.notificaciones.dtos.ArtistaFavoritoRequestDTO;
import com.ticketya.notificaciones.dtos.CompraConfirmadaRequestDTO;
import com.ticketya.notificaciones.dtos.DestinatarioDTO;
import com.ticketya.notificaciones.dtos.EventoModificadoRequestDTO;
import com.ticketya.notificaciones.entities.EstadoNotificacion;
import com.ticketya.notificaciones.entities.Notificacion;
import com.ticketya.notificaciones.entities.TipoNotificacion;
import com.ticketya.notificaciones.repositories.NotificacionRepository;
import com.ticketya.notificaciones.services.impl.NotificacionServiceImpl;

class NotificacionServiceTest {

    private static final Instant AHORA = Instant.parse("2026-11-16T22:00:00Z");
    private static final DestinatarioDTO CLIENTE = new DestinatarioDTO(12L, "cliente@example.test");
    private static final CompraConfirmadaRequestDTO COMPRA = new CompraConfirmadaRequestDTO(CLIENTE, 101L, "Recital");
    private final NotificacionRepository repository = mock(NotificacionRepository.class);
    private final NotificacionServiceImpl service = new NotificacionServiceImpl(
            repository, Clock.fixed(AHORA, ZoneOffset.UTC));

    @BeforeEach
    void simularInsercion() {
        when(repository.saveAndFlush(any(Notificacion.class))).thenAnswer(invocacion -> invocacion.getArgument(0));
    }

    @Test
    void registraLaCompraPendienteConSuContenido() {
        var respuesta = service.registrarCompraConfirmada(COMPRA);
        var captor = ArgumentCaptor.forClass(Notificacion.class);
        verify(repository).saveAndFlush(captor.capture());

        assertThat(respuesta.estado()).isEqualTo(EstadoNotificacion.PENDIENTE);
        assertThat(respuesta.intentos()).isZero();
        assertThat(respuesta.proximoIntentoEn()).isEqualTo(AHORA);
        assertThat(captor.getValue().getEmail()).isEqualTo(CLIENTE.email());
        assertThat(captor.getValue().getMensaje()).contains("#101", "Recital");
    }

    @Test
    void reutilizaUnRegistroExistente() {
        when(repository.findByTipoAndReferenciaIdAndVersionEventoAndClienteId(
                TipoNotificacion.COMPRA_CONFIRMADA, 101L, 0L, 12L)).thenReturn(Optional.of(compraExistente()));

        assertThat(service.registrarCompraConfirmada(COMPRA).creadaEn()).isEqualTo(AHORA.minusSeconds(60));
        verify(repository, never()).saveAndFlush(any());
    }

    @Test
    void recuperaElRegistroQueInsertoOtraSolicitudSimultanea() {
        when(repository.findByTipoAndReferenciaIdAndVersionEventoAndClienteId(
                TipoNotificacion.COMPRA_CONFIRMADA, 101L, 0L, 12L))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(compraExistente()));
        when(repository.saveAndFlush(any(Notificacion.class)))
                .thenThrow(new DataIntegrityViolationException("Clave duplicada"));

        assertThat(service.registrarCompraConfirmada(COMPRA).creadaEn()).isEqualTo(AHORA.minusSeconds(60));
    }

    @Test
    void propagaUnErrorDeIntegridadSiLaClaveNoExiste() {
        var error = new DataIntegrityViolationException("Error de integridad");
        when(repository.saveAndFlush(any(Notificacion.class))).thenThrow(error);

        assertThatThrownBy(() -> service.registrarCompraConfirmada(COMPRA)).isSameAs(error);
    }

    @Test
    void usaLaVersionDelEventoParaDistinguirLasModificaciones() {
        var dto = new EventoModificadoRequestDTO(CLIENTE, 45L, 3L, "Recital", "Nueva hora: 20:30");

        var respuesta = service.registrarEventoModificado(dto);
        verify(repository).findByTipoAndReferenciaIdAndVersionEventoAndClienteId(
                TipoNotificacion.EVENTO_MODIFICADO, 45L, 3L, 12L);
        assertThat(respuesta.versionEvento()).isEqualTo(3);
    }

    @Test
    void registraAlArtistaFavoritoSiCoincideLaCiudad() {
        var respuesta = service.registrarArtistaFavorito(
                publicacion(" tandil ", Set.of("Los Viajeros", "Otra Banda")));

        var captor = ArgumentCaptor.forClass(Notificacion.class);
        verify(repository).saveAndFlush(captor.capture());

        assertThat(respuesta).hasValueSatisfying(aviso -> assertThat(aviso.tipo())
                .isEqualTo(TipoNotificacion.ARTISTA_FAVORITO));

        assertThat(captor.getValue().getMensaje())
                .contains("Los Viajeros", "Tandil", "05/12/2026 21:00");
    }

    @Test
    void omiteLaPublicacionSiFaltaAlgunaCoincidencia() {
        assertThat(service.registrarArtistaFavorito(
                publicacion("Azul", Set.of("Los Viajeros")))).isEmpty();

        assertThat(service.registrarArtistaFavorito(
                publicacion("Tandil", Set.of("Otra Banda")))).isEmpty();

        verifyNoInteractions(repository);
    }

    private Notificacion compraExistente() {
        return new Notificacion(TipoNotificacion.COMPRA_CONFIRMADA, 101L, 0, CLIENTE.clienteId(),
                CLIENTE.email(), "Compra confirmada", "Compra #101", AHORA.minusSeconds(60));
    }

    private ArtistaFavoritoRequestDTO publicacion(
            String ciudadCliente,
            Set<String> favoritos) {

        return new ArtistaFavoritoRequestDTO(
                CLIENTE,
                45L,
                "Recital en Tandil",
                LocalDateTime.parse("2026-12-05T21:00:00"),
                "Tandil",
                "Los Viajeros",
                ciudadCliente,
                favoritos);
    }

    @Test
    void noDuplicaNotificacionPorArtistaFavorito() {

        var solicitud = publicacion(
                "Tandil",
                Set.of("Los Viajeros"));

        Notificacion existente = new Notificacion(
                TipoNotificacion.ARTISTA_FAVORITO,
                45L,
                0L,
                CLIENTE.clienteId(),
                CLIENTE.email(),
                "TicketYa | Un artista favorito toca en tu ciudad",
                "Notificacion existente",
                AHORA.minusSeconds(60));

        when(repository.findByTipoAndReferenciaIdAndVersionEventoAndClienteId(
                TipoNotificacion.ARTISTA_FAVORITO,
                45L,
                0L,
                12L)).thenReturn(Optional.of(existente));

        var respuesta = service.registrarArtistaFavorito(solicitud);

        assertThat(respuesta).isPresent();

        assertThat(respuesta.get().creadaEn())
                .isEqualTo(AHORA.minusSeconds(60));

        verify(repository, never())
                .saveAndFlush(any(Notificacion.class));
    }
}
