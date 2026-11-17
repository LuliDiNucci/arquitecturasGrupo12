package com.ticketya.usuarios.exceptions;

import com.ticketya.usuarios.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private ResponseEntity<ErrorResponse> error(HttpStatus status, String message,
            HttpServletRequest request, List<String> detalles) {
        ResponseEntity.BodyBuilder builder = ResponseEntity.status(status);
        if (status == HttpStatus.TOO_MANY_REQUESTS) builder.header("Retry-After", "900");
        return builder.body(new ErrorResponse(status.value(), message, request.getRequestURI(), detalles));
    }
    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ErrorResponse> negocio(ApiException ex, HttpServletRequest request) {
        return error(ex.getStatus(), ex.getMessage(), request, List.of());
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> validacion(MethodArgumentNotValidException ex, HttpServletRequest request) {
        return error(HttpStatus.BAD_REQUEST, "Datos inválidos", request,
            ex.getBindingResult().getFieldErrors().stream()
              .map(e -> e.getField() + ": " + e.getDefaultMessage()).toList());
    }
    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
    public ResponseEntity<ErrorResponse> formato(Exception ex, HttpServletRequest request) {
        return error(HttpStatus.BAD_REQUEST, "Formato de solicitud inválido", request, List.of());
    }
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> duplicado(Exception ex, HttpServletRequest request) {
        return error(HttpStatus.CONFLICT, "El email ya está registrado o existe un conflicto de datos", request, List.of());
    }
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> prohibido(Exception ex, HttpServletRequest request) {
        return error(HttpStatus.FORBIDDEN, "Permisos insuficientes", request, List.of());
    }
}
