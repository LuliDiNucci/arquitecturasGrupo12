package com.ticketya.notificaciones.security;

import com.ticketya.notificaciones.entities.TipoNotificacion;
import com.ticketya.notificaciones.repositories.NotificacionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component("notificationAccess")
public class NotificationAccess {
    private final NotificacionRepository repository;
    public NotificationAccess(NotificacionRepository repository) { this.repository = repository; }
    public boolean read(long id) {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) return false;
        var notification = repository.findById(id).orElseThrow(() ->
            new ResponseStatusException(HttpStatus.NOT_FOUND, "Notificación inexistente"));
        var authorities = authentication.getAuthorities().stream().map(a -> a.getAuthority()).toList();
        return authorities.contains("ROLE_ADMINISTRADOR")
            || (authorities.contains("USER") && Long.toString(notification.getClienteId()).equals(authentication.getName()))
            || (notification.getTipo() == TipoNotificacion.COMPRA_CONFIRMADA
                ? authorities.contains("SERVICE_compras") : authorities.contains("SERVICE_eventos"));
    }
}
