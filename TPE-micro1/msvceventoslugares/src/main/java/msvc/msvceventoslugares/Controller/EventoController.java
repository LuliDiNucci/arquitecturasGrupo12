package msvc.msvceventoslugares.Controller;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import msvc.msvceventoslugares.Enums.TipoEvento;
import msvc.msvceventoslugares.Service.EventoService;
import msvc.msvceventoslugares.dto.EventoDTO;
import msvc.msvceventoslugares.dto.EventoRequestDTO;
import msvc.msvceventoslugares.dto.PrecioSectorDTO;

@RestController
@RequestMapping("/eventos")
public class EventoController {

    @Autowired
    private EventoService eventoService;

    // CREAR EVENTO
    @PostMapping
    public ResponseEntity<EventoDTO> crearEvento(
        @RequestBody EventoRequestDTO dto
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(eventoService.crearEvento(dto));
    }

    // OBTENER TODOS
    @GetMapping
    public List<EventoDTO> obtenerEventos() {
        return eventoService.obtenerEventos();
    }

    // OBTENER POR ID
    @GetMapping("/{id}")
    public EventoDTO obtenerEvento(
        @PathVariable Long id
    ) {
        return eventoService.obtenerEvento(id);
    }

    // MODIFICAR EVENTO
    @PutMapping("/{id}")
    public EventoDTO modificarEvento(
        @PathVariable Long id,
        @RequestBody EventoRequestDTO dto
    ) {
        return eventoService.modificarEvento(id, dto);
    }

    // MODIFICAR PRECIOS
    @PutMapping("/{id}/precios")
    public EventoDTO modificarPrecios(
        @PathVariable Long id,
        @RequestBody List<PrecioSectorDTO> precios
    ) {
        return eventoService.modificarPrecios(id, precios);
    }

    // PUBLICAR
    @PatchMapping("/{id}/publicar")
    public EventoDTO publicarEvento(
        @PathVariable Long id
    ) {
        return eventoService.publicarEvento(id);
    }

    // CANCELAR
    @PatchMapping("/{id}/cancelar")
    public EventoDTO cancelarEvento(
        @PathVariable Long id
    ) {
        return eventoService.cancelarEvento(id);
    }

    // SUSPENDER
    @PatchMapping("/{id}/suspender")
    public EventoDTO suspenderEvento(
        @PathVariable Long id
    ) {
        return eventoService.suspenderEvento(id);
    }

    // BUSCAR EVENTOS
    @GetMapping("/buscar")
    public List<EventoDTO> buscarEventos(
        @RequestParam(required = false) TipoEvento tipo,
        @RequestParam(required = false) Long idOrganizador,
        @RequestParam(required = false) Long idParticipante,
        @RequestParam(required = false) String ciudad,
        @RequestParam(required = false) LocalDateTime fechaDesde,
        @RequestParam(required = false) LocalDateTime fechaHasta,
        @RequestParam(required = false) String ordenarPor
    ) {
        return eventoService.buscarEventos(
            tipo, idOrganizador, idParticipante,
            ciudad, fechaDesde, fechaHasta, ordenarPor
        );
    }
}