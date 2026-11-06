
package com.ticketya.compras.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ticketya.compras.entities.Pago;

public interface PagoRepository extends JpaRepository<Pago, Long> {

    // Buscar una operación repetida por clave de idempotencia
    Optional<Pago> findByClaveIdempotencia(
            String claveIdempotencia);

    // Obtener el historial de intentos de pago
    List<Pago> findByCompra_IdOrderByFechaSolicitudDesc(
            Long compraId);

    // Buscar el último pago con un estado determinado
    Optional<Pago> findFirstByCompra_IdAndEstadoOrderByFechaSolicitudDesc(
            Long compraId,
            String estado);
}
