package com.vetSystem.duenio.adapter.out.persistence;

import com.vetSystem.duenio.domain.model.Duenio;
import com.vetSystem.duenio.domain.port.out.DuenioRepositoryPort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

// Adaptador de salida: JpaRepository ya implementa findAll, findById, save y deleteById
// del puerto, y Spring Data genera existsByEmail a partir del nombre del metodo
@Repository
public interface DuenioJpaRepository extends JpaRepository<Duenio, Long>, DuenioRepositoryPort {

    @Override
    boolean existsByEmail(String email);
}
