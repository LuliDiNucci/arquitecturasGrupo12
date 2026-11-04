package msvc.msvceventoslugares.repository;

<<<<<<< HEAD
import org.springframework.data.jpa.repository.JpaRepository;

import msvc.msvceventoslugares.entity.Sector;

public interface SectorRepository extends JpaRepository<Sector, Long> {
    
=======
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import msvc.msvceventoslugares.entity.Sector;

@Repository
public interface SectorRepository extends JpaRepository<Sector, Long> {

    List<Sector> findByIdLugarId(Long idLugar);
>>>>>>> ec5c1a8504968268c23a252810d19f3f960710a8
}
