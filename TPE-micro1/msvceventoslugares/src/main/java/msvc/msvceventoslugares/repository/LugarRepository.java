package msvc.msvceventoslugares.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import msvc.msvceventoslugares.entity.Lugar;

@Repository
public interface LugarRepository extends JpaRepository<Lugar, Long> {

}
