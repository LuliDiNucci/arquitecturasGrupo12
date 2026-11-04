package msvc.msvceventoslugares.dto;

import msvc.msvceventoslugares.Enums.TipoParticipante;

public class ParticipanteDTO {

    private Long id;
    private String nombre;
    private TipoParticipante tipoParticipante;
    
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public TipoParticipante getTipoParticipante() {
        return tipoParticipante;
    }

    public void setTipoParticipante(TipoParticipante tipoParticipante) {
        this.tipoParticipante = tipoParticipante;
    }
}