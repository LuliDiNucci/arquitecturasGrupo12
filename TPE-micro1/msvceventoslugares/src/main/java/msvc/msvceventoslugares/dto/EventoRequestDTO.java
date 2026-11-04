package msvc.msvceventoslugares.dto;

import java.time.LocalDateTime;
import java.util.List;

import msvc.msvceventoslugares.Enums.TipoEvento;

public class EventoRequestDTO {

    private String nombre;
    private String descripcion;
    private TipoEvento tipoEvento;

    private Long idOrganizador;

    private List<Long> idsParticipantes;

    private LocalDateTime fechaYHora;

    private Long idLugar;

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public TipoEvento getTipoEvento() {
        return tipoEvento;
    }

    public void setTipoEvento(TipoEvento tipoEvento) {
        this.tipoEvento = tipoEvento;
    }

    public Long getIdOrganizador() {
        return idOrganizador;
    }

    public void setIdOrganizador(Long idOrganizador) {
        this.idOrganizador = idOrganizador;
    }

    public List<Long> getIdsParticipantes() {
        return idsParticipantes;
    }

    public void setIdsParticipantes(List<Long> idsParticipantes) {
        this.idsParticipantes = idsParticipantes;
    }

    public LocalDateTime getFechaYHora() {
        return fechaYHora;
    }

    public void setFechaYHora(LocalDateTime fechaYHora) {
        this.fechaYHora = fechaYHora;
    }

    public Long getIdLugar() {
        return idLugar;
    }

    public void setIdLugar(Long idLugar) {
        this.idLugar = idLugar;
    }

    
}
