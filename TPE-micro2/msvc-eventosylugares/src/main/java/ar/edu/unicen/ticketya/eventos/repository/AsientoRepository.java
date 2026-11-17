package ar.edu.unicen.ticketya.eventos.repository;
import ar.edu.unicen.ticketya.eventos.model.Asiento;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface AsientoRepository extends JpaRepository<Asiento, Long> {
    long countBySectorId(Long sectorId);
    List<Asiento> findBySectorIdOrderByFilaAscNumeroAsc(Long sectorId);
    List<Asiento> findBySectorIdIn(List<Long> sectorIds);
    boolean existsBySectorIdAndFilaIgnoreCaseAndNumero(Long sectorId, String fila, int numero);
}
