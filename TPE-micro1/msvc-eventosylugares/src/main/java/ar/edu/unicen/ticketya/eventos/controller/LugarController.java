package ar.edu.unicen.ticketya.eventos.controller;

import ar.edu.unicen.ticketya.eventos.dto.LugarRequestDTO;
import ar.edu.unicen.ticketya.eventos.dto.LugarResponseDTO;
import ar.edu.unicen.ticketya.eventos.service.LugarService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import ar.edu.unicen.ticketya.eventos.dto.ApiErrorResponseDTO;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@Tag(name = "Lugares")
@ApiResponses({
        @ApiResponse(responseCode = "400", description = "Request invalido", content = @Content(schema = @Schema(implementation = ApiErrorResponseDTO.class))),
        @ApiResponse(responseCode = "404", description = "Recurso inexistente", content = @Content(schema = @Schema(implementation = ApiErrorResponseDTO.class))),
        @ApiResponse(responseCode = "409", description = "Regla de negocio o conflicto", content = @Content(schema = @Schema(implementation = ApiErrorResponseDTO.class)))
})
@RestController
@RequestMapping("/lugares")
public class LugarController {

    private final LugarService lugarService;

    public LugarController(LugarService lugarService) {
        this.lugarService = lugarService;
    }

    @Operation(summary = "Crear un lugar fisico con coordenadas y ciudad")
    @ApiResponse(responseCode = "201", description = "Recurso creado")
    @PostMapping
    public ResponseEntity<LugarResponseDTO> crearLugar(@Valid @RequestBody LugarRequestDTO request) {
        LugarResponseDTO creado = lugarService.crearLugar(request);
        return ResponseEntity.created(URI.create("/lugares/" + creado.getId())).body(creado);
    }

    @Operation(summary = "Listar lugares")
    @ApiResponse(responseCode = "200", description = "Operacion exitosa")
    @GetMapping
    public List<LugarResponseDTO> listarLugares() {
        return lugarService.listarLugares();
    }
    @Operation(summary = "Obtener lugar por ID")
    @ApiResponse(responseCode = "200", description = "Operacion exitosa")
    @GetMapping("/{id}")
    public LugarResponseDTO obtenerLugar(@PathVariable Long id) {
        return lugarService.obtenerLugar(id);
    }
}
