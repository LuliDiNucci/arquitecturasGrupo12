package com.ticketya.reservas.repositories;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ticketya.reservas.entities.FinalizacionFilaPendiente;

import jakarta.persistence.LockModeType;

public interface FinalizacionFilaPendienteRepository
        extends JpaRepository<FinalizacionFilaPendiente, Long> {

    List<FinalizacionFilaPendiente> findByEstadoAndProximoIntentoEnLessThanEqual(
            String estado,
            Instant fecha,
            Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT f
            FROM FinalizacionFilaPendiente f
            WHERE f.id = :id
            """)
    Optional<FinalizacionFilaPendiente> bloquearPorId(
            @Param("id") Long id);
}