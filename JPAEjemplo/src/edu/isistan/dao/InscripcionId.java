package edu.isistan.dao;

import java.io.Serializable;
import java.util.Objects;

import javax.persistence.Embeddable;

/*
 * @Embeddable indica que esta clase NO va a ser una entidad
 * ni va a generar una tabla propia.
 *
 * Esta clase solamente sirve para agrupar los campos
 * que juntos forman la clave primaria de Inscripcion.
 *
 * En nuestro caso:
 * PK = idEstudiante + idCarrera
 */
@Embeddable
public class InscripcionId implements Serializable {

    /*
     * Esta es una parte de la clave primaria.
     * Va a guardar el id del estudiante.
     */
    private int idEstudiante;

    /*
     * Esta es la otra parte de la clave primaria.
     * Va a guardar el id de la carrera.
     */
    private int idCarrera;


    /*
     * JPA necesita un constructor vacío.
     */
    public InscripcionId() {
    }



    public InscripcionId(int idEstudiante, int idCarrera) {
        this.idEstudiante = idEstudiante;
        this.idCarrera = idCarrera;
    }


    public int getIdEstudiante() {
        return idEstudiante;
    }

    public void setIdEstudiante(int idEstudiante) {
        this.idEstudiante = idEstudiante;
    }


    public int getIdCarrera() {
        return idCarrera;
    }

    public void setIdCarrera(int idCarrera) {
        this.idCarrera = idCarrera;
    }


    @Override
    public boolean equals(Object obj) {

        // Si son exactamente el mismo objeto
        if (this == obj) {
            return true;
        }

        // Si el otro objeto no es un InscripcionId
        if (!(obj instanceof InscripcionId)) {
            return false;
        }

        // Convertimos el objeto al tipo InscripcionId
        InscripcionId otro = (InscripcionId) obj;

        // Comparamos las dos partes de la PK
        return this.idEstudiante == otro.idEstudiante
                && this.idCarrera == otro.idCarrera;
    }


    /*
     * hashCode trabaja junto con equals.
     *
     * JPA lo necesita para manejar correctamente
     * las entidades con claves compuestas.
     */
    @Override
    public int hashCode() {
        return Objects.hash(idEstudiante, idCarrera);
    }
}