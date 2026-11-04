package msvc.msvceventoslugares.Service;

import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;

import msvc.msvceventoslugares.DTO.LugarRequestDTO;
import msvc.msvceventoslugares.DTO.LugarResponseDTO;
import msvc.msvceventoslugares.entity.Lugar;
import msvc.msvceventoslugares.repository.LugarRepository;

public class LugarService {
    @Autowired 
    private LugarRepository lugarRepository;

    public LugarService() {
    }

    public LugarResponseDTO crearLugar(LugarRequestDTO lugarNuevo) {

        if(lugarRepository.existsById(lugarNuevo.getId())) {
            throw new IllegalArgumentException("El lugar con ID " + lugarNuevo.getId() + " ya existe.");
        }
        
        Lugar nuevo = new Lugar();
        nuevo.setNombre(lugarNuevo.getNombre());
        nuevo.setDireccion(lugarNuevo.getDireccion());
        nuevo.setCiudad(lugarNuevo.getCiudad());
        nuevo.setLatitud(lugarNuevo.getLatitud());
        nuevo.setLongitud(lugarNuevo.getLongitud());
        nuevo.setSectores(new ArrayList<>());

        lugarRepository.save(nuevo);

        LugarResponseDTO lugarCreado = new LugarResponseDTO();

        lugarCreado.setNombre(nuevo.getNombre());
        lugarCreado.setDireccion(nuevo.getDireccion());
        lugarCreado.setCiudad(nuevo.getCiudad());
        lugarCreado.setLatitud(nuevo.getLatitud());
        lugarCreado.setLongitud(nuevo.getLongitud());
        lugarCreado.setSectores(nuevo.getSectores());


        return lugarCreado;
    }
}