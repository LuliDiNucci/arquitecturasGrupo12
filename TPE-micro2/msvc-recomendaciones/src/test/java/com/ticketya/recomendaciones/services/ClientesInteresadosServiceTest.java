package com.ticketya.recomendaciones.services;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import static org.mockito.Mockito.when;

import com.ticketya.recomendaciones.models.Preferencias;
import com.ticketya.recomendaciones.repositories.PreferenciasRepository;

public class ClientesInteresadosServiceTest {

    @Test
    void encuentraClientesPorArtistaYCiudad() {

        PreferenciasRepository repository = Mockito.mock(PreferenciasRepository.class);

        ClientesInteresadosService service = new ClientesInteresadosService(repository);

        // Coincide artista y ciudad
        Preferencias cliente1 = new Preferencias(
                1L,
                List.of("RECITAL"),
                List.of("Los Viajeros"),
                "Tandil");

        // Coincide artista, pero no ciudad
        Preferencias cliente2 = new Preferencias(
                2L,
                List.of("RECITAL"),
                List.of("Los Viajeros"),
                "Azul");

        // Coincide ciudad, pero no artista
        Preferencias cliente3 = new Preferencias(
                3L,
                List.of("RECITAL"),
                List.of("Otra Banda"),
                "Tandil");

        when(repository.findAll())
                .thenReturn(List.of(cliente1, cliente2, cliente3));

        List<Long> resultado = service.buscarClientesInteresados(
                "los viajeros",
                " tandil ");

        assertEquals(1, resultado.size());
        assertEquals(1L, resultado.get(0));
    }

    @Test
    void noEncuentraClientesSinCoincidencias() {

        PreferenciasRepository repository = Mockito.mock(PreferenciasRepository.class);

        ClientesInteresadosService service = new ClientesInteresadosService(repository);

        when(repository.findAll()).thenReturn(List.of());

        List<Long> resultado = service.buscarClientesInteresados(
                "Banda Inexistente",
                "Tandil");

        assertEquals(0, resultado.size());
    }
}