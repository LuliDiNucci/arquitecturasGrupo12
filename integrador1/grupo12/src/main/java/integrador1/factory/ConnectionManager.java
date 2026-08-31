package integrador1.factory;

import java.sql.Connection;

/** Contrato del gestor de conexiones JDBC usado por la aplicación. */
public interface ConnectionManager {

    /** Devuelve una conexión abierta a MySQL. */
    Connection getConnection();

    /** Cierra la conexión administrada, si está abierta. */
    void shutdown();
}
