package msvc.msvceventoslugares.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import msvc.msvceventoslugares.Enums.TipoEvento;
import msvc.msvceventoslugares.entity.Evento;

@Repository
public interface EventoRepository
        extends JpaRepository<Evento, Long> {

    @Query("""
        SELECT DISTINCT e
        FROM Evento e
        LEFT JOIN e.participantes p
        WHERE e.estadoEvento = 'PUBLICADO'
        AND (:tipo IS NULL OR e.tipoEvento = :tipo)
        AND (:idOrganizador IS NULL OR e.organizador.id = :idOrganizador)
        AND (:idParticipante IS NULL OR p.id = :idParticipante)
        AND (:ciudad IS NULL OR LOWER(e.lugar.ciudad) = LOWER(:ciudad))
        AND (:fechaDesde IS NULL OR e.fechaYHora >= :fechaDesde)
        AND (:fechaHasta IS NULL OR e.fechaYHora <= :fechaHasta)
    """)
    List<Evento> buscarEventos(
        @Param("tipo") TipoEvento tipo,
        @Param("idOrganizador") Long idOrganizador,
        @Param("idParticipante") Long idParticipante,
        @Param("ciudad") String ciudad,
        @Param("fechaDesde") LocalDateTime fechaDesde,
        @Param("fechaHasta") LocalDateTime fechaHasta
    );
}
