package ar.edu.unicen.ticketya.eventos.repository;
import ar.edu.unicen.ticketya.eventos.model.Sector;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
public interface SectorRepository extends JpaRepository<Sector, Long> {
    List<Sector> findByLugarIdOrderById(Long lugarId);
    boolean existsByLugarIdAndNombreIgnoreCase(Long lugarId, String nombre);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from Sector s where s.id = :id")
    Optional<Sector> bloquear(@Param("id") Long id);
}
