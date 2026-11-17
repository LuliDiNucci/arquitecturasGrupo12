package com.ticketya.entradas.security;

import com.ticketya.entradas.repositories.EntradaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component("entryAccess")
public class EntryAccess {
    private final EntradaRepository entradas;
    public EntryAccess(EntradaRepository entradas) { this.entradas = entradas; }
    public boolean client(Long id) { return CurrentIdentity.admin() || CurrentIdentity.authority("SERVICE_compras") || CurrentIdentity.owner(id); }
    public boolean entry(Long id) {
        if (CurrentIdentity.admin() || CurrentIdentity.authority("SERVICE_compras")) return true;
        return ownedEntry(id);
    }
    public boolean ownedEntry(Long id) {
        return CurrentIdentity.owner(entradas.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Recurso inexistente")).getClienteId());
    }
    public static Long administrator(Long supplied) {
        Long real = CurrentIdentity.userId();
        if (supplied != null && !supplied.equals(real))
            throw new org.springframework.security.access.AccessDeniedException("administradorId no coincide con el JWT");
        return real;
    }
}
