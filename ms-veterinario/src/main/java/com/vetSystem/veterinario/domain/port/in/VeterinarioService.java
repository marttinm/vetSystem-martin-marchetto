package com.vetSystem.veterinario.domain.port.in;

import com.vetSystem.veterinario.domain.model.Veterinario;

import java.util.List;

// Puerto de entrada: los casos de uso que el dominio ofrece sobre los veterinarios
public interface VeterinarioService {

    List<Veterinario> findAll();

    Veterinario findById(Long id);

    List<Veterinario> findByEspecialidad(String especialidad);

    Veterinario save(Veterinario veterinario);

    Veterinario update(Long id, Veterinario veterinario);

    void deleteById(Long id);
}
