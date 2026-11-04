package msvc.msvceventoslugares.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import msvc.msvceventoslugares.entity.Sector;

public interface SectorRepository extends JpaRepository<Sector, Long> {
    
}
