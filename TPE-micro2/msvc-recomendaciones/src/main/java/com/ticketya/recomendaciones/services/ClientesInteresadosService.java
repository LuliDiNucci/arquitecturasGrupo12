package com.ticketya.recomendaciones.services;

import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;

import com.ticketya.recomendaciones.models.Preferencias;
import com.ticketya.recomendaciones.repositories.PreferenciasRepository;

@Service
public class ClientesInteresadosService {

    private final PreferenciasRepository repository;

    public ClientesInteresadosService(PreferenciasRepository repository) {
        this.repository = repository;
    }

    public List<Long> buscarClientesInteresados(
            String nombreArtista,
            String ciudadEvento) {

        if (nombreArtista == null || nombreArtista.isBlank()
                || ciudadEvento == null || ciudadEvento.isBlank()) {
            return List.of();
        }

        return repository.findAll()
                .stream()
                .filter(Objects::nonNull)
                .filter(preferencias -> coincideCiudad(preferencias, ciudadEvento))
                .filter(preferencias -> coincideArtista(preferencias, nombreArtista))
                .map(Preferencias::getClienteId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
    }

    private boolean coincideCiudad(
            Preferencias preferencias,
            String ciudadEvento) {

        return preferencias.getCiudad() != null
                && preferencias.getCiudad().trim()
                        .equalsIgnoreCase(ciudadEvento.trim());
    }

    private boolean coincideArtista(
            Preferencias preferencias,
            String nombreArtista) {

        if (preferencias.getArtistasFavoritos() == null) {
            return false;
        }

        return preferencias.getArtistasFavoritos()
                .stream()
                .filter(Objects::nonNull)
                .anyMatch(artista -> artista.trim().equalsIgnoreCase(
                        nombreArtista.trim()));
    }
}