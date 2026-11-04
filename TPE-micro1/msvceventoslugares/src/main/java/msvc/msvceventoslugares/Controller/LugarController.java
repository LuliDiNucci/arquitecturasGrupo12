package msvc.msvceventoslugares.Controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import msvc.msvceventoslugares.Service.LugarService;
import msvc.msvceventoslugares.dto.LugarDTO;
import msvc.msvceventoslugares.entity.Sector;
import msvc.msvceventoslugares.Service.SectorService;

@RestController
@RequestMapping("/lugares")
public class LugarController {

    @Autowired
    private LugarService lugarService;

    @Autowired
    private SectorService sectorService;

    // CREAR LUGAR
    @PostMapping
    public ResponseEntity<LugarDTO> crearLugar(
            @RequestBody LugarDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(lugarService.crearLugar(dto));
    }

    // OBTENER TODOS
    @GetMapping
    public List<LugarDTO> obtenerLugares() {
        return lugarService.obtenerLugares();
    }

    // OBTENER POR ID
    @GetMapping("/{id}")
    public LugarDTO obtenerLugar(
            @PathVariable Long id) {
        return lugarService.obtenerLugar(id);
    }

    // CREAR SECTOR EN UN LUGAR
    @PostMapping("/{id}/sectores")
    public ResponseEntity<Sector> crearSector(
            @PathVariable Long id,
            @RequestBody Sector sector) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(sectorService.crearSector(id, sector));
    }
}
