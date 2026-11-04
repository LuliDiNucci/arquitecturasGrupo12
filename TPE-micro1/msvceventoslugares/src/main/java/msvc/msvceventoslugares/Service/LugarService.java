package msvc.msvceventoslugares.Service;

<<<<<<< HEAD
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
=======
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import msvc.msvceventoslugares.dto.LugarDTO;
import msvc.msvceventoslugares.entity.Lugar;
import msvc.msvceventoslugares.entity.Sector;
import msvc.msvceventoslugares.repository.LugarRepository;

@Service
public class LugarService {

    @Autowired
    private LugarRepository lugarRepository;

    // CREAR LUGAR

    public LugarDTO crearLugar(LugarDTO dto) {

        Lugar lugar = new Lugar();
        lugar.setNombre(dto.getNombre());
        lugar.setDireccion(dto.getDireccion());
        lugar.setCiudad(dto.getCiudad());
        // copiar ciudad también al DTO

        lugarRepository.save(lugar);

        dto.setId(lugar.getId());
        return dto;
    }

    // OBTENER TODOS

    public List<LugarDTO> obtenerLugares() {

        return lugarRepository.findAll()
                .stream()
                .map(l -> convertirDTO(l))
                .toList();
    }

    // OBTENER POR ID

    public LugarDTO obtenerLugar(Long id) {

        Lugar lugar = lugarRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Lugar inexistente"));

        return convertirDTO(lugar);
    }

    // CONVERTIR A DTO

    private LugarDTO convertirDTO(Lugar lugar) {

        LugarDTO dto = new LugarDTO();
        dto.setId(lugar.getId());
        dto.setNombre(lugar.getNombre());
        dto.setDireccion(lugar.getDireccion());
        dto.setCiudad(lugar.getCiudad());

        return dto;
    }
}
>>>>>>> ec5c1a8504968268c23a252810d19f3f960710a8
