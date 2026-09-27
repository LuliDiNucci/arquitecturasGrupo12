package edu.isistan.dao;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;

@Entity
public class Carrera {
	
	@Id
	private int id;

	@Column(nullable = false)
	private String nombre;
	

	@OneToMany(mappedBy = "carrera", fetch = FetchType.LAZY)
	private List<Inscripcion> inscripciones;
	
	
	public Carrera() {
		super();
		this.inscripciones = new ArrayList<Inscripcion>();
	}

	
	public Carrera(int id, String nombre) {
		super();
		this.id = id;
		this.nombre = nombre;
		this.inscripciones = new ArrayList<Inscripcion>();
	}

	
	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public int getId() {
		return id;
	}

	

	public List<Inscripcion> getInscripciones() {
		return new ArrayList<Inscripcion>(inscripciones);
	}

	

	public void addInscripcion(Inscripcion inscripcion) {
		this.inscripciones.add(inscripcion);
	}

	
	/*
	 si quiero imprimir los estudiantes de la carrera
	 tengo q pasar por cada Inscripcion y obtener
	 su Estudiante.
	 */
	public void printEstudiantes() {
		for (Inscripcion i : inscripciones) {

			Estudiante e = i.getEstudiante();

			System.out.println(
				e.getNombre() + " " + e.getApellido()
			);
		}
	}

	
	@Override
	public String toString() {
		return "Carrera [id=" + id + ", nombre=" + nombre + "]";
	}
}