package com.vetSystem.Repository;

import com.vetSystem.Entity.Duenio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface DuenioRepository extends JpaRepository<Duenio,Long> {
    boolean existsByDni(String dni);
    boolean existsById(Long id);
    Optional<Duenio> findByEmail(String email);
}
