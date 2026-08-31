package integrador1.DAO;

import java.util.List;

import integrador1.entity.Cliente;

public interface ClienteDAO {

    Cliente findById(Integer idCliente);

    List<Cliente> findAll();

    void create(Cliente c);

    void update(Cliente c);

    void delete(Integer idCliente);

    void deleteAll();
}

