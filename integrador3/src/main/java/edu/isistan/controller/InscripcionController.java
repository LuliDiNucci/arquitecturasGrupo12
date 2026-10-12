
package edu.isistan.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import edu.isistan.dto.InscripcionDTO;
import edu.isistan.modelo.Inscripcion;
import edu.isistan.service.InscripcionService;

@RestController
@RequestMapping("/inscripciones")
public class InscripcionController {

    @Autowired
    private InscripcionService inscripcionService;

    // b) Matricular un estudiante en una carrera
    @PostMapping
    public ResponseEntity<?> matricular(
            @RequestParam int id,
            @RequestParam int dniEstudiante,
            @RequestParam int idCarrera,
            @RequestParam int anioInscripcion,
            @RequestParam int anioGraduacion,
            @RequestParam int antiguedad) {

        try {

            // Guardar la inscripción
            Inscripcion inscripcion =
                    inscripcionService.matricular(
                            id,
                            dniEstudiante,
                            idCarrera,
                            anioInscripcion,
                            anioGraduacion,
                            antiguedad
                    );

            // Convertir la entidad a DTO
            InscripcionDTO dto =
                    new InscripcionDTO(inscripcion);

            // Devolver el DTO como JSON
            return ResponseEntity.ok(dto);

        } catch (Exception e) {

            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());
        }
    }
}

