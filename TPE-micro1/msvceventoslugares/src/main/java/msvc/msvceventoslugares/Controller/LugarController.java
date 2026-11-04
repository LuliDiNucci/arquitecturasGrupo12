package msvc.msvceventoslugares.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import msvc.msvceventoslugares.DTO.LugarRequestDTO;
import msvc.msvceventoslugares.DTO.LugarResponseDTO;
import msvc.msvceventoslugares.Service.LugarService;
import msvc.msvceventoslugares.entity.Lugar;


@RestController 
@RequestMapping ("/lugar")
public class LugarController {
    @Autowired 
    private LugarService lugarService;
    
    @PostMapping
    public ResponseEntity<LugarResponseDTO> crearLugar(@RequestBody LugarRequestDTO lugarNuevo) {

        return ResponseEntity.ok(
            lugarService.crearLugar(lugarNuevo)
        );
    }
    
}
