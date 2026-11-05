package ar.edu.unicen.ticketya.eventos.controller;

import ar.edu.unicen.ticketya.eventos.dto.*;
import ar.edu.unicen.ticketya.eventos.model.OrdenEvento;
import ar.edu.unicen.ticketya.eventos.model.TipoEvento;
import ar.edu.unicen.ticketya.eventos.service.EventoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import ar.edu.unicen.ticketya.eventos.dto.ApiErrorResponseDTO;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;

@Tag(name = "Eventos")
@ApiResponses({
        @ApiResponse(responseCode = "400", description = "Request invalido", content = @Content(schema = @Schema(implementation = ApiErrorResponseDTO.class))),
        @ApiResponse(responseCode = "404", description = "Recurso inexistente", content = @Content(schema = @Schema(implementation = ApiErrorResponseDTO.class))),
        @ApiResponse(responseCode = "409", description = "Regla de negocio o conflicto", content = @Content(schema = @Schema(implementation = ApiErrorResponseDTO.class)))
})
@RestController
@RequestMapping("/eventos")
public class EventoController {

    private final EventoService eventoService;

    public EventoController(EventoService eventoService) {
        this.eventoService = eventoService;
    }

    @Operation(summary = "Crear un evento en estado BORRADOR")
    @ApiResponse(responseCode = "201", description = "Recurso creado")
    @PostMapping
    public ResponseEntity<EventoResponseDTO> crear(@Valid @RequestBody EventoRequestDTO request) {
        EventoResponseDTO creado = eventoService.crear(request);
        return ResponseEntity.created(URI.create("/eventos/" + creado.getId())).body(creado);
    }

    @Operation(summary = "Listar todos los eventos (administracion)")
    @ApiResponse(responseCode = "200", description = "Operacion exitosa")
    @GetMapping
    public List<EventoResumenResponseDTO> listar() {
        return eventoService.listar();
    }

    @Operation(summary = "Listar eventos publicados y agotados (consulta publica)")
    @GetMapping("/publicados")
    public List<EventoResumenResponseDTO> listarPublicados() {
        return eventoService.buscar(null, null, null, null, null, null,
                null, OrdenEvento.FECHA_ASC, false);
    }

    // Los filtros son opcionales: el cliente puede combinar los que necesite.
    @Operation(summary = "Buscar eventos por tipo, organizador, participante, ciudad o fechas")
    @ApiResponse(responseCode = "200", description = "Operacion exitosa")
    @GetMapping("/buscar")
    public List<EventoResumenResponseDTO> buscar(
            @Parameter(description = "Tipo de evento", example = "RECITAL") @RequestParam(required = false) TipoEvento tipo,
            @Parameter(description = "ID del organizador", example = "1") @RequestParam(required = false) Long organizadorId,
            @Parameter(description = "ID del participante", example = "1") @RequestParam(required = false) Long participanteId,
            @Parameter(description = "Ciudad del lugar", example = "Tandil") @RequestParam(required = false) String ciudad,
            @Parameter(description = "Fecha minima en formato ISO", example = "2027-01-01T00:00:00") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime desde,
            @Parameter(description = "Fecha maxima en formato ISO", example = "2027-12-31T23:59:00") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime hasta,
            @Parameter(description = "Texto libre: nombre, descripcion, organizador o participante", example = "Recital") @RequestParam(required = false, name = "q") String texto,
            @Parameter(description = "Ordenar por fecha, precio o relevancia", example = "PRECIO_ASC") @RequestParam(defaultValue = "FECHA_ASC") OrdenEvento ordenar,
            @Parameter(description = "Incluir borradores y cancelados (uso administrativo provisional)", example = "false") @RequestParam(defaultValue = "false") boolean incluirNoPublicados) {

        return eventoService.buscar(
                tipo, organizadorId, participanteId, ciudad,
                desde, hasta, texto, ordenar, incluirNoPublicados
        );
    }
    @Operation(summary = "Obtener detalle de evento")
    @ApiResponse(responseCode = "200", description = "Operacion exitosa")
    @GetMapping("/{id}")
    public EventoResponseDTO obtener(@PathVariable Long id) {
        return eventoService.obtener(id);
    }

    @Operation(summary = "Modificar un evento segun su estado")
    @ApiResponse(responseCode = "200", description = "Operacion exitosa")
    @PutMapping("/{id}")
    public EventoResponseDTO actualizar(
            @PathVariable Long id,
            @Valid @RequestBody EventoRequestDTO request) {

        return eventoService.actualizar(id, request);
    }

    @Operation(summary = "Definir los precios de los sectores para un evento")
    @ApiResponse(responseCode = "200", description = "Operacion exitosa")
    @PutMapping("/{id}/precios")
    public EventoResponseDTO configurarPrecios(
            @PathVariable Long id,
            @Valid @RequestBody PreciosEventoRequestDTO request) {

        return eventoService.configurarPrecios(id, request);
    }
    @Operation(summary = "Publicar un evento")
    @ApiResponse(responseCode = "200", description = "Operacion exitosa")
    @PatchMapping("/{id}/publicar")
    public EventoResponseDTO publicar(@PathVariable Long id) {
        return eventoService.publicar(id);
    }
    @Operation(summary = "Suspender un evento")
    @ApiResponse(responseCode = "200", description = "Operacion exitosa")
    @PatchMapping("/{id}/suspender")
    public EventoResponseDTO suspender(@PathVariable Long id) {
        return eventoService.suspender(id);
    }

    // La cancelacion guarda un aviso para Reservas, Entradas y Compras.
    @Operation(summary = "Cancelar evento y registrar avisos")
    @ApiResponse(responseCode = "200", description = "Operacion exitosa")
    @PatchMapping("/{id}/cancelar")
    public EventoResponseDTO cancelar(@PathVariable Long id) {
        return eventoService.cancelar(id);
    }

    @Operation(summary = "Marcar agotado")
    @ApiResponse(responseCode = "200", description = "Operacion exitosa")
    @PatchMapping("/{id}/agotar")
    public EventoResponseDTO agotar(@PathVariable Long id) {
        return eventoService.marcarAgotado(id);
    }

    @Operation(summary = "Reabrir ventas")
    @ApiResponse(responseCode = "200", description = "Operacion exitosa")
    @PatchMapping("/{id}/reabrir")
    public EventoResponseDTO reabrir(@PathVariable Long id) {
        return eventoService.reabrirVentas(id);
    }
    @Operation(summary = "Finalizar evento")
    @ApiResponse(responseCode = "200", description = "Operacion exitosa")
    @PatchMapping("/{id}/finalizar")
    public EventoResponseDTO finalizar(@PathVariable Long id) {
        return eventoService.finalizar(id);
    }

    @Operation(summary = "Consultar sectores, precios y asientos de un evento")
    @ApiResponse(responseCode = "200", description = "Operacion exitosa")
    @GetMapping("/{id}/sectores")
    public List<PrecioSectorResponseDTO> obtenerSectores(@PathVariable Long id) {
        return eventoService.ofertas(id);
    }

    @Operation(summary = "Consultar si el evento permite recibir reservas")
    @ApiResponse(responseCode = "200", description = "Operacion exitosa")
    @GetMapping("/{id}/habilitado")
    public HabilitacionEventoResponseDTO habilitado(@PathVariable Long id) {
        return eventoService.habilitacion(id);
    }
    @Operation(summary = "Consultar entrega de avisos de cancelacion")
    @ApiResponse(responseCode = "200", description = "Operacion exitosa")
    @GetMapping("/{id}/notificaciones-cancelacion")
    public EstadoNotificacionesResponseDTO notificaciones(@PathVariable Long id) {
        return eventoService.estadoAvisos(id);
    }
}
