package edu.isistan.modelo;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;

@Entity
public class Estudiante {

	@Id
	private int dni;

	@Column
	private String nombre;

	@Column
	private String apellido;

	@Column
	private int edad;

	@Column
	private String genero;

	@Column
	private String ciudad;

	@Column
	private int LU;

	@OneToMany(mappedBy = "estudiante", fetch = FetchType.LAZY)
	private List<Inscripcion> inscripciones;


	public Estudiante() {
		super();
		this.inscripciones = new ArrayList<Inscripcion>();
	}


	public Estudiante(
			int dni,
			String nombre,
			String apellido,
			int edad,
			String genero,
			String ciudad,
			int LU) {

		super();

		this.dni = dni;
		this.nombre = nombre;
		this.apellido = apellido;
		this.edad = edad;
		this.genero = genero;
		this.ciudad = ciudad;
		this.LU = LU;

		this.inscripciones = new ArrayList<Inscripcion>();
	}


	public int getDni() {
		return dni;
	}

	public void setDni(int dni) {
		this.dni = dni;
	}


	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}


	public String getApellido() {
		return apellido;
	}

	public void setApellido(String apellido) {
		this.apellido = apellido;
	}


	public int getEdad() {
		return edad;
	}

	public void setEdad(int edad) {
		this.edad = edad;
	}


	public String getGenero() {
		return genero;
	}

	public void setGenero(String genero) {
		this.genero = genero;
	}


	public String getCiudad() {
		return ciudad;
	}

	public void setCiudad(String ciudad) {
		this.ciudad = ciudad;
	}


	public int getLU() {
		return LU;
	}

	public void setLU(int LU) {
		this.LU = LU;
	}


	public List<Inscripcion> getInscripciones() {
		return inscripciones;
	}

	public void setInscripciones(List<Inscripcion> inscripciones) {
		this.inscripciones = inscripciones;
	}


	public void addInscripcion(Inscripcion inscripcion) {
		this.inscripciones.add(inscripcion);
	}


	@Override
	public String toString() {
		return "Estudiante [dni=" + dni
				+ ", nombre=" + nombre
				+ ", apellido=" + apellido
				+ ", edad=" + edad
				+ ", genero=" + genero
				+ ", ciudad=" + ciudad
				+ ", LU=" + LU
				+ "]";
	}
}

