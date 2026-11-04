package msvc.msvceventoslugares.DTO;

import java.util.ArrayList;
import java.util.List;

import msvc.msvceventoslugares.entity.Sector;

public class LugarResponseDTO {
    private String nombre;
    private String direccion;
    private String ciudad;
    private double latitud;
    private double longitud;
    private List<Sector> sectores;

    public LugarResponseDTO(String nombre, String direccion, String ciudad, double latitud, double longitud, List<Sector> sectores) {
        this.nombre = nombre;
        this.direccion = direccion;
        this.ciudad = ciudad;
        this.latitud = latitud;
        this.longitud = longitud;
        this.sectores = sectores;
    }


    public LugarResponseDTO() {
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
        return new ArrayList<>(this.sectores);
    }

    public void setSectores(List<Sector> sectores) {
        this.sectores = sectores;
    }
}
