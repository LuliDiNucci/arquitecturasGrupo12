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

    //consigna C
    @Override
    public List<Estudiante> recuperarTodos() {

        EntityManager em = Persistence
                .createEntityManagerFactory("Example")
                .createEntityManager();

        List<Estudiante> estudiantes = em.createQuery(
                "SELECT e FROM Estudiante e ORDER BY e.apellido ASC",
                Estudiante.class
        ).getResultList();

        em.close();

        return estudiantes;
    }
}
