package integrador1.DAO;

import java.util.List;

import integrador1.entity.Factura_Producto;

public interface FacturaProductoDAO {

    Factura_Producto findById(Integer idFactura, Integer idProducto);

    List<Factura_Producto> findAll();

    List<Factura_Producto> findByFactura(Integer idFactura);

    List<Factura_Producto> findByProducto(Integer idProducto);

    void create(Factura_Producto fp);

    void update(Factura_Producto fp);

    void delete(Integer idFactura, Integer idProducto);

    void deleteAll();
}

