package ar.edu.unicen.ticketya.eventos.controller;

import ar.edu.unicen.ticketya.eventos.dto.ParticipanteRequestDTO;
import ar.edu.unicen.ticketya.eventos.dto.ParticipanteResponseDTO;
import ar.edu.unicen.ticketya.eventos.service.ParticipanteService;
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

@Tag(name = "Participantes")
@ApiResponses({
        @ApiResponse(responseCode = "400", description = "Request invalido", content = @Content(schema = @Schema(implementation = ApiErrorResponseDTO.class))),
        @ApiResponse(responseCode = "404", description = "Recurso inexistente", content = @Content(schema = @Schema(implementation = ApiErrorResponseDTO.class))),
        @ApiResponse(responseCode = "409", description = "Regla de negocio o conflicto", content = @Content(schema = @Schema(implementation = ApiErrorResponseDTO.class)))
})
@RestController
@RequestMapping("/participantes")
public class ParticipanteController {

    private final ParticipanteService participanteService;

    public ParticipanteController(ParticipanteService participanteService) {
        this.participanteService = participanteService;
    }

    @Operation(summary = "Crear participante")
    @ApiResponse(responseCode = "201", description = "Recurso creado")
    @PostMapping
    public ResponseEntity<ParticipanteResponseDTO> crearParticipante(
            @Valid @RequestBody ParticipanteRequestDTO request) {

        ParticipanteResponseDTO creado = participanteService.crearParticipante(request);
        return ResponseEntity.created(URI.create("/participantes/" + creado.getId())).body(creado);
    }

    @Operation(summary = "Listar participantes")
    @ApiResponse(responseCode = "200", description = "Operacion exitosa")
    @GetMapping
    public List<ParticipanteResponseDTO> listarParticipantes() {
        return participanteService.listarParticipantes();
    }
}
