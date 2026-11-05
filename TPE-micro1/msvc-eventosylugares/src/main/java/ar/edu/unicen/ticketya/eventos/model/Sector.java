package ar.edu.unicen.ticketya.eventos.model;

import jakarta.persistence.*;

@Entity
@Table(name = "sectores", uniqueConstraints = @UniqueConstraint(columnNames={"lugar_id", "nombre"}))
public class Sector {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name="lugar_id", nullable=false)
    private Lugar lugar;
    @Column(nullable = false, length = 120)
    private String nombre;
    @Column(nullable = false)
    private boolean numerado;
    @Column(nullable = false)
    private int capacidad;

    protected Sector() {}
    public Sector(Lugar lugar, String nombre, boolean numerado, int capacidad) {
        this.lugar = lugar;
        this.nombre = nombre;
        this.numerado = numerado;
        this.capacidad = capacidad;
    }

    public Long getId() {
        return id;
    }

    public Lugar getLugar() {
        return lugar;
    }

    public void setLugar(Lugar lugar) {
        this.lugar = lugar;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public boolean isNumerado() {
        return numerado;
    }

    public void setNumerado(boolean numerado) {
        this.numerado = numerado;
    }

    public int getCapacidad() {
        return capacidad;
    }

    public void setCapacidad(int capacidad) {
        this.capacidad = capacidad;
    }
}
