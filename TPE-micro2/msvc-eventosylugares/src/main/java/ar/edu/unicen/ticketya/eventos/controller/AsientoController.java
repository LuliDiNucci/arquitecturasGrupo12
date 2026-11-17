package ar.edu.unicen.ticketya.eventos.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import ar.edu.unicen.ticketya.eventos.dto.ApiErrorResponseDTO;
import ar.edu.unicen.ticketya.eventos.dto.AsientoResponseDTO;
import ar.edu.unicen.ticketya.eventos.dto.AsientosRequestDTO;
import ar.edu.unicen.ticketya.eventos.service.AsientoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "Asientos")
@ApiResponses({
        @ApiResponse(responseCode = "400", description = "Request invalido", content = @Content(schema = @Schema(implementation = ApiErrorResponseDTO.class))),
        @ApiResponse(responseCode = "404", description = "Recurso inexistente", content = @Content(schema = @Schema(implementation = ApiErrorResponseDTO.class))),
        @ApiResponse(responseCode = "409", description = "Regla de negocio o conflicto", content = @Content(schema = @Schema(implementation = ApiErrorResponseDTO.class)))
})
@RestController
public class AsientoController {

    private final AsientoService asientoService;

    public AsientoController(AsientoService asientoService) {
        this.asientoService = asientoService;
    }

    @Operation(summary = "Cargar asientos en un sector numerado")
    @ApiResponse(responseCode = "201", description = "Recurso creado")
    @PostMapping("/sectores/{id}/asientos")
    public ResponseEntity<List<AsientoResponseDTO>> agregarAsientos(
            @PathVariable Long id,
            @Valid @RequestBody AsientosRequestDTO request) {

        List<AsientoResponseDTO> creados = asientoService.agregarAsientos(id, request);
        return ResponseEntity.status(201).body(creados);
    }

    @Operation(summary = "Consultar asientos de un sector")
    @ApiResponse(responseCode = "200", description = "Operacion exitosa")
    @GetMapping("/sectores/{id}/asientos")
    public List<AsientoResponseDTO> listarAsientos(@PathVariable Long id) {
        return asientoService.listarAsientos(id);
    }

    @Operation(summary = "Consultar un asiento por ID")
    @ApiResponse(responseCode = "200", description = "Operacion exitosa")
    @GetMapping("/asientos/{id}")
    public AsientoResponseDTO obtenerAsiento(@PathVariable Long id) {
        return asientoService.obtenerAsiento(id);
    }

}
