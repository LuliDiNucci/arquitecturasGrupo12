package integrador1.repositorio;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import integrador1.DAO.ClienteDAO;
import integrador1.entity.Cliente;

public class MySqlClienteDAO implements ClienteDAO {

    private final Connection cn;

    public MySqlClienteDAO(Connection cn) {
        this.cn = cn;
    }

    @Override
    public Cliente findById(Integer idCliente) {
        final String sql
                = "SELECT idCliente, nombre, email FROM cliente WHERE idCliente = ?";

        try (PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setInt(1, idCliente);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error en findById(cliente)", e);
        }
    }

    @Override
    public List<Cliente> findAll() {
        final String sql
                = "SELECT idCliente, nombre, email FROM cliente";

        List<Cliente> out = new ArrayList<>();

        try (PreparedStatement ps = cn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                out.add(map(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error en findAll(cliente)", e);
        }

        return out;
    }

    @Override
    public void create(Cliente c) {

        final String sql
                = "INSERT INTO cliente (idCliente, nombre, email) VALUES (?, ?, ?)";

        try (PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setInt(1, c.getIdCliente());
            ps.setString(2, c.getNombre());
            ps.setString(3, c.getEmail());

            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Error en create(cliente)", e);
        }
    }

    @Override
    public void update(Cliente c) {
        final String sql
                = "UPDATE cliente SET nombre = ?, email = ? WHERE idCliente = ?";

        try (PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setString(1, c.getNombre());
            ps.setString(2, c.getEmail());
            ps.setInt(3, c.getIdCliente());

            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Error en update(cliente)", e);
        }
    }

    @Override
    public void delete(Integer idCliente) {
        final String sql
                = "DELETE FROM cliente WHERE idCliente = ?";

        try (PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setInt(1, idCliente);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Error en delete(cliente)", e);
        }
    }

    @Override
    public void deleteAll() {
        try (Statement st = cn.createStatement()) {

            st.executeUpdate("DELETE FROM cliente");

        } catch (SQLException e) {
            throw new RuntimeException("Error borrando 'cliente'", e);
        }
    }

    private Cliente map(ResultSet rs) throws SQLException {
        Cliente c = new Cliente();

        c.setIdCliente(rs.getInt("idCliente"));
        c.setNombre(rs.getString("nombre"));
        c.setEmail(rs.getString("email"));

        return c;
    }

    @Override
    public void imprimirListaFacturacion() {

        final String sql
                = "SELECT c.idCliente, c.nombre, c.email, "
                + "COALESCE(SUM(fp.cantidad * p.valor), 0) AS total_facturado "
                + "FROM Cliente c "
                + "LEFT JOIN Factura f ON c.idCliente = f.idCliente "
                + "LEFT JOIN Factura_Producto fp ON f.idFactura = fp.idFactura "
                + "LEFT JOIN Producto p ON fp.idProducto = p.idProducto "
                + "GROUP BY c.idCliente, c.nombre, c.email "
                + "ORDER BY total_facturado DESC";

        try (PreparedStatement ps = cn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {

                System.out.printf(
                        "%d - %s - %s - Total facturado: $%.2f%n",
                        rs.getInt("idCliente"),
                        rs.getString("nombre"),
                        rs.getString("email"),
                        rs.getDouble("total_facturado")
                );
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Error en mostrar la lista de facturacion", e);
        }
    }
}
