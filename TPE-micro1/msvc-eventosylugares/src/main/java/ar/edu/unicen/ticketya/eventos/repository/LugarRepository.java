package ar.edu.unicen.ticketya.eventos.repository;
import ar.edu.unicen.ticketya.eventos.model.Lugar;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import jakarta.persistence.LockModeType;
import org.springframework.data.repository.query.Param;
import java.util.Optional;
public interface LugarRepository extends JpaRepository<Lugar, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select l from Lugar l where l.id = :id")
    Optional<Lugar> bloquear(@Param("id") Long id);
}
