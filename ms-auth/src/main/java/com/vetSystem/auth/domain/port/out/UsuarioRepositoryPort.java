package com.vetSystem.auth.domain.port.out;

import com.vetSystem.auth.domain.model.Usuario;

import java.util.Optional;

// Puerto de salida: lo que el dominio necesita de la persistencia de usuarios
public interface UsuarioRepositoryPort {

    Optional<Usuario> findByUsername(String username);

    boolean existsByUsername(String username);

    Usuario save(Usuario usuario);
}
