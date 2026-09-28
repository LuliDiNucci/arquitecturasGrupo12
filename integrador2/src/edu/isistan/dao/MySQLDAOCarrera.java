package edu.isistan.dao;

/*f) recuperar las carreras con estudiantes inscriptos, y ordenar por cantidad de inscriptos. (JOIN CON inscripcion)
3)Generar el reporte (JOIN DE LAS TRES TABLAS)
*/
public class MySQLDAOCarrera implements DAOCarrera {
    // f) recuperar las carreras con estudiantes inscriptos, y ordenar por cantidad
    // de inscriptos.
    public List<Carrera> carrerasOrdenadasPorInscriptos() {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("Example");
        EntityManager em = emf.createEntityManager();

        String jpql = """
                SELECT c
                FROM Carrera c
                JOIN c.estudiantes e
                GROUP BY c
                ORDER BY COUNT(e) DESC
                """;

        List<Carrera> carreras = em.createQuery(jpql, Carrera.class)
                .getResultList();

        em.close();
        emf.close();

        return carreras;
    }
}
