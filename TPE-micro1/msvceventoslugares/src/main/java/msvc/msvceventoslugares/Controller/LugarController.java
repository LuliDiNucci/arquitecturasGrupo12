package msvc.msvceventoslugares.Controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import msvc.msvceventoslugares.DTO.LugarRequestDTO;
import msvc.msvceventoslugares.DTO.LugarResponseDTO;
import msvc.msvceventoslugares.entity.Lugar;


@RestController 
@RequestMapping ("/lugar")
public class LugarController {
    
    @PostMapping
    public LugarResponseDTO crearLugar(@RequestBody LugarRequestDTO lugarNuevo) {
        //TODO: process POST request

        Lugar nuevo = new Lugar();
        nuevo.setNombre(lugarNuevo.getNombre());
        nuevo.setDireccion(lugarNuevo.getDireccion());

        LugarResponseDTO lugarCreado = new LugarResponseDTO();
        lugarCreado.setNombre(nuevo.getNombre());
        lugarCreado.setDireccion(nuevo.getDireccion());
        return lugarCreado;
    }
    
}
