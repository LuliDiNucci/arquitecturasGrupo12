package integrador1;

import java.sql.Connection;

import integrador1.repositorio.MySqlConnectionManager;

public class TestConexion {

    public static void main(String[] args) {

        try {
            Connection conexion =
                    MySqlConnectionManager.getInstance().getConnection();

            System.out.println("¡Conexión exitosa!");
            System.out.println("Base de datos: " + conexion.getCatalog());

            MySqlConnectionManager.getInstance().shutdown();

        } catch (Exception e) {
            System.out.println("Error al conectar:");
            e.printStackTrace();
        }
    }
}