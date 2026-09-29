package edu.isistan.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import edu.isistan.dto.CarreraInscriptosDTO;
import edu.isistan.dto.ReporteCarreraDTO;

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


    

    // 3 - REPORTE DE CARRERAS

    @Override
    public List<ReporteCarreraDTO> generarReporte() {

        EntityManager em = emf.createEntityManager();

        try {

            // CONSULTA DE INSCRIPTOS

            String jpqlI =
                    "SELECT c.nombre, i.inscripcion, COUNT(i) "
                    + "FROM Inscripcion i "
                    + "JOIN i.carrera c "
                    + "GROUP BY c.nombre, i.inscripcion "
                    + "ORDER BY c.nombre, i.inscripcion";

            List<Object[]> inscriptos = em.createQuery(
                    jpqlI, Object[].class
            ).getResultList();
            // CONSULTA DE EGRESADOS

            String jpqlE =
                    "SELECT c.nombre, i.graduacion, COUNT(i) "
                    + "FROM Inscripcion i "
                    + "JOIN i.carrera c "
                    + "WHERE i.graduacion > 0 "
                    + "GROUP BY c.nombre, i.graduacion "
                    + "ORDER BY c.nombre, i.graduacion";

            List<Object[]> egresados = em.createQuery(
                    jpqlE, Object[].class
            ).getResultList();


            // COMBINAR LOS RESULTADOS

            Map<String, Map<Integer, ReporteCarreraDTO>> reporte =
                    new TreeMap<>();


            // Incorporar inscriptos

            for (Object[] fila : inscriptos) {

                String carrera = (String) fila[0];
                int anio = (Integer) fila[1];
                Long cantidad = (Long) fila[2];

                reporte.putIfAbsent(
                        carrera,
                        new TreeMap<>()
                );

                reporte.get(carrera).putIfAbsent(
                        anio,
                        new ReporteCarreraDTO(carrera, anio)
                );

                reporte.get(carrera)
                        .get(anio)
                        .setInscriptos(cantidad);
            }


            // Incorporar egresados

            for (Object[] fila : egresados) {

                String carrera = (String) fila[0];
                int anio = (Integer) fila[1];
                Long cantidad = (Long) fila[2];

                reporte.putIfAbsent(
                        carrera,
                        new TreeMap<>()
                );

                reporte.get(carrera).putIfAbsent(
                        anio,
                        new ReporteCarreraDTO(carrera, anio)
                );

                reporte.get(carrera)
                        .get(anio)
                        .setEgresados(cantidad);
            }


            // CONVERTIR A LISTA DE DTO

            List<ReporteCarreraDTO> resultado =
                    new ArrayList<>();

            for (Map<Integer, ReporteCarreraDTO> anios
                    : reporte.values()) {

                resultado.addAll(anios.values());
            }

            return resultado;

        } finally { //es para q pase lp q pase se ejecute esto (osea q cierre el entitymanager)
            em.close();
        }
    }

}
