package ar.edu.unicen.ticketya.eventos.service;

import ar.edu.unicen.ticketya.eventos.service.impl.*;

import ar.edu.unicen.ticketya.eventos.dto.AsientoRequestDTO;
import ar.edu.unicen.ticketya.eventos.dto.AsientosRequestDTO;
import ar.edu.unicen.ticketya.eventos.exception.ReglaNegocioException;
import ar.edu.unicen.ticketya.eventos.model.*;
import ar.edu.unicen.ticketya.eventos.repository.AsientoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AsientoServiceTest {

    @Mock private AsientoRepository asientoRepository;
    @Mock private SectorService sectorService;
    @Mock private LugarService lugarService;
    private AsientoService asientoService;

    @BeforeEach
    void preparar() {
        asientoService = new AsientoServiceImpl(asientoRepository, sectorService, lugarService);
    }

    private Sector sector(boolean numerado, int capacidad) {
        Lugar lugar = new Lugar("Lugar", "Dirección", "Tandil",
                new BigDecimal("-37.32"), new BigDecimal("-59.14"));
        ReflectionTestUtils.setField(lugar, "id", 4L);
        Sector sector = new Sector(lugar, "Platea", numerado, capacidad);
        ReflectionTestUtils.setField(sector, "id", 15L);
        return sector;
    }

    private void permitirEdicion(Sector sector) {
        when(sectorService.buscarSector(15L)).thenReturn(sector);
        when(sectorService.bloquearSector(15L)).thenReturn(sector);
    }

    @Test
    void noPermiteAsientosEnUnSectorNoNumerado() {
        permitirEdicion(sector(false, 10));
        AsientosRequestDTO request = new AsientosRequestDTO(List.of(new AsientoRequestDTO("A", 1)));

        assertThrows(ReglaNegocioException.class, () -> asientoService.agregarAsientos(15L, request));
        verify(asientoRepository, never()).saveAllAndFlush(any());
    }

    @Test
    void noPermiteSuperarCapacidad() {
        permitirEdicion(sector(true, 2));
        when(asientoRepository.countBySectorId(15L)).thenReturn(1L);
        AsientosRequestDTO request = new AsientosRequestDTO(List.of(
                new AsientoRequestDTO("A", 1), new AsientoRequestDTO("A", 2)
        ));

        assertThrows(ReglaNegocioException.class, () -> asientoService.agregarAsientos(15L, request));
    }

    @Test
    void noPermiteDosAsientosConLaMismaFilaYNumeroEnElRequest() {
        permitirEdicion(sector(true, 10));
        AsientosRequestDTO request = new AsientosRequestDTO(List.of(
                new AsientoRequestDTO("a", 1), new AsientoRequestDTO("A", 1)
        ));

        assertThrows(ReglaNegocioException.class, () -> asientoService.agregarAsientos(15L, request));
        verify(asientoRepository, never()).saveAllAndFlush(any());
    }

    @Test
    void noPermiteDuplicarUnAsientoYaExistente() {
        permitirEdicion(sector(true, 10));
        when(asientoRepository.existsBySectorIdAndFilaIgnoreCaseAndNumero(15L, "A", 1))
                .thenReturn(true);

        AsientosRequestDTO request = new AsientosRequestDTO(List.of(new AsientoRequestDTO("a", 1)));
        assertThrows(ReglaNegocioException.class, () -> asientoService.agregarAsientos(15L, request));
    }

    @Test
    void cargaAsientosNormalizandoLaFila() {
        permitirEdicion(sector(true, 2));
        when(asientoRepository.saveAllAndFlush(anyList())).thenAnswer(i -> i.getArgument(0));
        AsientosRequestDTO request = new AsientosRequestDTO(List.of(new AsientoRequestDTO("a", 1)));

        var resultado = asientoService.agregarAsientos(15L, request);

        assertEquals(1, resultado.size());
        assertEquals("A", resultado.get(0).getFila());
    }
}
