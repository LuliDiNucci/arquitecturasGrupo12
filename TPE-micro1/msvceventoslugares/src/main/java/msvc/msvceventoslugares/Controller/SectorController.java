package msvc.msvceventoslugares.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import msvc.msvceventoslugares.Service.AsientoService;
import msvc.msvceventoslugares.entity.Asiento;

@RestController
@RequestMapping("/sectores")
public class SectorController {

    @Autowired
    private AsientoService asientoService;

    // CREAR ASIENTO EN UN SECTOR
    @PostMapping("/{id}/asientos")
    public ResponseEntity<Asiento> crearAsiento(
            @PathVariable Long id,
            @RequestBody Asiento asiento) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(asientoService.crearAsiento(id, asiento));
    }
}
