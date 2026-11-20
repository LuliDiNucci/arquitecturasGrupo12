package com.ticketya.observability;

import java.net.URI;
import java.io.IOException;
import feign.Capability;
import feign.Client;
import org.slf4j.MDC;

/** También registra fallos de conexión, donde el destino no recibió una solicitud. */
public final class CorrelationFeignCapability implements Capability {
    private final String service;
    public CorrelationFeignCapability(String service) { this.service = service; }
    @Override public Client enrich(Client delegate) {
        return (request, options) -> {
            String id = request.headers().getOrDefault(Correlation.HEADER, java.util.List.of(Correlation.currentOrNew()))
                .stream().findFirst().orElseGet(Correlation::currentOrNew);
            long started = System.nanoTime();
            int status = 502;
            String error = null;
            try { var response = delegate.execute(request, options); status = response.status(); return response; }
            catch (IOException | RuntimeException ex) { error = ex.getClass().getSimpleName(); throw ex; }
            finally { HttpEvents.write("http_client", service, id, MDC.get("usuario") == null ? "anonimo" : MDC.get("usuario"),
                request.httpMethod().name(), HttpEvents.path(URI.create(request.url())), status, started, error); }
        };
    }
}
