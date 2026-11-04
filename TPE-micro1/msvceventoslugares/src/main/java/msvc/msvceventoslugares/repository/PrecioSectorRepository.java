package msvc.msvceventoslugares.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import msvc.msvceventoslugares.entity.PrecioSector;

@Repository
public interface PrecioSectorRepository extends JpaRepository<PrecioSector, Long> {

    List<PrecioSector> findByEventoId(Long idEvento);
}
