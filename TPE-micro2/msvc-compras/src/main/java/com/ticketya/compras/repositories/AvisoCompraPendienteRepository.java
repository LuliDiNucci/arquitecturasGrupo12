package com.ticketya.compras.repositories;

import com.ticketya.compras.entities.AvisoCompraPendiente;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.time.Instant;
import java.util.List;

public interface AvisoCompraPendienteRepository extends JpaRepository<AvisoCompraPendiente, Long> {
    boolean existsByCompraId(Long compraId);
    @Query("select a from AvisoCompraPendiente a where a.estado = 'PENDIENTE' and a.proximoIntentoEn <= :ahora order by a.id")
    List<AvisoCompraPendiente> pendientes(Instant ahora, Pageable pagina);
}
