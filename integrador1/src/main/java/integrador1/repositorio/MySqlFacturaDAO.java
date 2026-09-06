package integrador1.repositorio;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import integrador1.DAO.FacturaDAO;
import integrador1.entity.Factura;

public class MySqlFacturaDAO implements FacturaDAO {

    private final Connection cn;

    public MySqlFacturaDAO(Connection cn) {
        this.cn = cn;
    }

    @Override
    public Factura findById(Integer idFactura) {
        final String sql
                = "SELECT idFactura, idCliente FROM factura WHERE idFactura = ?";

        try (PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setInt(1, idFactura);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error en findById(factura)", e);
        }
    }

    @Override
    public List<Factura> findAll() {
        final String sql
                = "SELECT idFactura, idCliente FROM factura";

        List<Factura> out = new ArrayList<>();

        try (PreparedStatement ps = cn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                out.add(map(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error en findAll(factura)", e);
        }

        return out;
    }

    @Override
    public List<Factura> findByCliente(Integer idCliente) {
        final String sql
                = "SELECT idFactura, idCliente FROM factura WHERE idCliente = ?";

        List<Factura> out = new ArrayList<>();

        try (PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setInt(1, idCliente);

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    out.add(map(rs));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error en findByCliente(factura)", e);
        }

        return out;
    }

    @Override
    public void create(Factura f) {

        final String sql
                = "INSERT INTO factura (idFactura, idCliente) VALUES (?, ?)";

        try (PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setInt(1, f.getIdFactura());
            ps.setInt(2, f.getIdCliente());

            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Error en create(factura)", e);
        }
    }

    @Override
    public void update(Factura f) {
        final String sql
                = "UPDATE factura SET idCliente = ? WHERE idFactura = ?";

        try (PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setInt(1, f.getIdCliente());
            ps.setInt(2, f.getIdFactura());

            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Error en update(factura)", e);
        }
    }

    @Override
    public void delete(Integer idFactura) {
        final String sql
                = "DELETE FROM factura WHERE idFactura = ?";

        try (PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setInt(1, idFactura);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Error en delete(factura)", e);
        }
    }

    @Override
    public void deleteAll() {
        try (Statement st = cn.createStatement()) {

            st.executeUpdate("DELETE FROM factura");
            
        } catch (SQLException e) {
            throw new RuntimeException("Error borrando 'factura'", e);
        }
    }

    private Factura map(ResultSet rs) throws SQLException {
        Factura f = new Factura();

        f.setIdFactura(rs.getInt("idFactura"));
        f.setIdCliente(rs.getInt("idCliente"));

        return f;
    }
}
