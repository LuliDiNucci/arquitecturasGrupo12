
package com.ticketya.compras.exceptions;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import com.ticketya.compras.dtos.ErrorDTO;

import feign.FeignException;

@RestControllerAdvice
public class CompraExceptionHandler {

    // Datos inválidos
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorDTO> manejarDatosInvalidos(
            IllegalArgumentException e) {

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorDTO(400, e.getMessage()));
    }

    // Compra inexistente
    @ExceptionHandler(CompraNoEncontradaException.class)
    public ResponseEntity<ErrorDTO> manejarCompraNoEncontrada(
            CompraNoEncontradaException e) {

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ErrorDTO(404, e.getMessage()));
    }

    // Conflictos con estados de compras o reservas
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorDTO> manejarConflicto(
            IllegalStateException e) {

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(new ErrorDTO(409, e.getMessage()));
    }

    // Recurso inexistente en otro microservicio
    @ExceptionHandler(FeignException.NotFound.class)
    public ResponseEntity<ErrorDTO> manejarRecursoRemotoInexistente(
            FeignException.NotFound e) {

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ErrorDTO(
                        404,
                        "El recurso solicitado no existe en el microservicio externo"));
    }

    // Errores de comunicación con otros microservicios
    @ExceptionHandler(FeignException.class)
    public ResponseEntity<ErrorDTO> manejarErrorFeign(
            FeignException e) {

        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(new ErrorDTO(
                        503,
                        "No se pudo completar la comunicación con el microservicio externo"));
    }

    // Colisiones UNIQUE (reserva, pago idempotente o entrada reintegrada)
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorDTO> manejarDuplicado(
            DataIntegrityViolationException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorDTO(409, "Operación duplicada o en conflicto"));
    }

    @ExceptionHandler(FeignException.Conflict.class)
    public ResponseEntity<ErrorDTO> manejarConflictoRemoto(
            FeignException.Conflict e) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorDTO(409, "El microservicio externo rechazó la operación por conflicto"));
    }

    // JSON inválido o parámetros con tipos incorrectos
    @ExceptionHandler({
            HttpMessageNotReadableException.class,
            MethodArgumentTypeMismatchException.class
    })
    public ResponseEntity<ErrorDTO> manejarSolicitudMalFormada(
            Exception e) {

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorDTO(
                        400,
                        "La solicitud contiene datos inválidos"));
    }
}
