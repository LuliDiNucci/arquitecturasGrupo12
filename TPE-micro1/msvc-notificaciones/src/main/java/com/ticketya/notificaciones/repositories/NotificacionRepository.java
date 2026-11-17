package com.ticketya.notificaciones.repositories;

import com.ticketya.notificaciones.entities.EstadoNotificacion;
import com.ticketya.notificaciones.entities.Notificacion;
import com.ticketya.notificaciones.entities.TipoNotificacion;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface NotificacionRepository extends JpaRepository<Notificacion, Long> {

    Optional<Notificacion> findByTipoAndReferenciaIdAndVersionEventoAndClienteId(
            TipoNotificacion tipo, long referenciaId, long versionEvento, long clienteId);

    @Query("""
            select n.id from Notificacion n
            where n.estado = :estado and n.proximoIntentoEn <= :ahora
            order by n.proximoIntentoEn, n.id
            """)
    List<Long> buscarPendientes(@Param("estado") EstadoNotificacion estado,
                                @Param("ahora") Instant ahora, Pageable pagina);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select n from Notificacion n where n.id = :id")
    Optional<Notificacion> buscarParaProcesar(@Param("id") long id);
}
