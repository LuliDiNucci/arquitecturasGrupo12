package integrador1;

import java.sql.Connection;

import integrador1.DAO.ClienteDAO;
import integrador1.DAO.ProductoDAO;
import integrador1.entity.Producto;
import integrador1.repositorio.MySqlClienteDAO;
import integrador1.repositorio.MySqlConnectionManager;
import integrador1.repositorio.MySqlProductoDAO;
import integrador1.utils.BorrarDatos;
import integrador1.utils.CargarDatos;
import integrador1.utils.CrearEsquema;

public class Main {

    public static void main(String[] args) {

        Connection connection =
                MySqlConnectionManager
                        .getInstance()
                        .getConnection();

        if (connection == null) {

            System.err.println(
                    "No se pudo establecer la conexión con MySQL.");

            return;
        }

        try {

            // =========================================
            // 1. CREAR ESQUEMA
            // =========================================

            CrearEsquema crearEsquema =
                    new CrearEsquema();

            crearEsquema.run();


            // =========================================
            // LIMPIAR DATOS ANTERIORES
            // =========================================

            BorrarDatos borrarDatos =
                    new BorrarDatos();

            borrarDatos.run();


            // =========================================
            // 2. CARGAR CSV
            // =========================================

            CargarDatos cargarDatos =
                    new CargarDatos();

            cargarDatos.run();


            // =========================================
            // 3. PRODUCTO QUE MÁS RECAUDÓ
            // =========================================

            ProductoDAO productoDAO =
                    new MySqlProductoDAO(connection);

            Producto productoMayor =
                    productoDAO.productoMayorRecaudacion();

            System.out.println();
            System.out.println(
                    "======================================");

            System.out.println(
                    "PRODUCTO QUE MÁS RECAUDÓ");

            System.out.println(
                    "======================================");

            if (productoMayor != null) {

                System.out.println(productoMayor);

            } else {

                System.out.println(
                        "No se encontraron productos vendidos.");
            }


            // =========================================
            // 4. CLIENTES ORDENADOS POR FACTURACIÓN
            // =========================================

            ClienteDAO clienteDAO =
                    new MySqlClienteDAO(connection);

            System.out.println();
            System.out.println(
                    "======================================");

            System.out.println(
                    "CLIENTES ORDENADOS POR FACTURACIÓN");

            System.out.println(
                    "======================================");

            clienteDAO.imprimirListaFacturacion();

        } catch (Exception e) {

            System.err.println(
                    "Error durante la ejecución:");

            e.printStackTrace();

        } finally {

            MySqlConnectionManager
                    .getInstance()
                    .shutdown();
        }
    }
}