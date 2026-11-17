package ar.edu.unicen.ticketya.eventos.repository;

import ar.edu.unicen.ticketya.eventos.model.AvisoEventoPendiente;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface AvisoEventoPendienteRepository extends JpaRepository<AvisoEventoPendiente, Long> {

    @Query("""
            select a.id from AvisoEventoPendiente a
            where a.entregado = false and a.intentarEn <= :ahora
            order by a.intentarEn, a.id
            """)
    List<Long> buscarPendientes(@Param("ahora") Instant ahora, Pageable pagina);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from AvisoEventoPendiente a where a.id = :id")
    Optional<AvisoEventoPendiente> buscarParaDespachar(@Param("id") Long id);
}
