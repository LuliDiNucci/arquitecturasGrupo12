package integrador1.repositorio;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import integrador1.factory.ConnectionManager;

/**
 * Gestor de conexiones de MySQL.
 */
public final class MySqlConnectionManager implements ConnectionManager {

    /**
     * Singleton Thread-Safe.
     */
    private static volatile MySqlConnectionManager instance;

    private Connection connection;

    // --- Configuración de conexión ---
    private static final String URL = "jdbc:mysql://localhost:3306/arquidb?useSSL=false&serverTimezone=UTC&createDatabaseIfNotExist=true";
    private static final String USER = "root";
    private static final String PASSWORD = "arquitecturas_2026";

    // --- Constructor privado ---
    private MySqlConnectionManager() {
        try {
            // Registrar el driver de MySQL
            Class.forName("com.mysql.cj.jdbc.Driver");

            // Establecer la conexión
            this.connection = DriverManager.getConnection(URL, USER, PASSWORD);

            System.out.println("Conexión establecida correctamente con MySQL.");

        } catch (ClassNotFoundException e) {
            System.err.println("Error: No se encontró el driver de MySQL.");
            e.printStackTrace();

        } catch (SQLException e) {
            System.err.println("Error al conectar con la base de datos.");
            e.printStackTrace();
        }
    }

    // --- Singleton Thread-Safe ---
    public static MySqlConnectionManager getInstance() {

        if (instance == null) {
            synchronized (MySqlConnectionManager.class) {

                if (instance == null) {
                    instance = new MySqlConnectionManager();
                }
            }
        }

        return instance;
    }

    // --- Retornar la conexión ---
    @Override
    public Connection getConnection() {
        return connection;
    }

    /**
     * Cierra la conexión con MySQL.
     */
    @Override
    public void shutdown() {
        try {

            if (connection != null && !connection.isClosed()) {
                connection.close();

                System.out.println("Conexión con MySQL cerrada.");
            }

        } catch (SQLException e) {

            System.err.println(
                    "Error al cerrar la conexión con MySQL: "
                            + e.getMessage()
            );

        } finally {

            connection = null;

            synchronized (MySqlConnectionManager.class) {
                instance = null;
            }
        }
    }
}

