package com.vetSystem.Exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidacion(MethodArgumentNotValidException ex,
                                                          HttpServletRequest request) {
        String mensaje = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(". "));
        return construir(HttpStatus.BAD_REQUEST, mensaje, request);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoEncontrado(ResourceNotFoundException ex,
                                                             HttpServletRequest request) {
        return construir(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ErrorResponse> handleDuplicado(DuplicateResourceException ex,
                                                          HttpServletRequest request) {
        return construir(HttpStatus.CONFLICT, ex.getMessage(), request);
    }

    @ExceptionHandler(TurnoSuperpuestoException.class)
    public ResponseEntity<ErrorResponse> handleSuperpuesto(TurnoSuperpuestoException ex,
                                                            HttpServletRequest request) {
        return construir(HttpStatus.CONFLICT, ex.getMessage(), request);
    }

    @ExceptionHandler(BusinessRuleException.class)
    public ResponseEntity<ErrorResponse> handleReglaDeNegocio(BusinessRuleException ex,
                                                              HttpServletRequest request) {
        return construir(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage(), request);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleRutaInexistente(NoResourceFoundException ex,
                                                               HttpServletRequest request) {
        return construir(HttpStatus.NOT_FOUND, "La ruta solicitada no existe", request);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleJsonInvalido(HttpMessageNotReadableException ex,
                                                            HttpServletRequest request) {
        return construir(HttpStatus.BAD_REQUEST,
                "El cuerpo de la solicitud no es un JSON valido", request);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMetodoNoPermitido(HttpRequestMethodNotSupportedException ex,
                                                                 HttpServletRequest request) {
        return construir(HttpStatus.METHOD_NOT_ALLOWED,
                "El metodo " + ex.getMethod() + " no esta permitido en esta ruta", request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenerico(Exception ex, HttpServletRequest request) {
        return construir(HttpStatus.INTERNAL_SERVER_ERROR,
                "Ocurrio un error inesperado en el servidor", request);
    }

    private ResponseEntity<ErrorResponse> construir(HttpStatus status, String mensaje,
                                                     HttpServletRequest request) {
        ErrorResponse body = new ErrorResponse(
                LocalDateTime.now(), status.value(), status.getReasonPhrase(),
                mensaje, request.getRequestURI());
        return ResponseEntity.status(status).body(body);
    }
}
