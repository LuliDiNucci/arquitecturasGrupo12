package com.ticketya.recomendaciones.config;

import org.springframework.context.annotation.Bean;

import com.ticketya.recomendaciones.exceptions.CatalogoNoDisponibleException;

import feign.Request;
import feign.RetryableException;
import feign.Retryer;
import feign.codec.ErrorDecoder;

/** Reintenta sólo GET del catálogo ante fallos transitorios. */
public class EventosFeignConfig {

    @Bean
    public Retryer catalogoRetryer() {
        return new ReintentoCatalogo();
    }

    @Bean
    public ErrorDecoder catalogoErrorDecoder() {
        ErrorDecoder delegado = new ErrorDecoder.Default();
        return (methodKey, response) -> {
            Request request = response.request();
            int estado = response.status();
            if (request.httpMethod() == Request.HttpMethod.GET && esReintentable(estado)) {
                return new RetryableException(response.status(), "Catálogo temporalmente no disponible",
                        request.httpMethod(), (Long) null, request);
            }
            Exception error = delegado.decode(methodKey, response);
            return estado >= 500 && estado < 600 ? new CatalogoNoDisponibleException(error) : error;
        };
    }
    private static boolean esReintentable(int estado) {
        return estado == -1 || estado == 502 || estado == 503 || estado == 504;
    }

    private static final class ReintentoCatalogo implements Retryer {
        private final Retryer delegado = new Retryer.Default(100, 100, 2);
        @Override public void continueOrPropagate(RetryableException error) {
            if (error.request().httpMethod() != Request.HttpMethod.GET || !esReintentable(error.status())) throw error;
            delegado.continueOrPropagate(error);
        }
        @Override public Retryer clone() { return new ReintentoCatalogo(); }
    }
}
