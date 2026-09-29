package edu.isistan.dto;

public class ReporteCarreraDTO {

    private String carrera;
    private int anio;
    private Long inscriptos;
    private Long egresados;

    public ReporteCarreraDTO(String carrera, int anio) {
        this.carrera = carrera;
        this.anio = anio;
        this.inscriptos = 0L;
        this.egresados = 0L;
    }

    public String getCarrera() {
        return carrera;
    }

    public int getAnio() {
        return anio;
    }

    public Long getInscriptos() {
        return inscriptos;
    }

    public void setInscriptos(Long inscriptos) {
        this.inscriptos = inscriptos;
    }

    public Long getEgresados() {
        return egresados;
    }

    public void setEgresados(Long egresados) {
        this.egresados = egresados;
    }
}