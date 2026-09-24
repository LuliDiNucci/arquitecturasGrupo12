package edu.isistan;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import edu.isistan.dao.Carrera;
import edu.isistan.dao.Estudiante;
import edu.isistan.dao.Inscripcion;

public class Insert {

	public static void main(String[] args) {

		// Creo el EntityManagerFactory usando el nombre
		// que tengas definido en persistence.xml
		EntityManagerFactory emf =
				Persistence.createEntityManagerFactory("Example");

		// Creo el EntityManager
		EntityManager em = emf.createEntityManager();


		try {

			// Inicio una transacción
			em.getTransaction().begin();


			// 1. Creo un estudiante
			Estudiante estudiante = new Estudiante(
					12345678,
					"Julia",
					"Doxagarat",
					22,
					"F",
					"Tandil",
					1001,
					false
			);


			// 2. Creo una carrera
			Carrera carrera = new Carrera(
					1,
					"Ingenieria en Computacion"
			);


			// 3. Guardo primero Estudiante y Carrera
			em.persist(estudiante);
			em.persist(carrera);


			// 4. Creo la inscripción que relaciona ambos
			Inscripcion inscripcion =
					new Inscripcion(
							estudiante,
							carrera,
							2
					);


			// 5. Agrego la inscripción a las listas
			// de ambos objetos
			estudiante.addInscripcion(inscripcion);
			carrera.addInscripcion(inscripcion);


			// 6. Guardo la inscripción
			em.persist(inscripcion);


			// Confirmo los cambios en la base
			em.getTransaction().commit();


			System.out.println("Datos guardados correctamente");

		} catch (Exception e) {

			// Si ocurre un error, deshago la transacción
			if (em.getTransaction().isActive()) {
				em.getTransaction().rollback();
			}

			e.printStackTrace();

		} finally { //ejecuta haya error o no, para cerrar el EntityManager y el EntityManagerFactory

			em.close();
			emf.close();
		}
	}
}