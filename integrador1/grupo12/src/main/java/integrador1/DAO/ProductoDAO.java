package integrador1.DAO;

import java.util.List;

import integrador1.entity.Producto;

public interface ProductoDAO {

    Producto findById(Integer idProducto);

    List<Producto> findAll();

    void create(Producto p);

    void update(Producto p);

    void delete(Integer idProducto);

    void deleteAll();
}

