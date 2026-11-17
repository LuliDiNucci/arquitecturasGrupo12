package ar.edu.unicen.ticketya.eventos.repository;
import ar.edu.unicen.ticketya.eventos.model.NotificacionCancelacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;
public interface NotificacionCancelacionRepository extends JpaRepository<NotificacionCancelacion, Long> {
    Optional<NotificacionCancelacion> findByEventoId(Long eventoId);
    @Query("select n from NotificacionCancelacion n where n.reservasNotificadas = false or n.entradasNotificadas = false or n.comprasNotificadas = false or n.comprasNotificadas is null")
    List<NotificacionCancelacion> pendientes();
}
