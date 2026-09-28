package edu.isistan.repository;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import edu.isistan.modelo.Inscripcion;

import edu.isistan.modelo.Carrera;

import edu.isistan.modelo.Estudiante;

public class MySQLInscripcion implements InscripcionRepository {

    private EntityManagerFactory emf;

    public MySQLInscripcion() {
        emf = Persistence.createEntityManagerFactory("Example");
    }

    // b) Matricular un estudiante en una carrera
    @Override
    public void matricular(
            int id,
            int dniEstudiante,
            int idCarrera,
            int anioInscripcion,
            int anioGraduacion,
            int antiguedad) {

        EntityManager em = emf.createEntityManager();

        try {

            Estudiante estudiante
                    = em.find(Estudiante.class, dniEstudiante);

            Carrera carrera
                    = em.find(Carrera.class, idCarrera);

            Inscripcion inscripcion = new Inscripcion(
                    id,
                    estudiante,
                    carrera,
                    anioInscripcion,
                    anioGraduacion,
                    antiguedad
            );

            em.getTransaction().begin();

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
