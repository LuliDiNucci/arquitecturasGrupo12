package com.ticketya.observability;

import java.io.IOException;
import org.slf4j.MDC;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.*;

/** RestClient y RestTemplate, incluidos clientes de credenciales creados manualmente. */
public final class CorrelationHttpInterceptor implements ClientHttpRequestInterceptor {
    private final String service;
    public CorrelationHttpInterceptor(String service) { this.service = service; }
    @Override public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
            throws IOException {
        String id = Correlation.currentOrNew();
        request.getHeaders().set(Correlation.HEADER, id);
        long started = System.nanoTime();
        int status = 502;
        String error = null;
        try {
            ClientHttpResponse response = execution.execute(request, body);
            status = response.getStatusCode().value();
            return response;
        } catch (IOException | RuntimeException ex) { error = ex.getClass().getSimpleName(); throw ex; }
        finally { HttpEvents.write("http_client", service, id, ObjectsHelper.user(), request.getMethod().name(),
            HttpEvents.path(request.getURI()), status, started, error); }
    }
    private static final class ObjectsHelper {
        static String user() { String user = MDC.get("usuario"); return user == null ? "anonimo" : user; }
    }
}
