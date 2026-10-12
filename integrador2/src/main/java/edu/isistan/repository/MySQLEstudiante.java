package edu.isistan.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import edu.isistan.dto.EstudianteDTO;
import edu.isistan.modelo.Estudiante;

public interface EstudianteRepository extends JpaRepository<Estudiante, Integer> {

    // c) Recuperar todos los estudiantes
    // ordenados por apellido
    @Query("SELECT new edu.isistan.dto.EstudianteDTO("
         + "e.dni, e.nombre, e.apellido, e.edad, "
         + "e.genero, e.ciudad, e.LU) "
         + "FROM Estudiante e "
         + "ORDER BY e.apellido ASC")
    List<EstudianteDTO> recuperarTodos();

    // d) Recuperar estudiante por LU
    @Query("SELECT new edu.isistan.dto.EstudianteDTO("
         + "e.dni, e.nombre, e.apellido, e.edad, "
         + "e.genero, e.ciudad, e.LU) "
         + "FROM Estudiante e "
         + "WHERE e.LU = :LU")
    EstudianteDTO recuperarPorLU(@Param("LU") int LU);

    // e) Recuperar estudiantes por género
    @Query("SELECT new edu.isistan.dto.EstudianteDTO("
         + "e.dni, e.nombre, e.apellido, e.edad, "
         + "e.genero, e.ciudad, e.LU) "
         + "FROM Estudiante e "
         + "WHERE e.genero = :genero")
    List<EstudianteDTO> recuperarPorGenero(@Param("genero") String genero);

    // g) Recuperar estudiantes de una carrera
    // filtrados por ciudad
    @Query("SELECT new edu.isistan.dto.EstudianteDTO("
         + "i.estudiante.dni, "
         + "i.estudiante.nombre, "
         + "i.estudiante.apellido, "
         + "i.estudiante.edad, "
         + "i.estudiante.genero, "
         + "i.estudiante.ciudad, "
         + "i.estudiante.LU) "
         + "FROM Inscripcion i "
         + "WHERE i.carrera.nombre = :nombreCarrera "
         + "AND i.estudiante.ciudad = :ciudad")
    List<EstudianteDTO> recuperarPorCarreraYCiudad(
            @Param("nombreCarrera") String nombreCarrera,
            @Param("ciudad") String ciudad);
}
