package msvc.msvceventoslugares.Service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import msvc.msvceventoslugares.entity.Lugar;
import msvc.msvceventoslugares.entity.Lugar;
import msvc.msvceventoslugares.entity.Sector;
import msvc.msvceventoslugares.repository.LugarRepository;
import msvc.msvceventoslugares.repository.SectorRepository;

@Service
public class SectorService {

    @Autowired
    private SectorRepository sectorRepository;

    @Autowired
    private LugarRepository lugarRepository;

    // CREAR SECTOR EN UN LUGAR

    public Sector crearSector(Long idLugar, Sector sector) {

        Lugar lugar = lugarRepository.findById(idLugar)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Lugar inexistente"));

        sector.setIdLugar(lugar);

        return sectorRepository.save(sector);
    }

    // OBTENER SECTORES DE UN LUGAR

    public List<Sector> obtenerSectoresDeLugar(Long idLugar) {

        return sectorRepository.findByIdLugarId(idLugar);
    }
}
