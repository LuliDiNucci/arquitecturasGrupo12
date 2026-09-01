package integrador1.utils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;

import integrador1.DAO.ClienteDAO;
import integrador1.DAO.FacturaDAO;
import integrador1.DAO.FacturaProductoDAO;
import integrador1.DAO.ProductoDAO;
import integrador1.entity.Cliente;
import integrador1.entity.Factura;
import integrador1.entity.Factura_Producto;
import integrador1.entity.Producto;
import integrador1.factory.ConnectionManager;
import integrador1.repositorio.MySQLFacturaProductoDAO;
import integrador1.repositorio.MySqlClienteDAO;
import integrador1.repositorio.MySqlConnectionManager;
import integrador1.repositorio.MySqlFacturaDAO;
import integrador1.repositorio.MySqlProductoDAO;

public class CargarDatos {

    private final ClienteDAO clienteDAO;
    private final ProductoDAO productoDAO;
    private final FacturaDAO facturaDAO;
    private final FacturaProductoDAO facturaProductoDAO;

    public CargarDatos() {

        ConnectionManager cm = MySqlConnectionManager.getInstance();
        Connection connection = cm.getConnection();

        this.clienteDAO = new MySqlClienteDAO(connection);
        this.productoDAO = new MySqlProductoDAO(connection);
        this.facturaDAO = new MySqlFacturaDAO(connection);
        this.facturaProductoDAO = new MySQLFacturaProductoDAO(connection);
    }

    public void run() {

        cargarClientes("/data/clientes.csv");
        cargarProductos("/data/productos.csv");
        cargarFacturas("/data/facturas.csv");
        cargarFacturaProductos("/data/factura_productos.csv");
    }


    private void cargarClientes(String resourcePath) {

        try (InputStream is = mustGetResource(resourcePath);
             BufferedReader br = new BufferedReader(new InputStreamReader(is))) {

            String line;
            boolean first = true;

            while ((line = br.readLine()) != null) {

                if (first) {
                    first = false;
                    continue;
                }

                if (line.isBlank())
                    continue;

                String[] p = line.split(",", -1);

                String nombre = p[0].trim();
                String email = p[1].trim();

                Cliente cliente =
                        new Cliente(null, nombre, email);

                clienteDAO.create(cliente);
            }

            System.out.println("Clientes cargados OK.");

        } catch (Exception e) {
            throw new RuntimeException(
                    "Error cargando clientes desde " + resourcePath, e);
        }
    }


    private void cargarProductos(String resourcePath) {

        try (InputStream is = mustGetResource(resourcePath);
             BufferedReader br = new BufferedReader(new InputStreamReader(is))) {

            String line;
            boolean first = true;

            while ((line = br.readLine()) != null) {

                if (first) {
                    first = false;
                    continue;
                }

                if (line.isBlank())
                    continue;

                String[] p = line.split(",", -1);

                String nombre = p[0].trim();
                Float valor = Float.parseFloat(p[1].trim());

                Producto producto =
                        new Producto(null, nombre, valor);

                productoDAO.create(producto);
            }

            System.out.println("Productos cargados OK.");

        } catch (Exception e) {
            throw new RuntimeException(
                    "Error cargando productos desde " + resourcePath, e);
        }
    }


    private void cargarFacturas(String resourcePath) {

        try (InputStream is = mustGetResource(resourcePath);
             BufferedReader br = new BufferedReader(new InputStreamReader(is))) {

            String line;
            boolean first = true;

            while ((line = br.readLine()) != null) {

                if (first) {
                    first = false;
                    continue;
                }

                if (line.isBlank())
                    continue;

                String[] p = line.split(",", -1);

                Integer clienteId = Integer.parseInt(p[0].trim());
                Factura factura;
                factura = new Factura(null, clienteId);

                facturaDAO.create(factura);
            }

            System.out.println("Facturas cargadas OK.");

        } catch (Exception e) {
            throw new RuntimeException(
                    "Error cargando facturas desde " + resourcePath, e);
        }
    }


    private void cargarFacturaProductos(String resourcePath) {

        try (InputStream is = mustGetResource(resourcePath);
             BufferedReader br = new BufferedReader(new InputStreamReader(is))) {

            String line;
            boolean first = true;

            while ((line = br.readLine()) != null) {

                if (first) {
                    first = false;
                    continue;
                }

                if (line.isBlank())
                    continue;

                String[] p = line.split(",", -1);

                Integer facturaId = Integer.parseInt(p[0].trim());
                Integer productoId = Integer.parseInt(p[1].trim());
                Integer cantidad = Integer.parseInt(p[2].trim());

                Factura_Producto facturaProducto =
                        new Factura_Producto(
                                facturaId,
                                productoId,
                                cantidad
                        );

                facturaProductoDAO.create(facturaProducto);
            }

            System.out.println("Productos de facturas cargados OK.");

        } catch (Exception e) {
            throw new RuntimeException(
                    "Error cargando factura_productos desde "
                            + resourcePath, e);
        }
    }


    private InputStream mustGetResource(String path) {

        InputStream is = getClass().getResourceAsStream(path);

        if (is == null)
            throw new IllegalArgumentException(
                    "Recurso no encontrado: " + path);

        return is;
    }
}