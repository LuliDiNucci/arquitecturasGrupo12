package com.ticketya.reservas.repositories;

import com.ticketya.reservas.entities.ClaveIdempotenciaBloqueo;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface ClaveIdempotenciaBloqueoRepository extends JpaRepository<ClaveIdempotenciaBloqueo, String> {
    @Modifying(flushAutomatically = true)
    @Query(value = "INSERT IGNORE INTO clave_idempotencia_bloqueo (clave) VALUES (:clave)", nativeQuery = true)
    void crearSiNoExiste(@Param("clave") String clave);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT b FROM ClaveIdempotenciaBloqueo b WHERE b.clave = :clave")
    Optional<ClaveIdempotenciaBloqueo> bloquear(@Param("clave") String clave);
}
