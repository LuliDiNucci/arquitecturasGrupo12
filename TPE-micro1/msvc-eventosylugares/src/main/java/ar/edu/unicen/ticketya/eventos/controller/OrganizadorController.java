package ar.edu.unicen.ticketya.eventos.controller;

import ar.edu.unicen.ticketya.eventos.dto.OrganizadorRequestDTO;
import ar.edu.unicen.ticketya.eventos.dto.OrganizadorResponseDTO;
import ar.edu.unicen.ticketya.eventos.service.OrganizadorService;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import ar.edu.unicen.ticketya.eventos.dto.ApiErrorResponseDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@Tag(name = "Organizadores")
@ApiResponses({
        @ApiResponse(responseCode = "400", description = "Request invalido", content = @Content(schema = @Schema(implementation = ApiErrorResponseDTO.class))),
        @ApiResponse(responseCode = "404", description = "Recurso inexistente", content = @Content(schema = @Schema(implementation = ApiErrorResponseDTO.class))),
        @ApiResponse(responseCode = "409", description = "Regla de negocio o conflicto", content = @Content(schema = @Schema(implementation = ApiErrorResponseDTO.class)))
})
@RestController
@RequestMapping("/organizadores")
public class OrganizadorController {

    private final OrganizadorService organizadorService;

    public OrganizadorController(OrganizadorService organizadorService) {
        this.organizadorService = organizadorService;
    }

    @Operation(summary = "Crear organizador")
    @ApiResponse(responseCode = "201", description = "Recurso creado")
    @PostMapping
    public ResponseEntity<OrganizadorResponseDTO> crearOrganizador(
            @Valid @RequestBody OrganizadorRequestDTO request) {

        OrganizadorResponseDTO creado = organizadorService.crearOrganizador(request);
        return ResponseEntity.created(URI.create("/organizadores/" + creado.getId())).body(creado);
    }

    @Operation(summary = "Listar organizadores")
    @ApiResponse(responseCode = "200", description = "Operacion exitosa")
    @GetMapping
    public List<OrganizadorResponseDTO> listarOrganizadores() {
        return organizadorService.listarOrganizadores();
    }
}
