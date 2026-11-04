package msvc.msvceventoslugares.entity;

import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.util.List;

import jakarta.persistence.OneToMany;

@Entity
public class Lugar {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;

    private String direccion;

    private String ciudad;

<<<<<<< HEAD
    private double latitud;

    private double longitud;

    @OneToMany(mappedBy = "idLugar")
    private List<Sector> sector;
=======
    @OneToMany(mappedBy = "idLugar")
    private List<Sector> sectores;
>>>>>>> ec5c1a8504968268c23a252810d19f3f960710a8

    public Lugar() {
    }

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

<<<<<<< HEAD
    public double getLatitud() {
        return latitud;
    }

    public void setLatitud(double latitud) {
        this.latitud = latitud;
    }

    public double getLongitud() {
        return longitud;
    }

    public void setLongitud(double longitud) {
        this.longitud = longitud;
    }

    public List<Sector> getSectores() {
        return sector;
    }

    public void setSectores(List<Sector> sector) {
        this.sector = sector;
    }

=======
    public List<Sector> getSectores() {
        return sectores;
    }

    public void setSectores(List<Sector> sectores) {
        this.sectores = sectores;
    }
>>>>>>> ec5c1a8504968268c23a252810d19f3f960710a8
}
