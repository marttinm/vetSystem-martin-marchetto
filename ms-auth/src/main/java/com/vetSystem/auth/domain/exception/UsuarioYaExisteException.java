package com.vetSystem.auth.domain.exception;

public class UsuarioYaExisteException extends RuntimeException {
    public UsuarioYaExisteException(String username) {
        super("Ya existe un usuario con el username " + username);
    }
}
