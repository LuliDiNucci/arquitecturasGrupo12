package com.ticketya.recomendaciones.config;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.junit.jupiter.api.Test;

import feign.Request;
import feign.RetryableException;
import feign.Response;

class EventosFeignConfigTest {

    private final EventosFeignConfig config = new EventosFeignConfig();

    @Test
    void reintentaUn503DeGetDelCatalogo() {
        Exception resultado = config.catalogoErrorDecoder().decode("EventoClient#listar", respuesta(Request.HttpMethod.GET));
        assertInstanceOf(RetryableException.class, resultado);
    }

    @Test
    void noConvierteUnPostEnReintentable() {
        Exception resultado = config.catalogoErrorDecoder().decode("CompraClient#crear", respuesta(Request.HttpMethod.POST));
        org.junit.jupiter.api.Assertions.assertFalse(resultado instanceof RetryableException);
    }

    private Response respuesta(Request.HttpMethod method) {
        Request request = Request.create(method, "http://eventos/eventos/publicados", Map.of(), null,
                StandardCharsets.UTF_8, null);
        return Response.builder().status(503).reason("Service Unavailable").request(request).headers(Map.of()).build();
    }
}
