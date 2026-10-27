package com.vetSystem.veterinario.adapter.out.persistence;

import com.vetSystem.veterinario.domain.model.Veterinario;
import com.vetSystem.veterinario.domain.port.out.VeterinarioRepositoryPort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

// Adaptador de salida: JpaRepository ya implementa findAll, findById, save y deleteById
// del puerto, y Spring Data genera findByEspecialidad a partir del nombre del metodo
@Repository
public interface VeterinarioJpaRepository extends JpaRepository<Veterinario, Long>, VeterinarioRepositoryPort {

    @Override
    List<Veterinario> findByEspecialidad(String especialidad);
}
