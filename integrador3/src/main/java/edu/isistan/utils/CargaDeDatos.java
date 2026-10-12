package edu.isistan.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import edu.isistan.modelo.Carrera;
import edu.isistan.modelo.Estudiante;
import edu.isistan.modelo.Inscripcion;
import edu.isistan.repository.CarreraRepository;
import edu.isistan.repository.EstudianteRepository;
import edu.isistan.repository.InscripcionRepository;

import jakarta.annotation.PostConstruct;

@Component
public class CargaDeDatos {

    @Autowired
    private EstudianteRepository estudianteRepository;

    @Autowired
    private CarreraRepository carreraRepository;

    @Autowired
    private InscripcionRepository inscripcionRepository;

    @PostConstruct
    public void cargarDatos() throws Exception {

        // Evitamos cargar nuevamente los datos si la BD ya tiene información
        if (estudianteRepository.count() > 0
                || carreraRepository.count() > 0
                || inscripcionRepository.count() > 0) {

            System.out.println("La base de datos ya contiene datos. No se realiza la carga inicial.");
            return;
        }

        System.out.println("=================================");
        System.out.println("CARGANDO DATOS DESDE CSV");
        System.out.println("=================================");

        cargarEstudiantes();
        cargarCarreras();
        cargarInscripciones();

        System.out.println("=================================");
        System.out.println("DATOS CARGADOS CORRECTAMENTE");
        System.out.println("=================================");
    }

    private void cargarEstudiantes() throws IOException {

        InputStream archivo
                = getClass().getClassLoader()
                        .getResourceAsStream("estudiantes.csv");

        if (archivo == null) {
            throw new IOException("No se encontró estudiantes.csv");
        }

        try (BufferedReader reader
                = new BufferedReader(
                        new InputStreamReader(
                                archivo,
                                StandardCharsets.UTF_8))) {

            String linea;
            boolean primeraLinea = true;

            while ((linea = reader.readLine()) != null) {

                if (primeraLinea) {
                    primeraLinea = false;
                    continue;
                }

                String[] datos = linea.split(",");

                int dni = Integer.parseInt(datos[0]);
                String nombre = datos[1];
                String apellido = datos[2];
                int edad = Integer.parseInt(datos[3]);
                String genero = datos[4];
                String ciudad = datos[5];
                int LU = Integer.parseInt(datos[6]);

                Estudiante estudiante = new Estudiante(
                        dni,
                        nombre,
                        apellido,
                        edad,
                        genero,
                        ciudad,
                        LU
                );

                estudianteRepository.save(estudiante);
            }
        }

        System.out.println("Estudiantes cargados.");
    }

    private void cargarCarreras() throws IOException {

        InputStream archivo
                = getClass().getClassLoader()
                        .getResourceAsStream("carreras.csv");

        if (archivo == null) {
            throw new IOException("No se encontró carreras.csv");
        }

        try (BufferedReader reader
                = new BufferedReader(
                        new InputStreamReader(
                                archivo,
                                StandardCharsets.UTF_8))) {

            String linea;
            boolean primeraLinea = true;

            while ((linea = reader.readLine()) != null) {

                if (primeraLinea) {
                    primeraLinea = false;
                    continue;
                }

                String[] datos = linea.split(",");

                int id = Integer.parseInt(datos[0]);
                String nombre = datos[1];
                int duracion = Integer.parseInt(datos[2]);

                Carrera carrera = new Carrera(
                        id,
                        nombre,
                        duracion
                );

                carreraRepository.save(carrera);
            }
        }

        System.out.println("Carreras cargadas.");
    }

    private void cargarInscripciones() throws IOException {

        InputStream archivo
                = getClass().getClassLoader()
                        .getResourceAsStream("estudianteCarrera.csv");

        if (archivo == null) {
            throw new IOException("No se encontró estudianteCarrera.csv");
        }

        try (BufferedReader reader
                = new BufferedReader(
                        new InputStreamReader(
                                archivo,
                                StandardCharsets.UTF_8))) {

            String linea;
            boolean primeraLinea = true;

            while ((linea = reader.readLine()) != null) {

                if (primeraLinea) {
                    primeraLinea = false;
                    continue;
                }

                String[] datos = linea.split(",");

                int id = Integer.parseInt(datos[0]);
                int dniEstudiante = Integer.parseInt(datos[1]);
                int idCarrera = Integer.parseInt(datos[2]);
                int anioInscripcion = Integer.parseInt(datos[3]);
                int anioGraduacion = Integer.parseInt(datos[4]);
                int antiguedad = Integer.parseInt(datos[5]);

                Optional<Estudiante> estudiante
                        = estudianteRepository.findById(dniEstudiante);

                Optional<Carrera> carrera
                        = carreraRepository.findById(idCarrera);

                if (estudiante.isEmpty()) {
                    throw new IOException(
                            "No existe el estudiante con DNI: "
                            + dniEstudiante
                    );
                }

                if (carrera.isEmpty()) {
                    throw new IOException(
                            "No existe la carrera con ID: "
                            + idCarrera
                    );
                }

                Inscripcion inscripcion = new Inscripcion(
                        id,
                        estudiante.get(),
                        carrera.get(),
                        anioInscripcion,
                        anioGraduacion,
                        antiguedad
                );

                inscripcionRepository.save(inscripcion);
            }
        }

        System.out.println("Inscripciones cargadas.");
    }
}
