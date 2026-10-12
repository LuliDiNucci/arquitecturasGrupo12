package edu.isistan.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import edu.isistan.dto.CarreraInscriptosDTO;
import edu.isistan.dto.ReporteCarreraDTO;
import edu.isistan.service.CarreraService;

@RestController
@RequestMapping("/carreras")
public class CarreraController {

    @Autowired
    private CarreraService carreraService;

    // f) Recuperar las carreras con estudiantes inscriptos
    //    ordenadas por cantidad de inscriptos
    @GetMapping("/inscriptos")
    public ResponseEntity<?> carrerasOrdenadasPorInscriptos() {

        try {

            List<CarreraInscriptosDTO> carreras =
                    carreraService.carrerasOrdenadasPorInscriptos();

            return ResponseEntity.ok(carreras);

        } catch (Exception e) {

            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());
        }
    }

    // h) Generar reporte de carreras con inscriptos
    //    y egresados por año
    @GetMapping("/reporte")
    public ResponseEntity<?> generarReporte() {

        try {

            List<ReporteCarreraDTO> reporte =
                    carreraService.generarReporte();

            return ResponseEntity.ok(reporte);

        } catch (Exception e) {

            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());
        }
    }
}

