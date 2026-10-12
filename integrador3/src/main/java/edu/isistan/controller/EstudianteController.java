package edu.isistan.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import edu.isistan.dto.EstudianteDTO;
import edu.isistan.modelo.Estudiante;
import edu.isistan.service.EstudianteService;

@RestController
@RequestMapping("/estudiantes")
public class EstudianteController {

    @Autowired
    private EstudianteService estudianteService;

    // a) Dar de alta un estudiante
    @PostMapping
    public ResponseEntity<?> insertarEstudiante(@RequestBody Estudiante estudiante) {
        try {
            Estudiante estudianteGuardado = estudianteService.save(estudiante);

            EstudianteDTO dto = new EstudianteDTO(
                    estudianteGuardado.getDni(),
                    estudianteGuardado.getNombre(),
                    estudianteGuardado.getApellido(),
                    estudianteGuardado.getEdad(),
                    estudianteGuardado.getGenero(),
                    estudianteGuardado.getCiudad(),
                    estudianteGuardado.getLU()
            );

            return ResponseEntity.ok(dto);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    // c) Recuperar todos los estudiantes ordenados por apellido
    @GetMapping
    public ResponseEntity<?> recuperarTodos() {

        try {
            List<EstudianteDTO> estudiantes
                    = estudianteService.recuperarTodos();

            return ResponseEntity.ok(estudiantes);

        } catch (Exception e) {
            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());
        }
    }

    // d) Recuperar estudiante por LU
    @GetMapping("/lu/{LU}")
    public ResponseEntity<?> recuperarPorLU(
            @PathVariable int LU) {

        try {
            EstudianteDTO estudiante
                    = estudianteService.recuperarPorLU(LU);

            return ResponseEntity.ok(estudiante);

        } catch (Exception e) {
            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());
        }
    }

    // e) Recuperar estudiantes por género
    @GetMapping("/genero/{genero}")
    public ResponseEntity<?> recuperarPorGenero(
            @PathVariable String genero) {

        try {
            List<EstudianteDTO> estudiantes
                    = estudianteService.recuperarPorGenero(genero);

            return ResponseEntity.ok(estudiantes);

        } catch (Exception e) {
            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());
        }
    }

    // g) Recuperar estudiantes de una carrera filtrados por ciudad
    @GetMapping("/carrera/{nombreCarrera}/ciudad/{ciudad}")
    public ResponseEntity<?> recuperarPorCarreraYCiudad(
            @PathVariable String nombreCarrera,
            @PathVariable String ciudad) {

        try {
            List<EstudianteDTO> estudiantes
                    = estudianteService.recuperarPorCarreraYCiudad(
                            nombreCarrera,
                            ciudad
                    );

            return ResponseEntity.ok(estudiantes);

        } catch (Exception e) {
            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());
        }
    }
}
