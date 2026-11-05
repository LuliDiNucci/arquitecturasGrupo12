package ar.edu.unicen.ticketya.eventos.service;

import ar.edu.unicen.ticketya.eventos.service.impl.*;

import ar.edu.unicen.ticketya.eventos.dto.SectorRequestDTO;
import ar.edu.unicen.ticketya.eventos.exception.ReglaNegocioException;
import ar.edu.unicen.ticketya.eventos.model.*;
import ar.edu.unicen.ticketya.eventos.repository.AsientoRepository;
import ar.edu.unicen.ticketya.eventos.repository.SectorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SectorServiceTest {

    @Mock private SectorRepository sectorRepository;
    @Mock private AsientoRepository asientoRepository;
    @Mock private LugarService lugarService;
    private SectorService sectorService;

    @BeforeEach
    void preparar() {
        sectorService = new SectorServiceImpl(sectorRepository, asientoRepository, lugarService);
    }

    private Lugar lugar() {
        Lugar lugar = new Lugar("Teatro", "Calle 1", "Tandil",
                new BigDecimal("-37.32"), new BigDecimal("-59.14"));
        ReflectionTestUtils.setField(lugar, "id", 1L);
        return lugar;
    }

    @Test
    void noPermiteNombresRepetidosEnElMismoLugar() {
        when(lugarService.bloquearLugar(1L)).thenReturn(lugar());
        when(sectorRepository.existsByLugarIdAndNombreIgnoreCase(1L, "Platea"))
                .thenReturn(true);

        ReglaNegocioException error = assertThrows(ReglaNegocioException.class,
                () -> sectorService.crearSector(1L, new SectorRequestDTO("Platea", true, 30)));

        assertEquals(409, error.getStatus().value());
        verify(sectorRepository, never()).saveAndFlush(any());
    }

    @Test
    void creaUnSectorConCapacidadYTipo() {
        when(lugarService.bloquearLugar(1L)).thenReturn(lugar());
        when(sectorRepository.saveAndFlush(any(Sector.class))).thenAnswer(i -> {
            Sector sector = i.getArgument(0);
            ReflectionTestUtils.setField(sector, "id", 2L);
            return sector;
        });

        var respuesta = sectorService.crearSector(1L, new SectorRequestDTO("Campo", false, 500));

        assertEquals(2L, respuesta.getId());
        assertEquals(500, respuesta.getCapacidad());
        assertFalse(respuesta.isNumerado());
        verify(lugarService).validarLugarEditable(1L);
    }
}
