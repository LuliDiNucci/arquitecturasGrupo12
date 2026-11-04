package msvc.msvceventoslugares.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import msvc.msvceventoslugares.entity.Participante;

@Repository
public interface ParticipanteRepository extends JpaRepository<Participante, Long> {

}
