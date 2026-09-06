package integrador1.DAO;

import java.util.List;

import integrador1.entity.Factura;

public interface FacturaDAO {

    Factura findById(Integer idFactura);

    List<Factura> findAll();

    List<Factura> findByCliente(Integer idCliente);

    void create(Factura f);

    void update(Factura f);

    void delete(Integer idFactura);

    void deleteAll();
}

