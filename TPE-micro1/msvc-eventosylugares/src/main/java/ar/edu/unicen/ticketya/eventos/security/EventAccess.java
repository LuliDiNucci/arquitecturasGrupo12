package ar.edu.unicen.ticketya.eventos.security;

import ar.edu.unicen.ticketya.eventos.model.*;
import ar.edu.unicen.ticketya.eventos.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Component("eventAccess")
@Transactional(readOnly = true)
public class EventAccess {
    private final EventoRepository eventos;
    private final OrganizadorRepository organizadores;
    public EventAccess(EventoRepository eventos, OrganizadorRepository organizadores) {
        this.eventos = eventos; this.organizadores = organizadores;
    }
    public boolean organizer(Long id) {
        if (CurrentIdentity.admin()) return true;
        return CurrentIdentity.authority("ROLE_ORGANIZADOR") && CurrentIdentity.owner(organizadores.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Organizador inexistente")).getUsuarioId());
    }
    public boolean event(Long id) {
        if (CurrentIdentity.admin()) return true;
        Evento evento = find(id);
        return CurrentIdentity.authority("ROLE_ORGANIZADOR") && CurrentIdentity.owner(evento.getOrganizador().getUsuarioId());
    }
    public boolean read(Long id) {
        Evento evento = find(id);
        return evento.getEstado() == EstadoEvento.PUBLICADO || evento.getEstado() == EstadoEvento.AGOTADO
            || CurrentIdentity.admin() || CurrentIdentity.authority("SERVICE_reservas")
            || CurrentIdentity.authority("SERVICE_compras") || CurrentIdentity.authority("SERVICE_entradas")
            || (CurrentIdentity.authority("ROLE_ORGANIZADOR") && CurrentIdentity.owner(evento.getOrganizador().getUsuarioId()));
    }
    private Evento find(Long id) {
        return eventos.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Evento inexistente"));
    }
}
