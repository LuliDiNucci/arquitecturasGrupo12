package ar.edu.unicen.ticketya.eventos.repository;
import ar.edu.unicen.ticketya.eventos.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import java.util.List;
public interface EventoRepository extends JpaRepository<Evento, Long>, JpaSpecificationExecutor<Evento> {
    boolean existsByLugarIdAndEstadoIn(Long lugarId, List<EstadoEvento> estados);
    List<Evento> findAllByOrderByFechaHoraAsc();
}
