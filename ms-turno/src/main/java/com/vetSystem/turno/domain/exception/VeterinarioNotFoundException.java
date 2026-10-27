package com.vetSystem.turno.domain.exception;

public class VeterinarioNotFoundException extends RuntimeException {
    public VeterinarioNotFoundException(Long id) {
        super("Veterinario no encontrado: " + id);
    }
}
