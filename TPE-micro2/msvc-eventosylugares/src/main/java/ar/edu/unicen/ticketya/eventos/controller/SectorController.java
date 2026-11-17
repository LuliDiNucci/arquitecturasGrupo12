package ar.edu.unicen.ticketya.eventos.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import ar.edu.unicen.ticketya.eventos.dto.ApiErrorResponseDTO;
import ar.edu.unicen.ticketya.eventos.dto.SectorRequestDTO;
import ar.edu.unicen.ticketya.eventos.dto.SectorResponseDTO;
import ar.edu.unicen.ticketya.eventos.service.SectorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "Sectores")
@ApiResponses({
        @ApiResponse(responseCode = "400", description = "Request invalido", content = @Content(schema = @Schema(implementation = ApiErrorResponseDTO.class))),
        @ApiResponse(responseCode = "404", description = "Recurso inexistente", content = @Content(schema = @Schema(implementation = ApiErrorResponseDTO.class))),
        @ApiResponse(responseCode = "409", description = "Regla de negocio o conflicto", content = @Content(schema = @Schema(implementation = ApiErrorResponseDTO.class)))
})
@RestController
public class SectorController {

    private final SectorService sectorService;

    public SectorController(SectorService sectorService) {
        this.sectorService = sectorService;
    }

    @Operation(summary = "Agregar un sector numerado o no numerado a un lugar")
    @ApiResponse(responseCode = "201", description = "Recurso creado")
    @PostMapping("/lugares/{id}/sectores")
    public ResponseEntity<SectorResponseDTO> crearSector(
            @PathVariable Long id,
            @Valid @RequestBody SectorRequestDTO request) {

        SectorResponseDTO creado = sectorService.crearSector(id, request);
        return ResponseEntity.created(URI.create("/sectores/" + creado.getId())).body(creado);
    }

    @Operation(summary = "Listar sectores de un lugar")
    @ApiResponse(responseCode = "200", description = "Operacion exitosa")
    @GetMapping("/lugares/{id}/sectores")
    public List<SectorResponseDTO> listarSectores(@PathVariable Long id) {
        return sectorService.listarSectores(id);
    }

    @Operation(summary = "Consultar un sector por ID")
    @ApiResponse(responseCode = "200", description = "Operacion exitosa")
    @GetMapping("/sectores/{id}")
    public SectorResponseDTO obtenerSector(@PathVariable Long id) {
        return sectorService.obtenerSector(id);
    }

}
