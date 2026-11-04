package msvc.msvceventoslugares.DTO;

public class LugarResponseDTO {
    private String nombre;
    private Integer capacidad;
    private String direccion;

    public LugarResponseDTO(String nombre, String direccion) {
        this.nombre = nombre;
        this.direccion = direccion;
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
}
