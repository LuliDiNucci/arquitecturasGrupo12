package msvc.msvceventoslugares.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import msvc.msvceventoslugares.entity.Sector;

@Repository
public interface SectorRepository extends JpaRepository<Sector, Long> {

    List<Sector> findByIdLugarId(Long idLugar);
}
