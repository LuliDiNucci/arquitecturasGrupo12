package edu.isistan.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.persistence.EntityManager;

import edu.isistan.dto.CarreraInscriptosDTO;
import edu.isistan.dto.ReporteCarreraDTO;
import edu.isistan.util.JPAUtil;

public class MySQLCarrera implements CarreraRepository {

    // f) Recuperar carreras con estudiantes inscriptos
    // y ordenar por cantidad de inscriptos
    @Override
    public List<CarreraInscriptosDTO> carrerasOrdenadasPorInscriptos() {

        EntityManager em = JPAUtil.getEntityManager();

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

        EntityManager em = JPAUtil.getEntityManager();

        try {

            // CONSULTA DE INSCRIPTOS
            String jpqlI
                    = "SELECT new edu.isistan.dto.ReporteCarreraDTO("
                    + "c.nombre, i.inscripcion, COUNT(i), true) "
                    + "FROM Inscripcion i "
                    + "JOIN i.carrera c "
                    + "GROUP BY c.nombre, i.inscripcion "
                    + "ORDER BY c.nombre, i.inscripcion";

            List<ReporteCarreraDTO> inscriptos
                    = em.createQuery(
                            jpqlI,
                            ReporteCarreraDTO.class
                    ).getResultList();

            // CONSULTA DE EGRESADOS
            String jpqlE
                    = "SELECT new edu.isistan.dto.ReporteCarreraDTO("
                    + "c.nombre, i.graduacion, COUNT(i), false) "
                    + "FROM Inscripcion i "
                    + "JOIN i.carrera c "
                    + "WHERE i.graduacion > 0 "
                    + "GROUP BY c.nombre, i.graduacion "
                    + "ORDER BY c.nombre, i.graduacion";

            List<ReporteCarreraDTO> egresados
                    = em.createQuery(
                            jpqlE,
                            ReporteCarreraDTO.class
                    ).getResultList();

            // COMBINAR LOS RESULTADOS
            Map<String, Map<Integer, ReporteCarreraDTO>> reporte
                    = new TreeMap<>();

            // INCORPORAR INSCRIPTOS
            for (ReporteCarreraDTO dto : inscriptos) {

                reporte.putIfAbsent(
                        dto.getCarrera(),
                        new TreeMap<>()
                );

                reporte.get(dto.getCarrera()).put(
                        dto.getAnio(),
                        dto
                );
            }

            // INCORPORAR EGRESADOS
            for (ReporteCarreraDTO dto : egresados) {

                reporte.putIfAbsent(
                        dto.getCarrera(),
                        new TreeMap<>()
                );

                Map<Integer, ReporteCarreraDTO> anios
                        = reporte.get(dto.getCarrera());

                if (anios.containsKey(dto.getAnio())) {

                    anios.get(dto.getAnio())
                            .setEgresados(dto.getEgresados());

                } else {

                    anios.put(
                            dto.getAnio(),
                            dto
                    );
                }
            }

            // CONVERTIR A LISTA
            List<ReporteCarreraDTO> resultado
                    = new ArrayList<>();

            for (Map<Integer, ReporteCarreraDTO> anios
                    : reporte.values()) {

                resultado.addAll(anios.values());
            }

            return resultado;

        } finally {

            em.close();
        }
    }
}
