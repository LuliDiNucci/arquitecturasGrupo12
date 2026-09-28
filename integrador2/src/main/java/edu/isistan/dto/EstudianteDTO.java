package edu.isistan.dto;

public class EstudianteDTO {

    private int dni;
    private String nombre;
    private String apellido;
    private int edad;
    private String genero;
    private String ciudad;
    private int LU;

    public EstudianteDTO(
            int dni,
            String nombre,
            String apellido,
            int edad,
            String genero,
            String ciudad,
            int LU) {

        this.dni = dni;
        this.nombre = nombre;
        this.apellido = apellido;
        this.edad = edad;
        this.genero = genero;
        this.ciudad = ciudad;
        this.LU = LU;
    }

    public int getDni() {
        return dni;
    }

    public String getNombre() {
        return nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public int getEdad() {
        return edad;
    }

    public String getGenero() {
        return genero;
    }

    public String getCiudad() {
        return ciudad;
    }

    public int getLU() {
        return LU;
    }
}
