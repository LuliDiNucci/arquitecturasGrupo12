package edu.isistan.repository;

import java.util.List;

import edu.isistan.dto.EstudianteDTO;
import edu.isistan.modelo.Estudiante;

public interface EstudianteRepository {

    // a) Dar de alta un estudiante
    void insertarEstudiante(Estudiante estudiante);

    // c) Recuperar todos los estudiantes
    List<EstudianteDTO> recuperarTodos();

    // d) Recuperar estudiante por LU
    EstudianteDTO recuperarPorLU(int LU);

    // e) Recuperar estudiantes por género
    List<EstudianteDTO> recuperarPorGenero(String genero);

    // g) Recuperar estudiantes de una carrera filtrados por ciudad
    List<EstudianteDTO> recuperarPorCarreraYCiudad(
            String nombreCarrera,
            String ciudad);
}
