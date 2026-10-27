package com.vetSystem.veterinario.domain.port.out;

import com.vetSystem.veterinario.domain.model.Veterinario;

import java.util.List;
import java.util.Optional;

// Puerto de salida: lo que el dominio necesita de la persistencia, sin saber como se implementa
public interface VeterinarioRepositoryPort {

    List<Veterinario> findAll();

    Optional<Veterinario> findById(Long id);

    List<Veterinario> findByEspecialidad(String especialidad);

    Veterinario save(Veterinario veterinario);

    void deleteById(Long id);
}
