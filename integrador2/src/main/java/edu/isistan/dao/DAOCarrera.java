package edu.isistan.dao;

import java.util.List;

import edu.isistan.dto.CarreraInscriptosDTO;

public interface DAOCarrera {

    // f) Recuperar carreras con estudiantes inscriptos
    // ordenadas por cantidad de inscriptos
    List<CarreraInscriptosDTO> carrerasOrdenadasPorInscriptos();
}
