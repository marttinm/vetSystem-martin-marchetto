package com.vetSystem.Repository;

import com.vetSystem.Entity.Duenio;
import com.vetSystem.Entity.Mascota;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MascotaRepository extends JpaRepository<Mascota, Long> {
    Optional<Duenio> findByDuenoId(Long duenoId);
    boolean existsByNombreAndDuenoId(String nombre, Long duenioId);
}
