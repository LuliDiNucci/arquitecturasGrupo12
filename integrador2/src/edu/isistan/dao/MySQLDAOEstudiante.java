package edu.isistan.dao;

import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.Persistence;

/*
a)insertar (dar de alta) un estudiante

c) recuperar todos los estudiantes, y especificar algún criterio de ordenamiento simple.
d) recuperar un estudiante, en base a su número de libreta universitaria.
e) recuperar todos los estudiantes, en base a su género.
g) recuperar los estudiantes de una determinada carrera, filtrado por ciudad de residencia. (JOIN CON inscripcion y carrera) */
public class MySQLDAOEstudiante implements DAOEstudiante {

    // a) insertar (dar de alta) un estudiante

    public void insertarEstudiante(Estudiante e) {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("Example");
        EntityManager em = emf.createEntityManager();

        em.getTransaction().begin();
        em.persist(e);
        em.getTransaction().commit();

        em.close();
        emf.close();
    }

    // consigna C
    @Override
    public List<Estudiante> recuperarTodos() {

        EntityManager em = Persistence
                .createEntityManagerFactory("Example")
                .createEntityManager();

        List<Estudiante> estudiantes = em.createQuery(
                "SELECT e FROM Estudiante e ORDER BY e.apellido ASC",
                Estudiante.class).getResultList();

        em.close();

        return estudiantes;
    }

    // consigna G
    @Override 
    public List<Estudiante> recuperarPorCarreraYCiudad(String nombreCarrera, String ciudad) {
        EntityManager em = Persistence
            .createEntityManagerFactory("Example")
            .createEntityManager();
        
        List<Estudiante> estudiantes = em.createQuery(
            "SELECT i.estudiante " +
            "FROM Inscripcion i " +
            "WHERE i.carrera.nombre = :nombreCarrera " +
            "AND i.estudiante.ciudad = :ciudad",
            Estudiante.class
        )
        .setParameter("nombreCarrera", nombreCarrera)
        .setParameter("ciudad", ciudad)
        .getResultList();

        em.close();

        return estudiantes;
    }

}
