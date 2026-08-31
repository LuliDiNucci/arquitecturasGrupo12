package integrador1.repositorio;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import integrador1.DAO.FacturaProductoDAO;
import integrador1.entity.Factura_Producto;

public class MySQLFacturaProductoDAO implements FacturaProductoDAO {

    private final Connection cn;

    public MySQLFacturaProductoDAO(Connection cn) {
        this.cn = cn;
        crearTablaSiNoExiste();
    }

    private void crearTablaSiNoExiste() {

        // Asegura que exista cliente.
        final String sqlCliente = "CREATE TABLE IF NOT EXISTS cliente (" +
                "idCliente INT PRIMARY KEY AUTO_INCREMENT," +
                "nombre VARCHAR(500)," +
                "email VARCHAR(150)" +
                ")";

        // Asegura que exista factura.
        final String sqlFactura = "CREATE TABLE IF NOT EXISTS factura (" +
                "idFactura INT PRIMARY KEY AUTO_INCREMENT," +
                "idCliente INT NOT NULL," +
                "CONSTRAINT fk_factura_cliente " +
                "FOREIGN KEY (idCliente) REFERENCES cliente(idCliente)" +
                ")";

        // Asegura que exista producto.
        final String sqlProducto = "CREATE TABLE IF NOT EXISTS producto (" +
                "idProducto INT PRIMARY KEY AUTO_INCREMENT," +
                "nombre VARCHAR(45)," +
                "valor FLOAT" +
                ")";

        final String sql = "CREATE TABLE IF NOT EXISTS factura_producto (" +
                "idFactura INT NOT NULL," +
                "idProducto INT NOT NULL," +
                "cantidad INT NOT NULL," +
                "PRIMARY KEY (idFactura, idProducto)," +
                "CONSTRAINT fk_factura_producto_factura " +
                "FOREIGN KEY (idFactura) REFERENCES factura(idFactura) " +
                "ON DELETE CASCADE," +
                "CONSTRAINT fk_factura_producto_producto " +
                "FOREIGN KEY (idProducto) REFERENCES producto(idProducto)" +
                ")";

        try (Statement st = cn.createStatement()) {

            st.execute(sqlCliente);
            st.execute(sqlFactura);
            st.execute(sqlProducto);
            st.execute(sql);

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Error creando tabla 'factura_producto'", e);
        }
    }

    @Override
    public Factura_Producto findById(Integer idFactura, Integer idProducto) {

        final String sql =
                "SELECT idFactura, idProducto, cantidad " +
                "FROM factura_producto " +
                "WHERE idFactura = ? AND idProducto = ?";

        try (PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setInt(1, idFactura);
            ps.setInt(2, idProducto);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Error en findById(factura_producto)", e);
        }
    }

    @Override
    public List<Factura_Producto> findAll() {

        final String sql =
                "SELECT idFactura, idProducto, cantidad " +
                "FROM factura_producto";

        List<Factura_Producto> out = new ArrayList<>();

        try (PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                out.add(map(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Error en findAll(factura_producto)", e);
        }

        return out;
    }

    @Override
    public List<Factura_Producto> findByFactura(Integer idFactura) {

        final String sql =
                "SELECT idFactura, idProducto, cantidad " +
                "FROM factura_producto WHERE idFactura = ?";

        List<Factura_Producto> out = new ArrayList<>();

        try (PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setInt(1, idFactura);

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    out.add(map(rs));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Error en findByFactura(factura_producto)", e);
        }

        return out;
    }

    @Override
    public List<Factura_Producto> findByProducto(Integer idProducto) {

        final String sql =
                "SELECT idFactura, idProducto, cantidad " +
                "FROM factura_producto WHERE idProducto = ?";

        List<Factura_Producto> out = new ArrayList<>();

        try (PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setInt(1, idProducto);

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    out.add(map(rs));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Error en findByProducto(factura_producto)", e);
        }

        return out;
    }

    @Override
    public void create(Factura_Producto fp) {

        final String sql =
                "INSERT INTO factura_producto " +
                "(idFactura, idProducto, cantidad) VALUES (?, ?, ?)";

        try (PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setInt(1, fp.getIdFactura());
            ps.setInt(2, fp.getIdProducto());
            ps.setInt(3, fp.getCantidad());

            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Error en create(factura_producto)", e);
        }
    }

    @Override
    public void update(Factura_Producto fp) {

        final String sql =
                "UPDATE factura_producto SET cantidad = ? " +
                "WHERE idFactura = ? AND idProducto = ?";

        try (PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setInt(1, fp.getCantidad());
            ps.setInt(2, fp.getIdFactura());
            ps.setInt(3, fp.getIdProducto());

            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Error en update(factura_producto)", e);
        }
    }

    @Override
    public void delete(Integer idFactura, Integer idProducto) {

        final String sql =
                "DELETE FROM factura_producto " +
                "WHERE idFactura = ? AND idProducto = ?";

        try (PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setInt(1, idFactura);
            ps.setInt(2, idProducto);

            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Error en delete(factura_producto)", e);
        }
    }

    @Override
    public void deleteAll() {

        try (Statement st = cn.createStatement()) {

            st.executeUpdate("DELETE FROM factura_producto");

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Error borrando 'factura_producto'", e);
        }
    }

    private Factura_Producto map(ResultSet rs) throws SQLException {

        Factura_Producto fp = new Factura_Producto();

        fp.setIdFactura(rs.getInt("idFactura"));
        fp.setIdProducto(rs.getInt("idProducto"));
        fp.setCantidad(rs.getInt("cantidad"));

        return fp;
    }
}


