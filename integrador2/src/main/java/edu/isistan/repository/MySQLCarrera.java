package edu.isistan.repository;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import edu.isistan.dto.CarreraInscriptosDTO;

public class MySQLCarrera implements CarreraRepository {

    private EntityManagerFactory emf;

    public MySQLCarrera() {
        emf = Persistence.createEntityManagerFactory("Example");
    }

    // f) Recuperar carreras con estudiantes inscriptos
    // y ordenar por cantidad de inscriptos
    @Override
    public List<CarreraInscriptosDTO> carrerasOrdenadasPorInscriptos() {

        EntityManager em = emf.createEntityManager();

        try {

            List<CarreraInscriptosDTO> carreras = em.createQuery(
                    "SELECT new edu.isistan.dto.CarreraInscriptosDTO("
                    + "c.nombre, COUNT(i)) "
                    + "FROM Carrera c "
                    + "JOIN c.inscripciones i "
                    + "GROUP BY c.nombre "
                    + "ORDER BY COUNT(i) DESC",
                    CarreraInscriptosDTO.class
            ).getResultList();

            return carreras;

        } catch (Exception e) {

            e.printStackTrace();
            return null;

        } finally {

            em.close();
        }
    }
}
