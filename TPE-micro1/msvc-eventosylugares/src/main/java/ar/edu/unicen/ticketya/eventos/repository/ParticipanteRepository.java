package ar.edu.unicen.ticketya.eventos.repository;
import org.springframework.data.jpa.repository.JpaRepository;

import ar.edu.unicen.ticketya.eventos.model.Participante;
public interface ParticipanteRepository extends JpaRepository<Participante, Long> {}
