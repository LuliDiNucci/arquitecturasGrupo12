
package com.ticketya.reservas.exceptions;

import com.ticketya.reservas.dtos.ErrorDTO;
import feign.FeignException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.dao.DataIntegrityViolationException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class ReservaExceptionHandler {

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorDTO> jsonIncorrecto(HttpMessageNotReadableException ex) {
        return ResponseEntity.badRequest().body(new ErrorDTO(400, "JSON inválido"));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorDTO> parametroIncorrecto(MethodArgumentTypeMismatchException ex) {
        return ResponseEntity.badRequest().body(new ErrorDTO(400, "Parámetro inválido: " + ex.getName()));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorDTO> conflictoBaseDatos(DataIntegrityViolationException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorDTO(409, "Conflicto de unicidad o integridad de datos"));
    }

    // DATOS INVALIDOS
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorDTO> manejarArgumentoInvalido(
            IllegalArgumentException ex) {

        ErrorDTO error = new ErrorDTO(
                400,
                ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(error);
    }

    // CONFLICTOS DE DISPONIBILIDAD O ESTADO
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorDTO> manejarConflicto(
            IllegalStateException ex) {

        ErrorDTO error = new ErrorDTO(
                409,
                ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(error);
    }

    // ERRORES CON ESTADO HTTP EXPLICITO
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorDTO> manejarEstadoHTTP(
            ResponseStatusException ex) {

        int estado = ex.getStatusCode().value();

        ErrorDTO error = new ErrorDTO(
                estado,
                ex.getReason());

        return ResponseEntity
                .status(ex.getStatusCode())
                .body(error);
    }

    // ERRORES AL CONSULTAR EVENTOS Y LUGARES
    @ExceptionHandler(FeignException.class)
    public ResponseEntity<ErrorDTO> manejarFeign(
            FeignException ex) {

        if (ex.status() == 404) {

            ErrorDTO error = new ErrorDTO(
                    404,
                    "El recurso solicitado no existe en Eventos y Lugares");

            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(error);
        }

        ErrorDTO error = new ErrorDTO(
                503,
                "No se pudo consultar el microservicio Eventos y Lugares");

        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(error);
    }
}
