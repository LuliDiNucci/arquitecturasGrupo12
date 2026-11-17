package com.ticketya.entradas.security;

import java.io.IOException;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.*;
import org.springframework.stereotype.Component;

@Component
public class InternalRestSecurity implements ClientHttpRequestInterceptor {
    private final InternalCredentials credentials;
    public InternalRestSecurity(InternalCredentials credentials) { this.credentials = credentials; }
    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        request.getHeaders().set("Authorization", credentials.authorization(request.getMethod().name()));
        return execution.execute(request, body);
    }
}
