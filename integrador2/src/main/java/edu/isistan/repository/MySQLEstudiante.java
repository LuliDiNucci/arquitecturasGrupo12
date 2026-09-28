package edu.isistan.repository;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import edu.isistan.dto.EstudianteDTO;
import edu.isistan.modelo.Estudiante;

public class MySQLEstudiante implements EstudianteRepository {

    private EntityManagerFactory emf;

    public MySQLEstudiante() {
        emf = Persistence.createEntityManagerFactory("Example");
    }

    // a) Dar de alta un estudiante
    @Override
    public void insertarEstudiante(Estudiante estudiante) {

        EntityManager em = emf.createEntityManager();

        try {

            em.getTransaction().begin();
            em.persist(estudiante);
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

// c) Recuperar todos los estudiantes
// ordenados por apellido
    @Override
    public List<EstudianteDTO> recuperarTodos() {

        EntityManager em = emf.createEntityManager();

        try {

            List<EstudianteDTO> estudiantes = em.createQuery(
                    "SELECT new edu.isistan.dto.EstudianteDTO("
                    + "e.dni, e.nombre, e.apellido, e.edad, "
                    + "e.genero, e.ciudad, e.LU) "
                    + "FROM Estudiante e "
                    + "ORDER BY e.apellido ASC",
                    EstudianteDTO.class
            ).getResultList();

            return estudiantes;

        } catch (Exception e) {

            e.printStackTrace();
            return null;

        } finally {

            em.close();
        }
    }

// d) Recuperar estudiante por LU
    @Override
    public EstudianteDTO recuperarPorLU(int LU) {

        EntityManager em = emf.createEntityManager();

        try {

            EstudianteDTO estudiante = em.createQuery(
                    "SELECT new edu.isistan.dto.EstudianteDTO("
                    + "e.dni, e.nombre, e.apellido, e.edad, "
                    + "e.genero, e.ciudad, e.LU) "
                    + "FROM Estudiante e "
                    + "WHERE e.LU = :LU",
                    EstudianteDTO.class
            )
                    .setParameter("LU", LU)
                    .getSingleResult();

            return estudiante;

        } catch (Exception e) {

            e.printStackTrace();
            return null;

        } finally {

            em.close();
        }
    }

// e) Recuperar estudiantes por género
    @Override
    public List<EstudianteDTO> recuperarPorGenero(String genero) {

        EntityManager em = emf.createEntityManager();

        try {

            List<EstudianteDTO> estudiantes = em.createQuery(
                    "SELECT new edu.isistan.dto.EstudianteDTO("
                    + "e.dni, e.nombre, e.apellido, e.edad, "
                    + "e.genero, e.ciudad, e.LU) "
                    + "FROM Estudiante e "
                    + "WHERE e.genero = :genero",
                    EstudianteDTO.class
            )
                    .setParameter("genero", genero)
                    .getResultList();

            return estudiantes;

        } catch (Exception e) {

            e.printStackTrace();
            return null;

        } finally {

            em.close();
        }
    }

// g) Recuperar estudiantes de una carrera
// filtrados por ciudad
    @Override
    public List<EstudianteDTO> recuperarPorCarreraYCiudad(
            String nombreCarrera,
            String ciudad) {

        EntityManager em = emf.createEntityManager();

        try {

            List<EstudianteDTO> estudiantes = em.createQuery(
                    "SELECT new edu.isistan.dto.EstudianteDTO("
                    + "i.estudiante.dni, "
                    + "i.estudiante.nombre, "
                    + "i.estudiante.apellido, "
                    + "i.estudiante.edad, "
                    + "i.estudiante.genero, "
                    + "i.estudiante.ciudad, "
                    + "i.estudiante.LU) "
                    + "FROM Inscripcion i "
                    + "WHERE i.carrera.nombre = :nombreCarrera "
                    + "AND i.estudiante.ciudad = :ciudad",
                    EstudianteDTO.class
            )
                    .setParameter("nombreCarrera", nombreCarrera)
                    .setParameter("ciudad", ciudad)
                    .getResultList();

            return estudiantes;

        } catch (Exception e) {

            e.printStackTrace();
            return null;

        } finally {

            em.close();
        }
    }

}
