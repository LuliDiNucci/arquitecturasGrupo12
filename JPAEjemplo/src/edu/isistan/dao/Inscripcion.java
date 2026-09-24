package edu.isistan.dao;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;


/*
 * @Entity indica que esta clase representa
 * una tabla de la base de datos.
 * En este caso representa la tabla INSCRIPCION.
 */
@Entity
public class Inscripcion {

    /*
     * @EmbeddedId indica que la clave primaria
     * de esta entidad está formada por un objeto.
     *
     * Ese objeto es InscripcionId.
     *
     * InscripcionId contiene:
     *
     * idEstudiante
     * idCarrera
     *
     * Por lo tanto:
     *
     * PK de Inscripcion =
     * (idEstudiante, idCarrera)
     */
    @EmbeddedId
    private InscripcionId id;


    /*
     Muchas inscripciones pueden pertenecer
     al mismo estudiante.
     */
    @ManyToOne

    /*
     * @MapsId("idEstudiante") indica que el ID
     * del estudiante se corresponde con:
     *
     * id.idEstudiante
     *
     * Es decir, ambos usan el mismo valor.
     */
    @MapsId("idEstudiante")

    /*
     * @JoinColumn indica qué columna de la tabla
     * Inscripcion es la FK que apunta a Estudiante.
     *
     * INSCRIPCION.idEstudiante
     *            ↓
     * ESTUDIANTE.idEstudiante
     */
    @JoinColumn(name = "idEstudiante")
    private Estudiante estudiante;


    /*
     Muchas inscripciones pueden pertenecer
     a una misma carrera.
     */
    @ManyToOne

    /*
     * Le decimos que el id de esta Carrera
     * corresponde a:
     *
     * id.idCarrera
     */
    @MapsId("idCarrera")

    /*
     * Esta es la FK que apunta a la tabla Carrera.
     */
    @JoinColumn(name = "idCarrera")
    private Carrera carrera;

    @Column
    private int antiguedad;


    /*
     * Constructor vacío obligatorio/recomendado
     * para que JPA pueda crear objetos de esta clase.
     */
    public Inscripcion() {
    }

    
    public Inscripcion(
            Estudiante estudiante,
            Carrera carrera,
            int antiguedad) {

        this.estudiante = estudiante;
        this.carrera = carrera;
        this.antiguedad = antiguedad;

        /*
         id del estudiante + id de la carrera (InscripcionId)
         */
        this.id = new InscripcionId(
                estudiante.getDni(),
                carrera.getId()
        );
    }


    // Getter de la PK compuesta
    public InscripcionId getId() {
        return id;
    }

    public void setId(InscripcionId id) {
        this.id = id;
    }


    // Getter y setter de Estudiante
    public Estudiante getEstudiante() {
        return estudiante;
    }

    public void setEstudiante(Estudiante estudiante) {
        this.estudiante = estudiante;
    }


    // Getter y setter de Carrera
    public Carrera getCarrera() {
        return carrera;
    }

    public void setCarrera(Carrera carrera) {
        this.carrera = carrera;
    }


    // Getter y setter de antiguedad
    public int getAntiguedad() {
        return antiguedad;
    }

    public void setAntiguedad(int antiguedad) {
        this.antiguedad = antiguedad;
    }
}