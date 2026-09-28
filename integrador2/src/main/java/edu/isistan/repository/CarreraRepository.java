package edu.isistan.repository;

import java.util.List;

import edu.isistan.dto.CarreraInscriptosDTO;

public interface CarreraRepository {

    // f) Recuperar carreras con estudiantes inscriptos
    // ordenadas por cantidad de inscriptos
    List<CarreraInscriptosDTO> carrerasOrdenadasPorInscriptos();
}
