package edu.isistan.modelo;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;


@Entity
public class Inscripcion {

	/*
	 * Cada inscripción tiene su propio ID.
	 * CSV:
	 *
	 * id,id_estudiante,id_carrera,inscripcion,graduacion,antiguedad
	 * 1,71779527,15,2017,2022,5
	 */
	@Id
	private int id;

	@ManyToOne
	@JoinColumn(name = "id_estudiante")
	private Estudiante estudiante;

	@ManyToOne
	@JoinColumn(name = "id_carrera")
	private Carrera carrera;

	@Column
	private int inscripcion;

	@Column
	private int graduacion;


	@Column
	private int antiguedad;


	public Inscripcion() {
	}

	public Inscripcion(
			int id,
			Estudiante estudiante,
			Carrera carrera,
			int inscripcion,
			int graduacion,
			int antiguedad) {

		this.id = id;
		this.estudiante = estudiante;
		this.carrera = carrera;
		this.inscripcion = inscripcion;
		this.graduacion = graduacion;
		this.antiguedad = antiguedad;
	}


	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}


	public Estudiante getEstudiante() {
		return estudiante;
	}

	public void setEstudiante(Estudiante estudiante) {
		this.estudiante = estudiante;
	}


	public Carrera getCarrera() {
		return carrera;
	}

	public void setCarrera(Carrera carrera) {
		this.carrera = carrera;
	}


	public int getInscripcion() {
		return inscripcion;
	}

	public void setInscripcion(int inscripcion) {
		this.inscripcion = inscripcion;
	}


	public int getGraduacion() {
		return graduacion;
	}

	public void setGraduacion(int graduacion) {
		this.graduacion = graduacion;
	}


	public int getAntiguedad() {
		return antiguedad;
	}

	public void setAntiguedad(int antiguedad) {
		this.antiguedad = antiguedad;
	}


	@Override
	public String toString() {
		return "Inscripcion [id=" + id
				+ ", estudiante=" + estudiante.getDni()
				+ ", carrera=" + carrera.getId()
				+ ", inscripcion=" + inscripcion
				+ ", graduacion=" + graduacion
				+ ", antiguedad=" + antiguedad
				+ "]";
	}
}

