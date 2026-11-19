package ar.edu.unicen.ticketya.eventos.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Supplier;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import ar.edu.unicen.ticketya.eventos.dto.EventoResponseDTO;
import ar.edu.unicen.ticketya.eventos.dto.EventoResumenResponseDTO;
import ar.edu.unicen.ticketya.eventos.model.EstadoEvento;
import ar.edu.unicen.ticketya.eventos.model.OrdenEvento;
import ar.edu.unicen.ticketya.eventos.model.TipoEvento;

@Service
public class CatalogoCacheService {

    private record Filtros(TipoEvento tipo, Long organizadorId, Long participanteId,
            String ciudad, LocalDateTime desde, LocalDateTime hasta, String texto, OrdenEvento ordenar) {
    }

    private record Entradas(Cache<String, List<EventoResumenResponseDTO>> publicados,
            Cache<Long, EventoResponseDTO> detalles,
            Cache<Filtros, List<EventoResumenResponseDTO>> busquedas) {
    }

    private final Duration ttl;
    private volatile Entradas entradas;

    public CatalogoCacheService(@Value("${app.catalogo.cache-ttl-segundos:30}") long ttlSegundos) {
        ttl = Duration.ofSeconds(Math.max(1, ttlSegundos));
        entradas = nuevasEntradas();
    }

    public EventoResponseDTO detalle(Long id, boolean fresca, Supplier<EventoResponseDTO> consulta) {
        if (fresca || TransactionSynchronizationManager.isActualTransactionActive()) {
            return consulta.get();
        }

        Cache<Long, EventoResponseDTO> cache = entradas.detalles();
        EventoResponseDTO resultado = cache.getIfPresent(id);
        if (resultado != null) {
            return resultado;
        }

        resultado = consulta.get();
        if (resultado.getEstado() == EstadoEvento.PUBLICADO || resultado.getEstado() == EstadoEvento.AGOTADO) {
            cache.put(id, resultado);
        }
        return resultado;
    }

    public List<EventoResumenResponseDTO> publicados(Supplier<List<EventoResumenResponseDTO>> consulta) {
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            return consulta.get();
        }
        return entradas.publicados().get("publicados", clave -> consulta.get());
    }

    public List<EventoResumenResponseDTO> buscar(TipoEvento tipo, Long organizadorId, Long participanteId,
            String ciudad, LocalDateTime desde, LocalDateTime hasta, String texto, OrdenEvento ordenar,
            Supplier<List<EventoResumenResponseDTO>> consulta) {
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            return consulta.get();
        }

        var clave = new Filtros(tipo, organizadorId, participanteId, ciudad, desde, hasta, texto, ordenar);
        return entradas.busquedas().get(clave, filtro -> consulta.get());
    }

    public void invalidarDespuesDeConfirmar() {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            invalidarTodo();
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                invalidarTodo();
            }
        });
    }

    private void invalidarTodo() {
        // Las lecturas en curso conservan una referencia a las cachés anteriores.
        entradas = nuevasEntradas();
    }

    private Entradas nuevasEntradas() {
        return new Entradas(crearCache(1), crearCache(1024), crearCache(256));
    }

    private <K, V> Cache<K, V> crearCache(long maximo) {
        return Caffeine.newBuilder().maximumSize(maximo).expireAfterWrite(ttl).build();
    }
}