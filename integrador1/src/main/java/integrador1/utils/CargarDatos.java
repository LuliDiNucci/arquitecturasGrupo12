package integrador1.utils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import integrador1.DAO.ClienteDAO;
import integrador1.DAO.FacturaDAO;
import integrador1.DAO.FacturaProductoDAO;
import integrador1.DAO.ProductoDAO;
import integrador1.entity.Cliente;
import integrador1.entity.Factura;
import integrador1.entity.Factura_Producto;
import integrador1.entity.Producto;
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

        Connection connection =
                MySqlConnectionManager.getInstance().getConnection();

        this.clienteDAO = new MySqlClienteDAO(connection);
        this.productoDAO = new MySqlProductoDAO(connection);
        this.facturaDAO = new MySqlFacturaDAO(connection);
        this.facturaProductoDAO =
                new MySQLFacturaProductoDAO(connection);
    }

    public void run() {

        cargarClientes("/data/clientes.csv");

        cargarProductos("/data/productos.csv");

        cargarFacturas("/data/facturas.csv");

        cargarFacturaProductos("/data/facturas-productos.csv");

        System.out.println("Carga de datos finalizada.");
    }

    private void cargarClientes(String resourcePath) {

        try (
            InputStream is = mustGetResource(resourcePath);

            Reader reader = new BufferedReader(
                    new InputStreamReader(
                            is,
                            StandardCharsets.UTF_8));

            CSVParser parser = CSVFormat.DEFAULT
                    .builder()
                    .setHeader()
                    .setSkipHeaderRecord(true)
                    .build()
                    .parse(reader)
        ) {

            for (CSVRecord row : parser) {

                Integer idCliente =
                        Integer.parseInt(
                                row.get("idCliente").trim());

                String nombre =
                        row.get("nombre").trim();

                String email =
                        row.get("email").trim();

                Cliente cliente =
                        new Cliente(
                                idCliente,
                                nombre,
                                email);

                clienteDAO.create(cliente);
            }

            System.out.println("Clientes cargados OK.");

        } catch (Exception e) {

            throw new RuntimeException(
                    "Error cargando clientes desde "
                            + resourcePath,
                    e);
        }
    }

    private void cargarProductos(String resourcePath) {

        try (
            InputStream is = mustGetResource(resourcePath);

            Reader reader = new BufferedReader(
                    new InputStreamReader(
                            is,
                            StandardCharsets.UTF_8));

            CSVParser parser = CSVFormat.DEFAULT
                    .builder()
                    .setHeader()
                    .setSkipHeaderRecord(true)
                    .build()
                    .parse(reader)
        ) {

            for (CSVRecord row : parser) {

                Integer idProducto =
                        Integer.parseInt(
                                row.get("idProducto").trim());

                String nombre =
                        row.get("nombre").trim();

                Float valor =
                        Float.parseFloat(
                                row.get("valor").trim());

                Producto producto =
                        new Producto(
                                idProducto,
                                nombre,
                                valor);

                productoDAO.create(producto);
            }

            System.out.println("Productos cargados OK.");

        } catch (Exception e) {

            throw new RuntimeException(
                    "Error cargando productos desde "
                            + resourcePath,
                    e);
        }
    }

    private void cargarFacturas(String resourcePath) {

        try (
            InputStream is = mustGetResource(resourcePath);

            Reader reader = new BufferedReader(
                    new InputStreamReader(
                            is,
                            StandardCharsets.UTF_8));

            CSVParser parser = CSVFormat.DEFAULT
                    .builder()
                    .setHeader()
                    .setSkipHeaderRecord(true)
                    .build()
                    .parse(reader)
        ) {

            for (CSVRecord row : parser) {

                Integer idFactura =
                        Integer.parseInt(
                                row.get("idFactura").trim());

                Integer idCliente =
                        Integer.parseInt(
                                row.get("idCliente").trim());

                Factura factura =
                        new Factura(
                                idCliente,
                                idFactura);

                facturaDAO.create(factura);
            }

            System.out.println("Facturas cargadas OK.");

        } catch (Exception e) {

            throw new RuntimeException(
                    "Error cargando facturas desde "
                            + resourcePath,
                    e);
        }
    }

    private void cargarFacturaProductos(String resourcePath) {

        try (
            InputStream is = mustGetResource(resourcePath);

            Reader reader = new BufferedReader(
                    new InputStreamReader(
                            is,
                            StandardCharsets.UTF_8));

            CSVParser parser = CSVFormat.DEFAULT
                    .builder()
                    .setHeader()
                    .setSkipHeaderRecord(true)
                    .build()
                    .parse(reader)
        ) {

            for (CSVRecord row : parser) {

                Integer idFactura =
                        Integer.parseInt(
                                row.get("idFactura").trim());

                Integer idProducto =
                        Integer.parseInt(
                                row.get("idProducto").trim());

                Integer cantidad =
                        Integer.parseInt(
                                row.get("cantidad").trim());

                Factura_Producto detalle =
                        new Factura_Producto(
                                idFactura,
                                idProducto,
                                cantidad);

                facturaProductoDAO.create(detalle);
            }

            System.out.println(
                    "Facturas-productos cargados OK.");

        } catch (Exception e) {

            throw new RuntimeException(
                    "Error cargando facturas-productos desde "
                            + resourcePath,
                    e);
        }
    }

    private InputStream mustGetResource(String path) {

        InputStream is =
                getClass().getResourceAsStream(path);

        if (is == null) {
            throw new IllegalArgumentException(
                    "Recurso no encontrado: " + path);
        }

        return is;
    }
}