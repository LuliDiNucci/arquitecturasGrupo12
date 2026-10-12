package edu.isistan.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import edu.isistan.dto.CarreraInscriptosDTO;
import edu.isistan.dto.ReporteCarreraDTO;
import edu.isistan.modelo.Carrera;
import edu.isistan.repository.CarreraRepository;
import jakarta.transaction.Transactional;

@Service("CarreraService")
public class CarreraService implements BaseService<Carrera> {

    @Autowired
    private CarreraRepository carreraRepository;

    // f) Recuperar carreras con estudiantes inscriptos
    // ordenadas por cantidad de inscriptos
    @Transactional
    public List<CarreraInscriptosDTO> carrerasOrdenadasPorInscriptos() {
        return carreraRepository.carrerasOrdenadasPorInscriptos();
    }

    // h) Generar reporte de carreras
    @Transactional
    public List<ReporteCarreraDTO> generarReporte() {

        List<ReporteCarreraDTO> inscriptos =
                carreraRepository.obtenerInscriptosPorAnio();

        List<ReporteCarreraDTO> egresados =
                carreraRepository.obtenerEgresadosPorAnio();

        Map<String, Map<Integer, ReporteCarreraDTO>> reporte =
                new TreeMap<>();

        // Incorporar inscriptos
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

        // Incorporar egresados
        for (ReporteCarreraDTO dto : egresados) {

            reporte.putIfAbsent(
                    dto.getCarrera(),
                    new TreeMap<>()
            );

            Map<Integer, ReporteCarreraDTO> anios =
                    reporte.get(dto.getCarrera());

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

        // Convertir a lista
        List<ReporteCarreraDTO> resultado =
                new ArrayList<>();

        for (Map<Integer, ReporteCarreraDTO> anios :
                reporte.values()) {

            resultado.addAll(anios.values());
        }

        return resultado;
    }

    // Métodos CRUD de BaseService

    @Override
    @Transactional
    public List<Carrera> findAll() throws Exception {
        return carreraRepository.findAll();
    }

    @Override
    @Transactional
    public Carrera findById(Long id) throws Exception {
        try {
            return carreraRepository.findById(id.intValue()).get();
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    @Override
    @Transactional
    public Carrera save(Carrera entity) throws Exception {
        try {
            return carreraRepository.save(entity);
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    @Override
    @Transactional
    public Carrera update(Long id, Carrera entity) throws Exception {
        try {
            Carrera carrera = carreraRepository
                    .findById(id.intValue())
                    .get();

            carrera.setNombre(entity.getNombre());

            return carreraRepository.save(carrera);

        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    @Override
    @Transactional
    public boolean delete(Long id) throws Exception {
        try {
            if (carreraRepository.existsById(id.intValue())) {
                carreraRepository.deleteById(id.intValue());
                return true;
            } else {
                throw new Exception();
            }
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }
}

