
package com.ticketya.reservas.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ticketya.reservas.entities.BloqueoEvento;

import jakarta.persistence.LockModeType;

public interface BloqueoEventoRepository
                extends JpaRepository<BloqueoEvento, Long> {

        @Modifying(flushAutomatically = true)
        @Query(value = """
                        INSERT INTO bloqueo_evento (evento_id, cancelado)
                        VALUES (:eventoId, 0)
                        ON DUPLICATE KEY UPDATE evento_id = evento_id
                        """, nativeQuery = true)
        void crearSiNoExiste(
                        @Param("eventoId") Long eventoId);

        @Lock(LockModeType.PESSIMISTIC_WRITE)
        @Query("""
                            SELECT b
                            FROM BloqueoEvento b
                            WHERE b.eventoId = :eventoId
                        """)
        Optional<BloqueoEvento> bloquear(
                        @Param("eventoId") Long eventoId);
}
