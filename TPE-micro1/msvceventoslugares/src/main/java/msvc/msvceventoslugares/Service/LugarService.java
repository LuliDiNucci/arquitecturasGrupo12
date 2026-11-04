package msvc.msvceventoslugares.Service;

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
