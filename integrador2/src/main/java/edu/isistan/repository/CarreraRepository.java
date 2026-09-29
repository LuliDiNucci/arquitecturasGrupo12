package edu.isistan.repository;

import java.util.List;

import edu.isistan.dto.CarreraInscriptosDTO;
import edu.isistan.dto.ReporteCarreraDTO;

public interface CarreraRepository {

    // f) Recuperar carreras con estudiantes inscriptos
    // ordenadas por cantidad de inscriptos
    List<CarreraInscriptosDTO> carrerasOrdenadasPorInscriptos();

    List<ReporteCarreraDTO> generarReporte();
}
