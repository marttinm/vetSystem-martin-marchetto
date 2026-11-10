package com.vetSystem.auth.adapter.out.persistence;

import com.vetSystem.auth.domain.model.Usuario;
import com.vetSystem.auth.domain.port.out.UsuarioRepositoryPort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

// Adaptador de salida: Spring Data genera findByUsername y existsByUsername a partir del nombre
@Repository
public interface UsuarioJpaRepository extends JpaRepository<Usuario, Long>, UsuarioRepositoryPort {

    @Override
    Optional<Usuario> findByUsername(String username);

    @Override
    boolean existsByUsername(String username);
}
