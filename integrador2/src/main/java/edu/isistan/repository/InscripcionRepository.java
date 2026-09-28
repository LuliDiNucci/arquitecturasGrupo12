package edu.isistan.repository;

public interface InscripcionRepository {

    // b) Matricular un estudiante en una carrera
    void matricular(
            int id,
            int dniEstudiante,
            int idCarrera,
            int anioInscripcion,
            int anioGraduacion,
            int antiguedad
    );
}