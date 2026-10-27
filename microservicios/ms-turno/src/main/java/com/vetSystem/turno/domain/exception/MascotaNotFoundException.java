package com.vetSystem.turno.domain.exception;

public class MascotaNotFoundException extends RuntimeException {
    public MascotaNotFoundException(Long id) {
        super("Mascota no encontrada: " + id);
    }
}
