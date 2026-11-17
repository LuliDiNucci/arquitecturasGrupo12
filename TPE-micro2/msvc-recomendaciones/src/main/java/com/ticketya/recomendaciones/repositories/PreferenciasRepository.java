
package com.ticketya.recomendaciones.repositories;

import com.ticketya.recomendaciones.models.Preferencias;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface PreferenciasRepository extends MongoRepository<Preferencias, String> {

    Optional<Preferencias> findByClienteId(Long clienteId);

    boolean existsByClienteId(Long clienteId);

}

