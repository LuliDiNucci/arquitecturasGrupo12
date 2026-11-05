
package com.ticketya.entradas.clients;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

import com.ticketya.entradas.dtos.CompraDTO;

@Component
public class CompraClient {

    private final RestClient restClient;

    public CompraClient(
            RestClient.Builder builder,
            @Value("${msvc.compras.url}") String baseUrl) {

        this.restClient = builder.baseUrl(baseUrl).build();
    }

    // Consulta el estado real de la compra y su pago.
    public CompraDTO obtenerConfirmacion(Long compraId) {
        try {
            CompraDTO compra = restClient.get()
                    .uri("/compras/{id}/confirmacion", compraId)
                    .retrieve()
                    .body(CompraDTO.class);

            if (compra == null) {
                throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Compras devolvio una respuesta vacia"
                );
            }

            return compra;

        } catch (RestClientException ex) {
            throw new ResponseStatusException(
                HttpStatus.BAD_GATEWAY,
                "No se pudo verificar la compra aprobada con msvc-compras",
                ex
            );
        }
    }
}
