package integrador1;

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

public class Main {

    public static void main(String[] args) {
        // Único motor implementado actualmente por el proyecto.
        ConnectionManager connectionManager = MySqlConnectionManager.getInstance();
        Connection connection = connectionManager.getConnection();

        if (connection == null) {
            System.err.println("No se pudo establecer la conexión con MySQL.");
            return;
        }

        try {
            ClienteDAO clienteDAO = new MySqlClienteDAO(connection);
            ProductoDAO productoDAO = new MySqlProductoDAO(connection);
            FacturaDAO facturaDAO = new MySqlFacturaDAO(connection);
            FacturaProductoDAO facturaProductoDAO =
                    new MySQLFacturaProductoDAO(connection);

            // Borrar datos: primero las tablas que dependen de otras por claves foráneas.
            facturaProductoDAO.deleteAll();
            facturaDAO.deleteAll();
            productoDAO.deleteAll();
            clienteDAO.deleteAll();
            System.out.println("Datos eliminados.");

            // Carga inicial.
            Cliente cliente = new Cliente(null, "Ana Pérez", "ana.perez@email.com");
            clienteDAO.create(cliente);

            Producto producto = new Producto(null, "Producto de prueba", 1500.0f);
            productoDAO.create(producto);

            Factura factura = new Factura(cliente.getIdCliente(), null);
            facturaDAO.create(factura);

            Factura_Producto detalle = new Factura_Producto(
                    factura.getIdFactura(), producto.getIdProducto(), 2);
            facturaProductoDAO.create(detalle);
            System.out.println("Carga inicial finalizada.");

            // Consulta y actualización de un cliente.
            Cliente encontrado = clienteDAO.findById(cliente.getIdCliente());
            System.out.println("findById: " + encontrado);

            encontrado.setEmail("ana.perez.actualizado@email.com");
            clienteDAO.update(encontrado);
            System.out.println("Actualizado: "
                    + clienteDAO.findById(encontrado.getIdCliente()));

            System.out.println("Todos los clientes: " + clienteDAO.findAll());
            System.out.println("Todos los productos: " + productoDAO.findAll());
            System.out.println("Todas las facturas: " + facturaDAO.findAll());
            System.out.println("Todos los detalles de factura: "
                    + facturaProductoDAO.findAll());

            // clienteDAO.delete(encontrado.getIdCliente());
        } finally {
            connectionManager.shutdown();
        }
    }
}
