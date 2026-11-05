package ar.edu.unicen.ticketya.eventos.repository;
import ar.edu.unicen.ticketya.eventos.model.PrecioSector;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface PrecioSectorRepository extends JpaRepository<PrecioSector, Long> {
    List<PrecioSector> findByEventoId(Long eventoId);
    List<PrecioSector> findByEventoIdIn(List<Long> eventoIds);
}
