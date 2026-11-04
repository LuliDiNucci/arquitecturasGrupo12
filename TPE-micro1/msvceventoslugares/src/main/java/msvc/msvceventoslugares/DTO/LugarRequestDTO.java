package msvc.msvceventoslugares.DTO;

public class LugarRequestDTO {
    private String nombre;
    private Integer capacidad;
    private String direccion;

    public LugarRequestDTO(String nombre, String direccion) {
        this.nombre = nombre;
        this.direccion = direccion;
    }

    //TO DO sector_id

    public LugarRequestDTO() {
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
}
