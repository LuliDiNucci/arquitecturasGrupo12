package ar.edu.unicen.ticketya.eventos.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import ar.edu.unicen.ticketya.eventos.model.PrecioSector;

public interface PrecioSectorRepository extends JpaRepository<PrecioSector, Long> {
    List<PrecioSector> findByEventoId(Long eventoId);

    List<PrecioSector> findByEventoIdIn(List<Long> eventoIds);

    Optional<PrecioSector> findByEventoIdAndSectorId(
            Long eventoId,
            Long sectorId);

}
