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
        crearTablaSiNoExiste();
    }

    private void crearTablaSiNoExiste() {
        final String sql = "CREATE TABLE IF NOT EXISTS cliente (" +
                "idCliente INT PRIMARY KEY AUTO_INCREMENT," +
                "nombre VARCHAR(500)," +
                "email VARCHAR(150)" +
                ")";

        try (Statement st = cn.createStatement()) {
            st.execute(sql);
        } catch (SQLException e) {
            throw new RuntimeException("Error creando tabla 'cliente'", e);
        }
    }

    @Override
    public Cliente findById(Integer idCliente) {
        final String sql =
                "SELECT idCliente, nombre, email FROM cliente WHERE idCliente = ?";

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
        final String sql =
                "SELECT idCliente, nombre, email FROM cliente";

        List<Cliente> out = new ArrayList<>();

        try (PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

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
        final String sql =
                "INSERT INTO cliente (nombre, email) VALUES (?, ?)";

        try (PreparedStatement ps =
                     cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, c.getNombre());
            ps.setString(2, c.getEmail());

            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    c.setIdCliente(keys.getInt(1));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error en create(cliente)", e);
        }
    }

    @Override
    public void update(Cliente c) {
        final String sql =
                "UPDATE cliente SET nombre = ?, email = ? WHERE idCliente = ?";

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
        final String sql =
                "DELETE FROM cliente WHERE idCliente = ?";

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
            st.execute("ALTER TABLE cliente AUTO_INCREMENT = 1");

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
}

