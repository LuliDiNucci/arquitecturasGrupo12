package ar.edu.unicen.ticketya.eventos.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name="eventos", indexes={@Index(name="idx_eventos_fecha", columnList="fecha_hora"), @Index(name="idx_eventos_estado_tipo", columnList="estado,tipo")})
public class Evento {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 180)
    private String nombre;
    @Column(nullable = false, length = 4000)
    private String descripcion;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 35)
    private TipoEvento tipo;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name="organizador_id", nullable=false)
    private Organizador organizador;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name="lugar_id", nullable=false)
    private Lugar lugar;
    @Column(nullable = false)
    private LocalDateTime fechaHora;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private EstadoEvento estado;
    @Column(nullable = false)
    private boolean altaDemanda;
    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private EstadoEvento estadoPrevioSuspension;
    // Evita sobrescribir cambios simultaneos de estado o datos del evento.
    @Version
    private Long version;

    protected Evento() {
    }

    public Evento(String nombre, String descripcion, TipoEvento tipo, Organizador organizador, Lugar lugar, LocalDateTime fechaHora, EstadoEvento estado, boolean altaDemanda) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.tipo = tipo;
        this.organizador = organizador;
        this.lugar = lugar;
        this.fechaHora = fechaHora;
        this.estado = estado;
        this.altaDemanda = altaDemanda;
    }

    public Long getId() {
        return id;
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

    public TipoEvento getTipo() {
        return tipo;
    }

    public void setTipo(TipoEvento tipo) {
        this.tipo = tipo;
    }

    public Organizador getOrganizador() {
        return organizador;
    }

    public void setOrganizador(Organizador organizador) {
        this.organizador = organizador;
    }

    public Lugar getLugar() {
        return lugar;
    }

    public void setLugar(Lugar lugar) {
        this.lugar = lugar;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(LocalDateTime fechaHora) {
        this.fechaHora = fechaHora;
    }

    public EstadoEvento getEstado() {
        return estado;
    }

    public void setEstado(EstadoEvento estado) {
        this.estado = estado;
    }

    public EstadoEvento getEstadoPrevioSuspension() {
        return estadoPrevioSuspension;
    }

    public void setEstadoPrevioSuspension(EstadoEvento estadoPrevioSuspension) {
        this.estadoPrevioSuspension = estadoPrevioSuspension;
    }

    public boolean isAltaDemanda() {
        return altaDemanda;
    }

    public void setAltaDemanda(boolean altaDemanda) {
        this.altaDemanda = altaDemanda;
    }

    public Long getVersion() {
        return version;
    }
}
