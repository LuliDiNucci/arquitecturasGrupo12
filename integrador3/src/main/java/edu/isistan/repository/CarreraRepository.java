package edu.isistan.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import edu.isistan.dto.CarreraInscriptosDTO;
import edu.isistan.dto.ReporteCarreraDTO;
import edu.isistan.modelo.Carrera;

public interface CarreraRepository extends JpaRepository<Carrera, Integer> {

    // f) Recuperar carreras con estudiantes inscriptos
    // y ordenar por cantidad de inscriptos de mayor a menor
    @Query("SELECT new edu.isistan.dto.CarreraInscriptosDTO("
            + "c.nombre, COUNT(i)) "
            + "FROM Carrera c "
            + "JOIN c.inscripciones i "
            + "GROUP BY c.nombre "
            + "ORDER BY COUNT(i) DESC")
    List<CarreraInscriptosDTO> carrerasOrdenadasPorInscriptos();

    // h) Obtener inscriptos agrupados por carrera y año
    @Query("SELECT new edu.isistan.dto.ReporteCarreraDTO("
            + "c.nombre, i.inscripcion, COUNT(i), true) "
            + "FROM Inscripcion i "
            + "JOIN i.carrera c "
            + "GROUP BY c.nombre, i.inscripcion "
            + "ORDER BY c.nombre, i.inscripcion")
    List<ReporteCarreraDTO> obtenerInscriptosPorAnio();

    // h) Obtener egresados agrupados por carrera y año
    @Query("SELECT new edu.isistan.dto.ReporteCarreraDTO("
            + "c.nombre, i.graduacion, COUNT(i), false) "
            + "FROM Inscripcion i "
            + "JOIN i.carrera c "
            + "WHERE i.graduacion > 0 "
            + "GROUP BY c.nombre, i.graduacion "
            + "ORDER BY c.nombre, i.graduacion")
    List<ReporteCarreraDTO> obtenerEgresadosPorAnio();
}
