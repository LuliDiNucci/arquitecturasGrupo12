package integrador1.utils;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import integrador1.repositorio.MySqlConnectionManager;

public class CrearEsquema {

    public void run() {

        Connection connection =
                MySqlConnectionManager.getInstance().getConnection();

        try (Statement st = connection.createStatement()) {

            st.executeUpdate("""
                CREATE TABLE IF NOT EXISTS cliente (
                    idCliente INT PRIMARY KEY,
                    nombre VARCHAR(500),
                    email VARCHAR(150)
                )
            """);

            st.executeUpdate("""
                CREATE TABLE IF NOT EXISTS producto (
                    idProducto INT PRIMARY KEY,
                    nombre VARCHAR(45),
                    valor FLOAT
                )
            """);

            st.executeUpdate("""
                CREATE TABLE IF NOT EXISTS factura (
                    idFactura INT PRIMARY KEY,
                    idCliente INT NOT NULL,
                    CONSTRAINT fk_factura_cliente
                        FOREIGN KEY (idCliente)
                        REFERENCES cliente(idCliente)
                )
            """);

            st.executeUpdate("""
                CREATE TABLE IF NOT EXISTS factura_producto (
                    idFactura INT NOT NULL,
                    idProducto INT NOT NULL,
                    cantidad INT NOT NULL,

                    PRIMARY KEY (idFactura, idProducto),

                    CONSTRAINT fk_factura_producto_factura
                        FOREIGN KEY (idFactura)
                        REFERENCES factura(idFactura)
                        ON DELETE CASCADE,

                    CONSTRAINT fk_factura_producto_producto
                        FOREIGN KEY (idProducto)
                        REFERENCES producto(idProducto)
                )
            """);

            System.out.println("Esquema creado correctamente.");

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Error creando el esquema.", e);
        }
    }
}