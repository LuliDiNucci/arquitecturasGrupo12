package com.ticketya.notificaciones.controllers;

import java.net.URI;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.ticketya.notificaciones.dtos.ArtistaFavoritoRequestDTO;
import com.ticketya.notificaciones.dtos.CompraConfirmadaRequestDTO;
import com.ticketya.notificaciones.dtos.EventoCanceladoRequestDTO;
import com.ticketya.notificaciones.dtos.EventoModificadoRequestDTO;
import com.ticketya.notificaciones.dtos.NotificacionResponseDTO;
import com.ticketya.notificaciones.entities.EstadoNotificacion;
import com.ticketya.notificaciones.services.NotificacionService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;

@RestController
@RequestMapping("/notificaciones")
@Tag(name = "Notificaciones", description = "Recepción de solicitudes y consulta de envíos por correo")
public class NotificacionController {

        private final NotificacionService service;

        public NotificacionController(NotificacionService service) {
                this.service = service;
        }

        @PostMapping("/compras-confirmadas")
        @Operation(summary = "Avisar que una compra fue confirmada", description = "Se registra un correo por compra y cliente. El envío se procesa en segundo plano.")
        @ApiResponse(responseCode = "202", description = "Notificación pendiente", headers = @Header(name = "Location", description = "Ruta para consultar el estado"))
        @ApiResponse(responseCode = "200", description = "La notificación ya tiene un estado final")
        @ApiResponse(responseCode = "400", description = "Solicitud inválida", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
        public ResponseEntity<NotificacionResponseDTO> compraConfirmada(
                        @Valid @RequestBody CompraConfirmadaRequestDTO solicitud) {
                return responderRegistro(service.registrarCompraConfirmada(solicitud));
        }

        @PostMapping("/eventos-cancelados")
        @Operation(summary = "Avisar que un evento fue cancelado", description = "El solicitante aporta los datos de un comprador. Se registra un correo por evento y cliente.")
        @ApiResponse(responseCode = "202", description = "Notificación pendiente", headers = @Header(name = "Location", description = "Ruta para consultar el estado"))
        @ApiResponse(responseCode = "200", description = "La notificación ya tiene un estado final")
        @ApiResponse(responseCode = "400", description = "Solicitud inválida", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
        public ResponseEntity<NotificacionResponseDTO> eventoCancelado(
                        @Valid @RequestBody EventoCanceladoRequestDTO solicitud) {
                return responderRegistro(service.registrarEventoCancelado(solicitud));
        }

        @PostMapping("/eventos-modificados")
        @Operation(summary = "Avisar que un evento fue modificado", description = "Se registra un correo por evento, versión del cambio y cliente.")
        @ApiResponse(responseCode = "202", description = "Notificación pendiente", headers = @Header(name = "Location", description = "Ruta para consultar el estado"))
        @ApiResponse(responseCode = "200", description = "La notificación ya tiene un estado final")
        @ApiResponse(responseCode = "400", description = "Solicitud inválida", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
        public ResponseEntity<NotificacionResponseDTO> eventoModificado(
                        @Valid @RequestBody EventoModificadoRequestDTO solicitud) {
                return responderRegistro(service.registrarEventoModificado(solicitud));
        }

        @PostMapping("/artistas-favoritos")
        @Operation(summary = "Avisar que se publicó un evento de un artista favorito", description = "El solicitante aporta las preferencias de un cliente. Se verifica que el artista sea "
                        + "favorito y que la ciudad coincida. Se registra un correo por evento y cliente.")
        @ApiResponse(responseCode = "202", description = "Notificación pendiente", headers = @Header(name = "Location", description = "Ruta para consultar el estado"))
        @ApiResponse(responseCode = "200", description = "La notificación ya tiene un estado final")
        @ApiResponse(responseCode = "204", description = "El artista o la ciudad no coincide", content = @Content)
        @ApiResponse(responseCode = "400", description = "Solicitud inválida", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
        public ResponseEntity<NotificacionResponseDTO> artistaFavorito(
                        @Valid @RequestBody ArtistaFavoritoRequestDTO solicitud) {
                Optional<NotificacionResponseDTO> notificacion = service.registrarArtistaFavorito(solicitud);

                if (notificacion.isEmpty()) {
                        return ResponseEntity.noContent().build();
                }

                return responderRegistro(notificacion.get());
        }

        @GetMapping("/{id}")
        @Operation(summary = "Consultar el estado de una notificación")
        @ApiResponse(responseCode = "200", description = "Estado de la notificación")
        @ApiResponse(responseCode = "400", description = "ID inválido", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
        @ApiResponse(responseCode = "404", description = "Notificación inexistente", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
        public NotificacionResponseDTO buscarPorId(@PathVariable @Positive long id) {
                return service.buscarPorId(id)
                                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                                                "Notificación inexistente"));
        }

        private ResponseEntity<NotificacionResponseDTO> responderRegistro(NotificacionResponseDTO notificacion) {
                HttpStatus status = notificacion.estado() == EstadoNotificacion.PENDIENTE
                                ? HttpStatus.ACCEPTED
                                : HttpStatus.OK;
                return ResponseEntity.status(status)
                                .location(URI.create("/notificaciones/" + notificacion.id()))
                                .body(notificacion);
        }
}
