package com.ticketya.entradas.services.impl;

import com.ticketya.entradas.entities.AsignacionPersonalAcceso;
import com.ticketya.entradas.repositories.AsignacionPersonalAccesoRepository;
import com.ticketya.entradas.security.CurrentIdentity;
import com.ticketya.entradas.services.AccesoEventoService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("accessEvent")
public class AccesoEventoServiceImpl implements AccesoEventoService {
    private final AsignacionPersonalAccesoRepository repository;
    public AccesoEventoServiceImpl(AsignacionPersonalAccesoRepository repository) { this.repository = repository; }
    @Override @Transactional @PreAuthorize("hasRole('ADMINISTRADOR')")
    public void asignar(Long eventoId, Long usuarioId) {
        if (!repository.existsByEventoIdAndUsuarioId(eventoId, usuarioId))
            repository.save(new AsignacionPersonalAcceso(eventoId, usuarioId));
    }
    @Override @Transactional @PreAuthorize("hasRole('ADMINISTRADOR')")
    public void revocar(Long eventoId, Long usuarioId) { repository.deleteByEventoIdAndUsuarioId(eventoId, usuarioId); }
    @Override @Transactional(readOnly = true)
    public boolean puedeValidar(Long eventoId) {
        return CurrentIdentity.authority("ROLE_PERSONAL_ACCESO") && CurrentIdentity.userId() != null
            && repository.existsByEventoIdAndUsuarioId(eventoId, CurrentIdentity.userId());
    }
}
