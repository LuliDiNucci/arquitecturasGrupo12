package com.ticketya.recomendaciones.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component("clienteAccess")
public class ClienteAccess {

    public boolean puedeAcceder(Long clienteId) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        boolean esAdministrador = authentication.getAuthorities()
                .stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMINISTRADOR"));

        if (esAdministrador) {
            return true;
        }

        boolean esCliente = authentication.getAuthorities()
                .stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_CLIENTE"));

        return esCliente
                && clienteId != null
                && clienteId.toString().equals(authentication.getName());
    }
}