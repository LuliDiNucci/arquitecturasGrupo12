package ar.edu.unicen.ticketya.eventos.repository;
import ar.edu.unicen.ticketya.eventos.model.EventoParticipante;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface EventoParticipanteRepository extends JpaRepository<EventoParticipante, Long> {
    List<EventoParticipante> findByEventoId(Long eventoId);
    List<EventoParticipante> findByEventoIdIn(List<Long> ids);
    void deleteByEventoId(Long eventoId);
}
