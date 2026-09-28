package edu.isistan.dao;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

public class MySQLDAOInscripcion implements DAOInscripcion {

    private EntityManagerFactory emf;

    public MySQLDAOInscripcion() {
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
