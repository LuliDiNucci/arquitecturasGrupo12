package msvc.msvceventoslugares.Service;

<<<<<<< HEAD
import org.springframework.beans.factory.annotation.Autowired;

import msvc.msvceventoslugares.DTO.SectorRequestDTO;
import msvc.msvceventoslugares.DTO.SectorResponseDTO;
import msvc.msvceventoslugares.entity.Sector;
import msvc.msvceventoslugares.repository.SectorRepository;

public class SectorService {
    @Autowired 
    private SectorRepository sectorRepository;

    public SectorService() {
    }

        public SectorResponseDTO crearSector(SectorRequestDTO sectorNuevo) {

            if(sectorRepository.existsById(sectorNuevo.getId())) {
                throw new IllegalArgumentException("El sector con ID " + sectorNuevo.getId() + " ya existe.");
            }

            Sector nuevo = new Sector();
            nuevo.setNombre(sectorNuevo.getNombre());
            nuevo.setNumerado(sectorNuevo.isNumerado());

            sectorRepository.save(nuevo);

            SectorResponseDTO sectorCreado = new SectorResponseDTO();

            sectorCreado.setNombre(nuevo.getNombre());
            sectorCreado.setNumerado(nuevo.isNumerado());

            return sectorCreado;
        }
=======
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
>>>>>>> ec5c1a8504968268c23a252810d19f3f960710a8
}

