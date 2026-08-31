package integrador1.repositorio;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import integrador1.DAO.ProductoDAO;
import integrador1.entity.Producto;

public class MySqlProductoDAO implements ProductoDAO {

    private final Connection cn;

    public MySqlProductoDAO(Connection cn) {
        this.cn = cn;
        crearTablaSiNoExiste();
    }

    private void crearTablaSiNoExiste() {
        final String sql = "CREATE TABLE IF NOT EXISTS producto (" +
                "idProducto INT PRIMARY KEY AUTO_INCREMENT," +
                "nombre VARCHAR(45)," +
                "valor FLOAT" +
                ")";

        try (Statement st = cn.createStatement()) {
            st.execute(sql);
        } catch (SQLException e) {
            throw new RuntimeException("Error creando tabla 'producto'", e);
        }
    }

    @Override
    public Producto findById(Integer idProducto) {
        final String sql =
                "SELECT idProducto, nombre, valor FROM producto WHERE idProducto = ?";

        try (PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setInt(1, idProducto);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error en findById(producto)", e);
        }
    }

    @Override
    public List<Producto> findAll() {
        final String sql =
                "SELECT idProducto, nombre, valor FROM producto";

        List<Producto> out = new ArrayList<>();

        try (PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                out.add(map(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error en findAll(producto)", e);
        }

        return out;
    }

    @Override
    public void create(Producto p) {
        final String sql =
                "INSERT INTO producto (nombre, valor) VALUES (?, ?)";

        try (PreparedStatement ps =
                     cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, p.getNombre());

            if (p.getValor() == null) {
                ps.setNull(2, Types.FLOAT);
            } else {
                ps.setFloat(2, p.getValor());
            }

            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    p.setIdProducto(keys.getInt(1));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error en create(producto)", e);
        }
    }

    @Override
    public void update(Producto p) {
        final String sql =
                "UPDATE producto SET nombre = ?, valor = ? WHERE idProducto = ?";

        try (PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setString(1, p.getNombre());

            if (p.getValor() == null) {
                ps.setNull(2, Types.FLOAT);
            } else {
                ps.setFloat(2, p.getValor());
            }

            ps.setInt(3, p.getIdProducto());

            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Error en update(producto)", e);
        }
    }

    @Override
    public void delete(Integer idProducto) {
        final String sql =
                "DELETE FROM producto WHERE idProducto = ?";

        try (PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setInt(1, idProducto);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Error en delete(producto)", e);
        }
    }

    @Override
    public void deleteAll() {
        try (Statement st = cn.createStatement()) {

            st.executeUpdate("DELETE FROM producto");
            st.execute("ALTER TABLE producto AUTO_INCREMENT = 1");

        } catch (SQLException e) {
            throw new RuntimeException("Error borrando 'producto'", e);
        }
    }

    private Producto map(ResultSet rs) throws SQLException {
        Producto p = new Producto();

        p.setIdProducto(rs.getInt("idProducto"));
        p.setNombre(rs.getString("nombre"));

        float valor = rs.getFloat("valor");
        p.setValor(rs.wasNull() ? null : valor);

        return p;
    }
}

