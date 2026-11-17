package com.ticketya.entradas;

import com.ticketya.entradas.clients.*;
import com.ticketya.entradas.entities.Entrada;
import com.ticketya.entradas.repositories.EntradaRepository;
import com.ticketya.entradas.services.QrCodeService;
import com.ticketya.entradas.services.impl.EntradaServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EntradaServiceTest {
    private final EntradaRepository repository = mock(EntradaRepository.class);
    private final CompraClient compras = mock(CompraClient.class);
    private final ReservaClient reservas = mock(ReservaClient.class);
    private final QrCodeService qr = mock(QrCodeService.class);
    private final EntradaServiceImpl service = new EntradaServiceImpl(repository,
            compras, reservas, qr, mock(EventoFeignClient.class),
            mock(ReservaFeignClient.class), mock(CompraFeignClient.class));

    @Test
    void compraInvalidaNoConsultaOtrosServicios() {
        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> service.emitir(0L));
        assertEquals(HttpStatus.BAD_REQUEST, error.getStatusCode());
        verifyNoInteractions(repository, compras, reservas, qr);
    }

    @Test
    void emisionRepetidaDevuelveExistentesSinCrearNiConsultarCompra() {
        Entrada entrada = new Entrada();
        entrada.setCompraId(1L);
        entrada.setCodigoQr("codigo-existente");
        when(repository.findByCompraIdOrderByIdAsc(1L)).thenReturn(List.of(entrada));
        when(qr.generarImagenBase64("codigo-existente")).thenReturn("imagen");
        var resultado = service.emitir(1L);
        assertFalse(resultado.isCreada());
        assertEquals(1, resultado.getEntradas().size());
        assertEquals("codigo-existente", resultado.getEntradas().get(0).getCodigoQr());
        assertEquals("imagen", resultado.getEntradas().get(0).getQrImagenBase64());
        verifyNoInteractions(compras, reservas);
        verify(repository, never()).saveAllAndFlush(any());
    }

    @Test
    void consultaRechazaClienteAjenoAntesDeGenerarQr() {
        Entrada entrada = new Entrada();
        entrada.setClienteId(10L);
        when(repository.findById(1L)).thenReturn(Optional.of(entrada));
        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> service.obtener(1L, 20L));
        assertEquals(HttpStatus.FORBIDDEN, error.getStatusCode());
        verifyNoInteractions(qr);
    }
}
