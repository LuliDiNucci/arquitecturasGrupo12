package ar.edu.unicen.ticketya.eventos.exception;

import org.springframework.http.HttpStatus;

public class ReglaNegocioException extends RuntimeException {
    private final HttpStatus status;
    public ReglaNegocioException(HttpStatus status, String mensaje) {
        super(mensaje); this.status = status;
    }
    public HttpStatus getStatus() {
        return status;
    }
    public static ReglaNegocioException noEncontrado(String mensaje) {
        return new ReglaNegocioException(HttpStatus.NOT_FOUND, mensaje);
    }
    public static ReglaNegocioException conflicto(String mensaje) {
        return new ReglaNegocioException(HttpStatus.CONFLICT, mensaje);
    }
    public static ReglaNegocioException invalido(String mensaje) {
        return new ReglaNegocioException(HttpStatus.BAD_REQUEST, mensaje);
    }
}
