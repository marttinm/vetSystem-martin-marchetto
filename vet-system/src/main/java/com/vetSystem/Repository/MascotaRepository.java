package com.vetSystem.Repository;

import com.vetSystem.Entity.Mascota;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MascotaRepository extends JpaRepository<Mascota, Long> {
    List<Mascota> findByDuenioId(Long duenoId);
    boolean existsByNombreAndDuenioId(String nombre, Long duenioId);
    boolean existsById(Long id);
    long countByDuenioId(Long duenioId);
}
