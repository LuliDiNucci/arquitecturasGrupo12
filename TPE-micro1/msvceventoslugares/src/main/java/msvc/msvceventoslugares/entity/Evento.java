package msvc.msvceventoslugares.entity;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import msvc.msvceventoslugares.Enums.EstadoEvento;
import msvc.msvceventoslugares.Enums.TipoEvento;
import msvc.msvceventoslugares.entity.Lugar;

@Entity
public class Evento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;

    private String descripcion;

    @Enumerated(EnumType.STRING)
    private TipoEvento tipoEvento;

    @ManyToOne
    @JoinColumn(name = "id_organizador")
    private Organizador organizador;

    @ElementCollection
    private List<Long> participantes;

    private LocalDateTime fechaYHora;

    @ManyToOne
    @JoinColumn(name = "id_lugar")
    private Lugar lugar;

    @Enumerated(EnumType.STRING)
    private EstadoEvento estadoEvento;

    public Evento() {
    }

    // Getters y setters

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

    public Organizador getOrganizador() {
        return organizador;
    }

    public void setOrganizador(Organizador organizador) {
        this.organizador = organizador;
    }

    public List<Long> getParticipantes() {
        return participantes;
    }

    public void setParticipantes(List<Long> participantes) {
        this.participantes = participantes;
    }

    public LocalDateTime getFechaYHora() {
        return fechaYHora;
    }

    public void setFechaYHora(LocalDateTime fechaYHora) {
        this.fechaYHora = fechaYHora;
    }

    public Lugar getLugar() {
        return lugar;
    }

    public void setLugar(Lugar lugar) {
        this.lugar = lugar;
    }

    public EstadoEvento getEstadoEvento() {
        return estadoEvento;
    }

    public void setEstadoEvento(EstadoEvento estadoEvento) {
        this.estadoEvento = estadoEvento;
    }
}