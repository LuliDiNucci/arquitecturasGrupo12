package msvc.msvceventoslugares.Service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import msvc.msvceventoslugares.entity.Asiento;
import msvc.msvceventoslugares.entity.Sector;
import msvc.msvceventoslugares.repository.AsientoRepository;
import msvc.msvceventoslugares.repository.SectorRepository;

@Service
public class AsientoService {

    @Autowired
    private AsientoRepository asientoRepository;

    @Autowired
    private SectorRepository sectorRepository;

    // CREAR ASIENTO EN UN SECTOR

    public Asiento crearAsiento(Long idSector, Asiento asiento) {

        Sector sector = sectorRepository.findById(idSector)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sector inexistente"));

        asiento.setSector(sector);

        return asientoRepository.save(asiento);
    }

    // OBTENER ASIENTOS DE UN SECTOR

    public List<Asiento> obtenerAsientosDeSector(Long idSector) {

        return asientoRepository.findBySectorId(idSector);
    }
}
