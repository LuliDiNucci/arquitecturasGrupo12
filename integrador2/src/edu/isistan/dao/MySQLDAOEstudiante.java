package edu.isistan.dao;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;

/*
hay q crear el entitymanager en cada metodo o globalmente??
*/

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

    //consigna E
    @Override
    public List<Estudiante> recuperarPorGenero(String genero){
        EntityManagerFactory emf= Persistence
          .createEntityManagerFactory("Example");
        EntityManager em= emf.createEntityManager();

        String jpql= "SELECT e FROM Estudiante e WHERE e.genero= :genero";

        TypedQuery<Estudiante> query = em.createQuery(jpql, Estudiante.class);

        query.setParameter("genero", genero);

        List<Estudiante> estudiantes = query.getResultList();

    
        em.close();
        emf.close();
        
        return estudiantes;
    }

    //consigna d
    @Override
    public Estudiante recuperarLU(int l) {

        EntityManagerFactory emf =
            Persistence.createEntityManagerFactory("Example");

        EntityManager em = emf.createEntityManager();

        String jpql =
            "SELECT e FROM Estudiante e WHERE e.LU = :l";

        TypedQuery<Estudiante> query =
            em.createQuery(jpql, Estudiante.class);

        query.setParameter("l", l);

        List<Estudiante> estudiantes = query.getResultList();

        em.close();
        emf.close();

        if (estudiantes.isEmpty()) {
            return null;
        }

        return estudiantes.get(0); //preguntar si se devuelve asi uno solo!!
    }


}
