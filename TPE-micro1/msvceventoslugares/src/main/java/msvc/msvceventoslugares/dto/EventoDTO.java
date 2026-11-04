package msvc.msvceventoslugares.dto;

import java.time.LocalDateTime;
import java.util.List;

import msvc.msvceventoslugares.Enums.EstadoEvento;
import msvc.msvceventoslugares.Enums.TipoEvento;

public class EventoDTO {

    private Long id;
    private String nombre;
    private String descripcion;
    private TipoEvento tipoEvento;

    private OrganizadorDTO organizador;

    private List<ParticipanteDTO> participantes;

    private LocalDateTime fechaYHora;

    private LugarDTO lugar;

    private EstadoEvento estadoEvento;

    private List<PrecioSectorDTO> precios;

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

    public OrganizadorDTO getOrganizador() {
        return organizador;
    }

    public void setOrganizador(OrganizadorDTO organizador) {
        this.organizador = organizador;
    }

    public List<ParticipanteDTO> getParticipantes() {
        return participantes;
    }

    public void setParticipantes(List<ParticipanteDTO> participantes) {
        this.participantes = participantes;
    }

    public LocalDateTime getFechaYHora() {
        return fechaYHora;
    }

    public void setFechaYHora(LocalDateTime fechaYHora) {
        this.fechaYHora = fechaYHora;
    }

    public LugarDTO getLugar() {
        return lugar;
    }

    public void setLugar(LugarDTO lugar) {
        this.lugar = lugar;
    }

    public EstadoEvento getEstadoEvento() {
        return estadoEvento;
    }

    public void setEstadoEvento(EstadoEvento estadoEvento) {
        this.estadoEvento = estadoEvento;
    }

    public List<PrecioSectorDTO> getPrecios() {
        return precios;
    }

    public void setPrecios(List<PrecioSectorDTO> precios) {
        this.precios = precios;
    }

    
}
