package ar.edu.unicen.ticketya.eventos.service.impl;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import ar.edu.unicen.ticketya.eventos.dto.EventoResponseDTO;
import ar.edu.unicen.ticketya.eventos.dto.ParticipanteResponseDTO;
import ar.edu.unicen.ticketya.eventos.dto.UsuarioContactoDTO;
import ar.edu.unicen.ticketya.eventos.feign.NotificacionesEventoFeignClient;
import ar.edu.unicen.ticketya.eventos.feign.RecomendacionesFeignClient;
import ar.edu.unicen.ticketya.eventos.feign.UsuariosFeignClient;
import ar.edu.unicen.ticketya.eventos.service.AvisoArtistasFavoritosService;
import feign.RetryableException;

@Service
public class AvisoArtistasFavoritosServiceImpl
        implements AvisoArtistasFavoritosService {

    private static final Logger log = LoggerFactory.getLogger(AvisoArtistasFavoritosServiceImpl.class);

    private final RecomendacionesFeignClient recomendacionesClient;
    private final UsuariosFeignClient usuariosClient;
    private final NotificacionesEventoFeignClient notificacionesClient;

    public AvisoArtistasFavoritosServiceImpl(
            RecomendacionesFeignClient recomendacionesClient,
            UsuariosFeignClient usuariosClient,
            NotificacionesEventoFeignClient notificacionesClient) {

        this.recomendacionesClient = recomendacionesClient;
        this.usuariosClient = usuariosClient;
        this.notificacionesClient = notificacionesClient;
    }

    @Override
    public void notificarPublicacion(EventoResponseDTO evento) {

        if (evento == null
                || evento.getId() == null
                || evento.getLugar() == null
                || evento.getParticipantes() == null) {
            return;
        }

        String ciudad = evento.getLugar().getCiudad();

        if (ciudad == null || ciudad.isBlank()) {
            return;
        }

        Set<Long> clientesNotificados = new HashSet<>();

        for (ParticipanteResponseDTO participante : evento.getParticipantes()) {

            if (participante == null
                    || participante.getNombre() == null
                    || participante.getNombre().isBlank()) {
                continue;
            }

            String artista = participante.getNombre();

            List<Long> interesados;

            try {
                interesados = ejecutarConReintentos(
                        () -> recomendacionesClient.buscarClientesInteresados(
                                artista,
                                ciudad));

            } catch (RuntimeException ex) {
                log.error(
                        "No se pudieron consultar clientes interesados para el evento {}",
                        evento.getId(),
                        ex);
                continue;
            }

            if (interesados == null) {
                continue;
            }

            for (Long clienteId : interesados) {

                if (clienteId == null
                        || clientesNotificados.contains(clienteId)) {
                    continue;
                }

                try {
                    UsuarioContactoDTO contacto = ejecutarConReintentos(
                            () -> usuariosClient.obtenerContacto(clienteId));

                    if (contacto == null
                            || contacto.email() == null
                            || contacto.email().isBlank()) {
                        continue;
                    }

                    var destinatario = new NotificacionesEventoFeignClient.Destinatario(
                            clienteId,
                            contacto.email());

                    var solicitud = new NotificacionesEventoFeignClient.ArtistaFavorito(
                            destinatario,
                            evento.getId(),
                            evento.getNombre(),
                            evento.getFechaHora(),
                            ciudad,
                            artista,
                            ciudad,
                            Set.of(artista));

                    ejecutarConReintentos(() -> {
                        notificacionesClient.avisarArtistaFavorito(solicitud);
                        return null;
                    });

                    clientesNotificados.add(clienteId);

                } catch (RuntimeException ex) {
                    log.error(
                            "No se pudo registrar la notificacion del evento {} para el cliente {}",
                            evento.getId(),
                            clienteId,
                            ex);
                }
            }
        }
    }

    private <T> T ejecutarConReintentos(Supplier<T> operacion) {

        for (int intento = 1; intento <= 3; intento++) {

            try {
                return operacion.get();

            } catch (RetryableException ex) {

                if (intento == 3) {
                    throw ex;
                }

                log.warn(
                        "Error de comunicacion. Reintento {} de 3",
                        intento + 1);

                try {
                    Thread.sleep(300L * intento);

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new IllegalStateException(
                            "Operacion interrumpida",
                            e);
                }
            }
        }

        throw new IllegalStateException("No se pudo completar la operacion");
    }
}