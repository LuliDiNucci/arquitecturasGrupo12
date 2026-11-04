package msvc.msvceventoslugares.Service;

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
}

