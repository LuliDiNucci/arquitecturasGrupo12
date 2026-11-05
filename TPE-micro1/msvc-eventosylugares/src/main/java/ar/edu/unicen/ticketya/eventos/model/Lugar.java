package ar.edu.unicen.ticketya.eventos.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "lugares")
public class Lugar {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 150)
    private String nombre;
    @Column(nullable = false, length = 220)
    private String direccion;
    @Column(nullable = false, length = 100)
    private String ciudad;
    @Column(nullable = false, precision = 9, scale = 6)
    private BigDecimal latitud;
    @Column(nullable = false, precision = 9, scale = 6)
    private BigDecimal longitud;

    protected Lugar() {}
    public Lugar(String nombre, String direccion, String ciudad, BigDecimal latitud, BigDecimal longitud) {
        this.nombre = nombre;
        this.direccion = direccion;
        this.ciudad = ciudad;
        this.latitud = latitud;
        this.longitud = longitud;
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

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getCiudad() {
        return ciudad;
    }

    public void setCiudad(String ciudad) {
        this.ciudad = ciudad;
    }

    public BigDecimal getLatitud() {
        return latitud;
    }

    public void setLatitud(BigDecimal latitud) {
        this.latitud = latitud;
    }

    public BigDecimal getLongitud() {
        return longitud;
    }

    public void setLongitud(BigDecimal longitud) {
        this.longitud = longitud;
    }
}
