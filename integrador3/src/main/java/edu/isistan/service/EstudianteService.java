package edu.isistan.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import edu.isistan.dto.EstudianteDTO;
import edu.isistan.modelo.Estudiante;
import edu.isistan.repository.EstudianteRepository;
import jakarta.transaction.Transactional;

@Service("EstudianteService")
public class EstudianteService implements BaseService<Estudiante> {

    @Autowired
    private EstudianteRepository estudianteRepository;

    // c) Recuperar todos los estudiantes
    @Transactional
    public List<EstudianteDTO> recuperarTodos() {
        return estudianteRepository.recuperarTodos();
    }

    // d) Recuperar estudiante por LU
    @Transactional
    public EstudianteDTO recuperarPorLU(int LU) {
        return estudianteRepository.recuperarPorLU(LU);
    }

    // e) Recuperar estudiantes por género
    @Transactional
    public List<EstudianteDTO> recuperarPorGenero(String genero) {
        return estudianteRepository.recuperarPorGenero(genero);
    }

    // g) Recuperar estudiantes de una carrera
    // filtrados por ciudad
    @Transactional
    public List<EstudianteDTO> recuperarPorCarreraYCiudad(
            String nombreCarrera,
            String ciudad) {

        return estudianteRepository.recuperarPorCarreraYCiudad(
                nombreCarrera,
                ciudad
        );
    }

    // Métodos CRUD de BaseService

    @Override
    @Transactional
    public List<Estudiante> findAll() throws Exception {
        return estudianteRepository.findAll();
    }

    @Override
    @Transactional
    public Estudiante findById(Long id) throws Exception {
        try {
            return estudianteRepository.findById(id.intValue()).get();
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    @Override
    @Transactional
    public Estudiante save(Estudiante entity) throws Exception {
        try {
            return estudianteRepository.save(entity);
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    @Override
    @Transactional
    public Estudiante update(Long id, Estudiante entity) throws Exception {
        try {
            Estudiante estudiante =
                    estudianteRepository.findById(id.intValue()).get();

            estudiante.setNombre(entity.getNombre());
            estudiante.setApellido(entity.getApellido());
            estudiante.setEdad(entity.getEdad());
            estudiante.setGenero(entity.getGenero());
            estudiante.setCiudad(entity.getCiudad());
            estudiante.setLU(entity.getLU());

            return estudianteRepository.save(estudiante);

        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    @Override
    @Transactional
    public boolean delete(Long id) throws Exception {
        try {
            if (estudianteRepository.existsById(id.intValue())) {
                estudianteRepository.deleteById(id.intValue());
                return true;
            } else {
                throw new Exception();
            }
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }
}

