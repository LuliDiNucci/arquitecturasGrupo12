package msvc.msvceventoslugares.DTO;

public class SectorRequestDTO {
    private Long id;
    private String nombre;
    private boolean numerado;

    public SectorRequestDTO(Long id, String nombre, boolean numerado) {
        this.id = id;
        this.nombre = nombre;
        this.numerado = numerado;
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

    public boolean isNumerado() {
        return numerado;
    }

    public void setNumerado(boolean numerado) {
        this.numerado = numerado;
    }

}
