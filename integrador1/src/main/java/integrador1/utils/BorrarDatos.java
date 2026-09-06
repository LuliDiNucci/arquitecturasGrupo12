package integrador1.utils;

import java.sql.Connection;

import integrador1.DAO.ClienteDAO;
import integrador1.DAO.FacturaDAO;
import integrador1.DAO.FacturaProductoDAO;
import integrador1.DAO.ProductoDAO;
import integrador1.repositorio.MySqlClienteDAO;
import integrador1.repositorio.MySqlConnectionManager;
import integrador1.repositorio.MySqlFacturaDAO;
import integrador1.repositorio.MySQLFacturaProductoDAO;
import integrador1.repositorio.MySqlProductoDAO;

public class BorrarDatos {

    private final ClienteDAO clienteDAO;
    private final FacturaDAO facturaDAO;
    private final FacturaProductoDAO facturaProductoDAO;
    private final ProductoDAO productoDAO;

    public BorrarDatos() {
        Connection cn = MySqlConnectionManager.getInstance().getConnection();
        this.clienteDAO = new MySqlClienteDAO(cn);
        this.facturaDAO = new MySqlFacturaDAO(cn);
        this.facturaProductoDAO = new MySQLFacturaProductoDAO(cn);
        this.productoDAO = new MySqlProductoDAO(cn);
    }

    public void run() {
        try {
            facturaProductoDAO.deleteAll();
            facturaDAO.deleteAll();
            productoDAO.deleteAll();
            clienteDAO.deleteAll();
            System.out.println("Borrado completo de clientes, productos, facturas y detalles.");

        } catch (Exception e) {
            throw new RuntimeException("Error durante el borrado masivo.", e);
        }
    }
}
