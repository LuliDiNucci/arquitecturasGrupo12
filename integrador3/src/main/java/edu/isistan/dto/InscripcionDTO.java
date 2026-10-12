
package edu.isistan.dto;

import edu.isistan.modelo.Inscripcion;

public class InscripcionDTO {

    private int id;
    private int dniEstudiante;
    private int idCarrera;
    private int anioInscripcion;
    private int anioGraduacion;
    private int antiguedad;

    public InscripcionDTO(Inscripcion inscripcion) {
        this.id = inscripcion.getId();
        this.dniEstudiante = inscripcion.getEstudiante().getDni();
        this.idCarrera = inscripcion.getCarrera().getId();
        this.anioInscripcion = inscripcion.getInscripcion();
        this.anioGraduacion = inscripcion.getGraduacion();
        this.antiguedad = inscripcion.getAntiguedad();
    }

    public int getId() {
        return id;
    }

    public int getDniEstudiante() {
        return dniEstudiante;
    }

    public int getIdCarrera() {
        return idCarrera;
    }

    public int getAnioInscripcion() {
        return anioInscripcion;
    }

    public int getAnioGraduacion() {
        return anioGraduacion;
    }

    public int getAntiguedad() {
        return antiguedad;
    }
}
