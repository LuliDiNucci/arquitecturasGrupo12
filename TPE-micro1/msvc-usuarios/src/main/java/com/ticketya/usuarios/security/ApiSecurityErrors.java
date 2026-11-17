package com.ticketya.usuarios.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ticketya.usuarios.dto.ErrorResponse;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.List;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

@Component
public class ApiSecurityErrors implements AuthenticationEntryPoint, AccessDeniedHandler {
    private final ObjectMapper mapper;
    public ApiSecurityErrors(ObjectMapper mapper) { this.mapper = mapper; }
    @Override public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException error) throws IOException {
        response.setHeader("WWW-Authenticate", "Bearer");
        write(request, response, 401, "Autenticación requerida o JWT inválido");
    }
    @Override public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException error) throws IOException {
        write(request, response, 403, "Permisos insuficientes");
    }
    private void write(HttpServletRequest request, HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        mapper.writeValue(response.getOutputStream(), new ErrorResponse(status, message, request.getRequestURI(), List.of()));
    }
}
