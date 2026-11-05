
package com.ticketya.entradas.clients;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

import com.ticketya.entradas.dtos.ReservaDTO;

@Component
public class ReservaClient {

    private final RestClient restClient;

    public ReservaClient(
            RestClient.Builder builder,
            @Value("${msvc.reservas.url}") String baseUrl) {

        this.restClient = builder.baseUrl(baseUrl).build();
    }

    // Consulta una reserva utilizando su ID.
    public ReservaDTO obtener(Long reservaId) {
        try {
            ReservaDTO reserva = restClient.get()
                    .uri("/reservas/{id}", reservaId)
                    .retrieve()
                    .body(ReservaDTO.class);

            if (reserva == null) {
                throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Reservas devolvio una respuesta vacia"
                );
            }

            return reserva;

        } catch (RestClientException ex) {
            throw new ResponseStatusException(
                HttpStatus.BAD_GATEWAY,
                "No se pudo verificar la reserva confirmada con msvc-reservas",
                ex
            );
        }
    }
}

