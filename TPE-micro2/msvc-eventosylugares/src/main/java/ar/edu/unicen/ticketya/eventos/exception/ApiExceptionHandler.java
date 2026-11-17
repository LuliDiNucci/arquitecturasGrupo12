package ar.edu.unicen.ticketya.eventos.exception;

import ar.edu.unicen.ticketya.eventos.dto.ApiErrorResponseDTO;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.List;

@RestControllerAdvice
public class ApiExceptionHandler {

    private ResponseEntity<ApiErrorResponseDTO> respuesta(HttpStatus status, String mensaje,
                                                    HttpServletRequest request, List<String> detalles) {
        return ResponseEntity.status(status).body(new ApiErrorResponseDTO(LocalDateTime.now(), status.value(),
                status.getReasonPhrase(), mensaje, request.getRequestURI(), detalles));
    }
    @ExceptionHandler(ReglaNegocioException.class)
    public ResponseEntity<ApiErrorResponseDTO> negocio(ReglaNegocioException ex, HttpServletRequest request) {
        return respuesta(ex.getStatus(), ex.getMessage(), request, List.of());
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponseDTO> validacion(MethodArgumentNotValidException ex,
                                                    HttpServletRequest request) {
        var detalles = ex.getBindingResult().getFieldErrors().stream()
                .map(f -> f.getField() + ": " + f.getDefaultMessage()).toList();
        return respuesta(HttpStatus.BAD_REQUEST, "Datos de entrada invalidos", request, detalles);
    }
    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class,
                       IllegalArgumentException.class})
    public ResponseEntity<ApiErrorResponseDTO> formato(Exception ex, HttpServletRequest request) {
        return respuesta(HttpStatus.BAD_REQUEST, "Formato, fecha, numero o valor de enum invalido", request, List.of());
    }
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiErrorResponseDTO> integridad(DataIntegrityViolationException ex, HttpServletRequest request) {
        return respuesta(HttpStatus.CONFLICT, "Registro duplicado o violacion de integridad de datos", request, List.of());
    }
    @ExceptionHandler(org.springframework.orm.ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<ApiErrorResponseDTO> concurrente(Exception ex, HttpServletRequest request) {
        return respuesta(HttpStatus.CONFLICT, "El recurso fue modificado simultaneamente; reintentar", request, List.of());
    }
}
