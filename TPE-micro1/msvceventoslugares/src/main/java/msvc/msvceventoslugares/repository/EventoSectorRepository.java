package msvc.msvceventoslugares.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import msvc.msvceventoslugares.entity.PrecioSector;

@Repository
public interface EventoSectorRepository extends JpaRepository<PrecioSector, Long> {

    Optional<PrecioSector> findByEventoIdAndSectorId(Long idEvento, Long idSector);
}
