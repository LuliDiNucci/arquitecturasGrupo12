package edu.isistan;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import edu.isistan.dao.Carrera;
import edu.isistan.dao.Estudiante;

public class Select {

	public static void main(String[] args) {

		EntityManagerFactory emf =
				Persistence.createEntityManagerFactory("Example");

		EntityManager em = emf.createEntityManager();

		em.getTransaction().begin();


		// 1. IMPRIMIR TODOS LOS ESTUDIANTES

		List<Estudiante> estudiantes =
				em.createQuery(
					"SELECT e FROM Estudiante e",
					Estudiante.class
				).getResultList();

		System.out.println("ESTUDIANTES:");

		for (Estudiante e : estudiantes) {
			System.out.println(e);
		}


		System.out.println("--------------------------------");


		// 2. IMPRIMIR TODAS LAS CARRERAS

		List<Carrera> carreras =
				em.createQuery(
					"SELECT c FROM Carrera c",
					Carrera.class
				).getResultList();

		System.out.println("CARRERAS:");

		for (Carrera c : carreras) {
			System.out.println(c);
		}


		System.out.println("--------------------------------");


		// 3. BUSCAR ESTUDIANTE POR ID

		Estudiante estudiante =
				em.find(Estudiante.class, 12345678);

		System.out.println("ESTUDIANTE POR ID:");

		System.out.println(estudiante);


		System.out.println("--------------------------------");


		// 4. BUSCAR CARRERA POR ID

		Carrera carrera =
				em.find(Carrera.class, 1);

		System.out.println("CARRERA POR ID:");

		System.out.println(carrera);


		em.getTransaction().commit();

		em.close();
		emf.close();
	}
}