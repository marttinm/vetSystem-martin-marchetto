package com.vetSystem.turno.domain.exception;

public class ServicioNoDisponibleException extends RuntimeException {
    public ServicioNoDisponibleException(String mensaje) {
        super(mensaje);
    }
}
