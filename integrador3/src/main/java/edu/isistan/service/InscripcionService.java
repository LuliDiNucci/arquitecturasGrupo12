package edu.isistan.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import edu.isistan.modelo.Carrera;
import edu.isistan.modelo.Estudiante;
import edu.isistan.modelo.Inscripcion;
import edu.isistan.repository.CarreraRepository;
import edu.isistan.repository.EstudianteRepository;
import edu.isistan.repository.InscripcionRepository;
import jakarta.transaction.Transactional;

@Service("InscripcionService")
public class InscripcionService implements BaseService<Inscripcion> {

    @Autowired
    private InscripcionRepository inscripcionRepository;

    @Autowired
    private EstudianteRepository estudianteRepository;

    @Autowired
    private CarreraRepository carreraRepository;

    // b) Matricular un estudiante en una carrera
    @Transactional
    public Inscripcion matricular(
            int id,
            int dniEstudiante,
            int idCarrera,
            int anioInscripcion,
            int anioGraduacion,
            int antiguedad) throws Exception {

        try {

            Estudiante estudiante =
                    estudianteRepository.findById(dniEstudiante)
                            .orElseThrow(() ->
                                    new Exception(
                                            "No existe el estudiante con DNI: "
                                            + dniEstudiante
                                    )
                            );

            Carrera carrera =
                    carreraRepository.findById(idCarrera)
                            .orElseThrow(() ->
                                    new Exception(
                                            "No existe la carrera con ID: "
                                            + idCarrera
                                    )
                            );

            Inscripcion inscripcion = new Inscripcion(
                    id,
                    estudiante,
                    carrera,
                    anioInscripcion,
                    anioGraduacion,
                    antiguedad
            );

            return inscripcionRepository.save(inscripcion);

        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    // Métodos CRUD de BaseService

    @Override
    @Transactional
    public List<Inscripcion> findAll() throws Exception {
        return inscripcionRepository.findAll();
    }

    @Override
    @Transactional
    public Inscripcion findById(Long id) throws Exception {
        try {
            return inscripcionRepository
                    .findById(id.intValue())
                    .orElseThrow(() ->
                            new Exception("No existe la inscripción")
                    );
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    @Override
    @Transactional
    public Inscripcion save(Inscripcion entity) throws Exception {
        try {
            return inscripcionRepository.save(entity);
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    @Override
    @Transactional
    public Inscripcion update(
            Long id,
            Inscripcion entity) throws Exception {

        try {
            Inscripcion inscripcion =
                    inscripcionRepository
                            .findById(id.intValue())
                            .orElseThrow(() ->
                                    new Exception(
                                            "No existe la inscripción"
                                    )
                            );

            inscripcion.setEstudiante(entity.getEstudiante());
            inscripcion.setCarrera(entity.getCarrera());
            inscripcion.setInscripcion(entity.getInscripcion());
            inscripcion.setGraduacion(entity.getGraduacion());
            inscripcion.setAntiguedad(entity.getAntiguedad());

            return inscripcionRepository.save(inscripcion);

        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    @Override
    @Transactional
    public boolean delete(Long id) throws Exception {
        try {

            if (inscripcionRepository.existsById(id.intValue())) {

                inscripcionRepository.deleteById(id.intValue());

                return true;

            } else {

                throw new Exception("No existe la inscripción");
            }

        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }
}