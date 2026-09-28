package edu.isistan.dao;

import java.util.List;

public interface DAOEstudiante {
    //hacer os metodos abstractos de as consignas para q lo implementen los dao de mysql
    List<Estudiante> recuperarTodos();
    
    List<Estudiante> recuperarPorCarreraYCiudad(String nombreCarrera, String ciudad);

    List<Estudiante> recuperarPorGenero(String genero);

    Estudiante recuperarLU(int LU);
}
