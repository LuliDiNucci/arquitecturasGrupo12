package edu.isistan;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

import javax.persistence.EntityManager;

import edu.isistan.dto.CarreraInscriptosDTO;
import edu.isistan.dto.EstudianteDTO;
import edu.isistan.dto.ReporteCarreraDTO;
import edu.isistan.modelo.Carrera;
import edu.isistan.modelo.Estudiante;
import edu.isistan.modelo.Inscripcion;
import edu.isistan.repository.CarreraRepository;
import edu.isistan.repository.EstudianteRepository;
import edu.isistan.repository.InscripcionRepository;
import edu.isistan.repository.MySQLCarrera;
import edu.isistan.repository.MySQLEstudiante;
import edu.isistan.repository.MySQLInscripcion;
import edu.isistan.util.JPAUtil;

public class Main {

    public static void main(String[] args) {

        EntityManager em = JPAUtil.getEntityManager();

        try {

            // =================================
            // CARGA INICIAL DE DATOS
            // =================================
            borrarDatos(em);

            cargarEstudiantes(em);
            cargarCarreras(em);
            cargarInscripciones(em);

            // =================================
            // REPOSITORIES
            // =================================
            EstudianteRepository estudianteRepository
                    = new MySQLEstudiante();

            InscripcionRepository inscripcionRepository
                    = new MySQLInscripcion();

            CarreraRepository carreraRepository
                    = new MySQLCarrera();

            // =================================
            // 2.a - DAR DE ALTA UN ESTUDIANTE
            // =================================
            System.out.println();
            System.out.println("=================================");
            System.out.println("2.a - ALTA DE ESTUDIANTE");
            System.out.println("=================================");

            Estudiante nuevoEstudiante
                    = new Estudiante(
                            11111111,
                            "Lucia",
                            "Alcibar",
                            22,
                            "Female",
                            "Tandil",
                            99999
                    );

            estudianteRepository.insertarEstudiante(
                    nuevoEstudiante
            );

            System.out.println(
                    "Estudiante agregado: "
                    + nuevoEstudiante.getNombre()
                    + " "
                    + nuevoEstudiante.getApellido()
            );

            // =================================
            // 2.b - MATRICULAR ESTUDIANTE
            // =================================
            System.out.println();
            System.out.println("=================================");
            System.out.println("2.b - MATRICULAR ESTUDIANTE");
            System.out.println("=================================");

            inscripcionRepository.matricular(
                    200,
                    11111111,
                    1,
                    2026,
                    0,
                    0
            );

            System.out.println(
                    "Estudiante matriculado en TUDAI."
            );

            // =================================
            // 2.c - RECUPERAR TODOS
            // ORDENADOS POR APELLIDO
            // =================================
            System.out.println();
            System.out.println("=================================");
            System.out.println("2.c - TODOS LOS ESTUDIANTES");
            System.out.println("=================================");

            List<EstudianteDTO> estudiantes
                    = estudianteRepository.recuperarTodos();

            for (EstudianteDTO estudiante : estudiantes) {

                System.out.println(
                        estudiante.getApellido()
                        + ", "
                        + estudiante.getNombre()
                        + " - LU: "
                        + estudiante.getLU()
                );
            }

            // =================================
            // 2.d - RECUPERAR POR LU
            // =================================
            System.out.println();
            System.out.println("=================================");
            System.out.println("2.d - ESTUDIANTE POR LU");
            System.out.println("=================================");

            EstudianteDTO estudianteLU
                    = estudianteRepository.recuperarPorLU(34978);

            if (estudianteLU != null) {

                System.out.println(
                        estudianteLU.getNombre()
                        + " "
                        + estudianteLU.getApellido()
                        + " - LU: "
                        + estudianteLU.getLU()
                );
            }

            // =================================
            // 2.e - RECUPERAR POR GENERO
            // =================================
            System.out.println();
            System.out.println("=================================");
            System.out.println("2.e - ESTUDIANTES POR GENERO");
            System.out.println("=================================");

            List<EstudianteDTO> estudiantesGenero
                    = estudianteRepository.recuperarPorGenero(
                            "Female"
                    );

            for (EstudianteDTO estudiante
                    : estudiantesGenero) {

                System.out.println(
                        estudiante.getApellido()
                        + ", "
                        + estudiante.getNombre()
                        + " - Genero: "
                        + estudiante.getGenero()
                );
            }

            // =================================
            // 2.f - CARRERAS ORDENADAS POR
            // CANTIDAD DE INSCRIPTOS
            // =================================
            System.out.println();
            System.out.println("=================================");
            System.out.println("2.f - CARRERAS POR INSCRIPTOS");
            System.out.println("=================================");

            List<CarreraInscriptosDTO> carreras
                    = carreraRepository
                            .carrerasOrdenadasPorInscriptos();

            for (CarreraInscriptosDTO carrera : carreras) {

                System.out.println(
                        carrera.getNombreCarrera()
                        + " - Inscriptos: "
                        + carrera.getCantidadInscriptos()
                );
            }

            // =================================
            // 2.g - ESTUDIANTES DE UNA CARRERA
            // FILTRADOS POR CIUDAD
            // =================================
            System.out.println();
            System.out.println("=================================");
            System.out.println(
                    "2.g - ESTUDIANTES POR CARRERA Y CIUDAD"
            );
            System.out.println("=================================");

            List<EstudianteDTO> estudiantesCarreraCiudad
                    = estudianteRepository
                            .recuperarPorCarreraYCiudad(
                                    "TUDAI",
                                    "Tandil"
                            );

            for (EstudianteDTO estudiante
                    : estudiantesCarreraCiudad) {

                System.out.println(
                        estudiante.getNombre()
                        + " "
                        + estudiante.getApellido()
                );
            }

            // =================================
            // 3 - REPORTE DE CARRERAS
            // =================================
            imprimirReporte(carreraRepository);

            // =================================
            // FIN
            // =================================
            System.out.println();
            System.out.println("=================================");
            System.out.println("TODAS LAS CONSULTAS FINALIZADAS");
            System.out.println("=================================");

        } catch (Exception e) {

            e.printStackTrace();

        } finally {

            em.close();
            JPAUtil.cerrar();
        }
    }

    // =================================
    // CARGAR ESTUDIANTES
    // =================================
    private static void cargarEstudiantes(
            EntityManager em) {

        System.out.println("Cargando estudiantes...");

        InputStream archivo
                = Main.class.getClassLoader()
                        .getResourceAsStream(
                                "estudiantes.csv"
                        );

        if (archivo == null) {

            throw new RuntimeException(
                    "No se encontró estudiantes.csv"
            );
        }

        try (
                BufferedReader br
                = new BufferedReader(
                        new InputStreamReader(
                                archivo,
                                StandardCharsets.UTF_8
                        )
                )) {

            br.readLine();

            em.getTransaction().begin();

            while (br.ready()) {

                String linea = br.readLine();

                if (linea == null) {
                    break;
                }

                String[] datos = linea.split(",");

                int dni
                        = Integer.parseInt(datos[0]);

                String nombre = datos[1];

                String apellido = datos[2];

                int edad
                        = Integer.parseInt(datos[3]);

                String genero = datos[4];

                String ciudad = datos[5];

                int LU
                        = Integer.parseInt(datos[6]);

                Estudiante estudiante
                        = new Estudiante(
                                dni,
                                nombre,
                                apellido,
                                edad,
                                genero,
                                ciudad,
                                LU
                        );

                em.persist(estudiante);
            }

            em.getTransaction().commit();

            System.out.println(
                    "Estudiantes cargados."
            );

        } catch (Exception e) {

            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }

            throw new RuntimeException(
                    "Error cargando estudiantes",
                    e
            );
        }
    }

    // =================================
    // CARGAR CARRERAS
    // =================================
    private static void cargarCarreras(
            EntityManager em) {

        System.out.println("Cargando carreras...");

        InputStream archivo
                = Main.class.getClassLoader()
                        .getResourceAsStream(
                                "carreras.csv"
                        );

        if (archivo == null) {

            throw new RuntimeException(
                    "No se encontró carreras.csv"
            );
        }

        try (
                BufferedReader br
                = new BufferedReader(
                        new InputStreamReader(
                                archivo,
                                StandardCharsets.UTF_8
                        )
                )) {

            br.readLine();

            em.getTransaction().begin();

            while (br.ready()) {

                String linea = br.readLine();

                if (linea == null) {
                    break;
                }

                String[] datos = linea.split(",");

                int id
                        = Integer.parseInt(datos[0]);

                String nombre = datos[1];

                int duracion
                        = Integer.parseInt(datos[2]);

                Carrera carrera
                        = new Carrera(
                                id,
                                nombre,
                                duracion
                        );

                em.persist(carrera);
            }

            em.getTransaction().commit();

            System.out.println(
                    "Carreras cargadas."
            );

        } catch (Exception e) {

            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }

            throw new RuntimeException(
                    "Error cargando carreras",
                    e
            );
        }
    }

    // =================================
    // CARGAR INSCRIPCIONES
    // =================================
    private static void cargarInscripciones(
            EntityManager em) {

        System.out.println(
                "Cargando inscripciones..."
        );

        InputStream archivo
                = Main.class.getClassLoader()
                        .getResourceAsStream(
                                "estudianteCarrera.csv"
                        );

        if (archivo == null) {

            throw new RuntimeException(
                    "No se encontró estudianteCarrera.csv"
            );
        }

        try (
                BufferedReader br
                = new BufferedReader(
                        new InputStreamReader(
                                archivo,
                                StandardCharsets.UTF_8
                        )
                )) {

            br.readLine();

            em.getTransaction().begin();

            while (br.ready()) {

                String linea = br.readLine();

                if (linea == null) {
                    break;
                }

                String[] datos = linea.split(",");

                int id
                        = Integer.parseInt(datos[0]);

                int dniEstudiante
                        = Integer.parseInt(datos[1]);

                int idCarrera
                        = Integer.parseInt(datos[2]);

                int anioInscripcion
                        = Integer.parseInt(datos[3]);

                int anioGraduacion
                        = Integer.parseInt(datos[4]);

                int antiguedad
                        = Integer.parseInt(datos[5]);

                Estudiante estudiante
                        = em.find(
                                Estudiante.class,
                                dniEstudiante
                        );

                Carrera carrera
                        = em.find(
                                Carrera.class,
                                idCarrera
                        );

                if (estudiante == null) {

                    throw new RuntimeException(
                            "No existe el estudiante con DNI: "
                            + dniEstudiante
                    );
                }

                if (carrera == null) {

                    throw new RuntimeException(
                            "No existe la carrera con ID: "
                            + idCarrera
                    );
                }

                Inscripcion inscripcion
                        = new Inscripcion(
                                id,
                                estudiante,
                                carrera,
                                anioInscripcion,
                                anioGraduacion,
                                antiguedad
                        );

                em.persist(inscripcion);
            }

            em.getTransaction().commit();

            System.out.println(
                    "Inscripciones cargadas."
            );

        } catch (Exception e) {

            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }

            throw new RuntimeException(
                    "Error cargando inscripciones",
                    e
            );
        }
    }

    // =================================
    // IMPRIMIR REPORTE
    // =================================
    private static void imprimirReporte(
            CarreraRepository carreraRepository) {

        List<ReporteCarreraDTO> reporte
                = carreraRepository.generarReporte();

        System.out.println();
        System.out.println("=================================");
        System.out.println("3 - REPORTE DE CARRERAS");
        System.out.println("=================================");

        for (ReporteCarreraDTO r : reporte) {

            System.out.println(
                    r.getCarrera()
                    + " | Año: "
                    + r.getAnio()
                    + " | Inscriptos: "
                    + r.getInscriptos()
                    + " | Egresados: "
                    + r.getEgresados()
            );
        }
    }

    // =================================
    // BORRAR DATOS
    // =================================
    private static void borrarDatos(
            EntityManager em) {

        System.out.println(
                "Borrando datos anteriores..."
        );

        try {

            em.getTransaction().begin();

            em.createQuery(
                    "DELETE FROM Inscripcion"
            ).executeUpdate();

            em.createQuery(
                    "DELETE FROM Estudiante"
            ).executeUpdate();

            em.createQuery(
                    "DELETE FROM Carrera"
            ).executeUpdate();

            em.getTransaction().commit();

            System.out.println(
                    "Datos anteriores borrados."
            );

        } catch (Exception e) {

            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }

            throw new RuntimeException(
                    "Error borrando datos",
                    e
            );
        }
    }
}
