package com.vetSystem.duenio.domain.port.out;

import com.vetSystem.duenio.domain.model.Duenio;

import java.util.List;
import java.util.Optional;

// Puerto de salida: lo que el dominio necesita de la persistencia, sin saber como se implementa
public interface DuenioRepositoryPort {

    List<Duenio> findAll();

    Optional<Duenio> findById(Long id);

    Duenio save(Duenio duenio);

    void deleteById(Long id);

    boolean existsByEmail(String email);
}
