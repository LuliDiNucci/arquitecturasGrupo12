package com.ticketya.notificaciones.services.impl;

import com.ticketya.notificaciones.dtos.ArtistaFavoritoRequestDTO;
import com.ticketya.notificaciones.dtos.CompraConfirmadaRequestDTO;
import com.ticketya.notificaciones.dtos.DestinatarioDTO;
import com.ticketya.notificaciones.dtos.EventoCanceladoRequestDTO;
import com.ticketya.notificaciones.dtos.EventoModificadoRequestDTO;
import com.ticketya.notificaciones.dtos.NotificacionResponseDTO;
import com.ticketya.notificaciones.entities.Notificacion;
import com.ticketya.notificaciones.entities.TipoNotificacion;
import com.ticketya.notificaciones.repositories.NotificacionRepository;
import com.ticketya.notificaciones.services.NotificacionService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Service
public class NotificacionServiceImpl implements NotificacionService {

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/uuuu HH:mm");

    private final NotificacionRepository repository;
    private final Clock reloj;

    public NotificacionServiceImpl(NotificacionRepository repository, Clock reloj) {
        this.repository = repository;
        this.reloj = reloj;
    }

    @Override
    public NotificacionResponseDTO registrarCompraConfirmada(CompraConfirmadaRequestDTO solicitud) {
        String mensaje = """
                Confirmamos tu compra #%d para %s.
                Gracias por elegir TicketYa.
                """.formatted(solicitud.compraId(), solicitud.nombreEvento());

        return registrar(TipoNotificacion.COMPRA_CONFIRMADA, solicitud.compraId(), 0,
                solicitud.destinatario(), "TicketYa | Compra confirmada", mensaje);
    }

    @Override
    public NotificacionResponseDTO registrarEventoCancelado(EventoCanceladoRequestDTO solicitud) {
        String mensaje = """
                El evento %s fue cancelado.
                Podés consultar el estado de tu compra y del reintegro en TicketYa.
                """.formatted(solicitud.nombreEvento());

        return registrar(TipoNotificacion.EVENTO_CANCELADO, solicitud.eventoId(), 0,
                solicitud.destinatario(), "TicketYa | Evento cancelado", mensaje);
    }

    @Override
    public NotificacionResponseDTO registrarEventoModificado(EventoModificadoRequestDTO solicitud) {
        String mensaje = """
                Hubo un cambio en el evento %s.

                %s
                """.formatted(solicitud.nombreEvento(), solicitud.detalleCambios());

        return registrar(TipoNotificacion.EVENTO_MODIFICADO, solicitud.eventoId(), solicitud.versionEvento(),
                solicitud.destinatario(), "TicketYa | Cambio en tu evento", mensaje);
    }

    @Override
    public Optional<NotificacionResponseDTO> registrarArtistaFavorito(ArtistaFavoritoRequestDTO solicitud) {
        boolean mismaCiudad = solicitud.ciudadEvento().strip().equalsIgnoreCase(solicitud.ciudadCliente().strip());
        boolean artistaFavorito = solicitud.artistasFavoritosIds().contains(solicitud.artistaId());
        if (!mismaCiudad || !artistaFavorito) {
            return Optional.empty();
        }

        String mensaje = """
                %s, uno de tus artistas favoritos, participa en %s.
                El evento se realizará en %s el %s.
                """.formatted(solicitud.nombreArtista(), solicitud.nombreEvento(),
                solicitud.ciudadEvento().strip(), solicitud.fechaHora().format(FORMATO_FECHA));

        return Optional.of(registrar(TipoNotificacion.ARTISTA_FAVORITO, solicitud.eventoId(), 0,
                solicitud.destinatario(), "TicketYa | Un artista favorito toca en tu ciudad", mensaje));
    }

    @Override
    public Optional<NotificacionResponseDTO> buscarPorId(long id) {
        return repository.findById(id).map(NotificacionResponseDTO::desde);
    }

    private NotificacionResponseDTO registrar(TipoNotificacion tipo, long referenciaId, long versionEvento,
                                               DestinatarioDTO destinatario, String asunto, String mensaje) {
        long clienteId = destinatario.clienteId();
        return repository.findByTipoAndReferenciaIdAndVersionEventoAndClienteId(
                        tipo, referenciaId, versionEvento, clienteId)
                .map(NotificacionResponseDTO::desde)
                .orElseGet(() -> insertar(new Notificacion(tipo, referenciaId, versionEvento, clienteId,
                        destinatario.email(), asunto, mensaje, reloj.instant())));
    }

    private NotificacionResponseDTO insertar(Notificacion nueva) {
        try {
            return NotificacionResponseDTO.desde(repository.saveAndFlush(nueva));
        } catch (DataIntegrityViolationException ex) {
            // La transacción de saveAndFlush ya terminó. Otra solicitud pudo registrar la misma clave.
            return repository.findByTipoAndReferenciaIdAndVersionEventoAndClienteId(
                            nueva.getTipo(), nueva.getReferenciaId(), nueva.getVersionEvento(), nueva.getClienteId())
                    .map(NotificacionResponseDTO::desde)
                    .orElseThrow(() -> ex);
        }
    }
}
