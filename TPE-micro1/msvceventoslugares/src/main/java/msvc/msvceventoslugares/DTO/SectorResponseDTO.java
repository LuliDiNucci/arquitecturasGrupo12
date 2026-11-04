package msvc.msvceventoslugares.DTO;

public class SectorResponseDTO {
    private String nombre;

    private boolean numerado;

    public SectorResponseDTO(String nombre, boolean numerado) {
        this.nombre = nombre;
        this.numerado = numerado;
    }

    public SectorResponseDTO() {
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

}
