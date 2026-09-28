package edu.isistan.dao;

import javax.persistence.EntityManager;
import javax.persistence.Persistence;

/*
b) matricular un estudiante en una carrera
f) recuperar las carreras con estudiantes inscriptos, y ordenar por cantidad de inscriptos. (JOIN CON CARRERA)
g) recuperar los estudiantes de una determinada carrera, filtrado por ciudad de residencia. (JOIN CON ESTUDIANTE)*/

public class MySQLDAOInscripcion implements DAOInscripcion {

    @Override 
    public void matricular(int dniEstudiante, int idCarrera, int antiguedad) {
        EntityManager em = Persistence.createEntityManagerFactory("Example").createEntityManager();

        Estudiante estudiante = em.find(Estudiante.class, dniEstudiante);
        Carrera carrera = em.find(Carrera.class, idCarrera);
        Inscripcion inscripcion = new Inscripcion(estudiante, carrera, antiguedad);

        em.getTransaction().begin();
        em.persist(inscripcion);
        em.getTransaction().commit();
        em.close();
    }
}
