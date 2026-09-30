package edu.isistan.repository;

import javax.persistence.EntityManager;

import edu.isistan.modelo.Carrera;
import edu.isistan.modelo.Estudiante;
import edu.isistan.modelo.Inscripcion;
import edu.isistan.util.JPAUtil;

public class MySQLInscripcion implements InscripcionRepository {

    // b) Matricular un estudiante en una carrera
    @Override
    public void matricular(
            int id,
            int dniEstudiante,
            int idCarrera,
            int anioInscripcion,
            int anioGraduacion,
            int antiguedad) {

        EntityManager em = JPAUtil.getEntityManager();

        try {

            em.getTransaction().begin();

            Estudiante estudiante
                    = em.find(Estudiante.class, dniEstudiante);

            Carrera carrera
                    = em.find(Carrera.class, idCarrera);

            if (estudiante == null) {
                throw new RuntimeException(
                        "No existe el estudiante con DNI: "
                        + dniEstudiante
                );
            }

            if (carrera == null) {
                throw new RuntimeException(
                        "No existe la carrera con ID: "
                        + idCarrera
                );
            }

            Inscripcion inscripcion = new Inscripcion(
                    id,
                    estudiante,
                    carrera,
                    anioInscripcion,
                    anioGraduacion,
                    antiguedad
            );

            em.persist(inscripcion);

            em.getTransaction().commit();

        } catch (Exception e) {

            e.printStackTrace();

            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }

        } finally {

            em.close();
        }
    }
}
