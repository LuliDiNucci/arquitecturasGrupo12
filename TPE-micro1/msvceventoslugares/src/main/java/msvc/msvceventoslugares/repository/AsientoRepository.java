package msvc.msvceventoslugares.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import msvc.msvceventoslugares.entity.Asiento;

@Repository
public interface AsientoRepository extends JpaRepository<Asiento, Long> {

    List<Asiento> findBySectorId(Long idSector);
}
